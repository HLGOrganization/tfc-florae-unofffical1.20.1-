package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.core.BlockPos;
import tfcflorae.common.blocks.soil.TFCFRockSoil;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.world.surface.TFCFSoilSurfaceState;

public class RockyDirtSurfaceBuilder implements SurfaceBuilder {
   private final SurfaceBuilder parent;
   private final Noise2D surfaceMaterialNoise;
   private final Noise2D heightNoise;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new RockyDirtSurfaceBuilder(parent.apply(seed), seed);
   }

   public RockyDirtSurfaceBuilder(SurfaceBuilder parent, long seed) {
      this.parent = parent;
      this.surfaceMaterialNoise = new OpenSimplex2D(seed).octaves(2).spread(0.04F);
      this.heightNoise = new OpenSimplex2D(seed + 71829341L).octaves(2).spread(0.1F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.parent.buildSurface(context, startY, endY);
      float noiseRainfall = context.rainfall() + 10.0F * (float)context.random().nextGaussian();
      if (noiseRainfall >= 80.0F) {
         this.buildSoilSurface(context, startY, endY);
      }
   }

   private void buildSoilSurface(SurfaceBuilderContext context, int startY, int endY) {
      int topBlock = startY - 1;
      ChunkData data = context.getChunkData();
      BlockPos pos = new BlockPos(context.pos().getX(), topBlock, context.pos().getZ());
      float rainfallFraction = data.getRainfall(pos) * 5.0E-4F;
      float rainfall = data.getRainfall(pos);
      SurfaceState PEBBLE_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.PEBBLE_COMPACT_DIRT);
      SurfaceState ROCKY_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.ROCKY_COMPACT_DIRT);
      SurfaceState ROCKIER_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.ROCKIER_COMPACT_DIRT);
      SurfaceState ROCKIEST_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.ROCKIEST_COMPACT_DIRT);
      SurfaceState SPARSE_GRASS = TFCFSoilSurfaceState.buildType(TFCFSoil.SPARSE_GRASS);
      SurfaceState DENSE_GRASS = TFCFSoilSurfaceState.buildType(TFCFSoil.DENSE_GRASS);
      SurfaceState COARSE_DIRT = TFCFSoilSurfaceState.buildType(TFCFSoil.COARSE_DIRT);
      SurfaceState COMPACT_DIRT = TFCFSoilSurfaceState.buildType(TFCFSoil.COMPACT_DIRT);
      double randomGauss = Math.abs(context.random().nextGaussian()) * 0.1F;
      double gauss = context.random().nextGaussian();
      double altitudeChance = Math.pow(2.0, (double)((float)topBlock * 0.02F) + 1.75) + 5.0;
      float noise = this.surfaceMaterialNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 0.9F
         + context.random().nextFloat() * 0.1F;
      double heightNoise = (double)(this.heightNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 4.0F + (float)startY);
      if (pos.getY() > context.getSeaLevel() && heightNoise <= 130.0) {
         if ((double)rainfall < 1.5 * gauss + 100.0) {
            if ((double)rainfall >= 1.5 * gauss + 65.0) {
               context.setBlockState(topBlock, DENSE_GRASS);
            } else {
               context.setBlockState(topBlock, SPARSE_GRASS);
            }
         }

         if (Math.abs(context.random().nextGaussian()) * 25.0 <= altitudeChance && topBlock >= 95) {
            int random = context.random().nextInt(7);
            if ((double)noise >= -0.45F + randomGauss && (double)noise < -0.2F + randomGauss) {
               context.setBlockState(topBlock, DENSE_GRASS);
            } else if ((double)noise >= -0.2F + randomGauss && (double)noise < -0.05F + randomGauss) {
               context.setBlockState(topBlock, SPARSE_GRASS);
            } else if ((double)noise >= -0.05F + randomGauss && random <= 0) {
               context.setBlockState(topBlock, COARSE_DIRT);
            } else if ((double)noise >= -0.05F + randomGauss && random == 1) {
               context.setBlockState(topBlock, COMPACT_DIRT);
            } else if ((double)noise >= -0.05F + randomGauss && random == 2) {
               context.setBlockState(topBlock, PEBBLE_COMPACT_DIRT);
            } else if ((double)noise >= -0.05F + randomGauss && random == 3) {
               context.setBlockState(topBlock, ROCKY_COMPACT_DIRT);
            } else if ((double)noise >= -0.05F + randomGauss && random == 4) {
               context.setBlockState(topBlock, ROCKIER_COMPACT_DIRT);
            } else if ((double)noise >= -0.05F + randomGauss && random == 5) {
               context.setBlockState(topBlock, ROCKIEST_COMPACT_DIRT);
            } else if ((double)noise >= -0.05F + randomGauss && random == 6) {
               context.setBlockState(topBlock, SPARSE_GRASS);
            }
         }

         if (rainfallFraction / 5.0F <= (float)context.random().nextInt(500) && topBlock <= 90 && (double)noise >= 0.05F + randomGauss) {
            int random = context.random().nextInt(7);
            if ((double)noise >= 0.05F + randomGauss && (double)noise < 0.2F + randomGauss) {
               context.setBlockState(topBlock, DENSE_GRASS);
            } else if ((double)noise >= 0.2F + randomGauss && (double)noise < 0.35F + randomGauss) {
               context.setBlockState(topBlock, SPARSE_GRASS);
            } else if ((double)noise >= 0.35F + randomGauss && random <= 0) {
               context.setBlockState(topBlock, COARSE_DIRT);
            } else if ((double)noise >= 0.35F + randomGauss && random == 1) {
               context.setBlockState(topBlock, COMPACT_DIRT);
            } else if ((double)noise >= 0.35F + randomGauss && random == 2) {
               context.setBlockState(topBlock, PEBBLE_COMPACT_DIRT);
            } else if ((double)noise >= 0.35F + randomGauss && random == 3) {
               context.setBlockState(topBlock, ROCKY_COMPACT_DIRT);
            } else if ((double)noise >= 0.35F + randomGauss && random == 4) {
               context.setBlockState(topBlock, ROCKIER_COMPACT_DIRT);
            } else if ((double)noise >= 0.35F + randomGauss && random == 5) {
               context.setBlockState(topBlock, ROCKIEST_COMPACT_DIRT);
            } else if ((double)noise >= 0.35F + randomGauss && random == 6) {
               context.setBlockState(topBlock, SPARSE_GRASS);
            }
         }
      }
   }
}
