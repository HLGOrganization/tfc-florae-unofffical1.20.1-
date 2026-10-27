package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

public class MangroveSurfaceBuilder implements SurfaceBuilder {
   private final SurfaceBuilder parent;
   private final Noise2D surfaceMaterialNoise;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new MangroveSurfaceBuilder(parent.apply(seed), seed);
   }

   public MangroveSurfaceBuilder(SurfaceBuilder parent, long seed) {
      this.parent = parent;
      this.surfaceMaterialNoise = new OpenSimplex2D(seed).octaves(2).spread(0.04F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.parent.buildSurface(context, startY, endY);
      this.buildMudSurface(context, startY, endY);
   }

   private void buildMudSurface(SurfaceBuilderContext context, int startY, int endY) {
      float noise = this.surfaceMaterialNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 0.9F
         + context.random().nextFloat() * 0.1F;
      NormalSurfaceBuilder.INSTANCE
         .buildSurface(context, startY, endY, noise < 0.0F ? SurfaceStates.GRASS : SurfaceStates.MUD, SurfaceStates.GRAVEL, SurfaceStates.SANDSTONE_OR_GRAVEL);
   }
}
