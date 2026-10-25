package tfcflorae.common.blocks.plant;

import java.awt.Color;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.plant.BodyPlantBlock;
import net.dries007.tfc.common.blocks.plant.CreepingPlantBlock;
import net.dries007.tfc.common.blocks.plant.EpiphytePlantBlock;
import net.dries007.tfc.common.blocks.plant.FloatingWaterPlantBlock;
import net.dries007.tfc.common.blocks.plant.KelpTreeBlock;
import net.dries007.tfc.common.blocks.plant.KelpTreeFlowerBlock;
import net.dries007.tfc.common.blocks.plant.PlantBlock;
import net.dries007.tfc.common.blocks.plant.ShortGrassBlock;
import net.dries007.tfc.common.blocks.plant.TFCCactusBlock;
import net.dries007.tfc.common.blocks.plant.TFCKelpBlock;
import net.dries007.tfc.common.blocks.plant.TFCKelpTopBlock;
import net.dries007.tfc.common.blocks.plant.TFCSeagrassBlock;
import net.dries007.tfc.common.blocks.plant.TFCTallGrassBlock;
import net.dries007.tfc.common.blocks.plant.TFCVineBlock;
import net.dries007.tfc.common.blocks.plant.TallWaterPlantBlock;
import net.dries007.tfc.common.blocks.plant.TopPlantBlock;
import net.dries007.tfc.common.blocks.plant.WaterPlantBlock;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.calendar.Month;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.items.TFCFFood;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.util.climate.TFCFClimateRanges;

public enum TFCFPlant implements RegistryPlant {
   CAT_GRASS(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, false, false, false),
   CROWNGRASS(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, false, false, false),
   GOOSEGRASS(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, false, false, false),
   HALFA_GRASS(TFCFPlant.BlockType.DESERT_SHORT_GRASS, 0.8F, false, false, false),
   MARRAM_GRASS(TFCFPlant.BlockType.DESERT_SHORT_GRASS, 0.8F, false, false, false),
   PRAIRIE_JUNEGRASS(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, new int[]{0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0}, false, false, false),
   WHEATGRASS(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, false, false, false),
   BUNCH_GRASS(TFCFPlant.BlockType.TALL_WATER_FRESH, 0.8F, false, false, false),
   SAWGRASS(TFCFPlant.BlockType.TALL_WATER_FRESH, 0.8F, false, false, false),
   CINNAMON_FERN(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, false, false, true),
   LEAF_LITTER(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, false, false, false),
   LOW_UNDERGROWTH(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, false, false, true),
   PIERIS(TFCFPlant.BlockType.TFCF_STANDARD, 0.5F, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0}, false, false, false),
   WOOLLY_BUSH(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, false, false, false),
   BEARDED_MOSS_PLANT(TFCFPlant.BlockType.WEEPING, 0.9F, false, false, false),
   BEARDED_MOSS(TFCFPlant.BlockType.WEEPING_TOP, 0.9F, false, false, false),
   BEARDED_MOSS_CREEPING(TFCFPlant.BlockType.CREEPING, 0.9F, false, false, false),
   MONSTERA(TFCFPlant.BlockType.TALL_GRASS, 0.5F, false, false, false),
   MONSTERA_EPIPHYTE(TFCFPlant.BlockType.EPIPHYTE, 0.5F, false, false, false),
   DECIDUOUS_SHRUB(TFCFPlant.BlockType.TALL_GRASS, 0.5F, new int[]{0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0}, false, false, true),
   CONIFEROUS_SHRUB(TFCFPlant.BlockType.TALL_GRASS, 0.5F, true, false, false),
   LARGE_RED_SEALING_WAX_PALM(TFCFPlant.BlockType.TALL_GRASS, 0.2F, false, false, false),
   SHIELD_FERN(TFCFPlant.BlockType.SHORT_GRASS, 0.7F, true, false, false),
   LICORICE_FERN(TFCFPlant.BlockType.SHORT_GRASS, 0.7F, true, false, false),
   BIRD_OF_PARADISE(TFCFPlant.BlockType.TFCF_STANDARD, 0.6F, new int[]{0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}, false, false, false),
   BONATEA(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 1, 2, 3, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   CAMAS(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 0, 1, 2, 2, 2, 3, 3, 2, 2, 1, 0}, false, false, false),
   CHIVES(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 0, 1, 2, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   CLOVER(TFCFPlant.BlockType.CREEPING, 0.9F, new int[]{0, 1, 2, 3, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   COMMON_REEDS(TFCFPlant.BlockType.TALL_LAND_WATER_FRESH, 0.5F, new int[]{0, 0, 1, 2, 2, 2, 2, 3, 3, 3, 1, 0}, false, false, false),
   CREEPING_CHARLIE(TFCFPlant.BlockType.CREEPING, 0.9F, new int[]{0, 1, 2, 3, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   CUP_PLANT(TFCFPlant.BlockType.TALL_GRASS, 0.5F, new int[]{0, 0, 1, 2, 2, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   DAFFODIL(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, new int[]{0, 3, 3, 2, 2, 2, 2, 2, 2, 2, 1, 0}, false, false, false),
   DEVILS_TONGUE(TFCFPlant.BlockType.TFCF_STANDARD, 0.4F, new int[]{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, false, false, false),
   DUNE_GRASS(TFCFPlant.BlockType.DESERT_SHORT_GRASS, 0.8F, new int[]{0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0}, false, false, false),
   EGYPTIAN_AUTUMN_CROCUS(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, new int[]{2, 2, 0, 0, 0, 0, 0, 0, 1, 1, 1, 2}, false, false, false),
   ELEPHANT_GRASS(TFCFPlant.BlockType.TALL_GRASS, 0.5F, false, false, false),
   EUROPEAN_BEDSTRAW(TFCFPlant.BlockType.TFCF_STANDARD, 0.6F, new int[]{0, 0, 1, 2, 2, 2, 3, 3, 3, 2, 1, 0}, false, false, false),
   FREESIA(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 1, 3, 3, 3, 2, 2, 2, 2, 2, 1, 0}, false, false, false),
   GARLIC_MUSTARD(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, new int[]{0, 0, 1, 3, 3, 3, 2, 2, 2, 2, 1, 0}, false, false, false),
   GIANT_HOGWEED(TFCFPlant.BlockType.POISONOUS_TALL_GRASS, 0.6F, new int[]{0, 0, 1, 2, 2, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   PINK_HEATHER(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 0, 1, 1, 1, 1, 2, 2, 1, 1, 1, 0}, false, false, false),
   WHITE_HEATHER(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 0, 1, 1, 1, 1, 2, 2, 1, 1, 1, 0}, false, false, false),
   JACK_IN_THE_PULPIT(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 1, 2, 3, 3, 3, 2, 2, 2, 2, 1, 0}, false, false, false),
   LAVENDER(TFCFPlant.BlockType.TFCF_STANDARD, 0.7F, new int[]{0, 1, 2, 2, 2, 3, 3, 3, 2, 2, 1, 0}, false, false, false),
   LEAFY_LOW_UNDERGROWTH(TFCFPlant.BlockType.SHORT_GRASS, 0.8F, new int[]{0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 1, 0}, false, false, true),
   LEAFY_UNDERGROWTH(TFCFPlant.BlockType.TALL_GRASS, 0.6F, new int[]{0, 0, 1, 2, 2, 2, 2, 2, 3, 3, 1, 0}, false, false, true),
   LEOPARD_ORCHID(TFCFPlant.BlockType.TFCF_STANDARD, 0.6F, new int[]{0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0}, false, false, false),
   LEOPARD_ORCHID_EPIPHYTE(TFCFPlant.BlockType.EPIPHYTE, 0.6F, new int[]{0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0}, false, false, false),
   LIANA_VINE(TFCFPlant.BlockType.VINE, 1.0F, false, false, false),
   LOBSTER_CLAWS(TFCFPlant.BlockType.TFCF_STANDARD, 0.6F, new int[]{0, 0, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0}, false, false, false),
   ORCHID(TFCFPlant.BlockType.TFCF_STANDARD, 0.9F, new int[]{0, 1, 2, 3, 3, 3, 2, 2, 2, 2, 1, 0}, false, false, false),
   BLUE_LUPINE(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 1, 2, 3, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   PURPLE_LUPINE(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 1, 2, 3, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   RED_LUPINE(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 1, 2, 3, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   YELLOW_LUPINE(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 1, 2, 3, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   MALLOW(TFCFPlant.BlockType.TALL_GRASS, 0.7F, new int[]{0, 1, 2, 3, 3, 3, 3, 3, 3, 3, 1, 0}, false, false, false),
   AMERICAN_MISTLETOE(TFCFPlant.BlockType.EPIPHYTE, 0.7F, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1}, false, false, false),
   DESERT_MISTLETOE(TFCFPlant.BlockType.EPIPHYTE, 0.7F, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1}, false, false, false),
   EUROPEAN_MISTLETOE(TFCFPlant.BlockType.EPIPHYTE, 0.7F, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1}, false, false, false),
   DWARF_MISTLETOE(TFCFPlant.BlockType.EPIPHYTE, 0.7F, false, false, false),
   NARBON_VETCH(TFCFPlant.BlockType.TFCF_STANDARD, 0.9F, new int[]{0, 0, 1, 2, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   PAPYRUS(TFCFPlant.BlockType.TALL_LAND_WATER_FRESH, 0.6F, false, false, false),
   PERIWINKLE(TFCFPlant.BlockType.TFCF_STANDARD, 0.9F, new int[]{0, 0, 1, 2, 2, 3, 3, 3, 3, 3, 3, 0}, false, false, false),
   POKEWEED(
      TFCFPlant.BlockType.FRUITING_TALL_PLANT,
      0.5F,
      new int[]{0, 0, 1, 2, 2, 2, 2, 3, 3, 4, 1, 0},
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.DRAGONBERRY),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      false,
      false,
      true,
      false
   ),
   POND_GRASS(TFCFPlant.BlockType.SHORT_GRASS, 0.6F, new int[]{0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 0}, false, false, false),
   PRAIRIE_GRASS(TFCFPlant.BlockType.TALL_GRASS, 0.5F, new int[]{0, 1, 2, 2, 2, 2, 2, 2, 3, 3, 1, 0}, false, false, false),
   ROYAL_JASMINE(TFCFPlant.BlockType.TALL_GRASS, 0.7F, new int[]{0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0}, false, false, false),
   SALTWORT(TFCFPlant.BlockType.SHORT_GRASS, 0.7F, new int[]{0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, false, false, false),
   SUNFLOWER(TFCFPlant.BlockType.TALL_GRASS, 0.5F, new int[]{0, 0, 1, 2, 2, 3, 3, 3, 3, 2, 1, 0}, false, false, false),
   TITAN_ARUM(TFCFPlant.BlockType.TFCF_STANDARD, 0.4F, new int[]{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, false, false, false),
   VOODOO_LILY(TFCFPlant.BlockType.TFCF_STANDARD, 0.4F, new int[]{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, false, false, false),
   WATER_HYACINTH(TFCFPlant.BlockType.FLOATING_FRESH, 0.5F, new int[]{0, 0, 1, 1, 1, 1, 1, 2, 2, 1, 1, 0}, false, false, false),
   WATER_PLANTAIN(TFCFPlant.BlockType.TALL_WATER_FRESH, 0.6F, new int[]{0, 1, 2, 3, 3, 3, 3, 3, 3, 2, 1, 0}, false, false, false),
   WOOD_BITTER_VETCH(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 0, 1, 2, 3, 3, 3, 2, 2, 2, 1, 0}, false, false, false),
   YARROW(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 0, 1, 2, 2, 3, 3, 3, 3, 2, 1, 0}, false, false, false),
   SUNDEW(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, false, false, false),
   THISTLE(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, new int[]{0, 1, 2, 2, 3, 3, 3, 3, 4, 1, 1, 0}, false, false, false),
   VENUS_FLYTRAP(TFCFPlant.BlockType.TFCF_STANDARD, 0.8F, false, false, false),
   BARREL_CACTUS(
      TFCFPlant.BlockType.FRUITING_CACTUS,
      0.0F,
      new int[]{0, 0, 0, 0, 1, 2, 2, 2, 2, 3, 3, 0},
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.BARREL_CACTUS_FRUIT),
      new Lifecycle[]{
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      false,
      false,
      true,
      false
   ),
   AFRICAN_MILK_BARREL(TFCFPlant.BlockType.SHORT_CACTUS, 0.2F, new int[]{0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0}, false, false, false),
   AFRICAN_MILK_TREE(TFCFPlant.BlockType.TFCF_CACTUS, 0.2F, new int[]{0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0}, false, false, false),
   ALBANIAN_SPURGE(TFCFPlant.BlockType.SHORT_CACTUS, 0.2F, new int[]{0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0}, false, false, false),
   ANGEL_WING_CACTUS(TFCFPlant.BlockType.SHORT_CACTUS, 0.2F, new int[]{0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0}, false, false, false),
   BLUE_CEREUS_CACTUS(TFCFPlant.BlockType.TFCF_CACTUS, 0.2F, new int[]{0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0}, false, false, false),
   CARALLUMA(TFCFPlant.BlockType.SHORT_CACTUS, 0.2F, new int[]{0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0}, false, false, false),
   OCOTILLO(TFCFPlant.BlockType.TFCF_CACTUS, 0.2F, new int[]{0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0}, false, false, false),
   QUAQUA(TFCFPlant.BlockType.SHORT_CACTUS, 0.2F, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1}, false, false, false),
   PRICKLY_PEAR(
      TFCFPlant.BlockType.SHORT_FRUITING_CACTUS,
      0.0F,
      new int[]{0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 0, 0},
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.PRICKLY_PEAR),
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
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT
      },
      false,
      false,
      true,
      false
   ),
   SAGUARO_CACTUS(
      TFCFPlant.BlockType.SAGUARO_CACTUS,
      0.2F,
      new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.SAGUARO),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.HEALTHY,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY
      },
      false,
      false,
      true,
      false
   ),
   PITAHAYA(
      TFCFPlant.BlockType.PITAHAYA,
      0.2F,
      new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.PITAHAYA),
      new Lifecycle[]{
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.HEALTHY,
         Lifecycle.FLOWERING,
         Lifecycle.FLOWERING,
         Lifecycle.FRUITING,
         Lifecycle.FRUITING,
         Lifecycle.DORMANT,
         Lifecycle.DORMANT,
         Lifecycle.HEALTHY
      },
      false,
      false,
      true,
      false
   ),
   GLOW_VINES_PLANT(
      TFCFPlant.BlockType.FRUITING_WEEPING,
      1.0F,
      null,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.GLOW_BERRY),
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
      false,
      false,
      true,
      false
   ),
   GLOW_VINES(
      TFCFPlant.BlockType.FRUITING_WEEPING_TOP,
      1.0F,
      null,
      (Supplier<? extends Item>)TFCFItems.FOOD.get(TFCFFood.GLOW_BERRY),
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
      false,
      false,
      true,
      false
   ),
   BLUE_BAMBOO(TFCFPlant.BlockType.BAMBOO, 0.0F, false, false, false),
   BLUE_BAMBOO_SAPLING(TFCFPlant.BlockType.BAMBOO_SAPLING, 0.5F, false, false, false),
   DRAGON_BAMBOO(TFCFPlant.BlockType.BAMBOO, 0.0F, false, false, false),
   DRAGON_BAMBOO_SAPLING(TFCFPlant.BlockType.BAMBOO_SAPLING, 0.5F, false, false, false),
   RED_BAMBOO(TFCFPlant.BlockType.BAMBOO, 0.0F, false, false, false),
   RED_BAMBOO_SAPLING(TFCFPlant.BlockType.BAMBOO_SAPLING, 0.5F, false, false, false),
   AMANITA(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false, new Color(196, 29, 38).getRGB(), 2),
   ARTISTS_CONK(TFCFPlant.BlockType.FUNGI_EPIPHYTE_SOLID, 0.0F, false, false, false),
   BEEFSTEAK_FUNGUS(TFCFPlant.BlockType.FUNGI_EPIPHYTE, 0.9F, false, false, false),
   BIRCH_POLYPORE(TFCFPlant.BlockType.FUNGI_EPIPHYTE, 0.9F, false, false, false),
   BITTER_OYSTER(TFCFPlant.BlockType.FUNGI_EPIPHYTE, 0.9F, false, false, false),
   BLACK_POWDERPUFF(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false, new Color(194, 183, 160).getRGB(), 3),
   BOLETUS(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false),
   BRIDAL_VEIL_STINKHORN(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false, new Color(209, 204, 185).getRGB(), 3),
   CHANTERELLE(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false),
   CHLOROPHOS_FOXFIRE(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false),
   DEATH_CAP(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false, new Color(105, 109, 41).getRGB(), 2),
   DEVILS_FINGERS(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false, new Color(191, 54, 44).getRGB(), 4),
   DRYADS_SADDLE(TFCFPlant.BlockType.FUNGI_EPIPHYTE_SOLID, 0.9F, false, false, false),
   ENTOLOMA(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false),
   GIANT_CLUB(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false, new Color(189, 183, 119).getRGB(), 2),
   INDIGO_MILK_CAP(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false, new Color(83, 97, 157).getRGB(), 3),
   LIONS_MANE(TFCFPlant.BlockType.FUNGI_EPIPHYTE_HANGING, 0.9F, false, false, false, new Color(218, 213, 183).getRGB(), 6),
   PARASOL_MUSHROOM(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false),
   PORCINI(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false),
   REISHI(TFCFPlant.BlockType.FUNGI_EPIPHYTE, 0.9F, false, false, false),
   SHAGGY_BRACKET(TFCFPlant.BlockType.FUNGI_EPIPHYTE, 0.9F, false, false, false),
   SHIITAKE(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false),
   STINKHORN(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false, new Color(193, 72, 58).getRGB(), 2),
   SULPHUR_SHELF(TFCFPlant.BlockType.FUNGI_EPIPHYTE_SOLID, 0.0F, false, false, false, new Color(242, 233, 148).getRGB(), 5),
   TURKEY_TAIL(TFCFPlant.BlockType.FUNGI_EPIPHYTE_SOLID, 0.0F, false, false, false),
   WEEPING_MILK_CAP(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false),
   WOOD_BLEWIT(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false, new Color(115, 57, 105).getRGB(), 2),
   WOOLLY_GOMPHUS(TFCFPlant.BlockType.FUNGI, 0.9F, false, false, false),
   BLUE_SKYFLOWER(TFCFPlant.BlockType.VINE_TFCF, 0.8F, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1}, false, true, false),
   JADE_VINE(TFCFPlant.BlockType.VINE_TFCF, 0.8F, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0}, false, true, false),
   JAPANESE_IVY(TFCFPlant.BlockType.VINE_TFCF, 0.8F, false, true, false),
   MADEIRA_VINE(TFCFPlant.BlockType.VINE_TFCF, 0.8F, new int[]{1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1}, false, true, false),
   MYSORE_TRUMPETVINE(TFCFPlant.BlockType.VINE_TFCF, 0.8F, new int[]{0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0}, false, true, false),
   SILVERVEIN_CREEPER(TFCFPlant.BlockType.VINE_TFCF, 0.8F, false, true, false),
   SWEDISH_IVY(TFCFPlant.BlockType.VINE_TFCF, 0.8F, false, true, false),
   VARIEGATED_PERSIAN_IVY(TFCFPlant.BlockType.VINE_TFCF, 0.8F, false, true, false),
   WISTERIA(TFCFPlant.BlockType.VINE_TFCF, 0.8F, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0}, false, true, false),
   HANGING_BLUE_SKYFLOWER_PLANT(TFCFPlant.BlockType.WEEPING_TFCF, 0.8F, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1}, false, true, false),
   HANGING_BLUE_SKYFLOWER(TFCFPlant.BlockType.WEEPING_TOP_TFCF, 0.8F, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1}, false, true, false),
   HANGING_JADE_VINE_PLANT(TFCFPlant.BlockType.WEEPING_TFCF, 0.8F, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0}, false, true, false),
   HANGING_JADE_VINE(TFCFPlant.BlockType.WEEPING_TOP_TFCF, 0.8F, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0}, false, true, false),
   HANGING_JAPANESE_IVY_PLANT(TFCFPlant.BlockType.WEEPING_TFCF, 0.8F, false, true, false),
   HANGING_JAPANESE_IVY(TFCFPlant.BlockType.WEEPING_TOP_TFCF, 0.8F, false, true, false),
   HANGING_MADEIRA_VINE_PLANT(TFCFPlant.BlockType.WEEPING_TFCF, 0.8F, new int[]{1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1}, false, true, false),
   HANGING_MADEIRA_VINE(TFCFPlant.BlockType.WEEPING_TOP_TFCF, 0.8F, new int[]{1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1}, false, true, false),
   HANGING_MYSORE_TRUMPETVINE_PLANT(TFCFPlant.BlockType.WEEPING_TFCF, 0.8F, new int[]{0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0}, false, true, false),
   HANGING_MYSORE_TRUMPETVINE(TFCFPlant.BlockType.WEEPING_TOP_TFCF, 0.8F, new int[]{0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0}, false, true, false),
   HANGING_SILVERVEIN_CREEPER_PLANT(TFCFPlant.BlockType.WEEPING_TFCF, 0.8F, false, true, false),
   HANGING_SILVERVEIN_CREEPER(TFCFPlant.BlockType.WEEPING_TOP_TFCF, 0.8F, false, true, false),
   HANGING_SWEDISH_IVY_PLANT(TFCFPlant.BlockType.WEEPING_TFCF, 0.8F, false, true, false),
   HANGING_SWEDISH_IVY(TFCFPlant.BlockType.WEEPING_TOP_TFCF, 0.8F, false, true, false),
   HANGING_VARIEGATED_PERSIAN_IVY_PLANT(TFCFPlant.BlockType.WEEPING_TFCF, 0.8F, false, true, false),
   HANGING_VARIEGATED_PERSIAN_IVY(TFCFPlant.BlockType.WEEPING_TOP_TFCF, 0.8F, false, true, false),
   HANGING_WISTERIA_PLANT(TFCFPlant.BlockType.WEEPING_TFCF, 0.8F, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0}, false, true, false),
   HANGING_WISTERIA(TFCFPlant.BlockType.WEEPING_TOP_TFCF, 0.8F, new int[]{0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0}, false, true, false),
   PONDWEED_PLANT(TFCFPlant.BlockType.KELP_FRESH, 0.6F, false, false, false),
   PONDWEED(TFCFPlant.BlockType.KELP_TOP_FRESH, 0.8F, false, false, false),
   WHITESTEM_PONDWEED(TFCFPlant.BlockType.WATER_FRESH, 0.8F, false, false, false),
   HORNWORT(TFCFPlant.BlockType.WATER_FRESH, 0.9F, false, false, false),
   RED_ALGAE(TFCFPlant.BlockType.WATER, 0.7F, false, false, false),
   RED_SEA_WHIP(TFCFPlant.BlockType.WATER, 0.7F, false, false, false),
   SEA_ANEMONE(TFCFPlant.BlockType.WATER, 0.7F, false, false, false),
   SEAGRASS(TFCFPlant.BlockType.GROWING_PLANT_WATER, 0.6F, false, false, false),
   SEAWEED(TFCFPlant.BlockType.WATER, 0.7F, false, false, false),
   BEACHGRASS(TFCFPlant.BlockType.BEACH_GRASS, 0.8F, false, false, false),
   SEA_PALM(TFCFPlant.BlockType.DRY, 0.8F, false, false, false),
   COBBLESTONE_LICHEN(TFCFPlant.BlockType.CREEPING_STONE, 1.0F, false, false, false),
   FLOATING_GREEN_ALGAE(TFCFPlant.BlockType.FLOATING_FRESH, 0.9F, false, false, false),
   FLOATING_RED_ALGAE(TFCFPlant.BlockType.FLOATING, 0.7F, false, false, false);

   public static final EnumSet<TFCFPlant> ITEM_TINTED_PLANTS = EnumSet.of(
      SHIELD_FERN,
      LICORICE_FERN,
      LEAF_LITTER,
      CAT_GRASS,
      CROWNGRASS,
      GOOSEGRASS,
      WHEATGRASS,
      SAWGRASS,
      CINNAMON_FERN,
      ELEPHANT_GRASS,
      LEAF_LITTER,
      MONSTERA,
      MONSTERA_EPIPHYTE,
      POND_GRASS,
      PRAIRIE_GRASS,
      WOOLLY_BUSH,
      CONIFEROUS_SHRUB,
      DECIDUOUS_SHRUB,
      HORNWORT
   );
   public final float speedFactor;
   @Nullable
   private final IntegerProperty property;
   @Nullable
   public final int[] stagesByMonth;
   public final TFCFPlant.BlockType type;
   public boolean isSeasonalFruitPlant;
   public Supplier<? extends Item> productItem;
   public Lifecycle[] stages;
   public final boolean conifer;
   public final boolean vine;
   public boolean isSeasonal;
   public int sporeColor;
   public int sporeSpread;

   private TFCFPlant(TFCFPlant.BlockType type, float speedFactor, boolean conifer, boolean vine, boolean isSeasonal) {
      this(type, speedFactor, null, conifer, vine, isSeasonal);
      this.isSeasonalFruitPlant = false;
      this.isSeasonal = isSeasonal;
      this.sporeColor = -1;
      this.sporeSpread = 0;
   }

   private TFCFPlant(TFCFPlant.BlockType type, float speedFactor, boolean conifer, boolean vine, boolean isSeasonal, int sporeColor, int sporeSpread) {
      this(type, speedFactor, null, conifer, vine, isSeasonal);
      this.isSeasonalFruitPlant = false;
      this.isSeasonal = isSeasonal;
      this.sporeColor = sporeColor;
      this.sporeSpread = sporeSpread;
   }

   private TFCFPlant(TFCFPlant.BlockType type, float speedFactor, @Nullable int[] stagesByMonth, boolean conifer, boolean vine, boolean isSeasonal) {
      this.type = type;
      this.speedFactor = speedFactor;
      this.stagesByMonth = stagesByMonth;
      this.isSeasonalFruitPlant = false;
      this.isSeasonal = isSeasonal;
      this.conifer = conifer;
      this.vine = vine;
      this.sporeColor = -1;
      this.sporeSpread = 0;
      int maxStage = 0;
      if (stagesByMonth != null) {
         maxStage = Arrays.stream(stagesByMonth).max().orElse(0);
      }

      this.property = maxStage > 0 ? TFCBlockStateProperties.getStageProperty(maxStage) : null;
   }

   private TFCFPlant(
      TFCFPlant.BlockType type,
      float speedFactor,
      @Nullable int[] stagesByMonth,
      Supplier<? extends Item> productItem,
      Lifecycle[] stages,
      boolean conifer,
      boolean vine,
      boolean isSeasonalFruitPlant,
      boolean isSeasonal
   ) {
      this.type = type;
      this.speedFactor = speedFactor;
      this.stagesByMonth = stagesByMonth;
      this.isSeasonalFruitPlant = true;
      this.isSeasonal = isSeasonal;
      this.conifer = conifer;
      this.productItem = productItem;
      this.stages = stages;
      this.vine = vine;
      this.sporeColor = -1;
      this.sporeSpread = 0;
      int maxStage = 0;
      if (stagesByMonth != null) {
         maxStage = Arrays.stream(stagesByMonth).max().orElse(0);
      }

      this.property = maxStage > 0 ? TFCBlockStateProperties.getStageProperty(maxStage) : null;
   }

   public Block create() {
      return this.type.factory.apply(this, this.type);
   }

   @Nullable
   public Function<Block, BlockItem> createBlockItem(Properties properties) {
      return this.needsItem() ? block -> this.type.blockItemFactory.apply(block, properties) : null;
   }

   public int stageFor(Month month) {
      assert this.stagesByMonth != null;

      return this.stagesByMonth.length < month.ordinal() ? 0 : this.stagesByMonth[month.ordinal()];
   }

   @Nullable
   public IntegerProperty getStageProperty() {
      return this.property;
   }

   public boolean needsItem() {
      return !TFCFPlant.BlockType.NO_ITEM_TYPES.contains(this.type);
   }

   public boolean isFoliage() {
      return TFCFPlant.BlockType.FOLIAGE_TYPES.contains(this.type);
   }

   public boolean isSeasonal() {
      return this.type == TFCFPlant.BlockType.VINE || this.isSeasonal;
   }

   public boolean isTallGrass() {
      return this.type == TFCFPlant.BlockType.TALL_GRASS || this.type == TFCFPlant.BlockType.SHORT_GRASS;
   }

   public boolean isBambooSapling() {
      return this.type == TFCFPlant.BlockType.BAMBOO_SAPLING;
   }

   public boolean isItemTinted() {
      return ITEM_TINTED_PLANTS.contains(this);
   }

   public boolean hasFlowerPot() {
      return this.type == TFCFPlant.BlockType.STANDARD
         || this.type == TFCFPlant.BlockType.DRY
         || this.type == TFCFPlant.BlockType.TFCF_STANDARD
         || this.type == TFCFPlant.BlockType.BAMBOO;
   }

   public Boolean isSeasonalFruitPlant() {
      return this.isSeasonalFruitPlant;
   }

   @Nullable
   public Supplier<? extends Item> getProductItem() {
      return this.productItem;
   }

   public Lifecycle[] getStages() {
      return this.stages;
   }

   public boolean isConifer() {
      return this.conifer;
   }

   public boolean isVine() {
      return this.vine;
   }

   public int getSporeColor() {
      return this.sporeColor;
   }

   public int getSporeSpread() {
      return this.sporeSpread;
   }

   public Supplier<? extends Block> transform() {
      return (Supplier<? extends Block>)TFCFBlocks.PLANTS.get(switch (this) {
         case BEARDED_MOSS -> BEARDED_MOSS_PLANT;
         case BEARDED_MOSS_PLANT -> BEARDED_MOSS;
         case GLOW_VINES -> GLOW_VINES_PLANT;
         case GLOW_VINES_PLANT -> GLOW_VINES;
         case HANGING_BLUE_SKYFLOWER_PLANT -> HANGING_BLUE_SKYFLOWER;
         case HANGING_BLUE_SKYFLOWER -> HANGING_BLUE_SKYFLOWER_PLANT;
         case HANGING_JADE_VINE_PLANT -> HANGING_JADE_VINE;
         case HANGING_JADE_VINE -> HANGING_JADE_VINE_PLANT;
         case HANGING_JAPANESE_IVY_PLANT -> HANGING_JAPANESE_IVY;
         case HANGING_JAPANESE_IVY -> HANGING_JAPANESE_IVY_PLANT;
         case HANGING_MADEIRA_VINE_PLANT -> HANGING_MADEIRA_VINE;
         case HANGING_MADEIRA_VINE -> HANGING_MADEIRA_VINE_PLANT;
         case HANGING_MYSORE_TRUMPETVINE_PLANT -> HANGING_MYSORE_TRUMPETVINE;
         case HANGING_MYSORE_TRUMPETVINE -> HANGING_MYSORE_TRUMPETVINE_PLANT;
         case HANGING_SILVERVEIN_CREEPER_PLANT -> HANGING_SILVERVEIN_CREEPER;
         case HANGING_SILVERVEIN_CREEPER -> HANGING_SILVERVEIN_CREEPER_PLANT;
         case HANGING_SWEDISH_IVY_PLANT -> HANGING_SWEDISH_IVY;
         case HANGING_SWEDISH_IVY -> HANGING_SWEDISH_IVY_PLANT;
         case HANGING_VARIEGATED_PERSIAN_IVY_PLANT -> HANGING_VARIEGATED_PERSIAN_IVY;
         case HANGING_VARIEGATED_PERSIAN_IVY -> HANGING_VARIEGATED_PERSIAN_IVY_PLANT;
         case HANGING_WISTERIA_PLANT -> HANGING_WISTERIA;
         case HANGING_WISTERIA -> HANGING_WISTERIA_PLANT;
         case PONDWEED_PLANT -> PONDWEED;
         case PONDWEED -> PONDWEED_PLANT;
         default -> throw new IllegalStateException("Uhh why did you try to transform something that's not a tall plant? </3");
      });
   }

   public Supplier<? extends Block> stem() {
      return (Supplier<? extends Block>)TFCFBlocks.PLANTS.get(switch (this) {
         case BLUE_BAMBOO_SAPLING -> BLUE_BAMBOO;
         case DRAGON_BAMBOO_SAPLING -> DRAGON_BAMBOO;
         case RED_BAMBOO_SAPLING -> RED_BAMBOO;
         default -> this;
      });
   }

   public Supplier<? extends Block> sapling() {
      return (Supplier<? extends Block>)TFCFBlocks.PLANTS.get(switch (this) {
         case BLUE_BAMBOO -> BLUE_BAMBOO_SAPLING;
         case DRAGON_BAMBOO -> DRAGON_BAMBOO_SAPLING;
         case RED_BAMBOO -> RED_BAMBOO_SAPLING;
         default -> throw new IllegalStateException("Pls fix plant sapling asap </3");
      });
   }

   public Supplier<? extends Block> transformFruiting() {
      return (Supplier<? extends Block>)TFCFBlocks.FRUITING_PLANTS.get(switch (this) {
         case GLOW_VINES -> GLOW_VINES_PLANT;
         case GLOW_VINES_PLANT -> GLOW_VINES;
         default -> null;
      });
   }

   public static enum BlockType {
      STANDARD((plant, type) -> PlantBlock.create(plant, fire(nonSolid(plant)))),
      CACTUS((plant, type) -> TFCCactusBlock.create(plant, fire(solid().strength(0.25F).sound(SoundType.WOOL)).pathType(BlockPathTypes.DAMAGE_OTHER))),
      DRY((plant, type) -> net.dries007.tfc.common.blocks.plant.PlantBlock.createDry(plant, fire(nonSolid(plant)))),
      CREEPING((plant, type) -> CreepingPlantBlock.create(plant, fire(nonSolid(plant).hasPostProcess(TFCFBlocks::always)))),
      EPIPHYTE((plant, type) -> EpiphytePlantBlock.create(plant, fire(nonSolid(plant).hasPostProcess(TFCFBlocks::always)))),
      SHORT_GRASS((plant, type) -> ShortGrassBlock.create(plant, fire(nonSolid(plant)))),
      DESERT_SHORT_GRASS((plant, type) -> DesertShortGrassBlock.create(plant, fire(nonSolid(plant)))),
      TALL_GRASS((plant, type) -> TFCTallGrassBlock.create(plant, fire(nonSolid(plant)))),
      VINE((plant, type) -> new TFCVineBlock(fire(nonSolid(plant)))),
      WEEPING((plant, type) -> new BodyPlantBlock(fire(nonSolidTallPlant(plant)), plant.transform(), BodyPlantBlock.BODY_SHAPE, Direction.DOWN)),
      WEEPING_TOP((plant, type) -> new TopPlantBlock(fire(nonSolidTallPlant(plant)), plant.transform(), Direction.DOWN, BodyPlantBlock.WEEPING_SHAPE)),
      TWISTING((plant, type) -> new BodyPlantBlock(fire(nonSolidTallPlant(plant)), plant.transform(), BodyPlantBlock.BODY_SHAPE, Direction.UP)),
      TWISTING_TOP((plant, type) -> new TopPlantBlock(fire(nonSolidTallPlant(plant)), plant.transform(), Direction.UP, BodyPlantBlock.TWISTING_SHAPE)),
      TWISTING_SOLID((plant, type) -> new BodyPlantBlock(fire(solidTallPlant()), plant.transform(), BodyPlantBlock.BODY_SHAPE, Direction.UP)),
      TWISTING_SOLID_TOP((plant, type) -> new TopPlantBlock(fire(solidTallPlant()), plant.transform(), Direction.UP, BodyPlantBlock.TWISTING_SHAPE)),
      VINE_TFCF((plant, type) -> TFCFVineBlock.create(plant, fire(nonSolid(plant)).randomTicks())),
      WEEPING_TFCF(
         (plant, type) -> TFCFBodyPlantBlock.create(
               plant, fire(nonSolidTallPlant(plant)).randomTicks(), plant.transform(), BodyPlantBlock.BODY_SHAPE, Direction.DOWN
            )
      ),
      WEEPING_TOP_TFCF(
         (plant, type) -> TFCFTopPlantBlock.create(
               plant, fire(nonSolidTallPlant(plant)).randomTicks(), plant.transform(), Direction.DOWN, BodyPlantBlock.WEEPING_SHAPE
            )
      ),
      TFCF_STANDARD((plant, type) -> TFCFPlantBlock.create(plant, fire(nonSolid(plant)))),
      CREEPING_WATER((plant, type) -> CreepingWaterPlantBlock.create(plant, fire(nonSolid(plant).hasPostProcess(TFCFBlocks::always)))),
      SPORE_BLOSSOM((plant, type) -> SporeBlossomPlantBlock.create(plant, fire(nonSolid(plant).hasPostProcess(TFCFBlocks::always)).sound(SoundType.SPORE_BLOSSOM))),
      FRUITING_WEEPING(
         (plant, type) -> new FruitingBodyPlantBlock(
               ExtendedProperties.of(nonSolidTallPlant(plant).hasPostProcess(TFCFBlocks::always))
                  .lightLevel(FruitingBodyPlantBlock.emission(14, true))
                  .instabreak()
                  .sound(SoundType.CAVE_VINES)
                  .randomTicks(),
               plant.transform(),
               BodyPlantBlock.BODY_SHAPE,
               Direction.DOWN,
               plant.getProductItem(),
               plant.getStages(),
               TFCFClimateRanges.SEASONAL_PLANT.get(plant)
            )
      ),
      FRUITING_WEEPING_TOP(
         (plant, type) -> new FruitingTopPlantBlock(
               ExtendedProperties.of(nonSolidTallPlant(plant).hasPostProcess(TFCFBlocks::always))
                  .lightLevel(FruitingBodyPlantBlock.emission(14, true))
                  .instabreak()
                  .sound(SoundType.CAVE_VINES)
                  .randomTicks(),
               plant.transform(),
               BodyPlantBlock.WEEPING_SHAPE,
               Direction.DOWN,
               plant.getProductItem(),
               plant.getStages(),
               TFCFClimateRanges.SEASONAL_PLANT.get(plant)
            )
      ),
      TALL_LAND_WATER((plant, type) -> TallLandWaterPlantBlock.create(plant, TFCBlockStateProperties.SALT_WATER, nonSolid(plant))),
      TALL_LAND_WATER_FRESH((plant, type) -> TallLandWaterPlantBlock.create(plant, TFCBlockStateProperties.FRESH_WATER, nonSolid(plant))),
      POISONOUS_TALL_GRASS((plant, type) -> TFCFTallGrassBlock.create(plant, fire(nonSolid(plant)))),
      SHORT_CACTUS(
         (plant, type) -> ShortCactusBlock.create(plant, fire(solid().strength(0.25F).sound(SoundType.MOSS)).pathType(BlockPathTypes.DAMAGE_OTHER))
      ),
      TFCF_CACTUS(
         (plant, type) -> TFCFCactusBlock.create(plant, fire(solid().strength(0.25F).sound(SoundType.MOSS)).pathType(BlockPathTypes.DAMAGE_OTHER))
      ),
      SAGUARO_CACTUS(
         (plant, type) -> SaguaroCactusBlock.create(
               plant,
               fire(solid().strength(0.25F).sound(SoundType.MOSS).hasPostProcess(TFCFBlocks::always)).pathType(BlockPathTypes.DAMAGE_OTHER).randomTicks(),
               plant.getProductItem(),
               plant.getStages(),
               TFCFClimateRanges.SEASONAL_PLANT.get(plant)
            )
      ),
      PITAHAYA(
         (plant, type) -> PitahayaBlock.create(
               plant,
               fire(solid().strength(0.25F).sound(SoundType.NETHER_WART).hasPostProcess(TFCFBlocks::always)).pathType(BlockPathTypes.DAMAGE_OTHER).randomTicks(),
               plant.getProductItem(),
               plant.getStages(),
               TFCFClimateRanges.SEASONAL_PLANT.get(plant)
            )
      ),
      SHORT_FRUITING_CACTUS(
         (plant, type) -> ShortFruitingCactusBlock.create(
               plant,
               fire(solid().strength(0.25F).sound(SoundType.MOSS).hasPostProcess(TFCFBlocks::always)).pathType(BlockPathTypes.DAMAGE_OTHER).randomTicks(),
               plant.getProductItem(),
               plant.getStages(),
               TFCFClimateRanges.SEASONAL_PLANT.get(plant)
            )
      ),
      FRUITING_CACTUS(
         (plant, type) -> TFCFFruitingCactusBlock.create(
               plant,
               fire(solid().strength(0.25F).sound(SoundType.MOSS).hasPostProcess(TFCFBlocks::always)).pathType(BlockPathTypes.DAMAGE_OTHER).randomTicks(),
               plant.getProductItem(),
               plant.getStages(),
               TFCFClimateRanges.SEASONAL_PLANT.get(plant)
            )
      ),
      FUNGI((plant, type) -> FungiBlock.create(plant, plant, fire(nonSolid(plant)))),
      FUNGI_EPIPHYTE((plant, type) -> FungiEpiphyteBlock.create(plant, plant, fire(nonSolid(plant).hasPostProcess(TFCFBlocks::always)))),
      FUNGI_EPIPHYTE_SOLID((plant, type) -> FungiEpiphyteSolidBlock.create(plant, plant, fire(solid().strength(0.25F).hasPostProcess(TFCFBlocks::always)))),
      FUNGI_EPIPHYTE_HANGING(
         (plant, type) -> FungiEpiphyteCeilingBlock.create(plant, plant, fire(nonSolid(plant).strength(0.25F).hasPostProcess(TFCFBlocks::always)))
      ),
      FRUITING_TALL_PLANT(
         (plant, type) -> FruitingTallPlantBlock.create(
               plant,
               fire(nonSolid(plant).hasPostProcess(TFCFBlocks::always)).randomTicks(),
               plant.getProductItem(),
               plant.getStages(),
               TFCFClimateRanges.SEASONAL_PLANT.get(plant)
            )
      ),
      CACAO(
         (plant, type) -> CacaoPodBlock.create(
               plant, fire(nonSolid(plant).hasPostProcess(TFCFBlocks::always)).randomTicks().strength(0.2F, 3.0F).sound(SoundType.BAMBOO).noOcclusion()
            )
      ),
      BAMBOO(
         (plant, type) -> BambooPlantBlock.create(
               plant,
               ExtendedProperties.of(MapColor.PLANT)
                  .randomTicks()
                  .instabreak()
                  .strength(1.0F)
                  .sound(SoundType.BAMBOO)
                  .noOcclusion()
                  .dynamicShape()
                  .hasPostProcess(TFCFBlocks::always),
               plant.stem(),
               plant.sapling()
            )
      ),
      BAMBOO_SAPLING(
         (plant, type) -> BambooSaplingPlantBlock.create(
               plant,
               ExtendedProperties.of(MapColor.PLANT)
                  .randomTicks()
                  .instabreak()
                  .noCollission()
                  .strength(1.0F)
                  .sound(SoundType.BAMBOO_SAPLING)
                  .hasPostProcess(TFCFBlocks::always),
               plant.stem()
            )
      ),
      KELP(
         (plant, type) -> TFCKelpBlock.create(
               nonSolidTallPlant(plant).lootFrom(plant.transform()).sound(SoundType.WET_GRASS),
               plant.transform(),
               Direction.UP,
               BodyPlantBlock.THIN_BODY_SHAPE,
               TFCBlockStateProperties.SALT_WATER
            )
      ),
      KELP_TOP(
         (plant, type) -> TFCKelpTopBlock.create(
               nonSolidTallPlant(plant).sound(SoundType.WET_GRASS),
               plant.transform(),
               Direction.UP,
               BodyPlantBlock.TWISTING_THIN_SHAPE,
               TFCBlockStateProperties.SALT_WATER
            )
      ),
      KELP_TREE((plant, type) -> KelpTreeBlock.create(ExtendedProperties.of(kelp(plant)), TFCBlockStateProperties.SALT_WATER)),
      KELP_TREE_FLOWER((plant, type) -> KelpTreeFlowerBlock.create(kelp(plant), plant.transform())),
      FLOATING((plant, type) -> FloatingWaterPlantBlock.create(plant, TFCFluids.SALT_WATER.source(), nonSolid(plant)), PlaceOnWaterBlockItem::new),
      FLOATING_FRESH((plant, type) -> FloatingWaterPlantBlock.create(plant, () -> Fluids.WATER, nonSolid(plant)), PlaceOnWaterBlockItem::new),
      TALL_WATER((plant, type) -> TallWaterPlantBlock.create(plant, TFCBlockStateProperties.SALT_WATER, nonSolid(plant))),
      TALL_WATER_FRESH((plant, type) -> TallWaterPlantBlock.create(plant, TFCBlockStateProperties.FRESH_WATER, nonSolid(plant))),
      WATER((plant, type) -> WaterPlantBlock.create(plant, TFCBlockStateProperties.SALT_WATER, nonSolid(plant).sound(SoundType.WET_GRASS))),
      WATER_FRESH((plant, type) -> WaterPlantBlock.create(plant, TFCBlockStateProperties.FRESH_WATER, nonSolid(plant).sound(SoundType.WET_GRASS))),
      GRASS_WATER((plant, type) -> TFCSeagrassBlock.create(plant, TFCBlockStateProperties.SALT_WATER, nonSolid(plant).sound(SoundType.WET_GRASS))),
      GRASS_WATER_FRESH((plant, type) -> TFCSeagrassBlock.create(plant, TFCBlockStateProperties.FRESH_WATER, nonSolid(plant).sound(SoundType.WET_GRASS))),
      KELP_FRESH(
         (plant, type) -> TFCKelpBlock.create(
               nonSolidTallPlant(plant).lootFrom(plant.transform()).sound(SoundType.WET_GRASS),
               plant.transform(),
               Direction.UP,
               BodyPlantBlock.THIN_BODY_SHAPE,
               TFCBlockStateProperties.FRESH_WATER
            )
      ),
      KELP_TOP_FRESH(
         (plant, type) -> TFCKelpTopBlock.create(
               nonSolidTallPlant(plant).sound(SoundType.WET_GRASS),
               plant.transform(),
               Direction.UP,
               BodyPlantBlock.TWISTING_THIN_SHAPE,
               TFCBlockStateProperties.FRESH_WATER
            )
      ),
      GROWING_PLANT_WATER_FRESH(
         (plant, type) -> TFCFSeagrassBlock.create(plant, TFCBlockStateProperties.FRESH_WATER, nonSolid(plant).randomTicks().sound(SoundType.WET_GRASS))
      ),
      GROWING_PLANT_WATER(
         (plant, type) -> TFCFSeagrassBlock.create(plant, TFCBlockStateProperties.SALT_WATER, nonSolid(plant).randomTicks().sound(SoundType.WET_GRASS))
      ),
      TALL_WATER_SMALL_DRIPLEAF((plant, type) -> SmallDripleafPlantBlock.create(plant, TFCBlockStateProperties.SALT_WATER, nonSolid(plant))),
      TALL_WATER_FRESH_SMALL_DRIPLEAF((plant, type) -> SmallDripleafPlantBlock.create(plant, TFCBlockStateProperties.FRESH_WATER, nonSolid(plant))),
      BEACH_GRASS((plant, type) -> DesertShortGrassBlock.createBeachGrass(plant, fire(nonSolid(plant)))),
      CREEPING_STONE((plant, type) -> CreepingStonePlantBlock.createStone(plant, fire(nonSolid(plant).hasPostProcess(TFCBlocks::always))));

      public static final EnumSet<TFCFPlant.BlockType> NO_ITEM_TYPES = EnumSet.of(WEEPING, TWISTING_SOLID, KELP, KELP_TREE, TWISTING);
      public static final EnumSet<TFCFPlant.BlockType> FOLIAGE_TYPES = EnumSet.of(
         WEEPING, WEEPING_TOP, FLOATING_FRESH, FLOATING, WATER_FRESH, GRASS_WATER_FRESH, GRASS_WATER
      );
      public final BiFunction<TFCFPlant, TFCFPlant.BlockType, ? extends Block> factory;
      public final BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory;

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties solid() {
         return net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
            .noOcclusion()
            .sound(SoundType.GRASS)
            .randomTicks()
            .hasPostProcess(TFCFBlocks::always);
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties nonSolid(TFCFPlant plant) {
         return solid().instabreak().speedFactor(plant.speedFactor).noCollission().hasPostProcess(TFCFBlocks::always);
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties solidTallPlant() {
         return net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
            .randomTicks()
            .sound(SoundType.WEEPING_VINES)
            .hasPostProcess(TFCFBlocks::always);
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties nonSolidTallPlant(TFCFPlant plant) {
         return solidTallPlant().instabreak().noCollission().speedFactor(plant.speedFactor).hasPostProcess(TFCFBlocks::always);
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties kelp(TFCFPlant plant) {
         return net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
            .noCollission()
            .randomTicks()
            .speedFactor(plant.speedFactor)
            .strength(1.0F)
            .sound(SoundType.WET_GRASS)
            .hasPostProcess(TFCFBlocks::always);
      }

      public static ExtendedProperties fire(net.minecraft.world.level.block.state.BlockBehaviour.Properties properties) {
         return ExtendedProperties.of(properties).flammable(60, 30);
      }

      private BlockType(BiFunction<TFCFPlant, TFCFPlant.BlockType, ? extends Block> factory) {
         this(factory, BlockItem::new);
      }

      private BlockType(
         BiFunction<TFCFPlant, TFCFPlant.BlockType, ? extends Block> factory, BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory
      ) {
         this.factory = factory;
         this.blockItemFactory = blockItemFactory;
      }
   }
}
