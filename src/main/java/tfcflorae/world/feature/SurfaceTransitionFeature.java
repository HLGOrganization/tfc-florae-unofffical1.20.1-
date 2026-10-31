package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * 地表过渡带。
 * <p>
 * TFC 的 岩 / 土 / 沙 三类表层是在 {@code TFCChunkGenerator} 里按气候和岩层逐列判定的硬阈值，
 * 交界处是一条生硬的线。这个地物在表层生成完成之后（{@code #tfc:in_biome/top_layer_modification}）
 * 扫一遍整块 chunk，把交界两侧若干格内的表层换成对应的「多石」变体，形成过渡。
 * <p>
 * 关键点：
 * <ul>
 *   <li>岩石类型从该列下方真实岩层读取（向下最多 {@code search_depth} 格），找不到就跳过该列 ——
 *       不会出现「花岗岩地上长安山岩混石土」</li>
 *   <li>土壤变体 / 沙颜色也取自原地块，只换「石」的部分</li>
 *   <li>宽度按 4 格粒度的位置哈希在 {@code widthMin..widthMax} 之间抖动，
 *       避免出现一条等宽的、同样笔直的带子</li>
 *   <li>水下（上方有流体）一律不动</li>
 * </ul>
 */
public class SurfaceTransitionFeature extends Feature<SurfaceTransitionConfig>
{
    private static final int CHUNK = 16;

    /** {@code tfcflorae:rockier_compact_dirt} 实际存在的 8 种土壤变体，其余变体回退到 loam。 */
    private static final Set<String> ROCKY_SOIL_VARIANTS = Set.of(
        "silt", "loam", "sandy_loam", "silty_loam",
        "fluvisol", "mollisol", "oxisol", "podzol"
    );

    private static final String ROCKY_SAND_PREFIX = "sand/rocky/";
    private static final String ROCKY_SOIL_PREFIX = "rockier_compact_dirt/";
    private static final String ROCK_RAW_PREFIX = "rock/raw/";
    private static final String ROCK_GRAVEL_PREFIX = "rock/gravel/";

    /** 表层类别。 */
    private enum Kind
    {
        GRASS,  // 带草的土
        SOIL,   // 裸土
        SAND,   // 沙
        ROCK,   // 裸岩
        OTHER   // 其它（含水下、非地表方块）
    }

    public SurfaceTransitionFeature(Codec<SurfaceTransitionConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SurfaceTransitionConfig> context)
    {
        final WorldGenLevel level = context.level();
        final SurfaceTransitionConfig config = context.config();
        final BlockPos origin = context.origin();

        final int chunkX = origin.getX() >> 4;
        final int chunkZ = origin.getZ() >> 4;
        final int margin = Math.max(1, config.widthMax());
        final int span = CHUNK + margin * 2;

        // 1. 采样 chunk 本体 + margin 环，得到每列的表层类别
        final Kind[] kinds = new Kind[span * span];
        for (int gx = 0; gx < span; gx++)
        {
            for (int gz = 0; gz < span; gz++)
            {
                kinds[gx * span + gz] = sampleKind(level, (chunkX << 4) + gx - margin, (chunkZ << 4) + gz - margin);
            }
        }

        boolean changed = false;

        // 2. 只处理 chunk 本体，逐列判断是否在交界带内
        for (int dx = 0; dx < CHUNK; dx++)
        {
            for (int dz = 0; dz < CHUNK; dz++)
            {
                final int gx = dx + margin;
                final int gz = dz + margin;
                final Kind kind = kinds[gx * span + gz];
                if (kind == Kind.OTHER || kind == Kind.ROCK)
                {
                    continue;
                }

                final int wx = (chunkX << 4) + dx;
                final int wz = (chunkZ << 4) + dz;

                final BlockPos top = findTop(level, wx, wz);
                if (top == null)
                {
                    continue;
                }

                final BlockState current = level.getBlockState(top);
                final String path = BuiltInRegistries.BLOCK.getKey(current.getBlock()).getPath();

                // 已经是过渡方块，不重复处理
                if (path.startsWith(ROCKY_SAND_PREFIX) || path.startsWith(ROCKY_SOIL_PREFIX))
                {
                    continue;
                }

                // 3. 找交界
                final int w = widthAt(config, wx, wz);
                boolean nearGrass = false;
                boolean nearSoil = false;
                boolean nearSand = false;
                boolean nearRock = false;
                for (int ox = -w; ox <= w; ox++)
                {
                    for (int oz = -w; oz <= w; oz++)
                    {
                        if (ox == 0 && oz == 0)
                        {
                            continue;
                        }
                        final int ngx = gx + ox;
                        final int ngz = gz + oz;
                        if (ngx < 0 || ngx >= span || ngz < 0 || ngz >= span)
                        {
                            continue;
                        }
                        switch (kinds[ngx * span + ngz])
                        {
                            case GRASS -> nearGrass = true;
                            case SOIL -> nearSoil = true;
                            case SAND -> nearSand = true;
                            case ROCK -> nearRock = true;
                            default -> { }
                        }
                    }
                }

                // 4. 按类别 + 相邻类别决定换什么
                final boolean toSand = (nearSoil && config.dirtToSand()) || (nearGrass && config.grassToSand());
                final boolean toRock = (kind == Kind.GRASS ? config.grassToRock() : config.dirtToRock());

                final boolean wantSandSide = kind == Kind.SAND && (toSand || (nearRock && config.sandToRock()));
                final boolean wantSoilSide = (kind == Kind.SOIL && (toSand || (nearRock && toRock)))
                    || (kind == Kind.GRASS && nearRock && config.grassToRock());
                if (!wantSandSide && !wantSoilSide)
                {
                    continue;
                }

                // 5. 读真实岩层
                final String rock = findRock(level, top, config.searchDepth());
                if (rock == null)
                {
                    continue;
                }

                final BlockState replacement;
                if (wantSandSide)
                {
                    final String color = lastSegment(path);
                    replacement = color == null ? null : lookup("tfcflorae:" + ROCKY_SAND_PREFIX + color + "/" + rock);
                }
                else
                {
                    final String variant = normalizeVariant(lastSegment(path));
                    replacement = lookup("tfcflorae:" + ROCKY_SOIL_PREFIX + variant + "/" + rock);
                }

                if (replacement != null && !replacement.is(current.getBlock()))
                {
                    level.setBlock(top, replacement, 2);
                    changed = true;
                }
            }
        }
        return changed;
    }

    /**
     * 找这一列真正的地表方块。地表之上可能压着松散岩石、雪层之类的非地形方块，
     * 所以最多向下找 3 格，取第一个「地形方块」。
     */
    private static BlockPos findTop(WorldGenLevel level, int x, int z)
    {
        final int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        if (surfaceY <= level.getMinBuildHeight())
        {
            return null;
        }
        if (!level.getFluidState(new BlockPos(x, surfaceY + 1, z)).isEmpty())
        {
            return null; // 水下不做
        }
        for (int d = 0; d <= 2; d++)
        {
            final BlockPos pos = new BlockPos(x, surfaceY - d, z);
            if (pos.getY() <= level.getMinBuildHeight())
            {
                break;
            }
            if (classify(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock())) != Kind.OTHER)
            {
                return pos;
            }
        }
        return null;
    }

    /** 只取表层类别，不做岩层查询，用于采样邻域。 */
    private static Kind sampleKind(WorldGenLevel level, int x, int z)
    {
        final BlockPos top = findTop(level, x, z);
        if (top == null)
        {
            return Kind.OTHER;
        }
        return classify(BuiltInRegistries.BLOCK.getKey(level.getBlockState(top).getBlock()));
    }

    private static Kind classify(ResourceLocation id)
    {
        final String ns = id.getNamespace();
        if (!"tfc".equals(ns) && !"tfcflorae".equals(ns))
        {
            return Kind.OTHER;
        }
        final String p = id.getPath();

        if (p.startsWith(ROCK_RAW_PREFIX) || p.startsWith(ROCK_GRAVEL_PREFIX))
        {
            return Kind.ROCK;
        }
        // 已经是过渡方块
        if (p.startsWith(ROCKY_SAND_PREFIX) || p.startsWith(ROCKY_SOIL_PREFIX))
        {
            return Kind.OTHER;
        }
        if (p.startsWith("sand/"))
        {
            return Kind.SAND;
        }
        // 地表覆盖物、植物不是地形方块
        if (p.startsWith("groundcover/") || p.startsWith("plant/"))
        {
            return Kind.OTHER;
        }
        if (p.contains("grass"))
        {
            return Kind.GRASS;
        }
        if (p.startsWith("dirt/")
            || p.startsWith("clay/")
            || p.startsWith("mud/")
            || p.startsWith("coarse_dirt/")
            || p.startsWith("compact_dirt/")
            || p.startsWith("rooted_dirt/")
            || p.startsWith("podzol/")
            || p.startsWith("mycelium_dirt/")
            || p.startsWith("loose_mud/")
            || p.startsWith("packed_mud/")
            || p.startsWith("mossy_packed_mud/"))
        {
            return Kind.SOIL;
        }
        return Kind.OTHER;
    }

    /** 从地表向下找岩层，返回岩石名（如 {@code granite}），找不到返回 null。 */
    private static String findRock(WorldGenLevel level, BlockPos top, int depth)
    {
        for (int d = 1; d <= depth; d++)
        {
            final BlockPos pos = top.below(d);
            if (pos.getY() < level.getMinBuildHeight())
            {
                break;
            }
            final String path = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath();
            if (path.startsWith(ROCK_RAW_PREFIX) && path.indexOf('/', ROCK_RAW_PREFIX.length()) < 0)
            {
                return path.substring(ROCK_RAW_PREFIX.length());
            }
            if (path.startsWith(ROCK_GRAVEL_PREFIX) && path.indexOf('/', ROCK_GRAVEL_PREFIX.length()) < 0)
            {
                return path.substring(ROCK_GRAVEL_PREFIX.length());
            }
        }
        return null;
    }

    private static String lastSegment(String path)
    {
        final int i = path.lastIndexOf('/');
        return i < 0 ? path : path.substring(i + 1);
    }

    private static String normalizeVariant(String variant)
    {
        return variant != null && ROCKY_SOIL_VARIANTS.contains(variant) ? variant : "loam";
    }

    private static BlockState lookup(String id)
    {
        final Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(id));
        return block == Blocks.AIR ? null : block.defaultBlockState();
    }

    /** 这一列用多宽的过渡带：按 4 格粒度的位置哈希在 [widthMin, widthMax] 内取，让带子边缘不齐整。 */
    private static int widthAt(SurfaceTransitionConfig config, int x, int z)
    {
        final int span = config.widthMax() - config.widthMin();
        if (span <= 0)
        {
            return config.widthMin();
        }
        return config.widthMin() + Math.floorMod(hash(x >> 2, z >> 2), span + 1);
    }

    private static int hash(int x, int z)
    {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return h ^ (h >>> 16);
    }
}
