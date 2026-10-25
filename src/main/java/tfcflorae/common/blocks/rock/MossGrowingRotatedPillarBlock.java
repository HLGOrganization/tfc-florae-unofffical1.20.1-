package tfcflorae.common.blocks.rock;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.rock.IMossGrowingBlock;
import net.dries007.tfc.common.blocks.rock.MossGrowingBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
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
import net.minecraft.world.level.material.Fluids;

public class MossGrowingRotatedPillarBlock extends MossGrowingBlock implements IMossGrowingBlock {
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;
   private final Supplier<? extends Block> mossy;

   public MossGrowingRotatedPillarBlock(Properties properties, Supplier<? extends Block> mossy) {
      super(properties, mossy);
      this.mossy = mossy;
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

   public void convertToMossy(Level worldIn, BlockPos pos, BlockState state, boolean needsWater) {
      if (!needsWater || FluidHelpers.isSame(worldIn.getFluidState(pos.above()), Fluids.WATER)) {
         worldIn.setBlock(pos, this.mossy.get().defaultBlockState(), 3);
      }
   }
}
