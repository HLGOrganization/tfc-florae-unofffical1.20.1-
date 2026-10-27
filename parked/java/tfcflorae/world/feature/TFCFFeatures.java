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
import tfcflorae.world.feature.tree.DynamicTreeConfig;
import tfcflorae.world.feature.tree.ForestFeature;
import tfcflorae.world.feature.tree.RandomBeachTreeFeature;
import tfcflorae.world.feature.tree.RootedTreeConfig;
import tfcflorae.world.feature.tree.ShrublandConfig;
import tfcflorae.world.feature.tree.ShrublandFeature;
import tfcflorae.world.feature.tree.SubsurfaceForestFeature;
import tfcflorae.world.feature.tree.TFCFRandomTreeFeature;
import tfcflorae.world.feature.tree.baobab.BaobabTreeFeature;
import tfcflorae.world.feature.tree.joshua.JoshuaTreeFeature;
import tfcflorae.world.feature.tree.mangrove.AttachedToLeavesDecorator;
import tfcflorae.world.feature.tree.mangrove.RootedTreeFeature;
import tfcflorae.world.feature.tree.mangrove.UpwardBranchingTrunk;
import tfcflorae.world.feature.tree.mangrove.WeightedLeaveVineDecorator;
import tfcflorae.world.feature.tree.palm.CoconutPalmTreeFeature;
import tfcflorae.world.feature.tree.palm.DatePalmTreeFeature;
import tfcflorae.world.feature.tree.palm.PalmTreeFeature;

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
   public static final RegistryObject<BaobabTreeFeature> BAOBAB_TREE = register("baobab_tree", BaobabTreeFeature::new, DynamicTreeConfig.CODEC);
   public static final RegistryObject<TFCFRandomTreeFeature> RANDOM_TREE = register("random_tree", TFCFRandomTreeFeature::new, RandomTreeConfig.CODEC);
   public static final RegistryObject<RandomBeachTreeFeature> RANDOM_BEACH_TREE = register(
      "random_beach_tree", RandomBeachTreeFeature::new, RandomTreeConfig.CODEC
   );
   public static final RegistryObject<PalmTreeFeature> PALM_TREE = register("palm_tree", PalmTreeFeature::new, NoneFeatureConfiguration.CODEC);
   public static final RegistryObject<CoconutPalmTreeFeature> COCONUT_PALM_TREE = register(
      "coconut_palm_tree", CoconutPalmTreeFeature::new, NoneFeatureConfiguration.CODEC
   );
   public static final RegistryObject<DatePalmTreeFeature> DATE_PALM_TREE = register(
      "date_palm_tree", DatePalmTreeFeature::new, NoneFeatureConfiguration.CODEC
   );
   public static final RegistryObject<BeeNestFeature> BEE_NEST = register("bee_nest", BeeNestFeature::new, NoneFeatureConfiguration.CODEC);
   public static final RegistryObject<HotSpringFeature> HOT_SPRING = register("hot_spring", HotSpringFeature::new, HotSpringConfig.CODEC);
   public static final RegistryObject<MonsterRoomFeature> MONSTER_ROOM = register("monster_room", MonsterRoomFeature::new, RockConfig.CODEC);
   public static final RegistryObject<ForestFeature> FOREST = register("forest", ForestFeature::new, ForestConfig.CODEC);
   public static final RegistryObject<ShrublandFeature> SHRUBLAND = register("shrubland", ShrublandFeature::new, ShrublandConfig.CODEC);
   public static final RegistryObject<SubsurfaceForestFeature> SUBSURFACE_FOREST = register(
      "subsurface_forest", SubsurfaceForestFeature::new, ForestConfig.CODEC
   );
   public static final RegistryObject<GlacierFeature> GLACIER = register("glacier", GlacierFeature::new, BlockStateConfiguration.CODEC);
   public static final RegistryObject<CrystalFeature> CRYSTAL = register("crystal", CrystalFeature::new, BlockStateConfiguration.CODEC);
   public static final RegistryObject<BoulderRockFeature> BOULDER_BLOCK = register("boulder_block", BoulderRockFeature::new, RockConfig.CODEC);
   public static final RegistryObject<RotatedBoulderRockFeature> ROTATED_BOULDER_BLOCK = register(
      "rotated_boulder_block", RotatedBoulderRockFeature::new, NoneFeatureConfiguration.CODEC
   );
   public static final RegistryObject<BoulderDeepRockFeature> DEEP_BOULDER_BLOCK = register("deep_boulder_block", BoulderDeepRockFeature::new, RockConfig.CODEC);
   public static final RegistryObject<LooseFlintFeature> LOOSE_FLINT = register("loose_flint", LooseFlintFeature::new, NoneFeatureConfiguration.CODEC);
   public static final RegistryObject<RockColumnFeature> ROCK_COLUMN = register("rock_column", RockColumnFeature::new, RockColumnConfig.CODEC);
   public static final RegistryObject<RockPillarFeature> ROCK_PILLAR = register("rock_pillar", RockPillarFeature::new, RockPillarConfig.CODEC);
   public static final RegistryObject<SnowAndFreezeFeature> FREEZE_TOP_LAYER = register(
      "freeze_top_layer", SnowAndFreezeFeature::new, NoneFeatureConfiguration.CODEC
   );
   public static final RegistryObject<UnderwaterMagmaFeature> UNDERWATER_MAGMA = register(
      "underwater_magma", UnderwaterMagmaFeature::new, UnderwaterMagmaConfig.CODEC
   );
   public static final RegistryObject<DripstoneClusterFeature> DRIPSTONE_CLUSTER = register(
      "dripstone_cluster", DripstoneClusterFeature::new, DripstoneClusterConfig.CODEC
   );
   public static final RegistryObject<LargeDripstoneFeature> LARGE_DRIPSTONE = register(
      "large_dripstone", LargeDripstoneFeature::new, LargeDripstoneConfig.CODEC
   );
   public static final RegistryObject<PointedDripstoneFeature> POINTED_DRIPSTONE = register(
      "pointed_dripstone", PointedDripstoneFeature::new, PointedDripstoneConfig.CODEC
   );
   public static final RegistryObject<GeyserClusterFeature> GEYSER_CLUSTER = register("geyser_cluster", GeyserClusterFeature::new, GeyserClusterConfig.CODEC);
   public static final RegistryObject<GeyserPointedFeature> GEYSER_POINTED = register("geyser_pointed", GeyserPointedFeature::new, GeyserPointedConfig.CODEC);
   public static final RegistryObject<LakeFeature> LAKE = register("lake", LakeFeature::new, LakeConfig.CODEC);
   public static final RegistryObject<ColumnFeature> BLOCK_COLUMN = register("block_column", ColumnFeature::new, BlockColumnConfiguration.CODEC);
   public static final RegistryObject<BouldersFeature> ROCK_BOULDER = register("boulder", BouldersFeature::new, BoulderConfig.CODEC);
   public static final RegistryObject<DeltaFeature> DELTA = register("delta", DeltaFeature::new, DeltaConfig.CODEC);
   public static final RegistryObject<FissureFeature> FISSURE = register("fissure", FissureFeature::new, FissureConfig.CODEC);
   public static final RegistryObject<RivuletFeature> RIVULET = register("rivulet", RivuletFeature::new, Codecs.BLOCK_STATE_CONFIG);
   public static final RegistryObject<SandDunesFeature> SAND_DUNES = register("sand_dunes", SandDunesFeature::new, NoneFeatureConfiguration.CODEC);
   public static final RegistryObject<SandLayerFeature> SAND_LAYER = register("sand_layer", SandLayerFeature::new, NoneFeatureConfiguration.CODEC);
   public static final RegistryObject<GravelLayerFeature> GRAVEL_LAYER = register("gravel_layer", GravelLayerFeature::new, NoneFeatureConfiguration.CODEC);
   public static final RegistryObject<TidePoolFeature> TIDE_POOL = register("tide_pool", TidePoolFeature::new, NoneFeatureConfiguration.CODEC);
   public static final RegistryObject<SubTerraneanRiver> UNDERGROUND_RIVER = register(
      "underground_river", SubTerraneanRiver::new, NoneFeatureConfiguration.CODEC
   );
   public static final RegistryObject<TFCBambooFeature> BAMBOO = register("bamboo", TFCBambooFeature::new, ProbabilityFeatureConfiguration.CODEC);
   public static final RegistryObject<RootSpikeFeature> ROOT_SPIKE = register("root_spike", RootSpikeFeature::new, BlockStateConfiguration.CODEC);

   public TFCFFeatures() {
   }

   private static <C extends FeatureConfiguration, F extends Feature<C>> RegistryObject<F> register(String name, Function<Codec<C>, F> factory, Codec<C> codec) {
      return FEATURES.register(name, () -> factory.apply(codec));
   }

}
