package tfcflorae.common.blocks.plant;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.TFCTallGrassBlock;
import net.dries007.tfc.common.blocks.plant.ITallPlant.Part;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public abstract class TallLandWaterPlantBlock extends TFCTallGrassBlock implements IFluidLoggable {
   public static final EnumProperty<Part> PART = TFCBlockStateProperties.TALL_PLANT_PART;

   public static TallLandWaterPlantBlock create(final RegistryPlant plant, final FluidProperty fluid, Properties properties) {
      return new TallLandWaterPlantBlock(ExtendedProperties.of(properties)) {
         public RegistryPlant getPlant() {
            return plant;
         }

         public FluidProperty getFluidProperty() {
            return fluid;
         }
      };
   }

   protected TallLandWaterPlantBlock(ExtendedProperties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY)))
            .setValue(PART, Part.LOWER)
      );
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState belowState = level.getBlockState(pos.below());
      if (state.getValue(PART) == Part.LOWER) {
         return pos.getY() >= 63
            ? Helpers.isBlock(belowState, Blocks.BUSH_PLANTABLE_ON)
            : Helpers.isBlock(belowState, Blocks.SEA_BUSH_PLANTABLE_ON) || Helpers.isBlock(belowState, Blocks.BUSH_PLANTABLE_ON);
      } else if (state.getBlock() != this) {
         return pos.getY() >= 63
            ? Helpers.isBlock(belowState, Blocks.BUSH_PLANTABLE_ON)
            : Helpers.isBlock(belowState, Blocks.SEA_BUSH_PLANTABLE_ON) || Helpers.isBlock(belowState, Blocks.BUSH_PLANTABLE_ON);
      } else {
         return belowState.getBlock() == this && belowState.getValue(PART) == Part.LOWER;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos pos = context.getClickedPos();
      FluidState fluidState = context.getLevel().getFluidState(pos);
      BlockState state = this.updateStateWithCurrentMonth(this.defaultBlockState());
      if (this.getFluidProperty().canContain(fluidState.getType())) {
         state = (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
      }

      return pos.getY() < context.getLevel().getMaxBuildHeight() - 1 && context.getLevel().getBlockState(pos.above()).canBeReplaced(context) ? state : null;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{this.getFluidProperty()}));
   }

   public void placeTwoHalves(LevelAccessor level, BlockPos pos, int flags, RandomSource random) {
      IntegerProperty stageProperty = this.getPlant().getStageProperty();
      BlockPos posAbove = pos.above();
      int age = random.nextInt(4);
      Fluid fluidBottom = level.getFluidState(pos).getType();
      Fluid fluidTop = level.getFluidState(posAbove).getType();
      if (!fluidBottom.isSame(Fluids.EMPTY)) {
         BlockState state = FluidHelpers.fillWithFluid((BlockState)((BlockState)this.defaultBlockState().setValue(AGE, age)).setValue(PART, Part.LOWER), fluidBottom);
         BlockState stateUp = FluidHelpers.fillWithFluid((BlockState)((BlockState)this.defaultBlockState().setValue(AGE, age)).setValue(PART, Part.UPPER), fluidTop);
         if (stageProperty != null) {
            state = (BlockState)state.setValue(stageProperty, 2);
            stateUp = (BlockState)stateUp.setValue(stageProperty, 2);
         }

         if (state != null && stateUp != null) {
            level.setBlock(pos, state, flags);
            level.setBlock(posAbove, stateUp, flags);
         }
      } else if (fluidBottom.isSame(Fluids.EMPTY)) {
         super.placeTwoHalves(level, pos, flags, random);
      }
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }
}
