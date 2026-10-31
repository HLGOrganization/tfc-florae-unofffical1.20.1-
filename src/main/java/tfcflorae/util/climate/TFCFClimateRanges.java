package tfcflorae.util.climate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.dries007.tfc.util.RegisteredDataManager.Entry;
import net.dries007.tfc.util.climate.ClimateRange;
import tfcflorae.common.blocks.plant.TFCFFruitTrees;
import tfcflorae.common.blocks.plant.TFCFPlant;
import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.util.TFCFHelpers;

public class TFCFClimateRanges {
   public static final Map<TFCFWood, Supplier<ClimateRange>> LARGE_FRUIT_TREES = woodClimateMapper(TFCFWood.class);
   public static final Map<TFCFPlant, Supplier<ClimateRange>> SEASONAL_PLANT = plantClimateMapper(TFCFPlant.class);
   /** TFC 3.2 果树（fruit tree）用的气候范围，数据在 {@code tfcflorae:plant/<wood>_tree}。 */
   public static final Map<TFCFWood, Supplier<ClimateRange>> FRUIT_TREES = fruitTreeClimateMapper();

   public TFCFClimateRanges() {
   }

   private static Map<TFCFWood, Supplier<ClimateRange>> fruitTreeClimateMapper() {
      Map<TFCFWood, Supplier<ClimateRange>> Map = new HashMap<>();

      for (TFCFWood wood : TFCFFruitTrees.TREES) {
         Map.put(wood, register("plant/" + wood.getSerializedName().toLowerCase(Locale.ROOT) + "_tree"));
      }

      return Map;
   }

   private static Map<TFCFWood, Supplier<ClimateRange>> woodClimateMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, Supplier<ClimateRange>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (wood.hasFruitingLog()) {
            Map.put(wood, register("wood/log/" + wood.getSerializedName().toLowerCase(Locale.ROOT)));
            Map.put(wood, register("wood/wood/" + wood.getSerializedName().toLowerCase(Locale.ROOT)));
         } else if (wood.isFruitTree()) {
            Map.put(wood, register("wood/leaves/" + wood.getSerializedName().toLowerCase(Locale.ROOT)));
         }
         // 棕榈树（coconut_palm / date_palm）已整体移除，不再注册对应的 climate range。
      }

      return Map;
   }

   private static Map<TFCFPlant, Supplier<ClimateRange>> plantClimateMapper(Class<TFCFPlant> enumClass) {
      Map<TFCFPlant, Supplier<ClimateRange>> Map = new HashMap<>();

      for (TFCFPlant plant : enumClass.getEnumConstants()) {
         if (plant.isSeasonalFruitPlant()) {
            Map.put(plant, register("plant/" + plant.name().toLowerCase(Locale.ROOT)));
         }
      }

      return Map;
   }

   private static Entry<ClimateRange> register(String name) {
      return ClimateRange.MANAGER.register(TFCFHelpers.identifier(name.toLowerCase(Locale.ROOT)));
   }
}
