package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;

public class RotatedBoulderRockFeature extends Feature<NoneFeatureConfiguration> {
   public RotatedBoulderRockFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
      ChunkData data = provider.get(level, pos);
      float rainfall = data.getRainfall(pos);
      float forestDensity = data.getForestDensity();
      float forestWeirdness = data.getForestWeirdness();
      Boolean generateMossy = random.nextFloat() * (Mth.clamp(Mth.abs((rainfall + 1.0F) * forestDensity), 1.0F, Float.MAX_VALUE))
         > random.nextFloat() * (Mth.clamp(Mth.abs((rainfall + 1.0F) * forestWeirdness), 1.0F, Float.MAX_VALUE));
      TFCFRock.TFCFBlockType rockType = TFCFRock.TFCFBlockType.PEBBLE;
      if (generateMossy) {
         rockType = TFCFRock.TFCFBlockType.RUBBLE;
      }

      BlockState stateAt = level.getBlockState(pos);
      BlockState rockState = FluidHelpers.fillWithFluid(rockPile(level, pos, rockType), stateAt.getFluidState().getType());
      Direction direction = Direction.getRandom(random);
      BlockPos blockPos = pos.relative(direction.getOpposite());
      BlockState blockState = level.getBlockState(blockPos);
      if (EnvironmentHelpers.isWorldgenReplaceable(stateAt)
         && rockState != null
         && blockState.isFaceSturdy(level, blockPos, direction)
         && Helpers.isBlock(blockState, BlockTags.BASE_STONE_OVERWORLD)) {
         this.setBlock(level, pos, (BlockState)rockState.setValue(BlockStateProperties.FACING, direction));
         return true;
      } else {
         return false;
      }
   }

   public static BlockState rockPile(WorldGenLevel level, BlockPos pos, TFCFRock.TFCFBlockType rockType) {
      ChunkDataProvider provider = ChunkDataProvider.get(level);
      RockSettings surfaceRock = provider.get(level, pos).getRockData().getRock(pos);
      Rock rockTFC = null;
      TFCFRock rockTFCF = null;
      if (surfaceRock != null) {
         for (Rock r : Rock.values()) {
            if (surfaceRock.raw() == ((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(r)).get(BlockType.RAW)).get()) {
               rockTFC = r;
               break;
            }

            for (TFCFRock r2 : TFCFRock.values()) {
               if (surfaceRock.raw() == TFCFBlocks.TFCF_ROCK_BLOCKS.get(r2).get(BlockType.RAW).get()) {
                  rockTFCF = r2;
                  break;
               }
            }
         }
      }

      if (rockTFC != null) {
         return ((Block)TFCFBlocks.ROCK_BLOCKS.get(rockTFC).get(rockType).get()).defaultBlockState();
      } else {
         return rockTFCF != null
            ? ((Block)TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rockTFCF).get(rockType).get()).defaultBlockState()
            : ((Block)TFCFBlocks.ROCK_BLOCKS.get(Rock.GRANITE).get(rockType).get()).defaultBlockState();
      }
   }
}
