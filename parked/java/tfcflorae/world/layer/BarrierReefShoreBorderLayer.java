package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum BarrierReefShoreBorderLayer implements AdjacentTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int BARRIER_REEF_SHORE_MARKER = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReefShoreMarker();
   static final int BARRIER_REEF = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReef();
   static final int LAGOON = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoon();
   static final int ATOLL = ((TFCLayersMixinInterface)staticBiomes).getStaticAtoll();

   private BarrierReefShoreBorderLayer() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      return (center == BARRIER_REEF || center == LAGOON) && matcher.test(i -> i != center) ? BARRIER_REEF_SHORE_MARKER : center;
   }
}
