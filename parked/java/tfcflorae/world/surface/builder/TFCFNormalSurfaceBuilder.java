package tfcflorae.world.surface.builder;

import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory.Invariant;
import net.minecraft.world.level.block.state.BlockState;

public enum TFCFNormalSurfaceBuilder implements Invariant {
   INSTANCE(-1),
   ROCKY(-3);

   private final int subsurfaceMinDepth;

   private TFCFNormalSurfaceBuilder(int subsurfaceMinDepth) {
      this.subsurfaceMinDepth = subsurfaceMinDepth;
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.buildSurface(context, startY, endY, SurfaceStates.GRASS, SurfaceStates.DIRT, SurfaceStates.SANDSTONE_OR_GRAVEL);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY, SurfaceState topState, SurfaceState midState, SurfaceState underState) {
      this.buildSurface(context, startY, endY, topState, midState, underState, SurfaceStates.SAND_OR_GRAVEL, SurfaceStates.SANDSTONE_OR_GRAVEL);
   }

   public void buildSurface(
      SurfaceBuilderContext context,
      int startY,
      int endY,
      SurfaceState topState,
      SurfaceState midState,
      SurfaceState underState,
      SurfaceState underWaterState,
      SurfaceState thinUnderWaterState
   ) {
      int surfaceDepth = -1;
      int surfaceY = 0;
      boolean underwaterLayer = false;
      boolean firstLayer = false;
      SurfaceState surfaceState = SurfaceStates.RAW;

      for (int y = startY; y >= endY; y--) {
         BlockState stateAt = context.getBlockState(y);
         if (stateAt.isAir()) {
            surfaceDepth = -1;
         } else if (context.isDefaultBlock(stateAt)) {
            if (surfaceDepth == -1) {
               surfaceY = y;
               firstLayer = true;
               if (y < context.getSeaLevel() - 1) {
                  surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(y, -1, 0);
                  if (surfaceDepth < -1) {
                     surfaceDepth = 0;
                  } else if (surfaceDepth == -1) {
                     surfaceDepth = 0;
                     context.setBlockState(y, thinUnderWaterState);
                  } else {
                     context.setBlockState(y, underWaterState);
                  }

                  surfaceState = underWaterState;
                  underwaterLayer = true;
               } else {
                  surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(y, this.subsurfaceMinDepth, 0);
                  if (surfaceDepth < -1) {
                     surfaceDepth = 0;
                  } else if (surfaceDepth == -1) {
                     surfaceDepth = 0;
                     context.setBlockState(y, underState);
                  } else {
                     context.setBlockState(y, topState);
                  }

                  surfaceState = midState;
                  underwaterLayer = false;
               }
            } else if (surfaceDepth > 0) {
               surfaceDepth--;
               context.setBlockState(y, surfaceState);
               if (surfaceDepth == 0 && firstLayer) {
                  firstLayer = false;
                  if (underwaterLayer) {
                     surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 0, 0);
                     surfaceState = thinUnderWaterState;
                  } else {
                     surfaceDepth = context.calculateAltitudeSlopeSurfaceDepth(surfaceY, 0, 0);
                     surfaceState = underState;
                  }
               }
            }
         }
      }
   }
}
