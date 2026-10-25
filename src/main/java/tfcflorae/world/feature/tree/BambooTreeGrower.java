package tfcflorae.world.feature.tree;
import net.minecraft.core.registries.Registries;

import net.minecraft.resources.ResourceLocation;

import net.dries007.tfc.world.feature.tree.TFCTreeGrower;

import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.util.TFCFHelpers;

/**
 * Bamboo tree grower for TFC Florae's wood set.
 * <p>
 * In 1.18.2 the tree shape was assembled in code and pushed into
 * {@code BuiltinRegistries.CONFIGURED_FEATURE}. 1.19 moved worldgen features into
 * datapacks, so like TFC 3.x this grower only carries the {@link ResourceLocation}s
 * of the configured features, which live under {@code data/tfcflorae/worldgen/}.
 */
public class BambooTreeGrower extends TFCTreeGrower
{
    public final TFCFWood bamboo;

    public BambooTreeGrower(TFCFWood bamboo)
    {
        // 必须用 tfcflorae: 命名空间；Helpers.identifier() 是 TFC 的工具类，默认返回 tfc:，
        // 会导致 "Missing tree feature: tfc:tree/bamboo" 并在 randomTick 里抛异常崩服。
        this(bamboo, TFCFHelpers.identifier("tree/bamboo"), TFCFHelpers.identifier("tree/bamboo_large"));
    }

    public BambooTreeGrower(TFCFWood bamboo, ResourceLocation normalTree, ResourceLocation oldGrowthTree)
    {
        super(normalTree, oldGrowthTree);
        this.bamboo = bamboo;
    }
}
