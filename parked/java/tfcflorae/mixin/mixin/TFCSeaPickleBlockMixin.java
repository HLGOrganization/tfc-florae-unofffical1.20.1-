package tfcflorae.mixin;

import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.coral.TFCSeaPickleBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin({TFCSeaPickleBlock.class})
public abstract class TFCSeaPickleBlockMixin extends Block implements IFluidLoggable {
   @Unique
   @Final
   private static final FluidProperty FLUID_ALL = TFCBlockStateProperties.ALL_WATER;

   public TFCSeaPickleBlockMixin(Properties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue(TFCSeaPickleBlock.PICKLES, 1));
   }

   @Overwrite(
      remap = false
   )
   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
      if (Helpers.isBlock(blockstate, this)) {
         return (BlockState)blockstate.setValue(TFCSeaPickleBlock.PICKLES, Math.min(4, (Integer)blockstate.getValue(TFCSeaPickleBlock.PICKLES) + 1));
      } else {
         BlockState state = this.defaultBlockState();
         FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
         return this.getFluidProperty().canContain(fluidState.getType())
            ? (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()))
            : (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY));
      }
   }

   @Overwrite(
      remap = false
   )
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{TFCSeaPickleBlock.PICKLES, this.getFluidProperty()});
   }

   @Overwrite(
      remap = false
   )
   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
   }

   @Overwrite(
      remap = false
   )
   public FluidProperty getFluidProperty() {
      return FLUID_ALL;
   }
}
