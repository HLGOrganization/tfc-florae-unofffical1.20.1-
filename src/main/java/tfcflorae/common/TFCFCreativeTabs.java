package tfcflorae.common;

import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import tfcflorae.TFCFlorae;

/**
 * Creative tabs for TFC Florae.
 * <p>
 * Minecraft 1.19.3 removed {@code Item.Properties#tab(CreativeModeTab)}; content is now grouped by
 * a {@link CreativeModeTab.DisplayItemsGenerator} instead. TFC 3.x likewise replaced its old
 * {@code TFCItemGroup} constants with {@code TFCCreativeTabs.CreativeTabHolder} records, so an addon can
 * no longer hand its items to a TFC tab at item-construction time.
 * <p>
 * For this port the mod owns three tabs and enumerates its own registry, which keeps the registration
 * call sites unchanged and avoids reaching into TFC's tab internals.
 */
public final class TFCFCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TFCFlorae.MOD_ID);

    public static final RegistryObject<CreativeModeTab> BLOCKS = register("blocks", () -> new ItemStack(Items.STONE), 0);
    public static final RegistryObject<CreativeModeTab> ITEMS = register("items", () -> new ItemStack(Items.IRON_INGOT), 1);
    public static final RegistryObject<CreativeModeTab> FLORA = register("flora", () -> new ItemStack(Items.FERN), 2);

    /**
     * Compatibility shim: the 1.18.2 sources assigned a creative tab per item. That is no longer
     * possible, so the tab argument is accepted and discarded -- the tabs above enumerate the
     * mod's registries directly.
     */
    public static void noteTab(Object ignored)
    {
        // no-op
    }

    private static RegistryObject<CreativeModeTab> register(String name, Supplier<ItemStack> icon, int group)
    {
        return TABS.register(name, () -> CreativeModeTab.builder()
            .icon(icon)
            .title(Component.translatable("tfcflorae.creative_tab." + name))
            .displayItems((params, out) -> CreativeTabFiller.fill(group, out))
            .build());
    }

    private TFCFCreativeTabs() {}
}
