package tfcflorae.common.blocks.plant;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.EpiphytePlantBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

public abstract class CacaoPodBlock extends EpiphytePlantBlock {
   public static final int MAX_AGE = 2;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_2;
   protected static final int AGE_0_WIDTH = 4;
   protected static final int AGE_0_HEIGHT = 5;
   protected static final int AGE_0_HALFWIDTH = 2;
   protected static final int AGE_1_WIDTH = 6;
   protected static final int AGE_1_HEIGHT = 7;
   protected static final int AGE_1_HALFWIDTH = 3;
   protected static final int AGE_2_WIDTH = 8;
   protected static final int AGE_2_HEIGHT = 9;
   protected static final int AGE_2_HALFWIDTH = 4;
   protected static final VoxelShape[] EAST_AABB = new VoxelShape[]{
      Block.box(11.0, 7.0, 6.0, 15.0, 12.0, 10.0), Block.box(9.0, 5.0, 5.0, 15.0, 12.0, 11.0), Block.box(7.0, 3.0, 4.0, 15.0, 12.0, 12.0)
   };
   protected static final VoxelShape[] WEST_AABB = new VoxelShape[]{
      Block.box(1.0, 7.0, 6.0, 5.0, 12.0, 10.0), Block.box(1.0, 5.0, 5.0, 7.0, 12.0, 11.0), Block.box(1.0, 3.0, 4.0, 9.0, 12.0, 12.0)
   };
   protected static final VoxelShape[] NORTH_AABB = new VoxelShape[]{
      Block.box(6.0, 7.0, 1.0, 10.0, 12.0, 5.0), Block.box(5.0, 5.0, 1.0, 11.0, 12.0, 7.0), Block.box(4.0, 3.0, 1.0, 12.0, 12.0, 9.0)
   };
   protected static final VoxelShape[] SOUTH_AABB = new VoxelShape[]{
      Block.box(6.0, 7.0, 11.0, 10.0, 12.0, 15.0), Block.box(5.0, 5.0, 9.0, 11.0, 12.0, 15.0), Block.box(4.0, 3.0, 7.0, 12.0, 12.0, 15.0)
   };

   public static CacaoPodBlock create(final RegistryPlant plant, ExtendedProperties properties) {
      return new CacaoPodBlock(properties) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected CacaoPodBlock(ExtendedProperties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(AGE, 0));
   }

   public boolean isRandomlyTicking(BlockState state) {
      return (Integer)state.getValue(AGE) < 2;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if ((Integer)state.getValue(this.getPlant().getStageProperty()) > 1 && random.nextDouble() < (Double)TFCConfig.SERVER.plantGrowthChance.get()) {
         int i = (Integer)state.getValue(AGE);
         if (i < 2 && ForgeHooks.onCropsGrowPre(level, pos, state, level.random.nextInt(5) == 0)) {
            level.setBlock(pos, (BlockState)state.setValue(AGE, i + 1), 2);
            ForgeHooks.onCropsGrowPost(level, pos, state);
         }
      }

      level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      int i = (Integer)state.getValue(AGE);
      switch ((Direction)state.getValue(FACING)) {
         case SOUTH:
            return SOUTH_AABB[i];
         case WEST:
            return WEST_AABB[i];
         case EAST:
            return EAST_AABB[i];
         case NORTH:
         default:
            return NORTH_AABB[i];
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = this.defaultBlockState();
      LevelReader level = context.getLevel();
      BlockPos pos = context.getClickedPos();

      for (Direction direction : context.getNearestLookingDirections()) {
         if (direction.getAxis().isHorizontal()) {
            state = (BlockState)state.setValue(FACING, direction);
            if (state.canSurvive(level, pos)) {
               return state;
            }
         }
      }

      return null;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{this.getPlant().getStageProperty(), FACING, AGE});
   }
}
