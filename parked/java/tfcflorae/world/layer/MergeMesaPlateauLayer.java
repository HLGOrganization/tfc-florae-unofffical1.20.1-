package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterMergeLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum MergeMesaPlateauLayer implements CenterMergeLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int MESA_PLATEAU_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaPlateauMarker();
   static final int MESA_PLATEAU = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaPlateau();

   private MergeMesaPlateauLayer() {
   }

   public int apply(AreaContext context, int center, int caldera) {
      return caldera == MESA_PLATEAU_MARKER && TFCLayers.isLow(center) ? MESA_PLATEAU : center;
   }
}
