package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

public class GravelSurfaceBuilder implements SurfaceBuilder {
   private final SurfaceBuilder parent;
   private final Noise2D surfaceMaterialNoise;
   private final Noise2D heightNoise;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new GravelSurfaceBuilder(parent.apply(seed), seed);
   }

   public GravelSurfaceBuilder(SurfaceBuilder parent, long seed) {
      this.parent = parent;
      this.surfaceMaterialNoise = new OpenSimplex2D(seed).octaves(2).spread(0.04F);
      this.heightNoise = new OpenSimplex2D(seed + 71829341L).octaves(2).spread(0.1F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.parent.buildSurface(context, startY, endY);
      this.buildSoilSurface(context, startY, endY);
   }

   private void buildSoilSurface(SurfaceBuilderContext context, int startY, int endY) {
      NormalSurfaceBuilder surfaceBuilder = NormalSurfaceBuilder.INSTANCE;
      double heightNoise = (double)(this.heightNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 4.0F + (float)startY);
      if (heightNoise <= 64.0 + context.random().nextGaussian() && heightNoise > 60.0 - context.random().nextGaussian()) {
         float surfaceMaterialValue = this.surfaceMaterialNoise.noise((float)context.pos().getX(), (float)context.pos().getZ())
            + 0.1F * context.random().nextFloat()
            - 0.05F;
         if (surfaceMaterialValue > 0.3F) {
            surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.GRAVEL, SurfaceStates.GRAVEL, SurfaceStates.RAW);
         }
      }
   }
}
