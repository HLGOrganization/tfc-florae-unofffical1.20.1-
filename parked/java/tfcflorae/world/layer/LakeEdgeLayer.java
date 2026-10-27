package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum LakeEdgeLayer implements AdjacentTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int LAKE_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticLakeShore();

   private LakeEdgeLayer() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      return center != TFCLayers.LAKE && !TFCLayers.isRiver(center) && matcher.test(i -> i == TFCLayers.LAKE) ? LAKE_SHORE : center;
   }
}
