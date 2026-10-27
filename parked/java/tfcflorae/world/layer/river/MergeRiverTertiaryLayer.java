package tfcflorae.world.layer.river;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.Area;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.TransformLayer;
import net.dries007.tfc.world.river.MidpointFractal;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public class MergeRiverTertiaryLayer implements TransformLayer {
   private final WatershedTertiary.Context watersheds;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int RIVER_EDGE = ((TFCLayersMixinInterface)staticBiomes).getStaticRiverEdge();

   public MergeRiverTertiaryLayer(WatershedTertiary.Context watersheds) {
      this.watersheds = watersheds;
   }

   public int apply(AreaContext context, Area area, int x, int z) {
      int value = area.get(x, z);
      if ((TFCLayers.hasRiver(value) || value == RIVER_EDGE) && !TFCLayers.isOceanOrMarker(value)) {
         float scale = 0.0078125F;
         float x0 = (float)x * 0.0078125F;
         float z0 = (float)z * 0.0078125F;

         for (MidpointFractal fractal : this.watersheds.getFractalsByPartition(x, z)) {
            if (fractal.maybeIntersect(x0, z0, 0.005F) && fractal.intersect(x0, z0, 0.005F)) {
               return TFCLayers.riverFor(value);
            }
         }
      }

      return value;
   }
}
