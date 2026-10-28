package tfcflorae.common;
import net.minecraft.core.registries.Registries;

import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import tfcflorae.util.TFCFHelpers;

public class TFCFTags {
   public TFCFTags() {
   }

   public static class Biomes {
      public static final TagKey<Biome> OCEANIC = create("oceanic");
      public static final TagKey<Biome> COASTAL = create("coastal");
      public static final TagKey<Biome> IS_LAKE = create("is_lake");
      public static final TagKey<Biome> IS_RIVER = create("is_river");

      public Biomes() {
      }

      private static TagKey<Biome> create(String id) {
         return TagKey.create(Registries.BIOME, TFCFHelpers.identifier(id));
      }
   }

   public static class Blocks {
      public static final TagKey<Block> CLAY = create("clay");
      public static final TagKey<Block> GRAVEL = create("gravel");
      public static final TagKey<Block> MAGMA_BLOCKS = create("magma_blocks");
      public static final TagKey<Block> PODZOL = create("podzol");
      public static final TagKey<Block> ROOTED_DIRT = create("rooted_dirt");
      public static final TagKey<Block> MOSSY_PACKED_MUD = create("mossy_packed_mud");
      public static final TagKey<Block> MANGROVE_LOGS_CAN_GROW_THROUGH = create("mangrove_logs_can_grow_through");
      public static final TagKey<Block> MANGROVE_ROOTS_CAN_GROW_THROUGH = create("mangrove_roots_can_grow_through");
      public static final TagKey<Block> REPLACEABLE = create("replaceable");
      public static final TagKey<Block> SILKMOTH_TARGET_BLOCKS = create("silkmoth_target_blocks");
      public static final TagKey<Block> SILKMOTH_NESTS = create("silkmoth_nests");
      public static final TagKey<Block> TIDE_POOL_BLOCKS = create("tide_pool_blocks");
      public static final TagKey<Block> CREEPING_STONE_PLANTABLE_ON = create("creeping_stone_plantable_on");
      public static final TagKey<Block> SAPLING_CAN_REPLACE = create("sapling_can_replace");
      public static final TagKey<Block> FUNGI_ON_FALLEN_LOGS = create("fungi_on_fallen_logs");
      public static final TagKey<Block> IS_LOAM = create("loam_soil");
      public static final TagKey<Block> IS_SANDY_LOAM = create("sandy_loam_soil");
      public static final TagKey<Block> IS_SILT = create("silt_soil");
      public static final TagKey<Block> IS_SILTY_LOAM = create("silty_loam_soil");
      public static final TagKey<Block> IS_HUMUS = create("humus_soil");
      public static final TagKey<Block> IS_BOG_IRON = create("bog_iron_soil");

      public Blocks() {
      }

      private static TagKey<Block> create(String id) {
         return TagKey.create(Registries.BLOCK, TFCFHelpers.identifier(id));
      }
   }

   public static class Entities {

      public Entities() {
      }

      private static TagKey<EntityType<?>> create(String id) {
         return TagKey.create(Registries.ENTITY_TYPE, TFCFHelpers.identifier(id));
      }
   }

   public static class Items {
      public static final TagKey<Item> BRUSHES = create("brushes");
      public static final TagKey<Item> WALKING_CANES = create("walking_canes");
      public static final TagKey<Item> BYPASS_SLOWDOWN = create("bypass_slowdown");
      // 以下是 KnappingType 的输入 tag（见 data/tfcflorae/tfc/knapping_types/*.json），
      // 在 Java 侧只由 KNAPPING_ANY 间接引用，因此这里保留 tag 定义但不单独使用。
      public static final TagKey<Item> EARTHENWARE_CLAY_KNAPPING = create("earthenware_clay_knapping");
      public static final TagKey<Item> STONEWARE_CLAY_KNAPPING = create("stoneware_clay_knapping");
      /** 本模组所有可打制物品的合集；同时会以 replace:false 追加进 TFC 的 tfc:any_knapping。 */
      public static final TagKey<Item> KNAPPING_ANY = create("knapping_any");
      public static final TagKey<Item> MINERAL_SHEETS = create("mineral_sheets");
      public static final TagKey<Item> POTS = create("pots");
      public static final TagKey<Item> SILKMOTH_TEMPTATION_ITEMS = create("silkmoth_temptation_items");
      // 青蛙 / 鹦鹉的 tag 随实体一起移除（frog_temptation_items / parrot_food / poisonous_parrot_food）
      public static final TagKey<Item> SAPLINGS = create("saplings");

      public Items() {
      }

      private static TagKey<Item> create(String id) {
         return TagKey.create(Registries.ITEM, TFCFHelpers.identifier(id));
      }
   }
}
