package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum AddPuyMountainsLayer implements CenterTransformLayer {
   SMALL(110),
   LARGE(260);

   private final int chance;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int PUY_MOUNTAINS_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticPuyMountainsMarker();

   private AddPuyMountainsLayer(int chance) {
      this.chance = chance;
   }

   public int apply(AreaContext context, int value) {
      return value == TFCLayers.INLAND_MARKER && context.random().nextInt(this.chance) == 0 ? PUY_MOUNTAINS_MARKER : value;
   }
}
