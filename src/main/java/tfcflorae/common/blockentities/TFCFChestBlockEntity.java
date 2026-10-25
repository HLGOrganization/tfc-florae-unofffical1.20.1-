package tfcflorae.common.blockentities;

import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.container.RestrictedChestContainer;
import net.dries007.tfc.common.container.TFCContainerTypes;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TFCFChestBlockEntity extends TFCChestBlockEntity {
   public static boolean isValid(ItemStack stack) {
      return ItemSizeManager.get(stack).getSize(stack).isEqualOrSmallerThan((Size)TFCConfig.SERVER.chestMaximumItemSize.get());
   }

   public TFCFChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
   }

   public TFCFChestBlockEntity(BlockPos pos, BlockState state) {
      super(TFCFBlockEntities.CHEST.get(), pos, state);
   }

   public int getContainerSize() {
      return 18;
   }

   protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
      return new RestrictedChestContainer((MenuType)TFCContainerTypes.CHEST_9x2.get(), id, inventory, this, 2);
   }

   public boolean canPlaceItem(int slot, ItemStack stack) {
      return isValid(stack);
   }
}
