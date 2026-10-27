package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum AddAtollLayer implements CenterTransformLayer {
   SMALL(40),
   LARGE(160);

   private final int chance;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int LAGOON_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoonMarker();

   private AddAtollLayer(int chance) {
      this.chance = chance;
   }

   public int apply(AreaContext context, int value) {
      return (value == TFCLayers.DEEP_OCEAN || value == TFCLayers.OCEAN || value == TFCLayers.OCEAN_REEF) && context.random().nextInt(this.chance) == 0
         ? LAGOON_MARKER
         : value;
   }
}
