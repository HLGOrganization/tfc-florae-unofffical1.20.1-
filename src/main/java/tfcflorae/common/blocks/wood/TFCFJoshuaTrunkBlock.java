package tfcflorae.common.blocks.wood;
import tfcflorae.common.TFCFBlockStateProperties;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraftforge.common.ForgeHooks;
import tfcflorae.common.blocks.TFCFBlocks;

public class TFCFJoshuaTrunkBlock extends PipeBlock implements IFluidLoggable {
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   public static final BooleanProperty NATURAL = TFCFBlockStateProperties.NATURAL;
   public final RegistryWood wood;
   private final ExtendedProperties properties;

   public static TFCFJoshuaTrunkBlock create(RegistryWood wood, Properties builder, ExtendedProperties properties, final FluidProperty fluid) {
      return new TFCFJoshuaTrunkBlock(wood, properties.properties(), properties) {
         @Override
         public FluidProperty getFluidProperty() {
            return fluid;
         }
      };
   }

   public TFCFJoshuaTrunkBlock(RegistryWood wood, Properties builder, ExtendedProperties properties) {
      super(0.3125F, properties.properties());
      this.properties = properties;
      this.wood = wood;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any())
                              .setValue(NORTH, false))
                           .setValue(EAST, false))
                        .setValue(SOUTH, false))
                     .setValue(WEST, false))
                  .setValue(UP, false))
               .setValue(DOWN, false))
            .setValue(TFCFBlockStateProperties.NATURAL, false)
      );
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      float baseSpeed = (float)(state.getValue(TFCFBlockStateProperties.NATURAL) ? 2 : 1) * state.getDestroySpeed(level, pos);
      if (baseSpeed == -1.0F) {
         return 0.0F;
      } else {
         int toolModifier = ForgeHooks.isCorrectToolForDrops(state, player) ? 30 : 100;
         return player.getDigSpeed(state, pos) / baseSpeed / (float)toolModifier;
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
   }

   public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
      super.playerWillDestroy(level, pos, state, player);
      FluidHelpers.tickFluid(level, pos, state);
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      builder.add(new Property[]{this.getFluidProperty(), NORTH, EAST, SOUTH, WEST, UP, DOWN, NATURAL});
   }

   public BlockState getStateForPlacement(BlockGetter world, BlockPos pos) {
      FluidState fluidState = world.getFluidState(pos);
      BlockState state = this.defaultBlockState();
      BlockState downBlockState = world.getBlockState(pos.below());
      BlockState upBlockState = world.getBlockState(pos.above());
      BlockState northBlockState = world.getBlockState(pos.north());
      BlockState eastBlockState = world.getBlockState(pos.east());
      BlockState southBlockState = world.getBlockState(pos.south());
      BlockState westBlockState = world.getBlockState(pos.west());
      if (this.getFluidProperty().canContain(fluidState.getType())) {
         state = (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
      }

      return (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)state.setValue(
                        DOWN,
                        (this.getJoshuaTrunkBlock(downBlockState) || this.canConnectTo(downBlockState)) && !this.getJoshuaLeavesBlock(northBlockState)
                     ))
                     .setValue(UP, this.getJoshuaTreeBlocks(upBlockState)))
                  .setValue(NORTH, this.getJoshuaTrunkBlock(northBlockState) && !this.getJoshuaLeavesBlock(northBlockState)))
               .setValue(EAST, this.getJoshuaTrunkBlock(eastBlockState) && !this.getJoshuaLeavesBlock(eastBlockState)))
            .setValue(SOUTH, this.getJoshuaTrunkBlock(southBlockState) && !this.getJoshuaLeavesBlock(southBlockState)))
         .setValue(WEST, this.getJoshuaTrunkBlock(westBlockState) && !this.getJoshuaLeavesBlock(westBlockState));
   }

   public boolean getJoshuaTreeBlocks(BlockState state) {
      return Helpers.isBlock(state, ((Block)TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get()).defaultBlockState().getBlock())
         || Helpers.isBlock(state, ((Block)TFCFBlocks.JOSHUA_LEAVES.get(this.wood).get()).defaultBlockState().getBlock());
   }

   public boolean getJoshuaTrunkBlock(BlockState state) {
      return Helpers.isBlock(state, (Block)TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get());
   }

   public boolean getJoshuaLeavesBlock(BlockState state) {
      return Helpers.isBlock(state, (Block)TFCFBlocks.JOSHUA_LEAVES.get(this.wood).get());
   }

   public boolean canConnectTo(BlockState state) {
      return Helpers.isBlock(state, Blocks.BUSH_PLANTABLE_ON)
         || Helpers.isBlock(state, Blocks.TREE_GROWS_ON)
         || Helpers.isBlock(state, BlockTags.SAND)
         || Helpers.isBlock(state, net.minecraftforge.common.Tags.Blocks.GRAVEL)
         || state.isSolid();
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      if (!state.canSurvive(level, currentPos)) {
         level.scheduleTick(currentPos, this, 1);
         FluidHelpers.tickFluid(level, currentPos, state);
         return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
      } else {
         FluidHelpers.tickFluid(level, currentPos, state);
         boolean flag = this.getJoshuaTreeBlocks(facingState) || facing == Direction.DOWN && this.canConnectTo(facingState);
         return (BlockState)state.setValue((Property)net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION.get(facing), flag);
      }
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState belowState = level.getBlockState(pos.below());

      for (Direction direction : Plane.HORIZONTAL) {
         BlockPos relativePos = pos.relative(direction);
         if (Helpers.isBlock(level.getBlockState(relativePos), (Block)TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get())) {
            BlockState belowState2 = level.getBlockState(relativePos.below());
            if (this.getJoshuaTrunkBlock(belowState2) || this.canConnectTo(belowState2)) {
               return true;
            }
         }
      }

      return this.getJoshuaTrunkBlock(belowState) || this.canConnectTo(belowState);
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   public boolean isRandomlyTicking(BlockState state) {
      return true;
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public void setBodyStateWithFluidStatic(LevelAccessor level, BlockPos pos) {
      BlockState state = ((Block)TFCFBlocks.JOSHUA_TRUNK.get(this.wood).get()).defaultBlockState();
      FluidState fluidState = level.getFluidState(pos);
      if (this.getFluidProperty().canContain(fluidState.getType())) {
         state = (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
      }

      level.setBlock(pos, state, 2);
   }
}
