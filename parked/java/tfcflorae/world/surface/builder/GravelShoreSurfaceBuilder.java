package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;

public class GravelShoreSurfaceBuilder implements SurfaceBuilder {
   public static final SurfaceBuilderFactory INSTANCE = GravelShoreSurfaceBuilder::new;

   public GravelShoreSurfaceBuilder(long seed) {
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      NormalSurfaceBuilder surfaceBuilder = NormalSurfaceBuilder.INSTANCE;
      surfaceBuilder.buildSurface(context, startY, endY, SurfaceStates.GRAVEL, SurfaceStates.GRAVEL, SurfaceStates.SHORE_SANDSTONE);
   }
}
