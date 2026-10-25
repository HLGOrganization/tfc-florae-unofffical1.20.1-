package tfcflorae.common.items;

import net.dries007.tfc.common.blockentities.BarrelInventoryCallback;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.DelegateFluidHandler;
import net.dries007.tfc.common.capabilities.FluidTankCallback;
import net.dries007.tfc.common.container.ISlotCallback;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.items.BarrelBlockItem;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFBarrelBlockEntity;

public class TFCFBarrelBlockItem extends BarrelBlockItem {
   public TFCFBarrelBlockItem(Block block, Properties properties) {
      super(block, properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      InteractionResult result = this.tryInteractWithFluid(level, player, hand);
      return result != InteractionResult.PASS ? new InteractionResultHolder(result, player.getItemInHand(hand)) : super.use(level, player, hand);
   }

   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      if (player != null) {
         InteractionResult result = this.tryInteractWithFluid(context.getLevel(), player, context.getHand());
         if (result != InteractionResult.PASS) {
            return result;
         }
      }

      return super.useOn(context);
   }

   @Nullable
   public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
      return new TFCFBarrelBlockItem.BarrelItemStackInventory(stack);
   }

   private InteractionResult tryInteractWithFluid(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      IFluidHandler handler = (IFluidHandler)Helpers.getCapability(stack, Capabilities.FLUID_ITEM);
      if (handler == null) {
         return InteractionResult.PASS;
      } else {
         BlockHitResult hit = Helpers.rayTracePlayer(level, player, Fluid.SOURCE_ONLY);
         return FluidHelpers.transferBetweenWorldAndItem(stack, level, hit, player, hand, false, false, true)
            ? InteractionResult.SUCCESS
            : InteractionResult.PASS;
      }
   }

   private static class BarrelItemStackInventory
      implements ICapabilityProvider,
      DelegateFluidHandler,
      IFluidHandlerItem,
      ISlotCallback,
      FluidTankCallback,
      BarrelInventoryCallback {
      private final LazyOptional<TFCFBarrelBlockItem.BarrelItemStackInventory> capability = LazyOptional.of(() -> this);
      private final ItemStack stack;
      private final TFCFBarrelBlockEntity.BarrelInventory inventory;
      private boolean hasActiveRecipe;

      BarrelItemStackInventory(ItemStack stack) {
         this.stack = stack;
         this.inventory = new TFCFBarrelBlockEntity.BarrelInventory(this);
         this.hasActiveRecipe = false;
         this.load();
      }

      public boolean canModify() {
         return this.stack.getTag() == null || !this.hasActiveRecipe;
      }

      public void fluidTankChanged() {
         this.save();
      }

      public IFluidHandler getFluidHandler() {
         return this.inventory;
      }

      @NotNull
      public ItemStack getContainer() {
         return this.stack.copy();
      }

      @NotNull
      public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
         return cap != Capabilities.FLUID_ITEM && cap != Capabilities.FLUID ? LazyOptional.empty() : this.capability.cast();
      }

      private void load() {
         CompoundTag tag = this.stack.getTag();
         if (tag != null && tag.contains("BlockEntityTag", 10)) {
            CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
            this.hasActiveRecipe = blockEntityTag.contains("recipe", 8);
            this.inventory.deserializeNBT(blockEntityTag.getCompound("inventory"));
         }
      }

      private void save() {
         if (this.inventory.isInventoryEmpty()) {
            this.stack.removeTagKey("BlockEntityTag");
         } else {
            this.stack.getOrCreateTagElement("BlockEntityTag").put("inventory", this.inventory.serializeNBT());
         }
      }
   }
}
