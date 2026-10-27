package tfcflorae.mixin.world.layer;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IArtist;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.layer.AddLakesLayer;
import net.dries007.tfc.world.layer.BiasedLandPlateZoomLayer;
import net.dries007.tfc.world.layer.InlandLayer;
import net.dries007.tfc.world.layer.MergeLakeLayer;
import net.dries007.tfc.world.layer.Plate;
import net.dries007.tfc.world.layer.PlateBoundaryLayer;
import net.dries007.tfc.world.layer.PlateBoundaryModifierLayer;
import net.dries007.tfc.world.layer.PlateGenerationLayer;
import net.dries007.tfc.world.layer.RandomizeNeighborsLayer;
import net.dries007.tfc.world.layer.RockLayer;
import net.dries007.tfc.world.layer.ShoreLayer;
import net.dries007.tfc.world.layer.SmoothLayer;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.TypedZoomLayer;
import net.dries007.tfc.world.layer.ZoomLayer;
import net.dries007.tfc.world.layer.framework.AreaFactory;
import net.dries007.tfc.world.layer.framework.TypedAreaFactory;
import net.dries007.tfc.world.noise.Cellular2D;
import org.apache.commons.lang3.mutable.MutableInt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tfcflorae.Config;
import tfcflorae.interfaces.TFCBiomesMixinInterface;
import tfcflorae.interfaces.TFCLayersMixinInterface;
import tfcflorae.world.layer.AddCalderasLayer;
import tfcflorae.world.layer.AddMesaPlateauLayer;
import tfcflorae.world.layer.AddPuyMountainsLayer;
import tfcflorae.world.layer.ArchipelagoLayerTFCF;
import tfcflorae.world.layer.Chasm;
import tfcflorae.world.layer.CoastalCliffs;
import tfcflorae.world.layer.CoastalCliffsShoreLayer;
import tfcflorae.world.layer.EdgeBiomeLayer;
import tfcflorae.world.layer.EdgeDunesShoreLayer;
import tfcflorae.world.layer.ExtraDunesShoreLayer;
import tfcflorae.world.layer.LakeEdgeLayer;
import tfcflorae.world.layer.MergeCalderaLayer;
import tfcflorae.world.layer.MergeChasmsLayer;
import tfcflorae.world.layer.MergeCoastalCliffsLayer;
import tfcflorae.world.layer.MergeMesaPlateauLayer;
import tfcflorae.world.layer.MergePuyMountainsLayer;
import tfcflorae.world.layer.MergeShoreDunesLayer;
import tfcflorae.world.layer.NearShoreLayer;
import tfcflorae.world.layer.OceanBorderLayerTFCF;
import tfcflorae.world.layer.OceanEdgeBiomeLayer;
import tfcflorae.world.layer.OceanLayer;
import tfcflorae.world.layer.PlateBiomeLayer;
import tfcflorae.world.layer.PlateauCliffsLayer;
import tfcflorae.world.layer.ReefBorderLayerTFCF;
import tfcflorae.world.layer.ShoreDunes;
import tfcflorae.world.layer.ZoomBiomesLayer;
import tfcflorae.world.layer.river.EdgeRiverbankLayer;
import tfcflorae.world.layer.river.MergeRiverBanksLayer;
import tfcflorae.world.layer.river.TFCFMergeRiverLayer;
import tfcflorae.world.layer.river.Watershed;
import tfcflorae.world.layer.river.WatershedBank;

@Mixin({TFCLayers.class})
public class TFCLayersMixin implements TFCLayersMixinInterface {
   @Unique
   private static TFCBiomes staticBiomes = new TFCBiomes();
   @Shadow
   public static final int OCEANIC = 0;
   @Shadow
   public static final int CONTINENTAL_LOW = 1;
   @Shadow
   public static final int CONTINENTAL_MID = 2;
   @Shadow
   public static final int CONTINENTAL_HIGH = 3;
   @Shadow
   public static final int OCEAN_OCEAN_DIVERGING = 4;
   @Shadow
   public static final int OCEAN_OCEAN_CONVERGING_LOWER = 5;
   @Shadow
   public static final int OCEAN_OCEAN_CONVERGING_UPPER = 6;
   @Shadow
   public static final int OCEAN_CONTINENT_CONVERGING_LOWER = 7;
   @Shadow
   public static final int OCEAN_CONTINENT_CONVERGING_UPPER = 8;
   @Shadow
   public static final int OCEAN_CONTINENT_DIVERGING = 9;
   @Shadow
   public static final int CONTINENT_CONTINENT_DIVERGING = 10;
   @Shadow
   public static final int CONTINENT_CONTINENT_CONVERGING = 11;
   @Shadow
   public static final int CONTINENTAL_SHELF = 12;
   @Shadow
   public static final int FOREST_NONE = ForestType.NONE.ordinal();
   @Shadow
   public static final int FOREST_NORMAL = ForestType.NORMAL.ordinal();
   @Shadow
   public static final int FOREST_SPARSE = ForestType.SPARSE.ordinal();
   @Shadow
   public static final int FOREST_EDGE = ForestType.EDGE.ordinal();
   @Shadow
   public static final int FOREST_OLD = ForestType.OLD_GROWTH.ordinal();
   @Shadow
   public static final int OCEAN = register(() -> TFCBiomes.OCEAN);
   @Shadow
   public static final int OCEAN_REEF = register(() -> TFCBiomes.OCEAN_REEF);
   @Shadow
   public static final int DEEP_OCEAN = register(() -> TFCBiomes.DEEP_OCEAN);
   @Shadow
   public static final int DEEP_OCEAN_TRENCH = register(() -> TFCBiomes.DEEP_OCEAN_TRENCH);
   @Shadow
   public static final int PLAINS = register(() -> TFCBiomes.PLAINS);
   @Shadow
   public static final int HILLS = register(() -> TFCBiomes.HILLS);
   @Shadow
   public static final int LOWLANDS = register(() -> TFCBiomes.LOWLANDS);
   @Shadow
   public static final int LOW_CANYONS = register(() -> TFCBiomes.LOW_CANYONS);
   @Shadow
   public static final int ROLLING_HILLS = register(() -> TFCBiomes.ROLLING_HILLS);
   @Shadow
   public static final int BADLANDS = register(() -> TFCBiomes.BADLANDS);
   @Shadow
   public static final int INVERTED_BADLANDS = register(() -> TFCBiomes.INVERTED_BADLANDS);
   @Shadow
   public static final int PLATEAU = register(() -> TFCBiomes.PLATEAU);
   @Shadow
   public static final int OLD_MOUNTAINS = register(() -> TFCBiomes.OLD_MOUNTAINS);
   @Shadow
   public static final int MOUNTAINS = register(() -> TFCBiomes.MOUNTAINS);
   @Shadow
   public static final int VOLCANIC_MOUNTAINS = register(() -> TFCBiomes.VOLCANIC_MOUNTAINS);
   @Shadow
   public static final int OCEANIC_MOUNTAINS = register(() -> TFCBiomes.OCEANIC_MOUNTAINS);
   @Shadow
   public static final int VOLCANIC_OCEANIC_MOUNTAINS = register(() -> TFCBiomes.VOLCANIC_OCEANIC_MOUNTAINS);
   @Shadow
   public static final int CANYONS = register(() -> TFCBiomes.CANYONS);
   @Shadow
   public static final int SHORE = register(() -> TFCBiomes.SHORE);
   @Shadow
   public static final int LAKE = register(() -> TFCBiomes.LAKE);
   @Shadow
   public static final int RIVER = register(() -> TFCBiomes.RIVER);
   @Shadow
   public static final int MOUNTAIN_RIVER = register(() -> TFCBiomes.MOUNTAIN_RIVER);
   @Shadow
   public static final int VOLCANIC_MOUNTAIN_RIVER = register(() -> TFCBiomes.VOLCANIC_MOUNTAIN_RIVER);
   @Shadow
   public static final int OLD_MOUNTAIN_RIVER = register(() -> TFCBiomes.OLD_MOUNTAIN_RIVER);
   @Shadow
   public static final int OCEANIC_MOUNTAIN_RIVER = register(() -> TFCBiomes.OCEANIC_MOUNTAIN_RIVER);
   @Shadow
   public static final int VOLCANIC_OCEANIC_MOUNTAIN_RIVER = register(() -> TFCBiomes.VOLCANIC_OCEANIC_MOUNTAIN_RIVER);
   @Shadow
   public static final int MOUNTAIN_LAKE = register(() -> TFCBiomes.MOUNTAIN_LAKE);
   @Shadow
   public static final int VOLCANIC_MOUNTAIN_LAKE = register(() -> TFCBiomes.VOLCANIC_MOUNTAIN_LAKE);
   @Shadow
   public static final int OLD_MOUNTAIN_LAKE = register(() -> TFCBiomes.OLD_MOUNTAIN_LAKE);
   @Shadow
   public static final int OCEANIC_MOUNTAIN_LAKE = register(() -> TFCBiomes.OCEANIC_MOUNTAIN_LAKE);
   @Shadow
   public static final int VOLCANIC_OCEANIC_MOUNTAIN_LAKE = register(() -> TFCBiomes.VOLCANIC_OCEANIC_MOUNTAIN_LAKE);
   @Shadow
   public static final int PLATEAU_LAKE = register(() -> TFCBiomes.PLATEAU_LAKE);
   @Unique
   private static final int GRASSLANDS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticGrasslands);
   @Unique
   private static final int WETLANDS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticWetlands);
   @Unique
   private static final int MARSHES = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticMarshes);
   @Unique
   private static final int SWAMPS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticSwamps);
   @Unique
   private static final int MANGROVES = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticMangroves);
   @Unique
   private static final int PUY_MOUNTAINS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticPuyMountains);
   @Unique
   private static final int BRYCE_CANYONS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticBryceCanyons);
   @Unique
   private static final int MESA_HILLS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticMesaHills);
   @Unique
   private static final int BADLANDS_PLATEAU = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticBadlandsPlateau);
   @Unique
   private static final int CANYON_RIVER = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticCanyonRivers);
   @Unique
   private static final int ALPINE_MOUNTAIN_RIVER = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticAlpineMountainRivers);
   @Unique
   private static final int ALPINE_MOUNTAINS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticAlpineMountains);
   @Unique
   private static final int ALPINE_HIGHLANDS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticAlpineHighlands);
   @Unique
   private static final int ROLLING_HIGHLANDS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticRollingHighlands);
   @Unique
   private static final int CALDERAS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticCalderas);
   @Unique
   private static final int GRAVEL_SHORE = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticGravelShores);
   @Unique
   private static final int THERMAL_CANYONS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticThermalCanyons);
   @Unique
   private static final int MESA_PLATEAU = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticMesaPlateau);
   @Unique
   private static final int PEAT_BOG = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticPeatBog);
   @Unique
   private static final int RIVERBANK = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticRiverbank);
   @Unique
   private static final int RIVER_EDGE = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticRiverEdge);
   @Unique
   private static final int CHASMS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticChasms);
   @Unique
   private static final int PLATEAU_CLIFFS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticPlateauCliffs);
   @Unique
   private static final int STEPPES = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticSteppes);
   @Unique
   private static final int SHRUBLANDS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticShrublands);
   @Unique
   private static final int MOORLANDS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticMoorlands);
   @Unique
   private static final int MISTY_PEAKS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticMistyPeaks);
   @Unique
   private static final int COASTAL_CLIFFS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticCoastalCliffs);
   @Unique
   private static final int SHORE_DUNES = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticShoreDunes);
   @Unique
   private static final int VINICUNCA_MOUNTAINS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticVinicuncaMountains);
   @Unique
   private static final int NEAR_SHORE = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticNearShore);
   @Unique
   private static final int LAKE_SHORE = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticLakeShore);
   @Unique
   private static final int PELAGIC_ZONE = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticPelagicZone);
   @Unique
   private static final int SEAMOUNTS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticSeamounts);
   @Unique
   private static final int GUYOTS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticGuyots);
   @Unique
   private static final int ATOLL = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticAtoll);
   @Unique
   private static final int BARRIER_REEF = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticBarrierReef);
   @Unique
   private static final int LAGOON = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticLagoon);
   @Unique
   private static final int SAWTOOTH_CLIFFS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticSawtoothCliffs);
   @Unique
   private static final int TABLELANDS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticTablelands);
   @Unique
   private static final int PITLANDS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticPitlands);
   @Unique
   private static final int FRACTURED_MISTY_PEAKS = register(((TFCBiomesMixinInterface)staticBiomes)::getStaticFracturedMistyPeaks);
   @Shadow
   public static final int OCEAN_OCEAN_CONVERGING_MARKER = register();
   @Shadow
   public static final int OCEAN_OCEAN_DIVERGING_MARKER = register();
   @Shadow
   public static final int LAKE_MARKER = register();
   @Shadow
   public static final int NULL_MARKER = register();
   @Shadow
   public static final int INLAND_MARKER = register();
   @Shadow
   public static final int OCEAN_REEF_MARKER = register();
   @Unique
   private static final int CALDERA_MARKER = register();
   @Unique
   private static final int PUY_MOUNTAINS_MARKER = register();
   @Unique
   private static final int MESA_PLATEAU_MARKER = register();
   @Unique
   private static final int PELAGIC_ZONE_MARKER = register();
   @Unique
   private static final int SEAMOUNTS_MARKER = register();
   @Unique
   private static final int GUYOTS_MARKER = register();
   @Unique
   private static final int ATOLL_MARKER = register();
   @Unique
   private static final int BARRIER_REEF_MARKER = register();
   @Unique
   private static final int BARRIER_REEF_SHORE_MARKER = register();
   @Unique
   private static final int BARRIER_REEF_OCEAN_MARKER = register();
   @Unique
   private static final int LAGOON_MARKER = register();
   @Shadow
   @Mutable
   @Final
   private static final BiomeExtension[] BIOME_LAYERS = new BiomeExtension[128];
   @Shadow
   private static final MutableInt BIOME_LAYER_INDEX = new MutableInt(0);

   public TFCLayersMixin() {
   }

   @Unique
   @Override
   public int getStaticCalderaMarker() {
      return CALDERA_MARKER;
   }

   @Unique
   @Override
   public int getStaticPuyMountainsMarker() {
      return PUY_MOUNTAINS_MARKER;
   }

   @Unique
   @Override
   public int getStaticMesaPlateauMarker() {
      return MESA_PLATEAU_MARKER;
   }

   @Unique
   @Override
   public int getStaticPelagicZoneMarker() {
      return PELAGIC_ZONE_MARKER;
   }

   @Unique
   @Override
   public int getStaticSeamountsMarker() {
      return SEAMOUNTS_MARKER;
   }

   @Unique
   @Override
   public int getStaticGuyotsMarker() {
      return GUYOTS_MARKER;
   }

   @Unique
   @Override
   public int getStaticAtollMarker() {
      return ATOLL_MARKER;
   }

   @Unique
   @Override
   public int getStaticBarrierReefMarker() {
      return BARRIER_REEF_MARKER;
   }

   @Unique
   @Override
   public int getStaticBarrierReefShoreMarker() {
      return BARRIER_REEF_SHORE_MARKER;
   }

   @Unique
   @Override
   public int getStaticBarrierReefOceanMarker() {
      return BARRIER_REEF_OCEAN_MARKER;
   }

   @Unique
   @Override
   public int getStaticLagoonMarker() {
      return LAGOON_MARKER;
   }

   @Override
   public int getStaticGrasslands() {
      return GRASSLANDS;
   }

   @Override
   public int getStaticWetlands() {
      return WETLANDS;
   }

   @Override
   public int getStaticMarshes() {
      return MARSHES;
   }

   @Override
   public int getStaticSwamps() {
      return SWAMPS;
   }

   @Override
   public int getStaticMangroves() {
      return MANGROVES;
   }

   @Override
   public int getStaticPuyMountains() {
      return PUY_MOUNTAINS;
   }

   @Override
   public int getStaticBryceCanyons() {
      return BRYCE_CANYONS;
   }

   @Override
   public int getStaticMesaHills() {
      return MESA_HILLS;
   }

   @Override
   public int getStaticBadlandsPlateau() {
      return BADLANDS_PLATEAU;
   }

   @Override
   public int getStaticCanyonRivers() {
      return CANYON_RIVER;
   }

   @Override
   public int getStaticAlpineMountainRivers() {
      return ALPINE_MOUNTAIN_RIVER;
   }

   @Override
   public int getStaticAlpineMountains() {
      return ALPINE_MOUNTAINS;
   }

   @Override
   public int getStaticAlpineHighlands() {
      return ALPINE_HIGHLANDS;
   }

   @Override
   public int getStaticRollingHighlands() {
      return ROLLING_HIGHLANDS;
   }

   @Override
   public int getStaticCalderas() {
      return CALDERAS;
   }

   @Override
   public int getStaticGravelShores() {
      return GRAVEL_SHORE;
   }

   @Unique
   @Override
   public int getStaticThermalCanyons() {
      return THERMAL_CANYONS;
   }

   @Unique
   @Override
   public int getStaticMesaPlateau() {
      return MESA_PLATEAU;
   }

   @Unique
   @Override
   public int getStaticPeatBog() {
      return PEAT_BOG;
   }

   @Unique
   @Override
   public int getStaticRiverbank() {
      return RIVERBANK;
   }

   @Unique
   @Override
   public int getStaticRiverEdge() {
      return RIVER_EDGE;
   }

   @Unique
   @Override
   public int getStaticChasms() {
      return CHASMS;
   }

   @Unique
   @Override
   public int getStaticPlateauCliffs() {
      return PLATEAU_CLIFFS;
   }

   @Unique
   @Override
   public int getStaticSteppes() {
      return STEPPES;
   }

   @Unique
   @Override
   public int getStaticMoorlands() {
      return SHRUBLANDS;
   }

   @Unique
   @Override
   public int getStaticShrublands() {
      return MOORLANDS;
   }

   @Unique
   @Override
   public int getStaticMistyPeaks() {
      return MISTY_PEAKS;
   }

   @Unique
   @Override
   public int getStaticCoastalCliffs() {
      return COASTAL_CLIFFS;
   }

   @Unique
   @Override
   public int getStaticShoreDunes() {
      return SHORE_DUNES;
   }

   @Unique
   @Override
   public int getStaticVinicuncaMountains() {
      return VINICUNCA_MOUNTAINS;
   }

   @Unique
   @Override
   public int getStaticNearShore() {
      return NEAR_SHORE;
   }

   @Unique
   @Override
   public int getStaticLakeShore() {
      return LAKE_SHORE;
   }

   @Unique
   @Override
   public int getStaticPelagicZone() {
      return PELAGIC_ZONE;
   }

   @Unique
   @Override
   public int getStaticSeamounts() {
      return SEAMOUNTS;
   }

   @Unique
   @Override
   public int getStaticGuyots() {
      return GUYOTS;
   }

   @Unique
   @Override
   public int getStaticAtoll() {
      return ATOLL;
   }

   @Unique
   @Override
   public int getStaticBarrierReef() {
      return BARRIER_REEF;
   }

   @Unique
   @Override
   public int getStaticLagoon() {
      return LAGOON;
   }

   @Unique
   @Override
   public int getStaticSawtoothCliffs() {
      return SAWTOOTH_CLIFFS;
   }

   @Unique
   @Override
   public int getStaticTablelands() {
      return TABLELANDS;
   }

   @Unique
   @Override
   public int getStaticPitlands() {
      return PITLANDS;
   }

   @Unique
   @Override
   public int getStaticFracturedMistyPeaks() {
      return FRACTURED_MISTY_PEAKS;
   }

   @Overwrite(
      remap = false
   )
   public static BiomeExtension getFromLayerId(int id) {
      BiomeExtension v = BIOME_LAYERS[id];
      if (v == null) {
         throw new NullPointerException("Layer id = " + id + " returned null!");
      } else {
         return v;
      }
   }

   @Overwrite(
      remap = false
   )
   public static AreaFactory createOverworldBiomeLayer(long seed, IArtist<TypedAreaFactory<Plate>> plateArtist, IArtist<AreaFactory> layerArtist) {
      Random random = RandomSource.create(seed);
      TypedAreaFactory<Plate> plateLayer = new PlateGenerationLayer(new Cellular2D((long)random.nextInt()).spread(0.2F), 40).apply(random.nextLong());
      plateArtist.draw("plate_generation", 1, plateLayer);
      plateLayer = TypedZoomLayer.fuzzy().apply(random.nextLong(), plateLayer);
      plateArtist.draw("plate_generation", 2, plateLayer);
      AreaFactory mainLayer = PlateBoundaryLayer.INSTANCE.apply(random.nextLong(), plateLayer);
      layerArtist.draw("plate_boundary", 1, mainLayer);
      mainLayer = SmoothLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      layerArtist.draw("plate_boundary", 2, mainLayer);
      mainLayer = PlateBoundaryModifierLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      layerArtist.draw("plate_boundary", 3, mainLayer);
      mainLayer = PlateBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      layerArtist.draw("biomes", 1, mainLayer);

      for (int i = 0; i < Config.COMMON.landBiomeSizes.get(); i++) {
         mainLayer = ZoomBiomesLayer.NORMAL_LAND.apply(random.nextLong(), mainLayer);
      }

      AreaFactory inlandLayer = InlandLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      inlandLayer = ZoomLayer.NORMAL.apply(1001L, inlandLayer);

      for (int i = 0; i < Config.COMMON.earlyBiomeSizes.get(); i++) {
         inlandLayer = ZoomLayer.NORMAL.apply((long)(1001 + i), inlandLayer);
      }

      for (int i = 0; i < Config.COMMON.midBiomeSizes.get(); i++) {
         inlandLayer = ZoomLayer.NORMAL.apply((long)(1002 + i), inlandLayer);
      }

      AreaFactory oceanLayer = OceanLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      oceanLayer = ZoomLayer.NORMAL.apply(1001L, oceanLayer);

      for (int i = 0; i < Config.COMMON.earlyBiomeSizes.get(); i++) {
         oceanLayer = ZoomLayer.NORMAL.apply((long)(1001 + i), oceanLayer);
      }

      for (int i = 0; i < Config.COMMON.midBiomeSizes.get(); i++) {
         oceanLayer = ZoomLayer.NORMAL.apply((long)(1002 + i), oceanLayer);
      }

      AreaFactory lakeLayer = AddLakesLayer.LARGE.apply(random.nextLong(), inlandLayer);
      lakeLayer = ZoomLayer.NORMAL.apply(1002L, lakeLayer);
      lakeLayer = AddLakesLayer.SMALL.apply(random.nextLong(), lakeLayer);
      lakeLayer = ZoomLayer.NORMAL.apply(1003L, lakeLayer);
      AreaFactory calderaLayer = AddCalderasLayer.LARGE.apply(random.nextLong(), inlandLayer);
      calderaLayer = ZoomLayer.NORMAL.apply(1002L, calderaLayer);
      calderaLayer = AddCalderasLayer.SMALL.apply(random.nextLong(), calderaLayer);
      calderaLayer = ZoomLayer.NORMAL.apply(1003L, calderaLayer);
      AreaFactory puyMountainsLayer = AddPuyMountainsLayer.LARGE.apply(random.nextLong(), inlandLayer);
      puyMountainsLayer = ZoomLayer.NORMAL.apply(1002L, puyMountainsLayer);
      puyMountainsLayer = AddPuyMountainsLayer.SMALL.apply(random.nextLong(), puyMountainsLayer);
      puyMountainsLayer = ZoomLayer.NORMAL.apply(1003L, puyMountainsLayer);
      AreaFactory mesaPlateauLayer = AddMesaPlateauLayer.LARGE.apply(random.nextLong(), inlandLayer);
      mesaPlateauLayer = ZoomLayer.NORMAL.apply(1002L, mesaPlateauLayer);
      mesaPlateauLayer = AddMesaPlateauLayer.SMALL.apply(random.nextLong(), mesaPlateauLayer);
      mesaPlateauLayer = ZoomLayer.NORMAL.apply(1003L, mesaPlateauLayer);
      mainLayer = OceanBorderLayerTFCF.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanBorderLayerTFCF.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = ZoomLayer.NORMAL.apply(1001L, mainLayer);
      mainLayer = ArchipelagoLayerTFCF.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = ReefBorderLayerTFCF.INSTANCE.apply(random.nextLong(), mainLayer);

      for (int i = 0; i < Config.COMMON.earlyBiomeSizes.get(); i++) {
         mainLayer = ZoomLayer.NORMAL.apply((long)(1001 + i), mainLayer);
      }

      mainLayer = EdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);

      for (int i = 0; i < Config.COMMON.midBiomeSizes.get(); i++) {
         mainLayer = ZoomLayer.NORMAL.apply((long)(1002 + i), mainLayer);
      }

      mainLayer = MergeLakeLayer.INSTANCE.apply(random.nextLong(), mainLayer, lakeLayer);
      mainLayer = MergeCalderaLayer.INSTANCE.apply(random.nextLong(), mainLayer, calderaLayer);
      mainLayer = MergePuyMountainsLayer.INSTANCE.apply(random.nextLong(), mainLayer, puyMountainsLayer);
      mainLayer = MergeMesaPlateauLayer.INSTANCE.apply(random.nextLong(), mainLayer, mesaPlateauLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = OceanEdgeBiomeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = PlateauCliffsLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = ShoreLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = LakeEdgeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = LakeEdgeLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = NearShoreLayer.INSTANCE.apply(random.nextLong(), mainLayer);

      for (int i = 0; i < Config.COMMON.lateBiomeSizes.get(); i++) {
         mainLayer = ZoomLayer.NORMAL.apply((long)(1003 + i), mainLayer);
      }

      mainLayer = SmoothLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      layerArtist.draw("biomes", 34, mainLayer);
      ShoreDunes.Context shoreDunesContext = new ShoreDunes.Context(createEarlyPlateLayers(seed), seed + random.nextLong(), 0.6F, 0.8F, 14, 0.2F);
      mainLayer = new MergeShoreDunesLayer(shoreDunesContext).apply(seed + random.nextLong(), mainLayer);
      CoastalCliffs.Context cliffContext = new CoastalCliffs.Context(createEarlyPlateLayers(seed), seed + 2L, 0.5F, 0.8F, 14, 0.2F);
      mainLayer = new MergeCoastalCliffsLayer(cliffContext).apply(seed + 2L, mainLayer);
      mainLayer = CoastalCliffsShoreLayer.INSTANCE.apply(random.nextLong(), mainLayer);

      for (int i = 0; i < 4; i++) {
         mainLayer = ExtraDunesShoreLayer.INSTANCE.apply(seed + random.nextLong(), mainLayer);
         mainLayer = EdgeDunesShoreLayer.INSTANCE.apply(seed + random.nextLong(), mainLayer);
      }

      Chasm.Context chasmContext = new Chasm.Context(createEarlyPlateLayers(seed), seed + 5L, 0.58F, 0.7F, 16, 0.9F);
      mainLayer = new MergeChasmsLayer(chasmContext).apply(seed + 5L, mainLayer);
      TypedAreaFactory<Plate> riverLayer = createEarlyPlateLayers(seed);
      Watershed.Context riverContext = new Watershed.Context(riverLayer, seed, 0.5F, 0.8F, 14, 0.2F);
      WatershedBank.Context riverbankContext = new WatershedBank.Context(riverLayer, seed, 0.5F, 0.8F, 14, 0.2F);
      mainLayer = new TFCFMergeRiverLayer(riverContext, seed).apply(seed, mainLayer);
      mainLayer = new MergeRiverBanksLayer(riverbankContext, riverContext, seed).apply(seed, mainLayer);
      mainLayer = new TFCFMergeRiverLayer(riverContext, seed).apply(seed, mainLayer);
      mainLayer = EdgeRiverbankLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      mainLayer = PlateauCliffsLayer.INSTANCE.apply(random.nextLong(), mainLayer);
      layerArtist.draw("biomes", 53, mainLayer);
      return mainLayer;
   }

   @Shadow
   public static TypedAreaFactory<Plate> createEarlyPlateLayers(long seed) {
      Random random = RandomSource.create(seed);
      TypedAreaFactory<Plate> plateLayer = new PlateGenerationLayer(new Cellular2D((long)random.nextInt()).spread(0.2F), 40).apply(random.nextLong());
      return BiasedLandPlateZoomLayer.INSTANCE.apply(random.nextLong(), plateLayer);
   }

   @Shadow
   public static AreaFactory createOverworldPlateTectonicInfoLayer(long seed) {
      Random random = RandomSource.create(seed);
      TypedAreaFactory<Plate> plateLayer = new PlateGenerationLayer(new Cellular2D((long)random.nextInt()).spread(0.2F), 40).apply(random.nextLong());
      plateLayer = TypedZoomLayer.fuzzy().apply(random.nextLong(), plateLayer);
      AreaFactory mainLayer = PlateBoundaryLayer.INSTANCE.apply(random.nextLong(), plateLayer);

      for (int i = 0; i < 5; i++) {
         mainLayer = ZoomLayer.NORMAL.apply(random.nextLong(), mainLayer);
      }

      return mainLayer;
   }

   @Shadow
   public static AreaFactory createOverworldRockLayer(long seed, int layerScale, int rockCount) {
      Random random = RandomSource.create(seed);
      AreaFactory layer = new RockLayer(rockCount).apply(random.nextLong());
      layer = new RandomizeNeighborsLayer(rockCount).apply(random.nextLong(), layer);

      for (int i = 0; i < 2; i++) {
         layer = ZoomLayer.NORMAL.apply(random.nextLong(), layer);
         layer = ZoomLayer.NORMAL.apply(random.nextLong(), layer);
         layer = SmoothLayer.INSTANCE.apply(random.nextLong(), layer);
      }

      for (int i = 0; i < layerScale; i++) {
         layer = ZoomLayer.NORMAL.apply(random.nextLong(), layer);
      }

      return layer;
   }

   @Shadow
   public static boolean isContinental(int value) {
      return value == 1 || value == 2 || value == 3;
   }

   @Overwrite(
      remap = false
   )
   public static boolean hasShore(int value) {
      return value != LOW_CANYONS && value != CANYONS && value != MANGROVES && value != RIVER_EDGE && value != RIVERBANK && value != RIVER;
   }

   @Overwrite(
      remap = false
   )
   public static int shoreFor(int value) {
      if (value != PLATEAU && value != PUY_MOUNTAINS && value != CALDERAS && !isMountains(value)
         || value == MOUNTAINS && value == VOLCANIC_MOUNTAINS && value == ALPINE_MOUNTAINS) {
         if (value == COASTAL_CLIFFS) {
            return GRAVEL_SHORE;
         } else if (value == ALPINE_MOUNTAINS) {
            return ALPINE_HIGHLANDS;
         } else if (value == MOUNTAINS) {
            return OCEANIC_MOUNTAINS;
         } else if (value == VOLCANIC_MOUNTAINS) {
            return VOLCANIC_OCEANIC_MOUNTAINS;
         } else if (value == LOWLANDS || value == WETLANDS || value == MARSHES || value == SWAMPS || value == PEAT_BOG) {
            return MANGROVES;
         } else if (value == SEAMOUNTS || value == PELAGIC_ZONE) {
            return DEEP_OCEAN_TRENCH;
         } else if (value == GUYOTS) {
            return DEEP_OCEAN;
         } else {
            return value != OCEANIC_MOUNTAINS && value != VOLCANIC_OCEANIC_MOUNTAINS ? SHORE : GRAVEL_SHORE;
         }
      } else {
         return COASTAL_CLIFFS;
      }
   }

   @Overwrite(
      remap = false
   )
   public static boolean hasLake(int value) {
      return !isOcean(value) && value != BADLANDS && value != CALDERAS && value != NEAR_SHORE && value != ATOLL;
   }

   @Overwrite(
      remap = false
   )
   public static int lakeFor(int value) {
      if (value == ALPINE_MOUNTAINS) {
         return VOLCANIC_MOUNTAIN_LAKE;
      } else if (value == THERMAL_CANYONS) {
         return THERMAL_CANYONS;
      } else if (value == MOUNTAINS) {
         return MOUNTAIN_LAKE;
      } else if (value == VOLCANIC_MOUNTAINS) {
         return VOLCANIC_MOUNTAIN_LAKE;
      } else if (value == OLD_MOUNTAINS) {
         return OLD_MOUNTAIN_LAKE;
      } else if (value == OCEANIC_MOUNTAINS) {
         return OCEANIC_MOUNTAIN_LAKE;
      } else if (value == VOLCANIC_OCEANIC_MOUNTAINS) {
         return VOLCANIC_OCEANIC_MOUNTAIN_LAKE;
      } else {
         return value == PLATEAU ? PLATEAU_LAKE : LAKE;
      }
   }

   @Overwrite(
      remap = false
   )
   public static boolean hasRiver(int value) {
      return !isOcean(value) && !isLake(value);
   }

   @Overwrite(
      remap = false
   )
   public static int riverFor(int value) {
      if (value == CALDERAS || value == CANYON_RIVER) {
         return CANYON_RIVER;
      } else if (value == ALPINE_MOUNTAINS || value == ALPINE_MOUNTAIN_RIVER) {
         return ALPINE_MOUNTAIN_RIVER;
      } else if (value == MOUNTAINS || value == ALPINE_HIGHLANDS || value == MOUNTAIN_RIVER) {
         return MOUNTAIN_RIVER;
      } else if (value == VOLCANIC_MOUNTAINS || value == VOLCANIC_MOUNTAIN_RIVER) {
         return VOLCANIC_MOUNTAIN_RIVER;
      } else if (value == OLD_MOUNTAINS || value == OLD_MOUNTAIN_RIVER) {
         return OLD_MOUNTAIN_RIVER;
      } else if (value == OCEANIC_MOUNTAINS || value == OCEANIC_MOUNTAIN_RIVER) {
         return OCEANIC_MOUNTAIN_RIVER;
      } else if (value == VOLCANIC_OCEANIC_MOUNTAINS || value == VOLCANIC_OCEANIC_MOUNTAIN_RIVER) {
         return VOLCANIC_OCEANIC_MOUNTAIN_RIVER;
      } else {
         return isOceanOrMarker(value) ? value : RIVER;
      }
   }

   @Overwrite(
      remap = false
   )
   public static boolean isOcean(int value) {
      return value == OCEAN
         || value == DEEP_OCEAN
         || value == DEEP_OCEAN_TRENCH
         || value == OCEAN_REEF
         || value == PELAGIC_ZONE
         || value == SEAMOUNTS
         || value == GUYOTS
         || value == NEAR_SHORE;
   }

   @Overwrite(
      remap = false
   )
   public static boolean isOceanOrMarker(int value) {
      return isOcean(value)
         || value == OCEAN_OCEAN_CONVERGING_MARKER
         || value == OCEAN_OCEAN_DIVERGING_MARKER
         || value == OCEAN_REEF_MARKER
         || value == PELAGIC_ZONE_MARKER
         || value == SEAMOUNTS_MARKER
         || value == GUYOTS_MARKER;
   }

   @Overwrite(
      remap = false
   )
   public static boolean isLake(int value) {
      return value == LAKE
         || value == OCEANIC_MOUNTAIN_LAKE
         || value == OLD_MOUNTAIN_LAKE
         || value == MOUNTAIN_LAKE
         || value == VOLCANIC_OCEANIC_MOUNTAIN_LAKE
         || value == VOLCANIC_MOUNTAIN_LAKE
         || value == PLATEAU_LAKE;
   }

   @Overwrite(
      remap = false
   )
   public static boolean isRiver(int value) {
      return value == RIVER
         || value == OCEANIC_MOUNTAIN_RIVER
         || value == OLD_MOUNTAIN_RIVER
         || value == MOUNTAIN_RIVER
         || value == VOLCANIC_OCEANIC_MOUNTAIN_RIVER
         || value == VOLCANIC_MOUNTAIN_RIVER
         || value == CANYON_RIVER
         || value == ALPINE_MOUNTAIN_RIVER;
   }

   @Overwrite(
      remap = false
   )
   public static boolean isMountains(int value) {
      return value == MOUNTAINS
         || value == OCEANIC_MOUNTAINS
         || value == OLD_MOUNTAINS
         || value == VOLCANIC_MOUNTAINS
         || value == VOLCANIC_OCEANIC_MOUNTAINS
         || value == ALPINE_MOUNTAINS
         || value == ALPINE_HIGHLANDS
         || value == THERMAL_CANYONS
         || value == MESA_PLATEAU
         || value == SHRUBLANDS
         || value == MISTY_PEAKS
         || value == VINICUNCA_MOUNTAINS
         || value == SAWTOOTH_CLIFFS
         || value == TABLELANDS
         || value == PITLANDS
         || value == FRACTURED_MISTY_PEAKS
         || value == COASTAL_CLIFFS;
   }

   @Overwrite(
      remap = false
   )
   public static boolean isLow(int value) {
      return value == TFCLayers.PLAINS
         || value == TFCLayers.HILLS
         || value == TFCLayers.LOW_CANYONS
         || value == TFCLayers.LOWLANDS
         || value == GRASSLANDS
         || value == WETLANDS
         || value == MARSHES
         || value == SWAMPS
         || value == STEPPES;
   }

   @Shadow
   public static int register() {
      return register(() -> null);
   }

   @Shadow
   public static int register(Supplier<BiomeExtension> variants) {
      int index = BIOME_LAYER_INDEX.getAndIncrement();
      if (index >= BIOME_LAYERS.length) {
         throw new IllegalStateException("Tried to register layer id " + index + " but only had space for " + BIOME_LAYERS.length + " layers");
      } else {
         BIOME_LAYERS[index] = Helpers.BOOTSTRAP_ENVIRONMENT ? null : variants.get();
         return index;
      }
   }
}
