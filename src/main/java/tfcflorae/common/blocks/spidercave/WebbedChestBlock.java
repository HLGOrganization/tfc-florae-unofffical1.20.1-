package tfcflorae.common.blocks.spidercave;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.WebbedChestBlockEntity;

public class WebbedChestBlock extends BaseEntityBlock implements IForgeBlockExtension, IFluidLoggable, EntityBlockExtension {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final FluidProperty FLUID = TFCBlockStateProperties.WATER;
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   private final ExtendedProperties properties;

   public WebbedChestBlock(ExtendedProperties properties) {
      super(properties.properties());
      this.properties = properties;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY)))
               .setValue(FACING, Direction.NORTH))
            .setValue(OPEN, false)
      );
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{FACING, OPEN, this.getFluidProperty()});
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

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity blockentity = level.getBlockEntity(pos);
         if (blockentity instanceof WebbedChestBlockEntity) {
            pPlayer.openMenu((WebbedChestBlockEntity)blockentity);
            pPlayer.awardStat(Stats.OPEN_BARREL);
            PiglinAi.angerNearbyPiglins(pPlayer, true);
         }

         return InteractionResult.CONSUME;
      }
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
      if (!state.is(pNewState.getBlock())) {
         BlockEntity blockentity = level.getBlockEntity(pos);
         if (blockentity instanceof Container) {
            Containers.dropContents(level, pos, (Container)blockentity);
            level.updateNeighbourForOutputSignal(pos, this);
         }

         this.canSurvive(state, level, pos);
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      BlockEntity blockentity = level.getBlockEntity(pos);
      if (blockentity instanceof WebbedChestBlockEntity) {
         ((WebbedChestBlockEntity)blockentity).recheckOpen();
      }

      if (!this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new tfcflorae.common.blockentities.WebbedChestBlockEntity(pos, state);
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      if (pStack.hasCustomHoverName()) {
         BlockEntity blockentity = level.getBlockEntity(pos);
         if (blockentity instanceof WebbedChestBlockEntity) {
            ((WebbedChestBlockEntity)blockentity).setCustomName(pStack.getHoverName());
         }
      }
   }

   public boolean hasAnalogOutputSignal(BlockState state) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
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
