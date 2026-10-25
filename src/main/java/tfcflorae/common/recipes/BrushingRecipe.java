package tfcflorae.common.recipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import net.dries007.tfc.common.capabilities.player.PlayerDataCapability;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.recipes.RecipeSerializerImpl;
import net.dries007.tfc.common.recipes.SimpleBlockRecipe;
import net.dries007.tfc.common.recipes.ingredients.BlockIngredient;
import net.dries007.tfc.common.recipes.ingredients.BlockIngredient;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.JsonHelpers;
import net.dries007.tfc.util.collections.IndirectHashCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.TFCFTags;

public class BrushingRecipe extends SimpleBlockRecipe {
   public static final IndirectHashCollection<Block, BrushingRecipe> CACHE = IndirectHashCollection.createForRecipe(
      recipe -> recipe.getBlockIngredient().blocks(), TFCFRecipeTypes.BRUSHING
   );
   @Nullable
   private final Ingredient itemIngredient;
   private final ItemStackProvider extraDrop;

   public static Either<BlockState, InteractionResult> computeResult(Player player, BlockState state, BlockHitResult hit, boolean informWhy) {
      ItemStack held = player.getMainHandItem();
      if (Helpers.isItem(held, TFCFTags.Items.BRUSHES)) {
         BlockPos pos = hit.getBlockPos();
         return player.getCapability(PlayerDataCapability.CAPABILITY).<Either<BlockState, InteractionResult>>map(cap -> {
            BrushingRecipe recipe = getRecipe(state, held);
            if (recipe == null) {
               if (informWhy) {
                  complain(player, "no_recipe");
               }

               return Either.right(InteractionResult.PASS);
            } else {
               BlockState brushed = recipe.getBlockCraftingResult(state);
               brushed = brushed.getBlock().getStateForPlacement(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, new ItemStack(brushed.getBlock()), hit));
               if (brushed == null) {
                  if (informWhy) {
                     complain(player, "cannot_place");
                  }

                  return Either.right(InteractionResult.FAIL);
               } else {
                  brushed = FluidHelpers.fillWithFluid(brushed, player.level().getFluidState(pos).getType());
                  if (brushed == null) {
                     if (informWhy) {
                        complain(player, "bad_fluid");
                     }

                     return Either.right(InteractionResult.FAIL);
                  } else {
                     return Either.left(brushed);
                  }
               }
            }
         }).orElse(Either.<BlockState, InteractionResult>right(InteractionResult.PASS));
      } else {
         return Either.right(InteractionResult.PASS);
      }
   }

   private static void complain(Player player, String message) {
      player.displayClientMessage(Component.translatable("tfcflorae.brushing." + message), true);
   }

   @Nullable
   public static BrushingRecipe getRecipe(BlockState state, ItemStack held) {
      for (BrushingRecipe recipe : CACHE.getAll(state.getBlock())) {
         if (recipe.matches(state, held)) {
            return recipe;
         }
      }

      return null;
   }

   public BrushingRecipe(
      ResourceLocation id, BlockIngredient ingredient, BlockState outputState, @Nullable Ingredient itemIngredient, ItemStackProvider extraDrop
   ) {
      super(id, ingredient, outputState, false);
      this.itemIngredient = itemIngredient;
      this.extraDrop = extraDrop;
   }

   public RecipeSerializer<?> getSerializer() {
      return (RecipeSerializer<?>)TFCFRecipeSerializers.BRUSHING.get();
   }

   public RecipeType<?> getType() {
      return (RecipeType<?>)TFCFRecipeTypes.BRUSHING.get();
   }

   public boolean matches(BlockState state, ItemStack stack) {
      return this.itemIngredient != null && !this.itemIngredient.test(stack) ? false : this.matches(state);
   }

   @Nullable
   public Ingredient getItemIngredient() {
      return this.itemIngredient;
   }

   public ItemStack getExtraDrop(ItemStack chisel) {
      return this.extraDrop.getSingleStack(chisel);
   }

   public static class Serializer extends RecipeSerializerImpl<BrushingRecipe> {
      public Serializer() {
      }

      public BrushingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
         BlockIngredient ingredient = BlockIngredient.fromJson(JsonHelpers.get(json, "ingredient"));
         BlockState state = JsonHelpers.getBlockState(GsonHelper.getAsString(json, "result"));
         Ingredient itemIngredient = json.has("item_ingredient") ? Ingredient.fromJson(json.get("item_ingredient")) : null;
         ItemStackProvider drop = json.has("extra_drop") ? ItemStackProvider.fromJson(JsonHelpers.getAsJsonObject(json, "extra_drop")) : ItemStackProvider.empty();
         return new BrushingRecipe(recipeId, ingredient, state, itemIngredient, drop);
      }

      @Nullable
      public BrushingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
         BlockIngredient ingredient = BlockIngredient.fromNetwork(buffer);
         BlockState state = ((Block)buffer.readRegistryIdUnsafe(ForgeRegistries.BLOCKS)).defaultBlockState();
         Ingredient itemIngredient = (Ingredient)Helpers.decodeNullable(buffer, Ingredient::fromNetwork);
         ItemStackProvider drop = ItemStackProvider.fromNetwork(buffer);
         return new BrushingRecipe(recipeId, ingredient, state, itemIngredient, drop);
      }

      public void toNetwork(FriendlyByteBuf buffer, BrushingRecipe recipe) {
         recipe.ingredient.toNetwork(buffer);
         buffer.writeRegistryIdUnsafe(ForgeRegistries.BLOCKS, recipe.outputState.getBlock());
         Helpers.encodeNullable(recipe.itemIngredient, buffer, Ingredient::toNetwork);
         recipe.extraDrop.toNetwork(buffer);
      }
   }
}
