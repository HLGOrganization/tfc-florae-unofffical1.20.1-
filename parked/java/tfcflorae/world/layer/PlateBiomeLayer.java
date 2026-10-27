package tfcflorae.world.layer;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.AreaContext;
import net.dries007.tfc.world.layer.framework.CenterTransformLayer;
import tfcflorae.interfaces.TFCLayersMixinInterface;

public enum PlateBiomeLayer implements CenterTransformLayer {
   INSTANCE;

   public static TFCLayers staticBiomes = new TFCLayers();
   static final int GRASSLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticGrasslands();
   static final int WETLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticWetlands();
   static final int MARSHES = ((TFCLayersMixinInterface)staticBiomes).getStaticMarshes();
   static final int SWAMPS = ((TFCLayersMixinInterface)staticBiomes).getStaticSwamps();
   static final int PUY_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticPuyMountains();
   static final int BRYCE_CANYONS = ((TFCLayersMixinInterface)staticBiomes).getStaticBryceCanyons();
   static final int MESA_HILLS = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaHills();
   static final int BADLANDS_PLATEAU = ((TFCLayersMixinInterface)staticBiomes).getStaticBadlandsPlateau();
   static final int ALPINE_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineMountains();
   static final int ALPINE_HIGHLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineHighlands();
   static final int ROLLING_HIGHLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticRollingHighlands();
   static final int THERMAL_CANYONS = ((TFCLayersMixinInterface)staticBiomes).getStaticThermalCanyons();
   static final int MESA_PLATEAU = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaPlateau();
   static final int PEAT_BOG = ((TFCLayersMixinInterface)staticBiomes).getStaticPeatBog();
   static final int STEPPES = ((TFCLayersMixinInterface)staticBiomes).getStaticSteppes();
   static final int SHRUBLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticShrublands();
   static final int MOORLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticMoorlands();
   static final int MISTY_PEAKS = ((TFCLayersMixinInterface)staticBiomes).getStaticMistyPeaks();
   static final int VINICUNCA_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticVinicuncaMountains();
   static final int PELAGIC_ZONE = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZone();
   static final int SEAMOUNTS = ((TFCLayersMixinInterface)staticBiomes).getStaticSeamounts();
   static final int GUYOTS = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyots();
   static final int SAWTOOTH_CLIFFS = ((TFCLayersMixinInterface)staticBiomes).getStaticSawtoothCliffs();
   static final int PITLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticPitlands();
   static final int FRACTURED_MISTY_PEAKS = ((TFCLayersMixinInterface)staticBiomes).getStaticFracturedMistyPeaks();
   private static final int[] OCEANIC_BIOMES = new int[]{TFCLayers.DEEP_OCEAN, TFCLayers.DEEP_OCEAN, TFCLayers.DEEP_OCEAN, PELAGIC_ZONE};
   private static final int[] OCEAN_CONTINENT_CONVERGING = new int[]{TFCLayers.DEEP_OCEAN_TRENCH, TFCLayers.DEEP_OCEAN_TRENCH, SEAMOUNTS, GUYOTS};
   private static final int[] SUBDUCTION_BIOMES = new int[]{
      TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS,
      TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS,
      TFCLayers.VOLCANIC_MOUNTAINS,
      TFCLayers.VOLCANIC_MOUNTAINS,
      TFCLayers.MOUNTAINS,
      TFCLayers.PLATEAU,
      ALPINE_MOUNTAINS,
      ALPINE_HIGHLANDS,
      ROLLING_HIGHLANDS,
      THERMAL_CANYONS,
      THERMAL_CANYONS,
      MISTY_PEAKS,
      FRACTURED_MISTY_PEAKS,
      SAWTOOTH_CLIFFS
   };
   private static final int[] OROGENY_BIOMES = new int[]{
      TFCLayers.MOUNTAINS,
      TFCLayers.MOUNTAINS,
      TFCLayers.MOUNTAINS,
      TFCLayers.OLD_MOUNTAINS,
      TFCLayers.PLATEAU,
      ALPINE_MOUNTAINS,
      ALPINE_HIGHLANDS,
      ROLLING_HIGHLANDS,
      THERMAL_CANYONS,
      THERMAL_CANYONS,
      MISTY_PEAKS,
      VINICUNCA_MOUNTAINS,
      FRACTURED_MISTY_PEAKS,
      PITLANDS,
      SAWTOOTH_CLIFFS
   };
   private static final int[] RIFT_BIOMES = new int[]{
      TFCLayers.CANYONS,
      TFCLayers.CANYONS,
      TFCLayers.CANYONS,
      TFCLayers.CANYONS,
      TFCLayers.ROLLING_HILLS,
      TFCLayers.OLD_MOUNTAINS,
      PUY_MOUNTAINS,
      BRYCE_CANYONS,
      MESA_HILLS,
      ROLLING_HIGHLANDS,
      THERMAL_CANYONS,
      THERMAL_CANYONS,
      PITLANDS
   };
   private static final int[] CONTINENT_LOW_BIOMES = new int[]{
      TFCLayers.PLAINS,
      TFCLayers.PLAINS,
      TFCLayers.HILLS,
      TFCLayers.ROLLING_HILLS,
      TFCLayers.LOW_CANYONS,
      TFCLayers.LOW_CANYONS,
      TFCLayers.LOWLANDS,
      TFCLayers.LOWLANDS,
      GRASSLANDS,
      GRASSLANDS,
      WETLANDS,
      WETLANDS,
      MARSHES,
      MARSHES,
      SWAMPS,
      SWAMPS,
      ROLLING_HIGHLANDS,
      MESA_PLATEAU,
      PEAT_BOG,
      STEPPES,
      STEPPES,
      MOORLANDS
   };
   private static final int[] CONTINENT_MID_BIOMES = new int[]{
      TFCLayers.PLAINS,
      TFCLayers.HILLS,
      TFCLayers.ROLLING_HILLS,
      TFCLayers.INVERTED_BADLANDS,
      TFCLayers.BADLANDS,
      BADLANDS_PLATEAU,
      TFCLayers.PLATEAU,
      TFCLayers.LOW_CANYONS,
      TFCLayers.LOWLANDS,
      GRASSLANDS,
      WETLANDS,
      MARSHES,
      SWAMPS,
      PUY_MOUNTAINS,
      MESA_HILLS,
      ALPINE_HIGHLANDS,
      ROLLING_HIGHLANDS,
      MESA_PLATEAU,
      STEPPES,
      SHRUBLANDS,
      SHRUBLANDS,
      MOORLANDS,
      PITLANDS
   };
   private static final int[] CONTINENT_HIGH_BIOMES = new int[]{
      TFCLayers.HILLS,
      TFCLayers.ROLLING_HILLS,
      TFCLayers.ROLLING_HILLS,
      TFCLayers.INVERTED_BADLANDS,
      TFCLayers.BADLANDS,
      BADLANDS_PLATEAU,
      TFCLayers.PLATEAU,
      TFCLayers.PLATEAU,
      TFCLayers.OLD_MOUNTAINS,
      TFCLayers.OLD_MOUNTAINS,
      PUY_MOUNTAINS,
      BRYCE_CANYONS,
      MESA_HILLS,
      ALPINE_MOUNTAINS,
      ALPINE_HIGHLANDS,
      ROLLING_HIGHLANDS,
      THERMAL_CANYONS,
      SHRUBLANDS,
      MOORLANDS,
      MISTY_PEAKS,
      VINICUNCA_MOUNTAINS,
      FRACTURED_MISTY_PEAKS,
      SAWTOOTH_CLIFFS
   };

   private PlateBiomeLayer() {
   }

   public int apply(AreaContext context, int value) {
      switch (value) {
         case 0:
            return context.choose(OCEANIC_BIOMES);
         case 1:
            return context.choose(CONTINENT_LOW_BIOMES);
         case 2:
            return context.choose(CONTINENT_MID_BIOMES);
         case 3:
            return context.choose(CONTINENT_HIGH_BIOMES);
         case 4:
            return TFCLayers.OCEAN_OCEAN_DIVERGING_MARKER;
         case 5:
            return context.choose(OCEAN_CONTINENT_CONVERGING);
         case 6:
            return TFCLayers.OCEAN_OCEAN_CONVERGING_MARKER;
         case 7:
            return context.choose(OCEAN_CONTINENT_CONVERGING);
         case 8:
            return context.choose(SUBDUCTION_BIOMES);
         case 9:
         default:
            throw new IllegalStateException("What is this: " + value);
         case 10:
            return context.choose(RIFT_BIOMES);
         case 11:
            return context.choose(OROGENY_BIOMES);
         case 12:
            return TFCLayers.OCEAN;
      }
   }
}
