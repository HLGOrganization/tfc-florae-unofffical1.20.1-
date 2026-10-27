package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum AddGuyotsLayer implements CenterTransformLayer {
   SMALL(60),
   LARGE(200);

   private final int chance;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int GUYOTS_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyotsMarker();
   static final int PELAGIC_ZONE = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZone();

   private AddGuyotsLayer(int chance) {
      this.chance = chance;
   }

   public int apply(AreaContext context, int value) {
      return (value == TFCLayers.DEEP_OCEAN || value == TFCLayers.DEEP_OCEAN_TRENCH || value == PELAGIC_ZONE) && context.random().nextInt(this.chance) == 0
         ? GUYOTS_MARKER
         : value;
   }
}
