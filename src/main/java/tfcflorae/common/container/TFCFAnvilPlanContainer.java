package tfcflorae.common.container;

import net.dries007.tfc.common.container.BlockEntityContainer;
import net.dries007.tfc.common.container.ButtonHandlerContainer;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.util.Helpers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFAnvilBlockEntity;

public class TFCFAnvilPlanContainer extends BlockEntityContainer<TFCFAnvilBlockEntity> implements ButtonHandlerContainer {
   public static TFCFAnvilPlanContainer create(TFCFAnvilBlockEntity anvil, Inventory playerInventory, int windowId) {
      return (TFCFAnvilPlanContainer)new TFCFAnvilPlanContainer(windowId, anvil).init(playerInventory, 0);
   }

   protected TFCFAnvilPlanContainer(int windowId, TFCFAnvilBlockEntity anvil) {
      super((MenuType)TFCFContainerTypes.ANVIL_PLAN.get(), windowId, anvil);
   }

   public void onButtonPress(int buttonID, @Nullable CompoundTag extraNBT) {
      if (extraNBT != null && this.player != null) {
         ResourceLocation recipeId = new ResourceLocation(extraNBT.getString("recipe"));
         AnvilRecipe recipe = (AnvilRecipe)Helpers.getRecipes(this.player.level(), TFCRecipeTypes.ANVIL).get(recipeId);
         ((TFCFAnvilBlockEntity)this.blockEntity).chooseRecipe(recipe);
         if (this.player instanceof ServerPlayer serverPlayer) {
            Helpers.openScreen(serverPlayer, ((TFCFAnvilBlockEntity)this.blockEntity).anvilProvider(), ((TFCFAnvilBlockEntity)this.blockEntity).getBlockPos());
         }
      }
   }
}
