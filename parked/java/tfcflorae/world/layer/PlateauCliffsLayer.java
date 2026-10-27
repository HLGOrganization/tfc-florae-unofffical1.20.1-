package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum PlateauCliffsLayer implements AdjacentTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int MESA_PLATEAU = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaPlateau();
   static final int PLATEAU_CLIFFS = ((TFCLayersMixinInterface)staticBiomes).getStaticPlateauCliffs();
   static final int MISTY_PEAKS = ((TFCLayersMixinInterface)staticBiomes).getStaticMistyPeaks();
   static final int FRACTURED_MISTY_PEAKS = ((TFCLayersMixinInterface)staticBiomes).getStaticFracturedMistyPeaks();
   static final int RIVERBANK = ((TFCLayersMixinInterface)staticBiomes).getStaticRiverbank();
   static final int RIVER_EDGE = ((TFCLayersMixinInterface)staticBiomes).getStaticRiverEdge();
   static final int LAKE_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticLakeShore();

   private PlateauCliffsLayer() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      return (center == TFCLayers.PLATEAU || center == MESA_PLATEAU || center == MISTY_PEAKS || center == FRACTURED_MISTY_PEAKS)
            && matcher.test(i -> TFCLayers.isLow(i) || i == RIVERBANK || i == RIVER_EDGE || i == LAKE_SHORE)
         ? PLATEAU_CLIFFS
         : center;
   }
}
