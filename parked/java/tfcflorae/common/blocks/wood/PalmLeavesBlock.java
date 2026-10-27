package tfcflorae.common.blocks.wood;
import tfcflorae.common.TFCFBlockStateProperties;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public abstract class PalmLeavesBlock extends TFCFLeavesBlock {
   public static final DirectionProperty DIRECTION = DirectionProperty.create("direction", Plane.HORIZONTAL);
   public static final BooleanProperty CORNER_BLOCK = BooleanProperty.create("corner_block");
   public static final BooleanProperty CENTER_BLOCK = BooleanProperty.create("center_block");
   public static final BooleanProperty TOP_BLOCK = BooleanProperty.create("top_block");
   public final int maxDecayDistance;
   public final ExtendedProperties properties;
   public final Supplier<? extends Item> productItem;
   public final Supplier<ClimateRange> climateRange;
   public final Lifecycle[] lifecycle;
   @Nullable
   public final Supplier<? extends Block> fallenLeaves;
   @Nullable
   public final Supplier<? extends Block> fallenTwig;
   @Nullable
   public final Supplier<? extends Block> sapling;
   @Nullable
   public final Supplier<? extends Block> trunk;
   public long lastUpdateTick;

   public static PalmLeavesBlock create(
      ExtendedProperties properties,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      int autumnIndex,
      Supplier<ClimateRange> climateRange,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling,
      @Nullable Supplier<? extends Block> trunk
   ) {
      final IntegerProperty distanceProperty = distanceProperty();
      return new PalmLeavesBlock(properties, productItem, lifecycle, autumnIndex, climateRange, fallenLeaves, fallenTwig, sapling, trunk) {
         @Override
         protected IntegerProperty getDistanceProperty() {
            return distanceProperty;
         }
      };
   }

   public PalmLeavesBlock(
      ExtendedProperties properties,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      int autumnIndex,
      Supplier<ClimateRange> climateRange,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling,
      @Nullable Supplier<? extends Block> trunk
   ) {
      super(properties, productItem, lifecycle, autumnIndex, climateRange, fallenLeaves, fallenTwig, sapling);
      this.maxDecayDistance = java.util.Collections.max(getDistanceProperty().getPossibleValues()) - 1;
      this.properties = properties;
      this.climateRange = climateRange;
      this.lifecycle = lifecycle;
      this.productItem = productItem;
      this.fallenLeaves = fallenLeaves;
      this.fallenTwig = fallenTwig;
      this.sapling = sapling;
      this.trunk = trunk;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(PERSISTENT, false))
                     .setValue(LIFECYCLE, Lifecycle.HEALTHY))
                  .setValue(CORNER_BLOCK, false))
               .setValue(CENTER_BLOCK, false))
            .setValue(TOP_BLOCK, false)
      );
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(
         new Property[]{PERSISTENT, this.getDistanceProperty(), this.getFluidProperty(), LIFECYCLE, DIRECTION, CORNER_BLOCK, CENTER_BLOCK, TOP_BLOCK}
      );
   }

   // getDistanceProperty() 继承自 TFCFLeavesBlock（返回 TFC 的 DISTANCE_10），这里不再重复定义。

   public boolean isTopBlock(BlockState state, LevelReader level, BlockPos pos) {
      return Helpers.isBlock(level.getBlockState(pos.below()), this.trunk.get()) && (Boolean)level.getBlockState(pos.below()).getValue(PalmTrunkBlock.TOP_BLOCK);
   }

   public boolean isCornerBlock(BlockState state, LevelReader level, BlockPos pos) {
      int connectedFaces = 0;

      for (Direction direction : Direction.values()) {
         connectedFaces += Helpers.isBlock(level.getBlockState(pos.relative(direction)), this.trunk.get()) ? 1 : 0;
      }

      return connectedFaces <= 0;
   }

   public boolean isCenterBlock(BlockState state, LevelReader level, BlockPos pos) {
      int connectedFaces = 0;

      for (Direction direction : Direction.values()) {
         connectedFaces += Helpers.isBlock(level.getBlockState(pos.relative(direction)), this.trunk.get()) ? 1 : 0;
      }

      return connectedFaces > 0;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos relativePos = pos.relative(((Direction)state.getValue(DIRECTION)).getOpposite());
      return level.getBlockState(relativePos).getBlock() != this.trunk.get()
            && level.getBlockState(relativePos.below()).getBlock() != this.trunk.get()
            && !(level.getBlockState(relativePos).getBlock() instanceof PalmLeavesBlock)
         ? this.isTopBlock(state, level, pos)
         : true;
   }

   @Override
   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      super.updateShape(state, facing, facingState, level, currentPos, facingPos);
      FluidHelpers.tickFluid(level, currentPos, state);
      if (!state.canSurvive(level, currentPos)) {
         level.destroyBlock(currentPos, !(Boolean)state.getValue(PERSISTENT));
         return Blocks.AIR.defaultBlockState();
      } else {
         for (Direction direction : Plane.HORIZONTAL) {
            if (this.isCornerBlock(state, level, currentPos)) {
               ((BlockState)((BlockState)state.setValue(DIRECTION, direction)).setValue(CORNER_BLOCK, true)).setValue(CENTER_BLOCK, false);
            } else if (this.isCenterBlock(state, level, currentPos)) {
               ((BlockState)((BlockState)state.setValue(DIRECTION, direction)).setValue(CORNER_BLOCK, false)).setValue(CENTER_BLOCK, true);
            } else if (this.isTopBlock(state, level, currentPos)) {
               ((BlockState)state.setValue(DIRECTION, direction)).setValue(TOP_BLOCK, true);
            }
         }

         return state;
      }
   }

   @Override
   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      super.tick(state, level, pos, random);
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   @Override
   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if ((Integer)state.getValue(this.getDistanceProperty()) > 2 && !(Boolean)state.getValue(PERSISTENT)) {
         level.removeBlock(pos, false);
         if (random.nextFloat() < 0.01F) {
            this.createDestructionEffects(state, level, pos, random, false);
         }

         doParticles(
            level,
            (double)((float)pos.getX() + random.nextFloat()),
            (double)((float)pos.getY() + random.nextFloat()),
            (double)((float)pos.getZ() + random.nextFloat()),
            1
         );
      }

      super.randomTick(state, level, pos, random);
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      return (BlockState)state.setValue(DIRECTION, rot.rotate((Direction)state.getValue(DIRECTION)));
   }

   public BlockState mirror(BlockState state, Mirror mirrorIn) {
      return state.rotate(mirrorIn.getRotation((Direction)state.getValue(DIRECTION)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = this.defaultBlockState();
      Direction direction = context.getHorizontalDirection().getOpposite();
      return FluidHelpers.fillWithFluid(
         (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(DIRECTION, direction)).setValue(CORNER_BLOCK, this.isCornerBlock(state, level, pos)))
               .setValue(CENTER_BLOCK, this.isCenterBlock(state, level, pos)))
            .setValue(TOP_BLOCK, this.isTopBlock(state, level, pos)),
         level.getBlockState(pos).getFluidState().getType()
      );
   }

   public OffsetType getOffsetType() {
      return OffsetType.XYZ;
   }

   @Override
   public void onUpdate(Level level, BlockPos pos, BlockState state) {
      if (!(Boolean)state.getValue(PERSISTENT)) {
         Lifecycle currentLifecycle = (Lifecycle)state.getValue(LIFECYCLE);
         Lifecycle expectedLifecycle = this.getLifecycleForCurrentMonth();
         if (!checkAndSetDormant(level, pos, state, currentLifecycle, expectedLifecycle)) {
            long deltaTicks = Math.min(this.getTicksSinceBushUpdate(), Calendars.SERVER.getCalendarTicksInYear());
            long currentCalendarTick = Calendars.SERVER.getCalendarTicks();
            long nextCalendarTick = currentCalendarTick - deltaTicks;
            ClimateRange range = this.climateRange.get();
            int hydration = getHydration(level, pos);
            int monthsSpentDying = 0;

            do {
               nextCalendarTick = Math.min(nextCalendarTick + Calendars.SERVER.getCalendarTicksInMonth(), currentCalendarTick);
               float temperatureAtNextTick = Climate.getTemperature(level, pos, nextCalendarTick, Calendars.SERVER.getCalendarDaysInMonth());
               Lifecycle lifecycleAtNextTick = this.getLifecycleForMonth(
                  ICalendar.getMonthOfYear(nextCalendarTick, (long)Calendars.SERVER.getCalendarDaysInMonth())
               );
               if (range.checkBoth(hydration, temperatureAtNextTick, false)) {
                  currentLifecycle = currentLifecycle.advanceTowards(lifecycleAtNextTick);
               } else {
                  currentLifecycle = Lifecycle.DORMANT;
               }

               if (lifecycleAtNextTick != Lifecycle.DORMANT && currentLifecycle == Lifecycle.DORMANT) {
                  monthsSpentDying++;
               } else {
                  monthsSpentDying = 0;
               }
            } while (nextCalendarTick < currentCalendarTick);

            BlockState newState;
            if (this.mayDie(level, pos, state, monthsSpentDying)) {
               newState = Blocks.AIR.defaultBlockState();
            } else {
               newState = (BlockState)state.setValue(LIFECYCLE, currentLifecycle);
            }

            if (state != newState) {
               level.setBlock(pos, newState, 3);
               level.blockUpdated(pos, newState.getBlock());
            }
         }
      }
   }
}
