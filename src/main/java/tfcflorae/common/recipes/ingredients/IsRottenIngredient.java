package tfcflorae.common.recipes.ingredients;

import com.google.gson.JsonObject;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.recipes.ingredients.DelegateIngredient;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.JsonHelpers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.Nullable;

public class IsRottenIngredient extends DelegateIngredient {
   public static IsRottenIngredient of(Ingredient ingredient) {
      return new IsRottenIngredient(ingredient);
   }

   protected IsRottenIngredient(@Nullable Ingredient delegate) {
      super(delegate);
   }

   public boolean test(@Nullable ItemStack stack) {
      return super.test(stack) && stack != null && stack.getCapability(FoodCapability.CAPABILITY).map(cap -> cap.isRotten()).orElse(false);
   }

   public IIngredientSerializer<? extends DelegateIngredient> getSerializer() {
      return IsRottenIngredient.Serializer.INSTANCE;
   }

   @Nullable
   protected ItemStack testDefaultItem(ItemStack stack) {
      return (ItemStack)stack.getCapability(FoodCapability.CAPABILITY).map(food -> {
         food.setNonDecaying();
         return stack;
      }).orElse(null);
   }

   public static enum Serializer implements IIngredientSerializer<IsRottenIngredient> {
      INSTANCE;

      private Serializer() {
      }

      public IsRottenIngredient parse(JsonObject json) {
         Ingredient internal = json.has("ingredient") ? Ingredient.fromJson(JsonHelpers.get(json, "ingredient")) : null;
         return new IsRottenIngredient(internal);
      }

      public IsRottenIngredient parse(FriendlyByteBuf buffer) {
         return new IsRottenIngredient((Ingredient)Helpers.decodeNullable(buffer, Ingredient::fromNetwork));
      }

      public void write(FriendlyByteBuf buffer, IsRottenIngredient ingredient) {
         Helpers.encodeNullable(ingredient.delegate, buffer, Ingredient::toNetwork);
      }
   }
}
