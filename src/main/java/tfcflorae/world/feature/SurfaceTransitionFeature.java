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

    /** 采样时要额外记录的信息：每列的类别、实际地表方块、以及「草方块盖在沙上」这个标记。 */
    private static final class Sample
    {
        final Kind kind;
        /** 这一列真正要改的方块（「草方块盖在沙上」时是下面那块沙，不是两块草） */
        final BlockPos pos;
        final BlockState state;
        /** 地表是草方块、但正下方是沙 —— 这种列归类成 SAND，改的是底下那块沙 */
        final boolean grassOverSand;

        Sample(Kind kind, BlockPos pos, BlockState state, boolean grassOverSand)
        {
            this.kind = kind;
            this.pos = pos;
            this.state = state;
            this.grassOverSand = grassOverSand;
        }
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
        // 采样环要在 chunk 外留够余量：过渡带本身、最小宽度检测的半径，再各加 1 格容错。
        // 否则 chunk 边缘的接触线判断不到，最小宽度也数不准。
        final int minHalf = (Math.max(4, config.minWidth()) - 1) / 2;
        final int margin = Math.max(config.widthMax(), minHalf) + 1;
        final int span = CHUNK + margin * 2;
        final int total = span * span;
        final TagKey<Biome> skip = config.skipBiomes().orElse(null);

        // ---------- 1. 采样表层类别 ----------
        final Kind[] kinds = new Kind[total];
        final Sample[] samples = new Sample[total];
        for (int gx = 0; gx < span; gx++)
        {
            for (int gz = 0; gz < span; gz++)
            {
                final Sample s = sample(level, (chunkX << 4) + gx - margin, (chunkZ << 4) + gz - margin);
                samples[gx * span + gz] = s;
                kinds[gx * span + gz] = s == null ? Kind.OTHER : s.kind;
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

        // ---------- 4. 同类连通块 ----------
        final int[] comp = new int[total];
        Arrays.fill(comp, -1);
        int compCount = 0;
        for (int i = 0; i < total; i++)
        {
            if (kinds[i] == Kind.OTHER || comp[i] != -1)
            {
                continue;
            }
            final int id = compCount++;
            final ArrayDeque<Integer> flood = new ArrayDeque<>();
            flood.add(i);
            comp[i] = id;
            while (!flood.isEmpty())
            {
                final int cur = flood.poll();
                final int gx = cur / span;
                final int gz = cur % span;
                if (gx > 0) floodInto(kinds, comp, flood, cur - span, kinds[cur], id);
                if (gx < span - 1) floodInto(kinds, comp, flood, cur + span, kinds[cur], id);
                if (gz > 0) floodInto(kinds, comp, flood, cur - 1, kinds[cur], id);
                if (gz < span - 1) floodInto(kinds, comp, flood, cur + 1, kinds[cur], id);
            }
        }

        // 每列的「同类地带的格数」：在 (2R+1)^2 的方块邻域里数同类的列。
        // R 取宽度上限的一半（至少 1）—— 只关心「左右各 R 格内还有没有同类」，不是整块连通块的面积。
        // 这就是「沙/土/岩的地带宽度不足就不生成过渡」的判定依据：
        // 一条 1~3 格的河道小沙岸周围全是草，数出来的上限只有 3^2 = 9，达不到 min_cells，于是整片跳过。
        // 注意这个量跟面积无关、跟「最窄处实际宽度」有关：只有无限宽的地带才能数满 (2R+1)^2。
        final int cap2 = config.minWidth() + 1;
        final int[] narrowStats = new int[total];
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
                int best = 0;
                for (int axis = 0; axis < 2; axis++)
                {
                    int len = 1;
                    for (int sign = -1; sign <= 1; sign += 2)
                    {
                        for (int k = 1; k <= cap2; k++)
                        {
                            final int nx = gx + (axis == 0 ? sign * k : 0);
                            final int nz = gz + (axis == 1 ? sign * k : 0);
                            if (nx < 0 || nz < 0 || nx >= span || nz >= span)
                            {
                                break;
                            }
                            if (kinds[nx * span + nz] != self)
                            {
                                break;
                            }
                            len++;
                        }
                    }
                    best = axis == 0 ? len : Math.min(best, len);
                }
                narrowStats[idx] = best;
            }
        }

        // ---------- 5. 逐列处理 chunk 本体 ----------
        boolean changed = false;
        final int minWidth = config.minWidth();
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

                final Sample self = samples[idx];
                if (self == null || self.kind == Kind.OTHER || self.kind == Kind.ROCK)
                {
                    continue;
                }

                // 地带太窄就不生成，避免把小河道两侧的小沙岸整个盖住。
                // 判定用「同类地带的最窄跨度」，不是整块连通块的面积。
                if (narrowStats[idx] < minWidth)
                {
                    continue;
                }

                // 向下扫一遍：同时拿到岩层名 + 紧贴岩层上方那一块。
                // 对「草方块压在沙上」的列，从底下那块沙开始扫，草方块不参与。
                String rock = null;
                BlockPos aboveRock = null;
                for (int d = 1; d <= config.searchDepth(); d++)
                {
                    final BlockPos q = self.pos.below(d);
                    if (q.getY() < level.getMinBuildHeight())
                    {
                        break;
                    }
                    final String name = rockName(pathOf(level, q));
                    if (name != null)
                    {
                        rock = name;
                        aboveRock = self.pos.below(d - 1);
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
                if (depth > 0)
                {
                    done = applyHorizontal(level, config, self, contacts[idx], depth, wx, wz, rock);
                }
                if (done)
                {
                    changed = true;
                    continue;
                }

                // ---------- 规则三：垂直贴岩 ----------
                if (config.rockContact() && !aboveRock.equals(self.pos))
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
     * 沙一侧的优先级：<b>挨着草</b> ＞ 挨着土/岩。挨着草时铺沙草渐变（由密到疏），否则铺多石沙。
     * <p>
     * <b>沙草渐变不参与宽度抖动</b>：环数由 {@code grass_rings} 定死，只要地带够厚就整圈铺满。
     * 之前给它加了「每 4 格抖一次」的宽度，结果最外那一环（{@code sparse_grass}）只在宽度=3 的格子里出现，
     * 沿沙滩看是「4 格有 4 格无」的虚线，也就是肉眼看到的断层。
     * 多石变体没有渐变，抖动不会破坏图案，所以照旧保留抖动。
     */
    private static boolean applyHorizontal(WorldGenLevel level, SurfaceTransitionConfig config, Sample self,
                                           int mask, int depth, int wx, int wz, String rock)
    {
        final Kind kind = self.kind;
        final String path = BuiltInRegistries.BLOCK.getKey(self.state.getBlock()).getPath();
        final boolean touchGrass = (mask & bitOf(Kind.GRASS)) != 0;
        final boolean touchSoil = (mask & bitOf(Kind.SOIL)) != 0;
        final boolean touchSand = (mask & bitOf(Kind.SAND)) != 0;
        final boolean touchRock = (mask & bitOf(Kind.ROCK)) != 0;

        // ---------- 沙草渐变：固定环数，不抖动 ----------
        // 沙草方块「下方必须是沙」，所以这一支只在 self 本身就是沙方块时成立。
        // 若这一列是「草方块压在沙上」，self.pos 指向底下那块沙，「顶上那块草」原样保留 ——
        // 绝不会出现「草方块上面再叠一个沙草方块」。
        if (kind == Kind.SAND && touchGrass && config.grassToSand() && depth <= config.grassRings())
        {
            final BlockState sandGrass = lookup("tfcflorae:" + sandGrassFamily(depth) + lastSegment(path));
            if (sandGrass != null && !sandGrass.is(self.state.getBlock()))
            {
                level.setBlock(self.pos, sandGrass, 3);
                return true;
            }
            return false;
        }

        // ---------- 多石变体：保留宽度抖动，让边缘不齐整 ----------
        final int rockyWidth = widthAt(config, wx, wz);
        if (depth > rockyWidth)
        {
            return false;
        }

        BlockState replacement = null;
        if (kind == Kind.SAND)
        {
            if ((touchSoil && config.dirtToSand()) || (touchRock && config.sandToRock()))
            {
                replacement = lookup("tfcflorae:" + ROCKY_SAND_PREFIX + lastSegment(path) + "/" + rock);
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

        if (replacement == null || replacement.is(self.state.getBlock()))
        {
            return false;
        }
        level.setBlock(self.pos, replacement, 3);
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

    /**
     * 采样一列：类别 + 真正要改的方块。
     * <p>
     * 特殊处理「<b>草方块盖在沙上</b>」：TFC 的沙滩最上两层是草方块、下面是沙，这是原生状态。
     * 这种列按 <b>SAND</b> 归类，改的是<b>底下那块沙</b>，绝不改草方块 ——
     * 否则会刷出「草方块上面再叠一个沙草方块」的假方块，那是错的。
     */
    private static Sample sample(WorldGenLevel level, int x, int z)
    {
        final BlockPos top = findTop(level, x, z);
        if (top == null)
        {
            return null;
        }
        final Kind kind = classify(BuiltInRegistries.BLOCK.getKey(level.getBlockState(top).getBlock()));
        if (kind == Kind.GRASS)
        {
            final BlockPos below = top.below();
            final BlockState belowState = level.getBlockState(below);
            if (below.getY() >= level.getMinBuildHeight()
                && level.getFluidState(top).isEmpty()
                && classify(BuiltInRegistries.BLOCK.getKey(belowState.getBlock())) == Kind.SAND)
            {
                // 草方块压在沙上：把「沙」当成这一列的表层
                return new Sample(Kind.SAND, below, belowState, true);
            }
        }
        return new Sample(kind, top, level.getBlockState(top), false);
    }

    /** 兼容旧调用：只要类别。 */
    private static Kind sampleKind(WorldGenLevel level, int x, int z)
    {
        final Sample s = sample(level, x, z);
        return s == null ? Kind.OTHER : s.kind;
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
