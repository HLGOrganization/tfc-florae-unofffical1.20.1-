package tfcflorae.common.container;

import net.dries007.tfc.common.container.ItemStackContainerProvider;

public class TFCFContainerProviders {
   /**
    * 打制界面的唯一入口。与 TFC 原生一致：菜单类型固定为 tfcflorae:knapping，
    * 具体 KnappingType 由玩家手上的物品决定（见 {@link TFCFContainerTypes#createKnapping}）。
    */
   public static final ItemStackContainerProvider KNAPPING = new ItemStackContainerProvider(TFCFContainerTypes::createKnapping);

   public TFCFContainerProviders() {
   }
}
