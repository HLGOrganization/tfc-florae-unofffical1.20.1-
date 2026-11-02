package tfcflorae.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * 间歇泉簇地物：把间歇泉方块放在地表上（对应 1.18 的 {@code tfcflorae:geyser_cluster}）。
 * <p>
 * 简化点：只在「上方是空气」的地面顶部放一个方块，不做 1.18 的锥形簇与
 * {@code surface_states} 地面替换；视觉效果由 {@code Geyser} 方块自身的粒子提供。
 */
public class GeyserClusterFeature extends Feature<GeyserClusterFeatureConfig>
{
    public GeyserClusterFeature(com.mojang.serialization.Codec<GeyserClusterFeatureConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GeyserClusterFeatureConfig> context)
    {
        final WorldGenLevel level = context.level();
        final RandomSource random = context.random();
        final BlockPos origin = context.origin();
        final GeyserClusterFeatureConfig config = context.config();

        final int count = config.count().sample(random);
        final int radius = config.radius();
        boolean placed = false;

        for (int i = 0; i < count; i++)
        {
            final int dx = radius == 0 ? 0 : random.nextInt(radius * 2 + 1) - radius;
            final int dz = radius == 0 ? 0 : random.nextInt(radius * 2 + 1) - radius;
            final BlockPos ground = findGround(level, origin.offset(dx, 0, dz));
            if (ground == null)
            {
                continue;
            }
            final BlockPos pos = ground.above();
            if (!level.getBlockState(pos).canBeReplaced())
            {
                continue;
            }
            final BlockState geyser = config.blocks().get(random.nextInt(config.blocks().size()));
            level.setBlock(pos, geyser, 2);
            placed = true;
        }
        return placed;
    }

    private static BlockPos findGround(WorldGenLevel level, BlockPos base)
    {
        for (int dy = 4; dy >= -4; dy--)
        {
            final BlockPos pos = base.offset(0, dy, 0);
            if (pos.getY() <= level.getMinBuildHeight())
            {
                break;
            }
            if (!level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above()))
            {
                return pos;
            }
        }
        return null;
    }
}
