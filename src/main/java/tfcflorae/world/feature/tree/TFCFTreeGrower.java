package tfcflorae.world.feature.tree;

import net.dries007.tfc.util.registry.RegistryWood;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.resources.ResourceLocation;

public class TFCFTreeGrower extends TFCTreeGrower
{
    public final RegistryWood wood;

    public TFCFTreeGrower(RegistryWood wood, ResourceLocation normalTree, ResourceLocation oldGrowthTree)
    {
        super(normalTree, oldGrowthTree);
        this.wood = wood;
    }
}
