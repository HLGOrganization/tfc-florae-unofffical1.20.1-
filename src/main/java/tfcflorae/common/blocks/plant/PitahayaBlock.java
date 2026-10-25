package tfcflorae.common.blocks.plant;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.PlantBlock;
import net.dries007.tfc.common.blocks.plant.fruit.IBushBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public abstract class PitahayaBlock extends PlantBlock implements ILeavesBlock, IBushBlock, HoeOverlayBlock {
   public static final EnumProperty<Lifecycle> LIFECYCLE = TFCBlockStateProperties.LIFECYCLE;
   private static final int MONTHS_SPENT_DORMANT_TO_DIE = 4;
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   protected static final VoxelShape NORTH_SHAPE = box(0.0, 0.0, 12.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape SOUTH_SHAPE = box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0);
   protected static final VoxelShape WEST_SHAPE = box(12.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape EAST_SHAPE = box(0.0, 0.0, 0.0, 4.0, 16.0, 16.0);
   public static final BooleanProperty TOP = BooleanProperty.create("top");
   protected final Supplier<? extends Item> productItem;
   protected final Supplier<ClimateRange> climateRange;
   private final Lifecycle[] lifecycle;
   private long lastUpdateTick;
   protected static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.of(
      Direction.NORTH, NORTH_SHAPE, Direction.SOUTH, SOUTH_SHAPE, Direction.WEST, WEST_SHAPE, Direction.EAST, EAST_SHAPE
   );

   public static int getHydration(Level level, BlockPos pos) {
      return (int)(Climate.getRainfall(level, pos) / 5.0F);
   }

   public static PitahayaBlock create(
      final RegistryPlant plant,
      ExtendedProperties properties,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      Supplier<ClimateRange> climateRange
   ) {
      return new PitahayaBlock(properties, productItem, lifecycle, climateRange) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected PitahayaBlock(ExtendedProperties properties, Supplier<? extends Item> productItem, Lifecycle[] lifecycle, Supplier<ClimateRange> climateRange) {
      super(properties);
      Preconditions.checkArgument(lifecycle.length == 12, "Lifecycle length must be 12");
      this.climateRange = climateRange;
      this.lifecycle = lifecycle;
      this.productItem = productItem;
      this.lastUpdateTick = Calendars.SERVER.getTicks();
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(FACING, Direction.NORTH)).setValue(TOP, false))
            .setValue(LIFECYCLE, Lifecycle.HEALTHY)
      );
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, false);
      }

      if (level.getBlockState(pos.above()).isSolid() && !(level.getBlockState(pos.above()).getBlock() instanceof PitahayaBlock)) {
         state.setValue(TOP, true);
      }

      if (!level.getBlockState(pos.above()).isSolid()) {
         state.setValue(TOP, false);
      }
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      if (level.getBlockState(currentPos.above()).isSolid() && !(level.getBlockState(currentPos.above()).getBlock() instanceof PitahayaBlock)) {
         state.setValue(TOP, true);
      }

      if (!level.getBlockState(currentPos.above()).isSolid()) {
         state.setValue(TOP, false);
      }

      if (level instanceof ServerLevel server && this.getLifecycleForCurrentMonth() != this.getLifecycleForMonth(Calendars.SERVER.getCalendarMonthOfYear())) {
         this.onUpdate(server, currentPos, state);
      }

      return direction.getOpposite() == state.getValue(FACING) && !this.canSurvive(state, level, currentPos) ? Blocks.AIR.defaultBlockState() : state;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState attachedState = level.getBlockState(pos.relative(((Direction)state.getValue(FACING)).getOpposite()));
      return (Helpers.isBlock(attachedState, BlockTags.LOGS) || Helpers.isBlock(attachedState, net.dries007.tfc.common.TFCTags.Blocks.WILD_CROP_GROWS_ON))
         && (
            level.getBlockState(pos.below()).is(this)
               || Helpers.isBlock(level.getBlockState(pos.below()), net.dries007.tfc.common.TFCTags.Blocks.GRASS_PLANTABLE_ON)
               || Helpers.isBlock(level.getBlockState(pos.below()), net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
               || Helpers.isBlock(level.getBlockState(pos.below()), net.dries007.tfc.common.TFCTags.Blocks.WILD_CROP_GROWS_ON)
         );
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPES.get(state.getValue(FACING));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = context.getClickedFace();
      return direction.getAxis() != Axis.Y ? (BlockState)this.updateStateWithCurrentMonth(this.defaultBlockState()).setValue(FACING, direction) : null;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{FACING, TOP, LIFECYCLE}));
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      BlockState attachedState = level.getBlockState(pos.above().relative(((Direction)state.getValue(FACING)).getOpposite()));
      if (this.getLifecycleForCurrentMonth() != this.getLifecycleForMonth(Calendars.SERVER.getCalendarMonthOfYear())) {
         this.onUpdate(level, pos, state);
         this.lastUpdateTick = Calendars.SERVER.getTicks();
      }

      if (random.nextDouble() < (Double)TFCConfig.SERVER.plantGrowthChance.get()
         && (Integer)state.getValue(AGE) >= 3
         && (level.getBlockState(pos.above()).isAir() || EnvironmentHelpers.isWorldgenReplaceable(level.getBlockState(pos.above())))
         && (Helpers.isBlock(attachedState, BlockTags.LOGS) || Helpers.isBlock(attachedState, net.dries007.tfc.common.TFCTags.Blocks.WILD_CROP_GROWS_ON))
         && (
            level.getBlockState(pos.below()).is(this)
               || Helpers.isBlock(level.getBlockState(pos.below()), net.dries007.tfc.common.TFCTags.Blocks.GRASS_PLANTABLE_ON)
               || Helpers.isBlock(level.getBlockState(pos.below()), net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
               || Helpers.isBlock(level.getBlockState(pos.below()), net.dries007.tfc.common.TFCTags.Blocks.WILD_CROP_GROWS_ON)
         )) {
         level.setBlock(pos.above(), this.updateStateWithCurrentMonth((BlockState)this.defaultBlockState().setValue(FACING, (Direction)state.getValue(FACING))), 2);
      }

      super.randomTick(state, level, pos, random);
   }

   public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
      entity.hurt(entity.damageSources().cactus(), 1.0F);
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

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (state.getValue(LIFECYCLE) == Lifecycle.FRUITING && this.productItem != null) {
         level.playSound(player, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.PLAYERS, 1.0F, level.getRandom().nextFloat() + 0.7F + 0.3F);
         if (!level.isClientSide()) {
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
      return monthsSpentDying >= 4;
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
