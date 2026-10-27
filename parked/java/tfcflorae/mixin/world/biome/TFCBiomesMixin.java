package tfcflorae.mixin.world.biome;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.biome.BiomeBridge;
import net.dries007.tfc.world.biome.BiomeBuilder;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.biome.BiomeExtension.Group;
import net.dries007.tfc.world.surface.builder.BadlandsSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.LowlandsSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.MountainSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.OceanSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.ShoreSurfaceBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tfcflorae.interfaces.TFCBiomesMixinInterface;
import tfcflorae.world.biome.TFCFBiomeNoise;
import tfcflorae.world.surface.builder.AtollSurfaceBuilder;
import tfcflorae.world.surface.builder.DuneShoreSurfaceBuilder;
import tfcflorae.world.surface.builder.ForestSurfaceBuilder;
import tfcflorae.world.surface.builder.GrassSurfaceBuilder;
import tfcflorae.world.surface.builder.GravelShoreSurfaceBuilder;
import tfcflorae.world.surface.builder.GravelSurfaceBuilder;
import tfcflorae.world.surface.builder.LakeShoreSurfaceBuilder;
import tfcflorae.world.surface.builder.LowMountainSurfaceBuilder;
import tfcflorae.world.surface.builder.MangroveSurfaceBuilder;
import tfcflorae.world.surface.builder.RainbowMountainsSurfaceBuilder;
import tfcflorae.world.surface.builder.RockyDirtSurfaceBuilder;
import tfcflorae.world.surface.builder.SubSoilSurfaceBuilder;
import tfcflorae.world.surface.builder.TFCFBadlandsSurfaceBuilder;
import tfcflorae.world.surface.builder.TFCFNormalSurfaceBuilder;
import tfcflorae.world.surface.builder.TopBadlandsSurfaceBuilder;

@Mixin({TFCBiomes.class})
public final class TFCBiomesMixin implements TFCBiomesMixinInterface {
   @Shadow
   public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, "tfc");
   @Shadow
   private static final Map<ResourceKey<Biome>, BiomeExtension> EXTENSIONS = new IdentityHashMap<>();
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension OCEAN = register(
      "ocean",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.ocean(seed, -26, -12))
         .surface(OceanSurfaceBuilder.INSTANCE)
         .aquiferHeightOffset(-24.0)
         .salty()
         .group(Group.OCEAN)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension OCEAN_REEF = register(
      "ocean_reef",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.ocean(seed, -16, -8))
         .surface(OceanSurfaceBuilder.INSTANCE)
         .aquiferHeightOffset(-24.0)
         .salty()
         .group(Group.OCEAN)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension DEEP_OCEAN = register(
      "deep_ocean",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.ocean(seed, -100, -30))
         .surface(OceanSurfaceBuilder.INSTANCE)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .aquiferHeightOffset(-24.0)
         .group(Group.OCEAN)
         .salty()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension DEEP_OCEAN_TRENCH = register(
      "deep_ocean_trench",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.oceanRidge(seed, -128, -30))
         .surface(OceanSurfaceBuilder.INSTANCE)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .aquiferHeightOffset(-24.0)
         .group(Group.OCEAN)
         .salty()
   );
   @Unique
   @Final
   private static final BiomeExtension PELAGIC_ZONE = register(
      "abyssal_plains",
      BiomeBuilder.builder().heightmap(seed -> BiomeNoise.ocean(seed, -250, -168)).surface(OceanSurfaceBuilder.INSTANCE).group(Group.OCEAN).salty()
   );
   @Unique
   @Final
   private static final BiomeExtension SEAMOUNTS = register(
      "seamounts",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.seamounts(seed, -110, -90))
         .surface(OceanSurfaceBuilder.INSTANCE)
         .group(Group.OCEAN)
         .salty()
         .volcanoes(3, -15, 50, 0)
   );
   @Unique
   @Final
   private static final BiomeExtension GUYOTS = register(
      "guyots",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.seamounts(seed, -200, -90))
         .surface(OceanSurfaceBuilder.INSTANCE)
         .group(Group.OCEAN)
         .salty()
         .volcanoes(1, -15, 50, 0)
   );
   @Unique
   @Final
   private static final BiomeExtension BARRIER_REEF = register(
      "barrier_reef",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 1, 20))
         .surface(
            AtollSurfaceBuilder.create(
               SubSoilSurfaceBuilder.create(
                  RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
               )
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .group(Group.OCEAN)
         .volcanoes(1, 25, 30, 5)
         .salty()
   );
   @Unique
   @Final
   private static final BiomeExtension LAGOON = register(
      "lagoon",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.ocean(seed, -15, -8))
         .surface(OceanSurfaceBuilder.INSTANCE)
         .aquiferHeightOffset(-24.0)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .salty()
         .group(Group.OCEAN)
   );
   @Unique
   @Final
   private static final BiomeExtension ATOLL = register(
      "atoll",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.dunes(seed, 1, 5))
         .surface(
            AtollSurfaceBuilder.create(
               SubSoilSurfaceBuilder.create(
                  RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE)))
               )
            )
         )
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .salty()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension PLAINS = register(
      "plains",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, 4, 10))
         .surface(
            SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         )
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension HILLS = register(
      "hills",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, -5, 16))
         .surface(
            SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         )
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension LOWLANDS = register(
      "lowlands",
      BiomeBuilder.builder()
         .heightmap(TFCFBiomeNoise::lowlandsNew)
         .surface(SubSoilSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(LowlandsSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .aquiferHeightOffset(-8.0)
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension LOW_CANYONS = register(
      "low_canyons",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.canyons(seed, -8, 21))
         .surface(
            SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         )
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .aquiferHeightOffset(-16.0)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension GRASSLANDS = register(
      "grasslands",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, 0, 2))
         .surface(SubSoilSurfaceBuilder.create(GrassSurfaceBuilder.create(ForestSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension WETLANDS = register(
      "wetlands",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.canyons(seed, -4, 2))
         .surface(SubSoilSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(LowlandsSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .aquiferHeightOffset(-8.0)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension MARSHES = register(
      "marshes",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, -3, -2))
         .surface(SubSoilSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(LowlandsSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .aquiferHeightOffset(-8.0)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension SWAMPS = register(
      "swamps",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, -8, 2))
         .surface(SubSoilSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(LowlandsSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .aquiferHeightOffset(-8.0)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension MANGROVES = register(
      "mangroves",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, -3, -1))
         .surface(MangroveSurfaceBuilder.create(DuneShoreSurfaceBuilder.create(ShoreSurfaceBuilder.INSTANCE)))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .aquiferHeightOffset(-8.0)
         .group(Group.OCEAN)
         .salty()
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension PUY_MOUNTAINS = register(
      "puy_mountains",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 8, 40))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .aquiferHeightOffset(-32.0)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension BRYCE_CANYONS = register(
      "bryce_canyons",
      BiomeBuilder.builder()
         .heightmap(TFCFBiomeNoise::grandCanyon)
         .surface(SubSoilSurfaceBuilder.create(BadlandsSurfaceBuilder.INVERTED))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension MESA_HILLS = register(
      "mesa_hills",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.canyonCliffs(seed, -10, 54))
         .surface(SubSoilSurfaceBuilder.create(BadlandsSurfaceBuilder.INVERTED))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension BADLANDS_PLATEAU = register(
      "badlands_plateau",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.mountainsBadlands(seed, 0, 20))
         .surface(SubSoilSurfaceBuilder.create(TFCFBadlandsSurfaceBuilder.INVERTED))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension CANYON_RIVER = register(
      "canyon_river",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.badlands(seed, 21, 32))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(BiomeNoise::undergroundRivers)
         .group(Group.RIVER)
   );
   @Unique
   @Final
   private static final BiomeExtension ALPINE_MOUNTAIN_RIVER = register(
      "alpine_mountain_river",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 72, 100))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(BiomeNoise::undergroundRivers)
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .group(Group.RIVER)
   );
   @Unique
   @Final
   private static final BiomeExtension ALPINE_MOUNTAINS = register(
      "alpine_mountains",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 72, 100))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .aquiferHeightOffset(64.0)
         .volcanoes(4, 28, 100, 50)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension ALPINE_HIGHLANDS = register(
      "alpine_highlands",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.canyons(seed, 32, 100))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .aquiferHeightOffset(48.0)
         .volcanoes(10, 10, 40, 32)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension ROLLING_HIGHLANDS = register(
      "rolling_highlands",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.badlands(seed, -10, 64))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension CALDERAS = register(
      "calderas",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.badlands(seed, 21, 32))
         .surface(SubSoilSurfaceBuilder.create(LowMountainSurfaceBuilder.INSTANCE))
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .group(Group.LAKE)
         .aquiferHeightOffset(24.0)
         .volcanoes(5, -12, 20, 0)
   );
   @Unique
   @Final
   private static final BiomeExtension GRAVEL_SHORE = register(
      "gravel_shore",
      BiomeBuilder.builder()
         .heightmap(BiomeNoise::shore)
         .surface(GravelShoreSurfaceBuilder.INSTANCE)
         .aquiferHeightOffset(-16.0)
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .group(Group.OCEAN)
         .salty()
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension THERMAL_CANYONS = register(
      "thermal_canyons",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.canyons(seed, -10, 18))
         .surface(
            SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         )
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .spawnable()
         .volcanoes(2, -12, 20, 0)
         .aquiferHeightOffset(-16.0)
   );
   @Unique
   @Final
   private static final BiomeExtension MESA_PLATEAU = register(
      "mesa_plateau",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.canyons(seed, 60, 66))
         .surface(
            SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         )
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension PEAT_BOG = register(
      "peat_bog",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.canyons(seed, -4, 1))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(LowlandsSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension RIVERBANK = register(
      "riverbank",
      BiomeBuilder.builder()
         .heightmap(TFCFBiomeNoise::riverShore)
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(LowlandsSurfaceBuilder.INSTANCE))))
         .aquiferHeightOffset(-16.0)
         .group(Group.OCEAN)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension RIVER_EDGE = register(
      "river_edge",
      BiomeBuilder.builder()
         .noise(TFCFBiomeNoise::riverBankSampler)
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(LowlandsSurfaceBuilder.INSTANCE))))
         .aquiferHeight(h -> 47.0)
         .group(Group.RIVER)
   );
   @Unique
   @Final
   private static final BiomeExtension CHASMS = register(
      "chasms",
      BiomeBuilder.builder()
         .noise(TFCFBiomeNoise::chasmSampler)
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .aquiferHeight(h -> 47.0)
         .group(Group.RIVER)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension PLATEAU_CLIFFS = register(
      "plateau_cliffs",
      BiomeBuilder.builder()
         .noise(TFCFBiomeNoise::plateauCliffsSampler)
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .aquiferHeight(h -> 47.0)
         .group(Group.RIVER)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension STEPPES = register(
      "steppes",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, 2, 5))
         .surface(SubSoilSurfaceBuilder.create(GrassSurfaceBuilder.create(ForestSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension SHRUBLANDS = register(
      "shrublands",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.canyons(seed, 10, 38))
         .surface(SubSoilSurfaceBuilder.create(GrassSurfaceBuilder.create(ForestSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension MOORLANDS = register(
      "moorlands",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.moorlands(seed, 10, 42))
         .surface(SubSoilSurfaceBuilder.create(GrassSurfaceBuilder.create(ForestSurfaceBuilder.create(TFCFNormalSurfaceBuilder.ROCKY))))
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension COASTAL_CLIFFS = register(
      "coastal_cliffs",
      BiomeBuilder.builder()
         .noise(TFCFBiomeNoise::coastalCliffsSampler)
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension SHORE_DUNES = register(
      "shore_dunes",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.dunes(seed, 3, 18))
         .surface(SubSoilSurfaceBuilder.create(DuneShoreSurfaceBuilder.create(ShoreSurfaceBuilder.INSTANCE)))
         .aquiferHeightOffset(-16.0)
         .group(Group.OCEAN)
         .salty()
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension VINICUNCA_MOUNTAINS = register(
      "vinicunca_mountains",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.rainbowMountains(seed, 30, 50))
         .surface(
            SubSoilSurfaceBuilder.create(
               RainbowMountainsSurfaceBuilder.create(
                  ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
               )
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension NEAR_SHORE = register(
      "near_shore",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.dunes(seed, -8, 2))
         .surface(ShoreSurfaceBuilder.INSTANCE)
         .aquiferHeightOffset(-16.0)
         .group(Group.OCEAN)
         .salty()
   );
   @Unique
   @Final
   private static final BiomeExtension LAKE_SHORE = register(
      "lake_shore",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.lakeShore(seed, -8, 2))
         .surface(LakeShoreSurfaceBuilder.INSTANCE)
         .aquiferHeightOffset(-16.0)
         .group(Group.LAKE)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension SAWTOOTH_CLIFFS = register(
      "sawtooth_cliffs",
      BiomeBuilder.builder()
         .heightmap(TFCFBiomeNoise::sharpHills)
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(TFCFNormalSurfaceBuilder.ROCKY)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension TABLELANDS = register(
      "tablelands",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.tablelands(seed, 1, 30))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension PITLANDS = register(
      "pitlands",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.pitlands(seed, -8, 28))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCavesUpper3)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension MISTY_PEAKS = register(
      "misty_peaks",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.mistyPeaksNew(seed, 7, 28))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .spawnable()
   );
   @Unique
   @Final
   private static final BiomeExtension FRACTURED_MISTY_PEAKS = register(
      "fractured_misty_peaks",
      BiomeBuilder.builder()
         .heightmap(seed -> TFCFBiomeNoise.mistyPeaksFractured(seed, 7, 28))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension ROLLING_HILLS = register(
      "rolling_hills",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, -5, 28))
         .surface(
            SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         )
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension BADLANDS = register(
      "badlands",
      BiomeBuilder.builder()
         .heightmap(BiomeNoise::badlands)
         .surface(
            TopBadlandsSurfaceBuilder.create(
               SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(BadlandsSurfaceBuilder.NORMAL))), false
            )
         )
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension INVERTED_BADLANDS = register(
      "inverted_badlands",
      BiomeBuilder.builder()
         .heightmap(BiomeNoise::bryceCanyon)
         .surface(TopBadlandsSurfaceBuilder.create(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(BadlandsSurfaceBuilder.INVERTED)), true))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension PLATEAU = register(
      "plateau",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension CANYONS = register(
      "canyons",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.canyons(seed, -2, 40))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .volcanoes(6, 14, 30, 28)
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension MOUNTAINS = register(
      "mountains",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 10, 70))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension OLD_MOUNTAINS = register(
      "old_mountains",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 16, 40))
         .surface(
            SubSoilSurfaceBuilder.create(
               RockyDirtSurfaceBuilder.create(ForestSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE)))
            )
         )
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension OCEANIC_MOUNTAINS = register(
      "oceanic_mountains",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .aquiferHeightOffset(-8.0)
         .salty()
         .spawnable()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension VOLCANIC_MOUNTAINS = register(
      "volcanic_mountains",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 10, 60))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .volcanoes(4, 25, 50, 40)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension VOLCANIC_OCEANIC_MOUNTAINS = register(
      "volcanic_oceanic_mountains",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, -24, 50))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCavesUpper1)
         .carving(TFCFBiomeNoise::undergroundCavesUpper2)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep3)
         .aquiferHeightOffset(-8.0)
         .salty()
         .volcanoes(2, -12, 50, 20)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension SHORE = register(
      "shore", BiomeBuilder.builder().heightmap(BiomeNoise::shore).surface(ShoreSurfaceBuilder.INSTANCE).aquiferHeightOffset(-16.0).group(Group.OCEAN).salty()
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension LAKE = register(
      "lake",
      BiomeBuilder.builder()
         .heightmap(BiomeNoise::lake)
         .surface(
            SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GravelSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         )
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .aquiferHeightOffset(-16.0)
         .group(Group.LAKE)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension RIVER = register(
      "river",
      BiomeBuilder.builder()
         .noise(TFCFBiomeNoise::riverSampler)
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(NormalSurfaceBuilder.INSTANCE))))
         .aquiferHeight(h -> 47.0)
         .group(Group.RIVER)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension MOUNTAIN_RIVER = register(
      "mountain_river",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 10, 70))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(BiomeNoise::undergroundRivers)
         .group(Group.RIVER)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension OLD_MOUNTAIN_RIVER = register(
      "old_mountain_river",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 16, 40))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(BiomeNoise::undergroundRivers)
         .group(Group.RIVER)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension OCEANIC_MOUNTAIN_RIVER = register(
      "oceanic_mountain_river",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(BiomeNoise::undergroundRivers)
         .salty()
         .group(Group.RIVER)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension VOLCANIC_MOUNTAIN_RIVER = register(
      "volcanic_mountain_river",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 10, 60))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .volcanoes(4, 25, 50, 40)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .carving(BiomeNoise::undergroundRivers)
         .group(Group.RIVER)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension VOLCANIC_OCEANIC_MOUNTAIN_RIVER = register(
      "volcanic_oceanic_mountain_river",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, -24, 50))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCaves)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle1)
         .carving(TFCFBiomeNoise::undergroundCavesMiddle2)
         .carving(TFCFBiomeNoise::undergroundCavesDeep1)
         .carving(TFCFBiomeNoise::undergroundCavesDeep2)
         .volcanoes(2, -12, 50, 20)
         .carving(BiomeNoise::undergroundRivers)
         .salty()
         .group(Group.RIVER)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension MOUNTAIN_LAKE = register(
      "mountain_lake",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 10, 70))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(BiomeNoise::undergroundLakes)
         .group(Group.LAKE)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension OLD_MOUNTAIN_LAKE = register(
      "old_mountain_lake",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(BiomeNoise::undergroundLakes)
         .group(Group.LAKE)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension OCEANIC_MOUNTAIN_LAKE = register(
      "oceanic_mountain_lake",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, -16, 60))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(BiomeNoise::undergroundLakes)
         .salty()
         .group(Group.LAKE)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension VOLCANIC_MOUNTAIN_LAKE = register(
      "volcanic_mountain_lake",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, 10, 60))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .volcanoes(4, 25, 50, 40)
         .carving(BiomeNoise::undergroundLakes)
         .group(Group.LAKE)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension VOLCANIC_OCEANIC_MOUNTAIN_LAKE = register(
      "volcanic_oceanic_mountain_lake",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.mountains(seed, -24, 50))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .volcanoes(2, -12, 50, 20)
         .carving(BiomeNoise::undergroundLakes)
         .salty()
         .group(Group.LAKE)
   );
   @Shadow
   @Mutable
   @Final
   public static final BiomeExtension PLATEAU_LAKE = register(
      "plateau_lake",
      BiomeBuilder.builder()
         .heightmap(seed -> BiomeNoise.hills(seed, 20, 30))
         .surface(SubSoilSurfaceBuilder.create(RockyDirtSurfaceBuilder.create(GrassSurfaceBuilder.create(MountainSurfaceBuilder.INSTANCE))))
         .carving(BiomeNoise::undergroundLakes)
         .group(Group.LAKE)
   );

   public TFCBiomesMixin() {
   }

   @Shadow
   public static BiomeExtension getExtensionOrThrow(LevelAccessor level, Biome biome) {
      return Objects.requireNonNull(getExtension(level, biome), () -> "Biome: " + biome.getRegistryName());
   }

   @Shadow
   public static boolean hasExtension(CommonLevelAccessor level, Biome biome) {
      return getExtension(level, biome) != null;
   }

   @Shadow
   @Nullable
   public static BiomeExtension getExtension(CommonLevelAccessor level, Biome biome) {
      return ((BiomeBridge)biome).tfc$getExtension(() -> findExtension(level, biome));
   }

   @Shadow
   public static Collection<ResourceKey<Biome>> getAllKeys() {
      return EXTENSIONS.keySet();
   }

   @Shadow
   public static Collection<BiomeExtension> getExtensions() {
      return EXTENSIONS.values();
   }

   @Shadow
   public static Collection<ResourceLocation> getExtensionKeys() {
      return EXTENSIONS.keySet().stream().<ResourceLocation>map(ResourceKey::location).toList();
   }

   @Shadow
   @Nullable
   public static BiomeExtension getById(ResourceLocation id) {
      return EXTENSIONS.get(ResourceKey.create(Registry.BIOME_REGISTRY, id));
   }

   @Shadow
   @Nullable
   private static BiomeExtension findExtension(CommonLevelAccessor level, Biome biome) {
      RegistryAccess registryAccess = level.registryAccess();
      Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
      return registry.getResourceKey(biome).map(EXTENSIONS::get).orElse(null);
   }

   @Shadow
   private static BiomeExtension register(String name, BiomeBuilder builder) {
      ResourceLocation id = Helpers.identifier(name);
      ResourceKey<Biome> key = ResourceKey.create(Registry.BIOME_REGISTRY, id);
      BiomeExtension variants = builder.build(key);
      EXTENSIONS.put(key, variants);
      TFCBiomes.BIOMES.register(name, OverworldBiomes::theVoid);
      return variants;
   }

   @Unique
   @Override
   public BiomeExtension getStaticGrasslands() {
      return GRASSLANDS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticWetlands() {
      return WETLANDS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticMarshes() {
      return MARSHES;
   }

   @Unique
   @Override
   public BiomeExtension getStaticSwamps() {
      return SWAMPS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticMangroves() {
      return MANGROVES;
   }

   @Unique
   @Override
   public BiomeExtension getStaticPuyMountains() {
      return PUY_MOUNTAINS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticBryceCanyons() {
      return BRYCE_CANYONS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticMesaHills() {
      return MESA_HILLS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticBadlandsPlateau() {
      return BADLANDS_PLATEAU;
   }

   @Unique
   @Override
   public BiomeExtension getStaticCanyonRivers() {
      return CANYON_RIVER;
   }

   @Unique
   @Override
   public BiomeExtension getStaticAlpineMountainRivers() {
      return ALPINE_MOUNTAIN_RIVER;
   }

   @Unique
   @Override
   public BiomeExtension getStaticAlpineMountains() {
      return ALPINE_MOUNTAINS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticAlpineHighlands() {
      return ALPINE_HIGHLANDS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticRollingHighlands() {
      return ROLLING_HIGHLANDS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticCalderas() {
      return CALDERAS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticGravelShores() {
      return GRAVEL_SHORE;
   }

   @Unique
   @Override
   public BiomeExtension getStaticThermalCanyons() {
      return THERMAL_CANYONS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticMesaPlateau() {
      return MESA_PLATEAU;
   }

   @Unique
   @Override
   public BiomeExtension getStaticPeatBog() {
      return PEAT_BOG;
   }

   @Unique
   @Override
   public BiomeExtension getStaticRiverbank() {
      return RIVERBANK;
   }

   @Unique
   @Override
   public BiomeExtension getStaticRiverEdge() {
      return RIVER_EDGE;
   }

   @Unique
   @Override
   public BiomeExtension getStaticChasms() {
      return CHASMS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticPlateauCliffs() {
      return PLATEAU_CLIFFS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticSteppes() {
      return STEPPES;
   }

   @Unique
   @Override
   public BiomeExtension getStaticShrublands() {
      return SHRUBLANDS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticMoorlands() {
      return MOORLANDS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticMistyPeaks() {
      return MISTY_PEAKS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticCoastalCliffs() {
      return COASTAL_CLIFFS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticShoreDunes() {
      return SHORE_DUNES;
   }

   @Unique
   @Override
   public BiomeExtension getStaticVinicuncaMountains() {
      return VINICUNCA_MOUNTAINS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticNearShore() {
      return NEAR_SHORE;
   }

   @Unique
   @Override
   public BiomeExtension getStaticLakeShore() {
      return LAKE_SHORE;
   }

   @Unique
   @Override
   public BiomeExtension getStaticPelagicZone() {
      return PELAGIC_ZONE;
   }

   @Unique
   @Override
   public BiomeExtension getStaticSeamounts() {
      return SEAMOUNTS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticGuyots() {
      return GUYOTS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticAtoll() {
      return ATOLL;
   }

   @Unique
   @Override
   public BiomeExtension getStaticBarrierReef() {
      return BARRIER_REEF;
   }

   @Unique
   @Override
   public BiomeExtension getStaticLagoon() {
      return LAGOON;
   }

   @Unique
   @Override
   public BiomeExtension getStaticSawtoothCliffs() {
      return SAWTOOTH_CLIFFS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticTablelands() {
      return TABLELANDS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticPitlands() {
      return PITLANDS;
   }

   @Unique
   @Override
   public BiomeExtension getStaticFracturedMistyPeaks() {
      return FRACTURED_MISTY_PEAKS;
   }
}
