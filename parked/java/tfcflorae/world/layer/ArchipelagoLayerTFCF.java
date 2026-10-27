package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum ArchipelagoLayerTFCF implements CenterTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int PELAGIC_ZONE = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZone();
   static final int GUYOTS = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyots();
   static final int SEAMOUNTS = ((TFCLayersMixinInterface)staticBiomes).getStaticSeamounts();

   private ArchipelagoLayerTFCF() {
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
         if (value != TFCLayers.DEEP_OCEAN && value != PELAGIC_ZONE && value != GUYOTS && value != SEAMOUNTS) {
            if (value == TFCLayers.OCEAN) {
               return TFCLayers.OCEAN_REEF_MARKER;
            }
         } else {
            int r = context.random().nextInt(250);
            if (r == 0) {
               return TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS;
            }
         }

         return value;
      }
   }
}
