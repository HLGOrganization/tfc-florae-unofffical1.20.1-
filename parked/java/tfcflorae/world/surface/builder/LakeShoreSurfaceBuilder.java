package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

public class LakeShoreSurfaceBuilder implements SurfaceBuilder {
   public static final SurfaceBuilderFactory INSTANCE = LakeShoreSurfaceBuilder::new;
   private final Noise2D surfaceMaterialNoise;

   public LakeShoreSurfaceBuilder(long seed) {
      this.surfaceMaterialNoise = new OpenSimplex2D(seed).octaves(2).spread(0.04F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      float noise = this.surfaceMaterialNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 0.9F
         + context.random().nextFloat() * 0.1F;
      NormalSurfaceBuilder surfaceBuilder = NormalSurfaceBuilder.INSTANCE;
      surfaceBuilder.buildSurface(
         context, startY, endY, noise < 0.0F ? SurfaceStates.GRAVEL : SurfaceStates.MUD, SurfaceStates.GRAVEL, SurfaceStates.SHORE_SANDSTONE
      );
   }
}
