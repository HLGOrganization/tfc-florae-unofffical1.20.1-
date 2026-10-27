package tfcflorae.world.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.RiverWaterBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class RockColumnFeature extends Feature<RockColumnConfig> {
   private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of(
      Blocks.LAVA,
      Blocks.BEDROCK,
      Blocks.MAGMA_BLOCK,
      Blocks.SOUL_SAND,
      Blocks.NETHER_BRICKS,
      Blocks.NETHER_BRICK_FENCE,
      Blocks.NETHER_BRICK_STAIRS,
      Blocks.NETHER_WART,
      Blocks.CHEST,
      Blocks.SPAWNER
   );

   public RockColumnFeature(Codec<RockColumnConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<RockColumnConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      RockColumnConfig config = (RockColumnConfig)context.config();
      ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
      ChunkData data = provider.get(context.level(), pos);
      RockSettings rock = data.getRockData().getRock(pos);
      List<BlockState> states = config.getStates(rock.raw());
      if (states != null) {
         if (!canPlaceAt(level, pos.mutable())) {
            return false;
         } else {
            int j = config.height().sample(random);
            boolean flag = random.nextFloat() < 0.9F;
            int k = Math.min(j, flag ? 5 : 8);
            int l = flag ? 50 : 15;
            boolean flag1 = false;

            for (BlockPos blockpos1 : BlockPos.randomBetweenClosed(
               random, l, pos.getX() - k, pos.getY(), pos.getZ() - k, pos.getX() + k, pos.getY(), pos.getZ() + k
            )) {
               int i1 = j - blockpos1.distManhattan(pos);
               if (i1 >= 0) {
                  flag1 |= this.placeColumn(level, blockpos1, i1, config.reach().sample(random), states, random);
               }
            }

            return flag1;
         }
      } else {
         return false;
      }
   }

   private boolean placeColumn(LevelAccessor level, BlockPos pos, int distance, int reach, List<BlockState> states, RandomSource random) {
      Supplier<BlockState> state;
      if (states.size() == 1) {
         BlockState onlyState = states.get(0);
         state = () -> onlyState;
      } else {
         state = () -> states.get(random.nextInt(states.size()));
      }

      boolean flag = false;

      for (BlockPos blockpos : BlockPos.betweenClosed(
         pos.getX() - reach, pos.getY(), pos.getZ() - reach, pos.getX() + reach, pos.getY(), pos.getZ() + reach
      )) {
         int i = blockpos.distManhattan(pos);
         BlockPos blockpos1 = isAirOrLavaOcean(level, blockpos) ? findSurface(level, blockpos.mutable(), i) : findAir(level, blockpos.mutable(), i);
         if (blockpos1 != null) {
            int j = distance - i / 2;

            for (MutableBlockPos blockpos$mutableBlockPos = blockpos1.mutable(); j >= 0; j--) {
               if (isAirOrLavaOcean(level, blockpos$mutableBlockPos)) {
                  this.setBlock(level, blockpos$mutableBlockPos, state.get());
                  blockpos$mutableBlockPos.move(Direction.UP);
                  flag = true;
               } else {
                  if (!level.getBlockState(blockpos$mutableBlockPos).is(state.get().getBlock())) {
                     break;
                  }

                  blockpos$mutableBlockPos.move(Direction.UP);
               }
            }
         }
      }

      return flag;
   }

   @Nullable
   private static BlockPos findSurface(LevelAccessor level, MutableBlockPos pos, int distance) {
      while (pos.getY() > level.getMinBuildHeight() + 1 && distance > 0) {
         distance--;
         if (canPlaceAt(level, pos)) {
            return pos;
         }

         pos.move(Direction.DOWN);
      }

      return null;
   }

   private static boolean canPlaceAt(LevelAccessor level, MutableBlockPos pos) {
      if (!isAirOrLavaOcean(level, pos)) {
         return false;
      } else {
         BlockState blockstate = level.getBlockState(pos.move(Direction.DOWN));
         pos.move(Direction.UP);
         return !blockstate.isAir() && !CANNOT_PLACE_ON.contains(blockstate.getBlock());
      }
   }

   @Nullable
   private static BlockPos findAir(LevelAccessor level, MutableBlockPos pos, int distance) {
      while (pos.getY() < level.getMaxBuildHeight() && distance > 0) {
         distance--;
         BlockState blockstate = level.getBlockState(pos);
         if (CANNOT_PLACE_ON.contains(blockstate.getBlock())) {
            return null;
         }

         if (isAirOrLavaOcean(level, pos)) {
            return pos;
         }

         pos.move(Direction.UP);
      }

      return null;
   }

   private static boolean isAirOrLavaOcean(LevelAccessor level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      return isUnderwater(level, pos)
         || EnvironmentHelpers.isWater(state)
         || FluidHelpers.isAirOrEmptyFluid(state)
         || state.isAir()
         || state.is(Blocks.LAVA);
   }

   private static boolean isUnderwater(LevelAccessor level, BlockPos pos) {
      BlockState stateAt = level.getBlockState(pos);
      FluidState fluid = stateAt.getFluidState();
      return Helpers.isFluid(fluid, FluidTags.WATER)
         || Helpers.isBlock(stateAt, (Block)TFCBlocks.SALT_WATER.get())
         || stateAt.hasProperty(RiverWaterBlock.FLOW);
   }
}
