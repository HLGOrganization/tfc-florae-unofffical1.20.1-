package tfcflorae.world.layer.river;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.Area;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.TransformLayer;
import net.dries007.tfc.world.river.MidpointFractal;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public class MergeRiverBanksLayer implements TransformLayer {
   public final long seed;
   public final WatershedBank.Context watershedBank;
   public final Watershed.Context watersheds;
   public static TFCLayers staticBiomes = new TFCLayers();
   static final int CHASMS = ((TFCLayersMixinInterface)staticBiomes).getStaticChasms();
   static final int RIVERBANK = ((TFCLayersMixinInterface)staticBiomes).getStaticRiverbank();
   static final int RIVER_EDGE = ((TFCLayersMixinInterface)staticBiomes).getStaticRiverEdge();
   static final int WETLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticWetlands();
   static final int CALDERAS = ((TFCLayersMixinInterface)staticBiomes).getStaticCalderas();
   static final int ALPINE_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineMountains();
   static final int ALPINE_HIGHLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineHighlands();
   static final int CANYON_RIVER = ((TFCLayersMixinInterface)staticBiomes).getStaticCanyonRivers();
   static final int ALPINE_MOUNTAIN_RIVER = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineMountainRivers();
   static final int THERMAL_CANYONS = ((TFCLayersMixinInterface)staticBiomes).getStaticThermalCanyons();
   static final int MARSHES = ((TFCLayersMixinInterface)staticBiomes).getStaticThermalCanyons();
   static final int SWAMPS = ((TFCLayersMixinInterface)staticBiomes).getStaticMarshes();
   static final int MANGROVES = ((TFCLayersMixinInterface)staticBiomes).getStaticMangroves();
   static final int LAKE_SHORE = ((TFCLayersMixinInterface)staticBiomes).getStaticLakeShore();

   public MergeRiverBanksLayer(WatershedBank.Context watershedBank, Watershed.Context watersheds, long seed) {
      this.watershedBank = watershedBank;
      this.watersheds = watersheds;
      this.seed = seed;
   }

   public int apply(AreaContext context, Area area, int x, int z) {
      int value = area.get(x, z);
      if (specialRiver(value)) {
         float scale = 0.0078125F;
         float x0 = (float)x * 0.0078125F;
         float z0 = (float)z * 0.0078125F;

         for (MidpointFractal fractal : this.watershedBank.getFractalsByPartition(x, z)) {
            if (fractal.maybeIntersect(x0, z0, Watershed.RIVER_WIDTH_OLD) && fractal.intersect(x0, z0, Watershed.RIVER_WIDTH_OLD)) {
               return value;
            }
         }
      } else if (!TFCLayers.isRiver(value)
         && TFCLayers.hasRiver(value)
         && !hasRiverCave(value)
         && !isLowlands(value)
         && !TFCLayers.isOcean(value)
         && !TFCLayers.isLake(value)
         && value != LAKE_SHORE
         && !TFCLayers.isOceanOrMarker(value)) {
         float scale = 0.0078125F;
         float x0 = (float)x * 0.0078125F;
         float z0 = (float)z * 0.0078125F;

         for (MidpointFractal fractalx : this.watershedBank.getFractalsByPartition(x, z)) {
            if (fractalx.maybeIntersect(x0, z0, WatershedBank.RIVERBANK_WIDTH_OLD) && fractalx.intersect(x0, z0, WatershedBank.RIVERBANK_WIDTH_OLD)) {
               return RIVER_EDGE;
            }
         }
      } else if (value == TFCLayers.RIVER) {
         float scale = 0.0078125F;
         float x0 = (float)x * 0.0078125F;
         float z0 = (float)z * 0.0078125F;

         for (MidpointFractal fractalxx : this.watershedBank.getFractalsByPartition(x, z)) {
            if (fractalxx.maybeIntersect(x0, z0, Watershed.RIVER_WIDTH_OLD) && fractalxx.intersect(x0, z0, Watershed.RIVER_WIDTH_OLD)) {
               return TFCLayers.RIVER;
            }
         }
      }

      return value;
   }

   public static boolean hasRiverCave(int value) {
      return value == CALDERAS
         || value == ALPINE_MOUNTAINS
         || value == ALPINE_HIGHLANDS
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

   public static boolean specialRiver(int value) {
      return value == CANYON_RIVER
         || value == ALPINE_MOUNTAIN_RIVER
         || value == TFCLayers.MOUNTAIN_RIVER
         || value == TFCLayers.VOLCANIC_MOUNTAIN_RIVER
         || value == TFCLayers.OLD_MOUNTAIN_RIVER
         || value == TFCLayers.OCEANIC_MOUNTAIN_RIVER
         || value == TFCLayers.VOLCANIC_OCEANIC_MOUNTAIN_RIVER;
   }
}
