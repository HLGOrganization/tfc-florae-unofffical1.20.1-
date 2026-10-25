package tfcflorae.common.blocks.plant;

import java.util.Arrays;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.CreepingPlantBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class CreepingWaterPlantBlock extends CreepingPlantBlock implements DirectionPropertyBlock, IFluidLoggable {
   protected static final Direction[] DIRECTIONS = Direction.values();
   protected final Map<BlockState, VoxelShape> shapeCache;

   public static CreepingWaterPlantBlock create(final RegistryPlant plant, ExtendedProperties properties, final FluidProperty fluid) {
      return new CreepingWaterPlantBlock(properties) {
         public RegistryPlant getPlant() {
            return plant;
         }

         public FluidProperty getFluidProperty() {
            return fluid;
         }
      };
   }

   protected CreepingWaterPlantBlock(ExtendedProperties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)DirectionPropertyBlock.setAllDirections((BlockState)this.getStateDefinition().any(), false)
            .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY))
      );
      this.shapeCache = DirectionPropertyBlock.makeShapeCache(this.getStateDefinition(), SHAPES::get);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      super.getStateForPlacement(context);
      BlockPos pos = context.getClickedPos();
      FluidState fluidState = context.getLevel().getFluidState(pos);
      BlockState state = this.updateStateWithCurrentMonth(this.defaultBlockState());
      if (this.getFluidProperty().canContain(fluidState.getType())) {
         state = (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
      }

      return state;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{this.getFluidProperty()}));
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
      return state.getFluidState().isEmpty();
   }

   public static ToIntFunction<BlockState> emission(int direction) {
      return state -> hasAnyFace(state) ? direction : 0;
   }

   public static boolean hasAnyFace(BlockState state) {
      return Arrays.stream(DIRECTIONS).anyMatch(direction -> hasFace(state, direction));
   }

   public static boolean hasFace(BlockState state, Direction direction) {
      BooleanProperty booleanProperty = getFaceProperty(direction);
      return state.hasProperty(booleanProperty) && (Boolean)state.getValue(booleanProperty);
   }

   public static BooleanProperty getFaceProperty(Direction direction) {
      return (BooleanProperty)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(direction);
   }
}
