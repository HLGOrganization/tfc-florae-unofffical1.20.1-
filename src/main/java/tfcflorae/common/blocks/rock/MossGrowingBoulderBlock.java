package tfcflorae.common.blocks.rock;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.dries007.tfc.common.blocks.rock.IMossGrowingBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.TFCFTags;

public class MossGrowingBoulderBlock extends Block implements IFluidLoggable, IMossGrowingBlock {
   public static final FluidProperty ALL_WATER_AND_LAVA = FluidProperty.create(
      "fluid",
      Stream.of(
         Fluids.EMPTY,
         Fluids.FLOWING_WATER,
         Fluids.WATER,
         TFCFluids.SALT_WATER,
         TFCFluids.SPRING_WATER,
         TFCFluids.RIVER_WATER,
         Fluids.LAVA,
         Fluids.FLOWING_LAVA
      )
   );
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   protected static final VoxelShape UP_SHAPE = box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
   protected static final VoxelShape DOWN_SHAPE = box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape NORTH_SHAPE = box(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape SOUTH_SHAPE = box(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
   protected static final VoxelShape WEST_SHAPE = box(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape EAST_SHAPE = box(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
   protected static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.of(
      Direction.UP,
      UP_SHAPE,
      Direction.DOWN,
      DOWN_SHAPE,
      Direction.NORTH,
      NORTH_SHAPE,
      Direction.SOUTH,
      SOUTH_SHAPE,
      Direction.WEST,
      WEST_SHAPE,
      Direction.EAST,
      EAST_SHAPE
   );
   public static final FluidProperty FLUID = ALL_WATER_AND_LAVA;
   public final Supplier<? extends Block> mossy;
   public boolean IS_UP_DOWN;
   public boolean IS_NORTH_SOUTH;
   public boolean IS_EAST_WEST;

   public MossGrowingBoulderBlock(Properties properties, Supplier<? extends Block> mossy) {
      super(properties);
      this.mossy = mossy;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY)))
            .setValue(FACING, Direction.UP)
      );
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      level.scheduleTick(pos, this, 1);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      if (!this.canSurvive(state, level, currentPos)) {
         level.destroyBlock(currentPos, true);
      }

      if (state.getValue(FACING) == Direction.UP || state.getValue(FACING) == Direction.UP) {
         this.IS_UP_DOWN = true;
      }

      if (state.getValue(FACING) == Direction.NORTH || state.getValue(FACING) == Direction.SOUTH) {
         this.IS_NORTH_SOUTH = true;
      }

      if (state.getValue(FACING) == Direction.EAST || state.getValue(FACING) == Direction.WEST) {
         this.IS_EAST_WEST = true;
      }

      return state;
   }

   public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random) {
      for (Direction direction : Direction.values()) {
         BlockPos relativePos = blockPos.relative(direction);
         if (Helpers.isBlock(serverLevel.getBlockState(relativePos), TFCFTags.Blocks.PODZOL)
            || Helpers.isBlock(serverLevel.getBlockState(relativePos), TFCFTags.Blocks.ROOTED_DIRT)
            || Helpers.isBlock(serverLevel.getBlockState(relativePos), TFCFTags.Blocks.MOSSY_PACKED_MUD)
               && random.nextInt((Integer)TFCConfig.SERVER.mossyRockSpreadRate.get()) == 0) {
            this.convertToMossy(serverLevel, blockPos, blockState, false);
         }
      }
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      Direction direction = (Direction)state.getValue(FACING);
      BlockPos blockPos = pos.relative(direction.getOpposite());
      BlockState blockState = level.getBlockState(blockPos);
      return blockState.isFaceSturdy(level, blockPos, direction);
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{this.getFluidProperty(), FACING});
   }

   public void convertToMossy(Level worldIn, BlockPos pos, BlockState state, boolean needsWater) {
      if (!needsWater || FluidHelpers.isSame(worldIn.getFluidState(pos.above()), Fluids.WATER)) {
         worldIn.setBlock(pos, this.mossy.get().defaultBlockState(), 3);
      }
   }

   public OffsetType getOffsetType() {
      return this.IS_UP_DOWN ? OffsetType.XZ : OffsetType.NONE;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      if (state.getValue(FACING) == Direction.UP || state.getValue(FACING) == Direction.UP) {
         this.IS_UP_DOWN = true;
      }

      if (state.getValue(FACING) == Direction.NORTH || state.getValue(FACING) == Direction.SOUTH) {
         this.IS_NORTH_SOUTH = true;
      }

      if (state.getValue(FACING) == Direction.EAST || state.getValue(FACING) == Direction.WEST) {
         this.IS_EAST_WEST = true;
      }

      return SHAPES.get(state.getValue(FACING));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = context.getClickedFace();
      return (BlockState)this.defaultBlockState().setValue(FACING, direction);
   }
}
