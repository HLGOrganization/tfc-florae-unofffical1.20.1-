package tfcflorae.common.blocks.wood;
import tfcflorae.common.TFCFBlockStateProperties;
import tfcflorae.util.TFCFLog;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.fruit.IBushBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.calendar.Season;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import tfcflorae.Config;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.entities.Silkmoth;
import tfcflorae.common.entities.TFCFEntities;
import net.minecraft.world.level.storage.loot.LootParams;

public abstract class TFCFLeavesBlock extends TFCLeavesBlock implements IBushBlock, HoeOverlayBlock {
   public static final int MONTHS_SPENT_DORMANT_TO_DIE = 4;
   public static final EnumProperty<Lifecycle> LIFECYCLE = TFCBlockStateProperties.LIFECYCLE;
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
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
   public long lastUpdateTick;

   public static TFCFLeavesBlock create(
      ExtendedProperties properties, Supplier<? extends Item> productItem, Lifecycle[] lifecycle, int maxDecayDistance, Supplier<ClimateRange> climateRange
   ) {
      return create(properties, productItem, lifecycle, maxDecayDistance, climateRange, null, null, null);
   }

   public static TFCFLeavesBlock create(
      ExtendedProperties properties,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      int autumnIndex,
      Supplier<ClimateRange> climateRange,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling
   ) {
      return new TFCFLeavesBlock(properties, productItem, lifecycle, autumnIndex, climateRange, fallenLeaves, fallenTwig, sapling) {
      };
   }

   /**
    * 树叶的“到树干距离”属性。必须用 TFC 的 {@code DISTANCE_10}（范围 1..10），
    * 而不是原版 {@code BlockStateProperties.DISTANCE}（1..7）：
    * 父类 {@code maxDecayDistance} 由该属性范围推导（{@code max - 1}），
    * 用 1..7 会算出 6，而新长出的树叶距离常常是 7/8，会被立刻判定为超距删除。
    */
   public static IntegerProperty distanceProperty() {
      return net.dries007.tfc.common.blocks.TFCBlockStateProperties.DISTANCE_10;
   }

   @Override
   protected IntegerProperty getDistanceProperty() {
      return distanceProperty();
   }

   public TFCFLeavesBlock(
      ExtendedProperties properties,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      int autumnIndex,
      Supplier<ClimateRange> climateRange,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling
   ) {
      super(properties, autumnIndex, fallenLeaves, fallenTwig);
      Preconditions.checkArgument(lifecycle.length == 12, "Lifecycle length must be 12");
      // TFC 3.x 的 TFCLeavesBlock 构造器里，第二个 int 参数是 **autumnIndex**（秋色索引），
      // maxDecayDistance 是它自己按属性范围算出来的：
      //     maxDecayDistance = Collections.max(getDistanceProperty().getPossibleValues()) - 1
      // 用 DISTANCE_10(1..10) 时即为 9。父类该字段是 private，若这里沿用 1.18.2 的“固定 7”，
      // 我们的判定就会和父类用两个不同的上限：叶子距离可以存到 8/9，
      // 而 8 > 7 会被立刻判成“离树干太远”删掉 —— 这正是「树叶长出来一瞬间就消失」的原因。
      // 因此这里与 TFC 一致，从属性范围推导。
      this.maxDecayDistance = Collections.max(getDistanceProperty().getPossibleValues()) - 1;
      this.properties = properties;
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
      // 只调用父类。TFC 的 TFCLeavesBlock.tick 已经完整实现了“按到树干距离衰减/删除”的逻辑
      // （内部用 getDistanceProperty() 与它自己的 maxDecayDistance 判定）。
      // 1.18.2 版本在这之后又用 updateDistanceNew() 重复算了一遍并改写方块状态，
      // 两套结果不一致时就会把树叶状态反复改坏——这是“树叶瞬间消失”的根因之一。
      final boolean before = !level.getBlockState(pos).isAir();
      final int distBefore = state.getValue(this.getDistanceProperty());
      super.tick(state, level, pos, rand);
      if (TFCFLog.sapling() && before && level.getBlockState(pos).isAir()) {
         TFCFLog.saplingInfo("[树叶被移除] %s @%s | 移除前 distance=%d | maxDecay=%d | persistent=%s | 上=%s 下=%s",
            level.getBlockState(pos.below()).getBlock(), pos, distBefore, this.maxDecayDistance,
            state.getValue(PERSISTENT),
            level.getBlockState(pos.above()).getBlock(),
            level.getBlockState(pos.below()).getBlock());
      }
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      // 同样只走父类。父类的 randomTick 已包含“距离超限则删除”的判定与粒子效果，
      // 此处若再判一次会把刚刚长出来、距离还合法的树叶误删。
      super.randomTick(state, level, pos, random);

      // 以下只保留本模组特有的“果树叶按季节结果/落叶”逻辑，不再触碰 distance。
      if (random.nextInt((Integer)Config.COMMON.fruitingLeavesUpdateChance.get()) == 0 && !(Boolean)state.getValue(PERSISTENT)) {
         Lifecycle currentLifecycle = (Lifecycle)state.getValue(LIFECYCLE);
         Lifecycle expectedLifecycle = this.getLifecycleForCurrentMonth();
         if (currentLifecycle != expectedLifecycle && (level.getRawBrightness(pos, 0) >= 11 || level.isDay())) {
            this.onUpdate(level, pos, state);
            this.lastUpdateTick = Calendars.SERVER.getTicks();
         }

         if (this == TFCFBlocks.WOODS_SEASONAL_LEAVES.get(TFCFWood.MULBERRY).get() && !level.isDay() && state.getValue(LIFECYCLE) != Lifecycle.DORMANT) {
            Month currentMonth = Calendars.SERVER.getCalendarMonthOfYear();
            Season season = currentMonth.getSeason();
            if (random.nextInt(26400) == 0 && season == Season.SUMMER && Climate.getTemperature(level, pos) >= 2.0F) {
               boolean flag = !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
               Direction direction = Direction.getRandom(random);
               Entity entity = ((EntityType)TFCFEntities.SILKMOTH.get()).create(level);
               if (entity != null) {
                  if (entity instanceof Silkmoth moth) {
                     if (moth.getSavedTargetPos() != null && !moth.hasSavedTargetPos() && level.random.nextFloat() < 0.9F) {
                        moth.setSavedTargetPos(moth.getSavedTargetPos());
                     }

                     float f = entity.getBbWidth();
                     double d3 = flag ? 0.0 : 0.55 + (double)(f / 2.0F);
                     double d0 = (double)pos.getX() + 0.5 + d3 * (double)direction.getStepX();
                     double d1 = (double)pos.getY() + 0.5 - (double)(entity.getBbHeight() / 2.0F);
                     double d2 = (double)pos.getZ() + 0.5 + d3 * (double)direction.getStepZ();
                     entity.moveTo(d0, d1, d2, entity.getYRot(), entity.getXRot());
                  }

                  level.playSound((Player)null, pos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                  level.addFreshEntity(entity);
               }
            }
         }
      }

      // 注意：1.18.2 在这里还有一段「树叶随季节点播撒树苗」的逻辑（读 ChunkData 算降雨/森林密度）。
      // TFC 1.20.1 的 TFCLeavesBlock.randomTick 里没有这段，且 ChunkDataProvider 只在 TFC 区块
      // 生成器下存在 —— 在超平坦/虚空世界执行会抛 "Tried to access ChunkDataProvider" 崩服。
      // 已按 1.20.1 行为整体移除；森林自然再生走世界生成的 tfc:forest。
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      // 与 TFC 的 TFCLeavesBlock.updateShape 逐行一致：
      //   1) 只在 currentPos 处推进流体；
      //   2) 距离可能变化时 scheduleTick(1)，由 tick() 重新计算传播。
      // 不调用 super：父类（Block）的 updateShape 会做 canSurvive 判定，
      // 而 TFC 的树叶刻意不这么做（树叶不因下方缺失而掉落，只按到树干距离衰减）。
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
               newState = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
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

   public int getDistanceNew(BlockState neighbor) {
      if (Helpers.isBlock(neighbor.getBlock(), BlockTags.LOGS)) {
         return 0;
      } else {
         return neighbor.getBlock() == this ? (Integer)neighbor.getValue(this.getDistanceProperty()) : this.maxDecayDistance;
      }
   }

   public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }
}
