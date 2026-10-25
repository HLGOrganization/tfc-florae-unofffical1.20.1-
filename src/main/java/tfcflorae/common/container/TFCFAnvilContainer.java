package tfcflorae.common.container;

import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.Forging;
import net.dries007.tfc.common.capabilities.forge.ForgingCapability;
import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.container.CallbackSlot;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.util.Helpers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFAnvilBlockEntity;

public class TFCFAnvilContainer extends BlockEntityContainer<TFCFAnvilBlockEntity> implements ButtonHandlerContainer {
   public static final int PLAN_ID = 8;

   public static TFCFAnvilContainer create(TFCFAnvilBlockEntity anvil, Inventory playerInv, int windowId) {
      return (TFCFAnvilContainer)new TFCFAnvilContainer(windowId, anvil).init(playerInv, 41);
   }

   protected TFCFAnvilContainer(int windowId, TFCFAnvilBlockEntity anvil) {
      super((MenuType)TFCFContainerTypes.ANVIL.get(), windowId, anvil);
   }

   public void onButtonPress(int buttonID, @Nullable CompoundTag extraNBT) {
      if (buttonID == 8) {
         Level level = ((TFCFAnvilBlockEntity)this.blockEntity).getLevel();
         if (level != null) {
            ItemStack stack = this.getSlot(0).getItem();
            if (AnvilRecipe.hasAny(level, stack, ((TFCFAnvilBlockEntity)this.blockEntity).getTier()) && this.player instanceof ServerPlayer serverPlayer) {
               Helpers.openScreen(serverPlayer, ((TFCFAnvilBlockEntity)this.blockEntity).planProvider(), ((TFCFAnvilBlockEntity)this.blockEntity).getBlockPos());
            }
         }
      } else {
         ForgeStep step = ForgeStep.valueOf(buttonID);
         if (this.player instanceof ServerPlayer serverPlayer && step != null) {
            ((TFCFAnvilBlockEntity)this.blockEntity).work(serverPlayer, step);
         }
      }
   }

   protected void addContainerSlots() {
      ((TFCFAnvilBlockEntity)this.blockEntity).getCapability(Capabilities.ITEM).ifPresent(handler -> {
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 0, 31, 68));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 1, 13, 68));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 2, 129, 68));
         this.addSlot(new CallbackSlot(this.blockEntity, handler, 3, 147, 68));
      });
   }

   protected boolean moveStack(ItemStack stack, int slotIndex) {
      return switch (this.typeOf(slotIndex)) {
         case MAIN_INVENTORY, HOTBAR -> !this.moveItemStackTo(stack, 2, 4, false) && !this.moveItemStackTo(stack, 0, 2, false);
         case CONTAINER -> {
            Level level = ((TFCFAnvilBlockEntity)this.blockEntity).getLevel();
            Forging forge = ForgingCapability.get(stack);
            AnvilRecipe recipe = null;
            int target = -1;
            if (forge != null && level != null) {
               recipe = forge.getRecipe(level);
               target = forge.getWorkTarget();
               forge.clearRecipeIfNotWorked();
            }

            boolean result = !this.moveItemStackTo(stack, this.containerSlots, this.slots.size(), false);
            if (!stack.isEmpty() && recipe != null) {
               forge.setRecipe(recipe, target);
            }

            yield result;
         }
         default -> throw new IncompatibleClassChangeError();
      };
   }
}
