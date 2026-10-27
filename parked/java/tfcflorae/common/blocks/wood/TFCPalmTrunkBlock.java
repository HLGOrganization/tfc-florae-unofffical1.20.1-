package tfcflorae.common.blocks.wood;
import tfcflorae.common.TFCFBlockStateProperties;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tfcflorae.util.TFCFHelpers;

public class TFCPalmTrunkBlock extends TFCFJoshuaTrunkBlock {
   public static final int MONTHS_SPENT_DORMANT_TO_DIE = 4;
   public static final BooleanProperty NATURAL = TFCFBlockStateProperties.NATURAL;
   public static final BooleanProperty TOP_BLOCK = BooleanProperty.create("top_block");
   public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
   public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   public final RegistryWood wood;
   @Nullable
   public final Supplier<? extends Block> leaves;
   @Nullable
   public final Supplier<? extends Block> trunk;

   public static TFCPalmTrunkBlock create(
      RegistryWood wood, ExtendedProperties properties, @Nullable Supplier<? extends Block> leaves, @Nullable Supplier<? extends Block> trunk
   ) {
      return new TFCPalmTrunkBlock(wood, properties, leaves, trunk) {
      };
   }

   public TFCPalmTrunkBlock(
      RegistryWood wood, ExtendedProperties properties, @Nullable Supplier<? extends Block> leaves, @Nullable Supplier<? extends Block> trunk
   ) {
      super(wood, properties.properties(), properties);
      this.wood = wood;
      this.leaves = leaves;
      this.trunk = trunk;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition
                                       .any())
                                    .setValue(AGE, 0))
                                 .setValue(TFCFBlockStateProperties.NATURAL, true))
                              .setValue(TOP_BLOCK, false))
                           .setValue(NORTH, false))
                        .setValue(EAST, false))
                     .setValue(SOUTH, false))
                  .setValue(WEST, false))
               .setValue(UP, false))
            .setValue(DOWN, false)
      );
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{this.getFluidProperty(), AGE, TFCFBlockStateProperties.NATURAL, TOP_BLOCK, NORTH, EAST, SOUTH, WEST, UP, DOWN});
   }

   public Block getTrunk() {
      return this.trunk.get();
   }

   public Block getLeaves() {
      return this.leaves.get();
   }

   public boolean isTrunkOrLeaves(BlockState state) {
      return this.isTrunk(state) || this.isLeaves(state);
   }

   public boolean isTrunk(BlockState state) {
      return Helpers.isBlock(state, this.getTrunk());
   }

   public boolean isLeaves(BlockState state) {
      return Helpers.isBlock(state, this.getLeaves());
   }

   @Override
   public boolean canConnectTo(BlockState state) {
      return this.isTrunk(state) || this.canConnectToSoil(state);
   }

   public boolean canConnectToSoil(BlockState state) {
      return Helpers.isBlock(state, Blocks.BUSH_PLANTABLE_ON)
         || Helpers.isBlock(state, Blocks.TREE_GROWS_ON)
         || Helpers.isBlock(state, BlockTags.SAND)
         || Helpers.isBlock(state, net.minecraftforge.common.Tags.Blocks.GRAVEL)
         || state.isSolid();
   }

   public boolean isTopBlock(BlockState state, LevelReader level, BlockPos pos) {
      int connectedFaces = 0;

      for (Direction direction : Direction.values()) {
         connectedFaces += level.getBlockState(pos.relative(direction)).getBlock().equals(this.getTrunk()) ? 1 : 0;
      }

      return connectedFaces <= 1 && !level.getBlockState(pos.above()).getBlock().equals(this.getTrunk());
   }

   public int neighbourTopBlocks(BlockState state, LevelReader level, BlockPos pos) {
      int connectedFaces = 0;

      for (Direction direction : Direction.values()) {
         BlockState neighbourState = level.getBlockState(pos.relative(direction));
         connectedFaces += neighbourState.getBlock().equals(this.getTrunk()) && neighbourState.getValue(TOP_BLOCK) ? 1 : 0;
      }

      return connectedFaces;
   }

   public boolean enoughConnections(BlockState state, LevelReader level, BlockPos pos, int minimumFaces) {
      int connectedFaces = 0;

      for (Direction direction : Direction.values()) {
         connectedFaces += level.getBlockState(pos.relative(direction)).getBlock().equals(this.getTrunk()) ? 1 : 0;
      }

      return connectedFaces >= minimumFaces;
   }

   @Override
   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      if (!state.canSurvive(level, currentPos)) {
         level.scheduleTick(currentPos, this, 3);
         return state;
      } else {
         boolean flag = this.isTrunk(facingState)
            || facing == Direction.DOWN && this.canConnectTo(facingState)
            || facing == Direction.UP && Helpers.isBlock(facingState, Blocks.FRUIT_TREE_SAPLING);
         if (!this.isTrunkOrLeaves(level.getBlockState(currentPos.below())) && !this.isTopBlock(state, level, currentPos)) {
            return (BlockState)((BlockState)state.setValue((Property)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(facing == Direction.DOWN ? Direction.UP : facing), flag))
               .setValue(TOP_BLOCK, this.isTopBlock(state, level, currentPos));
         } else {
            return (!this.isLeaves(facingState) || !state.getValue(TOP_BLOCK)) && this.isLeaves(facingState)
               ? state
               : (BlockState)((BlockState)state.setValue((Property)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(facing), flag)).setValue(TOP_BLOCK, this.isTopBlock(state, level, currentPos));
         }
      }
   }

   @Override
   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      for (Direction direction : Plane.HORIZONTAL) {
         BlockPos relativePos = pos.relative(direction);
         BlockState relativeStateBelow = level.getBlockState(relativePos.below());
         if (this.isTrunk(level.getBlockState(relativePos)) || this.isTrunk(relativeStateBelow) || this.canConnectTo(relativeStateBelow)) {
            return !state.getValue(TOP_BLOCK) && !this.canConnectToSoil(relativeStateBelow) ? this.enoughConnections(state, level, pos, 2) : true;
         }
      }

      BlockState belowState = level.getBlockState(pos.below());
      return this.isTrunk(belowState) || this.canConnectTo(belowState) || level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
   }

   @Override
   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = this.defaultBlockState();
      return FluidHelpers.fillWithFluid(
         (BlockState)((BlockState)this.getStateForPlacement(level, pos).setValue(TFCFBlockStateProperties.NATURAL, context.getPlayer() == null))
            .setValue(TOP_BLOCK, this.isTopBlock(state, level, pos)),
         level.getBlockState(pos).getFluidState().getType()
      );
   }

   @Override
   public BlockState getStateForPlacement(BlockGetter level, BlockPos pos) {
      Block trunkBlock = this.getTrunk();
      BlockState downBlock = level.getBlockState(pos.below());
      BlockState upBlock = level.getBlockState(pos.above());
      BlockState northBlock = level.getBlockState(pos.north());
      BlockState eastBlock = level.getBlockState(pos.east());
      BlockState southBlock = level.getBlockState(pos.south());
      BlockState westBlock = level.getBlockState(pos.west());
      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState()
                        .setValue(
                           DOWN,
                           Helpers.isBlock(downBlock, trunkBlock)
                              || Helpers.isBlock(downBlock, Blocks.BUSH_PLANTABLE_ON)
                              || downBlock.isFaceSturdy(level, pos.below(), Direction.UP)
                        ))
                     .setValue(UP, Helpers.isBlock(upBlock, trunkBlock) || Helpers.isBlock(upBlock, this.getLeaves())))
                  .setValue(NORTH, Helpers.isBlock(northBlock, trunkBlock)))
               .setValue(EAST, Helpers.isBlock(eastBlock, trunkBlock)))
            .setValue(SOUTH, Helpers.isBlock(southBlock, trunkBlock)))
         .setValue(WEST, Helpers.isBlock(westBlock, trunkBlock));
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(TOP_BLOCK) ? SHAPE : super.getShape(state, level, pos, context);
   }

   public boolean generatePlant(LevelAccessor level, BlockPos pos, RandomSource random, int maxHorizontalDistance) {
      if (pos.getY() >= 63 && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
         BlockState originalState = level.getBlockState(pos);
         this.setTrunkWithFluid(level, pos);
         if (this.growTreeRecursive(level, pos, random, pos, maxHorizontalDistance, 0, Plane.HORIZONTAL.getRandomDirection(random))) {
            return true;
         }

         level.setBlock(pos, originalState, 3);
      }

      return false;
   }

   public boolean growTreeRecursive(
      LevelAccessor level,
      BlockPos branchPos,
      RandomSource random,
      BlockPos originalBranchPos,
      int maxHorizontalDistance,
      int iterations,
      Direction originalDirection
   ) {
      int maxHeight = level.getHeight(Types.OCEAN_FLOOR, originalBranchPos.getX(), originalBranchPos.getZ()) + 10 + random.nextInt(6);
      Boolean horizontal = random.nextBoolean() && iterations < 3 && maxHorizontalDistance > 0;
      Direction direction = Plane.HORIZONTAL.getRandomDirection(random);
      boolean willContinue = false;
      if (horizontal) {
         BlockPos adjustedBranchPos = branchPos;
         if (iterations == 0) {
            this.setTrunkWithFluid(level, branchPos);
            adjustedBranchPos = branchPos.above();
         }

         if (this.isTrunk(level.getBlockState(adjustedBranchPos.relative(direction)))) {
            direction = TFCFHelpers.chooseOtherDirection(direction, random);
         }

         if (iterations > 0) {
            direction = originalDirection;
         }

         int horizontalDistance = Mth.randomBetweenInclusive(random, 1, maxHorizontalDistance);
         if (iterations < 4) {
            willContinue = true;

            for (int l = 0; l < horizontalDistance; l++) {
               BlockPos aboveRelativePos = adjustedBranchPos.relative(direction, l + 1);
               Boolean emptyNeighbours = this.allNeighborsEmpty(level, aboveRelativePos, direction.getOpposite());
               if (!emptyNeighbours || aboveRelativePos.getY() >= maxHeight) {
                  return false;
               }

               if (emptyNeighbours) {
                  this.setTrunkWithFluid(level, aboveRelativePos);
                  this.setTrunkWithFluid(level, aboveRelativePos.relative(direction.getOpposite()));
                  this.growTreeRecursive(
                     level,
                     adjustedBranchPos.above().relative(direction, horizontalDistance),
                     random,
                     originalBranchPos,
                     maxHorizontalDistance,
                     iterations + random.nextInt(2) + 1,
                     direction
                  );
               }

               if (aboveRelativePos.getY() == maxHeight - 1) {
                  this.placeGrownLeaves(level, aboveRelativePos, 5, Direction.UP);
               }
            }
         }

         if (!willContinue) {
            this.placeGrownLeaves(
               level, adjustedBranchPos.relative(direction, horizontalDistance).above(), random.nextInt(10) == 1 ? 3 : 5, Plane.HORIZONTAL.getRandomDirection(random)
            );
         }
      } else {
         int height = random.nextInt(5) + 1;
         if (iterations == 0) {
            height++;
         }

         for (int j = 0; j < height; j++) {
            BlockPos blockpos = branchPos.above(j + 1);
            if (!this.allNeighborsEmpty(level, blockpos, null) || blockpos.getY() >= maxHeight) {
               return false;
            }

            if (blockpos.getY() == maxHeight - 1) {
               this.placeGrownLeaves(level, blockpos, 5, Direction.UP);
            } else {
               this.setTrunkWithFluid(level, blockpos);
            }

            this.setTrunkWithFluid(level, blockpos.below());
         }

         if (iterations < 4) {
            int branchAttempts = random.nextInt(3);
            if (iterations == 0) {
               branchAttempts++;
            }

            for (int k = 0; k < branchAttempts; k++) {
               BlockPos aboveRelativePosx = branchPos.above(height).relative(direction);
               if (Math.abs(aboveRelativePosx.getX() - originalBranchPos.getX()) < maxHorizontalDistance * 10
                  && Math.abs(aboveRelativePosx.getZ() - originalBranchPos.getZ()) < maxHorizontalDistance * 10
                  && level.getBlockState(aboveRelativePosx).isAir()
                  && level.getBlockState(aboveRelativePosx.below()).isAir()
                  && this.allNeighborsEmpty(level, aboveRelativePosx, direction.getOpposite())) {
                  willContinue = true;
                  this.setTrunkWithFluid(level, aboveRelativePosx);
                  this.setTrunkWithFluid(level, aboveRelativePosx.relative(direction.getOpposite()));
                  this.growTreeRecursive(
                     level, aboveRelativePosx, random, originalBranchPos, maxHorizontalDistance, iterations + random.nextInt(2) + 1, direction
                  );
               }
            }
         }

         if (!willContinue) {
            this.placeGrownLeaves(level, branchPos.above(height), random.nextInt(10) == 1 ? 3 : 5, Plane.HORIZONTAL.getRandomDirection(random));
         }
      }

      return true;
   }

   public boolean allNeighborsEmpty(LevelReader level, BlockPos pos, @Nullable Direction excludingSide) {
      for (Direction direction : Plane.HORIZONTAL) {
         if (direction != excludingSide && !level.getBlockState(pos.relative(direction)).isAir()) {
            return false;
         }
      }

      return true;
   }

   public boolean clearPassage(LevelReader level, BlockPos pos, Direction direction, int horizontalDistance) {
      int occupiedBlocks = 0;

      for (int m = 0; m < horizontalDistance; m++) {
         occupiedBlocks += !level.getBlockState(pos.relative(direction, m + 1)).isAir() ? 1 : 0;
      }

      return occupiedBlocks <= 0;
   }

   public void placeGrownLeaves(LevelAccessor level, BlockPos pos, int age, Direction facing) {
      level.setBlock(
         pos,
         (BlockState)FluidHelpers.fillWithFluid(
               (BlockState)((BlockState)((BlockState)this.getTrunk().defaultBlockState().setValue(TOP_BLOCK, true)).setValue(AGE, age))
                  .setValue((Property)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(facing), true),
               level.getBlockState(pos).getFluidState().getType()
            )
            .setValue(TFCFBlockStateProperties.NATURAL, true),
         3
      );
      this.placeLeavesAround(level, pos, level.getRandom().nextInt(Mth.clamp(5 - age, 1, 5)));
   }

   public void placeDeadLeaves(LevelAccessor level, BlockPos pos, Direction facing) {
      level.setBlock(
         pos,
         (BlockState)FluidHelpers.fillWithFluid(
               (BlockState)((BlockState)((BlockState)this.getTrunk().defaultBlockState().setValue(TOP_BLOCK, true)).setValue(AGE, 5))
                  .setValue((Property)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(facing), true),
               level.getBlockState(pos).getFluidState().getType()
            )
            .setValue(TFCFBlockStateProperties.NATURAL, true),
         3
      );
      this.placeLeavesAround(level, pos, 0);
   }

   public void setTrunkWithFluid(LevelAccessor level, BlockPos pos) {
      level.setBlock(pos, (BlockState)this.getBodyStateWithFluid(level, pos).setValue(TFCFBlockStateProperties.NATURAL, true), 3);
   }

   public BlockState getBodyStateWithFluid(LevelAccessor level, BlockPos pos) {
      return FluidHelpers.fillWithFluid(this.getStateForPlacement(level, pos), level.getBlockState(pos).getFluidState().getType());
   }

   public void placeLeavesAround(LevelAccessor level, BlockPos pos, int chanceToGrowCornerLeaves) {
      for (Direction direction : Plane.HORIZONTAL) {
         BlockPos relativePos = pos.relative(direction);
         BlockState relativeState = level.getBlockState(relativePos);
         BlockState leavesBlock = FluidHelpers.fillWithFluid(this.getLeaves().defaultBlockState(), level.getBlockState(pos).getFluidState().getType());
         if ((EnvironmentHelpers.isWorldgenReplaceable(relativeState) || relativeState.canBeReplaced()) && !this.isTrunk(relativeState)) {
            level.setBlock(
               relativePos,
               (BlockState)((BlockState)((BlockState)leavesBlock.setValue(PalmLeavesBlock.DIRECTION, direction)).setValue(PalmLeavesBlock.CENTER_BLOCK, true))
                  .setValue(TFCPalmLeavesBlock.PERSISTENT, false),
               3
            );
         }

         if (chanceToGrowCornerLeaves == 0) {
            BlockPos relativePosCorner = relativePos.relative(direction.getClockWise());
            BlockState relativeStateCorner = level.getBlockState(relativePosCorner);
            if ((EnvironmentHelpers.isWorldgenReplaceable(relativeStateCorner) || relativeStateCorner.canBeReplaced())
               && !this.isTrunk(relativeStateCorner)) {
               level.setBlock(
                  relativePosCorner,
                  (BlockState)((BlockState)((BlockState)leavesBlock.setValue(PalmLeavesBlock.DIRECTION, direction.getClockWise()))
                        .setValue(PalmLeavesBlock.CORNER_BLOCK, true))
                     .setValue(TFCPalmLeavesBlock.PERSISTENT, false),
                  3
               );
            }
         }
      }
   }

   public void removeLeavesAround(LevelAccessor level, BlockPos pos) {
      for (Direction direction : Plane.HORIZONTAL) {
         BlockPos relativePos = pos.relative(direction);
         BlockPos relativePosCorner = relativePos.relative(direction.getClockWise());
         if (this.isLeaves(level.getBlockState(relativePos))) {
            level.destroyBlock(relativePos, false);
         }

         if (this.isLeaves(level.getBlockState(relativePosCorner))) {
            level.destroyBlock(relativePosCorner, false);
         }
      }
   }
}
