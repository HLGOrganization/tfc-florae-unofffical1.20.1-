package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.noise.Metaballs3D;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class BouldersFeature extends Feature<BoulderConfig> {
   public BouldersFeature(Codec<BoulderConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<BoulderConfig> context) {
      WorldGenLevel level = context.getLevel();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      BoulderConfig config = (BoulderConfig)context.config();
      ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
      ChunkData data = provider.get(context.getLevel(), pos);
      RockSettings rock = data.getRockData().getRock(pos);
      List<BlockState> states = config.getStates(rock.raw());
      int seaLevel = level.getLevel().getChunkSource().getGenerator().getSeaLevel();
      if (states != null && pos.getY() < seaLevel - 8) {
         this.place(level, pos, states, random, context);
         return true;
      } else {
         return false;
      }
   }

   private void place(WorldGenLevel level, BlockPos pos, List<BlockState> states, RandomSource random, FeaturePlaceContext<BoulderConfig> context) {
      MutableBlockPos mutablePos = new MutableBlockPos();
      BoulderConfig config = (BoulderConfig)context.config();
      int minSize = config.sizeMin();
      int randomSize = config.sizeRandom();
      int sizeX = minSize + random.nextInt(randomSize);
      int sizeY = minSize + random.nextInt(randomSize);
      int sizeZ = minSize + random.nextInt(randomSize);
      Metaballs3D noise = new Metaballs3D(Helpers.fork(random), 6, 8, -0.12F * (float)sizeX, 0.3F * (float)sizeY, 0.3F * (float)sizeZ);
      Supplier<BlockState> state;
      if (states.size() == 1) {
         BlockState onlyState = states.get(0);
         state = () -> onlyState;
      } else {
         state = () -> states.get(random.nextInt(states.size()));
      }

      for (int x = -sizeX; x <= sizeX; x++) {
         for (int y = -sizeY; y <= sizeY; y++) {
            for (int z = -sizeZ; z <= sizeZ; z++) {
               if (noise.inside((float)x, (float)y, (float)z)) {
                  mutablePos.setWithOffset(pos, x, y, z);
                  this.setBlock(level, mutablePos, state.get());
               }
            }
         }
      }
   }
}
