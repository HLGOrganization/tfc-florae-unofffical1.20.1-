package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import tfcflorae.TFCFlorae;

public class PointedDripstoneFeature extends Feature<PointedDripstoneConfig> {
   public PointedDripstoneFeature(Codec<PointedDripstoneConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<PointedDripstoneConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      PointedDripstoneConfig config = (PointedDripstoneConfig)context.config();
      Boolean hasSurface = config.hasSurface;
      BlockState inputState = config.state.getState(random, pos);
      BlockState inputSurfaceState = config.surfaceState.getState(random, pos);
      Optional<Direction> optional = getTipDirection(inputState, level, pos, random);
      if (optional.isEmpty()) {
         return false;
      } else {
         BlockPos pos1 = pos.relative(optional.get().getOpposite());
         createPatchOfDripstoneBlocks(inputSurfaceState, hasSurface, level, random, pos1, config);
         int i = random.nextFloat() < config.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(level.getBlockState(pos.relative(optional.get())))
            ? Mth.randomBetweenInclusive(random, 2, config.height)
            : 1;
         DripstoneUtils.growPointedDripstone(inputState, hasSurface, level, pos, optional.get(), i, false);
         if (pos.getY() > 55) {
            TFCFlorae.LOGGER.debug("Attempted to place spike at XYZ: " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
         }

         return true;
      }
   }

   public static Optional<Direction> getTipDirection(BlockState inputState, LevelAccessor level, BlockPos blockPos, RandomSource random) {
      boolean flag = DripstoneUtils.isDripstoneBase(level.getBlockState(blockPos.above()), inputState);
      boolean flag1 = DripstoneUtils.isDripstoneBase(level.getBlockState(blockPos.below()), inputState);
      if (flag && flag1) {
         return Optional.of(random.nextBoolean() ? Direction.DOWN : Direction.UP);
      } else if (flag) {
         return Optional.of(Direction.DOWN);
      } else {
         return flag1 ? Optional.of(Direction.UP) : Optional.empty();
      }
   }

   public static void createPatchOfDripstoneBlocks(
      BlockState inputSurfaceState, Boolean hasSurface, LevelAccessor level, RandomSource random, BlockPos blockPos, PointedDripstoneConfig config
   ) {
      DripstoneUtils.placeDripstoneBlockIfPossible(inputSurfaceState, level, blockPos, hasSurface);

      for (Direction direction : Plane.HORIZONTAL) {
         if (!(random.nextFloat() > config.chanceOfDirectionalSpread)) {
            BlockPos pos = blockPos.relative(direction);
            DripstoneUtils.placeDripstoneBlockIfPossible(inputSurfaceState, level, pos, hasSurface);
            if (!(random.nextFloat() > config.chanceOfSpreadRadius2)) {
               BlockPos pos1 = pos.relative(Direction.getRandom(random));
               DripstoneUtils.placeDripstoneBlockIfPossible(inputSurfaceState, level, pos1, hasSurface);
               if (!(random.nextFloat() > config.chanceOfSpreadRadius3)) {
                  BlockPos pos2 = pos1.relative(Direction.getRandom(random));
                  DripstoneUtils.placeDripstoneBlockIfPossible(inputSurfaceState, level, pos2, hasSurface);
               }
            }
         }
      }
   }
}
