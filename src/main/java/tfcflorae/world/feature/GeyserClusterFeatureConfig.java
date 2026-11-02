package tfcflorae.world.feature;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** 间歇泉簇配置：在落点附近随机撒 count 个间歇泉方块。 */
public record GeyserClusterFeatureConfig(List<BlockState> blocks, IntProvider count, int radius) implements FeatureConfiguration
{
    public static final Codec<GeyserClusterFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockState.CODEC.listOf().fieldOf("blocks").forGetter(GeyserClusterFeatureConfig::blocks),
        IntProvider.codec(1, 8).fieldOf("count").forGetter(GeyserClusterFeatureConfig::count),
        Codec.intRange(0, 12).optionalFieldOf("radius", 3).forGetter(GeyserClusterFeatureConfig::radius)
    ).apply(instance, GeyserClusterFeatureConfig::new));
}
