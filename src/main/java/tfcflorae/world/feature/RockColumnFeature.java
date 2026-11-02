package tfcflorae.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * 石柱 / 岩柱地物：在落点附近找地面，向上堆一节柱状方块。
 * <p>
 * 对应 1.18 的 {@code tfcflorae:rock_pillar} 与 {@code tfcflorae:rock_column}：
 * 单根立柱用 radius=0 / density=1，成片柱群用 radius&gt;0 + 较低的 density。
 * 简化点：柱体方块不按当地岩石挑选，而是从配置的 blocks 里随机取一种。
 */
public class RockColumnFeature extends Feature<RockColumnFeatureConfig>
{
    public RockColumnFeature(com.mojang.serialization.Codec<RockColumnFeatureConfig> codec)
    {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RockColumnFeatureConfig> context)
    {
        final WorldGenLevel level = context.level();
        final RandomSource random = context.random();
        final BlockPos origin = context.origin();
        final RockColumnFeatureConfig config = context.config();

        final int radius = config.radius().sample(random);
        boolean placed = false;

        for (int dx = -radius; dx <= radius; dx++)
        {
            for (int dz = -radius; dz <= radius; dz++)
            {
                if (dx * dx + dz * dz > radius * radius)
                {
                    continue;
                }
                if (radius > 0 && random.nextFloat() > config.density())
                {
                    continue;
                }

                final BlockPos ground = findGround(level, origin.offset(dx, 0, dz));
                if (ground == null)
                {
                    continue;
                }

                final BlockState column = config.blocks().get(random.nextInt(config.blocks().size()));
                final int height = config.height().sample(random);
                for (int i = 1; i <= height; i++)
                {
                    final BlockPos pos = ground.above(i);
                    final BlockState existing = level.getBlockState(pos);
                    if (!existing.canBeReplaced())
                    {
                        break;
                    }
                    level.setBlock(pos, column, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    /** 从基准点附近上下找一处「上方是空气、自身不是空气」的地面。 */
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
