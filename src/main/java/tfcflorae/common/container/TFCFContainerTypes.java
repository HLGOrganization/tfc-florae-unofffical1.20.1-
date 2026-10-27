package tfcflorae.common.container;
import net.minecraft.resources.ResourceLocation;
import tfcflorae.common.container.TFCFAnvilPlanContainer;
import tfcflorae.common.container.TFCFAnvilContainer;
import tfcflorae.common.container.TFCFBarrelContainer;
import tfcflorae.common.blockentities.ceramics.LargeVesselBlockEntity;
import tfcflorae.common.blockentities.TFCFAnvilBlockEntity;
import tfcflorae.common.blockentities.TFCFBarrelBlockEntity;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ItemStackContainer;
import net.dries007.tfc.common.container.KnappingContainer;
import net.dries007.tfc.common.container.BlockEntityContainer.Factory;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.container.ceramics.LargeVesselContainer;
import tfcflorae.common.recipes.TFCFRecipeTypes;

public class TFCFContainerTypes {
   public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "tfcflorae");
   public static final RegistryObject<MenuType<LargeVesselContainer>> LARGE_VESSEL = TFCFContainerTypes.<LargeVesselBlockEntity, LargeVesselContainer>registerBlockEntityContainer(
      "large_vessel", TFCFBlockEntities.LARGE_VESSEL, LargeVesselContainer::create
   );
   public static final RegistryObject<MenuType<TFCFBarrelContainer>> BARREL = TFCFContainerTypes.<TFCFBarrelBlockEntity, TFCFBarrelContainer>registerBlockEntityContainer("barrel", TFCFBlockEntities.BARREL, TFCFBarrelContainer::create);
   public static final RegistryObject<MenuType<TFCFAnvilContainer>> ANVIL = TFCFContainerTypes.<TFCFAnvilBlockEntity, TFCFAnvilContainer>registerBlockEntityContainer("anvil", TFCFBlockEntities.ANVIL, TFCFAnvilContainer::create);
   public static final RegistryObject<MenuType<TFCFAnvilPlanContainer>> ANVIL_PLAN = TFCFContainerTypes.<TFCFAnvilBlockEntity, TFCFAnvilPlanContainer>registerBlockEntityContainer(
      "anvil_plan", TFCFBlockEntities.ANVIL, TFCFAnvilPlanContainer::create
   );
   // 打制界面统一为一个 MenuType，与 TFC 的做法一致（TFC 也只有 tfc:knapping 一个菜单）。
   // 四种材质靠 KnappingType 区分，而 KnappingType 是数据驱动的：
   //   data/tfcflorae/tfc/knapping_types/{earthenware,stoneware}_clay_knapping.json
   //   data/tfcflorae/tfc/knapping_types/flint_knapping.json
   public static final RegistryObject<MenuType<KnappingContainer>> KNAPPING = registerItem(
      "knapping", TFCFContainerTypes::createKnapping
   );

   public TFCFContainerTypes() {
   }

   /**
    * 打开打制界面。与 TFC 原生一致：菜单类型固定，具体用哪个 {@link net.dries007.tfc.util.KnappingType}
    * 由玩家手上的物品决定（TFC 的 KnappingType.get(Player) 就是这么做的）。
    */
   public static KnappingContainer createKnapping(ItemStack stack, InteractionHand hand, int slot, Inventory playerInventory, int windowId) {
      return (KnappingContainer)new KnappingContainer(
            (MenuType)KNAPPING.get(),
            net.dries007.tfc.util.KnappingType.get(playerInventory.player),
            windowId,
            playerInventory,
            stack,
            hand,
            slot
         )
         .init(playerInventory, 20);
   }

   private static <T extends InventoryBlockEntity<?>, C extends BlockEntityContainer<T>> RegistryObject<MenuType<C>> registerBlockEntityContainer(
      String name, Supplier<BlockEntityType<T>> type, Factory<T, C> factory
   ) {
      return RegistrationHelpers.registerBlockEntityContainer(CONTAINERS, name, type, factory);
   }

   private static <C extends ItemStackContainer> RegistryObject<MenuType<C>> registerItem(
      String name, net.dries007.tfc.common.container.ItemStackContainer.Factory<C> factory
   ) {
      return RegistrationHelpers.registerItemStackContainer(CONTAINERS, name, factory);
   }

   private static <C extends AbstractContainerMenu> RegistryObject<MenuType<C>> register(String name, IContainerFactory<C> factory) {
      return RegistrationHelpers.registerContainer(CONTAINERS, name, factory);
   }
}
