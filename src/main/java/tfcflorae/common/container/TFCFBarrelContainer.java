package tfcflorae.common.container;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFBarrelBlockEntity;
import tfcflorae.common.blocks.devices.TFCFBarrelBlock;

public class TFCFBarrelContainer extends BlockEntityContainer<TFCFBarrelBlockEntity> implements ButtonHandlerContainer {
   public static TFCFBarrelContainer create(TFCFBarrelBlockEntity barrel, Inventory playerInv, int windowId) {
      return (TFCFBarrelContainer)new TFCFBarrelContainer(windowId, barrel).init(playerInv, 12);
   }

   private TFCFBarrelContainer(int windowId, TFCFBarrelBlockEntity barrel) {
      super((MenuType)TFCFContainerTypes.BARREL.get(), windowId, barrel);
   }

   public void onButtonPress(int buttonID, @Nullable CompoundTag extraNBT) {
      Level level = ((TFCFBarrelBlockEntity)this.blockEntity).getLevel();
      if (level != null) {
         TFCFBarrelBlock.toggleSeal(level, ((TFCFBarrelBlockEntity)this.blockEntity).getBlockPos(), ((TFCFBarrelBlockEntity)this.blockEntity).getBlockState());
      }
   }

   protected void addContainerSlots() {
      ((TFCFBarrelBlockEntity)this.blockEntity).getCapability(Capabilities.ITEM).ifPresent(inventory -> {
         this.addSlot(new CallbackSlot(this.blockEntity, inventory, 0, 35, 20));
         this.addSlot(new CallbackSlot(this.blockEntity, inventory, 1, 35, 54));
         this.addSlot(new CallbackSlot(this.blockEntity, inventory, 2, 89, 37));
      });
   }

   protected boolean moveStack(ItemStack stack, int slotIndex) {
      if ((Boolean)((TFCFBarrelBlockEntity)this.blockEntity).getBlockState().getValue(TFCFBarrelBlock.SEALED)) {
         return true;
      } else {
         int containerSlot = stack.getCapability(Capabilities.FLUID_ITEM).isPresent()
               && stack.getCapability(HeatCapability.CAPABILITY).map(cap -> cap.getTemperature() == 0.0F).orElse(false)
            ? 0
            : 2;

         return switch (this.typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> !this.moveItemStackTo(stack, containerSlot, containerSlot + 1, false);
            case CONTAINER -> !this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), false);
            default -> throw new IncompatibleClassChangeError();
         };
      }
   }
}
