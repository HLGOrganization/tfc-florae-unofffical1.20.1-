package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

public class LowMountainSurfaceBuilder implements SurfaceBuilder {
   public static final SurfaceBuilderFactory INSTANCE = LowMountainSurfaceBuilder::new;
   private final Noise2D surfaceMaterialNoise;
   private final Noise2D heightNoise;

   public LowMountainSurfaceBuilder(long seed) {
      this.surfaceMaterialNoise = new OpenSimplex2D(seed).octaves(2).spread(0.02F);
      this.heightNoise = new OpenSimplex2D(seed + 71829341L).octaves(2).spread(0.1F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      NormalSurfaceBuilder surfaceBuilder = NormalSurfaceBuilder.INSTANCE;
      double heightNoise = (double)(this.heightNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 4.0F + (float)startY);
      if (heightNoise >= 65.0) {
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
      } else {
         surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.GRASS, SurfaceStates.DIRT, SurfaceStates.SANDSTONE_OR_GRAVEL);
      }
   }
}
