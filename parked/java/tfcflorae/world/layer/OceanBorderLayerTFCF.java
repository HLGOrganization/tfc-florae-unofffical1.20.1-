package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum OceanBorderLayerTFCF implements AdjacentTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int PELAGIC_ZONE = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZone();
   static final int SEAMOUNTS = ((TFCLayersMixinInterface)staticBiomes).getStaticSeamounts();
   static final int GUYOTS = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyots();
   static final int NEAR_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticNearShore();
   static final int ATOLL = ((TFCLayersMixinInterface)staticBiomes).getStaticAtoll();
   static final int LAGOON = ((TFCLayersMixinInterface)staticBiomes).getStaticLagoon();
   static final int BARRIER_REEF = ((TFCLayersMixinInterface)staticBiomes).getStaticBarrierReef();
   static final int SHORE_DUNES = ((TFCLayersMixinInterface)staticBiomes).getStaticShoreDunes();

   private OceanBorderLayerTFCF() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      if (center == TFCLayers.DEEP_OCEAN) {
         if (matcher.test(i -> !TFCLayers.isOceanOrMarker(i)) || this.isShallowWaters(matcher)) {
            return TFCLayers.OCEAN;
         }
      } else if (center == PELAGIC_ZONE || center == SEAMOUNTS || center == GUYOTS) {
         if (!this.isShallowWaters(matcher) && matcher.test(TFCLayers::isOceanOrMarker)) {
            return TFCLayers.DEEP_OCEAN_TRENCH;
         }

         if (!matcher.test(TFCLayers::isOceanOrMarker) || this.isShallowWaters(matcher)) {
            return TFCLayers.OCEAN;
         }
      }

      return center;
   }

   public boolean isShallowWaters(Predicate<IntPredicate> matcher) {
      return matcher.test(i -> i == TFCLayers.OCEAN)
         || matcher.test(i -> i == TFCLayers.OCEAN_REEF)
         || matcher.test(i -> i == NEAR_SHORE)
         || matcher.test(i -> i == ATOLL)
         || matcher.test(i -> i == LAGOON)
         || matcher.test(i -> i == BARRIER_REEF);
   }
}
