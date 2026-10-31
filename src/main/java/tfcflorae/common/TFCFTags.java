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
      // 浠ヤ笅鏄?KnappingType 鐨勮緭鍏?tag锛堣 data/tfcflorae/tfc/knapping_types/*.json锛夛紝
      // 鍦?Java 渚у彧鐢?KNAPPING_ANY 闂存帴寮曠敤锛屽洜姝よ繖閲屼繚鐣?tag 瀹氫箟浣嗕笉鍗曠嫭浣跨敤銆?
      public static final TagKey<Item> EARTHENWARE_CLAY_KNAPPING = create("earthenware_clay_knapping");
      public static final TagKey<Item> STONEWARE_CLAY_KNAPPING = create("stoneware_clay_knapping");
      /** 鏈ā缁勬墍鏈夊彲鎵撳埗鐗╁搧鐨勫悎闆嗭紱鍚屾椂浼氫互 replace:false 杩藉姞杩?TFC 鐨?tfc:any_knapping銆?*/
      public static final TagKey<Item> KNAPPING_ANY = create("knapping_any");
      public static final TagKey<Item> MINERAL_SHEETS = create("mineral_sheets");
      public static final TagKey<Item> POTS = create("pots");
      public static final TagKey<Item> SILKMOTH_TEMPTATION_ITEMS = create("silkmoth_temptation_items");
      // 闈掕洐 / 楣﹂箟鐨?tag 闅忓疄浣撲竴璧风Щ闄わ紙frog_temptation_items / parrot_food / poisonous_parrot_food锛?
      public static final TagKey<Item> SAPLINGS = create("saplings");

      public Items() {
      }

      private static TagKey<Item> create(String id) {
         return TagKey.create(Registries.ITEM, TFCFHelpers.identifier(id));
      }
   }
}
