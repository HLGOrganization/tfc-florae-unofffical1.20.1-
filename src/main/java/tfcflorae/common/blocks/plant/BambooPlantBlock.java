package tfcflorae.common.blocks.plant;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.TFCBushBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolActions;
import tfcflorae.util.TFCFHelpers;

public abstract class BambooPlantBlock extends TFCBushBlock implements IFluidLoggable {
   protected static final float SMALL_LEAVES_AABB_OFFSET = 3.0F;
   protected static final float LARGE_LEAVES_AABB_OFFSET = 5.0F;
   protected static final float COLLISION_AABB_OFFSET = 1.5F;
   protected static final VoxelShape SMALL_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
   protected static final VoxelShape LARGE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
   protected static final VoxelShape COLLISION_SHAPE = Block.box(7.5, 0.0, 7.5, 10.5, 16.0, 10.5);
   public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
   public static final EnumProperty<BambooLeaves> LEAVES = BlockStateProperties.BAMBOO_LEAVES;
   public static final IntegerProperty GROWTH_STAGE = BlockStateProperties.STAGE;
   public static final int MAX_HEIGHT = 16;
   public static final int GROWTH_STAGE_GROWING = 0;
   public static final int GROWTH_STAGE_DONE_GROWING = 1;
   public static final int AGE_THIN_BAMBOO = 0;
   public static final int AGE_THICK_BAMBOO = 1;
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   private final Supplier<? extends Block> bambooStem;
   private final Supplier<? extends Block> bambooSapling;
   public boolean canWalkThroughEffortlessly;

   public static BambooPlantBlock create(
      final RegistryPlant plant, ExtendedProperties properties, Supplier<? extends Block> bambooStem, Supplier<? extends Block> bambooSapling
   ) {
      return new BambooPlantBlock(properties, bambooStem, bambooSapling) {
         @Override
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected BambooPlantBlock(ExtendedProperties properties, Supplier<? extends Block> bambooStem, Supplier<? extends Block> bambooSapling) {
      super(properties);
      this.bambooStem = bambooStem;
      this.bambooSapling = bambooSapling;
      BlockState stateDefinition = (BlockState)this.getStateDefinition().any();
      IntegerProperty stageProperty = this.getPlant().getStageProperty();
      if (stageProperty != null) {
         stateDefinition = (BlockState)stateDefinition.setValue(stageProperty, 0);
      }

      this.registerDefaultState((BlockState)((BlockState)((BlockState)stateDefinition.setValue(AGE, 0)).setValue(LEAVES, BambooLeaves.NONE)).setValue(GROWTH_STAGE, 0));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      if (this.getPlant().getStageProperty() != null) {
         builder.add(new Property[]{this.getPlant().getStageProperty()});
      }

      builder.add(new Property[]{AGE, LEAVES, GROWTH_STAGE, this.getFluidProperty()});
   }

   public OffsetType getOffsetType() {
      return OffsetType.XZ;
   }

   public boolean propagatesSkylightDown(BlockState state, BlockGetter pReader, BlockPos pos) {
      return true;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      VoxelShape voxelshape = state.getValue(LEAVES) == BambooLeaves.LARGE ? LARGE_SHAPE : SMALL_SHAPE;
      Vec3 vec3 = state.getOffset(level, pos);
      return voxelshape.move(vec3.x, vec3.y, vec3.z);
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return false;
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      Vec3 vec3 = state.getOffset(level, pos);
      return COLLISION_SHAPE.move(vec3.x, vec3.y, vec3.z);
   }

   public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
      return false;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      super.getStateForPlacement(context);
      BlockPos pos = context.getClickedPos();
      FluidState fluidState = context.getLevel().getFluidState(pos);
      BlockState state = this.updateStateWithCurrentMonth(this.defaultBlockState());
      if (this.getFluidProperty().canContain(fluidState.getType())) {
         return (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
      } else {
         BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos().below());
         if (context.getLevel().getBlockState(context.getClickedPos().below()).is(BlockTags.BAMBOO_PLANTABLE_ON)) {
            if (blockstate.is(this.bambooSapling.get())) {
               return (BlockState)state.setValue(AGE, 0);
            } else if (blockstate.is(this.bambooStem.get())) {
               int i = blockstate.getValue(AGE) > 0 ? 1 : 0;
               return (BlockState)state.setValue(AGE, i);
            } else {
               BlockState blockstate1 = context.getLevel().getBlockState(context.getClickedPos().above());
               return blockstate1.is(this.bambooStem.get())
                  ? (BlockState)state.setValue(AGE, (Integer)blockstate1.getValue(AGE))
                  : this.bambooSapling.get().defaultBlockState();
            }
         } else {
            return null;
         }
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   public boolean isRandomlyTicking(BlockState state) {
      return (Integer)state.getValue(GROWTH_STAGE) == 0;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextDouble() < (Double)TFCConfig.SERVER.plantGrowthChance.get()
         && (Integer)state.getValue(GROWTH_STAGE) == 0
         && level.getBlockState(pos.above()).isAir()
         && level.getRawBrightness(pos.above(), 0) >= 9) {
         int i = this.getHeightBelowUpToMax(level, pos) + 1;
         if (i < 16 && ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(3) == 0)) {
            this.growBamboo(state, level, pos, random, i);
            ForgeHooks.onCropsGrowPost(level, pos, state);
         }
      }

      level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return super.canSurvive(state, level, pos)
         || level.getBlockState(pos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON)
         || level.getBlockState(pos.below()).is(this.bambooStem.get());
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      if (!state.canSurvive(level, currentPos)) {
         level.scheduleTick(currentPos, this, 1);
      }

      if (facing == Direction.UP && facingState.is(this.bambooStem.get()) && (Integer)facingState.getValue(AGE) > (Integer)state.getValue(AGE)) {
         level.setBlock(currentPos, (BlockState)state.cycle(AGE), 2);
      }

      return state;
   }

   public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      if (!player.getMainHandItem().canPerformAction(ToolActions.SWORD_DIG)) {
         float baseSpeed = state.getDestroySpeed(level, pos);
         if (baseSpeed == -1.0F) {
            return 0.0F;
         } else {
            int toolModifier = ForgeHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
            return player.getDigSpeed(state, pos) / baseSpeed / (float)toolModifier;
         }
      } else {
         return 1.0F;
      }
   }

   public void growBamboo(BlockState state, Level level, BlockPos pos, RandomSource random, int pMaxTotalSize) {
      BlockState blockstate = level.getBlockState(pos.below());
      BlockPos blockpos = pos.below(2);
      BlockState blockstate1 = level.getBlockState(blockpos);
      BambooLeaves bambooleaves = BambooLeaves.NONE;
      if (pMaxTotalSize >= 1) {
         if (!blockstate.is(this.bambooStem.get()) || blockstate.getValue(LEAVES) == BambooLeaves.NONE) {
            bambooleaves = BambooLeaves.SMALL;
         } else if (blockstate.is(this.bambooStem.get()) && blockstate.getValue(LEAVES) != BambooLeaves.NONE) {
            bambooleaves = BambooLeaves.LARGE;
            if (blockstate1.is(this.bambooStem.get())) {
               level.setBlock(pos.below(), (BlockState)blockstate.setValue(LEAVES, BambooLeaves.SMALL), 3);
               level.setBlock(blockpos, (BlockState)blockstate1.setValue(LEAVES, BambooLeaves.NONE), 3);
            }
         }
      }

      int i = state.getValue(AGE) != 1 && !blockstate1.is(this.bambooStem.get()) ? 0 : 1;
      int j = (pMaxTotalSize < 11 || !(random.nextFloat() < 0.25F)) && pMaxTotalSize != 15 ? 0 : 1;
      level.setBlock(
         pos.above(), (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(AGE, i)).setValue(LEAVES, bambooleaves)).setValue(GROWTH_STAGE, j), 3
      );
   }

   public int getHeightAboveUpToMax(BlockGetter level, BlockPos pos) {
      int i = 0;

      while (i < 16 && level.getBlockState(pos.above(i + 1)).is(this.bambooStem.get())) {
         i++;
      }

      return i;
   }

   public int getHeightBelowUpToMax(BlockGetter level, BlockPos pos) {
      int i = 0;

      while (i < 16 && level.getBlockState(pos.below(i + 1)).is(this.bambooStem.get())) {
         i++;
      }

      return i;
   }

   public BlockState getPlant(BlockGetter world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return state.getBlock() != this ? this.defaultBlockState() : state;
   }

   public abstract RegistryPlant getPlant();

   public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
      if (entity != null) {
         if (TFCFHelpers.canWalkThroughEffortlessly(entity)) {
            this.canWalkThroughEffortlessly = true;
         } else {
            this.canWalkThroughEffortlessly = false;
         }
      }
   }

   public float getSpeedFactor() {
      float modifier = ((Double)TFCConfig.SERVER.plantsMovementModifier.get()).floatValue();
      return this.canWalkThroughEffortlessly ? 1.0F : Helpers.lerp(modifier, this.speedFactor, 1.0F);
   }

   protected BlockState updateStateWithCurrentMonth(BlockState state) {
      return this.getPlant().getStageProperty() != null
         ? (BlockState)state.setValue(this.getPlant().getStageProperty(), this.getPlant().stageFor(Calendars.SERVER.getCalendarMonthOfYear()))
         : state;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }
}
