package tfcflorae.common.recipes;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TFCFRecipeSerializers {
   public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "tfcflorae");
   public static final RegistryObject<BrushingRecipe.Serializer> BRUSHING = register("brushing", BrushingRecipe.Serializer::new);
   // 打制配方已统一走 TFC 原生的 tfc:knapping + 数据驱动的 tfc/knapping_types/<name>.json，
   // 因此这里不再注册 earthenware/kaolinite/stoneware/flint 四个自定义序列化器。
   // 对应的 knapping_type 定义位于：
   //   data/tfcflorae/tfc/knapping_types/{earthenware,kaolinite,stoneware}_clay_knapping.json
   //   data/tfcflorae/tfc/knapping_types/flint_knapping.json

   public TFCFRecipeSerializers() {
   }

   private static <S extends RecipeSerializer<?>> RegistryObject<S> register(String name, Supplier<S> factory) {
      return RECIPE_SERIALIZERS.register(name, factory);
   }
}
