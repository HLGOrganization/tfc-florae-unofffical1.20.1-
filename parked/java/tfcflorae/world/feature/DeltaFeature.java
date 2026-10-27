package tfcflorae.world.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import tfcflorae.common.TFCFTags;

public class DeltaFeature extends Feature<DeltaConfig> {
   public static final ImmutableList<Block> CANNOT_REPLACE = ImmutableList.of(
      Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER
   );
   public static final Direction[] DIRECTIONS = Direction.values();
   public static final double RIM_SPAWN_CHANCE = 1.0;

   public DeltaFeature(Codec<DeltaConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<DeltaConfig> context) {
      boolean flag = false;
      RandomSource random = context.random();
      WorldGenLevel level = context.level();
      DeltaConfig config = (DeltaConfig)context.config();
      BlockPos blockpos = context.origin();
      boolean flag1 = random.nextDouble() < 1.0;
      int i = flag1 ? config.rimSize().sample(random) : 0;
      int j = flag1 ? config.rimSize().sample(random) : 0;
      boolean flag2 = flag1 && i != 0 && j != 0;
      int k = config.size().sample(random);
      int l = config.size().sample(random);
      int i1 = Math.max(k, l);

      for (BlockPos blockpos1 : BlockPos.withinManhattan(blockpos, k, 0, l)) {
         if (blockpos1.distManhattan(blockpos) > i1) {
            break;
         }

         if (isClear(level, blockpos1, config)) {
            if (flag2) {
               flag = true;
               this.setBlock(level, blockpos1, config.rim());
            }

            BlockPos blockpos2 = blockpos1.offset(i, 0, j);
            if (isClear(level, blockpos2, config)) {
               flag = true;
               this.setBlock(level, blockpos2, config.contents());
            }

            if (isClear(level, blockpos2.below(), config) && !config.bottom().isAir()) {
               flag = true;
               this.setBlock(level, blockpos2.below(), config.bottom());
            }
         }
      }

      return flag;
   }

   public static boolean isClear(WorldGenLevel level, BlockPos pos, DeltaConfig config) {
      BlockState blockstate = level.getBlockState(pos);
      boolean replaceable = EnvironmentHelpers.isWorldgenReplaceable(level, pos)
         || level.getBlockState(pos).canBeReplaced()
         || level.getBlockState(pos).getBlock() instanceof BushBlock
         || Helpers.isBlock(level.getBlockState(pos).getBlock(), TFCFTags.Blocks.REPLACEABLE);
      if (blockstate.is(config.contents().getBlock())) {
         return false;
      } else if (replaceable) {
         return true;
      } else if (CANNOT_REPLACE.contains(blockstate.getBlock())) {
         return false;
      } else {
         for (Direction direction : DIRECTIONS) {
            boolean flag = level.isStateAtPosition(pos.relative(direction), net.minecraft.world.level.block.state.BlockState::isAir);
            if (flag && direction != Direction.UP || !flag && direction == Direction.UP) {
               return false;
            }
         }

         return true;
      }
   }
}
