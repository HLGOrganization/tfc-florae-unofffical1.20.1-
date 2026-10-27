package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.core.BlockPos;
import tfcflorae.common.blocks.soil.TFCFRockSand;
import tfcflorae.world.surface.TFCFSoilSurfaceState;

public class DuneShoreSurfaceBuilder implements SurfaceBuilder {
   private final SurfaceBuilder parent;
   private final Noise2D surfaceMaterialNoise;
   private final Noise2D variantNoise;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new DuneShoreSurfaceBuilder(parent.apply(seed), seed);
   }

   public DuneShoreSurfaceBuilder(SurfaceBuilder parent, long seed) {
      this.parent = parent;
      this.surfaceMaterialNoise = new OpenSimplex2D(seed).octaves(2).spread(0.04F);
      this.variantNoise = new OpenSimplex2D(seed).octaves(2).spread(0.003F).abs();
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.parent.buildSurface(context, startY, endY);
      float noiseRainfall = context.rainfall() + 10.0F * (float)context.random().nextGaussian();
      if (noiseRainfall >= 50.0F) {
         this.buildSandySurface(context, startY, endY);
      }
   }

   private void buildSandySurface(SurfaceBuilderContext context, int startY, int endY) {
      int surface = startY - 1;
      BlockPos pos = new BlockPos(context.pos().getX(), surface, context.pos().getZ());
      float variantNoiseValue = (float)(this.variantNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()));
      SurfaceState PEBBLE = TFCFSoilSurfaceState.rockSand(TFCFRockSand.PEBBLE);
      SurfaceState ROCKY = TFCFSoilSurfaceState.rockSand(TFCFRockSand.ROCKY);
      SurfaceState ROCKIER = TFCFSoilSurfaceState.rockSand(TFCFRockSand.ROCKIER);
      SurfaceState ROCKIEST = TFCFSoilSurfaceState.rockSand(TFCFRockSand.ROCKIEST);
      SurfaceState SPARSE_GRASS = TFCFSoilSurfaceState.rockSandSparseGrass();
      SurfaceState DENSE_GRASS = TFCFSoilSurfaceState.rockSandDenseGrass();
      SurfaceState SHORE_SAND = SurfaceStates.SHORE_SAND;
      if (variantNoiseValue > 0.6F) {
         SHORE_SAND = SurfaceStates.RARE_SHORE_SAND;
         SPARSE_GRASS = TFCFSoilSurfaceState.rockRareSandSparseGrass();
         DENSE_GRASS = TFCFSoilSurfaceState.rockRareSandDenseGrass();
      }

      double randomGauss = Math.abs(context.random().nextGaussian()) * 0.1F;
      double gauss = context.random().nextGaussian();
      float noise = this.surfaceMaterialNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 0.9F
         + context.random().nextFloat() * 0.1F;
      if ((double)pos.getY() > (double)(context.getSeaLevel() + 3) + gauss) {
         int random = context.random().nextInt(7);
         if ((double)noise >= -0.45F + randomGauss && (double)noise < -0.2F + randomGauss) {
            context.setBlockState(surface, DENSE_GRASS);
         } else if ((double)noise >= -0.2F + randomGauss && (double)noise < -0.05F + randomGauss) {
            context.setBlockState(surface, DENSE_GRASS);
         } else if ((double)noise >= -0.05F + randomGauss && random <= 0) {
            context.setBlockState(surface, SPARSE_GRASS);
         } else if ((double)noise >= -0.05F + randomGauss && random == 1) {
            context.setBlockState(surface, SHORE_SAND);
         } else if ((double)noise >= -0.05F + randomGauss && random == 2) {
            context.setBlockState(surface, PEBBLE);
         } else if ((double)noise >= -0.05F + randomGauss && random == 3) {
            context.setBlockState(surface, ROCKY);
         } else if ((double)noise >= -0.05F + randomGauss && random == 4) {
            context.setBlockState(surface, ROCKIER);
         } else if ((double)noise >= -0.05F + randomGauss && random == 5) {
            context.setBlockState(surface, ROCKIEST);
         } else if ((double)noise >= -0.05F + randomGauss && random == 6) {
            context.setBlockState(surface, SPARSE_GRASS);
         }
      }
   }
}
