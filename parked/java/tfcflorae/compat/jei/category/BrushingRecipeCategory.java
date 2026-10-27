package tfcflorae.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.dries007.tfc.compat.jei.category.BaseRecipeCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.common.recipes.BrushingRecipe;

public class BrushingRecipeCategory extends BaseRecipeCategory<BrushingRecipe> {
   public BrushingRecipeCategory(RecipeType<BrushingRecipe> type, IGuiHelper helper) {
      super(type, helper, helper.createBlankDrawable(118, 26), new ItemStack((ItemLike)TFCFItems.BRUSHES.get()));
   }

   public void setRecipe(IRecipeLayoutBuilder builder, BrushingRecipe recipe, IFocusGroup focuses) {
      Ingredient chiselIngredient = recipe.getItemIngredient();
      if (chiselIngredient == null) {
         chiselIngredient = Ingredient.of(TFCFTags.Items.BRUSHES);
      }

      ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.INPUT, 6, 5).addIngredients(collapse(recipe.getBlockIngredient())))
         .setBackground(this.slot, -1, -1);
      ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.INPUT, 26, 5).addIngredients(chiselIngredient)).setBackground(this.slot, -1, -1);
      ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.OUTPUT, 76, 5).addItemStack(new ItemStack(recipe.getBlockRecipeOutput())))
         .setBackground(this.slot, -1, -1);
      ((IRecipeSlotBuilder)builder.addSlot(RecipeIngredientRole.OUTPUT, 96, 5).addItemStack(recipe.getExtraDrop(ItemStack.EMPTY)))
         .setBackground(this.slot, -1, -1);
   }
}
