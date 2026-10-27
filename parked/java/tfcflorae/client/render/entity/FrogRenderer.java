package tfcflorae.client.render.entity;
import java.util.HashMap;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import tfcflorae.client.model.entity.FrogModel;
import tfcflorae.common.entities.Frog;

public class FrogRenderer extends TFCFSimpleMobRenderer<Frog, FrogModel<Frog>> {
   public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("tfcflorae", "frog"), "main");
   private static final Map<Frog.Variant, ResourceLocation> TEXTURES = Util.make(new HashMap<Frog.Variant, ResourceLocation>(), hashMap -> {
      for (Frog.Variant variant : Frog.Variant.values()) {
         hashMap.put(variant, new ResourceLocation("tfcflorae", String.format("textures/entity/animal/frog/%s_frog.png", variant.getName())));
      }
   });

   public FrogRenderer(Context context) {
      super(context, new FrogModel<>(context.bakeLayer(MODEL_LAYER)), "frog", 0.3F, false, 0.8F, false, false, null);
   }

   public ResourceLocation getTextureLocation(Frog frog) {
      return TEXTURES.get(frog.getVariant());
   }

   protected void setupRotations(Frog frog, PoseStack stack, float ageInTicks, float rotationYaw, float partialTicks) {
      float scale = frog.getVisualScale();
      super.setupRotations(frog, stack, ageInTicks, rotationYaw, partialTicks);
      stack.scale(scale, scale, scale);
   }
}
