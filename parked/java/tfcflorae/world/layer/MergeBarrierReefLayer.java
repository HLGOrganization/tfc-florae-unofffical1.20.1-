package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterMergeLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum MergeBarrierReefLayer implements CenterMergeLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int BARRIER_REEF_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReefMarker();
   static final int BARRIER_REEF = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReef();

   private MergeBarrierReefLayer() {
   }

   public int apply(AreaContext context, int center, int layer) {
      return layer == BARRIER_REEF_MARKER ? BARRIER_REEF : center;
   }
}
