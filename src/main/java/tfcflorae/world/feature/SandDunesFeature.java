package tfcflorae.world.feature;
import net.minecraft.world.level.material.MapColor;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import tfcflorae.Config;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.MossGrowingBoulderBlock;
import tfcflorae.common.blocks.rock.MossSpreadingBoulderBlock;
import tfcflorae.common.blocks.soil.SandLayerBlock;
import tfcflorae.util.TFCFHelpers;

public class SandDunesFeature extends Feature<NoneFeatureConfiguration> {
   public SandDunesFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      if (!(Boolean)Config.COMMON.enableDunes.get()) {
         return false;
      } else {
         WorldGenLevel level = context.level();
         BlockPos pos = context.origin();
         RandomSource random = context.random();
         ChunkDataProvider provider = ChunkDataProvider.get(level);
         if (provider.get(level, pos).getRainfall(pos) > 75.0F) {
            return false;
         } else {
            MutableBlockPos mutablePos = new MutableBlockPos();

            for (int i = 0; i < 16; i++) {
               for (int j = 0; j < 16; j++) {
                  int x = pos.getX() + i;
                  int z = pos.getZ() + j;
                  int y = level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                  int duneHeightNoise = 0;
                  float duneLimiter = 0.0F;
                  mutablePos.set(x, y, z);
                  ChunkData data = provider.get(level, mutablePos);
                  float rainfall = data.getRainfall(mutablePos) - 500.0F;
                  ChunkGenerator chunkPos = context.chunkGenerator();
                  if (chunkPos instanceof TFCChunkGenerator) {
                     TFCChunkGenerator chunkGen = (TFCChunkGenerator)chunkPos;
                     if (rainfall < 75.0F && !FluidHelpers.isAirOrEmptyFluid(level.getBlockState(mutablePos.below()))) {
                        ChunkPos chunkPosx = new ChunkPos(mutablePos);
                        double actualHeight = chunkGen.createHeightFillerForChunk(chunkPosx).sampleHeight(x, z);
                        if (rainfall < 72.0F) {
                           float heightModifier = Mth.clamp((float)actualHeight * 0.1F - 6.5F, 0.0F, 1.0F);
                           duneLimiter = Mth.clamp((float)Math.pow(0.99F, (double)(rainfall - 128.0F)) - 1.8F, 0.0F, 1.0F);
                           duneLimiter = heightModifier > 0.0F ? duneLimiter * heightModifier : 0.0F;
                           duneHeightNoise = Mth.clamp(
                              Mth.floor(dunes(level.getSeed(), 63, y).noise((float)mutablePos.getX(), (float)mutablePos.getZ()) * duneLimiter),
                              0,
                              y + 12
                           );
                        }

                        if (duneHeightNoise > 0) {
                           for (int yDune = y; yDune <= duneHeightNoise; yDune++) {
                              mutablePos.set(x, yDune, z);
                              if (this.canReplace(level, mutablePos, random)) {
                                 for (Direction facing : Plane.HORIZONTAL) {
                                    BlockPos posOffset = mutablePos.relative(facing).below();
                                    if (level.getBlockState(posOffset).isFaceSturdy(level, posOffset, Direction.UP)) {
                                       level.setBlock(
                                          mutablePos,
                                          TFCFHelpers.getSandBlock(level, mutablePos, (Boolean)Config.COMMON.toggleCheapSandColourCalculations.get())
                                             .defaultBlockState(),
                                          2
                                       );
                                       mutablePos.set(x, mutablePos.getY(), z).above();
                                       if (this.hasNearbySandOrIsDryEnough(level, mutablePos, random, true)
                                          && this.canReplace(level, mutablePos, random)
                                          && !(level.getBlockState(mutablePos).getBlock() instanceof SandLayerBlock)) {
                                          int layers = Mth.nextInt(random, 1, 5);
                                          SandLayerBlock.placeSandPileStatic(
                                             level,
                                             ((Block)TFCFBlocks.SAND_LAYERS
                                                   .get(
                                                      TFCFHelpers.getSandColorTFCF(
                                                         level, mutablePos, (Boolean)Config.COMMON.toggleCheapSandColourCalculations.get()
                                                      )
                                                   )
                                                   .get())
                                                .defaultBlockState(),
                                             mutablePos,
                                             level.getBlockState(mutablePos),
                                             layers,
                                             false,
                                             false
                                          );
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            return false;
         }
      }
   }

   public boolean hasNearbySandOrIsDryEnough(WorldGenLevel level, BlockPos pos, RandomSource random, boolean isPicky) {
      Direction[] var5 = TFCFHelpers.DIRECTIONS;
      int var6 = var5.length;
      byte var7 = 0;
      if (var7 >= var6) {
         return false;
      } else {
         Direction direction = var5[var7];
         boolean isSandy = false;
         if (isPicky) {
            isSandy = Helpers.isBlock(level.getBlockState(pos.relative(direction)).getBlock(), BlockTags.SAND);
         } else {
            isSandy = level.getBlockState(pos.relative(direction)).getBlock() instanceof SandBlock
               || level.getBlockState(pos.relative(direction)).getBlock().defaultMapColor() == MapColor.SAND
                  && level.getBlockState(pos.relative(direction)).getSoundType() == SoundType.SAND;
         }

         return isSandy || isSandy && this.properRainfall(level, pos, random);
      }
   }

   public static Noise2D dunes(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.2F).scaled(-100.0F, 100.0F, (float)minHeight, (float)maxHeight);
      Noise2D baseNoise = new OpenSimplex2D(seed).octaves(3).spread(0.25F).scaled(-3.0F, 3.0F).clamped(0.0F, 1.0F);
      Noise2D starDunes = new OpenSimplex2D(seed)
         .octaves(6)
         .spread(0.25F)
         .add(new OpenSimplex2D(seed + 1L).octaves(2).spread(0.25F).scaled(-0.7F, 0.7F).ridged())
         .map(x -> {
            float x0 = (float)(0.125 * (x + 1.0) * (x + 1.0) * (x + 1.0));
            return (float)minHeight + (float)(maxHeight - 63) * x0;
         });
      Noise2D randomDunes = new OpenSimplex2D(seed).octaves(6).spread(0.25F).warped(warp).ridged().map(x -> {
         float x0 = (float)(0.125 * (x + 1.0) * (x + 1.25) * (x + 1.0));
         return (float)minHeight + (float)(maxHeight - 63) * x0;
      });
      return (x, z) -> {
         float baseHeight = (float)(baseNoise.noise(x, z));
         float starDuneHeight = (float)(starDunes.noise(x, z));
         float randomDunesHeight = (float)(randomDunes.noise(x, z));
         return randomDunesHeight * (1.0F - baseHeight) + starDuneHeight * baseHeight;
      };
   }

   public boolean canReplace(WorldGenLevel level, BlockPos pos, RandomSource random) {
      return EnvironmentHelpers.isWorldgenReplaceable(level, pos)
         || level.getBlockState(pos).getBlock() instanceof GroundcoverBlock
         || level.getBlockState(pos).getBlock() instanceof MossGrowingBoulderBlock
         || level.getBlockState(pos).getBlock() instanceof MossSpreadingBoulderBlock
         || level.getBlockState(pos).getBlock().defaultMapColor() == MapColor.PLANT
         || level.getBlockState(pos).getBlock().defaultMapColor() == MapColor.PLANT;
   }

   public boolean canReplace(WorldGenLevel level, BlockPos pos, RandomSource random, BlockState sandLayer) {
      BlockState state = level.getBlockState(pos);
      return !level.getBlockState(pos).liquid()
         && (
            SandLayerBlock.canPlaceSandPileStatic(level, pos, state, sandLayer)
               || EnvironmentHelpers.isWorldgenReplaceable(level, pos)
               || state.getBlock() instanceof GroundcoverBlock
               || state.getBlock() instanceof MossGrowingBoulderBlock
               || state.getBlock() instanceof MossSpreadingBoulderBlock
               || state.getBlock().defaultMapColor() == MapColor.PLANT
               || state.getBlock().defaultMapColor() == MapColor.PLANT
         );
   }

   public boolean properRainfall(WorldGenLevel level, BlockPos pos, RandomSource random) {
      ChunkDataProvider provider = ChunkDataProvider.get(level);
      ChunkData data = provider.get(level, pos);
      float rainfall = data.getRainfall(pos);
      float noiseRainfall = rainfall + 10.0F * (float)random.nextGaussian();
      return noiseRainfall <= 80.0F;
   }
}
