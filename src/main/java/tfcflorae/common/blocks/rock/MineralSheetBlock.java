package tfcflorae.common.blocks.rock;

import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFBlockEntities;

public class MineralSheetBlock extends ExtendedBlock implements EntityBlockExtension, DirectionPropertyBlock, IFluidLoggable {
   public static final FluidProperty ALL_WATER_AND_LAVA = FluidProperty.create(
      "fluid",
      Stream.of(
         Fluids.EMPTY,
         Fluids.FLOWING_WATER,
         Fluids.WATER,
         TFCFluids.SALT_WATER,
         TFCFluids.SPRING_WATER,
         TFCFluids.RIVER_WATER,
         Fluids.LAVA,
         Fluids.FLOWING_LAVA
      )
   );
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final BooleanProperty MIRROR = BooleanProperty.create("mirror");
   public static final Map<BooleanProperty, VoxelShape> SHAPES = new com.google.common.collect.ImmutableMap.Builder()
      .put(NORTH, box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0))
      .put(SOUTH, box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0))
      .put(EAST, box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0))
      .put(WEST, box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0))
      .put(UP, box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0))
      .put(DOWN, box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0))
      .build();
   public static final FluidProperty FLUID = ALL_WATER_AND_LAVA;
   private final Map<BlockState, VoxelShape> shapeCache;

   public MineralSheetBlock(ExtendedProperties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)DirectionPropertyBlock.setAllDirections((BlockState)this.getStateDefinition().any(), false).setValue(FACING, Direction.NORTH))
            .setValue(MIRROR, false)
      );
      this.shapeCache = DirectionPropertyBlock.makeShapeCache(this.getStateDefinition(), SHAPES::get);
   }

   public static void removeSheet(Level level, BlockPos pos, BlockState state, Direction face, @Nullable Player player, boolean doDrops) {
      BlockState newState = (BlockState)state.setValue((Property)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(face), false);
      level.playSound(null, pos, SoundEvents.TUFF_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + 0.2F * level.getRandom().nextFloat());
      if (doDrops && (player == null || !player.isCreative())) {
         level.getBlockEntity(pos, TFCFBlockEntities.MINERAL_SHEET.get()).ifPresent(pile -> {
            ItemStack stack = pile.removeSheet(face);
            popResourceFromFace(level, pos, face, stack);
         });
      }

      if (isEmpty(newState)) {
         level.destroyBlock(pos, false);
      } else {
         level.setBlock(pos, newState, 2);
         level.levelEvent(2001, pos, Block.getId(state));
      }
   }

   public static void addSheet(LevelAccessor level, BlockPos pos, BlockState state, Direction face, ItemStack stack) {
      BlockState newState = (BlockState)state.setValue((Property)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(face), true);
      level.setBlock(pos, newState, 2);
      level.getBlockEntity(pos, TFCFBlockEntities.MINERAL_SHEET.get()).ifPresent(pile -> pile.addSheet(face, stack));
      SoundType placementSound = state.getSoundType(level, pos, null);
      level.playSound(
         null,
         pos,
         state.getSoundType(level, pos, null).getPlaceSound(),
         SoundSource.BLOCKS,
         (placementSound.getVolume() + 1.0F) / 2.0F,
         placementSound.getPitch() * 0.8F
      );
   }

   @Nullable
   public static Direction getTargetedFace(Level level, BlockState state, Player player) {
      BlockHitResult result = Helpers.rayTracePlayer(level, player, Fluid.NONE);
      if (result.getType() == Type.BLOCK) {
         Vec3 hit = result.getLocation();
         Direction firstDirection = null;

         for (Entry<BooleanProperty, VoxelShape> entry : SHAPES.entrySet()) {
            BooleanProperty property = entry.getKey();
            if ((Boolean)state.getValue(property)) {
               if (firstDirection == null) {
                  firstDirection = DirectionPropertyBlock.getDirection(property);
               }

               if (entry.getValue().bounds().move(result.getBlockPos()).inflate(0.01).contains(hit)) {
                  return DirectionPropertyBlock.getDirection(property);
               }
            }
         }

         return firstDirection;
      } else {
         return null;
      }
   }

   public static VoxelShape getShapeForSingleFace(Direction direction) {
      return SHAPES.get(DirectionPropertyBlock.getProperty(direction));
   }

   public static int countSheets(BlockState state, Predicate<Direction> onlyTheseDirections) {
      int count = 0;

      for (Direction direction : Helpers.DIRECTIONS) {
         if (onlyTheseDirections.test(direction) && (Boolean)state.getValue(DirectionPropertyBlock.getProperty(direction))) {
            count++;
         }
      }

      return count;
   }

   public static boolean isEmpty(BlockState state) {
      for (BooleanProperty property : PROPERTIES) {
         if ((Boolean)state.getValue(property)) {
            return false;
         }
      }

      return true;
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
      if (!neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite())) {
         level.scheduleTick(currentPos, this, 0);
      }

      return state;
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      for (Direction direction : Helpers.DIRECTIONS) {
         if ((Boolean)state.getValue((Property)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (!adjacentState.isFaceSturdy(level, adjacentPos, direction.getOpposite())) {
               removeSheet(level, pos, state, direction, null, true);
            }
         }
      }
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      for (Direction direction : Helpers.DIRECTIONS) {
         if ((Boolean)state.getValue((Property)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (!adjacentState.isFaceSturdy(level, adjacentPos, direction.getOpposite())) {
               return false;
            }
         }
      }

      return true;
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
      if (level instanceof Level realLevel) {
         Direction targetFace = getTargetedFace(realLevel, state, player);
         if (targetFace != null) {
            return level.getBlockEntity(pos, TFCFBlockEntities.MINERAL_SHEET.get())
               .map(pile -> pile.getSheet(targetFace))
               .orElse(ItemStack.EMPTY);
         }
      }

      return ItemStack.EMPTY;
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      boolean canActuallyHarvest = state.canHarvestBlock(level, pos, player);
      Direction targetFace = getTargetedFace(level, state, player);
      this.playerWillDestroy(level, pos, state, player);
      if (targetFace == null) {
         level.destroyBlock(pos, false);
      } else {
         removeSheet(level, pos, state, targetFace, player, canActuallyHarvest);
      }

      return true;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(
         builder.add(PROPERTIES).add(new Property[]{FACING}).add(new Property[]{MIRROR}).add(new Property[]{this.getFluidProperty()})
      );
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.shapeCache.get(state);
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      return (BlockState)DirectionPropertyBlock.rotate(state, rot).setValue(FACING, rot.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return mirror == Mirror.NONE
         ? state
         : (BlockState)((BlockState)DirectionPropertyBlock.mirror(state, mirror).setValue(FACING, mirror.mirror((Direction)state.getValue(FACING))))
            .cycle(MIRROR);
   }

   public static boolean canPlace(Level level, BlockPos pos, BlockState stateToPlace) {
      return stateToPlace.canSurvive(level, pos);
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }
}
