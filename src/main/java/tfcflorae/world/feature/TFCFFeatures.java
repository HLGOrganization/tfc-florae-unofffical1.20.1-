package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.dries007.tfc.world.Codecs;
import net.dries007.tfc.world.feature.FissureConfig;
import net.dries007.tfc.world.feature.HotSpringConfig;
import net.dries007.tfc.world.feature.tree.ForestConfig;
import net.dries007.tfc.world.feature.tree.RandomTreeConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.world.feature.tree.BambooLeavesDecorator;
import tfcflorae.world.feature.tree.ForestFeature;
import tfcflorae.world.feature.tree.RootedTreeConfig;
import tfcflorae.world.feature.tree.ShrublandConfig;
import tfcflorae.world.feature.tree.ShrublandFeature;
import tfcflorae.world.feature.tree.SubsurfaceForestFeature;
import tfcflorae.world.feature.tree.joshua.JoshuaTreeFeature;
import tfcflorae.world.feature.tree.mangrove.AttachedToLeavesDecorator;
import tfcflorae.world.feature.tree.mangrove.RootedTreeFeature;
import tfcflorae.world.feature.tree.mangrove.UpwardBranchingTrunk;
import tfcflorae.world.feature.tree.mangrove.WeightedLeaveVineDecorator;

public class TFCFFeatures {
   public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "tfcflorae");
   public static final TFCFAbstractDeferredRegistry<TrunkPlacerType<?>> TRUNK_DECOR = TFCFDeferredRegistry.create(Registries.TRUNK_PLACER_TYPE, "tfcflorae");
   public static final TFCFAbstractDeferredRegistry<TreeDecoratorType<?>> LEAF_DECOR = TFCFDeferredRegistry.create(Registries.TREE_DECORATOR_TYPE, "tfcflorae");
   public static final Supplier<TrunkPlacerType<UpwardBranchingTrunk>> UPWARDS_BRANCHING_TRUNK = TRUNK_DECOR.register(
      "upward_branching_trunk", () -> new TrunkPlacerType<>(UpwardBranchingTrunk.CODEC)
   );
   public static final Supplier<TreeDecoratorType<WeightedLeaveVineDecorator>> WEIGHTED_LEAVE_VINE = LEAF_DECOR.register(
      "leave_vine", () -> new TreeDecoratorType<>(WeightedLeaveVineDecorator.CODEC)
   );
   public static final Supplier<TreeDecoratorType<AttachedToLeavesDecorator>> ATTACHED_TO_LEAVES = LEAF_DECOR.register(
      "attached_to_leaves", () -> new TreeDecoratorType<>(AttachedToLeavesDecorator.CODEC)
   );
   public static final RegistryObject<Feature<RootedTreeConfig>> VANILLA_MANGROVE_TREE = register(
      "vanilla_mangrove_tree", RootedTreeFeature::new, RootedTreeConfig.CODEC
   );
   public static final RegistryObject<Feature<TreeConfiguration>> BAMBOO_TREE = register("bamboo_tree", TreeFeature::new, TreeConfiguration.CODEC);
   public static final RegistryObject<Feature<TreeConfiguration>> VANILLA_BAMBOO_TREE = register(
      "vanilla_bamboo_tree", TreeFeature::new, TreeConfiguration.CODEC
   );
   public static final Supplier<TreeDecoratorType<BambooLeavesDecorator>> BAMBOO_LEAVES = LEAF_DECOR.register(
      "bamboo_leaves", () -> new TreeDecoratorType<>(BambooLeavesDecorator.CODEC)
   );
   public static final RegistryObject<JoshuaTreeFeature> JOSHUA_TREE = register("joshua_tree", JoshuaTreeFeature::new, JoshuaTreeFeature.CODEC);
   // 猴面包树随木材一起裁减（BaobabTreeFeature 已移入 parked/），不再注册 baobab_tree 类型。
   // 普通树一律走 TFC 原生 tfc:random_tree：我们的 TFCFRandomTreeFeature / TFCFTreeHelpers /
   // RandomBeachTreeFeature 都是 1.18.2 的副本（placeTemplate 的 boundingBox 判断写反，
   // 会导致树叶不被放置），已全部移入 parked/，与 TFC 树共用同一条代码路径。
   // [未注册] BEE_NEST —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<BeeNestFeature> BEE_NEST = register("bee_nest", BeeNestFeature::new, NoneFeatureConfiguration.CODEC);
   // [未注册] HOT_SPRING —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<HotSpringFeature> HOT_SPRING = register("hot_spring", HotSpringFeature::new, HotSpringConfig.CODEC);
   // [未注册] MONSTER_ROOM —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<MonsterRoomFeature> MONSTER_ROOM = register("monster_room", MonsterRoomFeature::new, RockConfig.CODEC);
   // [未注册] FOREST —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<ForestFeature> FOREST = register("forest", ForestFeature::new, ForestConfig.CODEC);
   // [未注册] SHRUBLAND —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<ShrublandFeature> SHRUBLAND = register("shrubland", ShrublandFeature::new, ShrublandConfig.CODEC);
   // [未注册] SUBSURFACE_FOREST —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<SubsurfaceForestFeature> SUBSURFACE_FOREST = register(
   // "subsurface_forest", SubsurfaceForestFeature::new, ForestConfig.CODEC
   // );
   // [未注册] GLACIER —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<GlacierFeature> GLACIER = register("glacier", GlacierFeature::new, BlockStateConfiguration.CODEC);
   // [未注册] CRYSTAL —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<CrystalFeature> CRYSTAL = register("crystal", CrystalFeature::new, BlockStateConfiguration.CODEC);
   // [未注册] BOULDER_BLOCK —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<BoulderRockFeature> BOULDER_BLOCK = register("boulder_block", BoulderRockFeature::new, RockConfig.CODEC);
   // [未注册] DEEP_BOULDER_BLOCK —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<BoulderDeepRockFeature> DEEP_BOULDER_BLOCK = register("deep_boulder_block", BoulderDeepRockFeature::new, RockConfig.CODEC);
   // [未注册] LOOSE_FLINT —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<LooseFlintFeature> LOOSE_FLINT = register("loose_flint", LooseFlintFeature::new, NoneFeatureConfiguration.CODEC);
   // [未注册] ROCK_COLUMN —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<RockColumnFeature> ROCK_COLUMN = register("rock_column", RockColumnFeature::new, RockColumnConfig.CODEC);
   // [未注册] ROCK_PILLAR —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<RockPillarFeature> ROCK_PILLAR = register("rock_pillar", RockPillarFeature::new, RockPillarConfig.CODEC);
   // [未注册] FREEZE_TOP_LAYER —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<SnowAndFreezeFeature> FREEZE_TOP_LAYER = register(
   // "freeze_top_layer", SnowAndFreezeFeature::new, NoneFeatureConfiguration.CODEC
   // );
   // [未注册] UNDERWATER_MAGMA —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<UnderwaterMagmaFeature> UNDERWATER_MAGMA = register(
   // "underwater_magma", UnderwaterMagmaFeature::new, UnderwaterMagmaConfig.CODEC
   // );
   // [未注册] DRIPSTONE_CLUSTER —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<DripstoneClusterFeature> DRIPSTONE_CLUSTER = register(
   // "dripstone_cluster", DripstoneClusterFeature::new, DripstoneClusterConfig.CODEC
   // );
   // [未注册] LARGE_DRIPSTONE —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<LargeDripstoneFeature> LARGE_DRIPSTONE = register(
   // "large_dripstone", LargeDripstoneFeature::new, LargeDripstoneConfig.CODEC
   // );
   // [未注册] POINTED_DRIPSTONE —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<PointedDripstoneFeature> POINTED_DRIPSTONE = register(
   // "pointed_dripstone", PointedDripstoneFeature::new, PointedDripstoneConfig.CODEC
   // );
   // [未注册] GEYSER_CLUSTER —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<GeyserClusterFeature> GEYSER_CLUSTER = register("geyser_cluster", GeyserClusterFeature::new, GeyserClusterConfig.CODEC);
   // [未注册] GEYSER_POINTED —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<GeyserPointedFeature> GEYSER_POINTED = register("geyser_pointed", GeyserPointedFeature::new, GeyserPointedConfig.CODEC);
   // [未注册] LAKE —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<LakeFeature> LAKE = register("lake", LakeFeature::new, LakeConfig.CODEC);
   // [未注册] BLOCK_COLUMN —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<ColumnFeature> BLOCK_COLUMN = register("block_column", ColumnFeature::new, BlockColumnConfiguration.CODEC);
   // [未注册] ROCK_BOULDER —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<BouldersFeature> ROCK_BOULDER = register("boulder", BouldersFeature::new, BoulderConfig.CODEC);
   // [未注册] DELTA —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<DeltaFeature> DELTA = register("delta", DeltaFeature::new, DeltaConfig.CODEC);
   // [未注册] FISSURE —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<FissureFeature> FISSURE = register("fissure", FissureFeature::new, FissureConfig.CODEC);
   // [未注册] RIVULET —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<RivuletFeature> RIVULET = register("rivulet", RivuletFeature::new, Codecs.BLOCK_STATE_CONFIG);
   // [未注册] SAND_DUNES —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<SandDunesFeature> SAND_DUNES = register("sand_dunes", SandDunesFeature::new, NoneFeatureConfiguration.CODEC);
   // [未注册] SAND_LAYER —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<SandLayerFeature> SAND_LAYER = register("sand_layer", SandLayerFeature::new, NoneFeatureConfiguration.CODEC);
   // [未注册] GRAVEL_LAYER —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<GravelLayerFeature> GRAVEL_LAYER = register("gravel_layer", GravelLayerFeature::new, NoneFeatureConfiguration.CODEC);
   // [未注册] TIDE_POOL —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<TidePoolFeature> TIDE_POOL = register("tide_pool", TidePoolFeature::new, NoneFeatureConfiguration.CODEC);
   // [未注册] UNDERGROUND_RIVER —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<SubTerraneanRiver> UNDERGROUND_RIVER = register(
   // "underground_river", SubTerraneanRiver::new, NoneFeatureConfiguration.CODEC
   // );
   // [未注册] BAMBOO —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<TFCBambooFeature> BAMBOO = register("bamboo", TFCBambooFeature::new, ProbabilityFeatureConfiguration.CODEC);
   // [未注册] ROOT_SPIKE —— TFC 1.20.1 已有等价实现，或从未被数据包引用（类保留在源码树中）
   // public static final RegistryObject<RootSpikeFeature> ROOT_SPIKE = register("root_spike", RootSpikeFeature::new, BlockStateConfiguration.CODEC);

   public TFCFFeatures() {
   }

   private static <C extends FeatureConfiguration, F extends Feature<C>> RegistryObject<F> register(String name, Function<Codec<C>, F> factory, Codec<C> codec) {
      return FEATURES.register(name, () -> factory.apply(codec));
   }

}
