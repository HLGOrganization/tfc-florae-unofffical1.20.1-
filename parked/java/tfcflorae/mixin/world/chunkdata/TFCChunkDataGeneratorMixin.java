package tfcflorae.mixin.world.chunkdata;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.IArtist;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataGenerator;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.chunkdata.PlateTectonicsClassification;
import net.dries007.tfc.world.chunkdata.TFCChunkDataGenerator;
import net.dries007.tfc.world.layer.TFCLayers;
import net.dries007.tfc.world.layer.framework.ConcurrentArea;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.settings.ClimateSettings;
import net.dries007.tfc.world.settings.RockLayerSettings;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tfcflorae.interfaces.ChunkDataInterface;

@Mixin({TFCChunkDataGenerator.class})
public class TFCChunkDataGeneratorMixin implements ChunkDataGenerator, ChunkDataInterface {
   @Unique
   private static TFCBiomes staticBiomes = new TFCBiomes();
   @Shadow
   private final ConcurrentArea<ForestType> forestTypeLayer;
   @Shadow
   private final Noise2D temperatureNoise;
   @Shadow
   private final Noise2D rainfallNoise;
   @Shadow
   private final Noise2D layerHeightNoise;
   @Shadow
   private final Noise2D forestWeirdnessNoise;
   @Shadow
   private final Noise2D forestDensityNoise;
   @Shadow
   private final ConcurrentArea<PlateTectonicsClassification> plateTectonicsInfo;

   private TFCChunkDataGeneratorMixin(
      long worldSeed, RockLayerSettings rockLayerSettings, ClimateSettings temperatureSettings, ClimateSettings rainfallSettings
   ) {
      Random random = RandomSource.create(worldSeed);
      random.setSeed(worldSeed ^ random.nextLong());
      this.layerHeightNoise = new OpenSimplex2D(random.nextInt()).octaves(2).scaled(-10.0F, 10.0F).spread(0.03F);
      this.temperatureNoise = ((x, z) -> Helpers.triangle(
               1.0F,
               0.0F,
               1.0F / (4.0F * (float)temperatureSettings.scale()),
               temperatureSettings.endlessPoles() ? Mth.clamp(z, (float)(-temperatureSettings.scale()), (float)temperatureSettings.scale()) : z
            ))
         .scaled(-20.0F, 30.0F)
         .add(new OpenSimplex2D(random.nextInt()).octaves(2).spread(12.0F / (float)temperatureSettings.scale()).scaled(-2.0F, 2.0F));
      this.rainfallNoise = ((x, z) -> Helpers.triangle(
               1.0F,
               0.0F,
               1.0F / (4.0F * (float)rainfallSettings.scale()),
               rainfallSettings.endlessPoles() ? Mth.clamp(x, (float)(-rainfallSettings.scale()), (float)rainfallSettings.scale()) : x
            ))
         .scaled(0.0F, 500.0F)
         .add(new OpenSimplex2D(random.nextInt()).octaves(2).spread(12.0F / (float)rainfallSettings.scale()).scaled(-50.0F, 50.0F))
         .clamped(0.0F, 500.0F);
      this.forestTypeLayer = new ConcurrentArea(TFCLayers.createOverworldForestLayer(random.nextLong(), IArtist.nope()), ForestType::valueOf);
      this.forestWeirdnessNoise = new OpenSimplex2D(random.nextInt()).octaves(4).spread(0.0025F).map(x -> 1.1F * Math.abs(x)).clamped(0.0F, 1.0F);
      this.forestDensityNoise = new OpenSimplex2D(random.nextInt()).octaves(4).spread(0.0025F).scaled(-0.2F, 1.2F).clamped(0.0F, 1.0F);
      this.plateTectonicsInfo = new ConcurrentArea(TFCLayers.createOverworldPlateTectonicInfoLayer(worldSeed), PlateTectonicsClassification::valueOf);
   }

   @Shadow
   public void generate(ChunkData data) {
   }

   @Unique
   @Override
   public Noise2D getTemperatureNoise() {
      return this.temperatureNoise;
   }

   @Unique
   @Override
   public Noise2D getRainfallNoise() {
      return this.rainfallNoise;
   }

   @Unique
   @Override
   public Noise2D getLayerHeightNoise() {
      return this.layerHeightNoise;
   }

   @Unique
   @Override
   public Noise2D getForestWeirdnessNoise() {
      return this.forestWeirdnessNoise;
   }

   @Unique
   @Override
   public Noise2D getForestDensityNoise() {
      return this.forestDensityNoise;
   }
}
