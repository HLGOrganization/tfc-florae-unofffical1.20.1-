package tfcflorae.common.blocks.spidercave;

import java.util.Optional;
import net.minecraft.util.RandomSource;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BodyWebBlock extends Block implements IForgeBlockExtension, IFluidLoggable {
   private static final VoxelShape TIP_MERGE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
   private static final VoxelShape TIP_SHAPE_UP = Block.box(3.0, 0.0, 3.0, 13.0, 11.0, 13.0);
   private static final VoxelShape TIP_SHAPE_DOWN = Block.box(3.0, 5.0, 3.0, 13.0, 16.0, 13.0);
   private static final VoxelShape FRUSTUM_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   private static final VoxelShape MIDDLE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   private static final VoxelShape BASE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   public static final FluidProperty FLUID = TFCBlockStateProperties.WATER;
   public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
   public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
   public static final VoxelShape REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
   private final ExtendedProperties properties;

   public BodyWebBlock(ExtendedProperties properties) {
      super(properties.properties());
      this.properties = properties;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
                  .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY)))
               .setValue(TIP_DIRECTION, Direction.DOWN))
            .setValue(THICKNESS, DripstoneThickness.TIP)
      );
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
      pBuilder.add(new Property[]{TIP_DIRECTION, THICKNESS, this.getFluidProperty()});
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return isValidPointedWebPlacement(pLevel, pPos, (Direction)pState.getValue(TIP_DIRECTION));
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!this.canSurvive(pState, pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }
   }

   public BlockState updateShape(BlockState state, Direction pDirection, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      if (pDirection != Direction.UP && pDirection != Direction.DOWN) {
         return state;
      } else {
         Direction direction = (Direction)state.getValue(TIP_DIRECTION);
         if (pDirection == Direction.DOWN && level.getBlockTicks().hasScheduledTick(currentPos, this)) {
            return state;
         } else if (direction == direction.getOpposite() && !this.canSurvive(state, level, currentPos)) {
            if (direction == Direction.DOWN) {
               level.scheduleTick(currentPos, this, 2);
            } else {
               level.scheduleTick(currentPos, this, 1);
            }

            return state;
         } else if (!(level.getBlockState(currentPos.below()).getBlock() instanceof EggBlock)
            && !(level.getBlockState(currentPos.below()).getBlock() instanceof WebbedBlock)
            && !(level.getBlockState(currentPos.below()).getBlock() instanceof WebbedChestBlock)) {
            boolean flag = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
            DripstoneThickness webThickness = calculateWebThickness(level, currentPos, direction, flag);
            return (BlockState)state.setValue(THICKNESS, webThickness);
         } else {
            return (BlockState)((BlockState)state.setValue(THICKNESS, DripstoneThickness.BASE)).setValue(TIP_DIRECTION, Direction.UP);
         }
      }
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      DripstoneThickness dripstonethickness = (DripstoneThickness)pState.getValue(THICKNESS);
      VoxelShape voxelshape;
      if (dripstonethickness == DripstoneThickness.TIP_MERGE) {
         voxelshape = TIP_MERGE_SHAPE;
      } else if (dripstonethickness == DripstoneThickness.TIP) {
         if (pState.getValue(TIP_DIRECTION) == Direction.DOWN) {
            voxelshape = TIP_SHAPE_DOWN;
         } else {
            voxelshape = TIP_SHAPE_UP;
         }
      } else if (dripstonethickness == DripstoneThickness.FRUSTUM) {
         voxelshape = FRUSTUM_SHAPE;
      } else if (dripstonethickness == DripstoneThickness.MIDDLE) {
         voxelshape = MIDDLE_SHAPE;
      } else {
         voxelshape = BASE_SHAPE;
      }

      Vec3 vec3 = pState.getOffset(pLevel, pPos);
      return voxelshape.move(vec3.x, 0.0, vec3.z);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      LevelAccessor levelaccessor = context.getLevel();
      BlockPos blockpos = context.getClickedPos();
      Direction direction = context.getNearestLookingVerticalDirection().getOpposite();
      Direction direction1 = calculateTipDirection(levelaccessor, blockpos, direction);
      if (direction1 == null) {
         return null;
      } else {
         boolean flag = !context.isSecondaryUseActive();
         DripstoneThickness dripstonethickness = calculateWebThickness(levelaccessor, blockpos, direction1, flag);
         return dripstonethickness == null
            ? null
            : (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(TIP_DIRECTION, direction1)).setValue(THICKNESS, dripstonethickness))
               .setValue(this.getFluidProperty(), this.getFluidProperty().keyForOrEmpty(fluid.getType()));
      }
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public static DripstoneThickness calculateWebThickness(LevelReader p_154093_, BlockPos p_154094_, Direction p_154095_, boolean p_154096_) {
      Direction direction = p_154095_.getOpposite();
      BlockState blockstate = p_154093_.getBlockState(p_154094_.relative(p_154095_));
      if (!isPointedWebWithDirection(blockstate, direction)) {
         if (!isPointedWebWithDirection(blockstate, p_154095_)) {
            return DripstoneThickness.TIP;
         } else {
            DripstoneThickness webThickness = (DripstoneThickness)blockstate.getValue(THICKNESS);
            if (webThickness != DripstoneThickness.TIP && webThickness != DripstoneThickness.TIP_MERGE) {
               BlockState blockstate1 = p_154093_.getBlockState(p_154094_.relative(direction));
               return !isPointedWebWithDirection(blockstate1, p_154095_) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
            } else {
               return DripstoneThickness.FRUSTUM;
            }
         }
      } else {
         return !p_154096_ && blockstate.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
      }
   }

   public static boolean canTipGrow(BlockState pState, ServerLevel pLevel, BlockPos pPos) {
      Direction direction = (Direction)pState.getValue(TIP_DIRECTION);
      BlockPos blockpos = pPos.relative(direction);
      BlockState blockstate = pLevel.getBlockState(blockpos);
      if (!blockstate.getFluidState().isEmpty()) {
         return false;
      } else {
         return blockstate.isAir() ? true : isUnmergedTipWithDirection(blockstate, direction.getOpposite());
      }
   }

   public static boolean isUnmergedTipWithDirection(BlockState p_154144_, Direction p_154145_) {
      return isTip(p_154144_, false) && p_154144_.getValue(TIP_DIRECTION) == p_154145_;
   }

   public static boolean isStalactite(BlockState pState) {
      return isPointedWebWithDirection(pState, Direction.DOWN);
   }

   public static boolean isStalagmite(BlockState pState) {
      return isPointedWebWithDirection(pState, Direction.UP);
   }

   public static boolean isStalactiteStartPos(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return isStalactite(pState) && !(pLevel.getBlockState(pPos.above()).getBlock() instanceof BodyWebBlock);
   }

   @Nullable
   public static BlockPos findTip(BlockState state, LevelAccessor level, BlockPos pos, int val, boolean bool) {
      if (isTip(state, bool)) {
         return pos;
      } else {
         Direction direction = (Direction)state.getValue(TIP_DIRECTION);
         BiPredicate<BlockPos, BlockState> bipredicate = (p_202023_, p_202024_) -> p_202024_.getBlock() instanceof BodyWebBlock
               && p_202024_.getValue(TIP_DIRECTION) == direction;
         return findBlockVertical(level, pos, direction.getAxisDirection(), bipredicate, p_154168_ -> isTip(p_154168_, bool), val).orElse((BlockPos)null);
      }
   }

   @Nullable
   public static Direction calculateTipDirection(LevelReader p_154191_, BlockPos p_154192_, Direction p_154193_) {
      Direction direction;
      if (isValidPointedWebPlacement(p_154191_, p_154192_, p_154193_)) {
         direction = p_154193_;
      } else {
         if (!isValidPointedWebPlacement(p_154191_, p_154192_, p_154193_.getOpposite())) {
            return null;
         }

         direction = p_154193_.getOpposite();
      }

      return direction;
   }

   public static Optional<BlockPos> findRootBlock(Level p_154067_, BlockPos p_154068_, BlockState p_154069_, int p_154070_) {
      Direction direction = (Direction)p_154069_.getValue(TIP_DIRECTION);
      BiPredicate<BlockPos, BlockState> bipredicate = (p_202015_, p_202016_) -> p_202016_.getBlock() instanceof BodyWebBlock
            && p_202016_.getValue(TIP_DIRECTION) == direction;
      return findBlockVertical(
         p_154067_, p_154068_, direction.getOpposite().getAxisDirection(), bipredicate, p_154245_ -> !(p_154245_.getBlock() instanceof BodyWebBlock), p_154070_
      );
   }

   public static boolean isValidPointedWebPlacement(LevelReader level, BlockPos pos, Direction direction) {
      BlockPos blockpos = pos.relative(direction.getOpposite());
      BlockState blockstate = level.getBlockState(blockpos);
      BlockPos abovePos = pos.above();
      BlockState aboveState = level.getBlockState(abovePos);
      return (blockstate.isFaceSturdy(level, blockpos, direction) || isPointedWebWithDirection(blockstate, direction))
         && (
            aboveState.isFaceSturdy(level, abovePos, Direction.DOWN)
               || level.getBlockState(abovePos).getBlock() instanceof BodyWebBlock
               || level.getBlockState(abovePos).getBlock() instanceof WebbedBlock
               || level.getBlockState(abovePos).getBlock() instanceof WebbedChestBlock
         );
   }

   public static boolean isTip(BlockState state, boolean p_154155_) {
      if (!(state.getBlock() instanceof BodyWebBlock)) {
         return false;
      } else {
         DripstoneThickness dripstonethickness = (DripstoneThickness)state.getValue(THICKNESS);
         return dripstonethickness == DripstoneThickness.TIP || p_154155_ && dripstonethickness == DripstoneThickness.TIP_MERGE;
      }
   }

   public static boolean isPointedWebWithDirection(BlockState state, Direction direction) {
      return state.getBlock() instanceof BodyWebBlock && state.getValue(TIP_DIRECTION) == direction;
   }

   public static Optional<BlockPos> findBlockVertical(
      LevelAccessor level, BlockPos pos, AxisDirection axis, BiPredicate<BlockPos, BlockState> p_202010_, Predicate<BlockState> p_202011_, int p_202012_
   ) {
      Direction direction = Direction.get(axis, Axis.Y);
      MutableBlockPos blockpos$mutableblockpos = pos.mutable();

      for (int i = 1; i < p_202012_; i++) {
         blockpos$mutableblockpos.move(direction);
         BlockState blockstate = level.getBlockState(blockpos$mutableblockpos);
         if (p_202011_.test(blockstate)) {
            return Optional.of(blockpos$mutableblockpos.immutable());
         }

         if (level.isOutsideBuildHeight(blockpos$mutableblockpos.getY()) || !p_202010_.test(blockpos$mutableblockpos, blockstate)) {
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   public OffsetType getOffsetType() {
      return OffsetType.XZ;
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, false);
      }
   }
}
