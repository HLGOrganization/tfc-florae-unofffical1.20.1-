package tfcflorae.common.blocks.plant;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.TFCTags.Items;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.PlantBlock;
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
import net.minecraft.Util;
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
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import tfcflorae.util.TFCFHelpers;

public abstract class SaguaroCactusBlock extends PlantBlock implements ILeavesBlock, IBushBlock, HoeOverlayBlock {
   public static final EnumProperty<Lifecycle> LIFECYCLE = TFCBlockStateProperties.LIFECYCLE;
   private static final int MONTHS_SPENT_DORMANT_TO_DIE = 4;
   public static final BooleanProperty HORIZONTAL = BooleanProperty.create("horizontal");
   public static final DirectionProperty HORIZONTAL_DIRECTION = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty NORTH = BooleanProperty.create("north");
   public static final BooleanProperty SOUTH = BooleanProperty.create("south");
   public static final BooleanProperty EAST = BooleanProperty.create("east");
   public static final BooleanProperty WEST = BooleanProperty.create("west");
   protected final Supplier<? extends Item> productItem;
   protected final Supplier<ClimateRange> climateRange;
   private final Lifecycle[] lifecycle;
   private long lastUpdateTick;
   private final HashMap<BlockState, VoxelShape> shapes = Util.make(
      Maps.newHashMap(), m -> this.getStateDefinition().getPossibleStates().forEach(st -> m.put(st, this.getShapeForState(st)))
   );
   public static final Map<Direction, BooleanProperty> FACING_PROPERTIES = Util.make(
      Maps.newEnumMap(Direction.class), enumMap -> {
         enumMap.put(Direction.NORTH, NORTH);
         enumMap.put(Direction.EAST, EAST);
         enumMap.put(Direction.SOUTH, SOUTH);
         enumMap.put(Direction.WEST, WEST);
      }
   );
   private static final Direction[] NORTH_SOUTH = new Direction[]{Direction.NORTH, Direction.SOUTH};
   private static final Direction[] EAST_WEST = new Direction[]{Direction.EAST, Direction.WEST};

   public static int getHydration(Level level, BlockPos pos) {
      return (int)(Climate.getRainfall(level, pos) / 5.0F);
   }

   public static SaguaroCactusBlock create(
      final RegistryPlant plant,
      ExtendedProperties properties,
      Supplier<? extends Item> productItem,
      Lifecycle[] lifecycle,
      Supplier<ClimateRange> climateRange
   ) {
      return new SaguaroCactusBlock(properties, productItem, lifecycle, climateRange) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   public SaguaroCactusBlock(ExtendedProperties properties, Supplier<? extends Item> productItem, Lifecycle[] lifecycle, Supplier<ClimateRange> climateRange) {
      super(properties);
      Preconditions.checkArgument(lifecycle.length == 12, "Lifecycle length must be 12");
      this.climateRange = climateRange;
      this.lifecycle = lifecycle;
      this.productItem = productItem;
      // lastUpdateTick 保持 0（字段默认值）：注册阶段不能读 TFC 的 Calendars ——
      // 它的静态初始化要读配置，dev 环境会在构造方块时直接抛 IllegalStateException。
      // 生产环境里注册阶段 getTicks() 本来就是 0，因此行为不变。
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any())
                              .setValue(HORIZONTAL, false))
                           .setValue(HORIZONTAL_DIRECTION, Direction.NORTH))
                        .setValue(NORTH, false))
                     .setValue(SOUTH, false))
                  .setValue(EAST, false))
               .setValue(WEST, false))
            .setValue(LIFECYCLE, Lifecycle.HEALTHY)
      );
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext ctx) {
      BlockState st = this.defaultBlockState();
      if (ctx.getClickedFace().get2DDataValue() >= 0) {
         st = (BlockState)((BlockState)((BlockState)st.setValue(HORIZONTAL, true)).setValue(HORIZONTAL_DIRECTION, ctx.getClickedFace().getOpposite()))
            .setValue((Property)FACING_PROPERTIES.get(ctx.getClickedFace().getOpposite()), true);
      }

      return st;
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos posFrom) {
      if (level instanceof ServerLevel server && this.getLifecycleForCurrentMonth() != this.getLifecycleForMonth(Calendars.SERVER.getCalendarMonthOfYear())) {
         this.onUpdate(server, pos, state);
      }

      if (!this.canSurvive(state, level, pos)) {
         level.scheduleTick(pos, this, 1);
         return super.updateShape(state, direction, newState, level, pos, posFrom);
      } else if (direction.getAxis().isVertical()) {
         return super.updateShape(state, direction, newState, level, pos, posFrom);
      } else if (newState.getBlock() == this) {
         return (Boolean) newState.getValue(HORIZONTAL) && (Boolean) newState.getValue((Property)FACING_PROPERTIES.get(direction.getOpposite()))
            ? (BlockState)state.setValue((Property)FACING_PROPERTIES.get(direction), true)
            : super.updateShape(state, direction, newState, level, pos, posFrom);
      } else {
         return (BlockState)state.setValue((Property)FACING_PROPERTIES.get(direction), false);
      }
   }

   public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random) {
      if (!blockState.canSurvive(serverLevel, blockPos)) {
         serverLevel.destroyBlock(blockPos, true);
      }
   }

   public boolean canSurvive(BlockState state, LevelReader levelReader, BlockPos blockPos) {
      if ((Boolean)state.getValue(HORIZONTAL)) {
         Direction offset = (Direction)state.getValue(HORIZONTAL_DIRECTION);
         return levelReader.getBlockState(blockPos.relative(offset)).is(this);
      } else {
         BlockState checkState = levelReader.getBlockState(blockPos.below());
         return (
               checkState.is(this)
                  || Helpers.isBlock(checkState, BlockTags.SAND)
                  || Helpers.isBlock(checkState, Blocks.GRASS_PLANTABLE_ON)
                  || Helpers.isBlock(checkState, Blocks.BUSH_PLANTABLE_ON)
            )
            && !levelReader.getBlockState(blockPos.above()).liquid();
      }
   }

   public boolean isGrowBlock(BlockState state) {
      return Helpers.isBlock(state, BlockTags.SAND)
         || Helpers.isBlock(state, Blocks.GRASS_PLANTABLE_ON)
         || Helpers.isBlock(state, Blocks.BUSH_PLANTABLE_ON);
   }

   public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
      return this.getShape(blockState);
   }

   public VoxelShape getShape(BlockState state) {
      return this.shapes.get(state);
   }

   private VoxelShape getShapeForState(BlockState state) {
      double size = 4.0;
      VoxelShape base;
      if ((Boolean)state.getValue(HORIZONTAL)) {
         base = Block.box(size, size * 2.0, size, 16.0 - size, 15.98, 16.0 - size);
      } else {
         base = Block.box(size, 0.0, size, 16.0 - size, 15.98, 16.0 - size);
      }

      List<VoxelShape> connections = Lists.newArrayList();

      for (Direction dir : Direction.values()) {
         if (dir != Direction.DOWN && dir != Direction.UP && (Boolean)state.getValue((Property)FACING_PROPERTIES.get(dir))) {
            double x = dir == Direction.WEST ? 0.0 : (dir == Direction.EAST ? 16.0 : size);
            double z = dir == Direction.NORTH ? 0.0 : (dir == Direction.SOUTH ? 16.0 : size);
            double upper = 16.0 - size;
            double minX = Math.min(x, upper) / 16.0;
            double maxX = Math.max(x, upper) / 16.0;
            double minZ = Math.min(z, upper) / 16.0;
            double maxZ = Math.max(z, upper) / 16.0;
            VoxelShape sh = Shapes.box(minX, 0.5, minZ, maxX, 0.99875, maxZ);
            connections.add(sh);
         }
      }

      return Shapes.or(base, connections.toArray(new VoxelShape[0]));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{EAST, WEST, NORTH, SOUTH, HORIZONTAL, HORIZONTAL_DIRECTION, LIFECYCLE}));
   }

   public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource random) {
      if (this.getLifecycleForCurrentMonth() != this.getLifecycleForMonth(Calendars.SERVER.getCalendarMonthOfYear())) {
         this.onUpdate(serverLevel, blockPos, blockState);
         this.lastUpdateTick = Calendars.SERVER.getTicks();
      }

      if ((Integer)blockState.getValue(AGE) >= 3
         && random.nextInt(10) == 0
         && blockState.equals(this.defaultBlockState())
         && this.isGrowBlock(serverLevel.getBlockState(blockPos.below()))
         && serverLevel.getBlockState(blockPos.above()).isAir()) {
         this.generateCactus(this, serverLevel, random.nextBoolean(), blockPos, random, false);
      }

      super.randomTick(blockState, serverLevel, blockPos, random);
   }

   public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
      entity.hurt(entity.damageSources().cactus(), 1.0F);
   }

   public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
      return false;
   }

   public boolean generateCactus(Block block, WorldGenLevel world, boolean northSouth, BlockPos pos, RandomSource random, boolean isBig) {
      if (!block.defaultBlockState().canSurvive(world, pos)) {
         return false;
      } else {
         boolean hasArms = random.nextInt(10) > 1;
         boolean has2Arms = random.nextInt(5) != 0;
         int centerHeight = TFCFHelpers.randomRange(4, 8);
         MutableBlockPos p = new MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

         for (int yy = 0; yy < centerHeight && (yy <= 0 || world.getBlockState(p).isAir()); yy++) {
            world.setBlock(p, block.defaultBlockState(), 2);
            p.move(Direction.UP);
         }

         if (!hasArms) {
            return true;
         } else {
            int centerEndY = p.getY();
            int armStart = TFCFHelpers.randomRange(1, centerHeight - 2);
            Direction[] directions = northSouth ? NORTH_SOUTH : EAST_WEST;
            if (has2Arms) {
               for (Direction d : directions) {
                  generateArm(block, world, d, p.getX(), pos.getY() + armStart, p.getZ(), centerEndY);
                  armStart = TFCFHelpers.randomRange(1, centerHeight - 2);
               }
            } else {
               generateArm(block, world, directions[random.nextInt(directions.length)], p.getX(), pos.getY() + armStart, p.getZ(), centerEndY);
            }

            if (!isBig && random.nextInt(10) == 0 || isBig && random.nextInt(50) == 0) {
               BlockPos nextPos = new BlockPos(pos.getX(), centerEndY, pos.getZ());
               if (world.getBlockState(nextPos).isAir()) {
                  this.generateCactus(block, world, random.nextBoolean(), nextPos, random, true);
               }
            }

            return true;
         }
      }
   }

   private static void generateArm(Block block, WorldGenLevel world, Direction direction, int centerX, int armY, int centerZ, int centerHeight) {
      MutableBlockPos p = new MutableBlockPos(centerX + direction.getStepX(), armY, centerZ + direction.getStepZ());
      if (world.getBlockState(p).isAir()) {
         BlockPos centerPos = p.relative(direction.getOpposite());
         BlockState centerState = world.getBlockState(centerPos);
         if (centerState.is(block)) {
            world.setBlock(centerPos, (BlockState)centerState.setValue((Property)FACING_PROPERTIES.get(direction), true), 2);
            world.setBlock(
               p,
               (BlockState)((BlockState)((BlockState)block.defaultBlockState().setValue(HORIZONTAL, true)).setValue(HORIZONTAL_DIRECTION, direction.getOpposite()))
                  .setValue((Property)FACING_PROPERTIES.get(direction.getOpposite()), true),
               2
            );
            p.move(Direction.UP);
            int amt = Math.max(1, centerHeight - p.getY() + TFCFHelpers.randomRange(-3, -1));

            for (int i = 0; i < amt; i++) {
               if (!world.getBlockState(p).isAir()) {
                  return;
               }

               world.setBlock(p, block.defaultBlockState(), 2);
               p.move(Direction.UP);
            }
         }
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
      if (!(Boolean)state.getValue(HORIZONTAL) && level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.below()).is(this)) {
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
