package tfcflorae.common.blocks.plant;

import java.util.Map;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.CreepingPlantBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import tfcflorae.common.TFCFTags;

public abstract class CreepingStonePlantBlock extends CreepingPlantBlock {
   protected final Map<BlockState, VoxelShape> shapeCache;

   public static CreepingStonePlantBlock createStone(final RegistryPlant plant, ExtendedProperties properties) {
      return new CreepingStonePlantBlock(properties) {
         public RegistryPlant getPlant() {
            return plant;
         }

         @Override
         public boolean canCreepOn(BlockState state) {
            return Helpers.isBlock(state, TFCFTags.Blocks.CREEPING_STONE_PLANTABLE_ON);
         }
      };
   }

   protected CreepingStonePlantBlock(ExtendedProperties properties) {
      super(properties);
      this.registerDefaultState(DirectionPropertyBlock.setAllDirections((BlockState)this.getStateDefinition().any(), false));
      this.shapeCache = DirectionPropertyBlock.makeShapeCache(this.getStateDefinition(), SHAPES::get);
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      state = (BlockState)state.setValue((Property)PipeBlock.PROPERTY_BY_DIRECTION.get(direction), this.canCreepOn(facingState));
      return this.isEmpty(state) ? Blocks.AIR.defaultBlockState() : state;
   }

   private boolean isEmpty(BlockState state) {
      for (BooleanProperty property : SHAPES.keySet()) {
         if ((Boolean)state.getValue(property)) {
            return false;
         }
      }

      return true;
   }

   @NotNull
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.updateStateFromSides(context.getLevel(), context.getClickedPos(), this.updateStateWithCurrentMonth(this.defaultBlockState()));
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (Direction direction : UPDATE_SHAPE_ORDER) {
         if (this.canCreepOn(level.getBlockState(mutablePos.setWithOffset(pos, direction)))) {
            return true;
         }
      }

      return false;
   }

   protected static BlockState updateStateFromSidesStone(LevelAccessor level, BlockPos pos, BlockState state) {
      MutableBlockPos mutablePos = new MutableBlockPos();
      boolean hasEarth = false;

      for (Direction direction : UPDATE_SHAPE_ORDER) {
         mutablePos.setWithOffset(pos, direction);
         boolean ground = level.getBlockState(mutablePos).isSolid();
         state = (BlockState)state.setValue((Property)PipeBlock.PROPERTY_BY_DIRECTION.get(direction), ground);
         hasEarth |= ground;
      }

      return hasEarth ? state : Blocks.AIR.defaultBlockState();
   }

   public boolean canCreepOn(BlockState state) {
      return Helpers.isBlock(state, net.dries007.tfc.common.TFCTags.Blocks.CREEPING_PLANTABLE_ON);
   }
}
