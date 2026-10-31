package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * 地表过渡带配置。
 * <p>
 * 在 TFC 的 岩 / 土 / 沙 三类表层交界处，用「多石」变体铺一条 {@code widthMin..widthMax} 格宽的过渡带，
 * 让生硬的交界线变成渐变。
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
 */
public record SurfaceTransitionConfig(
    int widthMin,
    int widthMax,
    int searchDepth,
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
        Codec.BOOL.optionalFieldOf("dirt_to_sand", true).forGetter(SurfaceTransitionConfig::dirtToSand),
        Codec.BOOL.optionalFieldOf("grass_to_sand", true).forGetter(SurfaceTransitionConfig::grassToSand),
        Codec.BOOL.optionalFieldOf("dirt_to_rock", true).forGetter(SurfaceTransitionConfig::dirtToRock),
        Codec.BOOL.optionalFieldOf("grass_to_rock", false).forGetter(SurfaceTransitionConfig::grassToRock),
        Codec.BOOL.optionalFieldOf("sand_to_rock", true).forGetter(SurfaceTransitionConfig::sandToRock)
    ).apply(instance, SurfaceTransitionConfig::new));
}
