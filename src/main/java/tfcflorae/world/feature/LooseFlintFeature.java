package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import tfcflorae.common.blocks.TFCFBlocks;

public class LooseFlintFeature extends Feature<NoneFeatureConfiguration> {
   public LooseFlintFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      BlockState stateAt = level.getBlockState(pos);
      BlockState rockState = FluidHelpers.fillWithFluid(((Block)TFCFBlocks.LOOSE_FLINT.get()).defaultBlockState(), stateAt.getFluidState().getType());
      if (EnvironmentHelpers.isWorldgenReplaceable(stateAt) && rockState != null && rockState.canSurvive(level, pos)) {
         this.setBlock(level, pos, (BlockState)rockState.setValue(TFCBlockStateProperties.COUNT_1_3, 1 + random.nextInt(2)));
         return true;
      } else {
         return false;
      }
   }
}
