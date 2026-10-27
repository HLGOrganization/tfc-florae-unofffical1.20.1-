package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;

public enum ShoreLayerTFCF implements AdjacentTransformLayer {
   INSTANCE;

   private ShoreLayerTFCF() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      return !TFCLayers.isOcean(center) && TFCLayers.hasShore(center) && matcher.test(TFCLayers::isOcean) ? TFCLayers.shoreFor(center) : center;
   }
}
