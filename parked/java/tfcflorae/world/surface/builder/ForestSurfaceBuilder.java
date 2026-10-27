package tfcflorae.world.surface.builder;

import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SoilSurfaceState;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.core.BlockPos;
import tfcflorae.common.blocks.soil.TFCFRockSoil;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.world.surface.TFCFSoilSurfaceState;

public class ForestSurfaceBuilder implements SurfaceBuilder {
   private final SurfaceBuilder parent;
   private final Noise2D surfaceMaterialNoise;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new ForestSurfaceBuilder(parent.apply(seed), seed);
   }

   public ForestSurfaceBuilder(SurfaceBuilder parent, long seed) {
      this.parent = parent;
      this.surfaceMaterialNoise = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.04F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.parent.buildSurface(context, startY, endY);
      float noiseRainfall = context.rainfall() + 10.0F * (float)context.random().nextGaussian();
      if (noiseRainfall >= 80.0F) {
         this.buildForestSurface(context, startY, endY);
      }
   }

   private void buildForestSurface(SurfaceBuilderContext context, int startY, int endY) {
      int topBlock = startY - 1;
      ChunkData data = context.getChunkData();
      BlockPos pos = new BlockPos(context.pos().getX(), topBlock, context.pos().getZ());
      ForestType forestType = context.getChunkData().getForestType();
      float forestDensity = data.getForestDensity();
      SurfaceState PEBBLE_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.PEBBLE_COMPACT_DIRT);
      SurfaceState ROCKY_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.ROCKY_COMPACT_DIRT);
      SurfaceState ROCKIER_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.ROCKIER_COMPACT_DIRT);
      SurfaceState ROCKIEST_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.ROCKIEST_COMPACT_DIRT);
      SurfaceState GRASS = SoilSurfaceState.buildType(SoilBlockType.GRASS);
      SurfaceState PODZOL = TFCFSoilSurfaceState.buildType(TFCFSoil.PODZOL);
      SurfaceState ROOTED_DIRT = SoilSurfaceState.buildType(SoilBlockType.ROOTED_DIRT);
      SurfaceState SPARSE_GRASS = TFCFSoilSurfaceState.buildType(TFCFSoil.SPARSE_GRASS);
      SurfaceState DENSE_GRASS = TFCFSoilSurfaceState.buildType(TFCFSoil.DENSE_GRASS);
      SurfaceState COARSE_DIRT = TFCFSoilSurfaceState.buildType(TFCFSoil.COARSE_DIRT);
      SurfaceState COMPACT_DIRT = TFCFSoilSurfaceState.buildType(TFCFSoil.COMPACT_DIRT);
      double randomGauss = Math.abs(context.random().nextGaussian()) * 0.1F;
      float noise = this.surfaceMaterialNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 0.9F
         + context.random().nextFloat() * 0.1F;
      if (pos.getY() > context.getSeaLevel()
         && forestDensity >= 0.3F
         && topBlock <= 110
         && (forestType == ForestType.NORMAL || forestType == ForestType.OLD_GROWTH)) {
         int random = context.random().nextInt(7);
         if ((double)noise >= -0.22F + randomGauss && (double)noise < -0.13F + randomGauss) {
            context.setBlockState(topBlock, DENSE_GRASS);
         } else if ((double)noise >= -0.13F + randomGauss && (double)noise < 0.0 + randomGauss) {
            if (random > 3) {
               context.setBlockState(topBlock, SPARSE_GRASS);
            } else {
               context.setBlockState(topBlock, COMPACT_DIRT);
            }
         } else if ((double)noise >= 0.0 + randomGauss && (double)noise < 0.1F + randomGauss) {
            if (random > 4) {
               context.setBlockState(topBlock, DENSE_GRASS);
            } else {
               context.setBlockState(topBlock, PEBBLE_COMPACT_DIRT);
            }
         } else if ((double)noise >= 0.1F + randomGauss && (double)noise < 0.2F + randomGauss) {
            if (random > 4) {
               context.setBlockState(topBlock, DENSE_GRASS);
            } else {
               context.setBlockState(topBlock, ROCKY_COMPACT_DIRT);
            }
         } else if ((double)noise >= 0.2F + randomGauss && (double)noise < 0.35F + randomGauss) {
            if (random > 4) {
               context.setBlockState(topBlock, ROCKY_COMPACT_DIRT);
            } else {
               context.setBlockState(topBlock, ROCKIER_COMPACT_DIRT);
            }
         } else if ((double)noise >= 0.35F + randomGauss && (double)noise < 0.55F + randomGauss) {
            if (random > 4) {
               context.setBlockState(topBlock, DENSE_GRASS);
            } else {
               context.setBlockState(topBlock, ROOTED_DIRT);
            }
         } else if ((double)noise >= 0.55F + randomGauss && (double)noise < 0.75 + randomGauss) {
            context.setBlockState(topBlock, PODZOL);
         } else if ((double)noise >= 0.75 + randomGauss) {
            if (random > 3) {
               context.setBlockState(topBlock, PODZOL);
            } else {
               context.setBlockState(topBlock, ROOTED_DIRT);
            }
         }
      }
   }
}
