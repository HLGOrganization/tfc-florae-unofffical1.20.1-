package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterMergeLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum MergePelagicZonesLayer implements CenterMergeLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int PELAGIC_ZONE_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZoneMarker();
   static final int PELAGIC_ZONE = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZone();
   static final int NEAR_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticNearShore();

   private MergePelagicZonesLayer() {
   }

   public int apply(AreaContext context, int center, int layer) {
      return layer == PELAGIC_ZONE_MARKER ? PELAGIC_ZONE : center;
   }
}
