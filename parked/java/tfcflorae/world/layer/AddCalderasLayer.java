package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum AddCalderasLayer implements CenterTransformLayer {
   SMALL(16),
   LARGE(80);

   private final int chance;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int CALDERA_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticCalderaMarker();
   static final int SHRUBLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticShrublands();
   static final int VINICUNCA_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticVinicuncaMountains();

   private AddCalderasLayer(int chance) {
      this.chance = chance;
   }

   public int apply(AreaContext context, int value) {
      return value == TFCLayers.INLAND_MARKER
            && TFCLayers.isMountains(value)
            && value != SHRUBLANDS
            && value != VINICUNCA_MOUNTAINS
            && context.random().nextInt(this.chance) == 0
         ? CALDERA_MARKER
         : value;
   }
}
