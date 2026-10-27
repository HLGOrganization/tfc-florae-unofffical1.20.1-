package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

public class AtollSurfaceBuilder implements SurfaceBuilder {
   private final SurfaceBuilder parent;
   private final Noise2D variantNoise;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new AtollSurfaceBuilder(parent.apply(seed), seed);
   }

   public AtollSurfaceBuilder(SurfaceBuilder parent, long seed) {
      this.parent = parent;
      this.variantNoise = new OpenSimplex2D(seed).octaves(2).spread(0.003F).abs();
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.parent.buildSurface(context, startY, endY);
      if (startY <= 64 + (int)Math.abs(context.random().nextGaussian())) {
         this.buildAtollSurface(context, startY, endY);
      }
   }

   private void buildAtollSurface(SurfaceBuilderContext context, int startY, int endY) {
      float variantNoiseValue = (float)(this.variantNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()));
      if (variantNoiseValue > 0.6F) {
         NormalSurfaceBuilder.INSTANCE
            .buildSurface(context, startY, endY, SurfaceStates.RARE_SHORE_SAND, SurfaceStates.RARE_SHORE_SAND, SurfaceStates.RARE_SHORE_SANDSTONE);
      } else {
         SurfaceState top = context.rainfall() > 400.0F && variantNoiseValue > 0.4F ? SurfaceStates.SHORE_MUD : SurfaceStates.SHORE_SAND;
         NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, top, top, SurfaceStates.SHORE_SANDSTONE);
      }
   }
}
