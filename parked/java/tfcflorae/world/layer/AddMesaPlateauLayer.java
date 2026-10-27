package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum AddMesaPlateauLayer implements CenterTransformLayer {
   SMALL(70),
   LARGE(200);

   private final int chance;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int MESA_PLATEAU_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaPlateauMarker();

   private AddMesaPlateauLayer(int chance) {
      this.chance = chance;
   }

   public int apply(AreaContext context, int value) {
      return value == TFCLayers.INLAND_MARKER && context.random().nextInt(this.chance) == 0 ? MESA_PLATEAU_MARKER : value;
   }
}
