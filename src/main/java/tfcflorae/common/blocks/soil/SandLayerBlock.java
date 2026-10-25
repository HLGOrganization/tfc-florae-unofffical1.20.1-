package tfcflorae.common.blocks.soil;

import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.soil.TFCSandBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.common.fluids.FluidProperty.FluidKey;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcflorae.common.blockentities.TFCFBlockEntities;

public class SandLayerBlock extends TFCSandBlock implements IFluidLoggable, IForgeBlockExtension, EntityBlockExtension {
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
   public static final FluidProperty FLUID = ALL_WATER_AND_LAVA;
   public static final Boolean SNOWY = false;
   public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
   public static final VoxelShape[] SHAPES = new VoxelShape[]{
      Shapes.empty(),
      Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
      Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
   };
   public final Supplier<? extends Block> transformsInto;
   public final ExtendedProperties properties;

   public SandLayerBlock(int dustColorIn, ExtendedProperties properties, Supplier<? extends Block> transformsInto) {
      super(dustColorIn, properties.properties());
      this.properties = properties;
      this.transformsInto = transformsInto;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(LAYERS, 1))
            .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY))
      );
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   public BlockState transformsInto() {
      return this.transformsInto.get().defaultBlockState();
   }

   public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
      return fluid instanceof FlowingFluid && !this.getFluidProperty().canContain(fluid);
   }

   public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidStateIn) {
      return fluidStateIn.getType() instanceof FlowingFluid && !this.getFluidProperty().canContain(fluidStateIn.getType())
         ? true
         : false;
   }

   public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
      switch (type) {
         case LAND:
            return (Integer)state.getValue(LAYERS) < 5;
         case WATER:
            return false;
         case AIR:
            return false;
         default:
            return false;
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(LAYERS)];
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(LAYERS)];
   }

   public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
      return SHAPES[state.getValue(LAYERS)];
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(LAYERS)];
   }

   public boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      BlockState blockstate = worldIn.getBlockState(pos.below());
      if (blockstate.is(Blocks.ICE) || blockstate.is(Blocks.PACKED_ICE) || blockstate.is(Blocks.BARRIER)) {
         return false;
      } else {
         return !blockstate.is(Blocks.HONEY_BLOCK) && !blockstate.is(Blocks.SOUL_SAND)
            ? Block.isFaceFull(blockstate.getCollisionShape(worldIn, pos.below()), Direction.UP)
               || blockstate.getBlock() instanceof SandLayerBlock && (Integer)blockstate.getValue(LAYERS) == 8
            : true;
      }
   }

   public void addSandLayer(Level level, BlockState state, BlockPos pos, int extraLayer) {
      if ((Integer)state.getValue(LAYERS) + extraLayer >= 8) {
         if ((Integer)state.getValue(LAYERS) + extraLayer > 8) {
            level.setBlock(pos.above(), (BlockState)this.defaultBlockState().setValue(LAYERS, extraLayer), 3);
         } else {
            level.setBlock(pos, this.transformsInto.get().defaultBlockState(), 3);
         }
      } else {
         level.setBlock(pos, (BlockState)state.setValue(LAYERS, (Integer)state.getValue(LAYERS) + extraLayer), 3);
      }
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      if ((Integer)state.getValue(LAYERS) > 7 && this.transformsInto != null) {
         level.setBlock(currentPos, this.transformsInto.get().defaultBlockState(), 3);
      }

      return state.canSurvive(level, currentPos) ? super.updateShape(state, facing, facingState, level, currentPos, facingPos) : state.getFluidState().createLegacyBlock();
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
      int i = (Integer)state.getValue(LAYERS);
      if (!useContext.getItemInHand().is(this.asItem()) || i >= 8) {
         return i == 1;
      } else {
         return useContext.replacingClickedOnBlock() ? useContext.getClickedFace() == Direction.UP : true;
      }
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (Helpers.isItem(stack, this.asItem()) && this.transformsInto != null) {
         int sandLayers = (Integer)state.getValue(LAYERS);
         if (sandLayers >= 8 && level.getBlockState(pos.above()).isAir()) {
            level.setBlockAndUpdate(pos.above(), this.defaultBlockState());
         } else if (sandLayers == 7) {
            level.setBlockAndUpdate(pos, this.transformsInto.get().defaultBlockState());
         } else {
            level.setBlockAndUpdate(pos, (BlockState)state.setValue(LAYERS, Mth.clamp(sandLayers + 1, 1, 8)));
         }

         if (!player.isCreative()) {
            stack.shrink(1);
         }

         Helpers.playSound(level, pos, SoundType.SAND.getPlaceSound());
         level.scheduleTick(pos, this, 2);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (pos.getY() <= 63) {
         FluidHelpers.tickFluid(level, pos, state);
      }

      super.neighborChanged(state, level, fromPos, blockIn, fromPos, isMoving);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = level.getBlockState(pos);
      if (!state.is(this) && (!(state.getBlock() instanceof SandLayerBlock) || (Integer)state.getValue(LAYERS) < 1)) {
         BlockState defaultState = this.defaultBlockState();
         FluidState fluidState = level.getFluidState(pos);
         FluidKey fluidKey = this.getFluidProperty().canContain(fluidState.getType())
            ? this.getFluidProperty().keyFor(fluidState.getType())
            : this.getFluidProperty().keyFor(Fluids.EMPTY);
         return (BlockState)defaultState.setValue(this.getFluidProperty(), fluidKey);
      } else {
         int i = (Integer)state.getValue(LAYERS);
         return (BlockState)state.setValue(LAYERS, Mth.clamp(i + 1, 1, 8));
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{this.getFluidProperty(), LAYERS});
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
      this.playerWillDestroy(level, pos, state, player);
      this.removePileOrSand(level, pos, state);
      return true;
   }

   public boolean canPlaceSandPile(LevelAccessor level, BlockPos pos, BlockState state) {
      return Helpers.isBlock(state.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.CAN_BE_SNOW_PILED) && this.defaultBlockState().canSurvive(level, pos);
   }

   public static boolean canPlaceSandPileStatic(LevelAccessor level, BlockPos pos, BlockState state, BlockState sandLayer) {
      return Helpers.isBlock(state.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.CAN_BE_SNOW_PILED) && sandLayer.canSurvive(level, pos);
   }

   public void placeSandPile(LevelAccessor level, BlockPos pos, BlockState state, boolean byPlayer) {
      BlockPos posAbove = pos.above();
      BlockState aboveState = level.getBlockState(posAbove);
      BlockState savedAboveState = Helpers.isBlock(aboveState.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.CAN_BE_SNOW_PILED) ? aboveState : null;
      BlockState sandPile = this.defaultBlockState();
      FluidState fluidState = level.getFluidState(pos);
      level.setBlock(
         pos,
         (BlockState)sandPile.setValue(
            this.getFluidProperty(),
            this.getFluidProperty().canContain(fluidState.getType())
               ? this.getFluidProperty().keyFor(fluidState.getType())
               : this.getFluidProperty().keyFor(Fluids.EMPTY)
         ),
         3
      );
      if (state.hasBlockEntity()) {
         level.getBlockEntity(pos, TFCFBlockEntities.SAND_PILE.get()).ifPresent(entity -> entity.setHiddenStates(state, savedAboveState, byPlayer));
      }

      if (savedAboveState != null) {
         Helpers.removeBlock(level, posAbove, 3);
      }

      level.blockUpdated(pos, this.defaultBlockState().getBlock());
      if (savedAboveState != null) {
         level.blockUpdated(posAbove, Blocks.AIR);
      }
   }

   public void placeSandPile(LevelAccessor level, BlockPos pos, BlockState state, int layers, boolean byPlayer) {
      BlockPos posAbove = pos.above();
      BlockState aboveState = level.getBlockState(posAbove);
      BlockState savedAboveState = Helpers.isBlock(aboveState.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.CAN_BE_SNOW_PILED) ? aboveState : null;
      BlockState sandPile = this.defaultBlockState();
      FluidState fluidState = level.getFluidState(pos);
      level.setBlock(
         pos,
         (BlockState)((BlockState)sandPile.setValue(LAYERS, layers))
            .setValue(
               this.getFluidProperty(),
               this.getFluidProperty().canContain(fluidState.getType())
                  ? this.getFluidProperty().keyFor(fluidState.getType())
                  : this.getFluidProperty().keyFor(Fluids.EMPTY)
            ),
         3
      );
      if (state.hasBlockEntity()) {
         level.getBlockEntity(pos, TFCFBlockEntities.SAND_PILE.get()).ifPresent(entity -> entity.setHiddenStates(state, savedAboveState, byPlayer));
      }

      if (savedAboveState != null) {
         Helpers.removeBlock(level, posAbove, 3);
      }

      level.blockUpdated(pos, this.defaultBlockState().getBlock());
      if (savedAboveState != null) {
         level.blockUpdated(posAbove, Blocks.AIR);
      }
   }

   public void placeSandPile(WorldGenLevel level, BlockPos pos, BlockState state, int layers, boolean byPlayer) {
      BlockPos posAbove = pos.above();
      BlockState aboveState = level.getBlockState(posAbove);
      BlockState savedAboveState = Helpers.isBlock(aboveState.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.CAN_BE_SNOW_PILED) ? aboveState : null;
      BlockState sandPile = this.defaultBlockState();
      FluidState fluidState = level.getFluidState(pos);
      level.setBlock(
         pos,
         (BlockState)((BlockState)sandPile.setValue(LAYERS, layers))
            .setValue(
               this.getFluidProperty(),
               this.getFluidProperty().canContain(fluidState.getType())
                  ? this.getFluidProperty().keyFor(fluidState.getType())
                  : this.getFluidProperty().keyFor(Fluids.EMPTY)
            ),
         3
      );
      if (state.hasBlockEntity()) {
         level.getBlockEntity(pos, TFCFBlockEntities.SAND_PILE.get()).ifPresent(entity -> entity.setHiddenStates(state, savedAboveState, byPlayer));
      }

      if (savedAboveState != null) {
         Helpers.removeBlock(level, posAbove, 3);
      }

      level.blockUpdated(pos, this.defaultBlockState().getBlock());
      if (savedAboveState != null) {
         level.blockUpdated(posAbove, Blocks.AIR);
      }
   }

   public static void placeSandPileStatic(
      WorldGenLevel level, BlockState sandPile, BlockPos pos, BlockState state, int layers, boolean byPlayer, boolean shouldFluidLog
   ) {
      BlockPos posAbove = pos.above();
      BlockState aboveState = level.getBlockState(posAbove);
      BlockState savedAboveState = Helpers.isBlock(aboveState.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.CAN_BE_SNOW_PILED) ? aboveState : null;
      if (shouldFluidLog) {
         level.setBlock(pos, (BlockState)FluidHelpers.fillWithFluid(sandPile, level.getFluidState(pos).getType()).setValue(LAYERS, layers), 3);
      } else if (!shouldFluidLog) {
         level.setBlock(pos, (BlockState)sandPile.setValue(LAYERS, layers), 3);
      }

      if (sandPile.hasBlockEntity()) {
         level.getBlockEntity(pos, TFCFBlockEntities.SAND_PILE.get()).ifPresent(entity -> entity.setHiddenStates(state, savedAboveState, byPlayer));
      }

      if (savedAboveState != null) {
         Helpers.removeBlock(level, posAbove, 3);
      }

      level.blockUpdated(pos, sandPile.getBlock());
      if (savedAboveState != null) {
         level.blockUpdated(posAbove, Blocks.AIR);
      }
   }

   public void removePileOrSand(LevelAccessor level, BlockPos pos, BlockState state) {
      this.removePileOrSand(level, pos, state, -1);
   }

   public void removePileOrSand(LevelAccessor level, BlockPos pos, BlockState state, boolean removeAllLayers) {
      this.removePileOrSand(level, pos, state, removeAllLayers ? 0 : -1);
   }

   public void removePileOrSand(LevelAccessor level, BlockPos pos, BlockState state, int expectedLayers) {
      int layers = (Integer)state.getValue(LAYERS);
      if (expectedLayers < layers) {
         if (layers > 1 && expectedLayers != 0) {
            level.setBlock(pos, (BlockState)state.setValue(LAYERS, expectedLayers == -1 ? layers - 1 : expectedLayers), 3);
         } else if (state.is(this)) {
            level.removeBlock(pos, false);
         } else if (state.hasBlockEntity()) {
            level.getBlockEntity(pos, TFCBlockEntities.PILE.get()).ifPresent(pile -> {
               if (!level.isClientSide()) {
                  BlockPos above = pos.above();
                  level.setBlock(pos, pile.getInternalState(), 3);
                  if (pile.getAboveState() != null && level.getBlockState(above).isAir()) {
                     level.setBlock(above, pile.getAboveState(), 3);
                  }

                  pile.getInternalState().updateNeighbourShapes(level, pos, 2);
                  level.getBlockState(above).updateNeighbourShapes(level, above, 2);
                  level.blockUpdated(pos, pile.getInternalState().getBlock());
                  if (pile.getAboveState() != null) {
                     level.blockUpdated(above, pile.getAboveState().getBlock());
                  }
               }
            });
         }
      }
   }
}
