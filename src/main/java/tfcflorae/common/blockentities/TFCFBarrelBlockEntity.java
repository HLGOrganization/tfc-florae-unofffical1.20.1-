package tfcflorae.common.blockentities;

import java.util.ArrayList;
import java.util.List;
import net.dries007.tfc.common.TFCTags.Fluids;
import net.dries007.tfc.common.blockentities.BarrelBlockEntity;
import net.dries007.tfc.common.blockentities.BarrelInventoryCallback;
import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import net.dries007.tfc.common.capabilities.DelegateFluidHandler;
import net.dries007.tfc.common.capabilities.DelegateItemHandler;
import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.dries007.tfc.common.capabilities.InventoryFluidTank;
import net.dries007.tfc.common.capabilities.InventoryItemHandler;
import net.dries007.tfc.common.recipes.inventory.EmptyInventory;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.container.TFCFBarrelContainer;
import tfcflorae.util.TFCFHelpers;

public class TFCFBarrelBlockEntity extends BarrelBlockEntity {
   private static final Component NAME = TFCFHelpers.translatable("tfcflorae.block_entity.barrel");

   public TFCFBarrelBlockEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   protected TFCFBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(pos, state);
   }

   public BlockEntityType<?> getType() {
      return (BlockEntityType<?>)TFCFBlockEntities.BARREL.get();
   }

   @Nullable
   public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
      return TFCFBarrelContainer.create(this, player.getInventory(), containerId);
   }

   public static class BarrelInventory implements DelegateItemHandler, DelegateFluidHandler, INBTSerializable<CompoundTag>, EmptyInventory, FluidTankCallback {
      private final BarrelInventoryCallback callback;
      private final InventoryItemHandler inventory;
      private final List<ItemStack> excess;
      private final InventoryFluidTank tank;
      private boolean mutable;

      BarrelInventory(InventoryBlockEntity<?> entity) {
         this((BarrelInventoryCallback)entity);
      }

      public BarrelInventory(BarrelInventoryCallback callback) {
         this.callback = callback;
         this.inventory = new InventoryItemHandler(callback, 3);
         this.excess = new ArrayList<>();
         this.tank = new InventoryFluidTank(
            (Integer)TFCConfig.SERVER.barrelCapacity.get(), stack -> Helpers.isFluid(stack.getFluid(), Fluids.USABLE_IN_BARREL), this
         );
      }

      public void whileMutable(Runnable action) {
         try {
            this.mutable = true;
            action.run();
         } finally {
            this.mutable = false;
         }
      }

      public boolean isInventoryEmpty() {
         return this.tank.getFluid().isEmpty() && this.excess.isEmpty() && Helpers.isEmpty(this.inventory);
      }

      public void insertItemWithOverflow(ItemStack stack) {
         ItemStack remainder = this.inventory.insertItem(2, stack, false);
         if (!remainder.isEmpty()) {
            this.excess.add(remainder);
         }
      }

      public IItemHandlerModifiable getItemHandler() {
         return this.inventory;
      }

      public IFluidHandler getFluidHandler() {
         return this.tank;
      }

      public int fill(FluidStack resource, FluidAction action) {
         return this.canModify() ? this.tank.fill(resource, action) : 0;
      }

      @NotNull
      public FluidStack drain(FluidStack resource, FluidAction action) {
         return this.canModify() ? this.tank.drain(resource, action) : FluidStack.EMPTY;
      }

      @NotNull
      public FluidStack drain(int maxDrain, FluidAction action) {
         return this.canModify() ? this.tank.drain(maxDrain, action) : FluidStack.EMPTY;
      }

      @NotNull
      public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
         return this.canModify() ? this.inventory.insertItem(slot, stack, simulate) : stack;
      }

      @NotNull
      public ItemStack extractItem(int slot, int amount, boolean simulate) {
         return this.canModify() ? this.inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
      }

      public boolean isItemValid(int slot, ItemStack stack) {
         return this.canModify();
      }

      public CompoundTag serializeNBT() {
         CompoundTag nbt = new CompoundTag();
         nbt.put("inventory", this.inventory.serializeNBT());
         nbt.put("tank", this.tank.writeToNBT(new CompoundTag()));
         if (!this.excess.isEmpty()) {
            ListTag excessNbt = new ListTag();

            for (ItemStack stack : this.excess) {
               excessNbt.add(stack.save(new CompoundTag()));
            }

            nbt.put("excess", excessNbt);
         }

         return nbt;
      }

      public void deserializeNBT(CompoundTag nbt) {
         this.inventory.deserializeNBT(nbt.getCompound("inventory"));
         this.tank.readFromNBT(nbt.getCompound("tank"));
         this.excess.clear();
         if (nbt.contains("excess")) {
            ListTag excessNbt = nbt.getList("excess", 10);

            for (int i = 0; i < excessNbt.size(); i++) {
               this.excess.add(ItemStack.of(excessNbt.getCompound(i)));
            }
         }
      }

      public void fluidTankChanged() {
         this.callback.fluidTankChanged();
      }

      private boolean canModify() {
         return this.mutable || this.callback.canModify();
      }
   }
}
