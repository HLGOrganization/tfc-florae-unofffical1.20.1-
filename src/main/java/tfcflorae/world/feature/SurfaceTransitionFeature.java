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

    /**
     * 我们真正做了多石变体的岩石：TFC 原版 20 种 + 本模组自己的 10 种（含 {@code tuff}）。
     * <p>
     * {@code rockier_compact_dirt} 与 {@code sand/rocky} 两侧都有这 30 种。
     * tuff 的整套岩石方块（raw/gravel/cobble…）虽已移除、改由 TFE 提供，
     * 但 tuff 仍会生成在世界表层（{@code tfe:rock/raw/tuff}），所以它的多石变体照常注册，
     * 贴图直接引用 {@code tfe:block/rock/raw/tuff}。不在这张表里的岩石就跳过该列，
     * 硬套只会刷出「别的岩石地上长花岗岩混石土」。
     */
    private static final Set<String> ROCKY_ROCKS = Set.of(
        "andesite", "basalt", "boninite", "carbonatite", "chalk", "chert", "claystone",
        "conglomerate", "dacite", "diorite", "dolomite", "gabbro", "gneiss", "granite",
        "jaspillite", "laterite", "limestone", "marble", "mudstone", "phyllite", "porphyry",
        "quartzite", "red_granite", "rhyolite", "schist", "shale", "slate", "soapstone",
        "wackestone", "tuff"
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
                if (gx > 0 && needsPair(self, kinds[idx - span])) mask |= bitOf(kinds[idx - span]);
                if (gx < span - 1 && needsPair(self, kinds[idx + span])) mask |= bitOf(kinds[idx + span]);
                if (gz > 0 && needsPair(self, kinds[idx - 1])) mask |= bitOf(kinds[idx - 1]);
                if (gz < span - 1 && needsPair(self, kinds[idx + 1])) mask |= bitOf(kinds[idx + 1]);
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

        // 每列「在较长方向上有多长」：这是过渡带会不会把整块地带吞掉的依据。
        // 为什么不能只量「垂直接触线方向的厚度」：海滩天然是垂直方向只有 2~3 格，
        // 那样一量，所有沙列都被 min_width 挡掉 —— 实测存档里 sand/grass、
        // sand/dense_grass、sand/sparse_grass、sand/rocky 全是 0，而多石土正常
        // （土的长边很长，所以只有沙被全灭）。这正是「沙草过渡不生效」的根因。
        // 现在取两个轴向里较长的那个：薄而长的海滩能通过，方块状的小沙岸
        // （两个方向都短）仍然被拦下，不会把河道两侧的小沙岸整个盖住。
        final int[] thickStats = new int[total];
        {
            // 最近边界的反方向：BFS 只沿同类扩散，所以「上一层」就在来的方向上。
            // 这里改用一个更直接的度量：沿 BFS 深度增加的方向计数，
            // 即「该列到同类方块中心的厚度」。用 dist 无法直接得到，改为逐列实测四个方向，
            // 取「垂直接触线」的那个轴 —— 接触线方向由 contacts 掩码决定。
            final int maxCheck = Math.min(16, config.minWidth() + 8);
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
                    // 两个轴向都量，取较长者。只要地带在任一方向够长，
                    // 2~3 格宽的海滩 / 薄沙层就不会被过渡带整条吞掉。
                    final int tx = measure(kinds, span, gx, gz, self, 0, maxCheck);
                    final int tz = measure(kinds, span, gx, gz, self, 1, maxCheck);
                    thickStats[idx] = Math.max(tx, tz);
                }
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
                // 判定用「垂直接触线方向的厚度」，不是整块连通块的面积、也不是最窄跨度。
                if (thickStats[idx] < minWidth)
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
                    final String name = resolveRock(rockName(pathOf(level, q)));
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
                        BlockState repl = null;
                        if (aboveKind == Kind.SAND)
                        {
                            repl = lookup("tfcflorae:" + ROCKY_SAND_PREFIX + lastSegment(abovePath) + "/" + rock);
                        }
                        else
                        {
                            // 没有对应多石变体的土壤（如 bog_iron）不做，不回退成 loam
                            final String v = normalizeVariant(lastSegment(abovePath));
                            if (v != null)
                            {
                                repl = lookup("tfcflorae:" + ROCKY_SOIL_PREFIX + v + "/" + rock);
                            }
                        }
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
                // 没有对应多石变体的土壤（如 bog_iron）不做，不回退成 loam
                final String v = normalizeVariant(lastSegment(path));
                if (v != null)
                {
                    replacement = lookup("tfcflorae:" + ROCKY_SOIL_PREFIX + v + "/" + rock);
                }
            }
        }
        else if (kind == Kind.GRASS)
        {
            if (touchRock && config.grassToRock())
            {
                final String v = normalizeVariant(lastSegment(path));
                if (v != null)
                {
                    replacement = lookup("tfcflorae:" + ROCKY_SOIL_PREFIX + v + "/" + rock);
                }
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

    /** 沿某个轴（0 = x，1 = z）数同类连续长度，含自身，最多数到 max 格。 */
    private static int measure(Kind[] kinds, int span, int gx, int gz, Kind self, int axis, int max)
    {
        int len = 1;
        for (int sign = -1; sign <= 1; sign += 2)
        {
            for (int k = 1; k <= max; k++)
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
        return len;
    }

    private static boolean different(Kind self, Kind other)
    {
        return other != Kind.OTHER && other != self;
    }

    /**
     * 这一类地形会不会「因为」邻接另一类地形而需要改变。
     * <p>
     * 不能直接用「类别不同」：草地挨着岩石也会判成接触，于是 {@code dist} 被点亮，
     * 但 {@code grass_to_rock} 默认关，草分支什么都不做 —— 结果就是草侧整片没有过渡，
     * 而且还会把「草 ↔ 沙」的沙侧触发挡在后面（因为沙侧靠的是同一张 {@code dist} 图）。
     * <p>
     * 这里按已确认的设计表逐对声明：
     * <pre>
     *   草 需要 沙   ← 关键：草侧必须感知沙，沙草渐变才会被触发
     *   土 需要 沙 / 岩
     *   沙 需要 草 / 土 / 岩
     *   岩 不需要任何（岩侧永远不动）
     * </pre>
     */
    private static boolean needsPair(Kind self, Kind other)
    {
        if (other == Kind.OTHER || other == self)
        {
            return false;
        }
        return switch (self)
        {
            case GRASS -> other == Kind.SAND;
            case SOIL -> other == Kind.SAND || other == Kind.ROCK;
            case SAND -> other == Kind.GRASS || other == Kind.SOIL || other == Kind.ROCK;
            case ROCK -> false;
            default -> false;
        };
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
        // 必须把 tfe 也算进来！TerraFirmaEarth 的新土壤草方块用的是 tfe: 命名空间
        // （tfe:grass/fluvisol 等），只认 tfc / tfcflorae 会把整片 TFE 新土壤地表当成 OTHER，
        // 那一带上就完全不会有任何过渡。
        if (!"tfc".equals(ns) && !"tfcflorae".equals(ns) && !"tfe".equals(ns))
        {
            return Kind.OTHER;
        }
        final String p = id.getPath();

        if (p.startsWith(ROCK_RAW_PREFIX) || p.startsWith(ROCK_GRAVEL_PREFIX))
        {
            // tfe:rock/raw/tuff_slab 这类不是整块岩石
            if (p.contains("slab") || p.contains("stairs") || p.contains("wall"))
            {
                return Kind.OTHER;
            }
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
        // 腐殖层（TFE 的「X腐殖层」）按草处理 —— 它是有机质表土层，视觉上就是草皮那种质感，
        // 归成 SOIL 会在接沙时被换成多石土，反而更不对。
        // 注意 duff / clay_duff 两个都要，别漏 clay_duff。
        if (p.startsWith("duff/") || p.startsWith("clay_duff/"))
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

    /**
     * 这一列的岩石名 → 我们模组里对应的多石方块岩石名。
     * <p>
     * 我们只给 TFC 的岩石做了 {@code rockier_compact_dirt/*} 和 {@code sand/rocky/*}。
     * TFE 的 {@code tuff} 也有 {@code tfe:rock/raw/tuff}，但我们没做 tuff 的多石变体，
     * 所以这里返回 null —— 该列跳过，不会刷出「tuff 地上长花岗岩混石土」。
     */
    private static String resolveRock(String raw)
    {
        return raw != null && ROCKY_ROCKS.contains(raw) ? raw : null;
    }

    private static String lastSegment(String path)
    {
        final int i = path.lastIndexOf('/');
        return i < 0 ? path : path.substring(i + 1);
    }

    /**
     * 把土壤变体归一到我们有 {@code rockier_compact_dirt} 的那 8 种之一。
     * <p>
     * 没有对应多石变体的（目前只有 {@code bog_iron}）返回 {@code null} ——
     * 该列跳过，<b>不回退成 loam</b>。回退会刷出「沼铁土变成壤土」这种错误方块，
     * 宁可不做过渡。
     */
    private static String normalizeVariant(String variant)
    {
        return variant != null && ROCKY_SOIL_VARIANTS.contains(variant) ? variant : null;
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
