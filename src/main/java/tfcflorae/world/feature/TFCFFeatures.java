package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.dries007.tfc.world.feature.tree.RandomTreeConfig;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.world.feature.tree.BambooLeavesDecorator;
import tfcflorae.world.feature.tree.TemplateTreeFeature;
import tfcflorae.world.feature.tree.joshua.JoshuaTreeFeature;

/**
 * TFC Florae 的 worldgen 注册表。
 * <p>
 * 树木相关注册：{@code joshua_tree}（约书亚树，自定义地物）、{@code template_tree}（模板树）、
 * {@code bamboo_leaves}（竹叶装饰器）。普通树木仍然只用 TFC 的 {@code tfc:random_tree}，
 * 形状来自 data/tfcflorae/structures 下的结构模板。
 */
public final class TFCFFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(ForgeRegistries.FEATURES, "tfcflorae");

    public static final TFCFAbstractDeferredRegistry<TreeDecoratorType<?>> LEAF_DECOR =
        TFCFDeferredRegistry.create(Registries.TREE_DECORATOR_TYPE, "tfcflorae");

    public static final Supplier<TreeDecoratorType<BambooLeavesDecorator>> BAMBOO_LEAVES = LEAF_DECOR.register(
        "bamboo_leaves", () -> new TreeDecoratorType<>(BambooLeavesDecorator.CODEC)
    );

    public static final RegistryObject<JoshuaTreeFeature> JOSHUA_TREE =
        register("joshua_tree", JoshuaTreeFeature::new, JoshuaTreeFeature.CODEC);

    public static final RegistryObject<TemplateTreeFeature> TEMPLATE_TREE =
        register("template_tree", TemplateTreeFeature::new, RandomTreeConfig.CODEC);

    private TFCFFeatures() {}

    private static <C extends FeatureConfiguration, F extends Feature<C>> RegistryObject<F> register(
        String name, Function<Codec<C>, F> factory, Codec<C> codec
    ) {
        return FEATURES.register(name, () -> factory.apply(codec));
    }
}
