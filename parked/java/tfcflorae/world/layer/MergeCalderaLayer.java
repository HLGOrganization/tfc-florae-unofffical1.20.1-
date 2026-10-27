package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterMergeLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum MergeCalderaLayer implements CenterMergeLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int CALDERA_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticCalderaMarker();
   static final int CALDERAS = ((TFCLayersMixinInterface)staticBiomes).getStaticCalderas();
   static final int SHRUBLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticShrublands();
   static final int VINICUNCA_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticVinicuncaMountains();

   private MergeCalderaLayer() {
   }

   public int apply(AreaContext context, int center, int caldera) {
      return caldera == CALDERA_MARKER && TFCLayers.isMountains(center) && center != SHRUBLANDS && center != VINICUNCA_MOUNTAINS ? CALDERAS : center;
   }
}
