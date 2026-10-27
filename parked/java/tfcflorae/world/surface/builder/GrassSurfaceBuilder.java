package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.core.BlockPos;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.world.surface.TFCFSoilSurfaceState;

public class GrassSurfaceBuilder implements SurfaceBuilder {
   private final SurfaceBuilder parent;
   private final Noise2D surfaceMaterialNoise;
   private final Noise2D heightNoise;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new GrassSurfaceBuilder(parent.apply(seed), seed);
   }

   public GrassSurfaceBuilder(SurfaceBuilder parent, long seed) {
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
      BlockPos pos = new BlockPos(context.pos().getX(), topBlock, context.pos().getZ());
      float rainfall = context.getChunkData().getRainfall(pos);
      SurfaceState SPARSE_GRASS = TFCFSoilSurfaceState.buildType(TFCFSoil.SPARSE_GRASS);
      SurfaceState DENSE_GRASS = TFCFSoilSurfaceState.buildType(TFCFSoil.DENSE_GRASS);
      double gauss = context.random().nextGaussian();
      double heightNoise = (double)(this.heightNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 4.0F + (float)startY);
      if (pos.getY() > context.getSeaLevel() && heightNoise <= 130.0 && (double)rainfall < 1.5 * gauss + 100.0) {
         if ((double)rainfall >= 1.5 * gauss + 65.0) {
            context.setBlockState(topBlock, DENSE_GRASS);
         } else {
            context.setBlockState(topBlock, SPARSE_GRASS);
         }
      }
   }
}
