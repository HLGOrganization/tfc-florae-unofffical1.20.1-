package tfcflorae.common.blocks.wood;
import tfcflorae.common.TFCFBlockStateProperties;

import com.google.common.base.Preconditions;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.fruit.IBushBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.common.blocks.wood.LogBlock;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import tfcflorae.Config;

public class TFCFFruitingLogBlock extends LogBlock implements IBushBlock, HoeOverlayBlock {
   private static final int MONTHS_SPENT_DORMANT_TO_DIE = 4;
   public static final EnumProperty<Lifecycle> LIFECYCLE = TFCBlockStateProperties.LIFECYCLE;
   protected final Supplier<? extends Item> productItem;
   protected final Supplier<ClimateRange> climateRange;
   private final Lifecycle[] lifecycle;
   @Nullable
   private final Supplier<? extends Block> stripped;
   private final ExtendedProperties properties;
   private long lastUpdateTick;

   public TFCFFruitingLogBlock(
      ExtendedProperties properties,
      @Nullable Supplier<? extends Block> stripped,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      Supplier<ClimateRange> climateRange
   ) {
      super(properties, stripped);
      Preconditions.checkArgument(lifecycle.length == 12, "Lifecycle length must be 12");
      this.stripped = stripped;
      this.properties = properties;
      this.climateRange = climateRange;
      this.lifecycle = lifecycle;
      this.productItem = productItem;
      // lastUpdateTick 保持 0（字段默认值）：注册阶段不能读 TFC 的 Calendars ——
      // 它的静态初始化要读配置，dev 环境会在构造方块时直接抛 IllegalStateException。
      // 生产环境里注册阶段 getTicks() 本来就是 0，因此行为不变。
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(LIFECYCLE, Lifecycle.HEALTHY)).setValue(TFCFBlockStateProperties.NATURAL, false));
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (state.getValue(LIFECYCLE) == Lifecycle.FRUITING && this.productItem != null) {
         level.playSound(player, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.0F, level.getRandom().nextFloat() + 0.7F + 0.3F);
         if (!level.isClientSide()) {
            ItemHandlerHelper.giveItemToPlayer(player, this.getProductItem(level.random));
            level.setBlockAndUpdate(pos, this.stateAfterPicking(state));
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      super.randomTick(state, level, pos, random);
      if (random.nextInt((Integer)Config.COMMON.fruitingLeavesUpdateChance.get()) == 0 && level.isAreaLoaded(pos, 3)) {
         Lifecycle currentLifecycle = (Lifecycle)state.getValue(LIFECYCLE);
         Lifecycle expectedLifecycle = this.getLifecycleForCurrentMonth();
         if ((Boolean)state.getValue(TFCFBlockStateProperties.NATURAL)
            && currentLifecycle != expectedLifecycle
            && (level.getRawBrightness(pos, 0) >= 11 || Calendars.SERVER.getCalendarDayTime() == level.getDayTime())) {
            this.onUpdate(level, pos, state);
            this.lastUpdateTick = Calendars.SERVER.getTicks();
         }
      }
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      // 修复：第 5 个参数应为 currentPos（当前方块位置），1.18.2 移植时误传成了 facingPos，
      // 会让父类在邻居位置做流体/存活检查，树叶/原木在任意邻居更新时被误判。
      super.updateShape(state, facing, facingState, level, currentPos, facingPos);
      return state;
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

   protected boolean mayDie(Level level, BlockPos pos, BlockState state, int monthsSpentDying) {
      return monthsSpentDying >= 4;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{LIFECYCLE, TFCFBlockStateProperties.NATURAL}));
   }

   public static int getHydration(Level level, BlockPos pos) {
      return (int)(Climate.getRainfall(level, pos) / 5.0F);
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

   public BlockState stateAfterPicking(BlockState state) {
      return (BlockState)state.setValue(LIFECYCLE, Lifecycle.HEALTHY);
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

   public boolean isRandomlyTicking(BlockState state) {
      return true;
   }
}
