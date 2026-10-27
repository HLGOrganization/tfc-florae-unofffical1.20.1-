package tfcflorae.common.blocks.wood;
import tfcflorae.common.TFCFBlockStateProperties;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.wood.LogBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class BambooLogBlock extends LogBlock implements IFluidLoggable {
   public static final VoxelShape SHAPE = box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
   public static final VoxelShape AXIS_X_SHAPE = box(0.0, 4.0, 4.0, 16.0, 12.0, 12.0);
   public static final VoxelShape AXIS_Y_SHAPE = box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);
   public static final VoxelShape AXIS_Z_SHAPE = box(4.0, 4.0, 0.0, 12.0, 12.0, 16.0);
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   @Nullable
   private final Supplier<? extends Block> stripped;

   public BambooLogBlock(ExtendedProperties properties, @Nullable Supplier<? extends Block> stripped) {
      super(properties, stripped);
      this.stripped = stripped;
      this.registerDefaultState((BlockState)((BlockState)this.defaultBlockState().setValue(AXIS, Axis.Y)).setValue(TFCFBlockStateProperties.NATURAL, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{this.getFluidProperty(), TFCFBlockStateProperties.NATURAL}));
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
      return true;
   }

   public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
      switch ((Axis)state.getValue(AXIS)) {
         case X:
            return AXIS_X_SHAPE;
         case Y:
            return AXIS_Y_SHAPE;
         case Z:
            return AXIS_Z_SHAPE;
         default:
            return SHAPE;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      super.getStateForPlacement(context);
      BlockPos pos = context.getClickedPos();
      FluidState fluidState = context.getLevel().getFluidState(pos);
      BlockState state = this.defaultBlockState();
      if (this.getFluidProperty().canContain(fluidState.getType())) {
         state = (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
      }

      return (BlockState)state.setValue(AXIS, context.getClickedFace().getAxis());
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      if (rot == Rotation.CLOCKWISE_90 || rot == Rotation.COUNTERCLOCKWISE_90) {
         if (state.getValue(AXIS) == Axis.X) {
            return (BlockState)state.setValue(AXIS, Axis.Z);
         }

         if (state.getValue(AXIS) == Axis.Z) {
            return (BlockState)state.setValue(AXIS, Axis.X);
         }
      }

      return state;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      float baseSpeed = (float)(state.getValue(TFCFBlockStateProperties.NATURAL) ? 2 : 1) * state.getDestroySpeed(level, pos);
      if (baseSpeed == -1.0F) {
         return 0.0F;
      } else {
         int toolModifier = ForgeHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
         return player.getDigSpeed(state, pos) / baseSpeed / (float)toolModifier;
      }
   }

   @Nullable
   public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
      return context.getItemInHand().canPerformAction(action) && action == ToolActions.AXE_STRIP && this.stripped != null
         ? Helpers.copyProperties(this.stripped.get().defaultBlockState(), state)
         : null;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos posBelow = pos.below();
      BlockState stateBelow = level.getBlockState(posBelow);
      // 竹节是 4x16x4 的细柱方块，既不 isSolid 也不 faceSturdy，
      // 原来的判定会把"叠在另一节竹子上"当成不合法支撑，导致结构/世界生成放下后
      // 根以上的竹块在 updateShape -> tick 里被立刻 destroy。这里显式放行竹子堆叠。
      if (stateBelow.getBlock() instanceof BambooLogBlock) {
         return true;
      }
      return !FluidHelpers.isAirOrEmptyFluid(stateBelow) && (stateBelow.isSolid() || stateBelow.isFaceSturdy(level, posBelow, Direction.UP));
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      if (!state.canSurvive(level, currentPos)) {
         level.scheduleTick(currentPos, this, 1);
      }

      return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }
}
