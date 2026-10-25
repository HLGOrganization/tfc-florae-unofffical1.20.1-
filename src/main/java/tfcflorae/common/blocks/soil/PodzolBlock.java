package tfcflorae.common.blocks.soil;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.plant.PlantRegrowth;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class PodzolBlock extends ConnectedGrassBlock {
   private static final Map<Direction, BooleanProperty> PROPERTIES = ImmutableMap.of(
      Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.WEST, WEST, Direction.SOUTH, SOUTH
   );
   private final Supplier<? extends Block> dirt;
   @Nullable
   private final Supplier<? extends Block> path;
   @Nullable
   private final Supplier<? extends Block> farmland;

   public PodzolBlock(Properties properties, SoilBlockType dirtType, Variant variant) {
      this(
         properties,
         (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(dirtType)).get(variant),
         (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.GRASS_PATH)).get(variant),
         (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)).get(variant)
      );
   }

   public PodzolBlock(
      Properties properties, Supplier<? extends Block> dirt, @Nullable Supplier<? extends Block> path, @Nullable Supplier<? extends Block> farmland
   ) {
      super(properties.hasPostProcess(TFCBlocks::always), dirt, path, farmland);
      this.dirt = dirt;
      this.path = path;
      this.farmland = farmland;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(SOUTH, false)).setValue(EAST, false))
                  .setValue(NORTH, false))
               .setValue(WEST, false))
            .setValue(SNOWY, false)
      );
   }

   public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      if (facing == Direction.UP) {
         return (BlockState)stateIn.setValue(SNOWY, Helpers.isBlock(facingState, Blocks.SNOW));
      } else {
         return facing != Direction.DOWN ? this.updateStateFromDirection(level, currentPos, stateIn, facing) : stateIn;
      }
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      level.scheduleTick(pos, this, 0);
   }

   public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
      for (Direction direction : Plane.HORIZONTAL) {
         level.scheduleTick(pos.relative(direction).above(), this, 0);
      }
   }

   public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
      for (Direction direction : Plane.HORIZONTAL) {
         level.scheduleTick(pos.relative(direction).above(), this, 0);
      }

      super.onRemove(state, level, pos, newState, isMoving);
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!this.canBeGrass(state, level, pos)) {
         if (level.isAreaLoaded(pos, 3)) {
            level.setBlockAndUpdate(pos, this.getDirt());
         }
      } else {
         PlantRegrowth.placeRisingRock(level, pos.above(), random);
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
      level.setBlock(pos, this.updateStateFromNeighbors(level, pos, state), 2);
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState stateUp = context.getLevel().getBlockState(context.getClickedPos().above());
      return (BlockState)this.updateStateFromNeighbors(context.getLevel(), context.getClickedPos(), this.defaultBlockState())
         .setValue(
            SNOWY,
            Helpers.isBlock(stateUp, net.minecraft.world.level.block.Blocks.SNOW_BLOCK)
               || Helpers.isBlock(stateUp, net.minecraft.world.level.block.Blocks.SNOW)
         );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{NORTH, EAST, SOUTH, WEST, SNOWY});
   }

   public BlockState getDirt() {
      return this.dirt.get().defaultBlockState();
   }

   @Nullable
   public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
      if (context.getItemInHand().canPerformAction(action)) {
         if (action == ToolActions.SHOVEL_FLATTEN && (Boolean)TFCConfig.SERVER.enableGrassPathCreation.get() && this.path != null) {
            return this.path.get().defaultBlockState();
         }

         if (action == ToolActions.HOE_TILL && this.farmland != null && (Boolean)TFCConfig.SERVER.enableFarmlandCreation.get() && HoeItem.onlyIfAirAbove(context)) {
            BlockState farmlandState = this.farmland.get().defaultBlockState();
            HoeItem.changeIntoState(farmlandState).accept(context);
            return farmlandState;
         }
      }

      return null;
   }
}
