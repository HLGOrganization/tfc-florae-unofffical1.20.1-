package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterMergeLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum MergeLagoonLayer implements CenterMergeLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int BARRIER_REEF_SHORE_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReefOceanMarker();
   static final int BARRIER_REEF_OCEAN_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReefShoreMarker();
   static final int BARRIER_REEF_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReefMarker();
   static final int BARRIER_REEF = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReef();
   static final int LAGOON_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoonMarker();
   static final int LAGOON = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoon();
   static final int ATOLL_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticAtollMarker();
   static final int ATOLL = ((TFCLayersMixinInterface)staticBiomes).getStaticAtoll();

   private MergeLagoonLayer() {
   }

   public int apply(AreaContext context, int center, int layer) {
      if (layer == BARRIER_REEF_SHORE_MARKER) {
         return TFCLayers.SHORE;
      } else if (layer == ATOLL_MARKER) {
         return ATOLL;
      } else if (layer == BARRIER_REEF_OCEAN_MARKER) {
         return LAGOON;
      } else if (layer == LAGOON_MARKER) {
         return LAGOON;
      } else {
         return layer == BARRIER_REEF_MARKER ? BARRIER_REEF : center;
      }
   }
}
