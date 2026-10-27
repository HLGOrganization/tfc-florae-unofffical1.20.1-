package tfcflorae.world.layer;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum EdgeBiomeLayer implements AdjacentTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int GRASSLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticGrasslands();
   static final int WETLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticWetlands();
   static final int MARSHES = ((TFCLayersMixinInterface)staticBiomes).getStaticMarshes();
   static final int SWAMPS = ((TFCLayersMixinInterface)staticBiomes).getStaticSwamps();
   static final int BRYCE_CANYONS = ((TFCLayersMixinInterface)staticBiomes).getStaticBryceCanyons();
   static final int MESA_HILLS = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaHills();
   static final int BADLANDS_PLATEAU = ((TFCLayersMixinInterface)staticBiomes).getStaticBadlandsPlateau();
   static final int ALPINE_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineMountains();
   static final int ALPINE_HIGHLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineHighlands();
   static final int ROLLING_HIGHLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticRollingHighlands();
   static final int CALDERAS = ((TFCLayersMixinInterface)staticBiomes).getStaticCalderas();
   static final int THERMAL_CANYONS = ((TFCLayersMixinInterface)staticBiomes).getStaticThermalCanyons();
   static final int MESA_PLATEAU = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaPlateau();
   static final int PEAT_BOG = ((TFCLayersMixinInterface)staticBiomes).getStaticPeatBog();
   static final int STEPPES = ((TFCLayersMixinInterface)staticBiomes).getStaticSteppes();
   static final int SHRUBLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticShrublands();
   static final int MOORLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticMoorlands();
   static final int MISTY_PEAKS = ((TFCLayersMixinInterface)staticBiomes).getStaticMistyPeaks();
   static final int NEAR_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticNearShore();
   static final int SHORE_DUNES = ((TFCLayersMixinInterface)staticBiomes).getStaticShoreDunes();
   static final int PLATEAU_CLIFFS = ((TFCLayersMixinInterface)staticBiomes).getStaticPlateauCliffs();
   static final int PELAGIC_ZONE = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZone();
   static final int SEAMOUNTS = ((TFCLayersMixinInterface)staticBiomes).getStaticSeamounts();
   static final int GUYOTS = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyots();
   static final int SAWTOOTH_CLIFFS = ((TFCLayersMixinInterface)staticBiomes).getStaticSawtoothCliffs();
   static final int TABLELANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticTablelands();
   static final int PITLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticPitlands();
   static final int FRACTURED_MISTY_PEAKS = ((TFCLayersMixinInterface)staticBiomes).getStaticFracturedMistyPeaks();

   private EdgeBiomeLayer() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      if (center != TFCLayers.PLATEAU
         && center != TFCLayers.BADLANDS
         && center != TFCLayers.INVERTED_BADLANDS
         && center != ALPINE_HIGHLANDS
         && center != MESA_HILLS) {
         if (center == BRYCE_CANYONS) {
            if (matcher.test(i -> i != BRYCE_CANYONS)) {
               return MESA_HILLS;
            }
         } else if (center == MESA_HILLS) {
            if (matcher.test(i -> i == BRYCE_CANYONS)) {
               return MESA_HILLS;
            }

            if (matcher.test(i -> i != MESA_HILLS)) {
               return STEPPES;
            }
         } else if (center == BADLANDS_PLATEAU) {
            if (matcher.test(i -> i == MESA_HILLS || i == BRYCE_CANYONS)) {
               return TFCLayers.BADLANDS;
            }
         } else if (center == FRACTURED_MISTY_PEAKS) {
            if (matcher.test(i -> i != FRACTURED_MISTY_PEAKS)) {
               return MISTY_PEAKS;
            }
         } else if (center == MISTY_PEAKS) {
            if (matcher.test(TFCLayers::isLow)) {
               return MESA_PLATEAU;
            }
         } else if (center == CALDERAS) {
            if (matcher.test(i -> i != CALDERAS)) {
               return ALPINE_HIGHLANDS;
            }
         } else if (center != ALPINE_MOUNTAINS && !TFCLayers.isMountains(center) && !TFCLayers.isOcean(center)) {
            if (matcher.test(i -> i == ALPINE_MOUNTAINS)) {
               return ALPINE_HIGHLANDS;
            }
         } else if (center == ALPINE_MOUNTAINS) {
            if (matcher.test(i -> i != ALPINE_MOUNTAINS)) {
               return ALPINE_HIGHLANDS;
            }
         } else if (center == ALPINE_HIGHLANDS) {
            if (matcher.test(i -> i == ALPINE_MOUNTAINS)) {
               return ALPINE_HIGHLANDS;
            }

            if (matcher.test(i -> i != ALPINE_MOUNTAINS)) {
               return ROLLING_HIGHLANDS;
            }
         } else if (center == THERMAL_CANYONS) {
            if (matcher.test(i -> i != THERMAL_CANYONS)) {
               return THERMAL_CANYONS;
            }
         } else if (center == MESA_PLATEAU) {
            if (matcher.test(i -> i != MESA_PLATEAU && !TFCLayers.isLow(i))) {
               return ROLLING_HIGHLANDS;
            }
         } else if (center == ROLLING_HIGHLANDS) {
            if (matcher.test(i -> i != ROLLING_HIGHLANDS)) {
               return TFCLayers.ROLLING_HILLS;
            }
         } else if (center == PEAT_BOG) {
            if (matcher.test(i -> i != PEAT_BOG)) {
               return TFCLayers.PLAINS;
            }
         } else if (center == MOORLANDS) {
            if (matcher.test(i -> i != MOORLANDS)) {
               return MARSHES;
            }
         } else if (center == TABLELANDS) {
            if (matcher.test(i -> i != TABLELANDS)) {
               return PITLANDS;
            }
         } else if (TFCLayers.isMountains(center)) {
            if (matcher.test(TFCLayers::isLow)) {
               return TFCLayers.ROLLING_HILLS;
            }
         } else if (center != TFCLayers.LOWLANDS && center != TFCLayers.LOW_CANYONS && center != WETLANDS && center != MARSHES && center != SWAMPS) {
            if (center != TFCLayers.PLAINS && center != TFCLayers.HILLS && center != GRASSLANDS) {
               if ((
                     center == TFCLayers.DEEP_OCEAN
                        || center == TFCLayers.DEEP_OCEAN_TRENCH
                        || center == PELAGIC_ZONE
                        || center == SEAMOUNTS
                        || center == GUYOTS
                  )
                  && matcher.test(i -> !TFCLayers.isOcean(i))) {
                  return TFCLayers.OCEAN;
               }
            } else {
               if (matcher.test(
                  i -> i == TFCLayers.PLATEAU || i == TFCLayers.BADLANDS || i == TFCLayers.INVERTED_BADLANDS || i == ALPINE_HIGHLANDS || i == MESA_HILLS
               )) {
                  return TFCLayers.HILLS;
               }

               if (matcher.test(TFCLayers::isMountains)) {
                  return TFCLayers.ROLLING_HILLS;
               }
            }
         } else {
            if (matcher.test(
               i -> i == TFCLayers.PLATEAU || i == TFCLayers.BADLANDS || i == TFCLayers.INVERTED_BADLANDS || i == ALPINE_HIGHLANDS || i == MESA_HILLS
            )) {
               return TFCLayers.HILLS;
            }

            if (matcher.test(TFCLayers::isMountains)) {
               return TFCLayers.ROLLING_HILLS;
            }
         }
      } else {
         if (matcher.test(i -> i == TFCLayers.LOW_CANYONS || i == TFCLayers.LOWLANDS || i == WETLANDS || i == MARSHES || i == SWAMPS)) {
            return TFCLayers.HILLS;
         }

         if (matcher.test(i -> i == TFCLayers.PLAINS || i == TFCLayers.HILLS || i == GRASSLANDS)) {
            return TFCLayers.ROLLING_HILLS;
         }
      }

      return center;
   }
}
