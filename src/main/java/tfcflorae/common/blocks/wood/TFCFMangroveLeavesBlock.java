package tfcflorae.common.blocks.wood;
import tfcflorae.common.TFCFBlockStateProperties;

import com.google.common.base.Preconditions;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.fruit.IBushBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import tfcflorae.Config;
import net.minecraft.world.level.storage.loot.LootParams;

public abstract class TFCFMangroveLeavesBlock extends TFCLeavesBlock implements IBushBlock, HoeOverlayBlock {
   private static final int MONTHS_SPENT_DORMANT_TO_DIE = 4;
   public static final EnumProperty<Lifecycle> LIFECYCLE = TFCBlockStateProperties.LIFECYCLE;
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   public final RegistryWood wood;
   private final int maxDecayDistance;
   private final ExtendedProperties properties;
   protected final Supplier<? extends Item> productItem;
   protected final Supplier<ClimateRange> climateRange;
   private final Lifecycle[] lifecycle;
   @Nullable
   private final Supplier<? extends Block> fallenLeaves;
   @Nullable
   private final Supplier<? extends Block> fallenTwig;
   @Nullable
   private final Supplier<? extends Block> sapling;
   private long lastUpdateTick;

   public static TFCFMangroveLeavesBlock create(
      ExtendedProperties properties,
      RegistryWood wood,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      int maxDecayDistance,
      Supplier<ClimateRange> climateRange,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling
   ) {
      final IntegerProperty distanceProperty = getDistanceProperty(maxDecayDistance);
      return new TFCFMangroveLeavesBlock(properties, wood, productItem, lifecycle, maxDecayDistance, climateRange, fallenLeaves, fallenTwig, sapling) {
         @Override
         protected IntegerProperty getDistanceProperty() {
            return distanceProperty;
         }
      };
   }

   public static IntegerProperty getDistanceProperty(int maxDecayDistance) {
      // TFC 3.x only ships the vanilla 1-7 ladder (the 1.18 per-wood ladder is gone), so every
      // leaves block reports the standard property; `maxDecayDistance` still drives behaviour.
      return net.dries007.tfc.common.blocks.TFCBlockStateProperties.DISTANCE_10;
   }

   public TFCFMangroveLeavesBlock(
      ExtendedProperties properties,
      RegistryWood wood,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      int maxDecayDistance,
      Supplier<ClimateRange> climateRange,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling
   ) {
      super(properties, maxDecayDistance, fallenLeaves, fallenTwig);
      Preconditions.checkArgument(lifecycle.length == 12, "Lifecycle length must be 12");
      this.maxDecayDistance = maxDecayDistance;
      this.properties = properties;
      this.wood = wood;
      this.climateRange = climateRange;
      this.lifecycle = lifecycle;
      this.productItem = productItem;
      this.fallenLeaves = fallenLeaves;
      this.fallenTwig = fallenTwig;
      this.sapling = sapling;
      this.lastUpdateTick = Calendars.SERVER.getTicks();
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(PERSISTENT, false)).setValue(LIFECYCLE, Lifecycle.HEALTHY));
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected abstract IntegerProperty getDistanceProperty();

   private int getDistanceNew(BlockState neighbor) {
      if (Helpers.isBlock(neighbor.getBlock(), BlockTags.LOGS)) {
         return 0;
      } else {
         return neighbor.getBlock() == this ? (Integer)neighbor.getValue(this.getDistanceProperty()) : this.maxDecayDistance;
      }
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (state.getValue(LIFECYCLE) == Lifecycle.FRUITING && this.productItem != null) {
         level.playSound(player, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.0F, level.getRandom().nextFloat() + 0.7F + 0.3F);
         if (!level.isClientSide()) {
            ItemHandlerHelper.giveItemToPlayer(player, this.getProductItem(level.random));
            level.setBlock(pos, this.stateAfterPicking(state), 3);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      // 只走父类。TFC 的 TFCLeavesBlock.tick 已完整实现按“到树干距离”衰减/删除的逻辑；
      // 1.18.2 版本在此另写了一套 updateDistanceNew 判定且没有调用 super，
      // 既绕过了 TFC 的实现，又用了不一致的字段，是树叶异常消失的根因之一。
      super.tick(state, level, pos, rand);
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      // 父类已经处理了“距离超限删除”，这里不再重复判定，避免误删刚长出的树叶。
      super.randomTick(state, level, pos, random);

      if (random.nextInt((Integer)Config.COMMON.fruitingLeavesUpdateChance.get()) == 0 && !(Boolean)state.getValue(PERSISTENT)) {
         Lifecycle currentLifecycle = (Lifecycle)state.getValue(LIFECYCLE);
         Lifecycle expectedLifecycle = this.getLifecycleForCurrentMonth();
         if (currentLifecycle != expectedLifecycle && (level.getRawBrightness(pos, 0) >= 11 || level.isDay())) {
            this.onUpdate(level, pos, state);
            this.lastUpdateTick = Calendars.SERVER.getTicks();
         }
      }
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      // 与 TFC 的 TFCLeavesBlock.updateShape 一致：
      // 只在 currentPos 处推进流体，并在距离可能变化时 scheduleTick(1)。
      // 1.18.2 移植时这里只写了 super.updateShape 且参数错位（facingPos, facingPos），
      // 既没有推进流体，也没有触发距离重算 —— 红树叶因此永远不会更新 distance。
      FluidHelpers.tickFluid(level, currentPos, state);
      final int distance = this.getDistanceNew(facingState) + 1;
      if (distance != 1 || (Integer)state.getValue(this.getDistanceProperty()) != distance) {
         level.scheduleTick(currentPos, this, 1);
      }
      return state;
   }

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
            } else if (level.getBlockState(pos.below()).isAir()
               && level.getRandom().nextInt(6) == 0
               && !(Boolean)state.getValue(PERSISTENT)
               && state.getValue(LIFECYCLE) != Lifecycle.DORMANT) {
               level.setBlock(pos.below(), (BlockState)this.sapling.get().defaultBlockState().setValue(TFCFMangrovePropaguleBlock.HANGING, true), 3);
               newState = (BlockState)state.setValue(LIFECYCLE, currentLifecycle);
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

   protected boolean mayDie(Level level, BlockPos pos, BlockState state, int monthsSpentDying) {
      return monthsSpentDying >= 4;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{LIFECYCLE});
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

   /**
    * 成熟（FRUITING）状态下被破坏时也掉落果实。
    * 原版只有右键采摘（{@code use}），直接挖掉树叶会把果实一起丢掉，不合理。
    */
   @Override
   public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
      final List<ItemStack> drops = super.getDrops(state, params);
      if (state.getValue(LIFECYCLE) == Lifecycle.FRUITING && this.productItem != null) {
         drops.add(this.getProductItem(params.getLevel().getRandom()));
      }
      return drops;
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

   private int updateDistanceNew(LevelAccessor level, BlockPos pos) {
      int distance = 1 + this.maxDecayDistance;
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (Direction direction : Helpers.DIRECTIONS) {
         mutablePos.set(pos).move(direction);
         distance = Math.min(distance, this.getDistanceNew(level.getBlockState(mutablePos)) + 1);
         if (distance == 1) {
            break;
         }
      }

      return distance;
   }

   public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }
}
