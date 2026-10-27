package tfcflorae.world.layer.river;

import java.util.function.IntPredicate;
import java.util.function.Predicate;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AdjacentTransformLayer;
import net.dries007.tfc.world.layer.framework.AreaContext;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum EdgeRiverbankLayer implements AdjacentTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int CHASMS = ((TFCLayersMixinInterface)staticBiomes).getStaticChasms();
   static final int RIVERBANK = ((TFCLayersMixinInterface)staticBiomes).getStaticRiverbank();
   static final int RIVER_EDGE = ((TFCLayersMixinInterface)staticBiomes).getStaticRiverEdge();
   static final int WETLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticWetlands();
   static final int MARSHES = ((TFCLayersMixinInterface)staticBiomes).getStaticMarshes();
   static final int SWAMPS = ((TFCLayersMixinInterface)staticBiomes).getStaticSwamps();
   static final int CALDERAS = ((TFCLayersMixinInterface)staticBiomes).getStaticCalderas();
   static final int ALPINE_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineMountains();
   static final int ALPINE_HIGHLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineHighlands();
   static final int CANYON_RIVER = ((TFCLayersMixinInterface)staticBiomes).getStaticCanyonRivers();
   static final int ALPINE_MOUNTAIN_RIVER = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineMountainRivers();
   static final int GRAVEL_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticGravelShores();
   static final int THERMAL_CANYONS = ((TFCLayersMixinInterface)staticBiomes).getStaticThermalCanyons();
   static final int NEAR_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticNearShore();
   static final int SHORE_DUNES = ((TFCLayersMixinInterface)staticBiomes).getStaticShoreDunes();
   static final int MANGROVES = ((TFCLayersMixinInterface)staticBiomes).getStaticMangroves();
   static final int LAKE_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticLakeShore();

   private EdgeRiverbankLayer() {
   }

   public int apply(AreaContext context, int north, int east, int south, int west, int center) {
      Predicate<IntPredicate> matcher = p -> p.test(north) || p.test(east) || p.test(south) || p.test(west);
      if (matcher.test(i -> !TFCLayers.isOceanOrMarker(i))) {
         if (center == RIVER_EDGE) {
            if (matcher.test(i -> i == CALDERAS)) {
               return CANYON_RIVER;
            }

            if (matcher.test(i -> i == ALPINE_MOUNTAINS)) {
               return ALPINE_MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.MOUNTAINS)) {
               return TFCLayers.MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.VOLCANIC_MOUNTAINS)) {
               return TFCLayers.VOLCANIC_MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.OLD_MOUNTAINS)) {
               return TFCLayers.OLD_MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.OCEANIC_MOUNTAINS)) {
               return TFCLayers.OCEANIC_MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS)) {
               return TFCLayers.VOLCANIC_OCEANIC_MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.SHORE)) {
               return TFCLayers.SHORE;
            }

            if (matcher.test(i -> i == GRAVEL_SHORE)) {
               return GRAVEL_SHORE;
            }

            if (matcher.test(i -> TFCLayers.isOcean(i))) {
               return TFCLayers.OCEAN;
            }

            if (matcher.test(i -> i == TFCLayers.LOWLANDS)) {
               return TFCLayers.LOWLANDS;
            }

            if (matcher.test(i -> i == THERMAL_CANYONS)) {
               return THERMAL_CANYONS;
            }

            if (matcher.test(i -> i == TFCLayers.LOW_CANYONS)) {
               return TFCLayers.LOW_CANYONS;
            }

            if (matcher.test(i -> i == WETLANDS)) {
               return WETLANDS;
            }

            if (matcher.test(i -> i == MARSHES)) {
               return MARSHES;
            }

            if (matcher.test(i -> i == SWAMPS)) {
               return SWAMPS;
            }

            if (matcher.test(i -> i == TFCLayers.LAKE || i == LAKE_SHORE)) {
               return TFCLayers.LAKE;
            }

            if (matcher.test(i -> !TFCLayers.isRiver(i) && TFCLayers.hasRiver(i) && TFCLayers.riverFor(i) == TFCLayers.RIVER && !isLowlands(i))) {
               if (matcher.test(i -> i == TFCLayers.SHORE || i == NEAR_SHORE || i == GRAVEL_SHORE)) {
                  return NEAR_SHORE;
               }

               if (matcher.test(i -> i == SHORE_DUNES)) {
                  return SHORE_DUNES;
               }

               return RIVERBANK;
            }

            if (matcher.test(i -> i == TFCLayers.RIVER)) {
               return TFCLayers.RIVER;
            }
         } else if (center == TFCLayers.RIVER) {
            if (matcher.test(i -> i == CALDERAS)) {
               return CANYON_RIVER;
            }

            if (matcher.test(i -> i == ALPINE_MOUNTAINS)) {
               return ALPINE_MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.MOUNTAINS)) {
               return TFCLayers.MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.VOLCANIC_MOUNTAINS)) {
               return TFCLayers.VOLCANIC_MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.OLD_MOUNTAINS)) {
               return TFCLayers.OLD_MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.OCEANIC_MOUNTAINS)) {
               return TFCLayers.OCEANIC_MOUNTAIN_RIVER;
            }

            if (matcher.test(i -> i == TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS)) {
               return TFCLayers.VOLCANIC_OCEANIC_MOUNTAIN_RIVER;
            }
         }
      }

      return center;
   }

   public static boolean hasRiverCave(int value) {
      return value == ALPINE_MOUNTAINS
         || value == TFCLayers.MOUNTAINS
         || value == TFCLayers.VOLCANIC_MOUNTAINS
         || value == TFCLayers.OLD_MOUNTAINS
         || value == TFCLayers.OCEANIC_MOUNTAINS
         || value == TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS;
   }

   public static boolean isLowlands(int value) {
      return value == TFCLayers.LOWLANDS
         || value == THERMAL_CANYONS
         || value == TFCLayers.LOW_CANYONS
         || value == WETLANDS
         || value == MARSHES
         || value == SWAMPS
         || value == MANGROVES;
   }
}
