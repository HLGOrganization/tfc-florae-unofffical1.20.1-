package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class GeyserPointedFeature extends Feature<GeyserPointedConfig> {
   public GeyserPointedFeature(Codec<GeyserPointedConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<GeyserPointedConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      GeyserPointedConfig config = (GeyserPointedConfig)context.config();
      Boolean hasSurface = config.hasSurface;
      ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
      ChunkData data = provider.get(level, pos);
      RockSettings rock = data.getRockData().getRock(pos);
      List<BlockState> inputState = config.states.get(rock.raw());
      List<BlockState> inputSurfaceState = config.surfaceStates.get(rock.raw());
      Supplier<BlockState> state;
      if (inputState.size() == 1) {
         BlockState onlyState = inputState.get(0);
         state = () -> onlyState;
      } else {
         state = () -> inputState.get(random.nextInt(inputState.size()));
      }

      Supplier<BlockState> surfaceState;
      if (inputSurfaceState.size() == 1) {
         BlockState onlyState = inputSurfaceState.get(0);
         surfaceState = () -> onlyState;
      } else {
         surfaceState = () -> inputSurfaceState.get(random.nextInt(inputSurfaceState.size()));
      }

      Optional<Direction> optional = getTipDirection(state.get(), level, pos, random);
      if (optional.isEmpty()) {
         return false;
      } else {
         BlockPos pos1 = pos.relative(optional.get().getOpposite());
         createPatchOfDripstoneBlocks(surfaceState.get(), hasSurface, level, random, pos1, config);
         int i = random.nextFloat() < config.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(level.getBlockState(pos.relative(optional.get()))) ? 2 : 1;
         DripstoneUtils.growPointedDripstone(state.get(), hasSurface, level, pos, optional.get(), i, false);
         return true;
      }
   }

   private static Optional<Direction> getTipDirection(BlockState inputState, LevelAccessor level, BlockPos blockPos, RandomSource random) {
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

   private static void createPatchOfDripstoneBlocks(
      BlockState inputSurfaceState, Boolean hasSurface, LevelAccessor level, RandomSource random, BlockPos blockPos, GeyserPointedConfig config
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
