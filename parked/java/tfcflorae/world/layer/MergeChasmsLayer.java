package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.Area;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.TransformLayer;
import net.dries007.tfc.world.river.MidpointFractal;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public class MergeChasmsLayer implements TransformLayer {
   private final Chasm.Context chasm;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int RIVERBANK = ((TFCLayersMixinInterface)staticBiomes).getStaticRiverbank();
   static final int CHASMS = ((TFCLayersMixinInterface)staticBiomes).getStaticChasms();
   static final int ATOLL = ((TFCLayersMixinInterface)staticBiomes).getStaticAtoll();
   static final int NEAR_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticNearShore();
   static final int SHORE_DUNES = ((TFCLayersMixinInterface)staticBiomes).getStaticShoreDunes();

   public MergeChasmsLayer(Chasm.Context chasm) {
      this.chasm = chasm;
   }

   public int apply(AreaContext context, Area area, int x, int z) {
      int value = area.get(x, z);
      if (!this.currentBiome(value)) {
         float scale = 0.0078125F;
         float x0 = (float)x * 0.0078125F;
         float z0 = (float)z * 0.0078125F;

         for (MidpointFractal fractal : this.chasm.getFractalsByPartition(x, z)) {
            if (fractal.maybeIntersect(x0, z0, 0.023F) && fractal.intersect(x0, z0, 0.023F)) {
               return CHASMS;
            }
         }
      }

      return value;
   }

   public boolean currentBiome(int value) {
      return TFCLayers.isOcean(value)
         || TFCLayers.isLake(value)
         || TFCLayers.isRiver(value)
         || TFCLayers.isLow(value)
         || value == ATOLL
         || value == NEAR_SHORE
         || value == TFCLayers.SHORE
         || value == SHORE_DUNES
         || value == TFCLayers.NULL_MARKER
         || value == TFCLayers.LAKE_MARKER;
   }
}
