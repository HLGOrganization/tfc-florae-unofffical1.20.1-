package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import tfcflorae.common.blocks.rock.TFCFPointedDripstoneBlock;
import tfcflorae.common.blocks.soil.SandLayerBlock;

public class RootSpikeFeature extends Feature<BlockStateConfiguration> {
   public RootSpikeFeature(Codec<BlockStateConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      BlockStateConfiguration config = (BlockStateConfiguration)context.config();
      MutableBlockPos mutablePos = new MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
      BlockState inputState = config.state;
      int randomHeight = random.nextInt(5) + 1;

      for (int height = 0; height < randomHeight; height++) {
         int y = (level.getBlockState(pos.below()).getBlock() instanceof SandLayerBlock ? pos.getY() - 1 : pos.getY()) + height;
         mutablePos.set(mutablePos.getX(), y, mutablePos.getZ());
         if (inputState.getBlock() instanceof TFCFPointedDripstoneBlock dripstoneBlock) {
            ((BlockState)inputState.setValue(
                  TFCFPointedDripstoneBlock.THICKNESS, dripstoneBlock.calculateDripstoneThickness(level, mutablePos, Direction.UP, false)
               ))
               .setValue(TFCFPointedDripstoneBlock.TIP_DIRECTION, Direction.UP);
         }

         inputState = FluidHelpers.fillWithFluid(inputState, level.getBlockState(mutablePos).getFluidState().getType());
         level.setBlock(mutablePos, inputState, 3);
      }

      return true;
   }
}
