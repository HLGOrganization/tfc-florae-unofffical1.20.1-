package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterMergeLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum MergeGuyotsLayer implements CenterMergeLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int GUYOTS_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyotsMarker();
   static final int GUYOTS = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyots();
   static final int PELAGIC_ZONE = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZone();
   static final int NEAR_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticNearShore();

   private MergeGuyotsLayer() {
   }

   public int apply(AreaContext context, int center, int layer) {
      return layer == GUYOTS_MARKER ? GUYOTS : center;
   }
}
