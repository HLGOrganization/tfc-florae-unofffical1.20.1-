package tfcflorae.common.blocks.plant;
import tfcflorae.common.TFCFBlockStateProperties;

import com.google.common.base.Preconditions;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.TFCTags.Items;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.fruit.IBushBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class ShortFruitingCactusBlock extends ShortCactusBlock implements ILeavesBlock, IBushBlock, HoeOverlayBlock {
   public static final EnumProperty<Lifecycle> LIFECYCLE = TFCBlockStateProperties.LIFECYCLE;
   public static final BooleanProperty NATURAL = TFCFBlockStateProperties.NATURAL;
   private static final int MONTHS_SPENT_DORMANT_TO_DIE = 4;
   protected final Supplier<? extends Item> productItem;
   protected final Supplier<ClimateRange> climateRange;
   private final Lifecycle[] lifecycle;
   private long lastUpdateTick;
   protected static final VoxelShape COLLISION_SHAPE = box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
   protected static final VoxelShape OUTLINE_SHAPE = box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

   public static int getHydration(Level level, BlockPos pos) {
      return (int)(Climate.getRainfall(level, pos) / 5.0F);
   }

   public static ShortFruitingCactusBlock create(
      final RegistryPlant plant,
      ExtendedProperties properties,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      Supplier<ClimateRange> climateRange
   ) {
      return new ShortFruitingCactusBlock(properties, productItem, lifecycle, climateRange) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   public ShortFruitingCactusBlock(
      ExtendedProperties properties, Supplier<? extends Item> productItem, Lifecycle[] lifecycle, Supplier<ClimateRange> climateRange
   ) {
      super(properties);
      Preconditions.checkArgument(lifecycle.length == 12, "Lifecycle length must be 12");
      this.climateRange = climateRange;
      this.lifecycle = lifecycle;
      this.productItem = productItem;
      this.lastUpdateTick = Calendars.SERVER.getTicks();
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(LIFECYCLE, Lifecycle.HEALTHY)).setValue(TFCFBlockStateProperties.NATURAL, false));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{TFCFBlockStateProperties.NATURAL, LIFECYCLE}));
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (this.getLifecycleForCurrentMonth() != this.getLifecycleForMonth(Calendars.SERVER.getCalendarMonthOfYear())) {
         this.onUpdate(level, pos, state);
         this.lastUpdateTick = Calendars.SERVER.getTicks();
      }
   }

   public static boolean checkAndSetDormant(Level level, BlockPos pos, BlockState state, Lifecycle current, Lifecycle expected) {
      if (expected == Lifecycle.DORMANT) {
         if (expected != current) {
            level.setBlockAndUpdate(pos, (BlockState)state.setValue(LIFECYCLE, Lifecycle.DORMANT));
         }

         return true;
      } else {
         return false;
      }
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      if (level instanceof ServerLevel server && this.getLifecycleForCurrentMonth() != this.getLifecycleForMonth(Calendars.SERVER.getCalendarMonthOfYear())) {
         this.onUpdate(server, currentPos, state);
      }

      return state;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (state.getValue(LIFECYCLE) == Lifecycle.FRUITING && this.productItem != null) {
         level.playSound(player, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.PLAYERS, 1.0F, level.getRandom().nextFloat() + 0.7F + 0.3F);
         if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!Helpers.isItem(stack, Items.KNIVES) && !Helpers.isItem(stack, net.minecraft.world.item.Items.STICK)) {
               player.hurt(player.damageSources().cactus(), 2.0F);
            }

            ItemHandlerHelper.giveItemToPlayer(player, this.getProductItem(level.random));
            level.setBlockAndUpdate(pos, this.stateAfterPicking(state));
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void onUpdate(Level level, BlockPos pos, BlockState state) {
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
            newState = (BlockState)state.setValue(LIFECYCLE, Lifecycle.DORMANT);
         } else {
            newState = (BlockState)state.setValue(LIFECYCLE, currentLifecycle);
         }

         if (state != newState) {
            level.setBlock(pos, newState, 3);
         }
      }
   }

   public long getTicksSinceBushUpdate() {
      return Calendars.SERVER.getTicks() - this.lastUpdateTick;
   }

   public BlockState stateAfterPicking(BlockState state) {
      return (BlockState)state.setValue(LIFECYCLE, Lifecycle.HEALTHY);
   }

   protected boolean mayDie(Level level, BlockPos pos, BlockState state, int monthsSpentDying) {
      return monthsSpentDying >= 4 && (Boolean)state.getValue(TFCFBlockStateProperties.NATURAL);
   }

   public void addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug) {
      ClimateRange range = this.climateRange.get();
      text.add(FarmlandBlock.getHydrationTooltip(level, pos, range, false, getHydration(level, pos)));
      text.add(FarmlandBlock.getTemperatureTooltip(level, pos, range, false));
   }

   public boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   public ItemStack getProductItem(RandomSource random) {
      return new ItemStack((ItemLike)this.productItem.get());
   }

   protected Lifecycle getLifecycleForCurrentMonth() {
      return this.getLifecycleForMonth(Calendars.SERVER.getCalendarMonthOfYear());
   }

   protected Lifecycle getLifecycleForMonth(Month month) {
      return this.lifecycle[month.ordinal()];
   }
}
