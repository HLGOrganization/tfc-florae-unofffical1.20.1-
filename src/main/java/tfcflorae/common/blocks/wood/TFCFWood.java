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
   BLACK_WILLOW(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
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
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY
      },
      new Color(245, 232, 241).getRGB()
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
         Lifecycle.FLOWERING,
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
         Lifecycle.FLOWERING,
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
         Lifecycle.FLOWERING,
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
         Lifecycle.FLOWERING,
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
   RUBBER_FIG(false, false, false, false, false, MapColor.COLOR_GREEN, MapColor.COLOR_GREEN, 10, 19),
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
         Lifecycle.FLOWERING,
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
         Lifecycle.FLOWERING,
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

   /**
    * 只有果树形态（{@code plant/<tree>_sapling|_growing_branch|_branch|_leaves}）、
    * <b>不注册</b> {@code wood/*} 那一整套木材方块的树种。
    * <p>
    * 精确名单，禁止子串匹配。这些枚举常量<b>必须保留</b> —— 果树要用它们的
    * {@link #getStages()}、{@link #daysToGrow()}、{@link #getProductItem()}、花色与气候数据，
    * 只是不再产出木材方块（{@code woodMapper} / {@code seasonalLeavesMapper} 等映射器按此跳过）。
    */
   private static final java.util.Set<TFCFWood> NO_WOOD_SET = java.util.EnumSet.of(
      HAWTHORN, JABUTICABEIRA, MACLURA, MEDLAR, MULBERRY, PEAR, PERSIMMON, QUINCE, SORB, SYZYGIUM, YEW
   );

   /** 是否注册 {@code wood/*} 木材方块。见 {@link #NO_WOOD_SET}。 */
   public boolean hasWoodSet() {
      return !NO_WOOD_SET.contains(this);
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
