package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum CoastalCliffsShoreLayer implements AdjacentTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int SHORE_DUNES = ((TFCLayersMixinInterface)staticBiomes).getStaticShoreDunes();
   static final int COASTAL_CLIFFS = ((TFCLayersMixinInterface)staticBiomes).getStaticCoastalCliffs();
   static final int GRAVEL_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticGravelShores();

   private CoastalCliffsShoreLayer() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      return center == COASTAL_CLIFFS && matcher.test(TFCLayers::isOcean) ? TFCLayers.shoreFor(center) : center;
   }
}
