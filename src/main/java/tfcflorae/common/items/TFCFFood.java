package tfcflorae.common.items;

import net.dries007.tfc.common.TFCCreativeTabs;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.FoodProperties.Builder;
import net.minecraft.world.item.Item.Properties;

public enum TFCFFood {
   BARREL_CACTUS_FRUIT(false, false),
   CARAMBOLA(false, false),
   DRAGONBERRY(false, true, true),
   GLOW_BERRY(false, true),
   HAWTHORN(false, true),
   HOLLY_BERRY(false, true),
   JABUTICABA(false, true),
   JOSHUA_FRUIT(false, false),
   JUNIPER(false, true),
   KIWI(false, false),
   MEDLAR(false, false),
   MULBERRY(false, true),
   OSAGE_ORANGE(false, false),
   OTAHEITE_APPLE(false, false),
   PAPAYA(false, false),
   PEAR(false, false),
   PERSIMMON(false, false),
   PITAHAYA(false, false),
   PRICKLY_PEAR(false, false),
   QUINCE(false, false),
   RIBERRY(false, true),
   ROWAN_BERRY(false, true),
   SAGUARO(false, false),
   SKY_FRUIT(false, false),
   SLOE(false, true),
   SORB_APPLE(false, false),
   YEW_BERRY(false, true),
   ACORN_SQUASH(false, false),
   AEHOBAK(false, false),
   BUTTERNUT(false, false),
   CROOKNECK(false, false),
   CUCUMBER(false, false),
   SCALLOPINI(false, false),
   TROMBONCINO(false, false),
   ZUCCHINI(false, false),
   FRESH_SEAWEED(false, true),
   ROASTED_AMANITA(false, false, true),
   ROASTED_BEEFSTEAK_FUNGUS(false, false),
   ROASTED_BIRCH_POLYPORE(false, false),
   ROASTED_BITTER_OYSTER(false, false),
   ROASTED_BLACK_POWDERPUFF(false, false, true),
   ROASTED_BOLETUS(false, false),
   ROASTED_BRIDAL_VEIL_STINKHORN(false, false),
   ROASTED_CHANTERELLE(false, false, true),
   ROASTED_CHLOROPHOS_FOXFIRE(false, false),
   ROASTED_DEATH_CAP(false, false, true),
   ROASTED_DEVILS_FINGERS(false, false),
   ROASTED_DRYADS_SADDLE(false, false),
   ROASTED_ENTOLOMA(false, false),
   ROASTED_GIANT_CLUB(false, false),
   ROASTED_INDIGO_MILK_CAP(false, false, true),
   ROASTED_LIONS_MANE(false, false, true),
   ROASTED_PARASOL_MUSHROOM(false, false),
   ROASTED_PORCINI(false, false),
   ROASTED_REISHI(false, false),
   ROASTED_SHAGGY_BRACKET(false, false),
   ROASTED_SHIITAKE(false, false),
   ROASTED_STINKHORN(false, false, true),
   ROASTED_SULPHUR_SHELF(false, false, true),
   ROASTED_TURKEY_TAIL(false, false, true),
   ROASTED_WEEPING_MILK_CAP(false, false),
   ROASTED_WOOD_BLEWIT(false, false),
   ROASTED_WOOLLY_GOMPHUS(false, false, true),
   EUCALYPTUS_LEAVES(false, false, true);

   public final boolean meat;
   public final boolean fast;
   public final boolean hasEffect;

   private TFCFFood(boolean meat, boolean fast) {
      this(false, false, false);
   }

   private TFCFFood(boolean meat, boolean fast, boolean hasEffect) {
      this.meat = meat;
      this.fast = fast;
      this.hasEffect = hasEffect;
   }

   public FoodProperties getFoodProperties() {
      Builder builder = new Builder();
      if (this.meat) {
         builder.meat();
      }

      if (this.fast) {
         builder.fast();
      }

      return builder.nutrition(4).saturationMod(0.3F).build();
   }

   public Properties createProperties() {
      Properties props = new Properties().food(this.getFoodProperties());
      return props;
   }

   public boolean hasEffect() {
      return this.hasEffect;
   }
}
