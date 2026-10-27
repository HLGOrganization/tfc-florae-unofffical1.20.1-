package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterMergeLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum MergeBarrierShoreLayer implements CenterMergeLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int BARRIER_REEF_SHORE_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReefShoreMarker();

   private MergeBarrierShoreLayer() {
   }

   public int apply(AreaContext context, int center, int layer) {
      return layer == BARRIER_REEF_SHORE_MARKER ? TFCLayers.SHORE : center;
   }
}
