package tfcflorae.common.blockentities;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.InventoryWrapper;
import net.dries007.tfc.common.capabilities.size.ItemSizeManager;
import net.dries007.tfc.common.capabilities.size.Size;
import net.dries007.tfc.common.container.ISlotCallback;
import net.dries007.tfc.common.container.PestContainer;
import net.dries007.tfc.common.container.TFCContainerTypes;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blocks.spidercave.WebbedChestBlock;

public class WebbedChestBlockEntity extends RandomizableContainerBlockEntity implements PestContainer, ISlotCallback {
   @Nullable
   private LazyOptional<IItemHandler> inventoryHandler;
   private NonNullList<ItemStack> items = NonNullList.withSize(18, ItemStack.EMPTY);
   private ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
      protected void onOpen(Level p_155062_, BlockPos p_155063_, BlockState p_155064_) {
         WebbedChestBlockEntity.playSound(p_155062_, p_155063_, p_155064_, SoundEvents.BARREL_OPEN);
         WebbedChestBlockEntity.updateBlockState(p_155062_, p_155063_, p_155064_, true);
      }

      protected void onClose(Level p_155072_, BlockPos p_155073_, BlockState p_155074_) {
         WebbedChestBlockEntity.playSound(p_155072_, p_155073_, p_155074_, SoundEvents.BARREL_CLOSE);
         WebbedChestBlockEntity.updateBlockState(p_155072_, p_155073_, p_155074_, false);
      }

      protected void openerCountChanged(Level p_155066_, BlockPos p_155067_, BlockState p_155068_, int p_155069_, int p_155070_) {
      }

      protected boolean isOwnContainer(Player p_155060_) {
         if (p_155060_.containerMenu instanceof ChestMenu) {
            Container container = ((ChestMenu)p_155060_.containerMenu).getContainer();
            return container == WebbedChestBlockEntity.this;
         } else {
            return false;
         }
      }
   };

   public static boolean isValid(ItemStack stack) {
      return ItemSizeManager.get(stack).getSize(stack).isEqualOrSmallerThan((Size)TFCConfig.SERVER.chestMaximumItemSize.get());
   }

   public WebbedChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.inventoryHandler = null;
   }

   public WebbedChestBlockEntity(BlockPos pos, BlockState state) {
      super(TFCFBlockEntities.WEBBED_CHEST.get(), pos, state);
   }

   protected void saveAdditional(CompoundTag pTag) {
      super.saveAdditional(pTag);
      if (!this.trySaveLootTable(pTag)) {
         ContainerHelper.saveAllItems(pTag, this.items);
      }
   }

   public void load(CompoundTag pTag) {
      super.load(pTag);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(pTag)) {
         ContainerHelper.loadAllItems(pTag, this.items);
      }
   }

   public int getContainerSize() {
      return 18;
   }

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> pItems) {
      this.items = pItems;
   }

   protected Component getDefaultName() {
      return Component.translatable("container.chest");
   }

   protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
      return new ChestMenu((MenuType)TFCContainerTypes.CHEST_9x2.get(), id, inventory, this, 2);
   }

   public void startOpen(Player pPlayer) {
      if (!this.remove && !pPlayer.isSpectator()) {
         this.openersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
      }
   }

   public void stopOpen(Player pPlayer) {
      if (!this.remove && !pPlayer.isSpectator()) {
         this.openersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
      }
   }

   public void recheckOpen() {
      if (!this.remove) {
         this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
      }
   }

   static void updateBlockState(Level level, BlockPos pos, BlockState pState, boolean pOpen) {
      level.setBlock(pos, (BlockState)pState.setValue(WebbedChestBlock.OPEN, pOpen), 3);
   }

   static void playSound(Level level, BlockPos pos, BlockState state, SoundEvent sound) {
      double d0 = (double)pos.getX();
      double d1 = (double)pos.getY();
      double d2 = (double)pos.getZ();
      level.playSound((Player)null, d0, d1, d2, sound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
   }

   public boolean canPlaceItem(int slot, ItemStack stack) {
      return isValid(stack);
   }

   public boolean isItemValid(int slot, ItemStack stack) {
      return isValid(stack);
   }

   public void setBlockState(BlockState state) {
      super.setBlockState(state);
      this.invalidateInventoryHandler();
   }

   public void invalidateCaps() {
      this.invalidateInventoryHandler();
   }

   private void invalidateInventoryHandler() {
      if (this.inventoryHandler != null) {
         this.inventoryHandler.invalidate();
         this.inventoryHandler = null;
      }
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
      return this.getCapability(cap, null);
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
      return cap == Capabilities.ITEM ? this.getInventoryHandler().cast() : LazyOptional.empty();
   }

   private LazyOptional<IItemHandler> getInventoryHandler() {
      if (this.inventoryHandler != null) {
         return this.inventoryHandler;
      } else {
         assert this.level != null;

         BlockState state = this.getBlockState();
         if (state.getBlock() instanceof ChestBlock chest) {
            Container chestContainer = ChestBlock.getContainer(chest, state, this.level, this.getBlockPos(), true);
            if (chestContainer == null) {
               chestContainer = this;
            }

            InventoryWrapper itemHandler = new InventoryWrapper(chestContainer, this);
            this.inventoryHandler = LazyOptional.of(() -> itemHandler);
            return this.inventoryHandler;
         } else {
            return LazyOptional.empty();
         }
      }
   }
}
