package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.Area;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.TransformLayer;
import net.dries007.tfc.world.river.MidpointFractal;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public class MergeCoastalCliffsLayer implements TransformLayer {
   private final CoastalCliffs.Context cliff;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int GRAVEL_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticGravelShores();
   static final int COASTAL_CLIFFS = ((TFCLayersMixinInterface)staticBiomes).getStaticCoastalCliffs();

   public MergeCoastalCliffsLayer(CoastalCliffs.Context cliff) {
      this.cliff = cliff;
   }

   public int apply(AreaContext context, Area area, int x, int z) {
      int value = area.get(x, z);
      if (value == GRAVEL_SHORE) {
         float scale = 0.0078125F;
         float x0 = (float)x * 0.0078125F;
         float z0 = (float)z * 0.0078125F;

         for (MidpointFractal fractal : this.cliff.getFractalsByPartition(x, z)) {
            if (fractal.maybeIntersect(x0, z0, 0.35F) && fractal.intersect(x0, z0, 0.35F)) {
               return COASTAL_CLIFFS;
            }
         }
      }

      return value;
   }
}
