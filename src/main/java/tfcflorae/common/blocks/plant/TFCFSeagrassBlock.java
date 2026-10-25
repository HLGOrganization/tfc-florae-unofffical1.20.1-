package tfcflorae.common.blocks.plant;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.PlantRegrowth;
import net.dries007.tfc.common.blocks.plant.WaterPlantBlock;
import net.dries007.tfc.common.blocks.plant.ITallPlant.Part;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class TFCFSeagrassBlock extends WaterPlantBlock {
   protected static final EnumProperty<Part> PART = TFCBlockStateProperties.TALL_PLANT_PART;
   public static final BooleanProperty SINGLE = BooleanProperty.create("single");
   protected static final VoxelShape PLANT_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   protected static final VoxelShape SHORTER_PLANT_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);

   public static TFCFSeagrassBlock create(final RegistryPlant plant, final FluidProperty fluid, Properties properties) {
      return new TFCFSeagrassBlock(ExtendedProperties.of(properties)) {
         public RegistryPlant getPlant() {
            return plant;
         }

         public FluidProperty getFluidProperty() {
            return fluid;
         }
      };
   }

   protected TFCFSeagrassBlock(ExtendedProperties properties) {
      super(properties);
      BlockState stateDefinition = (BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(PART, Part.LOWER)).setValue(SINGLE, false);
      IntegerProperty stageProperty = this.getPlant().getStageProperty();
      if (stageProperty != null) {
         stateDefinition = (BlockState)stateDefinition.setValue(stageProperty, 0);
      }

      this.registerDefaultState(stateDefinition);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos pos = context.getClickedPos();
      FluidState fluidState = context.getLevel().getFluidState(pos);
      BlockState state = this.updateStateWithCurrentMonth((BlockState)this.defaultBlockState().setValue(SINGLE, true));
      if (this.getFluidProperty().canContain(fluidState.getType())) {
         state = (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
      }

      return state;
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      Part part = (Part)state.getValue(PART);
      if (facing.getAxis() != Axis.Y
         || part == Part.LOWER != (facing == Direction.UP)
         || facingState.getBlock() == this && facingState.getValue(PART) != part) {
         if (facing.getAxis() != Axis.Y
            || part == Part.LOWER != (facing == Direction.UP)
            || facingState.getBlock() == this && facingState.getValue(PART) != part) {
            return part == Part.LOWER && facing == Direction.DOWN && !state.canSurvive(level, currentPos)
               ? this.placeWaterOrAir(level, currentPos)
               : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
         } else {
            return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
         }
      } else if ((Boolean)state.getValue(SINGLE)
         && level.getBlockState(currentPos.below()).getBlock() instanceof TFCFSeagrassBlock
         && (Boolean)level.getBlockState(currentPos.below()).getValue(SINGLE)) {
         FluidState fluidState = level.getFluidState(currentPos.below());
         return fluidState.getType() != Fluids.EMPTY ? fluidState.createLegacyBlock() : Blocks.AIR.defaultBlockState();
      } else {
         FluidState fluidState = level.getFluidState(currentPos);
         return fluidState.getType() != Fluids.EMPTY ? fluidState.createLegacyBlock() : Blocks.AIR.defaultBlockState();
      }
   }

   public BlockState placeWaterOrAir(LevelAccessor level, BlockPos currentPos) {
      FluidState fluidState = level.getFluidState(currentPos.below());
      return fluidState.getType() != Fluids.EMPTY ? fluidState.createLegacyBlock() : Blocks.AIR.defaultBlockState();
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!state.canSurvive(level, pos)) {
         this.placeWaterOrAir(level, pos);
      }
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      if (state.getValue(this.getFluidProperty()) == this.getFluidProperty().keyFor(Fluids.EMPTY)) {
         return false;
      } else if (state.getValue(PART) != Part.LOWER && !(Boolean)state.getValue(SINGLE)) {
         BlockState blockstate = level.getBlockState(pos.below());
         return state.getBlock() != this ? super.canSurvive(state, level, pos) : blockstate.getBlock() == this && blockstate.getValue(PART) == Part.LOWER;
      } else {
         return level.getBlockState(pos.below()).getBlock() instanceof TFCFSeagrassBlock
            ? !(Boolean)level.getBlockState(pos.below()).getValue(SINGLE)
            : super.canSurvive(state, level, pos) && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      Part part = (Part)state.getValue(PART);
      Boolean single = (Boolean)state.getValue(SINGLE);
      return part != Part.LOWER && single ? SHORTER_PLANT_SHAPE : PLANT_SHAPE;
   }

   public OffsetType getOffsetType() {
      return OffsetType.XZ;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      if (this.getPlant().getStageProperty() != null) {
         builder.add(new Property[]{this.getPlant().getStageProperty()});
      }

      super.createBlockStateDefinition(builder.add(new Property[]{PART, SINGLE}));
   }

   public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      if (!level.isClientSide) {
         if (player.isCreative()) {
            if (state.getValue(PART) == Part.UPPER) {
               BlockPos blockpos = pos.below();
               BlockState blockstate = level.getBlockState(blockpos);
               if (blockstate.getBlock() == state.getBlock() && blockstate.getValue(PART) == Part.LOWER) {
                  level.setBlockAndUpdate(blockpos, (BlockState)((BlockState)state.setValue(AGE, 0)).setValue(SINGLE, true));
               }
            }
         } else {
            dropResources(state, level, pos, null, player, player.getMainHandItem());
            level.setBlockAndUpdate(pos.below(), (BlockState)((BlockState)state.setValue(AGE, 0)).setValue(SINGLE, true));
         }
      }
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      super.randomTick(state, level, pos, random);
      int j = (Integer)state.getValue(AGE);
      if (random.nextDouble() < (Double)TFCConfig.SERVER.plantGrowthChance.get()) {
         if (j == 3 && this.canGrow(level, pos, state)) {
            if (this.canContainDouble(level, pos)) {
               this.grow(level, random, pos, state);
            }
         } else if (j < 3) {
            FluidState fluidState = level.getFluidState(pos);
            if (this.getFluidProperty().canContain(fluidState.getType())) {
               level.setBlockAndUpdate(
                  pos,
                  (BlockState)((BlockState)((BlockState)state.setValue(AGE, j + 1)).setValue(PART, (Part)state.getValue(PART)))
                     .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()))
               );
            }
         }
      }

      if (PlantRegrowth.canSpread(level, random)) {
         BlockPos newPos = PlantRegrowth.spreadSelf(state, level, pos, random, 2, 2, 4);
         if (newPos != null) {
            FluidState fluidState = level.getFluidState(newPos);
            if (this.getFluidProperty().canContain(fluidState.getType())) {
               level.setBlockAndUpdate(
                  newPos,
                  this.updateStateWithCurrentMonth(
                     (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(AGE, 0)).setValue(PART, Part.LOWER)).setValue(SINGLE, true))
                        .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()))
                  )
               );
            }
         }
      }
   }

   public boolean canGrow(ServerLevel level, BlockPos pos, BlockState state) {
      return (Boolean)state.getValue(SINGLE) && !level.getBlockState(pos.above()).isAir();
   }

   public void grow(ServerLevel level, RandomSource rand, BlockPos pos, BlockState state) {
      IntegerProperty stageProperty = this.getPlant().getStageProperty();
      FluidState fluidState = level.getFluidState(pos);
      FluidState fluidStateAbove = level.getFluidState(pos.above());
      if (stageProperty != null) {
         level.setBlockAndUpdate(
            pos.above(1),
            (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(AGE, 0))
                        .setValue(stageProperty, this.getPlant().stageFor(Calendars.SERVER.getCalendarMonthOfYear())))
                     .setValue(PART, Part.UPPER))
                  .setValue(SINGLE, false))
               .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidStateAbove.getType()))
         );
         BlockState blockState = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(AGE, 0))
                     .setValue(stageProperty, this.getPlant().stageFor(Calendars.SERVER.getCalendarMonthOfYear())))
                  .setValue(PART, Part.LOWER))
               .setValue(SINGLE, false))
            .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
         level.setBlockAndUpdate(pos, blockState);
         blockState.neighborChanged(level, pos.above(1), this, pos, false);
      } else {
         level.setBlockAndUpdate(
            pos.above(1),
            (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(AGE, 0)).setValue(PART, Part.UPPER)).setValue(SINGLE, false))
               .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidStateAbove.getType()))
         );
         BlockState blockState = (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(AGE, 0)).setValue(PART, Part.LOWER)).setValue(SINGLE, false))
            .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
         level.setBlockAndUpdate(pos, blockState);
         blockState.neighborChanged(level, pos.above(1), this, pos, false);
      }
   }

   public void placeTwoHalves(LevelAccessor level, BlockPos pos, int flags, RandomSource random, Fluid fluid) {
      FluidState fluidState = level.getFluidState(pos);
      if (this.getFluidProperty().canContain(fluidState.getType()) && fluidState.getType() != Fluids.EMPTY) {
         if (level.getBlockState(pos.above()).isAir()) {
            this.placeSingle(level, pos, flags, random, fluid);
         } else if (this.canContainDouble(level, pos)) {
            int age = random.nextInt(3);
            level.setBlock(
               pos,
               this.updateStateWithCurrentMonth(
                  (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(TFCBlockStateProperties.TALL_PLANT_PART, Part.LOWER))
                           .setValue(TFCBlockStateProperties.AGE_3, age))
                        .setValue(SINGLE, false))
                     .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluid))
               ),
               flags
            );
            level.setBlock(
               pos.above(),
               this.updateStateWithCurrentMonth(
                  (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(TFCBlockStateProperties.TALL_PLANT_PART, Part.UPPER))
                           .setValue(TFCBlockStateProperties.AGE_3, age))
                        .setValue(SINGLE, false))
                     .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluid))
               ),
               flags
            );
         }
      }
   }

   public void placeSingle(LevelAccessor level, BlockPos pos, int flags, RandomSource random, Fluid fluid) {
      int age = random.nextInt(3);
      level.setBlock(
         pos,
         this.updateStateWithCurrentMonth(
            (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(TFCBlockStateProperties.TALL_PLANT_PART, Part.LOWER))
                     .setValue(TFCBlockStateProperties.AGE_3, age))
                  .setValue(SINGLE, true))
               .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluid))
         ),
         flags
      );
   }

   public boolean canContainDouble(LevelAccessor level, BlockPos pos) {
      FluidState fluidState = level.getFluidState(pos);
      FluidState fluidStateAbove = level.getFluidState(pos.above());
      return this.getFluidProperty().canContain(fluidState.getType())
         && fluidState.getType() != Fluids.EMPTY
         && this.getFluidProperty().canContain(fluidStateAbove.getType())
         && fluidStateAbove.getType() != Fluids.EMPTY;
   }
}
