package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class BoulderDeepRockFeature extends Feature<RockConfig> {
   public BoulderDeepRockFeature(Codec<RockConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<RockConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RockConfig config = (RockConfig)context.config();
      RandomSource random = context.random();
      ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
      ChunkData data = provider.get(level, pos);
      RockSettings rock = data.getRockData().getRock(pos.getX(), 10, pos.getZ());
      List<BlockState> states = config.getStates(rock.raw());
      Supplier<BlockState> state;
      if (states.size() == 1) {
         BlockState onlyState = states.get(0);
         state = () -> onlyState;
      } else {
         state = () -> states.get(random.nextInt(states.size()));
      }

      BlockState stateAt = level.getBlockState(pos);
      BlockState rockState = FluidHelpers.fillWithFluid(state.get(), stateAt.getFluidState().getType());
      if (EnvironmentHelpers.isWorldgenReplaceable(stateAt) && rockState != null && rockState.canSurvive(level, pos)) {
         this.setBlock(level, pos, rockState);
         return true;
      } else {
         return false;
      }
   }
}
