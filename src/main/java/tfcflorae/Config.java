package tfcflorae;

import java.util.EnumMap;
import java.util.function.Function;
import net.dries007.tfc.util.Helpers;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;
import org.apache.commons.lang3.tuple.Pair;
import tfcflorae.common.blocks.wood.TFCFWood;

public class Config {
   public static final Config COMMON = register(Type.COMMON, Config::new);
   public final BooleanValue enableDebug;
   public final BooleanValue enableDunes;
   public final BooleanValue enableSandLayers;
   public final BooleanValue enableGravelLayers;
   public final BooleanValue enableUndergroundRivers;
   public final BooleanValue toggleCheapSandColourCalculations;
   public final IntValue maxFortressHeight;
   public final IntValue minFortressHeight;
   public final IntValue mineralGenFrequency;
   public final IntValue fruitingLeavesUpdateChance;
   public final IntValue leavesSaplingPlacementChance;
   public final IntValue leavesSaplingSpreadDistance;
   public final EnumMap<TFCFWood, IntValue> saplingGrowthDays;
   public final IntValue landBiomeSizes;
   public final IntValue earlyBiomeSizes;
   public final IntValue midBiomeSizes;
   public final IntValue lateBiomeSizes;
   public final DoubleValue foliageDecayThreshold;

   public static void init() {
   }

   private static <C> C register(Type type, Function<Builder, C> factory) {
      Pair<C, ForgeConfigSpec> specPair = new Builder().configure(factory);
      if (!Helpers.BOOTSTRAP_ENVIRONMENT) {
         ModLoadingContext.get().registerConfig(type, (IConfigSpec)specPair.getRight());
      }

      return (C)specPair.getLeft();
   }

   Config(Builder innerBuilder) {
      Function<String, Builder> builder = name -> innerBuilder.translation("tfcflorae.config.common." + name);
      innerBuilder.push("general");
      this.enableDebug = builder.apply("enableDebug").comment("Toggles debug mode").define("enableDebug", false);
      this.enableDunes = builder.apply("enableDunes")
         .comment("Toggles generation of sand dunes in deserts. Disabling this (false) will shorten world generation time.")
         .define("enableDunes", false);
      this.enableSandLayers = builder.apply("enableSandLayers")
         .comment("Toggles generation of sand layers. Disabling this (false) will shorten world generation time.")
         .define("enableSandLayers", true);
      this.enableGravelLayers = builder.apply("enableGravelLayers")
         .comment("Toggles generation of gravel layers. Disabling this (false) will shorten world generation time.")
         .define("enableGravelLayers", true);
      this.enableUndergroundRivers = builder.apply("enableUndergroundRivers")
         .comment("Toggles generation of underground rivers.")
         .define("enableUndergroundRivers", false);
      this.toggleCheapSandColourCalculations = builder.apply("toggleCheapSandColourCalculations")
         .comment(
            "Toggles calculation method for sand colour detection. If true, then it simply checks for the colour of the block below, while false checks for every direction."
         )
         .define("toggleCheapSandColourCalculations", true);
      this.maxFortressHeight = builder.apply("maxFortressHeight")
         .comment("Heighest Y-value the Nether Fortress can generate at.")
         .defineInRange("maxFortressHeight", -170, -256, 512);
      this.minFortressHeight = builder.apply("minFortressHeight")
         .comment("Lowest Y-value the Nether Fortress can generate at.")
         .defineInRange("minFortressHeight", -220, -256, 512);
      this.mineralGenFrequency = builder.apply("mineralGenFrequency")
         .comment("How often minerals can generate near hot springs and lava.")
         .defineInRange("mineralGenFrequency", 32, 0, Integer.MAX_VALUE);
      this.fruitingLeavesUpdateChance = builder.apply("fruitingLeavesUpdateChance")
         .comment("How often seasonal/fruiting leaves should check for updates. Higher values --> slower updates.")
         .defineInRange("fruitingLeavesUpdateChance", 10, 0, Integer.MAX_VALUE);
      this.leavesSaplingPlacementChance = builder.apply("leavesSaplingPlacementChance")
         .comment("The chance for the tree leaves to spread and place saplings. Set to \"0\" to disable entirely.")
         .defineInRange("leavesSaplingPlacementChance", 200, 0, Integer.MAX_VALUE);
      this.leavesSaplingSpreadDistance = builder.apply("leavesSaplingSpreadDistance")
         .comment(
            "Distance multiplication factor of which leaves will randomly spread and place saplings, for natural regrowth of the forests, during fall and spring seasons."
         )
         .defineInRange("leavesSaplingSpreadDistance", 12, 0, 16);
      this.saplingGrowthDays = new EnumMap<>(TFCFWood.class);

      for (TFCFWood wood : TFCFWood.VALUES) {
         String valueName = String.format("%sSaplingGrowthDays", wood.getSerializedName());
         this.saplingGrowthDays
            .put(
               wood,
               builder.apply(valueName)
                  .comment(String.format("Days for a %s tree sapling to be ready to grow into a full tree.", wood.getSerializedName()))
                  .defineInRange(valueName, wood.defaultDaysToGrow(), 0, Integer.MAX_VALUE)
            );
      }

      this.landBiomeSizes = builder.apply("landBiomeSizes")
         .comment(
            new String[]{
               "The amount of times to increase land biome sizes. Setting to 0 equates to regular TFC-sized land biomes.",
               "Game needs to restart in order to take effect."
            }
         )
         .defineInRange("landBiomeSizes", 1, 0, 64);
      this.earlyBiomeSizes = builder.apply("earlyBiomeSizes")
         .comment(
            new String[]{
               "The amount of times to increase all biome sizes, after generating oceans, and prior to making edge biomes.",
               "This settings stacks on top of \"landBiomeSizes\", so choose wisely. Setting to 0 equates to smaller than regular TFC biome sizes.",
               "Game needs to restart in order to take effect."
            }
         )
         .defineInRange("earlyBiomeSizes", 1, 0, 64);
      this.midBiomeSizes = builder.apply("midBiomeSizes")
         .comment(
            new String[]{
               "The amount of times to increase all biome sizes, after generating edge-biomes and before final biome expansions.",
               "This settings stacks on top of \"earlyBiomeSizes\", so choose wisely. Setting to 0 equates to smaller than regular TFC biome sizes.",
               "Game needs to restart in order to take effect."
            }
         )
         .defineInRange("midBiomeSizes", 1, 0, 64);
      this.lateBiomeSizes = builder.apply("lateBiomeSizes")
         .comment(
            new String[]{
               "The amount of times to increase all biome sizes, during the final touches.",
               "This includes beaches, shores, lakes and similar garnishes. This settings stacks on top of \"midBiomeSizes\", so choose wisely.",
               "Setting to 0 equates to smaller than regular TFC biome sizes. Game needs to restart in order to take effect."
            }
         )
         .defineInRange("lateBiomeSizes", 4, 0, 64);
      this.foliageDecayThreshold = builder.apply("foliageDecayThreshold")
         .comment(
            "Mean temperature over the last 24 days, at which foliage will \"decay\" for it to later return to normal, when the temperature rises past the set threshold value."
         )
         .defineInRange("foliageDecayThreshold", -4.0, -20.0, 30.0);
   }
}
