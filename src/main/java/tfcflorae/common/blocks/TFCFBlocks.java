package tfcflorae.common.blocks;
import net.minecraft.core.registries.Registries;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.TFCCreativeTabs;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blockentities.LoomBlockEntity;
import net.dries007.tfc.common.blockentities.SluiceBlockEntity;
import net.dries007.tfc.common.blocks.DecorationBlockRegistryObject;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.Gem;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.blocks.OreDeposit;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.TFCMagmaBlock;
import net.dries007.tfc.common.blocks.plant.BodyPlantBlock;
import net.dries007.tfc.common.blocks.rock.MossGrowingSlabBlock;
import net.dries007.tfc.common.blocks.rock.MossGrowingStairsBlock;
import net.dries007.tfc.common.blocks.rock.MossGrowingWallBlock;
import net.dries007.tfc.common.blocks.rock.MossSpreadingSlabBlock;
import net.dries007.tfc.common.blocks.rock.MossSpreadingStairBlock;
import net.dries007.tfc.common.blocks.rock.MossSpreadingWallBlock;
import net.dries007.tfc.common.blocks.rock.Ore;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.blocks.rock.Ore.Grade;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.soil.TFCRootedDirtBlock;
import net.dries007.tfc.common.blocks.soil.TFCSandBlock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.common.blocks.wood.LogBlock;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;
import net.dries007.tfc.common.blocks.wood.TFCLecternBlock;
import net.dries007.tfc.common.blocks.wood.TFCStandingSignBlock;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.dries007.tfc.common.blocks.wood.TFCWallSignBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.common.items.ChestBlockItem;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.dries007.tfc.util.registry.RegistryWood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.client.TFCFSounds;
import tfcflorae.common.blockentities.SilkmothNestBlockEntity;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blocks.ceramics.Clay;
import tfcflorae.common.blocks.ceramics.LargeVesselBlock;
import tfcflorae.common.blocks.devices.SilkmothNestBlock;
import tfcflorae.common.blocks.devices.TFCFBarrelBlock;
import tfcflorae.common.blocks.devices.TFCFSluiceBlock;
import tfcflorae.common.blocks.plant.BambooSaplingPlantBlock;
import tfcflorae.common.blocks.plant.FruitingBodyPlantBlock;
import tfcflorae.common.blocks.plant.FruitingTallPlantBlock;
import tfcflorae.common.blocks.plant.FruitingTopPlantBlock;
import tfcflorae.common.blocks.plant.PitahayaBlock;
import tfcflorae.common.blocks.plant.SaguaroCactusBlock;
import tfcflorae.common.blocks.plant.ShortFruitingCactusBlock;
import tfcflorae.common.blocks.plant.TFCFFruitingCactusBlock;
import tfcflorae.common.blocks.plant.TFCFPlant;
import tfcflorae.common.blocks.rock.DripstoneRock;
import tfcflorae.common.blocks.rock.Geyser;
import tfcflorae.common.blocks.rock.MineralSheetBlock;
import tfcflorae.common.blocks.rock.TFCFOre;
import tfcflorae.common.blocks.rock.TFCFOreDepositBlock;
import tfcflorae.common.blocks.rock.TFCFPointedDripstoneBlock;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.blocks.rock.TFCFRockAnvilBlock;
import tfcflorae.common.blocks.soil.Colors;
import tfcflorae.common.blocks.soil.SandLayerBlock;
import tfcflorae.common.blocks.soil.TFCFRockSand;
import tfcflorae.common.blocks.soil.TFCFRockSoil;
import tfcflorae.common.blocks.soil.TFCFSandstoneBlockType;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.common.blocks.spidercave.BodyWebBlock;
import tfcflorae.common.blocks.spidercave.CreepingSpiderWebBlock;
import tfcflorae.common.blocks.spidercave.EggBlock;
import tfcflorae.common.blocks.spidercave.WebbedBlock;
import tfcflorae.common.blocks.spidercave.WebbedChestBlock;
import tfcflorae.common.blocks.wood.BambooLogBlock;
import tfcflorae.common.blocks.wood.ChiseledBookshelfBlock;
import tfcflorae.common.blocks.wood.TFCFChestBlock;
import tfcflorae.common.blocks.wood.TFCFFruitingLogBlock;
import tfcflorae.common.blocks.wood.TFCFJoshuaLeavesBlock;
import tfcflorae.common.blocks.wood.TFCFJoshuaTrunkBlock;
import tfcflorae.common.blocks.wood.TFCFLeavesBlock;
import tfcflorae.common.blocks.wood.TFCFLeavesBlockExt;
import tfcflorae.common.blocks.wood.TFCFLoomBlock;
import tfcflorae.common.blocks.wood.TFCFMangroveLeavesBlock;
import tfcflorae.common.blocks.wood.TFCFMangroveMuddyRootsBlocks;
import tfcflorae.common.blocks.wood.TFCFMangrovePropaguleBlock;
import tfcflorae.common.blocks.wood.TFCFMangroveRootsBlocks;
import tfcflorae.common.blocks.wood.TFCFToolRackBlock;
import tfcflorae.common.blocks.wood.TFCFTrappedChestBlock;
import tfcflorae.common.blocks.wood.TFCFWallBlock;
import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.common.blocks.wood.VanillaBambooSaplingBlock;
import tfcflorae.common.fluid.SimpleFluids;
import tfcflorae.common.fluid.TFCFFluids;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.util.TFCFHelpers;
import tfcflorae.util.climate.TFCFClimateRanges;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;

public final class TFCFBlocks {
   public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "tfcflorae");

   /**
    * 不再注册的土壤类型：
    * <ul>
    *   <li>所有土径（{@code *_grass_path}）—— TFC 1.20 已有 {@code tfc:grass_path/<variant>}，重复；</li>
    *   <li>{@code packed_mud} / {@code mossy_packed_mud} / {@code loose_mud} / {@code coarse_dirt} —— 与 TFC 的泥巴/泥土体系重复或不需要；</li>
    *   <li>所有干燥/稠密/稀疏草方块（{@code dry_*} / {@code dense_*} / {@code sparse_*}，含黏土、陶土、瓷土版本）
    *       —— 放置后会变成可透视的树叶方块，是坏的实现，直接去掉。</li>
    * </ul>
    * 枚举常量本身保留（其它地方仍有 switch/transform 引用），注册时给这些类型放**空表**，
    * 这样 {@code TFCFSOIL.get(type).values()} 是空集合而不是 NPE。
    */
   private static final java.util.Set<TFCFSoil> REMOVED_SOILS = java.util.EnumSet.of(
      TFCFSoil.GRASS_PATH, TFCFSoil.DENSE_GRASS_PATH, TFCFSoil.SPARSE_GRASS_PATH, TFCFSoil.DRY_GRASS_PATH,
      TFCFSoil.PACKED_MUD, TFCFSoil.MOSSY_PACKED_MUD, TFCFSoil.LOOSE_MUD, TFCFSoil.COARSE_DIRT,
      TFCFSoil.DRY_GRASS, TFCFSoil.DENSE_GRASS, TFCFSoil.SPARSE_GRASS,
      TFCFSoil.DRY_CLAY_GRASS, TFCFSoil.DENSE_CLAY_GRASS, TFCFSoil.SPARSE_CLAY_GRASS,
      TFCFSoil.DRY_EARTHENWARE_CLAY_GRASS, TFCFSoil.DENSE_EARTHENWARE_CLAY_GRASS, TFCFSoil.SPARSE_EARTHENWARE_CLAY_GRASS,
      TFCFSoil.DRY_STONEWARE_CLAY_GRASS, TFCFSoil.DENSE_STONEWARE_CLAY_GRASS, TFCFSoil.SPARSE_STONEWARE_CLAY_GRASS,
      TFCFSoil.PODZOL
   );

   public static final Map<TFCFSoil, Map<TFCFSoil.TFCFVariant, RegistryObject<Block>>> TFCFSOIL = tfcfSoilMap(TFCFSoil.class);
   public static final Map<TFCFSoil, Map<Variant, RegistryObject<Block>>> TFCSOIL = TFCSoilMap(TFCFSoil.class);
   public static final Map<TFCFSoil.TFCFVariant, DecorationBlockRegistryObject> MUD_BRICK_DECORATIONS = Helpers.mapOfKeys(
      TFCFSoil.TFCFVariant.class,
      variant -> new DecorationBlockRegistryObject(
            register("mud_bricks/" + variant.name() + "_slab", () -> new SlabBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).sound(SoundType.MUD_BRICKS).strength(2.6F)), TFCCreativeTabs.DECORATIONS),
            register(
               "mud_bricks/" + variant.name() + "_stairs",
               () -> new StairBlock(() -> ((Block)TFCFSOIL.get(TFCFSoil.MUD_BRICKS).get(variant).get()).defaultBlockState(), net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).sound(SoundType.MUD_BRICKS).strength(2.6F)),
               TFCCreativeTabs.DECORATIONS
            ),
            register("mud_bricks/" + variant.name() + "_wall", () -> new WallBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).sound(SoundType.MUD_BRICKS).strength(2.6F)), TFCCreativeTabs.DECORATIONS)
         )
   );
   public static final RegistryObject<Block> BOG_IRON = register(
      "dirt/bog_iron",
      () -> new Block(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN)
               .strength(3.0F)
               .sound(TFCSounds.PEAT)
         ),
      TFCCreativeTabs.EARTH
   );
   public static final RegistryObject<Block> SPARSE_BOG_IRON_GRASS = register(
      "sparse_grass/bog_iron",
      () -> new ConnectedGrassBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN)
               .randomTicks()
               .strength(3.0F)
               .sound(TFCSounds.PEAT),
            BOG_IRON,
            null,
            null
         ),
      TFCCreativeTabs.EARTH
   );
   public static final RegistryObject<Block> DENSE_BOG_IRON_GRASS = register(
      "dense_grass/bog_iron",
      () -> new ConnectedGrassBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.GRASS)
               .randomTicks()
               .strength(3.0F)
               .sound(TFCSounds.PEAT),
            SPARSE_BOG_IRON_GRASS,
            null,
            null
         ),
      TFCCreativeTabs.EARTH
   );
   public static final RegistryObject<Block> BOG_IRON_GRASS = register(
      "grass/bog_iron",
      () -> new ConnectedGrassBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.GRASS)
               .randomTicks()
               .strength(3.0F)
               .sound(TFCSounds.PEAT),
            DENSE_BOG_IRON_GRASS,
            null,
            null
         ),
      TFCCreativeTabs.EARTH
   );
   public static final RegistryObject<Block> ROOTED_BOG_IRON = register(
      "rooted_dirt/bog_iron",
      () -> new TFCRootedDirtBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN)
               .strength(3.5F)
               .sound(SoundType.ROOTED_DIRT),
            BOG_IRON,
            null
         ),
      TFCCreativeTabs.EARTH
   );
   public static final RegistryObject<Block> MYCELIUM_BOG_IRON = register(
      "mycelium_dirt/bog_iron",
      () -> new TFCRootedDirtBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN)
               .strength(3.5F)
               .sound(SoundType.ROOTED_DIRT),
            BOG_IRON,
            null
         ),
      TFCCreativeTabs.EARTH
   );
   public static final Map<Colors, RegistryObject<Block>> SPARSE_SAND_GRASS = Helpers.mapOfKeys(
      Colors.class,
      type -> register(
            "sand/sparse_grass/" + type.name(),
            () -> new ConnectedGrassBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                     .randomTicks()
                     .strength(3.0F)
                     .sound(SoundType.BIG_DRIPLEAF),
                  type.hasSandTFC() ? (Supplier)TFCBlocks.SAND.get(type.toSandTFC(true)) : (Supplier)TFCFBlocks.SAND.get(type),
                  null,
                  null
               ),
            TFCCreativeTabs.EARTH
         )
   );
   public static final Map<Colors, RegistryObject<Block>> DENSE_SAND_GRASS = Helpers.mapOfKeys(
      Colors.class,
      type -> register(
            "sand/dense_grass/" + type.name(),
            () -> new ConnectedGrassBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.GRASS)
                     .randomTicks()
                     .strength(3.0F)
                     .sound(SoundType.BIG_DRIPLEAF),
                  (Supplier)SPARSE_SAND_GRASS.get(type),
                  null,
                  null
               ),
            TFCCreativeTabs.EARTH
         )
   );
   public static final Map<Colors, RegistryObject<Block>> SAND_GRASS = Helpers.mapOfKeys(
      Colors.class,
      type -> register(
            "sand/grass/" + type.name(),
            () -> new ConnectedGrassBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.GRASS)
                     .randomTicks()
                     .strength(3.0F)
                     .sound(SoundType.BIG_DRIPLEAF),
                  (Supplier)DENSE_SAND_GRASS.get(type),
                  null,
                  null
               ),
            TFCCreativeTabs.EARTH
         )
   );
   public static final Map<TFCFRockSand, Map<Colors, Map<Rock, RegistryObject<Block>>>> ROCKY_SAND_TFC = TFCRockSandMapper(TFCFRockSand.class);
   public static final Map<TFCFRockSand, Map<Colors, Map<TFCFRock, RegistryObject<Block>>>> ROCKY_SAND_TFCF = TFCFRockSandMapper(TFCFRockSand.class);
   public static final Map<Colors, Map<TFCFSandstoneBlockType, RegistryObject<Block>>> SANDSTONE = Helpers.mapOfKeys(
      Colors.class,
      color -> color.hasLayered(),
      color -> Helpers.mapOfKeys(
            TFCFSandstoneBlockType.class,
            type -> register(
                  "sandstone/" + type.name() + "/" + color.name(),
                  () -> new Block(type.properties(color).sound(SoundType.ANCIENT_DEBRIS)),
                  TFCCreativeTabs.DECORATIONS
               )
         )
   );
   public static final Map<Colors, Map<TFCFSandstoneBlockType, DecorationBlockRegistryObject>> SANDSTONE_DECORATIONS = Helpers.mapOfKeys(
      Colors.class,
      color -> color.hasLayered(),
      color -> Helpers.mapOfKeys(
            TFCFSandstoneBlockType.class,
            type -> new DecorationBlockRegistryObject(
                  register(
                     "sandstone/" + type.name() + "/slab/" + color.name(),
                     () -> new SlabBlock(type.properties(color).sound(SoundType.ANCIENT_DEBRIS)),
                     TFCCreativeTabs.DECORATIONS
                  ),
                  register(
                     "sandstone/" + type.name() + "/stairs/" + color.name(),
                     () -> new StairBlock(() -> ((Block)SANDSTONE.get(color).get(type).get()).defaultBlockState(), type.properties(color).sound(SoundType.ANCIENT_DEBRIS)),
                     TFCCreativeTabs.DECORATIONS
                  ),
                  register(
                     "sandstone/" + type.name() + "/wall/" + color.name(),
                     () -> new WallBlock(type.properties(color).sound(SoundType.ANCIENT_DEBRIS)),
                     TFCCreativeTabs.DECORATIONS
                  )
               )
         )
   );
   public static final Map<Colors, RegistryObject<Block>> WEATHERED_TERRACOTTA = Helpers.mapOfKeys(
      Colors.class, type -> register("sand/weathered_terracotta/" + type.name(), type::create, TFCCreativeTabs.EARTH)
   );
   public static final Map<Colors, RegistryObject<Block>> SAND = sandMap(Colors.class);
   public static final Map<Colors, RegistryObject<Block>> SAND_LAYERS = sandLayerMap(Colors.class);
   public static final Map<TFCFGroundcoverBlockType, RegistryObject<Block>> GROUNDCOVER = Helpers.mapOfKeys(
      TFCFGroundcoverBlockType.class, type -> register("groundcover/" + type.name(), () -> new TFCFGroundcoverBlock(type), type.createBlockItem())
   );
   // TFC 的这 8 种矿石只以物品形式存在（宝石 / 火药类），从未提供方块贴图与模型。
   // 在岩石上注册成矿方块只会得到紫黑方块，因此不生成。
   private static final java.util.Set<Ore> ORE_WITHOUT_BLOCK = java.util.EnumSet.of(
      Ore.DIAMOND, Ore.EMERALD, Ore.LAPIS_LAZULI, Ore.OPAL, Ore.PYRITE, Ore.SALTPETER, Ore.SAPPHIRE, Ore.TOPAZ
   );
   // 凝灰岩（TUFF）不生成矿石 / 矿粒 / 矿床：这些资源从未随本模组补齐，
   // 注册出来必然是紫黑方块。外层 mapOfKeys 直接过滤掉 TUFF 这个键。
   public static final Map<TFCFRock, Map<Ore, RegistryObject<Block>>> ORES = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> rock != TFCFRock.TUFF,
      rock -> Helpers.mapOfKeys(
            Ore.class, ore -> !ore.isGraded() && !ORE_WITHOUT_BLOCK.contains(ore), ore -> register("ore/" + ore.name() + "/" + rock.name(), () -> ore.create(rock), TFCCreativeTabs.ORES)
         )
   );
   public static final Map<TFCFRock, Map<Ore, Map<Grade, RegistryObject<Block>>>> GRADED_ORES = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> rock != TFCFRock.TUFF,
      rock -> Helpers.mapOfKeys(
            Ore.class,
            Ore::isGraded,
            ore -> Helpers.mapOfKeys(
                  Grade.class, grade -> register("ore/" + grade.name() + "_" + ore.name() + "/" + rock.name(), () -> ore.create(rock), TFCCreativeTabs.ORES)
               )
         )
   );
   public static final Map<TFCFRock, Map<OreDeposit, RegistryObject<Block>>> ORE_DEPOSITS = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> rock != TFCFRock.TUFF,
      rock -> Helpers.mapOfKeys(
            OreDeposit.class,
            ore -> register(
                  "deposit/" + ore.name() + "/" + rock.name(),
                  () -> new TFCFOreDepositBlock(
                        net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(rock.color())
                           .sound(SoundType.GRAVEL)
                           .strength(rock.category().hardness(2.0F)),
                        rock,
                        ore
                     ),
                  TFCCreativeTabs.ORES
               )
         )
   );
   // TFC 岩石上的矿石：TFC 自己已经提供了全部矿石（含分级矿），
   // 本模组在这部分真正独有的只有非分级的 ANTHRACITE，其余一律不再重复注册。
   public static final Map<Rock, Map<TFCFOre, RegistryObject<Block>>> ORES_TFC_ROCK = Helpers.mapOfKeys(
      Rock.class,
      rock -> Helpers.mapOfKeys(
            TFCFOre.class,
            ore -> !ore.isGraded() && ore != TFCFOre.ANTHRACITE,
            ore -> register("ore/" + ore.name() + "/" + rock.name(), () -> ore.create(rock), TFCCreativeTabs.ORES)
         )
   );
   public static final Map<Rock, Map<TFCFOre, Map<TFCFOre.Grade, RegistryObject<Block>>>> GRADED_ORES_TFC_ROCK = Helpers.mapOfKeys(
      Rock.class,
      rock -> Helpers.mapOfKeys(
            TFCFOre.class,
            ore -> false,
            ore -> Helpers.mapOfKeys(
                  TFCFOre.Grade.class,
                  grade -> register("ore/" + grade.name() + "_" + ore.name() + "/" + rock.name(), () -> ore.create(rock), TFCCreativeTabs.ORES)
               )
         )
   );
   public static final Map<TFCFRock, Map<TFCFOre, RegistryObject<Block>>> ORES_TFCF_ROCK = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> Helpers.mapOfKeys(
            TFCFOre.class, ore -> !ore.isGraded() && ore != TFCFOre.ANTHRACITE, ore -> register("ore/" + ore.name() + "/" + rock.name(), () -> ore.create(rock), TFCCreativeTabs.ORES)
         )
   );
   public static final Map<TFCFRock, Map<TFCFOre, Map<TFCFOre.Grade, RegistryObject<Block>>>> GRADED_ORES_TFCF_ROCK = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> Helpers.mapOfKeys(
            TFCFOre.class,
            TFCFOre::isGraded,
            ore -> Helpers.mapOfKeys(
                  TFCFOre.Grade.class,
                  grade -> register("ore/" + grade.name() + "_" + ore.name() + "/" + rock.name(), () -> ore.create(rock), TFCCreativeTabs.ORES)
               )
         )
   );
   public static final RegistryObject<Block> GLOWSTONE = register(
      "crystal/glowstone",
      () -> new AmethystBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops()
               .strength(1.5F)
               .sound(SoundType.AMETHYST)
               .requiresCorrectToolForDrops()
               .lightLevel(state -> 15)
         ),
      TFCCreativeTabs.ORES
   );
   public static final RegistryObject<Block> GLOWSTONE_BUDDING = register(
      "crystal/budding/glowstone",
      () -> new BuddingAmethystBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops()
               .randomTicks()
               .strength(1.5F)
               .sound(SoundType.AMETHYST)
               .requiresCorrectToolForDrops()
               .lightLevel(state -> 15)
         ),
      TFCCreativeTabs.ORES
   );
   public static final RegistryObject<Block> GLOWSTONE_CLUSTER = register(
      "crystal/cluster/glowstone",
      () -> new AmethystClusterBlock(
            7,
            3,
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops()
               .noOcclusion()
               .randomTicks()
               .sound(SoundType.AMETHYST_CLUSTER)
               .strength(1.5F)
               .lightLevel(state -> 10)
         ),
      TFCCreativeTabs.ORES
   );
   public static final RegistryObject<Block> LARGE_GLOWSTONE_BUD = register(
      "crystal/large_bud/glowstone",
      () -> new AmethystClusterBlock(
            5,
            3,
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops()
               .noOcclusion()
               .randomTicks()
               .sound(SoundType.LARGE_AMETHYST_BUD)
               .strength(1.5F)
               .lightLevel(state -> 9)
         ),
      TFCCreativeTabs.ORES
   );
   public static final RegistryObject<Block> MEDIUM_GLOWSTONE_BUD = register(
      "crystal/medium_bud/glowstone",
      () -> new AmethystClusterBlock(
            4,
            3,
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops()
               .noOcclusion()
               .randomTicks()
               .sound(SoundType.MEDIUM_AMETHYST_BUD)
               .strength(1.5F)
               .lightLevel(state -> 7)
         ),
      TFCCreativeTabs.ORES
   );
   public static final RegistryObject<Block> SMALL_GLOWSTONE_BUD = register(
      "crystal/small_bud/glowstone",
      () -> new AmethystClusterBlock(
            3,
            4,
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops()
               .noOcclusion()
               .randomTicks()
               .sound(SoundType.SMALL_AMETHYST_BUD)
               .strength(1.5F)
               .lightLevel(state -> 6)
         ),
      TFCCreativeTabs.ORES
   );
   public static final Map<Gem, RegistryObject<Block>> CRYSTAL = Helpers.mapOfKeys(
      Gem.class,
      gem -> gem == net.dries007.tfc.common.blocks.Gem.AMETHYST || gem == net.dries007.tfc.common.blocks.Gem.RUBY,
      gem -> register(
            "crystal/" + gem.name(),
            () -> new AmethystBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(TFCFHelpers.gemColor(gem)).requiresCorrectToolForDrops()
                     .strength(1.5F)
                     .sound(SoundType.AMETHYST)
                     .lightLevel(state -> 5)
                     .requiresCorrectToolForDrops()
               ),
            TFCCreativeTabs.ORES
         )
   );
   public static final Map<Gem, RegistryObject<Block>> BUDDING_CRYSTAL = Helpers.mapOfKeys(
      Gem.class,
      gem -> gem == net.dries007.tfc.common.blocks.Gem.AMETHYST || gem == net.dries007.tfc.common.blocks.Gem.RUBY,
      gem -> register(
            "crystal/budding/" + gem.name(),
            () -> new BuddingAmethystBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(TFCFHelpers.gemColor(gem)).requiresCorrectToolForDrops()
                     .randomTicks()
                     .strength(1.5F)
                     .sound(SoundType.AMETHYST)
                     .lightLevel(state -> 5)
                     .requiresCorrectToolForDrops()
               ),
            TFCCreativeTabs.ORES
         )
   );
   public static final Map<Gem, RegistryObject<Block>> CLUSTER_CRYSTAL = Helpers.mapOfKeys(
      Gem.class,
      gem -> gem == net.dries007.tfc.common.blocks.Gem.AMETHYST || gem == net.dries007.tfc.common.blocks.Gem.RUBY,
      gem -> register(
            "crystal/cluster/" + gem.name(),
            () -> new AmethystClusterBlock(
                  7,
                  3,
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(TFCFHelpers.gemColor(gem)).requiresCorrectToolForDrops()
                     .noOcclusion()
                     .randomTicks()
                     .sound(SoundType.AMETHYST_CLUSTER)
                     .strength(1.5F)
                     .lightLevel(state -> 5)
               ),
            TFCCreativeTabs.ORES
         )
   );
   public static final Map<Gem, RegistryObject<Block>> LARGE_BUD_CRYSTAL = Helpers.mapOfKeys(
      Gem.class,
      gem -> gem == net.dries007.tfc.common.blocks.Gem.AMETHYST || gem == net.dries007.tfc.common.blocks.Gem.RUBY,
      gem -> register(
            "crystal/large_bud/" + gem.name(),
            () -> new AmethystClusterBlock(
                  5,
                  3,
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(TFCFHelpers.gemColor(gem)).requiresCorrectToolForDrops()
                     .noOcclusion()
                     .randomTicks()
                     .sound(SoundType.LARGE_AMETHYST_BUD)
                     .strength(1.5F)
                     .lightLevel(state -> 4)
               ),
            TFCCreativeTabs.ORES
         )
   );
   public static final Map<Gem, RegistryObject<Block>> MEDIUM_BUD_CRYSTAL = Helpers.mapOfKeys(
      Gem.class,
      gem -> gem == net.dries007.tfc.common.blocks.Gem.AMETHYST || gem == net.dries007.tfc.common.blocks.Gem.RUBY,
      gem -> register(
            "crystal/medium_bud/" + gem.name(),
            () -> new AmethystClusterBlock(
                  4,
                  3,
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(TFCFHelpers.gemColor(gem)).requiresCorrectToolForDrops()
                     .noOcclusion()
                     .randomTicks()
                     .sound(SoundType.MEDIUM_AMETHYST_BUD)
                     .strength(1.5F)
                     .lightLevel(state -> 2)
               ),
            TFCCreativeTabs.ORES
         )
   );
   public static final Map<Gem, RegistryObject<Block>> SMALL_BUD_CRYSTAL = Helpers.mapOfKeys(
      Gem.class,
      gem -> gem == net.dries007.tfc.common.blocks.Gem.AMETHYST || gem == net.dries007.tfc.common.blocks.Gem.RUBY,
      gem -> register(
            "crystal/small_bud/" + gem.name(),
            () -> new AmethystClusterBlock(
                  3,
                  4,
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(TFCFHelpers.gemColor(gem)).requiresCorrectToolForDrops()
                     .noOcclusion()
                     .randomTicks()
                     .sound(SoundType.SMALL_AMETHYST_BUD)
                     .strength(1.5F)
                     .lightLevel(state -> 1)
               ),
            TFCCreativeTabs.ORES
         )
   );
   public static final Map<Rock, Map<TFCFRock.TFCFBlockType, RegistryObject<Block>>> ROCK_BLOCKS = rockTypeTFCMapper(Rock.class);
   public static final Map<TFCFRock, Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, RegistryObject<Block>>> TFCF_ROCK_BLOCKS = rockMapper(
      TFCFRock.class
   );
   public static final Map<TFCFRock, Map<TFCFRock.TFCFBlockType, RegistryObject<Block>>> TFCF_ROCKTYPE_BLOCKS = rockTypeMapper(TFCFRock.class);
   public static final Map<DripstoneRock, Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, RegistryObject<Block>>> DRIPSTONE_BLOCKS = dripstoneMapper(
      DripstoneRock.class
   );
   public static final Map<Rock, RegistryObject<Block>> TFC_COLUMN = Helpers.mapOfKeys(
      Rock.class,
      rock -> register(
            "rock/column/" + rock.name(),
            () -> new RotatedPillarBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(rock.color()).requiresCorrectToolForDrops()
                     .sound(SoundType.BASALT)
                     .strength(rock.category().hardness(9.0F), 15.0F)
                     .requiresCorrectToolForDrops()
               ),
            TFCCreativeTabs.ROCKS
         )
   );
   public static final Map<TFCFRock, RegistryObject<Block>> TFCF_COLUMN = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> register(
            "rock/column/" + rock.name(),
            () -> new RotatedPillarBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(rock.color()).requiresCorrectToolForDrops()
                     .sound(SoundType.BASALT)
                     .strength(rock.category().hardness(9.0F), 15.0F)
                     .requiresCorrectToolForDrops()
               ),
            TFCCreativeTabs.ROCKS
         )
   );
   public static final Map<Rock, RegistryObject<Block>> TFC_POLISHED_COLUMN = Helpers.mapOfKeys(
      Rock.class,
      rock -> register(
            "rock/polished_column/" + rock.name(),
            () -> new RotatedPillarBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(rock.color()).requiresCorrectToolForDrops()
                     .sound(SoundType.BASALT)
                     .strength(rock.category().hardness(9.0F), 15.0F)
                     .requiresCorrectToolForDrops()
               ),
            TFCCreativeTabs.ROCKS
         )
   );
   public static final Map<TFCFRock, RegistryObject<Block>> TFCF_POLISHED_COLUMN = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> register(
            "rock/polished_column/" + rock.name(),
            () -> new RotatedPillarBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(rock.color()).requiresCorrectToolForDrops()
                     .sound(SoundType.BASALT)
                     .strength(rock.category().hardness(9.0F), 15.0F)
                     .requiresCorrectToolForDrops()
               ),
            TFCCreativeTabs.ROCKS
         )
   );
   // 板石（flagstone）整套已废弃：资源已删除，这里不再注册任何方块
   public static final Map<Rock, RegistryObject<Block>> TFC_FLAGSTONE = Map.of();
   // 板石（flagstone）整套已废弃：资源已删除，这里不再注册任何方块
   public static final Map<TFCFRock, RegistryObject<Block>> TFCF_FLAGSTONE = Map.of();
   // 板石（flagstone）整套已废弃：资源已删除，这里不再注册任何方块
   public static final Map<Rock, DecorationBlockRegistryObject> TFC_FLAGSTONE_DECORATIONS = Map.of();
   // 板石（flagstone）整套已废弃：资源已删除，这里不再注册任何方块
   public static final Map<TFCFRock, DecorationBlockRegistryObject> TFCF_FLAGSTONE_DECORATIONS = Map.of();
   public static final Map<Rock, RegistryObject<Block>> TFC_SMOKE_BLOCK = Helpers.mapOfKeys(
      Rock.class,
      rock -> register(
            "rock/smoke_block/" + rock.name(),
            () -> new SmokeBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(rock.color()).requiresCorrectToolForDrops()
                     .sound(SoundType.CALCITE)
                     .strength(rock.category().hardness(9.0F), 15.0F)
                     .requiresCorrectToolForDrops(),
                  true
               ),
            TFCCreativeTabs.ROCKS
         )
   );
   public static final Map<TFCFRock, RegistryObject<Block>> TFCF_SMOKE_BLOCK = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> register(
            "rock/smoke_block/" + rock.name(),
            () -> new SmokeBlock(
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(rock.color()).requiresCorrectToolForDrops()
                     .sound(SoundType.CALCITE)
                     .strength(rock.category().hardness(9.0F), 15.0F)
                     .requiresCorrectToolForDrops(),
                  true
               ),
            TFCCreativeTabs.ROCKS
         )
   );
   public static final Map<TFCFRock, RegistryObject<Block>> ROCK_ANVILS = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> rock != TFCFRock.TUFF && (rock.category() == RockCategory.IGNEOUS_EXTRUSIVE || rock.category() == RockCategory.IGNEOUS_INTRUSIVE),
      rock -> registerRockBlock(
            "rock/anvil/" + rock.name(),
            () -> new TFCFRockAnvilBlock(
                  ExtendedProperties.of(rock.color()).requiresCorrectToolForDrops()
                     .sound(SoundType.STONE)
                     .strength(2.0F, 10.0F)
                     .requiresCorrectToolForDrops()
                     .blockEntity(TFCFBlockEntities.ANVIL),
                  (Supplier<? extends Block>)TFCF_ROCK_BLOCKS.get(rock).get(net.dries007.tfc.common.blocks.rock.Rock.BlockType.RAW)
               ),
            rock != TFCFRock.TUFF
         )
   );
   public static final Map<TFCFRock, RegistryObject<Block>> MAGMA_BLOCKS = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> rock.category() == RockCategory.IGNEOUS_EXTRUSIVE || rock.category() == RockCategory.IGNEOUS_INTRUSIVE,
      rock -> register(
            "rock/magma/" + rock.name(),
            () -> new TFCMagmaBlock(
                  ExtendedProperties.of(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(rock.color()).requiresCorrectToolForDrops()
                     .requiresCorrectToolForDrops()
                     .lightLevel(s -> 6)
                     .randomTicks()
                     .strength(0.5F)
                     .isValidSpawn((state, level, pos, type) -> type.fireImmune())
                     .hasPostProcess(TFCFBlocks::always))
               ),
            TFCCreativeTabs.ROCKS
         )
   );
   // TFC 原生 20 种岩石的间歇泉只有通用部件模型，没有 blockstate / 物品模型，
   // 注册出来是紫黑方块；本模组岩石的间歇泉资源齐全，保留在下面的表里。
   public static final Map<Rock, RegistryObject<Block>> GEYSER_TFC = Map.of();
   public static final Map<TFCFRock, RegistryObject<Block>> GEYSER_TFCF = Helpers.mapOfKeys(
      TFCFRock.class,
      rock -> rock != TFCFRock.TUFF,
      rock -> register(
            "rock/geyser/" + rock.name(),
            () -> Geyser.create(
                  ExtendedProperties.of(rock.color()).requiresCorrectToolForDrops()
                     .sound(SoundType.BASALT)
                     .strength(rock.category().hardness(9.0F), 15.0F)
                     .requiresCorrectToolForDrops()
                     .noOcclusion()
                     .randomTicks()
                     .dynamicShape()
                     .hasPostProcess(TFCFBlocks::always),
                  (java.util.function.Supplier<? extends Block>) null,
                  true
               ),
            TFCCreativeTabs.ROCKS
         )
   );
   public static final Map<TFCFRock, Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, DecorationBlockRegistryObject>> TFCF_ROCKTYPE_DECORATIONS = RockTypeDecoTFCMapper(
      TFCFRock.class
   );
   public static final Map<Rock, Map<TFCFRock.TFCFBlockType, DecorationBlockRegistryObject>> TFC_ROCK_DECORATIONS = RockDecoTFCMapper(Rock.class);
   public static final Map<TFCFRock, Map<TFCFRock.TFCFBlockType, DecorationBlockRegistryObject>> TFCF_ROCK_DECORATIONS = RockDecoTFCFMapper(TFCFRock.class);
   public static final RegistryObject<Block> MINERAL_SHEET = register(
      "mineral/mineral_sheet",
      () -> new MineralSheetBlock(
            ExtendedProperties.of(MapColor.TERRACOTTA_GRAY).requiresCorrectToolForDrops()
               .strength(0.5F, 4.0F)
               .sound(TFCSounds.CHARCOAL)
               .noCollission()
               .noOcclusion()
               .blockEntity(TFCFBlockEntities.MINERAL_SHEET)
         )
   );
   public static final RegistryObject<Block> CHARRED_TREE_LOG = register(
      "wood/log/charred_tree",
      () -> new LogBlock(
            ExtendedProperties.of(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.TERRACOTTA_BLACK : MapColor.TERRACOTTA_BLACK)
               .strength(8.0F)
               .sound(TFCSounds.CHARCOAL)
               .requiresCorrectToolForDrops()
               .flammableLikeLogs(),
            TFCFBlocks.CHARRED_TREE_STRIPPED_LOG
         ),
      TFCCreativeTabs.WOOD
   );
   public static final RegistryObject<Block> CHARRED_TREE_STRIPPED_LOG = register(
      "wood/stripped_log/charred_tree",
      () -> new LogBlock(
            ExtendedProperties.of(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? MapColor.TERRACOTTA_BLACK : MapColor.TERRACOTTA_BLACK)
               .strength(7.5F)
               .sound(TFCSounds.CHARCOAL)
               .requiresCorrectToolForDrops()
               .flammableLikeLogs(),
            null
         ),
      TFCCreativeTabs.WOOD
   );
   public static final RegistryObject<Block> CHARRED_TREE_WOOD = register(
      "wood/wood/charred_tree",
      () -> new LogBlock(
            ExtendedProperties.of(MapColor.TERRACOTTA_BLACK)
               .strength(8.0F)
               .sound(TFCSounds.CHARCOAL)
               .requiresCorrectToolForDrops()
               .flammableLikeLogs(),
            TFCFBlocks.CHARRED_TREE_STRIPPED_WOOD
         ),
      TFCCreativeTabs.WOOD
   );
   public static final RegistryObject<Block> CHARRED_TREE_STRIPPED_WOOD = register(
      "wood/stripped_wood/charred_tree",
      () -> new LogBlock(
            ExtendedProperties.of(MapColor.TERRACOTTA_BLACK)
               .strength(7.5F)
               .sound(TFCSounds.CHARCOAL)
               .requiresCorrectToolForDrops()
               .flammableLikeLogs(),
            null
         ),
      TFCCreativeTabs.WOOD
   );
   public static final RegistryObject<Block> CHARRED_TREE_TWIG = register(
      "wood/twig/charred_tree",
      () -> GroundcoverBlock.twig(
            ExtendedProperties.of(MapColor.TERRACOTTA_BLACK).strength(0.05F, 0.0F).sound(TFCSounds.CHARCOAL).noCollission().flammableLikeWool()
         ),
      TFCCreativeTabs.WOOD
   );
   public static final Map<TFCFWood, Map<BlockType, RegistryObject<Block>>> WOODS = woodMapper(TFCFWood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> WOODS_SEASONAL_LEAVES = seasonalLeavesMapper(TFCFWood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> WOODS_SEASONAL_LOGS = seasonalLogMapper(TFCFWood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> WOODS_SEASONAL_WOOD = seasonalWoodMapper(TFCFWood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> LEAVES_ONLY = leavesOnlyMapper(TFCFWood.class);
   public static final Map<Wood, RegistryObject<Block>> NORMAL_BOOKSHELF_TFC = normalBookshelfMapperTFC(Wood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> CHISELED_BOOKSHELF_TFCF = chiseledBookshelfMapperTFCF(TFCFWood.class);
   public static final Map<Wood, RegistryObject<Block>> LOG_WALL_TFC = woodWallMapperTFC(Wood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> LOG_WALL_TFCF = woodWallMapperTFCF(TFCFWood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> MANGROVE_ROOTS = mangroveRootsMapper(TFCFWood.class);
   public static final Map<TFCFWood, Map<Variant, RegistryObject<Block>>> TFC_MUDDY_MANGROVE_ROOTS = TFCmangroveRootsMuddyMapper(TFCFWood.class);
   public static final Map<TFCFWood, Map<TFCFSoil.TFCFVariant, RegistryObject<Block>>> TFCF_MUDDY_MANGROVE_ROOTS = TFCFmangroveRootsMuddyMapper(TFCFWood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> ROOT_SPIKES = woodSpikeMapper(TFCFWood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> JOSHUA_TRUNK = joshuaTrunkMapper(TFCFWood.class);
   public static final Map<TFCFWood, RegistryObject<Block>> JOSHUA_LEAVES = joshuaLeavesMapper(TFCFWood.class);
   public static final Map<TFCFPlant, RegistryObject<Block>> BAMBOO_LOGS = bambooLogMapper(TFCFPlant.class);
   public static final Map<TFCFPlant, RegistryObject<Block>> STRIPPED_BAMBOO_LOGS = bambooStrippedLogMapper(TFCFPlant.class);
   public static final Map<TFCFPlant, RegistryObject<Block>> BAMBOO_LEAVES = bambooLeavesMapper(TFCFPlant.class);
   public static final Map<TFCFPlant, RegistryObject<Block>> BAMBOO_SAPLINGS = bambooSaplingMapper(TFCFPlant.class);
   public static final RegistryObject<Block> VANILLA_BAMBOO_LOGS = register(
      "wood/log/bamboo",
      () -> new BambooLogBlock(
            ExtendedProperties.of(MapColor.COLOR_LIGHT_GREEN)
               .sound(SoundType.WOOD)
               .strength(5.5F)
               .requiresCorrectToolForDrops()
               .flammableLikeLogs()
               .sound(SoundType.BAMBOO)
               .noOcclusion()
               .dynamicShape()
               .hasPostProcess(TFCFBlocks::always),
            TFCFBlocks.VANILLA_STRIPPED_BAMBOO_LOGS
         ),
      TFCCreativeTabs.WOOD
   );
   public static final RegistryObject<Block> VANILLA_STRIPPED_BAMBOO_LOGS = register(
      "wood/stripped_log/bamboo",
      () -> new BambooLogBlock(
            ExtendedProperties.of(MapColor.COLOR_LIGHT_GREEN)
               .sound(SoundType.WOOD)
               .strength(5.5F)
               .requiresCorrectToolForDrops()
               .flammableLikeLogs()
               .sound(SoundType.BAMBOO)
               .noOcclusion()
               .dynamicShape()
               .hasPostProcess(TFCFBlocks::always),
            null
         ),
      TFCCreativeTabs.WOOD
   );
   public static final RegistryObject<Block> BAMBOO_MOSAIC = register(
      "wood/mosaic/bamboo",
      () -> new ExtendedBlock(
            ExtendedProperties.of(MapColor.COLOR_LIGHT_GREEN).sound(SoundType.WOOD).strength(1.5F, 3.0F).flammableLikePlanks()
         ),
      TFCCreativeTabs.WOOD
   );
   public static final Map<TFCFPlant, RegistryObject<Block>> BAMBOO_BUNDLED_LOGS = bambooBundledLogMapper(TFCFPlant.class);
   public static final Map<TFCFPlant, RegistryObject<Block>> PLANTS = normalPlantMapper(TFCFPlant.class);
   public static final Map<TFCFPlant, RegistryObject<Block>> FRUITING_PLANTS = seasonalPlantMapper(TFCFPlant.class);
   public static final Map<TFCFPlant, RegistryObject<Block>> POTTED_PLANTS = Helpers.mapOfKeys(
      TFCFPlant.class,
      TFCFPlant::hasFlowerPot,
      plant -> register(
            "plant/potted/" + plant.name(),
            () -> new FlowerPotBlock(
                  () -> (FlowerPotBlock)Blocks.FLOWER_POT,
                  (Supplier)PLANTS.get(plant),
                  net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).instabreak().noOcclusion()
               )
         )
   );
   public static final RegistryObject<SlabBlock> FIRE_BRICKS_SLAB = register(
      "ceramic/fire_bricks/brick_slab",
      () -> new SlabBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).requiresCorrectToolForDrops().requiresCorrectToolForDrops().strength(2.0F, 6.0F)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<StairBlock> FIRE_BRICKS_STAIRS = register(
      "ceramic/fire_bricks/brick_stairs",
      () -> new StairBlock(
            () -> ((Block)TFCBlocks.FIRE_BRICKS.get()).defaultBlockState(),
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).requiresCorrectToolForDrops().requiresCorrectToolForDrops().strength(2.0F, 6.0F)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<WallBlock> FIRE_BRICKS_WALL = register(
      "ceramic/fire_bricks/brick_wall",
      () -> new WallBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_RED).requiresCorrectToolForDrops().requiresCorrectToolForDrops().strength(2.0F, 6.0F)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final Map<Clay, RegistryObject<Block>> CLAY_BLOCKS = clayBlocksMap(Clay.class);
   public static final Map<Clay, RegistryObject<Block>> CLAY_BRICKS = clayBricksMap(Clay.class);
   public static final Map<Clay, DecorationBlockRegistryObject> CLAY_DECO_BLOCKS = clayDecoMap(Clay.class);
   public static final Map<Clay, RegistryObject<Block>> CLAY_LARGE_VESSELS = clayLargeVesselMap(Clay.class);
   public static final RegistryObject<Block> SPIDER_EGG = register(
      "spider_cave/spider_egg",
      () -> new EggBlock(
            ExtendedProperties.of(MapColor.COLOR_CYAN)
               .strength(1.0F)
               .instabreak()
               .lightLevel(state -> 5)
               .sound(TFCFSounds.MUD)
               .speedFactor(0.3F)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> SPIDER_EGGS = register(
      "spider_cave/spider_eggs",
      () -> new EggBlock(
            ExtendedProperties.of(MapColor.TERRACOTTA_WHITE)
               .strength(1.0F)
               .instabreak()
               .noOcclusion()
               .sound(TFCFSounds.MUD)
               .speedFactor(0.3F)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> LARGE_SPIDER_EGG = register(
      "spider_cave/spider_egg_large",
      () -> new EggBlock(
            ExtendedProperties.of(MapColor.TERRACOTTA_LIGHT_GRAY)
               .strength(1.0F)
               .instabreak()
               .noOcclusion()
               .sound(TFCFSounds.MUD)
               .speedFactor(0.3F)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> WEBBED_GLOW_BLOCK = register(
      "spider_cave/webbed_glow",
      () -> new WebbedBlock(
            ExtendedProperties.of(MapColor.TERRACOTTA_LIGHT_GREEN)
               .strength(1.0F)
               .noOcclusion()
               .lightLevel(state -> 6)
               .sound(TFCFSounds.MUD)
               .speedFactor(0.5F)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> WEBBED_TORCH_BLOCK = register(
      "spider_cave/webbed_torch",
      () -> new WebbedBlock(
            ExtendedProperties.of(MapColor.TERRACOTTA_YELLOW)
               .strength(1.0F)
               .noOcclusion()
               .lightLevel(state -> 9)
               .sound(TFCFSounds.MUD)
               .speedFactor(0.5F)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> WEBBED_CHEST = register(
      "spider_cave/webbed_chest",
      () -> new WebbedChestBlock(
            ExtendedProperties.of(MapColor.TERRACOTTA_RED)
               .blockEntity(TFCFBlockEntities.WEBBED_CHEST)
               .strength(1.0F)
               .noOcclusion()
               .sound(SoundType.WOOD)
               .speedFactor(1.0F)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> CREEPING_WEBS = register(
      "spider_cave/creeping_webs",
      () -> new CreepingSpiderWebBlock(
            ExtendedProperties.of(MapColor.COLOR_LIGHT_GRAY)
               .noCollission()
               .strength(1.0F)
               .noOcclusion()
               .sound(SoundType.TWISTING_VINES)
               .speedFactor(0.7F)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> HANGING_SPIDER_WEB_SLENDER = register(
      "spider_cave/hanging_spider_web_slender",
      () -> new BodyWebBlock(
            ExtendedProperties.of(MapColor.COLOR_LIGHT_GRAY)
               .sound(SoundType.TWISTING_VINES)
               .noCollission()
               .strength(3.0F)
               .noOcclusion()
               .randomTicks()
               .dynamicShape()
               .speedFactor(0.7F)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> HANGING_SPIDER_WEB_THICK = register(
      "spider_cave/hanging_spider_web_thick",
      () -> new BodyWebBlock(
            ExtendedProperties.of(MapColor.COLOR_LIGHT_GRAY)
               .sound(SoundType.TWISTING_VINES)
               .noCollission()
               .strength(4.0F)
               .noOcclusion()
               .randomTicks()
               .dynamicShape()
               .speedFactor(0.9F)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> SILKMOTH_NEST = register(
      "devices/silkmoth_nest",
      () -> new SilkmothNestBlock(
            ExtendedProperties.of(MapColor.COLOR_BROWN)
               .sound(SoundType.HANGING_ROOTS)
               .flammableLikeLeaves()
               .strength(1.0F)
               .noOcclusion()
               .randomTicks()
               .dynamicShape()
               .blockEntity(TFCFBlockEntities.SILKMOTH_NEST)
               .serverTicks(SilkmothNestBlockEntity::serverTick)
               .hasPostProcess(TFCFBlocks::always)
         ),
      TFCCreativeTabs.MISC
   );
   public static final RegistryObject<Block> OCHRE_FROGLIGHT = register(
      "frog/ochre_froglight",
      () -> new RotatedPillarBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
               .instabreak()
               .lightLevel(value -> 15)
               .sound(TFCFSounds.FROGLIGHT)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> VERDANT_FROGLIGHT = register(
      "frog/verdant_froglight",
      () -> new RotatedPillarBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN)
               .instabreak()
               .lightLevel(value -> 15)
               .sound(TFCFSounds.FROGLIGHT)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   public static final RegistryObject<Block> PEARLESCENT_FROGLIGHT = register(
      "frog/pearlescent_froglight",
      () -> new RotatedPillarBlock(
            net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE)
               .instabreak()
               .lightLevel(value -> 15)
               .sound(TFCFSounds.FROGLIGHT)
         ),
      TFCCreativeTabs.DECORATIONS
   );
   // 蛙卵（frogspawn）随青蛙/蝌蚪移除；蛙明灯（froglight）是独立发光方块，保留
   public static final Map<TFCFRockSoil, Map<Variant, Map<Rock, RegistryObject<Block>>>> TFCROCKSOIL = TFCRockSoilMapper(TFCFRockSoil.class);
   public static final Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<Rock, RegistryObject<Block>>>> TFCFROCKSOIL = TFCFRockSoilMapper(TFCFRockSoil.class);
   public static final Map<TFCFRockSoil, Map<Variant, Map<TFCFRock, RegistryObject<Block>>>> TFCROCKSOIL2 = TFCRockSoil2Mapper(TFCFRockSoil.class);
   public static final Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<TFCFRock, RegistryObject<Block>>>> TFCFROCKSOIL2 = TFCFRockSoil2Mapper(
      TFCFRockSoil.class
   );
   public static final Map<TFCFRockSoil, Map<Variant, Map<Rock, DecorationBlockRegistryObject>>> TFCROCKSOILDECO = TFCRockSoilDecoMapper(TFCFRockSoil.class);
   public static final Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<Rock, DecorationBlockRegistryObject>>> TFCFROCKSOILDECO = TFCFRockSoilDecoMapper(
      TFCFRockSoil.class
   );
   public static final Map<TFCFRockSoil, Map<Variant, Map<TFCFRock, DecorationBlockRegistryObject>>> TFCROCKSOILDECO2 = TFCRockSoilDeco2Mapper(
      TFCFRockSoil.class
   );
   public static final Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<TFCFRock, DecorationBlockRegistryObject>>> TFCFROCKSOILDECO2 = TFCFRockSoilDeco2Mapper(
      TFCFRockSoil.class
   );
   public static final Map<SimpleFluids, RegistryObject<LiquidBlock>> SIMPLE_FLUIDS = new HashMap<>();

   public TFCFBlocks() {
   }

   public static boolean always(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }

   public static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
      return false;
   }

   public static boolean never(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type) {
      return false;
   }

   public static int lightEmission(BlockState state) {
      return state.getValue(BlockStateProperties.LIT) ? 15 : 0;
   }

   private static Map<Clay, RegistryObject<Block>> clayBlocksMap(Class<Clay> enumClass) {
      Map<Clay, RegistryObject<Block>> Map = new HashMap<>();

      for (Clay clay : Clay.values()) {
         if (!clay.hasRock()) {
            Map.put(
               clay,
               register(
                  ("ceramic/" + clay.getSerializedName()).toLowerCase(Locale.ROOT) + "/clay_block",
                  () -> new Block(
                        net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(clay.getMaterialColor())
                           .strength(0.6F)
                           .sound(TFCFSounds.MUD)
                     ),
                  TFCCreativeTabs.DECORATIONS
               )
            );
         }
      }

      return Map;
   }

   private static Map<Clay, RegistryObject<Block>> clayBricksMap(Class<Clay> enumClass) {
      Map<Clay, RegistryObject<Block>> Map = new HashMap<>();

      for (Clay clay : Clay.values()) {
         if (!clay.hasRock()) {
            Map.put(
               clay,
               register(
                  ("ceramic/" + clay.getSerializedName() + "/bricks").toLowerCase(Locale.ROOT),
                  () -> new Block(
                        net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(clay.getMaterialColor()).requiresCorrectToolForDrops()
                           .requiresCorrectToolForDrops()
                           .strength(2.0F, 6.0F)
                     ),
                  TFCCreativeTabs.DECORATIONS
               )
            );
         }
      }

      return Map;
   }

   private static Map<Clay, DecorationBlockRegistryObject> clayDecoMap(Class<Clay> enumClass) {
      Map<Clay, DecorationBlockRegistryObject> Map = new HashMap<>();

      for (Clay clay : Clay.values()) {
         if (!clay.hasRock()) {
            Map.put(
               clay,
               new DecorationBlockRegistryObject(
                  register(
                     ("ceramic/" + clay.getSerializedName() + "/brick_slab").toLowerCase(Locale.ROOT),
                     () -> new SlabBlock(
                           net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(clay.getMaterialColor()).requiresCorrectToolForDrops()
                              .requiresCorrectToolForDrops()
                              .strength(2.0F, 6.0F)
                        ),
                     TFCCreativeTabs.DECORATIONS
                  ),
                  register(
                     ("ceramic/" + clay.getSerializedName() + "/brick_stairs").toLowerCase(Locale.ROOT),
                     () -> new StairBlock(
                           () -> ((Block)CLAY_BRICKS.get(clay).get()).defaultBlockState(),
                           net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(clay.getMaterialColor()).requiresCorrectToolForDrops()
                              .requiresCorrectToolForDrops()
                              .strength(2.0F, 6.0F)
                        ),
                     TFCCreativeTabs.DECORATIONS
                  ),
                  register(
                     ("ceramic/" + clay.getSerializedName() + "/brick_wall").toLowerCase(Locale.ROOT),
                     () -> new WallBlock(
                           net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(clay.getMaterialColor()).requiresCorrectToolForDrops()
                              .requiresCorrectToolForDrops()
                              .strength(2.0F, 6.0F)
                        ),
                     TFCCreativeTabs.DECORATIONS
                  )
               )
            );
         }
      }

      return Map;
   }

   private static Map<Clay, RegistryObject<Block>> clayLargeVesselMap(Class<Clay> enumClass) {
      Map<Clay, RegistryObject<Block>> Map = new HashMap<>();

      for (Clay clay : Clay.values()) {
         if (!clay.hasRock()) {
            Map.put(
               clay,
               register(
                  ("ceramic/" + clay.getSerializedName()).toLowerCase(Locale.ROOT) + "/fired/large_vessel",
                  () -> new LargeVesselBlock(
                        ExtendedProperties.of(
                              net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(clay.getMaterialColor())
                                 .strength(2.5F)
                                 .noOcclusion()
                           )
                           .blockEntity(TFCFBlockEntities.LARGE_VESSEL)
                     ),
                  TFCCreativeTabs.MISC
               )
            );
         }
      }

      return Map;
   }

   private static Map<Colors, RegistryObject<Block>> sandMap(Class<Colors> enumClass) {
      Map<Colors, RegistryObject<Block>> Map = new HashMap<>();

      for (Colors type : Colors.values()) {
         if (type.hasSandNew()) {
            Map.put(
               type,
               register(
                  "sand/sand/" + type.name(),
                  () -> new TFCSandBlock(
                        type.getDustColor(),
                        net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(type.getMaterialColor())
                           .strength(0.5F)
                           .sound(SoundType.SAND)
                     ),
                  TFCCreativeTabs.EARTH
               )
            );
         }
      }

      return Map;
   }

   private static Map<Colors, RegistryObject<Block>> sandLayerMap(Class<Colors> enumClass) {
      Map<Colors, RegistryObject<Block>> Map = new HashMap<>();

      for (Colors type : Colors.values()) {
         if (type.hasSandNew()) {
            Map.put(
               type,
               register(
                  "sand/sand_layer/" + type.name(),
                  () -> new SandLayerBlock(
                        type.getDustColor(),
                        ExtendedProperties.of(type.getMaterialColor()).blockEntity(TFCFBlockEntities.SAND_PILE)
                           .strength(0.1F)
                           .requiresCorrectToolForDrops()
                           .sound(SoundType.SAND),
                        (Supplier<? extends Block>)SAND.get(type)
                     ),
                  TFCCreativeTabs.EARTH
               )
            );
         } else {
            Map.put(
               type,
               register(
                  "sand/sand_layer/" + type.name(),
                  () -> new SandLayerBlock(
                        type.getDustColor(),
                        ExtendedProperties.of(type.getMaterialColor()).blockEntity(TFCFBlockEntities.SAND_PILE)
                           .strength(0.1F)
                           .requiresCorrectToolForDrops()
                           .sound(SoundType.SAND),
                        (Supplier<? extends Block>)TFCBlocks.SAND.get(type.toSandTFC(false))
                     ),
                  TFCCreativeTabs.EARTH
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFSoil, Map<TFCFSoil.TFCFVariant, RegistryObject<Block>>> tfcfSoilMap(Class<TFCFSoil> enumClass) {
      Map<TFCFSoil, Map<TFCFSoil.TFCFVariant, RegistryObject<Block>>> Map = new HashMap<>();

      for (TFCFSoil type : enumClass.getEnumConstants()) {
         if (REMOVED_SOILS.contains(type)) {
            Map.put(type, java.util.Collections.emptyMap());
            continue;
         }
         Map.put(type, Helpers.mapOfKeys(
            TFCFSoil.TFCFVariant.class, variant -> register(type.name() + "/" + variant.name(), () -> type.TFCFCreate(variant), TFCCreativeTabs.EARTH)
         ));
      }

      return Map;
   }

   private static Map<TFCFSoil, Map<Variant, RegistryObject<Block>>> TFCSoilMap(Class<TFCFSoil> enumClass) {
      Map<TFCFSoil, Map<Variant, RegistryObject<Block>>> Map = new HashMap<>();

      for (TFCFSoil soil : enumClass.getEnumConstants()) {
         if (REMOVED_SOILS.contains(soil)) {
            Map.put(soil, java.util.Collections.emptyMap());
            continue;
         }
         if (soil.getTFCFactory() != null) {
            Map<Variant, RegistryObject<Block>> subMap = new HashMap<>();

            for (Variant variant : Variant.values()) {
               subMap.put(variant, register(soil.name() + "/" + variant.name(), () -> soil.TFCCreate(variant), TFCCreativeTabs.EARTH));
            }

            Map.put(soil, subMap);
         }
      }

      return Map;
   }

   /** Types whose rendering depends on a block entity (invisible in world otherwise). */
   private static boolean isRotationType(BlockType type) {
      return type == BlockType.WATER_WHEEL || type == BlockType.AXLE || type == BlockType.BLADED_AXLE
         || type == BlockType.ENCASED_AXLE || type == BlockType.CLUTCH || type == BlockType.GEAR_BOX
         || type == BlockType.WINDMILL;
   }

   private static ExtendedProperties rotationProperties(TFCFWood wood) {
      return ExtendedProperties.of(wood.woodColor())
         .sound(SoundType.WOOD)
         .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS);
   }

   private static Block createRotationBlock(BlockType type, TFCFWood wood) {
      switch (type) {
         case WATER_WHEEL:
            return new net.dries007.tfc.common.blocks.rotation.WaterWheelBlock(
               rotationProperties(wood).strength(9F).noOcclusion()
                  .blockEntity(TFCFBlockEntities.WATER_WHEEL)
                  .ticks(net.dries007.tfc.common.blockentities.rotation.WaterWheelBlockEntity::serverTick,
                         net.dries007.tfc.common.blockentities.rotation.WaterWheelBlockEntity::clientTick),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get(),
               wood.getSerializedName());
         case AXLE:
            return new net.dries007.tfc.common.blocks.rotation.AxleBlock(
               rotationProperties(wood).noOcclusion().strength(2.5F).flammableLikeLogs()
                  .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                  .blockEntity(TFCFBlockEntities.AXLE),
               () -> (net.dries007.tfc.common.blocks.rotation.WindmillBlock) WOODS.get(wood).get(BlockType.WINDMILL).get(),
               new net.minecraft.resources.ResourceLocation("tfcflorae", "block/wood/planks/" + wood.getSerializedName()));
         case BLADED_AXLE:
            return new net.dries007.tfc.common.blocks.rotation.BladedAxleBlock(
               rotationProperties(wood).noOcclusion().strength(2.5F).flammableLikeLogs()
                  .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                  .blockEntity(TFCFBlockEntities.BLADED_AXLE),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get());
         case ENCASED_AXLE:
            return new net.dries007.tfc.common.blocks.rotation.EncasedAxleBlock(
               rotationProperties(wood).strength(2.5F).flammableLikeLogs()
                  .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                  .blockEntity(TFCFBlockEntities.ENCASED_AXLE));
         case CLUTCH:
            return new net.dries007.tfc.common.blocks.rotation.ClutchBlock(
               rotationProperties(wood).strength(2.5F).flammableLikeLogs()
                  .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                  .blockEntity(TFCFBlockEntities.CLUTCH),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get());
         case GEAR_BOX:
            return new net.dries007.tfc.common.blocks.rotation.GearBoxBlock(
               rotationProperties(wood).strength(2F).noOcclusion()
                  .blockEntity(TFCFBlockEntities.GEAR_BOX),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get());
         case WINDMILL:
            return new net.dries007.tfc.common.blocks.rotation.WindmillBlock(
               rotationProperties(wood).strength(9F).noOcclusion()
                  .blockEntity(TFCFBlockEntities.WINDMILL)
                  .ticks(net.dries007.tfc.common.blockentities.rotation.WindmillBlockEntity::serverTick,
                         net.dries007.tfc.common.blockentities.rotation.WindmillBlockEntity::clientTick),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get());
         default:
            return type.create(wood).get();
      }
   }

   private static Map<TFCFWood, Map<BlockType, RegistryObject<Block>>> woodMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, Map<BlockType, RegistryObject<Block>>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (!wood.hasLeavesOnly()) {
            Map<BlockType, RegistryObject<Block>> subMap = new HashMap<>();

            for (BlockType type : BlockType.values()) {
               // rotation machines need a block entity that lists our blocks; TFC's own
               // types only know its 20 woods, so build these blocks ourselves
               if (isRotationType(type)) {
                  subMap.put(type, register(
                     ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                     () -> createRotationBlock(type, wood),
                     TFCCreativeTabs.WOOD
                  ));
                  continue;
               }
               if (wood == TFCFWood.BAMBOO) {
                  // 竹子没有 wood / stripped_wood 两种方块的 blockstate 与模型，
                  // 注册出来是紫黑方块，这里直接跳过（其余类型资源齐全）。
                  if (type == BlockType.WOOD || type == BlockType.STRIPPED_WOOD) {
                     continue;
                  }
                  if (type == BlockType.PLANKS) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new ExtendedBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(1.5F, 3.0F)
                                    .flammableLikePlanks()
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.LOG) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "_bundle/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new LogBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(5.5F)
                                    .requiresCorrectToolForDrops()
                                    .flammableLikeLogs(),
                                 (Supplier)WOODS.get(wood).get(BlockType.STRIPPED_LOG)
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.WOOD) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "_bundle/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new LogBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(5.5F)
                                    .requiresCorrectToolForDrops()
                                    .flammableLikeLogs(),
                                 (Supplier)WOODS.get(wood).get(BlockType.STRIPPED_WOOD)
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.STRIPPED_WOOD) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "_bundle/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new LogBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(5.5F)
                                    .requiresCorrectToolForDrops()
                                    .flammableLikeLogs(),
                                 null
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.STRIPPED_LOG) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "_bundle/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new LogBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(5.5F)
                                    .requiresCorrectToolForDrops()
                                    .flammableLikeLogs(),
                                 null
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.SAPLING) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new VanillaBambooSaplingBlock(
                                 new TFCTreeGrower(
                                    TFCFHelpers.identifier("tree/bamboo"),
                                    TFCFHelpers.identifier("tree/bamboo_large")
                                 ),
                                 ExtendedProperties.of(wood.woodColor())
                                    .noCollission()
                                    .randomTicks()
                                    .strength(0.0F)
                                    .sound(SoundType.GRASS)
                                    .flammableLikeLeaves()
                                    .blockEntity(net.dries007.tfc.common.blockentities.TFCBlockEntities.TICK_COUNTER),
                                  wood.daysToGrow()
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.LEAVES) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> TFCFLeavesBlockExt.create(
                                 ExtendedProperties.of(wood.woodColor())
                                    .strength(0.5F)
                                    .sound(SoundType.GRASS)
                                    .randomTicks()
                                    .noOcclusion()
                                    .isViewBlocking(TFCFBlocks::never)
                                    .flammableLikeLeaves(),
                                 wood.autumnIndex(),
                                 null,
                                 null,
                                 (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.SAPLING)
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.TRAPPED_CHEST) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFTrappedChestBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.5F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.TRAPPED_CHEST)
                                    .clientTicks(ChestBlockEntity::lidAnimateTick),
                                 wood.getSerializedName()
                              ),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.CHEST) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFChestBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.5F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.CHEST)
                                    .clientTicks(ChestBlockEntity::lidAnimateTick),
                                 wood.getSerializedName()
                              ),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.SIGN) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCStandingSignBlock(ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .noCollission()
                                    .strength(1.0F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.SIGN),
                               net.minecraft.world.level.block.state.properties.WoodType.OAK),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.WALL_SIGN) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCWallSignBlock(ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .noCollission()
                                    .strength(1.0F)
                                    .dropsLike(wood.getBlock(BlockType.SIGN))
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.SIGN),
                               net.minecraft.world.level.block.state.properties.WoodType.OAK),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.LECTERN) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCLecternBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .noCollission()
                                    .strength(2.5F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.LECTERN)
                              ),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.BOOKSHELF) {
                     subMap.put(
                        type,
                        register(
                           ("wood/bookshelf/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new ExtendedBlock(
                                 ExtendedProperties.of(
                                       net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                                          .sound(SoundType.WOOD)
                                          .strength(2.0F, 3.0F)
                                    )
                                    .flammableLikePlanks()
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.BARREL) {
                     subMap.put(
                        type,
                        register(
                           ("wood/barrel/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFBarrelBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.5F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.BARREL)
                                    .serverTicks(BarrelBlockEntity::serverTick)
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.LOOM) {
                     subMap.put(
                        type,
                        register(
                           ("wood/loom/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFLoomBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.5F)
                                    .noOcclusion()
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.LOOM)
                                    .ticks(LoomBlockEntity::tick),
                                 TFCFHelpers.identifier("block/wood/planks/" + wood.getSerializedName())
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.SLUICE) {
                     subMap.put(
                        type,
                        register(
                           ("wood/sluice/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFSluiceBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(3.0F)
                                    .noOcclusion()
                                    .flammableLikeLogs()
                                    .blockEntity(TFCFBlockEntities.SLUICE)
                                    .serverTicks(SluiceBlockEntity::serverTick)
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.TOOL_RACK) {
                     subMap.put(
                        type,
                        register(
                           ("wood/tool_rack/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFToolRackBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.0F)
                                    .noOcclusion()
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.TOOL_RACK)
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           type.create(wood),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  }
               } else if ((type != BlockType.LOG || !wood.isFruitTree() || !wood.hasFruitingLog())
                  && (type != BlockType.WOOD || !wood.isFruitTree() || !wood.hasFruitingLog())) {
                  if (type == BlockType.SAPLING) {
                     if (wood.isMangrove()) {
                        subMap.put(
                           type,
                           register(
                              ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                              () -> new TFCFMangrovePropaguleBlock(
                                    wood,
                                    ExtendedProperties.of(
                                          net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                                             .noCollission()
                                             .randomTicks()
                                             .strength(0.0F)
                                             .sound(SoundType.GRASS)
                                       )
                                       .flammableLikeLeaves()
                                       .blockEntity(net.dries007.tfc.common.blockentities.TFCBlockEntities.TICK_COUNTER),
                                     wood.daysToGrow()
                                 ),
                              type.createBlockItem(wood, new Properties())
                           )
                        );
                     } else {
                        subMap.put(
                           type,
                           register(
                              ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                              () -> new TFCSaplingBlock(
                                    wood.tree(),
                                    ExtendedProperties.of(wood.woodColor())
                                       .noCollission()
                                       .randomTicks()
                                       .strength(0.0F)
                                       .sound(SoundType.GRASS)
                                       .flammableLikeLeaves()
                                       .blockEntity(net.dries007.tfc.common.blockentities.TFCBlockEntities.TICK_COUNTER),
                                    wood::daysToGrow,
                                    false
                                 ),
                              type.createBlockItem(wood, new Properties())
                           )
                        );
                     }
                  } else if (type == BlockType.LEAVES) {
                     // 棕榈树（coconut_palm / date_palm）已整体移除，isPalmTree() 恒为 false，
                     // 这里的条件简化为“非果树非红树，或带产果木”。
                     if (!wood.isFruitTree() && !wood.isMangrove() || wood.hasFruitingLog()) {
                        subMap.put(
                           type,
                           register(
                              ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                              () -> TFCFLeavesBlockExt.create(
                                    ExtendedProperties.of(wood.woodColor())
                                       .strength(0.5F)
                                       .sound(SoundType.GRASS)
                                       .randomTicks()
                                       .noOcclusion()
                                       .isViewBlocking(TFCFBlocks::never)
                                       .flammableLikeLeaves(),
                                    wood.autumnIndex(),
                                    (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.FALLEN_LEAVES),
                                    (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.TWIG),
                                    (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.SAPLING)
                                 ),
                              TFCCreativeTabs.WOOD
                           )
                        );
                     }
                  } else if (type == BlockType.TRAPPED_CHEST) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFTrappedChestBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.5F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.TRAPPED_CHEST)
                                    .clientTicks(ChestBlockEntity::lidAnimateTick),
                                 wood.getSerializedName()
                              ),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.CHEST) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFChestBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.5F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.CHEST)
                                    .clientTicks(ChestBlockEntity::lidAnimateTick),
                                 wood.getSerializedName()
                              ),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.SIGN) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCStandingSignBlock(ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .noCollission()
                                    .strength(1.0F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.SIGN),
                               net.minecraft.world.level.block.state.properties.WoodType.OAK),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.WALL_SIGN) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCWallSignBlock(ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .noCollission()
                                    .strength(1.0F)
                                    .dropsLike(wood.getBlock(BlockType.SIGN))
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.SIGN),
                               net.minecraft.world.level.block.state.properties.WoodType.OAK),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.LECTERN) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCLecternBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .noCollission()
                                    .strength(2.5F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.LECTERN)
                              ),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.BOOKSHELF) {
                     subMap.put(
                        type,
                        register(
                           ("wood/bookshelf/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new ExtendedBlock(
                                 ExtendedProperties.of(
                                       net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                                          .sound(SoundType.WOOD)
                                          .strength(2.0F, 3.0F)
                                    )
                                    .flammableLikePlanks()
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.BARREL) {
                     subMap.put(
                        type,
                        register(
                           ("wood/barrel/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFBarrelBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.5F)
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.BARREL)
                                    .serverTicks(BarrelBlockEntity::serverTick)
                              ),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  } else if (type == BlockType.LOOM) {
                     subMap.put(
                        type,
                        register(
                           ("wood/loom/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFLoomBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.5F)
                                    .noOcclusion()
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.LOOM)
                                    .ticks(LoomBlockEntity::tick),
                                 TFCFHelpers.identifier("block/wood/planks/" + wood.getSerializedName())
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.SLUICE) {
                     subMap.put(
                        type,
                        register(
                           ("wood/sluice/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFSluiceBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(3.0F)
                                    .noOcclusion()
                                    .flammableLikeLogs()
                                    .blockEntity(TFCFBlockEntities.SLUICE)
                                    .serverTicks(SluiceBlockEntity::serverTick)
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (type == BlockType.TOOL_RACK) {
                     subMap.put(
                        type,
                        register(
                           ("wood/tool_rack/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new TFCFToolRackBlock(
                                 ExtendedProperties.of(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.0F)
                                    .noOcclusion()
                                    .flammableLikePlanks()
                                    .blockEntity(TFCFBlockEntities.TOOL_RACK)
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else if (wood != TFCFWood.BAMBOO
                     || type != BlockType.PLANKS
                        && type != BlockType.LOG
                        && type != BlockType.WOOD
                        && type != BlockType.STRIPPED_WOOD
                        && type != BlockType.STRIPPED_LOG
                        && type != BlockType.SAPLING) {
                     subMap.put(
                        type,
                        register(
                           ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           type.create(wood),
                           type.createBlockItem(wood, new Properties())
                        )
                     );
                  }
               }
            }

            Map.put(wood, subMap);
         }
      }

      return Map;
   }

   private static Map<Wood, RegistryObject<Block>> chiseledBookshelfMapperTFC(Class<Wood> enumClass) {
      Map<Wood, RegistryObject<Block>> Map = new HashMap<>();

      for (Wood wood : enumClass.getEnumConstants()) {
         Map.put(
            wood,
            register(
               ("wood/chiseled_bookshelf/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
               () -> new ChiseledBookshelfBlock(
                     ExtendedProperties.of(
                           net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                              .sound(SoundType.WOOD)
                              .strength(2.0F, 3.0F)
                        )
                        .blockEntity(TFCFBlockEntities.CHISELED_BOOKSHELF)
                        .flammableLikePlanks()
                  ),
               TFCCreativeTabs.WOOD
            )
         );
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Block>> chiseledBookshelfMapperTFCF(Class<TFCFWood> enumClass) {
      Map<TFCFWood, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (!wood.hasLeavesOnly()) {
            Map.put(
               wood,
               register(
                  ("wood/chiseled_bookshelf/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                  () -> new ChiseledBookshelfBlock(
                        ExtendedProperties.of(
                              net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                                 .sound(SoundType.WOOD)
                                 .strength(2.0F, 3.0F)
                           )
                           .blockEntity(TFCFBlockEntities.CHISELED_BOOKSHELF)
                           .flammableLikePlanks()
                     ),
                  TFCCreativeTabs.WOOD
               )
            );
         }
      }

      return Map;
   }

   private static Map<Wood, RegistryObject<Block>> normalBookshelfMapperTFC(Class<Wood> enumClass) {
      Map<Wood, RegistryObject<Block>> Map = new HashMap<>();

      for (Wood wood : enumClass.getEnumConstants()) {
         // 红树（TFC 木材）没有 bookshelf 的 blockstate / 模型，注册出来是紫黑方块
         if (wood == Wood.MANGROVE) {
            continue;
         }
         Map.put(
            wood,
            register(
               ("wood/bookshelf/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
               () -> new ExtendedBlock(
                     ExtendedProperties.of(
                           net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                              .sound(SoundType.WOOD)
                              .strength(2.0F, 3.0F)
                        )
                        .flammableLikePlanks()
                  ),
               TFCCreativeTabs.WOOD
            )
         );
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Block>> leavesOnlyMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (wood.hasLeavesOnly()) {
            BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory = BlockItem::new;
            Function<Block, BlockItem> blockItem = block -> blockItemFactory.apply(block, new Properties());
            Map.put(
               wood,
               register(
                  ("wood/leaves/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                  () -> TFCFLeavesBlock.create(
                        ExtendedProperties.of(
                              net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                                 .strength(0.5F)
                                 .sound(SoundType.GRASS)
                                 .randomTicks()
                                 .noOcclusion()
                                 .isViewBlocking(TFCFBlocks::never)
                           )
                           .randomTicks()
                           .hasPostProcess(TFCFBlocks::always)
                           .flammableLikeLeaves(),
                        wood.getProductItem(),
                        wood.getStages(),
                        0,
                        TFCFClimateRanges.LARGE_FRUIT_TREES.get(wood),
                        (Supplier<? extends Block>)((Map)TFCBlocks.WOODS.get(Wood.WHITE_CEDAR)).get(BlockType.FALLEN_LEAVES),
                        (Supplier<? extends Block>)((Map)TFCBlocks.WOODS.get(Wood.WHITE_CEDAR)).get(BlockType.TWIG),
                        (Supplier<? extends Block>)((Map)TFCBlocks.WOODS.get(Wood.WHITE_CEDAR)).get(BlockType.SAPLING)
                     ),
                  blockItem
               )
            );
         }
      }

      return Map;
   }


   private static Map<TFCFWood, RegistryObject<Block>> seasonalLeavesMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (!wood.hasLeavesOnly()) {
            BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory = BlockItem::new;
            Function<Block, BlockItem> blockItem = block -> blockItemFactory.apply(block, new Properties());
            if ((wood.isFruitTree() || wood.isMangrove()) && !wood.hasFruitingLog()) {
               if (wood.isFruitTree()) {
                  Map.put(
                     wood,
                     register(
                        ("wood/leaves/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                        () -> TFCFLeavesBlock.create(
                              ExtendedProperties.of(
                                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                                       .strength(0.5F)
                                       .sound(SoundType.GRASS)
                                       .randomTicks()
                                       .noOcclusion()
                                       .isViewBlocking(TFCFBlocks::never)
                                 )
                                 .randomTicks()
                                 .hasPostProcess(TFCFBlocks::always)
                                 .flammableLikeLeaves(),
                              wood.getProductItem(),
                              wood.getStages(),
                              0,
                              TFCFClimateRanges.LARGE_FRUIT_TREES.get(wood),
                              (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.FALLEN_LEAVES),
                              (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.TWIG),
                              (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.SAPLING)
                           ),
                        blockItem
                     )
                  );
               } else if (wood.isMangrove()) {
                  Map.put(
                     wood,
                     register(
                        ("wood/leaves/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                        () -> TFCFMangroveLeavesBlock.create(
                              ExtendedProperties.of(
                                    net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                                       .strength(0.5F)
                                       .sound(SoundType.GRASS)
                                       .randomTicks()
                                       .noOcclusion()
                                       .isViewBlocking(TFCFBlocks::never)
                                 )
                                 .randomTicks()
                                 .hasPostProcess(TFCFBlocks::always)
                                 .flammableLikeLeaves(),
                              wood,
                              wood.getProductItem(),
                              wood.getStages(),
                              0,
                              TFCFClimateRanges.LARGE_FRUIT_TREES.get(wood),
                              (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.FALLEN_LEAVES),
                              (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.TWIG),
                              (Supplier<? extends Block>)WOODS.get(wood).get(BlockType.SAPLING)
                           ),
                        blockItem
                     )
                  );
               }
            }
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Block>> seasonalLogMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (!wood.hasLeavesOnly()) {
            BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory = BlockItem::new;
            Function<Block, BlockItem> blockItem = block -> blockItemFactory.apply(block, new Properties());
            if (wood.isFruitTree() && wood.hasFruitingLog()) {
               Map.put(
                  wood,
                  register(
                     ("wood/log/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                     () -> new TFCFFruitingLogBlock(
                           ExtendedProperties.of(state -> state.getValue(RotatedPillarBlock.AXIS) == Axis.Y ? wood.woodColor() : wood.barkColor())
                              .strength(8.0F)
                              .sound(SoundType.WOOD)
                              .strength(8.0F)
                              .requiresCorrectToolForDrops()
                              .flammableLikeLogs()
                              .hasPostProcess(TFCFBlocks::always)
                              .randomTicks(),
                           wood.getBlock(BlockType.STRIPPED_LOG),
                           wood.getProductItem(),
                           wood.getStages(),
                           TFCFClimateRanges.LARGE_FRUIT_TREES.get(wood)
                        ),
                     blockItem
                  )
               );
            }
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Block>> seasonalWoodMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (!wood.hasLeavesOnly()) {
            BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory = BlockItem::new;
            Function<Block, BlockItem> blockItem = block -> blockItemFactory.apply(block, new Properties());
            if (wood.isFruitTree() && wood.hasFruitingLog()) {
               Map.put(
                  wood,
                  register(
                     ("wood/wood/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                     () -> new TFCFFruitingLogBlock(
                           ExtendedProperties.of(wood.woodColor())
                              .sound(SoundType.WOOD)
                              .strength(8.0F)
                              .requiresCorrectToolForDrops()
                              .flammableLikeLogs()
                              .hasPostProcess(TFCFBlocks::always)
                              .randomTicks(),
                           wood.getBlock(BlockType.STRIPPED_WOOD),
                           wood.getProductItem(),
                           wood.getStages(),
                           TFCFClimateRanges.LARGE_FRUIT_TREES.get(wood)
                        ),
                     blockItem
                  )
               );
            }
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Block>> woodSpikeMapper(Class<TFCFWood> enumClass) {
      // 木锥（wood/spike）已整体移除，不再注册任何方块
      return new HashMap<>();
   }


   private static Map<Wood, RegistryObject<Block>> woodWallMapperTFC(Class<Wood> enumClass) {
      Map<Wood, RegistryObject<Block>> Map = new HashMap<>();

      for (Wood wood : enumClass.getEnumConstants()) {
         // 红树（TFC 木材）没有 log_wall / wood_wall 的 blockstate 与模型，注册出来是紫黑方块
         if (wood == Wood.MANGROVE) {
            continue;
         }
         Map.put(
            wood,
            register(
               "wood/log_wall/" + wood.name(),
               () -> new TFCFWallBlock(
                     ExtendedProperties.of(wood.woodColor())
                        .strength(8.0F)
                        .sound(SoundType.WOOD)
                        .requiresCorrectToolForDrops()
                        .flammableLikeLogs(),
                     net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                        .strength(8.0F)
                        .sound(SoundType.WOOD)
                        .requiresCorrectToolForDrops()
                  ),
               TFCCreativeTabs.WOOD
            )
         );
         Map.put(
            wood,
            register(
               "wood/wood_wall/" + wood.name(),
               () -> new TFCFWallBlock(
                     ExtendedProperties.of(wood.barkColor())
                        .strength(8.0F)
                        .sound(SoundType.WOOD)
                        .requiresCorrectToolForDrops()
                        .flammableLikeLogs(),
                     net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                        .strength(8.0F)
                        .sound(SoundType.WOOD)
                        .requiresCorrectToolForDrops()
                  ),
               TFCCreativeTabs.WOOD
            )
         );
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Block>> woodWallMapperTFCF(Class<TFCFWood> enumClass) {
      Map<TFCFWood, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (!wood.hasLeavesOnly()) {
            Map.put(
               wood,
               register(
                  "wood/log_wall/" + wood.name(),
                  () -> new TFCFWallBlock(
                        ExtendedProperties.of(wood.woodColor())
                           .strength(8.0F)
                           .sound(SoundType.WOOD)
                           .requiresCorrectToolForDrops()
                           .flammableLikeLogs(),
                        net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                           .strength(8.0F)
                           .sound(SoundType.WOOD)
                           .requiresCorrectToolForDrops()
                     ),
                  TFCCreativeTabs.WOOD
               )
            );
            Map.put(
               wood,
               register(
                  "wood/wood_wall/" + wood.name(),
                  () -> new TFCFWallBlock(
                        ExtendedProperties.of(wood.barkColor())
                           .strength(8.0F)
                           .sound(SoundType.WOOD)
                           .requiresCorrectToolForDrops()
                           .flammableLikeLogs(),
                        net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                           .strength(8.0F)
                           .sound(SoundType.WOOD)
                           .requiresCorrectToolForDrops()
                     ),
                  TFCCreativeTabs.WOOD
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Block>> mangroveRootsMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (wood.isMangrove()) {
            BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory = BlockItem::new;
            Function<Block, BlockItem> blockItem = block -> blockItemFactory.apply(block, new Properties());
            Map.put(
               wood,
               register(
                  ("wood/roots/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                  () -> new TFCFMangroveRootsBlocks(
                        wood,
                        ExtendedProperties.of(
                              net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                 .noCollission()
                                 .randomTicks()
                                 .strength(0.7F)
                                 .sound(TFCFSounds.MANGROVE_ROOTS)
                           )
                           .flammableLikeLeaves()
                     ),
                  blockItem
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFWood, Map<Variant, RegistryObject<Block>>> TFCmangroveRootsMuddyMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, Map<Variant, RegistryObject<Block>>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (wood.isMangrove()) {
            Map<Variant, RegistryObject<Block>> soilVariantMap = new HashMap<>();

            for (Variant soilVariant : Variant.values()) {
         /* silty_loam 已补齐，不再跳过 */
               BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory = BlockItem::new;
               Function<Block, BlockItem> blockItem = block -> blockItemFactory.apply(block, new Properties());
               soilVariantMap.put(
                  soilVariant,
                  register(
                     ("wood/roots/" + wood.getSerializedName() + "/" + soilVariant.name()).toLowerCase(Locale.ROOT),
                     () -> new TFCFMangroveMuddyRootsBlocks(
                           wood,
                           ExtendedProperties.of(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                    .noCollission()
                                    .randomTicks()
                                    .strength(0.7F)
                                    .sound(TFCFSounds.MANGROVE_ROOTS)
                              )
                              .flammableLikeLeaves()
                        ),
                     blockItem
                  )
               );
            }

            Map.put(wood, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFWood, Map<TFCFSoil.TFCFVariant, RegistryObject<Block>>> TFCFmangroveRootsMuddyMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, Map<TFCFSoil.TFCFVariant, RegistryObject<Block>>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (wood.isMangrove()) {
            Map<TFCFSoil.TFCFVariant, RegistryObject<Block>> soilVariantMap = new HashMap<>();

            for (TFCFSoil.TFCFVariant soilVariant : TFCFSoil.TFCFVariant.values()) {
               BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory = BlockItem::new;
               Function<Block, BlockItem> blockItem = block -> blockItemFactory.apply(block, new Properties());
               soilVariantMap.put(
                  soilVariant,
                  register(
                     ("wood/roots/" + wood.getSerializedName() + "/" + soilVariant.name()).toLowerCase(Locale.ROOT),
                     () -> new TFCFMangroveMuddyRootsBlocks(
                           wood,
                           ExtendedProperties.of(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                    .noCollission()
                                    .randomTicks()
                                    .strength(0.7F)
                                    .sound(TFCFSounds.MUDDY_MANGROVE_ROOTS)
                              )
                              .flammableLikeLeaves()
                        ),
                     blockItem
                  )
               );
            }

            Map.put(wood, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Block>> joshuaTrunkMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (wood.isJoshua()) {
            BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory = BlockItem::new;
            Function<Block, BlockItem> blockItem = block -> blockItemFactory.apply(block, new Properties());
            Map.put(
               wood,
               register(
                  ("wood/trunks/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                  () -> TFCFJoshuaTrunkBlock.create(
                        wood,
                        net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                           .randomTicks()
                           .noOcclusion()
                           .strength(6.0F)
                           .sound(SoundType.WOOD)
                           .requiresCorrectToolForDrops()
                           .hasPostProcess(TFCFBlocks::always),
                        ExtendedProperties.of(
                              net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.WOOD)
                                 .randomTicks()
                                 .noOcclusion()
                                 .strength(6.0F)
                                 .sound(SoundType.WOOD)
                                 .requiresCorrectToolForDrops()
                                 .hasPostProcess(TFCFBlocks::always)
                           )
                           .flammableLikeLogs(),
                        TFCBlockStateProperties.FRESH_WATER
                     ),
                  blockItem
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFWood, RegistryObject<Block>> joshuaLeavesMapper(Class<TFCFWood> enumClass) {
      Map<TFCFWood, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFWood wood : enumClass.getEnumConstants()) {
         if (wood.isJoshua()) {
            BiFunction<Block, Properties, ? extends BlockItem> blockItemFactory = BlockItem::new;
            Function<Block, BlockItem> blockItem = block -> blockItemFactory.apply(block, new Properties());
            Map.put(
               wood,
               register(
                  ("wood/leaves_head/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                  () -> TFCFJoshuaLeavesBlock.create(
                        wood,
                        ExtendedProperties.of(
                              net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
                                 .strength(0.5F)
                                 .sound(SoundType.GRASS)
                                 .randomTicks()
                                 .noOcclusion()
                                 .isViewBlocking(TFCFBlocks::never)
                                 .hasPostProcess(TFCFBlocks::always)
                           )
                           .randomTicks()
                           .flammableLikeLeaves(),
                        wood.getProductItem(),
                        wood.getStages(),
                        TFCFClimateRanges.LARGE_FRUIT_TREES.get(wood)
                     ),
                  blockItem
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFPlant, RegistryObject<Block>> bambooLogMapper(Class<TFCFPlant> enumClass) {
      Map<TFCFPlant, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFPlant plant : TFCFPlant.values()) {
         if (plant == TFCFPlant.BLUE_BAMBOO || plant == TFCFPlant.DRAGON_BAMBOO || plant == TFCFPlant.RED_BAMBOO) {
            Map.put(
               plant,
               register(
                  ("wood/log/" + plant.name()).toLowerCase(Locale.ROOT),
                  () -> new BambooLogBlock(
                        ExtendedProperties.of(MapColor.WOOD)
                           .strength(8.0F)
                           .sound(SoundType.WOOD)
                           .strength(5.5F)
                           .requiresCorrectToolForDrops()
                           .flammableLikeLogs()
                           .sound(SoundType.BAMBOO)
                           .noOcclusion()
                           .dynamicShape()
                           .hasPostProcess(TFCFBlocks::always),
                        (Supplier<? extends Block>)STRIPPED_BAMBOO_LOGS.get(plant)
                     ),
                  plantBlockItem(plant)
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFPlant, RegistryObject<Block>> bambooLeavesMapper(Class<TFCFPlant> enumClass) {
      Map<TFCFPlant, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFPlant plant : TFCFPlant.values()) {
         if (plant == TFCFPlant.BLUE_BAMBOO || plant == TFCFPlant.DRAGON_BAMBOO || plant == TFCFPlant.RED_BAMBOO) {
            Map.put(
               plant,
               register(
                  ("wood/leaves/" + plant.name()).toLowerCase(Locale.ROOT),
                  () -> new net.dries007.tfc.common.blocks.wood.TFCLeavesBlock(
                        ExtendedProperties.of(MapColor.PLANT)
                           .strength(0.5F)
                           .sound(SoundType.GRASS)
                           .randomTicks()
                           .noOcclusion()
                           .isViewBlocking(TFCFBlocks::never)
                           .flammableLikeLeaves(),
                        7,
                        null,
                        null
                     ),
                  plantBlockItem(plant)
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFPlant, RegistryObject<Block>> bambooStrippedLogMapper(Class<TFCFPlant> enumClass) {
      Map<TFCFPlant, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFPlant plant : TFCFPlant.values()) {
         if (plant == TFCFPlant.BLUE_BAMBOO || plant == TFCFPlant.DRAGON_BAMBOO || plant == TFCFPlant.RED_BAMBOO) {
            Map.put(
               plant,
               register(
                  ("wood/stripped_log/" + plant.name()).toLowerCase(Locale.ROOT),
                  () -> new BambooLogBlock(
                        ExtendedProperties.of(MapColor.WOOD)
                           .strength(8.0F)
                           .sound(SoundType.WOOD)
                           .strength(5.5F)
                           .requiresCorrectToolForDrops()
                           .flammableLikeLogs()
                           .sound(SoundType.BAMBOO)
                           .noOcclusion()
                           .dynamicShape()
                           .hasPostProcess(TFCFBlocks::always),
                        null
                     ),
                  plantBlockItem(plant)
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFPlant, RegistryObject<Block>> bambooSaplingMapper(Class<TFCFPlant> enumClass) {
      Map<TFCFPlant, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFPlant plant : TFCFPlant.values()) {
         if (plant == TFCFPlant.BLUE_BAMBOO || plant == TFCFPlant.DRAGON_BAMBOO || plant == TFCFPlant.RED_BAMBOO) {
            // 这是「粗竹」竹苗（wood/sapling/<colour>_bamboo），长出来的是 wood/log/.. 粗竹，
            // 与 plant/<colour>_bamboo_sapling（细竹）分工不同。
            // 之前 grower 指向 tfcflorae:tree/grower/<plant>，那个 configured_feature 从未生成，
            // 所以既不能催熟也不自然生长；现在指向下面补上的 tree/<plant>。
            Map.put(
               plant,
               register(
                  ("wood/sapling/" + plant.name()).toLowerCase(Locale.ROOT),
                  () -> new VanillaBambooSaplingBlock(
                        new TFCTreeGrower(
                           TFCFHelpers.identifier("tree/" + plant.name().toLowerCase(Locale.ROOT)),
                           TFCFHelpers.identifier("tree/" + plant.name().toLowerCase(Locale.ROOT) + "_large")
                        ),
                        ExtendedProperties.of(MapColor.PLANT)
                           .noCollission()
                           .randomTicks()
                           .strength(0.0F)
                           .sound(SoundType.GRASS)
                           .flammableLikeLeaves()
                           .blockEntity(TFCFBlockEntities.TICK_COUNTER),
                        TFCFWood.BAMBOO.daysToGrow()
                     ),
                  plantBlockItem(plant)
               )
            );
         }
      }

      return Map;
   }

   private static Map<TFCFPlant, RegistryObject<Block>> bambooBundledLogMapper(Class<TFCFPlant> enumClass) {
      Map<TFCFPlant, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFPlant plant : TFCFPlant.values()) {
         if (plant == TFCFPlant.BLUE_BAMBOO || plant == TFCFPlant.DRAGON_BAMBOO || plant == TFCFPlant.RED_BAMBOO) {
            Map.put(
               plant,
               register(
                  ("wood/log_bundle/" + plant.name()).toLowerCase(Locale.ROOT),
                  () -> new LogBlock(
                        ExtendedProperties.of(MapColor.COLOR_LIGHT_GREEN)
                           .sound(SoundType.WOOD)
                           .strength(5.5F)
                           .requiresCorrectToolForDrops()
                           .flammableLikeLogs(),
                        (Supplier)WOODS.get(TFCFWood.BAMBOO).get(BlockType.STRIPPED_LOG)
                     ),
                  TFCCreativeTabs.WOOD
               )
            );
            Map.put(
               plant,
               register(
                  ("wood/wood_bundle/" + plant.name()).toLowerCase(Locale.ROOT),
                  () -> new LogBlock(
                        ExtendedProperties.of(MapColor.COLOR_LIGHT_GREEN)
                           .sound(SoundType.WOOD)
                           .strength(5.5F)
                           .requiresCorrectToolForDrops()
                           .flammableLikeLogs(),
                        // 竹子的 stripped_wood 方块已不再注册（无资源），
                        // 这里回退到仍在注册的 stripped_log，保证 supplier 非空。
                        (Supplier)WOODS.get(TFCFWood.BAMBOO).get(BlockType.STRIPPED_LOG)
                     ),
                  TFCCreativeTabs.WOOD
               )
            );
         }
      }

      return Map;
   }

   /**
    * Body segments of tall plants / hanging vines are named {@code *_PLANT} and must not get
    * a BlockItem - only the top segment is the obtainable item, which is how TFC does it.
    * Registering both made every hanging vine show up twice in the creative menu.
    */
   private static java.util.function.Function<Block, ? extends net.minecraft.world.item.BlockItem> plantBlockItem(TFCFPlant plant) {
      if (plant.name().endsWith("_PLANT")) {
         return null;
      }
      return plant.createBlockItem(new Properties());
   }

   private static Map<TFCFPlant, RegistryObject<Block>> normalPlantMapper(Class<TFCFPlant> enumClass) {
      Map<TFCFPlant, RegistryObject<Block>> Map = new HashMap<>();

      for (TFCFPlant plant : TFCFPlant.values()) {
         if (!plant.isSeasonalFruitPlant()) {
            Map.put(
               plant,
               register(("plant/" + plant.name()).toLowerCase(Locale.ROOT), plant::create, plantBlockItem(plant))
            );
         }
      }

      return Map;
   }

   private static Map<TFCFPlant, RegistryObject<Block>> seasonalPlantMapper(Class<TFCFPlant> enumClass) {
      Map<TFCFPlant, RegistryObject<Block>> Map = new HashMap<>();
      for (TFCFPlant plant : TFCFPlant.values()) {
         if (plant.isSeasonalFruitPlant()) {
            if (plant == TFCFPlant.GLOW_VINES_PLANT) {
               Map.put(
                  plant,
                  register(
                     ("plant/" + plant.name()).toLowerCase(Locale.ROOT),
                     () -> new FruitingBodyPlantBlock(
                           TFCFPlant.BlockType.fire(TFCFPlant.BlockType.nonSolidTallPlant(plant).hasPostProcess(TFCFBlocks::always))
                              .lightLevel(FruitingBodyPlantBlock.emission(14, true))
                              .instabreak()
                              .sound(SoundType.CAVE_VINES)
                              .randomTicks(),
                           plant.transformFruiting(),
                           BodyPlantBlock.BODY_SHAPE,
                           Direction.DOWN,
                           plant.getProductItem(),
                           plant.getStages(),
                           TFCFClimateRanges.SEASONAL_PLANT.get(plant)
                        ),
                     plantBlockItem(plant)
                  )
               );
            } else if (plant == TFCFPlant.GLOW_VINES) {
               Map.put(
                  plant,
                  register(
                     ("plant/" + plant.name()).toLowerCase(Locale.ROOT),
                     () -> new FruitingTopPlantBlock(
                           TFCFPlant.BlockType.fire(TFCFPlant.BlockType.nonSolidTallPlant(plant).hasPostProcess(TFCFBlocks::always))
                              .lightLevel(FruitingBodyPlantBlock.emission(14, true))
                              .instabreak()
                              .sound(SoundType.CAVE_VINES)
                              .randomTicks(),
                           plant.transformFruiting(),
                           BodyPlantBlock.WEEPING_SHAPE,
                           Direction.DOWN,
                           plant.getProductItem(),
                           plant.getStages(),
                           TFCFClimateRanges.SEASONAL_PLANT.get(plant)
                        ),
                     plantBlockItem(plant)
                  )
               );
            } else if (plant == TFCFPlant.PITAHAYA) {
               Map.put(
                  plant,
                  register(
                     ("plant/" + plant.name()).toLowerCase(Locale.ROOT),
                     () -> PitahayaBlock.create(
                           plant,
                           TFCFPlant.BlockType.fire(TFCFPlant.BlockType.solid().strength(0.25F).sound(SoundType.MOSS).hasPostProcess(TFCFBlocks::always))
                              .pathType(BlockPathTypes.DAMAGE_OTHER)
                              .randomTicks(),
                           plant.getProductItem(),
                           plant.getStages(),
                           TFCFClimateRanges.SEASONAL_PLANT.get(plant)
                        ),
                     plantBlockItem(plant)
                  )
               );
            } else if (plant == TFCFPlant.POKEWEED) {
               Map.put(
                  plant,
                  register(
                     ("plant/" + plant.name()).toLowerCase(Locale.ROOT),
                     () -> FruitingTallPlantBlock.create(
                           plant,
                           TFCFPlant.BlockType.fire(TFCFPlant.BlockType.nonSolid(plant).hasPostProcess(TFCFBlocks::always)).randomTicks(),
                           plant.getProductItem(),
                           plant.getStages(),
                           TFCFClimateRanges.SEASONAL_PLANT.get(plant)
                        ),
                     plantBlockItem(plant)
                  )
               );
            }
         }
      }

      return Map;
   }

   private static Map<TFCFRockSand, Map<Colors, Map<Rock, RegistryObject<Block>>>> TFCRockSandMapper(Class<TFCFRockSand> enumClass) {
      Map<TFCFRockSand, Map<Colors, Map<Rock, RegistryObject<Block>>>> Map = new HashMap<>();

      for (TFCFRockSand soilBlockType : enumClass.getEnumConstants()) {
         if (soilBlockType.getTFCFactory() != null) {
            Map<Colors, Map<Rock, RegistryObject<Block>>> soilVariantMap = new HashMap<>();

            for (Colors sandColor : Colors.values()) {
               Map<Rock, RegistryObject<Block>> rockMap = new HashMap<>();

               for (Rock rock : Rock.values()) {
                  rockMap.put(
                     rock,
                     register(
                        ("sand/" + soilBlockType.name() + "/" + sandColor.name() + "/" + rock.name()).toLowerCase(Locale.ROOT),
                        () -> soilBlockType.TFCCreate(sandColor, rock),
                        TFCCreativeTabs.EARTH
                     )
                  );
               }

               soilVariantMap.put(sandColor, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRockSand, Map<Colors, Map<TFCFRock, RegistryObject<Block>>>> TFCFRockSandMapper(Class<TFCFRockSand> enumClass) {
      Map<TFCFRockSand, Map<Colors, Map<TFCFRock, RegistryObject<Block>>>> Map = new HashMap<>();

      for (TFCFRockSand soilBlockType : enumClass.getEnumConstants()) {
         if (soilBlockType.getTFCFFactory() != null) {
            Map<Colors, Map<TFCFRock, RegistryObject<Block>>> soilVariantMap = new HashMap<>();

            for (Colors sandColor : Colors.values()) {
               Map<TFCFRock, RegistryObject<Block>> rockMap = new HashMap<>();

               for (TFCFRock rock : TFCFRock.values()) {
                  // 凝灰岩整套岩石方块已移除（TFE 提供），不再生成它的混合沙
                  if (rock == TFCFRock.TUFF) {
                     continue;
                  }
                  rockMap.put(
                     rock,
                     register(
                        ("sand/" + soilBlockType.name() + "/" + sandColor.name() + "/" + rock.name()).toLowerCase(Locale.ROOT),
                        () -> soilBlockType.TFCFCreate(sandColor, rock),
                        TFCCreativeTabs.EARTH
                     )
                  );
               }

               soilVariantMap.put(sandColor, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRockSoil, Map<Variant, Map<Rock, RegistryObject<Block>>>> TFCRockSoilMapper(Class<TFCFRockSoil> enumClass) {
      Map<TFCFRockSoil, Map<Variant, Map<Rock, RegistryObject<Block>>>> Map = new HashMap<>();

      for (TFCFRockSoil soilBlockType : enumClass.getEnumConstants()) {
            // 掺泥/渍泥弹石三族已废弃（资源已删），不再注册
            if (soilBlockType == TFCFRockSoil.DIRTY_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIER_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIEST_STONE_TILES) {
               continue;
            }
         if (soilBlockType.getTFCFFactory() != null && soilBlockType.getTFCFactory() != null) {
            Map<Variant, Map<Rock, RegistryObject<Block>>> soilVariantMap = new HashMap<>();

            for (Variant soilVariant : Variant.values()) {
         /* silty_loam 已补齐，不再跳过 */
               Map<Rock, RegistryObject<Block>> rockMap = new HashMap<>();

               for (Rock rock : Rock.values()) {
                  rockMap.put(
                     rock,
                     register(
                        soilBlockType.name() + "/" + soilVariant.name() + "/" + rock.name(),
                        () -> soilBlockType.TFCCreate(soilVariant, rock),
                        TFCCreativeTabs.EARTH
                     )
                  );
               }

               soilVariantMap.put(soilVariant, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<Rock, RegistryObject<Block>>>> TFCFRockSoilMapper(Class<TFCFRockSoil> enumClass) {
      Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<Rock, RegistryObject<Block>>>> Map = new HashMap<>();

      for (TFCFRockSoil soilBlockType : enumClass.getEnumConstants()) {
            // 掺泥/渍泥弹石三族已废弃（资源已删），不再注册
            if (soilBlockType == TFCFRockSoil.DIRTY_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIER_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIEST_STONE_TILES) {
               continue;
            }
         if (soilBlockType.getTFCFFactory() != null && soilBlockType.getTFCFactory() != null) {
            Map<TFCFSoil.TFCFVariant, Map<Rock, RegistryObject<Block>>> soilVariantMap = new HashMap<>();

            for (TFCFSoil.TFCFVariant soilVariant : TFCFSoil.TFCFVariant.values()) {
               Map<Rock, RegistryObject<Block>> rockMap = new HashMap<>();

               for (Rock rock : Rock.values()) {
                  rockMap.put(
                     rock,
                     register(
                        soilBlockType.name() + "/" + soilVariant.name() + "/" + rock.name(),
                        () -> soilBlockType.TFCFCreate(soilVariant, rock),
                        TFCCreativeTabs.EARTH
                     )
                  );
               }

               soilVariantMap.put(soilVariant, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRockSoil, Map<Variant, Map<TFCFRock, RegistryObject<Block>>>> TFCRockSoil2Mapper(Class<TFCFRockSoil> enumClass) {
      Map<TFCFRockSoil, Map<Variant, Map<TFCFRock, RegistryObject<Block>>>> Map = new HashMap<>();

      for (TFCFRockSoil soilBlockType : enumClass.getEnumConstants()) {
            // 掺泥/渍泥弹石三族已废弃（资源已删），不再注册
            if (soilBlockType == TFCFRockSoil.DIRTY_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIER_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIEST_STONE_TILES) {
               continue;
            }
         if (soilBlockType.getTFCFFactory2() != null && soilBlockType.getTFCFactory2() != null) {
            Map<Variant, Map<TFCFRock, RegistryObject<Block>>> soilVariantMap = new HashMap<>();

            for (Variant soilVariant : Variant.values()) {
         /* silty_loam 已补齐，不再跳过 */
               Map<TFCFRock, RegistryObject<Block>> rockMap = new HashMap<>();

               for (TFCFRock rock : TFCFRock.values()) {
            // 凝灰岩整套岩石方块已移除（TFE 提供），不再生成它的产物
            if (rock == TFCFRock.TUFF) { continue; }
                  rockMap.put(
                     rock,
                     register(
                        soilBlockType.name() + "/" + soilVariant.name() + "/" + rock.name(),
                        () -> soilBlockType.TFCCreate2(soilVariant, rock),
                        TFCCreativeTabs.EARTH
                     )
                  );
               }

               soilVariantMap.put(soilVariant, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<TFCFRock, RegistryObject<Block>>>> TFCFRockSoil2Mapper(Class<TFCFRockSoil> enumClass) {
      Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<TFCFRock, RegistryObject<Block>>>> Map = new HashMap<>();

      for (TFCFRockSoil soilBlockType : enumClass.getEnumConstants()) {
            // 掺泥/渍泥弹石三族已废弃（资源已删），不再注册
            if (soilBlockType == TFCFRockSoil.DIRTY_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIER_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIEST_STONE_TILES) {
               continue;
            }
         if (soilBlockType.getTFCFFactory2() != null && soilBlockType.getTFCFactory2() != null) {
            Map<TFCFSoil.TFCFVariant, Map<TFCFRock, RegistryObject<Block>>> soilVariantMap = new HashMap<>();

            for (TFCFSoil.TFCFVariant soilVariant : TFCFSoil.TFCFVariant.values()) {
               Map<TFCFRock, RegistryObject<Block>> rockMap = new HashMap<>();

               for (TFCFRock rock : TFCFRock.values()) {
            // 凝灰岩整套岩石方块已移除（TFE 提供），不再生成它的产物
            if (rock == TFCFRock.TUFF) { continue; }
                  rockMap.put(
                     rock,
                     register(
                        soilBlockType.name() + "/" + soilVariant.name() + "/" + rock.name(),
                        () -> soilBlockType.TFCFCreate2(soilVariant, rock),
                        TFCCreativeTabs.EARTH
                     )
                  );
               }

               soilVariantMap.put(soilVariant, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRockSoil, Map<Variant, Map<Rock, DecorationBlockRegistryObject>>> TFCRockSoilDecoMapper(Class<TFCFRockSoil> enumClass) {
      Map<TFCFRockSoil, Map<Variant, Map<Rock, DecorationBlockRegistryObject>>> Map = new HashMap<>();

      for (TFCFRockSoil soilBlockType : enumClass.getEnumConstants()) {
            // 掺泥/渍泥弹石三族已废弃（资源已删），不再注册
            if (soilBlockType == TFCFRockSoil.DIRTY_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIER_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIEST_STONE_TILES) {
               continue;
            }
         if (soilBlockType.name().contains("COMPACT_DIRT")) {
            continue; // compact-dirt derived products removed
         }
         if (soilBlockType.getTFCFFactory() != null && soilBlockType.getTFCFactory() != null) {
            Map<Variant, Map<Rock, DecorationBlockRegistryObject>> soilVariantMap = new HashMap<>();

            for (Variant soilVariant : Variant.values()) {
         /* silty_loam 已补齐，不再跳过 */
               Map<Rock, DecorationBlockRegistryObject> rockMap = new HashMap<>();

               for (Rock rock : Rock.values()) {
                  rockMap.put(
                     rock,
                     new DecorationBlockRegistryObject(
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/slab/" + rock.name(),
                           () -> new SlabBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCROCKSOIL.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        ),
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/stairs/" + rock.name(),
                           () -> new StairBlock(
                                 () -> ((Block)TFCROCKSOIL.get(soilBlockType).get(soilVariant).get(rock).get()).defaultBlockState(),
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCROCKSOIL.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        ),
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/wall/" + rock.name(),
                           () -> new WallBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCROCKSOIL.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        )
                     )
                  );
               }

               soilVariantMap.put(soilVariant, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<Rock, DecorationBlockRegistryObject>>> TFCFRockSoilDecoMapper(Class<TFCFRockSoil> enumClass) {
      Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<Rock, DecorationBlockRegistryObject>>> Map = new HashMap<>();

      for (TFCFRockSoil soilBlockType : enumClass.getEnumConstants()) {
            // 掺泥/渍泥弹石三族已废弃（资源已删），不再注册
            if (soilBlockType == TFCFRockSoil.DIRTY_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIER_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIEST_STONE_TILES) {
               continue;
            }
         if (soilBlockType.name().contains("COMPACT_DIRT")) {
            continue; // compact-dirt derived products removed
         }
         if (soilBlockType.getTFCFFactory() != null && soilBlockType.getTFCFactory() != null) {
            Map<TFCFSoil.TFCFVariant, Map<Rock, DecorationBlockRegistryObject>> soilVariantMap = new HashMap<>();

            for (TFCFSoil.TFCFVariant soilVariant : TFCFSoil.TFCFVariant.values()) {
               Map<Rock, DecorationBlockRegistryObject> rockMap = new HashMap<>();

               for (Rock rock : Rock.values()) {
                  rockMap.put(
                     rock,
                     new DecorationBlockRegistryObject(
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/slab/" + rock.name(),
                           () -> new SlabBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCFROCKSOIL.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        ),
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/stairs/" + rock.name(),
                           () -> new StairBlock(
                                 () -> ((Block)TFCFROCKSOIL.get(soilBlockType).get(soilVariant).get(rock).get()).defaultBlockState(),
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCFROCKSOIL.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        ),
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/wall/" + rock.name(),
                           () -> new WallBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCFROCKSOIL.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        )
                     )
                  );
               }

               soilVariantMap.put(soilVariant, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRockSoil, Map<Variant, Map<TFCFRock, DecorationBlockRegistryObject>>> TFCRockSoilDeco2Mapper(Class<TFCFRockSoil> enumClass) {
      Map<TFCFRockSoil, Map<Variant, Map<TFCFRock, DecorationBlockRegistryObject>>> Map = new HashMap<>();

      for (TFCFRockSoil soilBlockType : enumClass.getEnumConstants()) {
            // 掺泥/渍泥弹石三族已废弃（资源已删），不再注册
            if (soilBlockType == TFCFRockSoil.DIRTY_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIER_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIEST_STONE_TILES) {
               continue;
            }
         if (soilBlockType.name().contains("COMPACT_DIRT")) {
            continue; // compact-dirt derived products removed
         }
         if (soilBlockType.getTFCFFactory2() != null && soilBlockType.getTFCFactory2() != null) {
            Map<Variant, Map<TFCFRock, DecorationBlockRegistryObject>> soilVariantMap = new HashMap<>();

            for (Variant soilVariant : Variant.values()) {
         /* silty_loam 已补齐，不再跳过 */
               Map<TFCFRock, DecorationBlockRegistryObject> rockMap = new HashMap<>();

               for (TFCFRock rock : TFCFRock.values()) {
            // 凝灰岩整套岩石方块已移除（TFE 提供），不再生成它的产物
            if (rock == TFCFRock.TUFF) { continue; }
                  rockMap.put(
                     rock,
                     new DecorationBlockRegistryObject(
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/slab/" + rock.name(),
                           () -> new SlabBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCROCKSOIL2.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        ),
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/stairs/" + rock.name(),
                           () -> new StairBlock(
                                 () -> ((Block)TFCROCKSOIL2.get(soilBlockType).get(soilVariant).get(rock).get()).defaultBlockState(),
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCROCKSOIL2.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        ),
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/wall/" + rock.name(),
                           () -> new WallBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCROCKSOIL2.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        )
                     )
                  );
               }

               soilVariantMap.put(soilVariant, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<TFCFRock, DecorationBlockRegistryObject>>> TFCFRockSoilDeco2Mapper(
      Class<TFCFRockSoil> enumClass
   ) {
      Map<TFCFRockSoil, Map<TFCFSoil.TFCFVariant, Map<TFCFRock, DecorationBlockRegistryObject>>> Map = new HashMap<>();

      for (TFCFRockSoil soilBlockType : enumClass.getEnumConstants()) {
            // 掺泥/渍泥弹石三族已废弃（资源已删），不再注册
            if (soilBlockType == TFCFRockSoil.DIRTY_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIER_STONE_TILES
               || soilBlockType == TFCFRockSoil.DIRTIEST_STONE_TILES) {
               continue;
            }
         if (soilBlockType.name().contains("COMPACT_DIRT")) {
            continue; // compact-dirt derived products removed
         }
         if (soilBlockType.getTFCFFactory2() != null && soilBlockType.getTFCFactory2() != null) {
            Map<TFCFSoil.TFCFVariant, Map<TFCFRock, DecorationBlockRegistryObject>> soilVariantMap = new HashMap<>();

            for (TFCFSoil.TFCFVariant soilVariant : TFCFSoil.TFCFVariant.values()) {
               Map<TFCFRock, DecorationBlockRegistryObject> rockMap = new HashMap<>();

               for (TFCFRock rock : TFCFRock.values()) {
            // 凝灰岩整套岩石方块已移除（TFE 提供），不再生成它的产物
            if (rock == TFCFRock.TUFF) { continue; }
                  rockMap.put(
                     rock,
                     new DecorationBlockRegistryObject(
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/slab/" + rock.name(),
                           () -> new SlabBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCFROCKSOIL2.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        ),
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/stairs/" + rock.name(),
                           () -> new StairBlock(
                                 () -> ((Block)TFCFROCKSOIL2.get(soilBlockType).get(soilVariant).get(rock).get()).defaultBlockState(),
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCFROCKSOIL2.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        ),
                        register(
                           soilBlockType.name() + "/" + soilVariant.name() + "/wall/" + rock.name(),
                           () -> new WallBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCFROCKSOIL2.get(soilBlockType).get(soilVariant).get(rock).get()
                                 )
                              ),
                           TFCCreativeTabs.EARTH
                        )
                     )
                  );
               }

               soilVariantMap.put(soilVariant, rockMap);
            }

            Map.put(soilBlockType, soilVariantMap);
         }
      }

      return Map;
   }

   private static Map<TFCFRock, Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, DecorationBlockRegistryObject>> RockTypeDecoTFCMapper(
      Class<TFCFRock> enumClass
   ) {
      Map<TFCFRock, Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, DecorationBlockRegistryObject>> Map = new HashMap<>();

      for (TFCFRock rock : enumClass.getEnumConstants()) {
         Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, DecorationBlockRegistryObject> typeMap = new HashMap<>();

         for (net.dries007.tfc.common.blocks.rock.Rock.BlockType type : net.dries007.tfc.common.blocks.rock.Rock.BlockType.values()) {
            if (type.hasVariants()) {
               typeMap.put(
                  type,
                  new DecorationBlockRegistryObject(
                     register("rock/" + type.name() + "/slab/" + rock.name(), () -> type.createSlab(rock), TFCCreativeTabs.ROCKS),
                     register("rock/" + type.name() + "/stairs/" + rock.name(), () -> type.createStairs(rock), TFCCreativeTabs.ROCKS),
                     register("rock/" + type.name() + "/wall/" + rock.name(), () -> type.createWall(rock), TFCCreativeTabs.ROCKS)
                  )
               );
            }
         }

         Map.put(rock, typeMap);
      }

      return Map;
   }

   private static Map<Rock, Map<TFCFRock.TFCFBlockType, DecorationBlockRegistryObject>> RockDecoTFCMapper(Class<Rock> enumClass) {
      Map<Rock, Map<TFCFRock.TFCFBlockType, DecorationBlockRegistryObject>> Map = new HashMap<>();

      for (Rock rock : enumClass.getEnumConstants()) {
         Map<TFCFRock.TFCFBlockType, DecorationBlockRegistryObject> typeMap = new HashMap<>();

         for (TFCFRock.TFCFBlockType type : TFCFRock.TFCFBlockType.values()) {
            if (type.getTFCFactory() != null && type.hasVariants()) {
               if (type == TFCFRock.TFCFBlockType.COBBLED_BRICKS || type == TFCFRock.TFCFBlockType.FLAGSTONE_BRICKS) {
                  typeMap.put(
                     type,
                     new DecorationBlockRegistryObject(
                        register(
                           "rock/" + type.name() + "/slab/" + rock.name(),
                           () -> new MossSpreadingSlabBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy((BlockBehaviour)ROCK_BLOCKS.get(rock).get(type).get())
                              ),
                           TFCCreativeTabs.ROCKS
                        ),
                        register(
                           "rock/" + type.name() + "/stairs/" + rock.name(),
                           () -> new MossSpreadingStairBlock(
                                 () -> ((Block)ROCK_BLOCKS.get(rock).get(type).get()).defaultBlockState(),
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy((BlockBehaviour)ROCK_BLOCKS.get(rock).get(type).get())
                              ),
                           TFCCreativeTabs.ROCKS
                        ),
                        register(
                           "rock/" + type.name() + "/wall/" + rock.name(),
                           () -> new MossSpreadingWallBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy((BlockBehaviour)ROCK_BLOCKS.get(rock).get(type).get())
                              ),
                           TFCCreativeTabs.ROCKS
                        )
                     )
                  );
               }
            }
         }

         Map.put(rock, typeMap);
      }

      return Map;
   }

   private static Map<TFCFRock, Map<TFCFRock.TFCFBlockType, DecorationBlockRegistryObject>> RockDecoTFCFMapper(Class<TFCFRock> enumClass) {
      Map<TFCFRock, Map<TFCFRock.TFCFBlockType, DecorationBlockRegistryObject>> Map = new HashMap<>();

      for (TFCFRock rock : enumClass.getEnumConstants()) {
         Map<TFCFRock.TFCFBlockType, DecorationBlockRegistryObject> typeMap = new HashMap<>();

         for (TFCFRock.TFCFBlockType type : TFCFRock.TFCFBlockType.values()) {
            if (type.getTFCFFactory() != null && type.hasVariants()) {
               if (type == TFCFRock.TFCFBlockType.COBBLED_BRICKS || type == TFCFRock.TFCFBlockType.FLAGSTONE_BRICKS) {
                  typeMap.put(
                     type,
                     new DecorationBlockRegistryObject(
                        register(
                           "rock/" + type.name() + "/slab/" + rock.name(),
                           () -> new MossGrowingSlabBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()
                                 ),
                                 TFCF_ROCK_DECORATIONS.get(rock).get(type.mossy()).slab()
                              ),
                           TFCCreativeTabs.ROCKS
                        ),
                        register(
                           "rock/" + type.name() + "/stairs/" + rock.name(),
                           () -> new MossGrowingStairsBlock(
                                 () -> ((Block)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()).defaultBlockState(),
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()
                                 ),
                                 TFCF_ROCK_DECORATIONS.get(rock).get(type.mossy()).stair()
                              ),
                           TFCCreativeTabs.ROCKS
                        ),
                        register(
                           "rock/" + type.name() + "/wall/" + rock.name(),
                           () -> new MossGrowingWallBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()
                                 ),
                                 TFCF_ROCK_DECORATIONS.get(rock).get(type.mossy()).wall()
                              ),
                           TFCCreativeTabs.ROCKS
                        )
                     )
                  );
               } else if (type != TFCFRock.TFCFBlockType.MOSSY_COBBLED_BRICKS && type != TFCFRock.TFCFBlockType.MOSSY_FLAGSTONE_BRICKS) {
                  typeMap.put(
                     type,
                     new DecorationBlockRegistryObject(
                        register(
                           "rock/" + type.name() + "/slab/" + rock.name(),
                           () -> new SlabBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()
                                 )
                              ),
                           TFCCreativeTabs.ROCKS
                        ),
                        register(
                           "rock/" + type.name() + "/stairs/" + rock.name(),
                           () -> new StairBlock(
                                 () -> ((Block)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()).defaultBlockState(),
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()
                                 )
                              ),
                           TFCCreativeTabs.ROCKS
                        ),
                        register(
                           "rock/" + type.name() + "/wall/" + rock.name(),
                           () -> new WallBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()
                                 )
                              ),
                           TFCCreativeTabs.ROCKS
                        )
                     )
                  );
               } else {
                  typeMap.put(
                     type,
                     new DecorationBlockRegistryObject(
                        register(
                           "rock/" + type.name() + "/slab/" + rock.name(),
                           () -> new MossSpreadingSlabBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()
                                 )
                              ),
                           TFCCreativeTabs.ROCKS
                        ),
                        register(
                           "rock/" + type.name() + "/stairs/" + rock.name(),
                           () -> new MossSpreadingStairBlock(
                                 () -> ((Block)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()).defaultBlockState(),
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()
                                 )
                              ),
                           TFCCreativeTabs.ROCKS
                        ),
                        register(
                           "rock/" + type.name() + "/wall/" + rock.name(),
                           () -> new MossSpreadingWallBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.copy(
                                    (BlockBehaviour)TFCF_ROCKTYPE_BLOCKS.get(rock).get(type).get()
                                 )
                              ),
                           TFCCreativeTabs.ROCKS
                        )
                     )
                  );
               }
            }
         }

         Map.put(rock, typeMap);
      }

      return Map;
   }

   private static Map<DripstoneRock, Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, RegistryObject<Block>>> dripstoneMapper(
      Class<DripstoneRock> enumClass
   ) {
      Map<DripstoneRock, Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, RegistryObject<Block>>> Map = new HashMap<>();

      for (DripstoneRock rock : enumClass.getEnumConstants()) {
         Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, RegistryObject<Block>> typeMap = new HashMap<>();

         for (net.dries007.tfc.common.blocks.rock.Rock.BlockType type : net.dries007.tfc.common.blocks.rock.Rock.BlockType.values()) {
            if (type == net.dries007.tfc.common.blocks.rock.Rock.BlockType.SPIKE) {
               if (rock == DripstoneRock.GYPSUM) {
                  typeMap.put(
                     type,
                     register(
                        "rock/" + type.name() + "/" + rock.name(),
                        () -> TFCFPointedDripstoneBlock.create(
                              ExtendedProperties.of(MapColor.STONE).requiresCorrectToolForDrops()
                                 .sound(SoundType.AMETHYST)
                                 .strength(rock.category().hardness(2.0F), 7.0F)
                                 .requiresCorrectToolForDrops()
                                 .noOcclusion()
                                 .randomTicks()
                                 .dynamicShape()
                                 .hasPostProcess(TFCFBlocks::always)
                                 .lightLevel(state -> 6),
                              () -> (Block)DRIPSTONE_BLOCKS.get(rock).get(net.dries007.tfc.common.blocks.rock.Rock.BlockType.SPIKE).get()
                           ),
                        TFCCreativeTabs.ROCKS
                     )
                  );
               } else {
                  typeMap.put(
                     type,
                     register(
                        "rock/" + type.name() + "/" + rock.name(),
                        () -> TFCFPointedDripstoneBlock.create(
                              ExtendedProperties.of(MapColor.STONE).requiresCorrectToolForDrops()
                                 .sound(SoundType.POINTED_DRIPSTONE)
                                 .strength(rock.category().hardness(4.0F), 10.0F)
                                 .requiresCorrectToolForDrops()
                                 .noOcclusion()
                                 .randomTicks()
                                 .dynamicShape()
                                 .hasPostProcess(TFCFBlocks::always),
                              () -> (Block)DRIPSTONE_BLOCKS.get(rock).get(net.dries007.tfc.common.blocks.rock.Rock.BlockType.SPIKE).get()
                           ),
                        TFCCreativeTabs.ROCKS
                     )
                  );
               }
            } else if ((
                  type == net.dries007.tfc.common.blocks.rock.Rock.BlockType.RAW
                     || type == net.dries007.tfc.common.blocks.rock.Rock.BlockType.HARDENED
                     || type == net.dries007.tfc.common.blocks.rock.Rock.BlockType.LOOSE
               )
               && (type != net.dries007.tfc.common.blocks.rock.Rock.BlockType.LOOSE || rock != DripstoneRock.GYPSUM)) {
               typeMap.put(type, register("rock/" + type.name() + "/" + rock.name(), () -> type.create(rock), TFCCreativeTabs.ROCKS));
            }
         }

         Map.put(rock, typeMap);
      }

      return Map;
   }

   private static Map<Rock, Map<TFCFRock.TFCFBlockType, RegistryObject<Block>>> rockTypeTFCMapper(Class<Rock> enumClass) {
      Map<Rock, Map<TFCFRock.TFCFBlockType, RegistryObject<Block>>> Map = new HashMap<>();

      for (Rock rock : enumClass.getEnumConstants()) {
         Map<TFCFRock.TFCFBlockType, RegistryObject<Block>> typeMap = new HashMap<>();

         for (TFCFRock.TFCFBlockType type : TFCFRock.TFCFBlockType.values()) {
            if (type.getTFCFactory() != null) {
               typeMap.put(type, register("rock/" + type.name() + "/" + rock.name(), () -> type.createTFC(rock), TFCCreativeTabs.ROCKS));
            }
         }

         Map.put(rock, typeMap);
      }

      return Map;
   }

   private static Map<TFCFRock, Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, RegistryObject<Block>>> rockMapper(Class<TFCFRock> enumClass) {
      Map<TFCFRock, Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, RegistryObject<Block>>> Map = new HashMap<>();

      for (TFCFRock rock : enumClass.getEnumConstants()) {
         Map<net.dries007.tfc.common.blocks.rock.Rock.BlockType, RegistryObject<Block>> typeMap = new HashMap<>();

         // 凝灰岩整套岩石方块由 TFE 提供，本模组不再注册（空表而不是缺键，避免下游 NPE）。
         if (rock == TFCFRock.TUFF) {
            Map.put(rock, typeMap);
            continue;
         }

         for (net.dries007.tfc.common.blocks.rock.Rock.BlockType type : net.dries007.tfc.common.blocks.rock.Rock.BlockType.values()) {
            typeMap.put(type, register("rock/" + type.name() + "/" + rock.name(), () -> type.create(rock), TFCCreativeTabs.ROCKS));
         }

         Map.put(rock, typeMap);
      }

      return Map;
   }

   private static Map<TFCFRock, Map<TFCFRock.TFCFBlockType, RegistryObject<Block>>> rockTypeMapper(Class<TFCFRock> enumClass) {
      Map<TFCFRock, Map<TFCFRock.TFCFBlockType, RegistryObject<Block>>> Map = new HashMap<>();

      for (TFCFRock rock : enumClass.getEnumConstants()) {
         Map<TFCFRock.TFCFBlockType, RegistryObject<Block>> typeMap = new HashMap<>();

         for (TFCFRock.TFCFBlockType type : TFCFRock.TFCFBlockType.values()) {
            if (type.getTFCFFactory() != null) {
               typeMap.put(type, register("rock/" + type.name() + "/" + rock.name(), () -> type.createTFCF(rock), TFCCreativeTabs.ROCKS));
            }
         }

         Map.put(rock, typeMap);
      }

      return Map;
   }

   public static void registerFlowerPotFlowers() {
      FlowerPotBlock pot = (FlowerPotBlock)Blocks.FLOWER_POT;
      POTTED_PLANTS.forEach((plant, reg) -> pot.addPlant(PLANTS.get(plant).getId(), reg));
      WOODS.forEach((wood, map) -> pot.addPlant(map.get(BlockType.SAPLING).getId(), (Supplier)map.get(BlockType.POTTED_SAPLING)));
   }

   /**
    * 注册岩石方块，{@code withItem == false} 时只注册方块本体、不生成 BlockItem。
    * <p>
    * 凝灰岩（tuff）的整套岩石方块由 TFE 提供，TFCF 这边再给一份物品只会在创造栏里重复，
    * 所以 tuff 的 raw/hardened/smooth/cobble/bricks/gravel/spike/cracked_bricks/chiseled/
    * loose/pressure_plate/button/aqueduct/mossy_* 以及石砧都只保留方块、不再注册物品。
    * 保留方块本体是刻意的：世界存档、配方与内部引用都还指向它们，直接删方块会变成空气。
    */
   private static RegistryObject<Block> registerRockBlock(String name, Supplier<? extends Block> supplier, boolean withItem) {
      final Supplier<Block> wrapped = () -> supplier.get();
      return withItem ? register(name, wrapped, TFCCreativeTabs.ROCKS) : register(name, wrapped);
   }

   private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier) {
      return register(name, blockSupplier, (Function<T, ? extends BlockItem>)null);
   }

   private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, TFCCreativeTabs.CreativeTabHolder group) {
      return register(name, blockSupplier, (Function<T, ? extends BlockItem>)(block -> new BlockItem(block, new Properties())));
   }

   private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier, Properties blockItemProperties) {
      return register(name, blockSupplier, (Function<T, ? extends BlockItem>)(block -> new BlockItem(block, blockItemProperties)));
   }

   private static <T extends Block> RegistryObject<T> register(
      String name, Supplier<T> blockSupplier, @Nullable Function<T, ? extends BlockItem> blockItemFactory
   ) {
      return RegistrationHelpers.registerBlock(BLOCKS, TFCFItems.ITEMS, name, blockSupplier, blockItemFactory);
   }
}
