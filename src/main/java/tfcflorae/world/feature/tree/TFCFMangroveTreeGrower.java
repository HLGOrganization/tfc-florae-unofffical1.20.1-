package tfcflorae.world.feature.tree;

import java.util.Locale;

import net.minecraft.resources.ResourceLocation;

import net.dries007.tfc.world.feature.tree.TFCTreeGrower;

import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.util.TFCFHelpers;

/**
 * Mangrove propagule grower.
 * <p>
 * The original 1.18.2 implementation assembled the rooted-tree feature in code. 1.19
 * moved worldgen features to datapacks, so (as with TFC 3.x) the grower only holds the
 * configured-feature locations and the shape lives in {@code data/tfcflorae/worldgen/}.
 */
public class TFCFMangroveTreeGrower extends TFCTreeGrower
{
    public final TFCFWood wood;
    public final float tallChance;

    public TFCFMangroveTreeGrower(TFCFWood wood, float tallChance)
    {
        // tfcflorae: 命名空间（Helpers.identifier 会生成 tfc:，导致生长时找不到特性而崩服）
        this(wood, tallChance,
            TFCFHelpers.identifier("tree/" + wood.getSerializedName().toLowerCase(Locale.ROOT)),
            TFCFHelpers.identifier("tree/" + wood.getSerializedName().toLowerCase(Locale.ROOT) + "_tall"));
    }

    public TFCFMangroveTreeGrower(TFCFWood wood, float tallChance, ResourceLocation normalTree, ResourceLocation tallTree)
    {
        super(normalTree, tallTree);
        this.wood = wood;
        this.tallChance = tallChance;
    }
}
