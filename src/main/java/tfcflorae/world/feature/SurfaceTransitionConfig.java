package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
 * </ul>
 * <p>
 * 各开关的含义是「某一侧要不要响应相邻的另一种表层」，默认值对应已经确定的设计：
 * <pre>
 *   土(裸) ↔ 沙   两侧都做        dirt_to_sand = true
 *   土(草) ↔ 沙   只做沙一侧      grass_to_sand = true（草侧没有混石版本，不动）
 *   土(裸) ↔ 岩   只做土一侧      dirt_to_rock = true
 *   土(草) ↔ 岩   整段跳过        grass_to_rock = false
 *   沙     ↔ 岩   只做沙一侧      sand_to_rock = true
 * </pre>
 *
 * @param widthMin         过渡带最小宽度
 * @param widthMax         过渡带最大宽度（每 4 格抖动一次，让边缘不齐整）
 * @param searchDepth      向下找岩层的最大格数，找不到就跳过该列
 * @param thinZoneDivisor  薄地带保护：某一地带的过渡带宽度上限 = 该地带最大深度 / 这个值。
 *                         默认 2 表示「至少留一半原样」，设成 1 等于关掉保护。
 *                         例：3 格宽的沙滩，最大深度 3，上限 = 3/2 = 1，只有贴边那一格变多石沙
 * @param dirtToSand       裸土 ↔ 沙
 * @param grassToSand      草 ↔ 沙（只做沙一侧）
 * @param dirtToRock       裸土 ↔ 岩
 * @param grassToRock      草 ↔ 岩（默认关；打开会把草换成裸混石土，会出现秃圈）
 * @param sandToRock       沙 ↔ 岩
 */
public record SurfaceTransitionConfig(
    int widthMin,
    int widthMax,
    int searchDepth,
    int thinZoneDivisor,
    boolean dirtToSand,
    boolean grassToSand,
    boolean dirtToRock,
    boolean grassToRock,
    boolean sandToRock
) implements FeatureConfiguration
{
    public static final Codec<SurfaceTransitionConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.intRange(1, 8).optionalFieldOf("width_min", 2).forGetter(SurfaceTransitionConfig::widthMin),
        Codec.intRange(1, 8).optionalFieldOf("width_max", 3).forGetter(SurfaceTransitionConfig::widthMax),
        Codec.intRange(1, 32).optionalFieldOf("search_depth", 8).forGetter(SurfaceTransitionConfig::searchDepth),
        Codec.intRange(1, 8).optionalFieldOf("thin_zone_divisor", 2).forGetter(SurfaceTransitionConfig::thinZoneDivisor),
        Codec.BOOL.optionalFieldOf("dirt_to_sand", true).forGetter(SurfaceTransitionConfig::dirtToSand),
        Codec.BOOL.optionalFieldOf("grass_to_sand", true).forGetter(SurfaceTransitionConfig::grassToSand),
        Codec.BOOL.optionalFieldOf("dirt_to_rock", true).forGetter(SurfaceTransitionConfig::dirtToRock),
        Codec.BOOL.optionalFieldOf("grass_to_rock", false).forGetter(SurfaceTransitionConfig::grassToRock),
        Codec.BOOL.optionalFieldOf("sand_to_rock", true).forGetter(SurfaceTransitionConfig::sandToRock)
    ).apply(instance, SurfaceTransitionConfig::new));
}
