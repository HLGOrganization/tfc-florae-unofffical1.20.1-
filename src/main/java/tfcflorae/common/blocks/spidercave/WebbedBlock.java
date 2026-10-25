package tfcflorae.common.blocks.spidercave;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class WebbedBlock extends Block implements IForgeBlockExtension, IFluidLoggable {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final FluidProperty FLUID = TFCBlockStateProperties.WATER;
   private final ExtendedProperties properties;

   public WebbedBlock(ExtendedProperties properties) {
      super(properties.properties());
      this.properties = properties;
      this.registerDefaultState(
         (BlockState)((BlockState)this.defaultBlockState().setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY)))
            .setValue(FACING, Direction.NORTH)
      );
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, this.getFluidProperty()});
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState belowState = level.getBlockState(pos.below());
      BlockState aboveState = level.getBlockState(pos.above());
      return belowState.isFaceSturdy(level, pos, Direction.UP) || aboveState.getBlock() instanceof BodyWebBlock;
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, false);
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   public BlockState updateShape(BlockState state, Direction pDirection, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return state;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Nullable
   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   @Nullable
   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }
}
