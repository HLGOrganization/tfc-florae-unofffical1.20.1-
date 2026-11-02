package tfcflorae.world.feature;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * 石柱 / 岩柱配置。
 * <p>
 * radius = 0 时是单根立柱（对应 1.18 的 {@code rock_pillar}）；
 * radius &gt; 0 时成片生成（对应 {@code rock_column}）。
 */
public record RockColumnFeatureConfig(List<BlockState> blocks, IntProvider height, IntProvider radius, float density) implements FeatureConfiguration
{
    public static final Codec<RockColumnFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockState.CODEC.listOf().fieldOf("blocks").forGetter(RockColumnFeatureConfig::blocks),
        IntProvider.codec(1, 32).fieldOf("height").forGetter(RockColumnFeatureConfig::height),
        IntProvider.codec(0, 12).fieldOf("radius").forGetter(RockColumnFeatureConfig::radius),
        Codec.floatRange(0.0F, 1.0F).optionalFieldOf("density", 0.5F).forGetter(RockColumnFeatureConfig::density)
    ).apply(instance, RockColumnFeatureConfig::new));
}
