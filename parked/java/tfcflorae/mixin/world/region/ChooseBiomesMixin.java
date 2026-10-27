package tfcflorae.mixin.world.region;

import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.region.ChooseBiomes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tfcflorae.interfaces.TFCLayersMixinInterface;

@Mixin({ChooseBiomes.class})
public class ChooseBiomesMixin {
   @Unique
   private static TFCLayers staticBiomes = new TFCLayers();
   @Unique
   private static final int GRASSLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticGrasslands();
   @Unique
   private static final int WETLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticWetlands();
   @Unique
   private static final int MARSHES = ((TFCLayersMixinInterface)staticBiomes).getStaticMarshes();
   @Unique
   private static final int SWAMPS = ((TFCLayersMixinInterface)staticBiomes).getStaticSwamps();
   @Unique
   private static final int PUY_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticPuyMountains();
   @Unique
   private static final int BRYCE_CANYONS = ((TFCLayersMixinInterface)staticBiomes).getStaticBryceCanyons();
   @Unique
   private static final int MESA_HILLS = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaHills();
   @Unique
   private static final int BADLANDS_PLATEAU = ((TFCLayersMixinInterface)staticBiomes).getStaticBadlandsPlateau();
   @Unique
   private static final int ALPINE_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineMountains();
   @Unique
   private static final int ALPINE_HIGHLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticAlpineHighlands();
   @Unique
   private static final int ROLLING_HIGHLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticRollingHighlands();
   @Unique
   private static final int THERMAL_CANYONS = ((TFCLayersMixinInterface)staticBiomes).getStaticThermalCanyons();
   @Unique
   private static final int MESA_PLATEAU = ((TFCLayersMixinInterface)staticBiomes).getStaticMesaPlateau();
   @Unique
   private static final int PEAT_BOG = ((TFCLayersMixinInterface)staticBiomes).getStaticPeatBog();
   @Unique
   private static final int STEPPES = ((TFCLayersMixinInterface)staticBiomes).getStaticSteppes();
   @Unique
   private static final int SHRUBLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticShrublands();
   @Unique
   private static final int MOORLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticMoorlands();
   @Unique
   private static final int MISTY_PEAKS = ((TFCLayersMixinInterface)staticBiomes).getStaticMistyPeaks();
   @Unique
   private static final int VINICUNCA_MOUNTAINS = ((TFCLayersMixinInterface)staticBiomes).getStaticVinicuncaMountains();
   @Unique
   private static final int SAWTOOTH_CLIFFS = ((TFCLayersMixinInterface)staticBiomes).getStaticSawtoothCliffs();
   @Unique
   private static final int PITLANDS = ((TFCLayersMixinInterface)staticBiomes).getStaticPitlands();
   @Unique
   private static final int FRACTURED_MISTY_PEAKS = ((TFCLayersMixinInterface)staticBiomes).getStaticFracturedMistyPeaks();
   @Unique
   private static final int PELAGIC_ZONE = ((TFCLayersMixinInterface)staticBiomes).getStaticPelagicZone();
   @Unique
   private static final int SEAMOUNTS = ((TFCLayersMixinInterface)staticBiomes).getStaticSeamounts();
   @Unique
   private static final int GUYOTS = ((TFCLayersMixinInterface)staticBiomes).getStaticGuyots();
   @Shadow
   @Mutable
   @Final
   private static final int[] MOUNTAIN_ALTITUDE_BIOMES = new int[]{
      TFCLayers.MOUNTAINS,
      TFCLayers.MOUNTAINS,
      TFCLayers.MOUNTAINS,
      TFCLayers.OLD_MOUNTAINS,
      TFCLayers.OLD_MOUNTAINS,
      TFCLayers.PLATEAU,
      MESA_PLATEAU,
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
   @Shadow
   @Mutable
   @Final
   private static final int[] OCEANIC_MOUNTAIN_ALTITUDE_BIOMES = new int[]{
      TFCLayers.VOLCANIC_MOUNTAINS,
      TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS,
      TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS,
      TFCLayers.OCEANIC_MOUNTAINS,
      TFCLayers.OCEANIC_MOUNTAINS,
      TFCLayers.ROLLING_HILLS,
      ROLLING_HIGHLANDS,
      MISTY_PEAKS,
      ALPINE_HIGHLANDS,
      FRACTURED_MISTY_PEAKS
   };
   @Shadow
   @Mutable
   @Final
   private static final int[] HIGH_ALTITUDE_BIOMES = new int[]{
      TFCLayers.HILLS,
      TFCLayers.ROLLING_HILLS,
      TFCLayers.ROLLING_HILLS,
      TFCLayers.INVERTED_BADLANDS,
      TFCLayers.BADLANDS,
      TFCLayers.PLATEAU,
      TFCLayers.PLATEAU,
      TFCLayers.OLD_MOUNTAINS,
      TFCLayers.OLD_MOUNTAINS,
      PUY_MOUNTAINS,
      BRYCE_CANYONS,
      MESA_HILLS,
      BADLANDS_PLATEAU,
      MESA_PLATEAU,
      ROLLING_HIGHLANDS,
      ALPINE_MOUNTAINS,
      ALPINE_HIGHLANDS,
      THERMAL_CANYONS,
      SHRUBLANDS,
      MOORLANDS,
      MISTY_PEAKS,
      VINICUNCA_MOUNTAINS,
      FRACTURED_MISTY_PEAKS,
      SAWTOOTH_CLIFFS
   };
   @Shadow
   @Mutable
   @Final
   private static final int[] MID_ALTITUDE_BIOMES = new int[]{
      TFCLayers.PLAINS,
      TFCLayers.HILLS,
      TFCLayers.ROLLING_HILLS,
      TFCLayers.INVERTED_BADLANDS,
      TFCLayers.BADLANDS,
      TFCLayers.PLATEAU,
      TFCLayers.CANYONS,
      TFCLayers.LOW_CANYONS,
      TFCLayers.LOWLANDS,
      PUY_MOUNTAINS,
      BRYCE_CANYONS,
      MESA_HILLS,
      BADLANDS_PLATEAU,
      MESA_PLATEAU,
      ROLLING_HIGHLANDS,
      GRASSLANDS,
      WETLANDS,
      MARSHES,
      SWAMPS,
      PUY_MOUNTAINS,
      MESA_HILLS,
      ALPINE_HIGHLANDS,
      STEPPES,
      SHRUBLANDS,
      SHRUBLANDS,
      MOORLANDS,
      PITLANDS
   };
   @Shadow
   @Mutable
   @Final
   private static final int[] LOW_ALTITUDE_BIOMES = new int[]{
      TFCLayers.PLAINS,
      TFCLayers.PLAINS,
      TFCLayers.HILLS,
      TFCLayers.HILLS,
      TFCLayers.ROLLING_HILLS,
      TFCLayers.LOW_CANYONS,
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
      PEAT_BOG,
      STEPPES,
      STEPPES,
      MOORLANDS,
      THERMAL_CANYONS
   };
   @Shadow
   @Mutable
   @Final
   private static final int[] ISLAND_BIOMES = new int[]{
      TFCLayers.PLAINS, TFCLayers.HILLS, TFCLayers.ROLLING_HILLS, TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS, TFCLayers.VOLCANIC_OCEANIC_MOUNTAINS
   };
   @Shadow
   @Mutable
   @Final
   private static final int[] MID_DEPTH_OCEAN_BIOMES = new int[]{
      PELAGIC_ZONE,
      SEAMOUNTS,
      GUYOTS,
      TFCLayers.DEEP_OCEAN,
      TFCLayers.DEEP_OCEAN,
      TFCLayers.OCEAN,
      TFCLayers.OCEAN,
      TFCLayers.OCEAN,
      TFCLayers.OCEAN,
      TFCLayers.OCEAN_REEF,
      TFCLayers.OCEAN_REEF,
      TFCLayers.OCEAN_REEF,
      TFCLayers.OCEAN_REEF,
      TFCLayers.OCEAN_REEF,
      TFCLayers.OCEAN_REEF,
      TFCLayers.OCEAN_REEF,
      TFCLayers.OCEAN_REEF
   };

   public ChooseBiomesMixin() {
   }
}
