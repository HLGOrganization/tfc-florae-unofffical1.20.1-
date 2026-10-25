package tfcflorae.common.blockentities.ceramics;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTraits;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.capabilities.size.Size;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blocks.ceramics.LargeVesselBlock;
import tfcflorae.common.container.ceramics.LargeVesselContainer;
import tfcflorae.util.TFCFHelpers;

public class LargeVesselBlockEntity extends InventoryBlockEntity<LargeVesselBlockEntity.VesselInventory> {
   public static final int SLOTS = 9;
   private static final Component NAME = TFCFHelpers.translatable("tfcflorae.block_entity.large_vessel");

   public LargeVesselBlockEntity(BlockPos pos, BlockState state) {
      super(TFCFBlockEntities.LARGE_VESSEL.get(), pos, state, LargeVesselBlockEntity.VesselInventory::new, NAME);
   }

   public LargeVesselBlockEntity(BlockEntityType<? extends LargeVesselBlockEntity> type, BlockPos pos, BlockState state) {
      super(type, pos, state, LargeVesselBlockEntity.VesselInventory::new, NAME);
   }

   @Nullable
   public AbstractContainerMenu createMenu(int windowID, Inventory inv, Player player) {
      return LargeVesselContainer.create(this, inv, windowID);
   }

   public void onUnseal() {
      for (int i = 0; i < 9; i++) {
         ((LargeVesselBlockEntity.VesselInventory)this.inventory)
            .setStackInSlot(
               i, FoodCapability.removeTrait(((LargeVesselBlockEntity.VesselInventory)this.inventory).getStackInSlot(i).copy(), FoodTraits.PRESERVED)
            );
      }
   }

   public void onSeal() {
      for (int i = 0; i < 9; i++) {
         ((LargeVesselBlockEntity.VesselInventory)this.inventory)
            .setStackInSlot(
               i, FoodCapability.applyTrait(((LargeVesselBlockEntity.VesselInventory)this.inventory).getStackInSlot(i).copy(), FoodTraits.PRESERVED)
            );
      }
   }

   public void loadAdditional(CompoundTag nbt) {
      super.loadAdditional(nbt);
   }

   public void saveAdditional(CompoundTag nbt) {
      super.saveAdditional(nbt);
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
      return super.getCapability(cap, side);
   }

   public void clearContent() {
      super.clearContent();
   }

   public void ejectInventory() {
      super.ejectInventory();
   }

   public static class VesselInventory extends InventoryItemHandler implements INBTSerializable<CompoundTag> {
      private final LargeVesselBlockEntity vessel;

      VesselInventory(InventoryBlockEntity<?> entity) {
         super(entity, 9);
         this.vessel = (LargeVesselBlockEntity)entity;
      }

      @NotNull
      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
         return this.canModify() ? super.insertItem(slot, stack, simulate) : stack;
      }

      @NotNull
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
         return this.canModify() ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
      }

      public boolean isItemValid(int slot, ItemStack stack) {
         return this.canModify() && ItemSizeManager.get(stack).getSize(stack).isSmallerThan(Size.LARGE) && super.isItemValid(slot, stack);
      }

      private boolean canModify() {
         return !(Boolean)this.vessel.getBlockState().getValue(LargeVesselBlock.SEALED);
      }
   }
}
