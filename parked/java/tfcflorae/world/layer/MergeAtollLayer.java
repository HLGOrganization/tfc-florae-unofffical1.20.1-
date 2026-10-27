package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterMergeLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum MergeAtollLayer implements CenterMergeLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int LAGOON_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoonMarker();
   static final int LAGOON = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoon();

   private MergeAtollLayer() {
   }

   public int apply(AreaContext context, int center, int layer) {
      return layer == LAGOON_MARKER ? LAGOON : center;
   }
}
