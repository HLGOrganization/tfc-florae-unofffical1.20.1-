package tfcflorae.common;
import net.minecraft.core.registries.Registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import tfcflorae.TFCFlorae;

/**
 * Fills TFC Florae's creative tabs by walking the item registry.
 * <p>
 * Splitting is intentionally coarse (block items vs. everything else vs. plants) because the
 * 1.20.1 tab system no longer records a tab per item; refining this into TFC's tab taxonomy is
 * part of the deferred world-gen/content polish pass.
 */
final class CreativeTabFiller
{
    static void fill(int group, CreativeModeTab.Output out)
    {
        for (Item item : BuiltInRegistries.ITEM)
        {
            if (item == Items.AIR)
            {
                continue;
            }
            final ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!TFCFlorae.MOD_ID.equals(id.getNamespace()))
            {
                continue;
            }
            final String path = id.getPath();
            final boolean isBlock = item instanceof BlockItem;
            final boolean looksLikePlant = path.startsWith("plant/")
                || path.contains("sapling")
                || path.contains("leaves")
                || path.contains("flower")
                || path.contains("grass")
                || path.contains("vine")
                || path.contains("cactus")
                || path.contains("bamboo")
                || path.contains("kelp")
                || path.contains("seagrass")
                || path.contains("moss")
                || path.contains("fern")
                || path.contains("bush");

            final boolean want = switch (group)
            {
                case 0 -> isBlock && !looksLikePlant;
                case 1 -> !isBlock;
                default -> isBlock && looksLikePlant;
            };
            if (want)
            {
                out.accept(new ItemStack(item));
            }
        }
    }

    private CreativeTabFiller() {}
}
