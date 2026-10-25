package tfcflorae.common.blocks.plant;

import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.TFCCactusBlock;
import net.dries007.tfc.common.blocks.plant.ITallPlant.Part;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class TFCFCactusBlock extends TFCCactusBlock {
   protected static final VoxelShape COLLISION_SHAPE = box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
   protected static final VoxelShape OUTLINE_SHAPE = box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

   public static TFCFCactusBlock create(final RegistryPlant plant, ExtendedProperties properties) {
      return new TFCFCactusBlock(properties) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected TFCFCactusBlock(ExtendedProperties properties) {
      super(properties);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      MutableBlockPos mutable = new MutableBlockPos();

      for (Direction direction : Plane.HORIZONTAL) {
         mutable.setWithOffset(pos, direction);
         BlockState stateAt = level.getBlockState(mutable);
         if (stateAt.isSolid() || Helpers.isFluid(level.getFluidState(mutable), FluidTags.LAVA)) {
            return false;
         }
      }

      mutable.setWithOffset(pos, 0, -1, 0);
      BlockState belowState = level.getBlockState(mutable);
      if (state.getValue(PART) == Part.LOWER) {
         return Helpers.isBlock(belowState, BlockTags.SAND)
            || Helpers.isBlock(state.getBlock(), Blocks.GRASS_PLANTABLE_ON)
            || Helpers.isBlock(state.getBlock(), Blocks.BUSH_PLANTABLE_ON);
      } else {
         return state.getBlock() != this
            ? Helpers.isBlock(belowState, BlockTags.SAND)
               || Helpers.isBlock(state.getBlock(), Blocks.GRASS_PLANTABLE_ON)
               || Helpers.isBlock(state.getBlock(), Blocks.BUSH_PLANTABLE_ON)
            : belowState.getBlock() == this && belowState.getValue(PART) == Part.LOWER;
      }
   }
}
