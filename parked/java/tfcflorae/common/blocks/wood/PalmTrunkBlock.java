package tfcflorae.common.blocks.wood;
import tfcflorae.common.TFCFBlockStateProperties;

import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.fruit.IBushBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import tfcflorae.Config;
import tfcflorae.common.blocks.ICooldown;

public class PalmTrunkBlock extends TFCPalmTrunkBlock implements IBushBlock, HoeOverlayBlock, ICooldown {
   public static final EnumProperty<Lifecycle> LIFECYCLE = TFCBlockStateProperties.LIFECYCLE;
   public final TFCFWood wood;
   @Nullable
   public final Supplier<? extends Block> leaves;
   @Nullable
   public final Supplier<? extends Block> trunk;
   @Nullable
   public final Supplier<? extends Block> fruitBlock;
   public final Supplier<? extends Item> productItem;
   public final Supplier<ClimateRange> climateRange;
   public final Lifecycle[] lifecycle;
   public long lastUpdateTick;
   public long cooldown;

   public static PalmTrunkBlock create(
      TFCFWood wood,
      ExtendedProperties properties,
      @Nullable Supplier<? extends Block> leaves,
      @Nullable Supplier<? extends Block> trunk,
      @Nullable Supplier<? extends Block> fruitBlock,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      Supplier<ClimateRange> climateRange
   ) {
      return new PalmTrunkBlock(wood, properties, leaves, trunk, fruitBlock, productItem, lifecycle, climateRange) {
      };
   }

   public PalmTrunkBlock(
      TFCFWood wood,
      ExtendedProperties properties,
      @Nullable Supplier<? extends Block> leaves,
      @Nullable Supplier<? extends Block> trunk,
      @Nullable Supplier<? extends Block> fruitBlock,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      Supplier<ClimateRange> climateRange
   ) {
      super(wood, properties, leaves, trunk);
      this.wood = wood;
      this.leaves = leaves;
      this.trunk = trunk;
      this.fruitBlock = fruitBlock;
      this.climateRange = climateRange;
      this.lifecycle = lifecycle;
      this.productItem = productItem;
      this.cooldown = Long.MIN_VALUE;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition
                                          .any())
                                       .setValue(AGE, 0))
                                    .setValue(LIFECYCLE, Lifecycle.HEALTHY))
                                 .setValue(TFCFBlockStateProperties.NATURAL, true))
                              .setValue(TOP_BLOCK, false))
                           .setValue(NORTH, false))
                        .setValue(EAST, false))
                     .setValue(SOUTH, false))
                  .setValue(WEST, false))
               .setValue(UP, false))
            .setValue(DOWN, false)
      );
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{LIFECYCLE}));
   }

   @Override
   public Block getTrunk() {
      return this.trunk.get();
   }

   @Override
   public Block getLeaves() {
      return this.leaves.get();
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      super.randomTick(state, level, pos, random);
      if (random.nextInt((Integer)Config.COMMON.fruitingLeavesUpdateChance.get()) == 0
         && (Integer)state.getValue(AGE) >= 5
         && (Boolean)state.getValue(TOP_BLOCK)) {
         Lifecycle currentLifecycle = (Lifecycle)state.getValue(LIFECYCLE);
         Lifecycle expectedLifecycle = this.getLifecycleForCurrentMonth();
         if ((Boolean)state.getValue(TFCFBlockStateProperties.NATURAL)) {
            if (currentLifecycle != expectedLifecycle && (level.getRawBrightness(pos, 0) >= 11 || level.isDay())) {
               this.lastUpdateTick = Calendars.SERVER.getTicks();
               this.onUpdate(level, pos, state);
            }

            if (this.getCooldown(level, this.cooldown)) {
               for (Direction direction : Plane.HORIZONTAL) {
                  BlockPos posBelow = pos.relative(direction).below();
                  BlockState stateBelow = level.getBlockState(posBelow);
                  if (state.getValue(LIFECYCLE) == Lifecycle.FRUITING && stateBelow.isAir()) {
                     this.setCooldown(level, Math.round(24000.0 * (double)Calendars.get(level).getCalendarDaysInMonth() * 0.15), this.cooldown);
                     level.setBlock(pos, (BlockState)this.fruitBlock.get().defaultBlockState().setValue(PalmLeavesBlock.DIRECTION, direction.getOpposite()), 3);
                  } else if (stateBelow.getBlock().equals(this.fruitBlock.get())) {
                     this.setCooldown(level, (long)Mth.ceil(24000.0 * (double)Calendars.get(level).getCalendarDaysInMonth() * 0.15), this.cooldown);
                     if ((Integer)stateBelow.getValue(PalmFruitBlock.AGE) < 3) {
                        stateBelow.setValue(PalmFruitBlock.AGE, Mth.clamp((Integer)stateBelow.getValue(PalmFruitBlock.AGE) + 1, 0, 3));
                     } else if (random.nextInt((Integer)Config.COMMON.fruitingLeavesUpdateChance.get()) == 0) {
                        level.destroyBlock(posBelow, false);
                     }
                  }
               }
            }
         }
      }
   }

   public void onUpdate(Level level, BlockPos pos, BlockState state) {
      if ((Boolean)state.getValue(TFCFBlockStateProperties.NATURAL)) {
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

   public long getTicksSinceBushUpdate() {
      return Calendars.SERVER.getTicks() - this.lastUpdateTick;
   }

   public void addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug) {
      ClimateRange range = this.climateRange.get();
      text.add(FarmlandBlock.getHydrationTooltip(level, pos, range, false, getHydration(level, pos)));
      text.add(FarmlandBlock.getTemperatureTooltip(level, pos, range, false));
   }

   public boolean mayDie(Level level, BlockPos pos, BlockState state, int monthsSpentDying) {
      return monthsSpentDying >= 4;
   }

   public static int getHydration(Level level, BlockPos pos) {
      return (int)(Climate.getRainfall(level, pos) / 5.0F);
   }

   public static boolean checkAndSetDormant(Level level, BlockPos pos, BlockState state, Lifecycle current, Lifecycle expected) {
      if (expected == Lifecycle.DORMANT) {
         if (expected != current) {
            level.setBlock(pos, (BlockState)state.setValue(LIFECYCLE, Lifecycle.DORMANT), 3);
         }

         return true;
      } else {
         return false;
      }
   }

   public BlockState stateAfterPicking(BlockState state) {
      return (BlockState)state.setValue(LIFECYCLE, Lifecycle.HEALTHY);
   }

   public ItemStack getProductItem(RandomSource random) {
      return new ItemStack((ItemLike)this.productItem.get());
   }

   public Lifecycle getLifecycleForCurrentMonth() {
      return this.getLifecycleForMonth(Calendars.SERVER.getCalendarMonthOfYear());
   }

   public Lifecycle getLifecycleForMonth(Month month) {
      return this.lifecycle[month.ordinal()];
   }
}
