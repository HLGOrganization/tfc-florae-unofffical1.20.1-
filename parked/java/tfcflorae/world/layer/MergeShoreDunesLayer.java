package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.Area;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.TransformLayer;
import net.dries007.tfc.world.river.MidpointFractal;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public class MergeShoreDunesLayer implements TransformLayer {
   private final ShoreDunes.Context cliff;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int GRAVEL_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticGravelShores();
   static final int SHORE_DUNES = ((TFCLayersMixinInterface)staticBiomes).getStaticShoreDunes();

   public MergeShoreDunesLayer(ShoreDunes.Context cliff) {
      this.cliff = cliff;
   }

   public int apply(AreaContext context, Area area, int x, int z) {
      int value = area.get(x, z);
      if (value == TFCLayers.SHORE) {
         float scale = 0.0078125F;
         float x0 = (float)x * 0.0078125F;
         float z0 = (float)z * 0.0078125F;

         for (MidpointFractal fractal : this.cliff.getFractalsByPartition(x, z)) {
            if (fractal.maybeIntersect(x0, z0, 0.4F) && fractal.intersect(x0, z0, 0.4F)) {
               return SHORE_DUNES;
            }
         }
      }

      return value;
   }
}
