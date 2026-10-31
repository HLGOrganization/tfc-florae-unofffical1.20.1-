package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
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
 * 扫一遍整块 chunk，沿着真正的接触线往两侧铺过渡方块。
 * <p>
 * 一共有三条独立规则：
 * <ol>
 *   <li><b>水平交界</b>：只看<b>前后左右</b>四邻（不含对角），两侧都是地形方块且类别不同才算接触。
 *       水下/其它方块不参与，所以「沙滩挨着海」不算接触。然后沿同类方块做 BFS 得到「离接触线几格」，
 *       并用同类连通块的最大深度做薄地带保护（至少留一半原样）。</li>
 *   <li><b>草 ↔ 沙</b>：沙一侧铺沙草渐变，由密到疏 ——
 *       {@code sand/grass}（顶面 100% 草）→ {@code sand/dense_grass}（41.8%）→ {@code sand/sparse_grass}（10.2%）。
 *       这条<b>优先于</b>多石沙：同时挨着草和岩石时用沙草。</li>
 *   <li><b>垂直贴岩</b>：裸土/沙 直接压在岩石/砂砾上时，把紧贴岩石的那一层换成多石变体（只换 1 层）。</li>
 * </ol>
 * 岩石类型从该列下方真实岩层读取，找不到就跳过该列 —— 不会出现「花岗岩地上长安山岩混石土」。
 * 恶地类群系通过 {@code tfcflorae:no_surface_transition} 标签整段跳过。
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
    private static final String SAND_GRASS_PREFIX = "sand/grass/";
    private static final String SAND_DENSE_GRASS_PREFIX = "sand/dense_grass/";
    private static final String SAND_SPARSE_GRASS_PREFIX = "sand/sparse_grass/";
    private static final String ROCK_RAW_PREFIX = "rock/raw/";
    private static final String ROCK_GRAVEL_PREFIX = "rock/gravel/";

    /** 表层类别。 */
    private enum Kind
    {
        GRASS,  // 带草的土
        SOIL,   // 裸土
        SAND,   // 沙
        ROCK,   // 裸岩
        OTHER   // 其它（含水下、非地表方块、已过渡方块）
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
        // 采样环要比过渡带再宽 1 格，否则 chunk 边缘的接触线判断不到
        final int margin = Math.max(1, config.widthMax()) + 1;
        final int span = CHUNK + margin * 2;
        final int total = span * span;
        final TagKey<Biome> skip = config.skipBiomes().orElse(null);

        // ---------- 1. 采样表层类别 ----------
        final Kind[] kinds = new Kind[total];
        for (int gx = 0; gx < span; gx++)
        {
            for (int gz = 0; gz < span; gz++)
            {
                kinds[gx * span + gz] = sampleKind(level, (chunkX << 4) + gx - margin, (chunkZ << 4) + gz - margin);
            }
        }

        // ---------- 2. 找接触线（只认前后左右） ----------
        final int[] dist = new int[total];
        final int[] contacts = new int[total];
        Arrays.fill(dist, -1);

        final ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int gx = 0; gx < span; gx++)
        {
            for (int gz = 0; gz < span; gz++)
            {
                final int idx = gx * span + gz;
                final Kind self = kinds[idx];
                if (self == Kind.OTHER)
                {
                    continue;
                }
                int mask = 0;
                if (gx > 0 && different(self, kinds[idx - span])) mask |= bitOf(kinds[idx - span]);
                if (gx < span - 1 && different(self, kinds[idx + span])) mask |= bitOf(kinds[idx + span]);
                if (gz > 0 && different(self, kinds[idx - 1])) mask |= bitOf(kinds[idx - 1]);
                if (gz < span - 1 && different(self, kinds[idx + 1])) mask |= bitOf(kinds[idx + 1]);
                if (mask != 0)
                {
                    dist[idx] = 1;
                    contacts[idx] = mask;
                    queue.add(idx);
                }
            }
        }

        // ---------- 3. 沿同类方块向外扩散 ----------
        while (!queue.isEmpty())
        {
            final int cur = queue.poll();
            final int gx = cur / span;
            final int gz = cur % span;
            final Kind self = kinds[cur];
            final int nd = dist[cur] + 1;
            if (gx > 0) relax(kinds, dist, contacts, queue, cur - span, self, nd, contacts[cur]);
            if (gx < span - 1) relax(kinds, dist, contacts, queue, cur + span, self, nd, contacts[cur]);
            if (gz > 0) relax(kinds, dist, contacts, queue, cur - 1, self, nd, contacts[cur]);
            if (gz < span - 1) relax(kinds, dist, contacts, queue, cur + 1, self, nd, contacts[cur]);
        }

        // ---------- 4. 同类连通块 + 每块最大深度 ----------
        final int[] comp = new int[total];
        Arrays.fill(comp, -1);
        final int[] compMax = new int[total];
        int compCount = 0;
        for (int i = 0; i < total; i++)
        {
            if (kinds[i] == Kind.OTHER || comp[i] != -1)
            {
                continue;
            }
            final int id = compCount++;
            int max = 0;
            final ArrayDeque<Integer> flood = new ArrayDeque<>();
            flood.add(i);
            comp[i] = id;
            while (!flood.isEmpty())
            {
                final int cur = flood.poll();
                max = Math.max(max, dist[cur]);
                final int gx = cur / span;
                final int gz = cur % span;
                if (gx > 0) floodInto(kinds, comp, flood, cur - span, kinds[cur], id);
                if (gx < span - 1) floodInto(kinds, comp, flood, cur + span, kinds[cur], id);
                if (gz > 0) floodInto(kinds, comp, flood, cur - 1, kinds[cur], id);
                if (gz < span - 1) floodInto(kinds, comp, flood, cur + 1, kinds[cur], id);
            }
            compMax[id] = max;
        }

        // ---------- 5. 逐列处理 chunk 本体 ----------
        boolean changed = false;
        for (int dx = 0; dx < CHUNK; dx++)
        {
            for (int dz = 0; dz < CHUNK; dz++)
            {
                final int gx = dx + margin;
                final int gz = dz + margin;
                final int idx = gx * span + gz;

                final int wx = (chunkX << 4) + dx;
                final int wz = (chunkZ << 4) + dz;

                final BlockPos top = findTop(level, wx, wz);
                if (top == null)
                {
                    continue;
                }
                // 群系白名单：恶地类整段跳过
                if (skip != null && level.getBiome(top).is(skip))
                {
                    continue;
                }

                final BlockState current = level.getBlockState(top);
                final Kind kind = classify(BuiltInRegistries.BLOCK.getKey(current.getBlock()));
                if (kind == Kind.OTHER || kind == Kind.ROCK)
                {
                    continue;
                }

                // 向下扫一遍：同时拿到岩层名 + 紧贴岩层上方那一块
                String rock = null;
                BlockPos aboveRock = null;
                for (int d = 1; d <= config.searchDepth(); d++)
                {
                    final BlockPos q = top.below(d);
                    if (q.getY() < level.getMinBuildHeight())
                    {
                        break;
                    }
                    final String name = rockName(pathOf(level, q));
                    if (name != null)
                    {
                        rock = name;
                        aboveRock = top.below(d - 1);
                        break;
                    }
                }
                if (rock == null)
                {
                    continue; // 这一列探不到岩层，两条规则都没法确定岩石类型
                }

                // ---------- 规则一/二：水平交界 ----------
                boolean done = false;
                final int depth = dist[idx];
                if (depth > 0 && kind != Kind.OTHER)
                {
                    final int cap = compMax[comp[idx]] / config.thinZoneDivisor();
                    if (cap > 0 && depth <= Math.min(widthAt(config, wx, wz), cap))
                    {
                        done = applyHorizontal(level, config, top, current, kind, contacts[idx], depth, rock);
                    }
                }
                if (done)
                {
                    changed = true;
                    continue;
                }

                // ---------- 规则三：垂直贴岩 ----------
                if (config.rockContact())
                {
                    final BlockState above = level.getBlockState(aboveRock);
                    final Kind aboveKind = classify(BuiltInRegistries.BLOCK.getKey(above.getBlock()));
                    if (aboveKind == Kind.SOIL || aboveKind == Kind.SAND)
                    {
                        final String abovePath = pathOf(level, aboveRock);
                        final BlockState repl = aboveKind == Kind.SAND
                            ? lookup("tfcflorae:" + ROCKY_SAND_PREFIX + lastSegment(abovePath) + "/" + rock)
                            : lookup("tfcflorae:" + ROCKY_SOIL_PREFIX + normalizeVariant(lastSegment(abovePath)) + "/" + rock);
                        if (repl != null && !repl.is(above.getBlock()))
                        {
                            level.setBlock(aboveRock, repl, 3);
                            changed = true;
                        }
                    }
                }
            }
        }
        return changed;
    }

    /**
     * 水平交界规则。返回是否真的改了方块。
     * <p>
     * 沙一侧的优先级：<b>挨着草</b> ＞ 挨着土/岩。挨着草时铺沙草渐变（由密到疏），
     * 否则铺多石沙。
     */
    private static boolean applyHorizontal(WorldGenLevel level, SurfaceTransitionConfig config, BlockPos top,
                                           BlockState current, Kind kind, int mask, int depth, String rock)
    {
        final String path = BuiltInRegistries.BLOCK.getKey(current.getBlock()).getPath();
        final boolean touchGrass = (mask & bitOf(Kind.GRASS)) != 0;
        final boolean touchSoil = (mask & bitOf(Kind.SOIL)) != 0;
        final boolean touchSand = (mask & bitOf(Kind.SAND)) != 0;
        final boolean touchRock = (mask & bitOf(Kind.ROCK)) != 0;

        BlockState replacement = null;
        if (kind == Kind.SAND)
        {
            final String color = lastSegment(path);
            if (touchGrass && config.grassToSand())
            {
                // 沙草渐变：1 格最密（全草），越远越疏
                replacement = lookup("tfcflorae:" + sandGrassFamily(depth) + color);
            }
            else if ((touchSoil && config.dirtToSand()) || (touchRock && config.sandToRock()))
            {
                replacement = lookup("tfcflorae:" + ROCKY_SAND_PREFIX + color + "/" + rock);
            }
        }
        else if (kind == Kind.SOIL)
        {
            if ((touchSand && config.dirtToSand()) || (touchRock && config.dirtToRock()))
            {
                replacement = lookup("tfcflorae:" + ROCKY_SOIL_PREFIX + normalizeVariant(lastSegment(path)) + "/" + rock);
            }
        }
        else if (kind == Kind.GRASS)
        {
            if (touchRock && config.grassToRock())
            {
                replacement = lookup("tfcflorae:" + ROCKY_SOIL_PREFIX + normalizeVariant(lastSegment(path)) + "/" + rock);
            }
        }

        if (replacement == null || replacement.is(current.getBlock()))
        {
            return false;
        }
        level.setBlock(top, replacement, 3);
        return true;
    }

    /**
     * 沙草渐变的三档。密度实测（顶面不透明覆盖率）：
     * {@code sand/grass} = 100%（等同 {@code tfc:grass_top}）、
     * {@code sand/dense_grass} = 41.8%、
     * {@code sand/sparse_grass} = 10.2%。
     * 所以 1 格用 grass 最密，3 格及以外用 sparse_grass 最疏。
     */
    private static String sandGrassFamily(int depth)
    {
        if (depth <= 1)
        {
            return SAND_GRASS_PREFIX;
        }
        return depth == 2 ? SAND_DENSE_GRASS_PREFIX : SAND_SPARSE_GRASS_PREFIX;
    }

    // ------------------------------------------------------------------ 图遍历

    private static void relax(Kind[] kinds, int[] dist, int[] contacts, ArrayDeque<Integer> queue, int next, Kind self, int nd, int mask)
    {
        if (kinds[next] != self || dist[next] != -1)
        {
            return;
        }
        dist[next] = nd;
        contacts[next] = mask;
        queue.add(next);
    }

    private static void floodInto(Kind[] kinds, int[] comp, ArrayDeque<Integer> queue, int next, Kind self, int id)
    {
        if (kinds[next] != self || comp[next] != -1)
        {
            return;
        }
        comp[next] = id;
        queue.add(next);
    }

    private static boolean different(Kind self, Kind other)
    {
        return other != Kind.OTHER && other != self;
    }

    private static int bitOf(Kind kind)
    {
        return switch (kind)
        {
            case GRASS -> 1;
            case SOIL -> 2;
            case SAND -> 4;
            case ROCK -> 8;
            default -> 0;
        };
    }

    // ------------------------------------------------------------------ 取样

    /**
     * 找这一列真正的地表方块。
     * 高度图上方向上容错 2 格、向下找 4 格，取第一个「地形方块」——
     * 这样压在地表上的松散岩石、雪层、草叶、树叶都会被跳过。
     */
    private static BlockPos findTop(WorldGenLevel level, int x, int z)
    {
        final int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        for (int d = 2; d >= -4; d--)
        {
            final int y = surfaceY + d;
            if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight())
            {
                continue;
            }
            final BlockPos pos = new BlockPos(x, y, z);
            if (classify(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock())) == Kind.OTHER)
            {
                continue;
            }
            if (!level.getFluidState(pos.above()).isEmpty())
            {
                return null; // 水下不做
            }
            return pos;
        }
        return null;
    }

    private static Kind sampleKind(WorldGenLevel level, int x, int z)
    {
        final BlockPos top = findTop(level, x, z);
        return top == null ? Kind.OTHER : classify(BuiltInRegistries.BLOCK.getKey(level.getBlockState(top).getBlock()));
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
        // 已经是过渡方块，不重复处理
        if (p.startsWith(ROCKY_SAND_PREFIX)
            || p.startsWith(ROCKY_SOIL_PREFIX)
            || p.startsWith(SAND_GRASS_PREFIX)
            || p.startsWith(SAND_DENSE_GRASS_PREFIX)
            || p.startsWith(SAND_SPARSE_GRASS_PREFIX))
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

    private static String pathOf(WorldGenLevel level, BlockPos pos)
    {
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath();
    }

    /** 是岩层就返回岩石名（如 {@code granite}），否则 null。 */
    private static String rockName(String path)
    {
        if (path.startsWith(ROCK_RAW_PREFIX) && path.indexOf('/', ROCK_RAW_PREFIX.length()) < 0)
        {
            return path.substring(ROCK_RAW_PREFIX.length());
        }
        if (path.startsWith(ROCK_GRAVEL_PREFIX) && path.indexOf('/', ROCK_GRAVEL_PREFIX.length()) < 0)
        {
            return path.substring(ROCK_GRAVEL_PREFIX.length());
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
