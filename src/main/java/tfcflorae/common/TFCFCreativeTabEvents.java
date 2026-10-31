package tfcflorae.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.plant.TFCFFruitTrees;
import tfcflorae.common.blocks.wood.TFCFWood;

/**
 * 把果树整套放进 TFC 自己的「木质」创造页。
 *
 * <p>为什么不能靠 {@code TFCCreativeTabs.CreativeTabHolder}：TFC 3.x 的那个 holder 是个
 * record {@code (RegistryObject<CreativeModeTab> tab, DisplayItemsGenerator generator)}，
 * 标签页内容由 {@code generator} 显式列举，附属把 holder 传进去没有任何效果
 * （{@code TFCFBlocks.register(..., CreativeTabHolder)} 那个重载本来也只是吞掉参数）。
 * 1.20.1 里唯一正确的做法是监听 {@link BuildCreativeModeTabContentsEvent}。
 *
 * <p>本模组自己的三个创造页由 {@link CreativeTabFiller} 遍历注册表填充，果树苗因为 id 以
 * {@code plant/} 开头会落到「植被」页；但用户预期是在 TFC 的木质页里、和 TFC 自带的
 * 樱桃树苗并排，所以这里额外往 {@code tfc:wood} 追加一份。
 * 同一个物品出现在两个标签页是正常的，不冲突。
 */
public final class TFCFCreativeTabEvents
{
    /** TFC 木质创造页的注册名：{@code tfc:wood}。 */
    private static final ResourceLocation TFC_WOOD_TAB = new ResourceLocation("tfc", "wood");

    public static void onBuildContents(BuildCreativeModeTabContentsEvent event)
    {
        if (!TFC_WOOD_TAB.equals(event.getTabKey().location()))
        {
            return;
        }

        for (TFCFWood wood : TFCFFruitTrees.TREES)
        {
            // 枝干（plant/<tree>_branch、_growing_branch）没有对应物品，只放树苗与树叶。
            event.accept(new ItemStack(TFCFBlocks.FRUIT_TREE_SAPLINGS.get(wood).get()));
            event.accept(new ItemStack(TFCFBlocks.FRUIT_TREE_LEAVES.get(wood).get()));
        }
    }

    private TFCFCreativeTabEvents()
    {
    }
}
