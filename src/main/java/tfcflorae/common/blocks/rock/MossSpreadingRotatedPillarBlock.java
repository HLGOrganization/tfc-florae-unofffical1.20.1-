package tfcflorae.common.blocks.rock;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.rock.IMossGrowingBlock;
import net.dries007.tfc.common.blocks.rock.MossSpreadingBlock;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class MossSpreadingRotatedPillarBlock extends MossSpreadingBlock {
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;

   public MossSpreadingRotatedPillarBlock(Properties properties) {
      super(properties.randomTicks());
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(AXIS, Axis.Y));
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return rotatePillar(state, rotation);
   }

   public static BlockState rotatePillar(BlockState state, Rotation rotation) {
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            switch ((Axis)state.getValue(AXIS)) {
               case X:
                  return (BlockState)state.setValue(AXIS, Axis.Z);
               case Z:
                  return (BlockState)state.setValue(AXIS, Axis.X);
               default:
                  return state;
            }
         default:
            return state;
      }
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{AXIS});
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return (BlockState)this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
   }

   public static void spreadMoss(Level world, BlockPos pos, RandomSource random) {
      if (world.isAreaLoaded(pos, 5)
         && (Boolean)TFCConfig.SERVER.enableMossyRockSpreading.get()
         && random.nextInt((Integer)TFCConfig.SERVER.mossyRockSpreadRate.get()) == 0) {
         BlockPos targetPos = pos.offset(random.nextInt(4) - random.nextInt(4), random.nextInt(4) - random.nextInt(4), random.nextInt(4) - random.nextInt(4));
         BlockState targetState = world.getBlockState(targetPos);
         if (targetState.getBlock() instanceof IMossGrowingBlock block) {
            block.convertToMossy(world, targetPos, targetState, true);
         }
      }
   }

   public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      spreadMoss(worldIn, pos, random);
   }
}
