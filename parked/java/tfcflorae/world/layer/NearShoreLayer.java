package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum NearShoreLayer implements AdjacentTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int GRAVEL_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticGravelShores();
   static final int NEAR_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticNearShore();
   static final int MANGROVES = ((TFCLayersMixinInterface)staticBiomes).getStaticMangroves();

   private NearShoreLayer() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      return TFCLayers.isOcean(center) && matcher.test(i -> i == TFCLayers.SHORE || i == GRAVEL_SHORE || i == MANGROVES) ? NEAR_SHORE : center;
   }
}
