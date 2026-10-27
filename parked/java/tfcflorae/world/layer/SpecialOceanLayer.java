package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum SpecialOceanLayer implements CenterTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int PELAGIC_ZONES_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZoneMarker();
   static final int PELAGIC_ZONE = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZone();
   static final int GUYOTS_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyotsMarker();
   static final int GUYOTS = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyots();
   static final int SEAMOUNTS_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticSeamountsMarker();
   static final int SEAMOUNTS = ((TFCLayersMixinInterface)staticBiomes).getStaticSeamounts();
   static final int BARRIER_REEF_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReefMarker();
   static final int BARRIER_REEF = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReef();
   static final int LAGOON = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoon();
   static final int LAGOON_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoonMarker();
   static final int ATOLL = ((TFCLayersMixinInterface)staticBiomes).getStaticAtoll();
   static final int ATOLL_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticAtollMarker();
   static final int NEAR_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticNearShore();

   private SpecialOceanLayer() {
   }

   public int apply(AreaContext context, int value) {
      if (value == TFCLayers.OCEAN_OCEAN_CONVERGING_MARKER) {
         int r = context.random().nextInt(20);
         if (r <= 1) {
            return TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS;
         } else {
            return r == 2 ? TFCLayers.OCEAN_REEF : TFCLayers.OCEAN;
         }
      } else if (value == TFCLayers.OCEAN_OCEAN_DIVERGING_MARKER) {
         int r = context.random().nextInt(30);
         return r == 0 ? TFCLayers.PLAINS : TFCLayers.OCEAN;
      } else {
         if (value == TFCLayers.DEEP_OCEAN) {
            int r = context.random().nextInt(250);
            if (r == 0) {
               return TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS;
            }

            if (r > 0 && r <= 21) {
               return PELAGIC_ZONE;
            }
         } else if (value == PELAGIC_ZONE) {
            int rx = context.random().nextInt(250);
            if (rx >= 0 && rx <= 10) {
               return GUYOTS;
            }

            if (rx >= 11 && rx <= 21) {
               return SEAMOUNTS;
            }
         } else if (value == TFCLayers.OCEAN) {
            return TFCLayers.OCEAN_REEF_MARKER;
         }

         return value;
      }
   }
}
