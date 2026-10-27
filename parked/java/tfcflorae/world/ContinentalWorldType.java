package tfcflorae.world;

import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.TFCWorldType;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class ContinentalWorldType {
   public static final DeferredRegister<ForgeWorldPreset> WORLD_TYPES = DeferredRegister.create(Keys.WORLD_TYPES, "tfc");
   public static final RegistryObject<ForgeWorldPreset> WORLD_TYPE = WORLD_TYPES.register(
      "continental",
      () -> new ForgeWorldPreset(
            (registries, seed, settings) -> {
               Registry<StructureSet> structures = registries.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
               Registry<NoiseParameters> noiseParameters = registries.registryOrThrow(Registry.NOISE_REGISTRY);
               Registry<NoiseGeneratorSettings> noiseGeneratorSettings = registries.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
               Registry<Biome> biomes = registries.registryOrThrow(Registry.BIOME_REGISTRY);
               return TFCChunkGenerator.defaultChunkGenerator(
                  structures, noiseParameters, noiseGeneratorSettings.getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD), biomes, seed
               );
            }
         )
   );

   public ContinentalWorldType() {
   }

   public static void overrideDefaultWorldType() {
      if ((Boolean)TFCConfig.COMMON.setTFCWorldTypeAsDefault.get()
         && (
            ((String)ForgeConfig.COMMON.defaultWorldType.get()).equals("default")
               || ((String)ForgeConfig.COMMON.defaultWorldType.get()).equals(TFCWorldType.WORLD_TYPE.getId().toString())
         )) {
         ForgeConfig.COMMON.defaultWorldType.set(WORLD_TYPE.getId().toString());
      }
   }
}
