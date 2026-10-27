package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterMergeLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum MergePuyMountainsLayer implements CenterMergeLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int PUY_MOUNTAINS_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticPuyMountainsMarker();
   static final int PUY_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticPuyMountains();

   private MergePuyMountainsLayer() {
   }

   public int apply(AreaContext context, int center, int caldera) {
      return caldera == PUY_MOUNTAINS_MARKER && TFCLayers.isLow(center) ? PUY_MOUNTAINS : center;
   }
}
