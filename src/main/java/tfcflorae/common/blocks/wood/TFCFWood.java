package tfcflorae.common.blocks.wood;

import java.awt.Color;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;
import tfcflorae.Config;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.items.TFCFFood;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.util.TFCFHelpers;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;

public enum TFCFWood implements RegistryWood {
   AFRICAN_PADAUK(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   ALDER(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   ANGELIM(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   ARGYLE_EUCALYPTUS(
      false,
      true,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      17,
      null,
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY
      },
      new Color(255, 255, 239).getRGB()
   ),
   BALD_CYPRESS(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 17),
   BAMBOO(false, false, false, false, false, MapColor.COLOR_LIGHT_GREEN, MapColor.COLOR_LIGHT_GREEN, 10, 9),
   BEECH(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   BLACK_WILLOW(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   BRAZILWOOD(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   BUTTERNUT(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   BUXUS(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   COCOBOLO(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   COMMON_OAK(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   FEVER(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   GHAF(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   GINKGO(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   GREENHEART(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   HAWTHORN(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.HAWTHORN),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY
      },
      new Color(245, 232, 241).getRGB()
   ),
   HAZEL(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   HOLLY(
      false,
      true,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.HOLLY_BERRY),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(232, 211, 229).getRGB()
   ),
   HORNBEAM(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   IROKO(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   JABUTICABEIRA(
      false,
      true,
      false,
      false,
      true,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      23,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.JABUTICABA),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY
      },
      new Color(255, 213, 213).getRGB()
   ),
   JOSHUA(
      false,
      true,
      false,
      true,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      8,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.JOSHUA_FRUIT),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY
      },
      new Color(237, 249, 238).getRGB()
   ),
   JUNIPER(
      false,
      true,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.JUNIPER),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(255, 255, 249).getRGB()
   ),
   KAURI(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   LAUREL(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   LIMBA(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   LOCUST(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   LOGWOOD(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   MACLURA(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.OSAGE_ORANGE),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(192, 209, 121).getRGB()
   ),
   MAHOE(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      null,
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(241, 190, 61).getRGB()
   ),
   MARBLEWOOD(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   MEDLAR(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.MEDLAR),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(214, 232, 232).getRGB()
   ),
   MESSMATE(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   MOUNTAIN_ASH(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 17),
   MULBERRY(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.MULBERRY),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY
      },
      new Color(255, 162, 221).getRGB()
   ),
   NORDMANN_FIR(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   PEAR(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.PEAR),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(255, 255, 255).getRGB()
   ),
   PERSIMMON(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.PERSIMMON),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(238, 232, 174).getRGB()
   ),
   PINK_CHERRY_BLOSSOM(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCItems.FOOD.get(Food.CHERRY),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(252, 203, 231).getRGB()
   ),
   POPLAR(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   PURPLEHEART(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   PURPLE_IPE(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      null,
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(135, 88, 216).getRGB()
   ),
   PURPLE_JACARANDA(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      null,
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(200, 146, 218).getRGB()
   ),
   QUINCE(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.QUINCE),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(249, 82, 38).getRGB()
   ),
   RED_CEDAR(
      false,
      true,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.JUNIPER),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(255, 255, 237).getRGB()
   ),
   RED_CYPRESS(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 17),
   RED_ELM(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   ROWAN(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.ROWAN_BERRY),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(245, 232, 241).getRGB()
   ),
   RUBBER_FIG(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   SLOE(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.SLOE),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(228, 239, 243).getRGB()
   ),
   SORB(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.SORB_APPLE),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(255, 238, 158).getRGB()
   ),
   SWEETGUM(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   SYZYGIUM(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.OTAHEITE_APPLE),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY
      },
      new Color(235, 114, 179).getRGB()
   ),
   WENGE(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   WHITEBEAM(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.ROWAN_BERRY),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY
      },
      new Color(255, 213, 213).getRGB()
   ),
   WHITE_CHERRY_BLOSSOM(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCItems.FOOD.get(Food.CHERRY),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(237, 237, 239).getRGB()
   ),
   WHITE_ELM(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
   WHITE_IPE(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      null,
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(236, 214, 222).getRGB()
   ),
   WHITE_JACARANDA(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      null,
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(241, 235, 238).getRGB()
   ),
   WHITE_MANGROVE(
      false,
      false,
      true,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      17,
      null,
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(241, 251, 251).getRGB()
   ),
   YELLOW_JACARANDA(
      false,
      false,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      null,
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(241, 167, 0).getRGB()
   ),
   YELLOW_MERANTI(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   YEW(
      false,
      true,
      false,
      false,
      false,
      MapColor.COLOR_GREEN,
      MapColor.COLOR_GREEN,
      10,
      19,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.YEW_BERRY),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(244, 205, 205).getRGB()
   ),
   ZEBRAWOOD(false, true, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 29),
   WHITE_CEDAR(
      true,
      true,
      false,
      false,
      false,
      MapColor.TERRACOTTA_WHITE,
      MapColor.TERRACOTTA_LIGHT_GRAY,
      10,
      7,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.JUNIPER),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      new Color(255, 255, 249).getRGB()
   );

   public static final TFCFWood[] VALUES = values();
   public final String serializedName = this.name().toLowerCase(Locale.ROOT);
   public boolean hasLeavesOnly = false;
   public final boolean conifer;
   public final boolean mangrove;
   public final boolean joshua;
   public final boolean fruitingLog;
   public final MapColor woodColor;
   public final MapColor barkColor;
   public final TFCTreeGrower tree;
   public final int maxDecayDistance;
   public final int daysToGrow;
   public int floweringLeavesColor;
   public Boolean isFruitTree;
   public Boolean isPalmTree;
   public Supplier<? extends Item> productItem;
   public Lifecycle[] stages;

   private TFCFWood(
      boolean hasLeavesOnly,
      boolean conifer,
      boolean mangrove,
      boolean joshua,
      boolean fruitingLog,
      MapColor woodColor,
      MapColor barkColor,
      int maxDecayDistance,
      int daysToGrow
   ) {
      this(conifer, mangrove, joshua, fruitingLog, woodColor, barkColor, maxDecayDistance, daysToGrow);
      this.hasLeavesOnly = hasLeavesOnly;
      this.floweringLeavesColor = -1;
      this.isPalmTree = false;
   }

   private TFCFWood(
      boolean hasLeavesOnly,
      boolean conifer,
      boolean mangrove,
      boolean joshua,
      boolean fruitingLog,
      MapColor woodColor,
      MapColor barkColor,
      int maxDecayDistance,
      int daysToGrow,
      Supplier<? extends Item> productItem,
      Lifecycle[] stages,
      int floweringLeavesColor
   ) {
      this(conifer, mangrove, joshua, fruitingLog, woodColor, barkColor, maxDecayDistance, daysToGrow, productItem, stages, floweringLeavesColor);
      this.floweringLeavesColor = floweringLeavesColor;
      this.hasLeavesOnly = hasLeavesOnly;
      this.isPalmTree = false;
   }

   private TFCFWood(
      boolean conifer,
      boolean mangrove,
      boolean joshua,
      boolean fruitingLog,
      MapColor woodColor,
      MapColor barkColor,
      int maxDecayDistance,
      int daysToGrow,
      Supplier<? extends Item> productItem,
      Lifecycle[] stages,
      int floweringLeavesColor
   ) {
      this(conifer, mangrove, joshua, fruitingLog, woodColor, barkColor, maxDecayDistance, daysToGrow);
      this.hasLeavesOnly = false;
      this.productItem = productItem;
      this.stages = stages;
      this.isFruitTree = true;
      this.floweringLeavesColor = floweringLeavesColor;
      this.isPalmTree = false;
   }

   private TFCFWood(
      boolean conifer,
      boolean mangrove,
      boolean joshua,
      boolean fruitingLog,
      MapColor woodColor,
      MapColor barkColor,
      int maxDecayDistance,
      int daysToGrow,
      Supplier<? extends Item> productItem,
      Lifecycle[] stages,
      int floweringLeavesColor,
      boolean isPalmTree
   ) {
      this(conifer, mangrove, joshua, fruitingLog, woodColor, barkColor, maxDecayDistance, daysToGrow);
      this.hasLeavesOnly = false;
      this.productItem = productItem;
      this.stages = stages;
      this.isFruitTree = false;
      this.floweringLeavesColor = floweringLeavesColor;
      this.isPalmTree = isPalmTree;
      this.hasLeavesOnly = false;
   }

   private TFCFWood(
      boolean conifer,
      boolean mangrove,
      boolean joshua,
      boolean fruitingLog,
      MapColor woodColor,
      MapColor barkColor,
      int maxDecayDistance,
      int daysToGrow
   ) {
      this.conifer = conifer;
      this.mangrove = mangrove;
      this.joshua = joshua;
      this.fruitingLog = fruitingLog;
      this.woodColor = woodColor;
      this.barkColor = barkColor;
      this.tree = new TFCTreeGrower(
         TFCFHelpers.identifier("tree/" + this.serializedName),
         TFCFHelpers.identifier("tree/" + this.serializedName + "_large")
      );
      this.maxDecayDistance = maxDecayDistance;
      this.daysToGrow = daysToGrow;
      this.isFruitTree = false;
      this.floweringLeavesColor = -1;
   }

   public String getSerializedName() {
      return this.serializedName;
   }

   public boolean hasLeavesOnly() {
      return this.hasLeavesOnly;
   }

   public boolean isConifer() {
      return this.conifer;
   }

   public boolean isMangrove() {
      return this.mangrove;
   }

   public boolean isJoshua() {
      return this.joshua;
   }

   public boolean hasFruitingLog() {
      return this.fruitingLog;
   }

   public MapColor woodColor() {
      return this.woodColor;
   }

   public MapColor barkColor() {
      return this.barkColor;
   }

   public TFCTreeGrower tree() {
      return this.tree;
   }

   public int maxDecayDistance() {
      return this.maxDecayDistance;
   }

   public int daysToGrow() {
      return (Integer)Config.COMMON.saplingGrowthDays.get(this).get();
   }

   public int defaultDaysToGrow() {
      return this.daysToGrow;
   }

   public Boolean isFruitTree() {
      return this.isFruitTree;
   }

   public Boolean isPalmTree() {
      return this.isPalmTree;
   }

   public int getFloweringLeavesColor() {
      return this.floweringLeavesColor;
   }

   @Nullable
   public Supplier<? extends Item> getProductItem() {
      return this.productItem;
   }

   public Lifecycle[] getStages() {
      return this.stages;
   }

   @Nullable
   public static Function<Block, BlockItem> createBlockItem(Properties properties) {
      BiFunction<Block, Properties, ? extends BlockItem> blockItem = BlockItem::new;
      return block -> blockItem.apply(block, properties);
   }

   public Supplier<Block> getBlock(BlockType type) {
      return (Supplier<Block>)TFCFBlocks.WOODS.get(this).get(type);
   }

   /** TFC 3.x added this to {@code RegistryWood}; vanilla sign/boat types are not modelled per wood yet. */
   @Override
   public net.minecraft.world.level.block.state.properties.WoodType getVanillaWoodType() {
      return net.minecraft.world.level.block.state.properties.WoodType.OAK;
   }

   @Override
   public net.minecraft.world.level.block.state.properties.BlockSetType getBlockSet() {
      return net.minecraft.world.level.block.state.properties.BlockSetType.OAK;
   }

   @Override
   public int autumnIndex() {
      return 0;
   }
}
