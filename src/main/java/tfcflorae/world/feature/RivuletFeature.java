package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.LinkedList;
import net.minecraft.util.RandomSource;
import java.util.Set;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public class RivuletFeature extends Feature<BlockStateConfiguration> {
   public RivuletFeature(Codec<BlockStateConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
      WorldGenLevel world = context.level();
      BlockPos pos = context.origin();
      RandomSource rand = context.random();
      BlockStateConfiguration config = (BlockStateConfiguration)context.config();
      ChunkPos chunkPos = new ChunkPos(pos);
      BoundingBox box = new BoundingBox(
         chunkPos.getMinBlockX() - 14, Integer.MIN_VALUE, chunkPos.getMinBlockZ() - 14, chunkPos.getMaxBlockX() + 14, Integer.MAX_VALUE, chunkPos.getMaxBlockZ() + 14
      );
      Set<BlockPos> chosen = new HashSet<>();
      LinkedList<BlockPos> branches = new LinkedList<>();
      BlockPos startPos = new BlockPos(pos.getX(), world.getHeight(Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ()), pos.getZ());
      if (!world.getFluidState(startPos.below()).isEmpty()) {
         return false;
      } else {
         MutableBlockPos mutablePos = new MutableBlockPos();
         branches.add(startPos);
         boolean mainBranch = true;

         while (!branches.isEmpty()) {
            BlockPos branchStartPos = branches.removeFirst();
            int maxLength = 5 + rand.nextInt(8);
            if (mainBranch) {
               mainBranch = false;
               maxLength += 12;
            }

            BlockPos lastPos = branchStartPos;

            for (int i = 0; i < maxLength; i++) {
               chosen.add(branchStartPos);
               Direction chosenDirection = null;
               int chosenHeight = 0;
               int possibleDirections = 0;

               for (Direction direction : Plane.HORIZONTAL) {
                  mutablePos.setWithOffset(lastPos, direction);
                  if (box.isInside(mutablePos) && world.getFluidState(mutablePos.below()).isEmpty()) {
                     int height = world.getHeight(Types.OCEAN_FLOOR_WG, mutablePos.getX(), mutablePos.getZ());
                     if (height <= mutablePos.getY()) {
                        mutablePos.setY(height - 1);
                        if (world.getBlockState(mutablePos).isSolid()) {
                           if (chosenDirection == null || rand.nextInt(++possibleDirections) == 0) {
                              chosenHeight = height;
                              chosenDirection = direction;
                           }
                        }
                     }
                  }
               }

               if (possibleDirections == 0) {
                  break;
               }

               if (possibleDirections > 1 && rand.nextInt(3) == 0) {
                  branches.add(lastPos);
               }

               mutablePos.setWithOffset(lastPos, chosenDirection).setY(chosenHeight);
               if (chosen.contains(mutablePos) || !world.getFluidState(mutablePos.below()).isEmpty()) {
                  break;
               }

               lastPos = mutablePos.immutable();
               chosen.add(lastPos);
            }
         }

         if (chosen.isEmpty()) {
            return false;
         } else {
            for (BlockPos chosenPos : chosen) {
               mutablePos.set(chosenPos);

               while (!FluidHelpers.isAirOrEmptyFluid(world.getBlockState(mutablePos))) {
                  this.setBlock(world, mutablePos, this.getReplaceState(world, mutablePos));
                  mutablePos.move(Direction.UP);
               }

               mutablePos.setWithOffset(chosenPos, Direction.DOWN);
               this.setBlock(world, mutablePos, this.getReplaceState(world, mutablePos));
               mutablePos.move(Direction.DOWN);
               this.setBlock(world, mutablePos, config.state);
               mutablePos.move(Direction.DOWN);
               this.setBlock(world, mutablePos, config.state);
            }

            return true;
         }
      }
   }

   @NotNull
   private BlockState getReplaceState(WorldGenLevel level, MutableBlockPos pos) {
      for (Direction d : Plane.HORIZONTAL) {
         pos.move(d);
         FluidState fluidState = level.getFluidState(pos);
         if (!fluidState.isEmpty()) {
            pos.move(d.getOpposite());
            return fluidState.createLegacyBlock();
         }

         pos.move(d.getOpposite());
      }

      return Blocks.AIR.defaultBlockState();
   }
}
