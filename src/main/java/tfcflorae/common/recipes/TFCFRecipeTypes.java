package tfcflorae.common.recipes;
import net.minecraft.core.registries.Registries;

import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.util.TFCFHelpers;

public class TFCFRecipeTypes {
   public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, "tfcflorae");
   public static final RegistryObject<RecipeType<BrushingRecipe>> BRUSHING = register("brushing");
   // 打制（knapping）统一使用 TFC 原生的 tfc:knapping：
   //   配方 -> "type": "tfc:knapping" + "knapping_type": "tfcflorae:<variant>"
   //   类型定义 -> data/tfcflorae/tfc/knapping_types/<variant>.json
   // 这样打制界面、音效、消耗逻辑全部由 TFC 的 KnappingRecipe / KnappingContainer 处理。

   public TFCFRecipeTypes() {
   }

   private static <R extends Recipe<?>> RegistryObject<RecipeType<R>> register(String name) {
      return RECIPE_TYPES.register(name, () -> new RecipeType<R>() {
            @Override
            public String toString() {
               return TFCFHelpers.identifier(name).toString();
            }
         });
   }
}
