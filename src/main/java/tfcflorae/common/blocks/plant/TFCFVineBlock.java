package tfcflorae.common.blocks.plant;
import net.minecraft.world.level.storage.loot.LootParams;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcflorae.Config;
import tfcflorae.common.blocks.ICooldown;
import tfcflorae.util.TFCFHelpers;

public abstract class TFCFVineBlock extends VineBlock implements IForgeBlockExtension, ICooldown {
   public static final float AABB_OFFSET = 1.0F;
   public static final VoxelShape UP_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
   public static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
   public static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   public static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
   public static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
   public final Map<BlockState, VoxelShape> shapesCache;
   public final ExtendedProperties properties;
   public boolean canWalkThroughEffortlessly;
   public boolean isDead;
   public long cooldown;

   public static TFCFVineBlock create(final RegistryPlant plant, ExtendedProperties properties) {
      return new TFCFVineBlock(properties) {
         @Override
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected TFCFVineBlock(ExtendedProperties properties) {
      super(properties.properties());
      this.properties = properties;
      this.cooldown = Long.MIN_VALUE;
      this.isDead = false;
      BlockState stateDefinition = (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any())
                     .setValue(UP, false))
                  .setValue(NORTH, false))
               .setValue(EAST, false))
            .setValue(SOUTH, false))
         .setValue(WEST, false);
      IntegerProperty stageProperty = this.getPlant().getStageProperty();
      if (stageProperty != null) {
         stateDefinition = (BlockState)stateDefinition.setValue(stageProperty, 0);
      }

      this.registerDefaultState(stateDefinition);
      this.shapesCache = ImmutableMap.copyOf(this.stateDefinition.getPossibleStates().stream().collect(Collectors.toMap(Function.identity(), TFCFVineBlock::calculateShapeNew)));
   }

   public static VoxelShape calculateShapeNew(BlockState state) {
      VoxelShape voxelshape = Shapes.empty();
      if ((Boolean)state.getValue(UP)) {
         voxelshape = UP_AABB;
      }

      if ((Boolean)state.getValue(NORTH)) {
         voxelshape = Shapes.or(voxelshape, NORTH_AABB);
      }

      if ((Boolean)state.getValue(SOUTH)) {
         voxelshape = Shapes.or(voxelshape, SOUTH_AABB);
      }

      if ((Boolean)state.getValue(EAST)) {
         voxelshape = Shapes.or(voxelshape, EAST_AABB);
      }

      if ((Boolean)state.getValue(WEST)) {
         voxelshape = Shapes.or(voxelshape, WEST_AABB);
      }

      return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
   }

   public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
      return this.shapesCache.get(state);
   }

   public abstract RegistryPlant getPlant();

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
      boolean flag = blockstate.is(this);
      BlockState blockstate1 = flag ? blockstate : this.defaultBlockState();

      for (Direction direction : context.getNearestLookingDirections()) {
         if (direction != Direction.DOWN) {
            BooleanProperty bool = getPropertyForFace(direction);
            boolean flag1 = flag && (Boolean)blockstate.getValue(bool);
            if (!flag1 && this.canSupportAtFaceNew(context.getLevel(), context.getClickedPos(), direction)) {
               if (this.getPlant().getStageProperty() != null) {
                  return (BlockState)((BlockState)blockstate1.setValue(bool, true))
                     .setValue(this.getPlant().getStageProperty(), this.getPlant().stageFor(Calendars.SERVER.getCalendarMonthOfYear()));
               }

               return (BlockState)blockstate1.setValue(bool, true);
            }
         }
      }

      return flag ? blockstate1 : null;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      if (this.getPlant().getStageProperty() != null) {
         builder.add(new Property[]{this.getPlant().getStageProperty()});
      }
   }

   public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
      if (entity != null) {
         if (!TFCFHelpers.canWalkThroughEffortlessly(entity) && !this.isDead) {
            this.canWalkThroughEffortlessly = false;
         } else {
            this.canWalkThroughEffortlessly = true;
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

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
      double tempThreshold = (Double)Config.COMMON.foliageDecayThreshold.get();
      if (!this.isDead) {
         if ((double)Climate.getTemperature(level, pos) < tempThreshold && (double)TFCFHelpers.getAverageDailyTemperature(level, pos) < tempThreshold) {
            this.isDead = true;
            level.blockUpdated(pos, state.getBlock());
            return;
         }

         super.randomTick(state, level, pos, random);
      } else if (this.isDead
         && this.getCooldown(level, this.cooldown)
         && (double)Climate.getTemperature(level, pos) >= tempThreshold
         && (double)TFCFHelpers.getAverageDailyTemperature(level, pos) >= tempThreshold) {
         this.setCooldown(level, 72000L, this.cooldown);
         this.isDead = false;
         level.blockUpdated(pos, state.getBlock());
         return;
      }
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (Direction direction : UPDATE_SHAPE_ORDER) {
         BlockState attachedState = level.getBlockState(mutablePos.setWithOffset(pos, direction.getOpposite()));
         if (Helpers.isBlock(attachedState, Blocks.CREEPING_PLANTABLE_ON)
            || attachedState.getBlock() instanceof LeavesBlock
            || this.hasFacesNew(this.getUpdatedStateNew(state, level, pos))) {
            return true;
         }
      }

      return false;
   }

   public boolean hasFacesNew(BlockState state) {
      return this.countFacesNew(state) > 0;
   }

   public int countFacesNew(BlockState state) {
      int i = 0;

      for (BooleanProperty booleanproperty : net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.values()) {
         if ((Boolean)state.getValue(booleanproperty)) {
            i++;
         }
      }

      return i;
   }

   public boolean canSupportAtFaceNew(BlockGetter getter, BlockPos pos, Direction direction) {
      if (direction == Direction.DOWN) {
         return false;
      } else {
         BlockPos blockpos = pos.relative(direction);
         if (isAcceptableNeighbour(getter, blockpos, direction)) {
            return true;
         } else if (direction.getAxis() == Axis.Y) {
            return false;
         } else {
            BooleanProperty booleanproperty = (BooleanProperty)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(direction);
            BlockState blockstate = getter.getBlockState(pos.above());
            return blockstate.is(this) && (Boolean)blockstate.getValue(booleanproperty);
         }
      }
   }

   public static boolean isAcceptableNeighbour(BlockGetter getter, BlockPos pos, Direction direction) {
      BlockState blockstate = getter.getBlockState(pos);
      BlockState attachedState = getter.getBlockState(pos.relative(direction.getOpposite()));
      return Block.isFaceFull(blockstate.getCollisionShape(getter, pos), direction.getOpposite())
         || attachedState.getBlock() instanceof LeavesBlock
         || Helpers.isBlock(attachedState, Blocks.CREEPING_PLANTABLE_ON);
   }

   public BlockState getUpdatedStateNew(BlockState state, BlockGetter getter, BlockPos pos) {
      BlockPos blockpos = pos.above();
      if ((Boolean)state.getValue(UP)) {
         state = (BlockState)state.setValue(UP, isAcceptableNeighbour(getter, blockpos, Direction.DOWN));
      }

      BlockState blockstate = null;

      for (Direction direction : Plane.HORIZONTAL) {
         BooleanProperty booleanproperty = getPropertyForFace(direction);
         if ((Boolean)state.getValue(booleanproperty)) {
            boolean flag = this.canSupportAtFaceNew(getter, pos, direction);
            if (!flag) {
               if (blockstate == null) {
                  blockstate = getter.getBlockState(blockpos);
               }

               flag = blockstate.is(this) && (Boolean)blockstate.getValue(booleanproperty);
            }

            state = (BlockState)state.setValue(booleanproperty, flag);
         }
      }

      return state;
   }

   public RenderShape getRenderShape(BlockState state) {
      return this.isDead ? RenderShape.INVISIBLE : super.getRenderShape(state);
   }

   protected boolean isAir(BlockState state) {
      return this.isDead ? true : super.isAir(state);
   }

   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      return this.isDead ? Collections.emptyList() : super.getDrops(state, builder);
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      return this.isDead ? true : super.canBeReplaced(state, context);
   }

   public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
      return this.isDead ? 1.0F : super.getShadeBrightness(state, level, pos);
   }
}
