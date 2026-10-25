package tfcflorae.world.feature.tree;

import java.util.Locale;

import net.minecraft.resources.ResourceLocation;

import net.dries007.tfc.world.feature.tree.TFCTreeGrower;

import tfcflorae.common.blocks.plant.TFCFPlant;
import tfcflorae.util.TFCFHelpers;

/**
 * Tree grower for TFC Florae's decorative bamboo plant blocks.
 * <p>
 * Follows the TFC 3.x pattern: the grower stores the configured-feature locations
 * (datapack) instead of building a {@code ConfiguredFeature} in code.
 */
public class TFCFBambooTreeGrower extends TFCTreeGrower
{
    public final TFCFPlant bamboo;

    public TFCFBambooTreeGrower(TFCFPlant bamboo)
    {
        // tfcflorae: 命名空间（Helpers.identifier 会生成 tfc:）
        this(bamboo,
            TFCFHelpers.identifier("tree/grower/" + bamboo.name().toLowerCase(Locale.ROOT)),
            TFCFHelpers.identifier("tree/grower/" + bamboo.name().toLowerCase(Locale.ROOT) + "_large"));
    }

    public TFCFBambooTreeGrower(TFCFPlant bamboo, ResourceLocation normalTree, ResourceLocation oldGrowthTree)
    {
        super(normalTree, oldGrowthTree);
        this.bamboo = bamboo;
    }
}
