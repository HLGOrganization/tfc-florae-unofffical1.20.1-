package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

public class RainbowMountainsSurfaceBuilder implements SurfaceBuilder {
   private final SurfaceBuilder parent;
   private final Noise2D surfaceMaterialNoise;
   private final Noise2D heightNoise;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new RainbowMountainsSurfaceBuilder(parent.apply(seed), seed);
   }

   public RainbowMountainsSurfaceBuilder(SurfaceBuilder parent, long seed) {
      this.parent = parent;
      this.surfaceMaterialNoise = new OpenSimplex2D(seed).octaves(2).spread(1.0F).abs();
      this.heightNoise = new OpenSimplex2D(seed + 71829341L).octaves(2).spread(0.1F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.parent.buildSurface(context, startY, endY);
      this.buildRainbowSurface(context, startY, endY);
   }

   private void buildRainbowSurface(SurfaceBuilderContext context, int startY, int endY) {
      NormalSurfaceBuilder surfaceBuilder = NormalSurfaceBuilder.INSTANCE;
      double gaussSpread = context.random().nextGaussian() * 2.0;
      double heightNoise = (double)(this.heightNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 4.0F + (float)startY);
      if (heightNoise > 118.0 + gaussSpread) {
         float surfaceMaterialValue = this.surfaceMaterialNoise.noise((float)context.pos().getX(), (float)context.pos().getZ())
            + 0.1F * context.random().nextFloat()
            - 0.05F;
         if (surfaceMaterialValue > 0.3F) {
            surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.COBBLE, SurfaceStates.COBBLE, SurfaceStates.RAW);
         } else if (surfaceMaterialValue < -0.3F) {
            surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.GRAVEL, SurfaceStates.GRAVEL, SurfaceStates.RAW);
         } else {
            surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.RAW, SurfaceStates.RAW, SurfaceStates.RAW);
         }
      }
   }
}
