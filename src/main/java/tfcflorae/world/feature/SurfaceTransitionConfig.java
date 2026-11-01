package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * 地表过渡带配置。
 * <p>
 * 在 TFC 的 岩 / 土 / 沙 三类表层交界处，用「多石」变体铺一条过渡带，让生硬的交界线变成渐变。
 * <p>
 * 对应的方块族（都已存在，按岩石一一对应，共 29 种岩石）：
 * <ul>
 *   <li>多石土 {@code tfcflorae:rockier_compact_dirt/<土壤变体>/<岩石>} —— 232 个</li>
 *   <li>多石沙 {@code tfcflorae:sand/rocky/<颜色>/<岩石>} —— 203 个</li>
 *   <li>沙草 {@code tfcflorae:sand/{grass,dense_grass,sparse_grass}/<颜色>} —— 21 个</li>
 * </ul>
 * 各开关的含义是「某一侧要不要响应相邻的另一种表层」，默认值对应已经确定的设计：
 * <pre>
 *   土(裸) ↔ 沙   两侧都做        dirt_to_sand  = true
 *   土(草) ↔ 沙   只做沙一侧      grass_to_sand = true（沙一侧铺沙草渐变）
 *   土(裸) ↔ 岩   只做土一侧      dirt_to_rock  = true
 *   土(草) ↔ 岩   整段跳过        grass_to_rock = false
 *   沙     ↔ 岩   只做沙一侧      sand_to_rock  = true
 * </pre>
 *
 * @param widthMin         多石变体的过渡带最小宽度（有抖动）
 * @param widthMax         多石变体的过渡带最大宽度（有抖动）
 * @param searchDepth      向下找岩层的最大格数，找不到就跳过该列
 * @param minWidth         最小地带宽度。某列周围 {@code (2R+1)^2} 格方块范围内（R = width_max/2，至少 1），
 *                         同类地形（沙/土/岩）的列数少于这个值就整列跳过。
 *                         默认 <b>4</b>，即「沙/土/岩的地带最窄处不足 4 格就不生成过渡」——
 *                         一条 1~3 格的河道小沙岸最多只能数到 9，但它是个长条，
 *                         R=1 的邻域里数出来仍然可能不到 4；而 4 格以上的正常地带必定超过。
 *                         注意这个量跟面积无关，只跟「最窄处实际宽度」有关。
 * @param grassRings       沙草渐变铺几环（<b>不抖动</b>）。默认 3，对应
 *                         {@code sand/grass} → {@code sand/dense_grass} → {@code sand/sparse_grass}。
 *                         调大会让最外那一环（稀疏档）继续往外延伸，用来把大沙滩的过渡拉长。
 * @param dirtToSand       裸土 ↔ 沙
 * @param grassToSand      草 ↔ 沙（只做沙一侧，铺沙草渐变）
 * @param dirtToRock       裸土 ↔ 岩
 * @param grassToRock      草 ↔ 岩（默认关；打开会把草换成裸混石土，会出现秃圈）
 * @param sandToRock       沙 ↔ 岩
 * @param rockContact      垂直规则：裸土/沙 直接压在下层岩石/砂砾上时，把紧贴岩石的那一层换成多石变体。
 *                         只换 1 层，草方块不参与。
 * @param skipBiomes       这些群系整段跳过（恶地类风景会被多石沙破坏），默认空。
 *                         实际用 {@code tfcflorae:no_surface_transition} 标签，方便后续增删。
 */
public record SurfaceTransitionConfig(
    int widthMin,
    int widthMax,
    int searchDepth,
    int minWidth,
    int grassRings,
    boolean dirtToSand,
    boolean grassToSand,
    boolean dirtToRock,
    boolean grassToRock,
    boolean sandToRock,
    boolean rockContact,
    Optional<TagKey<Biome>> skipBiomes
) implements FeatureConfiguration
{
    public static final Codec<SurfaceTransitionConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.intRange(1, 8).optionalFieldOf("width_min", 2).forGetter(SurfaceTransitionConfig::widthMin),
        Codec.intRange(1, 8).optionalFieldOf("width_max", 3).forGetter(SurfaceTransitionConfig::widthMax),
        Codec.intRange(1, 32).optionalFieldOf("search_depth", 8).forGetter(SurfaceTransitionConfig::searchDepth),
        Codec.intRange(1, 64).optionalFieldOf("min_width", 4).forGetter(SurfaceTransitionConfig::minWidth),
        Codec.intRange(1, 8).optionalFieldOf("grass_rings", 3).forGetter(SurfaceTransitionConfig::grassRings),
        Codec.BOOL.optionalFieldOf("dirt_to_sand", true).forGetter(SurfaceTransitionConfig::dirtToSand),
        Codec.BOOL.optionalFieldOf("grass_to_sand", true).forGetter(SurfaceTransitionConfig::grassToSand),
        Codec.BOOL.optionalFieldOf("dirt_to_rock", true).forGetter(SurfaceTransitionConfig::dirtToRock),
        Codec.BOOL.optionalFieldOf("grass_to_rock", false).forGetter(SurfaceTransitionConfig::grassToRock),
        Codec.BOOL.optionalFieldOf("sand_to_rock", true).forGetter(SurfaceTransitionConfig::sandToRock),
        Codec.BOOL.optionalFieldOf("rock_contact", true).forGetter(SurfaceTransitionConfig::rockContact),
        TagKey.codec(Registries.BIOME).optionalFieldOf("skip_biomes").forGetter(SurfaceTransitionConfig::skipBiomes)
    ).apply(instance, SurfaceTransitionConfig::new));
}
