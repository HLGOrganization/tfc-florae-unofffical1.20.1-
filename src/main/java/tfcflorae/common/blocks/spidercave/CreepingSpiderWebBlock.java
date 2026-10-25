package tfcflorae.common.blocks.spidercave;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.TFCBushBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import tfcflorae.util.TFCFHelpers;

public class CreepingSpiderWebBlock extends TFCBushBlock implements DirectionPropertyBlock {
   protected static final VoxelShape UP_SHAPE = box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape DOWN_SHAPE = box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
   protected static final VoxelShape NORTH_SHAPE = box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
   protected static final VoxelShape EAST_SHAPE = box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape SOUTH_SHAPE = box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
   protected static final VoxelShape WEST_SHAPE = box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
   protected static final Map<BooleanProperty, VoxelShape> SHAPES = ImmutableMap.<BooleanProperty, VoxelShape>builder()
      .put(UP, UP_SHAPE)
      .put(DOWN, DOWN_SHAPE)
      .put(NORTH, NORTH_SHAPE)
      .put(SOUTH, SOUTH_SHAPE)
      .put(EAST, EAST_SHAPE)
      .put(WEST, WEST_SHAPE)
      .build();
   protected final Map<BlockState, VoxelShape> shapeCache;
   public boolean canWalkThroughEffortlessly;

   public CreepingSpiderWebBlock(ExtendedProperties properties) {
      super(properties);
      this.registerDefaultState(DirectionPropertyBlock.setAllDirections((BlockState)this.getStateDefinition().any(), false));
      this.shapeCache = DirectionPropertyBlock.makeShapeCache(this.getStateDefinition(), SHAPES::get);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      state = (BlockState)state.setValue((Property)PipeBlock.PROPERTY_BY_DIRECTION.get(direction), Helpers.isBlock(facingState, Blocks.CREEPING_PLANTABLE_ON));
      return this.isEmpty(state) ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState() : state;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (Direction direction : UPDATE_SHAPE_ORDER) {
         if (Helpers.isBlock(level.getBlockState(mutablePos.setWithOffset(pos, direction)), Blocks.CREEPING_PLANTABLE_ON)) {
            return true;
         }
      }

      return false;
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, false);
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.shapeCache.get(state);
   }

   @NotNull
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.updateStateFromSides(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{UP, DOWN, NORTH, SOUTH, EAST, WEST}));
   }

   private BlockState updateStateFromSides(LevelAccessor level, BlockPos pos, BlockState state) {
      MutableBlockPos mutablePos = new MutableBlockPos();
      boolean hasEarth = false;

      for (Direction direction : UPDATE_SHAPE_ORDER) {
         mutablePos.setWithOffset(pos, direction);
         boolean ground = Helpers.isBlock(level.getBlockState(mutablePos), Blocks.CREEPING_PLANTABLE_ON);
         state = (BlockState)state.setValue((Property)PipeBlock.PROPERTY_BY_DIRECTION.get(direction), ground);
         hasEarth |= ground;
      }

      return hasEarth ? state : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
   }

   private boolean isEmpty(BlockState state) {
      for (BooleanProperty property : SHAPES.keySet()) {
         if ((Boolean)state.getValue(property)) {
            return false;
         }
      }

      return true;
   }

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
}
