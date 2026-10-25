package tfcflorae.common.blocks.wood;
import net.minecraft.world.level.material.MapColor;

import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.TFCTags.Entities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.fruit.IBushBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.SeasonalPlantBlock;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.common.fluids.FluidProperty.FluidKey;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import tfcflorae.Config;
import tfcflorae.common.blocks.TFCFBlocks;

public abstract class TFCFJoshuaLeavesBlock extends SeasonalPlantBlock implements IFluidLoggable, ILeavesBlock, IBushBlock, HoeOverlayBlock {
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
   public static final EnumProperty<Lifecycle> LIFECYCLE = TFCBlockStateProperties.LIFECYCLE;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   private static final VoxelShape SHAPE = Block.box(1.0, 1.0, 1.0, 15.0, 15.0, 15.0);
   private final ExtendedProperties properties;
   public static final int MONTHS_SPENT_DORMANT_TO_DIE = 4;
   public final TFCFWood wood;
   private long lastUpdateTick;

   public static void doParticles(ServerLevel level, double x, double y, double z, int count) {
      level.sendParticles(
         (SimpleParticleType)TFCParticles.LEAF.get(),
         x,
         y,
         z,
         count,
         (double)Helpers.triangle(level.random),
         (double)Helpers.triangle(level.random),
         (double)Helpers.triangle(level.random),
         0.3F
      );
   }

   public static int getHydration(Level level, BlockPos pos) {
      return (int)(Climate.getRainfall(level, pos) / 5.0F);
   }

   public static TFCFJoshuaLeavesBlock create(
      TFCFWood wood, ExtendedProperties properties, Supplier<? extends Item> productItem, Lifecycle[] stages, Supplier<ClimateRange> climateRange
   ) {
      return new TFCFJoshuaLeavesBlock(wood, properties, productItem, stages, climateRange) {
         @Override
         public FluidProperty getFluidProperty() {
            return FLUID;
         }
      };
   }

   protected TFCFJoshuaLeavesBlock(
      TFCFWood wood, ExtendedProperties properties, Supplier<? extends Item> productItem, Lifecycle[] stages, Supplier<ClimateRange> climateRange
   ) {
      super(properties, climateRange, productItem, stages);
      this.wood = wood;
      this.properties = properties;
      this.lastUpdateTick = Calendars.SERVER.getTicks();
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0)).setValue(FACING, Direction.UP))
               .setValue(PERSISTENT, false))
            .setValue(LIFECYCLE, Lifecycle.HEALTHY)
      );
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   public void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{this.getFluidProperty(), AGE, FACING, LIFECYCLE, PERSISTENT});
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      if (facing != Direction.UP && !state.canSurvive(level, currentPos)) {
         level.scheduleTick(currentPos, this, 1);
         return Blocks.AIR.defaultBlockState();
      } else {
         return state;
      }
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState blockstate = level.getBlockState(pos.below());
      if (!blockstate.is((Block)TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get())
         && !Helpers.isBlock(blockstate, net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
         && !Helpers.isBlock(blockstate, net.dries007.tfc.common.TFCTags.Blocks.TREE_GROWS_ON)
         && !Helpers.isBlock(blockstate, BlockTags.SAND)
         && !Helpers.isBlock(blockstate, net.minecraftforge.common.Tags.Blocks.GRAVEL)) {
         if (!blockstate.isAir()) {
            return false;
         } else {
            boolean flag = false;

            for (Direction direction : Plane.HORIZONTAL) {
               BlockState blockstate1 = level.getBlockState(pos.relative(direction));
               if (blockstate1.is((Block)TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get())) {
                  if (flag) {
                     return false;
                  }

                  flag = true;
               } else if (!blockstate1.isAir()) {
                  return false;
               }
            }

            return flag;
         }
      } else {
         return true;
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (state.getValue(LIFECYCLE) == Lifecycle.FRUITING && this.productItem != null && (Integer)state.getValue(AGE) >= 4) {
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
      if (!(Boolean)state.getValue(PERSISTENT)) {
         Fluid fluid = level.getFluidState(pos).getType();
         Lifecycle currentLifecycle = (Lifecycle)state.getValue(LIFECYCLE);
         Lifecycle expectedLifecycle = this.getLifecycleForCurrentMonth();
         if (random.nextInt((Integer)Config.COMMON.fruitingLeavesUpdateChance.get()) == 0
            && level.isAreaLoaded(pos, 3)
            && !(Boolean)state.getValue(PERSISTENT)
            && currentLifecycle != expectedLifecycle
            && (level.getRawBrightness(pos, 0) >= 11 || Calendars.SERVER.getCalendarDayTime() == level.getDayTime())) {
            this.onUpdate(level, pos, state);
            this.lastUpdateTick = Calendars.SERVER.getTicks();
         }

         BlockPos abovePos = pos.above();
         if (level.getBlockState(abovePos).isAir() && abovePos.getY() < level.getMaxBuildHeight() && (Double)TFCConfig.SERVER.plantGrowthChance.get() > random.nextDouble()) {
            int i = (Integer)state.getValue(AGE);
            if (i < 5 && ForgeHooks.onCropsGrowPre(level, abovePos, state, true)) {
               boolean shouldPlaceNewBody = false;
               boolean foundGroundFurtherDown = false;
               BlockState belowState = level.getBlockState(pos.below());
               Block belowBlock = belowState.getBlock();
               if (Helpers.isBlock(belowBlock, net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
                  || Helpers.isBlock(belowBlock, net.dries007.tfc.common.TFCTags.Blocks.TREE_GROWS_ON)
                  || Helpers.isBlock(belowBlock, BlockTags.SAND)
                  || Helpers.isBlock(belowBlock, net.minecraftforge.common.Tags.Blocks.GRAVEL)) {
                  shouldPlaceNewBody = true;
               } else if (belowBlock == TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get()) {
                  int j = 1;

                  for (int k = 0; k < 4; k++) {
                     Block belowBlockOffset = level.getBlockState(pos.below(j + 1)).getBlock();
                     if (belowBlockOffset != TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get()) {
                        if (Helpers.isBlock(belowBlockOffset, net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
                           || Helpers.isBlock(belowBlockOffset, net.dries007.tfc.common.TFCTags.Blocks.TREE_GROWS_ON)
                           || Helpers.isBlock(belowBlockOffset, BlockTags.SAND)
                           || Helpers.isBlock(belowBlockOffset, net.minecraftforge.common.Tags.Blocks.GRAVEL)) {
                           foundGroundFurtherDown = true;
                        }
                        break;
                     }

                     j++;
                  }

                  if (j < 2 || j <= random.nextInt(foundGroundFurtherDown ? 5 : 4)) {
                     shouldPlaceNewBody = true;
                  }
               } else if (level.getBlockState(pos.below()).isAir()) {
                  shouldPlaceNewBody = true;
               }

               if (shouldPlaceNewBody && this.allNeighborsEmpty(level, abovePos, null) && level.getBlockState(pos.above(2)).isAir()) {
                  this.setTrunkWithFluid(level, pos);
                  this.placeGrownLeaves(level, abovePos, i, Direction.UP);
               } else if (i >= 4) {
                  this.placeDeadLeaves(level, pos, (Direction)level.getBlockState(pos).getValue(FACING));
               } else {
                  int l = 0;
                  if (i < 2) {
                     l = 2 + random.nextInt(4);
                  } else {
                     l = 2 + random.nextInt(8);
                  }

                  boolean foundValidGrowthSpace = false;
                  if (foundGroundFurtherDown) {
                     l++;
                  }

                  for (int i1 = 0; i1 < l; i1++) {
                     Direction direction = Plane.HORIZONTAL.getRandomDirection(random);
                     BlockPos relativePos = pos.relative(direction);
                     if (level.getBlockState(relativePos).isAir()
                        && level.getBlockState(relativePos.below()).isAir()
                        && this.allNeighborsEmpty(level, relativePos, direction.getOpposite())) {
                        this.placeGrownLeaves(level, relativePos, i + 1, direction);
                        foundValidGrowthSpace = true;
                     }
                  }

                  if (foundValidGrowthSpace) {
                     this.setTrunkWithFluid(level, pos);
                  } else {
                     this.placeDeadLeaves(level, pos, (Direction)level.getBlockState(pos).getValue(FACING));
                  }
               }

               ForgeHooks.onCropsGrowPost(level, pos, state);
            }
         }
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   public boolean generatePlant(LevelAccessor level, BlockPos pos, RandomSource rand, int maxHorizontalDistance, Fluid fluid) {
      if (this.getFluidProperty().canContain(fluid)) {
         BlockState originalState = level.getBlockState(pos);
         this.setTrunkWithFluid(level, pos);
         if (this.growTreeRecursive(level, pos, rand, pos, maxHorizontalDistance, 0, fluid)) {
            return true;
         }

         level.setBlock(pos, originalState, 3);
      }

      return false;
   }

   private boolean growTreeRecursive(
      LevelAccessor level, BlockPos branchPos, RandomSource rand, BlockPos originalBranchPos, int maxHorizontalDistance, int iterations, Fluid fluid
   ) {
      int status;
      do {
         status = this.growStep(level, branchPos, originalBranchPos, rand, maxHorizontalDistance, iterations, fluid);
         if (status == 0) {
            branchPos = branchPos.above();
         }
      } while (status == 0);

      // A blocked branch reports -1, so only statuses that placed a branch or leaves count as success.
      return status >= 1;
   }

   public int growStep(
      LevelAccessor level, BlockPos currentBlock, BlockPos originalBranchPos, RandomSource random, int maxHorizontalDistance, int iterations, Fluid fluid
   ) {
      BlockPos blockpos = currentBlock.above();
      if (level.getBlockState(blockpos).getBlock() == Blocks.AIR && iterations < 5) {
         boolean flag = false;
         boolean flag1 = false;
         BlockState state = level.getBlockState(currentBlock.below());
         Block block = state.getBlock();
         if (Helpers.isBlock(state, net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
            || Helpers.isBlock(state, net.dries007.tfc.common.TFCTags.Blocks.TREE_GROWS_ON)
            || Helpers.isBlock(state, BlockTags.SAND)
            || Helpers.isBlock(state, net.minecraftforge.common.Tags.Blocks.GRAVEL)) {
            flag = true;
         } else if (block != TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get()) {
            if (state.getBlock().defaultBlockState().isAir()) {
               flag = true;
            }
         } else {
            int j = 1;

            for (int k = 0; k < 4; k++) {
               Block block1 = level.getBlockState(currentBlock.below(j + 1)).getBlock();
               if (block1 != TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get()) {
                  BlockState currentBlockStateBelow = level.getBlockState(currentBlock.below(j + 1));
                  if (Helpers.isBlock(currentBlockStateBelow, net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
                     || Helpers.isBlock(currentBlockStateBelow, net.dries007.tfc.common.TFCTags.Blocks.TREE_GROWS_ON)
                     || Helpers.isBlock(currentBlockStateBelow, BlockTags.SAND)
                     || Helpers.isBlock(currentBlockStateBelow, net.minecraftforge.common.Tags.Blocks.GRAVEL)) {
                     flag1 = true;
                  }
                  break;
               }

               j++;
            }

            int growthMultiplier = random.nextInt(2);
            int i1 = 2;
            if (flag1) {
               if (j < 2 || random.nextInt(i1 + growthMultiplier) >= j) {
                  flag = true;
               }
            } else if (random.nextInt(i1) - random.nextInt(i1) >= j) {
               flag = true;
            }
         }

         if (flag && this.allNeighborsEmpty(level, blockpos, null) && level.getBlockState(currentBlock.above(2)).getBlock() == Blocks.AIR) {
            this.setTrunkWithFluid(level, currentBlock);
            this.placeGrownLeaves(level, blockpos, 5, Direction.UP);
            return 0;
         }

         if (iterations < 4) {
            int l = 0;
            if (iterations < 2) {
               l = 2 + random.nextInt(4);
            } else {
               l = 2 + random.nextInt(8);
            }

            boolean flag2 = false;
            if (flag1) {
               l++;
            }

            for (int j1 = 0; j1 < l; j1++) {
               Direction direction = Plane.HORIZONTAL.getRandomDirection(random);
               BlockPos blockpos1 = currentBlock.relative(direction);
               if (level.getBlockState(blockpos1).getBlock() == Blocks.AIR
                  && level.getBlockState(blockpos1.below()).getBlock() == Blocks.AIR
                  && this.allNeighborsEmpty(level, blockpos1, direction.getOpposite())
                  && Math.abs(blockpos1.getX() - originalBranchPos.getX()) < maxHorizontalDistance
                  && Math.abs(blockpos1.getZ() - originalBranchPos.getZ()) < maxHorizontalDistance
                  && level.getBlockState(blockpos1).isAir()) {
                  this.setTrunkWithFluid(level, currentBlock);
                  this.setTrunkWithFluid(level, blockpos1);
                  this.placeGrownLeaves(level, blockpos1.above(), 5, Direction.UP);
                  this.growTreeRecursive(
                     level,
                     blockpos1.above(),
                     random,
                     originalBranchPos,
                     maxHorizontalDistance,
                     Mth.clamp(iterations + random.nextInt(5 - iterations) + 1, 0, 4),
                     fluid
                  );
                  flag2 = true;
               }
            }

            if (flag2) {
               return 1;
            }

            this.placeGrownLeaves(level, currentBlock, 5, Direction.UP);
            return 2;
         }

         if (iterations == 4) {
            this.placeGrownLeaves(level, currentBlock, 5, Direction.UP);
            return 2;
         }
      }

      return -1;
   }

   protected boolean allNeighborsEmpty(LevelReader level, BlockPos pos, @Nullable Direction excludingSide) {
      for (Direction direction : Plane.HORIZONTAL) {
         if (direction != excludingSide && !level.getBlockState(pos.relative(direction)).isAir()) {
            return false;
         }
      }

      return true;
   }

   protected void placeGrownLeaves(LevelAccessor level, BlockPos pos, int age, Direction facing) {
      Fluid fluid = level.getFluidState(pos).getType();
      level.setBlock(
         pos,
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluid))).setValue(AGE, age))
            .setValue(FACING, facing),
         2
      );
   }

   protected void placeDeadLeaves(LevelAccessor level, BlockPos pos, Direction facing) {
      Fluid fluid = level.getFluidState(pos).getType();
      level.setBlock(
         pos,
         (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluid))).setValue(AGE, 5))
            .setValue(FACING, facing),
         2
      );
   }

   protected void setTrunkWithFluid(LevelAccessor level, BlockPos pos) {
      BlockState state = this.getBodyStateWithFluid(level, pos);
      level.setBlock(pos, state, 2);
   }

   protected BlockState getBodyStateWithFluid(LevelAccessor level, BlockPos pos) {
      TFCFJoshuaTrunkBlock state = (TFCFJoshuaTrunkBlock)TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get();
      return state.getStateForPlacement(level, pos);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      Direction direction = context.getClickedFace();
      BlockState state = this.defaultBlockState();
      if (world.getBlockState(pos.relative(direction.getOpposite())).is((Block)TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get())) {
         state = (BlockState)state.setValue(FACING, context.getClickedFace());
      }

      FluidState fluidState = world.getFluidState(context.getClickedPos());
      return !fluidState.isEmpty() && this.getFluidProperty().canContain(fluidState.getType())
         ? (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()))
         : (BlockState)this.defaultBlockState().setValue(PERSISTENT, context.getPlayer() != null);
   }

   public void addHoeOverlayInfo(Level level, BlockPos pos, BlockState state, List<Component> text, boolean isDebug) {
      ClimateRange range = (ClimateRange)this.climateRange.get();
      text.add(FarmlandBlock.getHydrationTooltip(level, pos, range, false, getHydration(level, pos)));
      text.add(FarmlandBlock.getTemperatureTooltip(level, pos, range, false));
   }

   public void onUpdate(Level level, BlockPos pos, BlockState state) {
      if (!(Boolean)state.getValue(PERSISTENT)) {
         Lifecycle currentLifecycle = (Lifecycle)state.getValue(LIFECYCLE);
         Lifecycle expectedLifecycle = this.getLifecycleForCurrentMonth();
         if (!checkAndSetDormant(level, pos, state, currentLifecycle, expectedLifecycle)) {
            long deltaTicks = Math.min(this.getTicksSinceBushUpdate(), Calendars.SERVER.getCalendarTicksInYear());
            long currentCalendarTick = Calendars.SERVER.getCalendarTicks();
            long nextCalendarTick = currentCalendarTick - deltaTicks;
            ClimateRange range = (ClimateRange)this.climateRange.get();
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
               Fluid fluid = level.getFluidState(pos).getType();
               newState = (BlockState)((BlockState)((BlockState)((Block)TFCFBlocks.JOSHUA_LEAVES.get(this.wood).get())
                        .defaultBlockState()
                        .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluid)))
                     .setValue(AGE, 5))
                  .setValue(FACING, Direction.UP);
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

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      float modifier = ((Double)TFCConfig.SERVER.leavesMovementModifier.get()).floatValue();
      if (modifier < 1.0F && ((FluidKey)state.getValue(this.getFluidProperty())).getFluid() == Fluids.EMPTY) {
         entity.makeStuckInBlock(state, new net.minecraft.world.phys.Vec3(0.8F, 0.15F, 0.8F));
      }

      if (Helpers.isEntity(entity, Entities.DESTROYED_BY_LEAVES)) {
         entity.kill();
      }

      if (level.random.nextInt(20) == 0 && level instanceof ServerLevel server) {
         doParticles(server, entity.getX(), entity.getEyeY() - 0.25, entity.getZ(), 3);
      }
   }

   protected boolean mayDie(Level level, BlockPos pos, BlockState state, int monthsSpentDying) {
      return monthsSpentDying >= 4 && !(Boolean)state.getValue(PERSISTENT);
   }

   public boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }
}
