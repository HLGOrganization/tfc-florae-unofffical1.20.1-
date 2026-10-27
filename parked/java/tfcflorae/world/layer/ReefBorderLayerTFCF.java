package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum ReefBorderLayerTFCF implements AdjacentTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int ATOLL = ((TFCLayersMixinInterface)staticBiomes).getStaticAtoll();
   static final int LAGOON = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoon();
   static final int BARRIER_REEF = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReef();

   private ReefBorderLayerTFCF() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      if (center == TFCLayers.OCEAN_REEF_MARKER) {
         return matcher.test(i -> !TFCLayers.isOceanOrMarker(i) || i != ATOLL || i != LAGOON || i != BARRIER_REEF) ? TFCLayers.OCEAN : TFCLayers.OCEAN_REEF;
      } else {
         return TFCLayers.isOceanOrMarker(center) && matcher.test(i -> i == TFCLayers.OCEAN_REEF_MARKER) ? TFCLayers.OCEAN_REEF : center;
      }
   }
}
