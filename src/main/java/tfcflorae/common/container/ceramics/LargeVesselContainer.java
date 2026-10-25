package tfcflorae.common.container.ceramics;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.container.PestContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.ceramics.LargeVesselBlockEntity;
import tfcflorae.common.blocks.ceramics.LargeVesselBlock;
import tfcflorae.common.container.TFCFContainerTypes;

public class LargeVesselContainer extends BlockEntityContainer<LargeVesselBlockEntity> implements ButtonHandlerContainer, PestContainer {
   public static LargeVesselContainer create(LargeVesselBlockEntity vessel, Inventory playerInventory, int windowId) {
      return (LargeVesselContainer)new LargeVesselContainer(vessel, windowId).init(playerInventory);
   }

   public LargeVesselContainer(LargeVesselBlockEntity vessel, int windowId) {
      super((MenuType)TFCFContainerTypes.LARGE_VESSEL.get(), windowId, vessel);
   }

   public void onButtonPress(int buttonID, @Nullable CompoundTag extraNBT) {
      Level level = ((LargeVesselBlockEntity)this.blockEntity).getLevel();
      if (level != null) {
         LargeVesselBlock.toggleSeal(
            level,
            ((LargeVesselBlockEntity)this.blockEntity).getBlockPos(),
            ((LargeVesselBlockEntity)this.blockEntity).getBlockState(),
            (net.minecraft.world.level.block.entity.BlockEntityType<LargeVesselBlockEntity>)((LargeVesselBlockEntity)this.blockEntity).getType()
         );
      }
   }

   protected boolean moveStack(ItemStack stack, int slotIndex) {
      if ((Boolean)((LargeVesselBlockEntity)this.blockEntity).getBlockState().getValue(LargeVesselBlock.SEALED)) {
         return true;
      } else {
         return switch (this.typeOf(slotIndex)) {
            case MAIN_INVENTORY, HOTBAR -> !this.moveItemStackTo(stack, 0, 9, false);
            case CONTAINER -> !this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), false);
            default -> throw new IncompatibleClassChangeError();
         };
      }
   }

   protected void addContainerSlots() {
      ((LargeVesselBlockEntity)this.blockEntity).getCapability(Capabilities.ITEM).ifPresent(handler -> {
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 0, 62, 19));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 1, 80, 19));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 2, 98, 19));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 3, 62, 37));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 4, 80, 37));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 5, 98, 37));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 6, 62, 55));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 7, 80, 55));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 8, 98, 55));
      });
   }

   public boolean canBeInfested() {
      return !this.isSealed();
   }

   public boolean isSealed() {
      return ((LargeVesselBlockEntity)this.blockEntity).getBlockState().hasProperty(LargeVesselBlock.SEALED)
         && (Boolean)((LargeVesselBlockEntity)this.blockEntity).getBlockState().getValue(LargeVesselBlock.SEALED);
   }
}
