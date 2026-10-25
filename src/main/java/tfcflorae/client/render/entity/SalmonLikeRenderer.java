package tfcflorae.client.render.entity;

import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Salmon;
import tfcflorae.util.TFCFHelpers;

public class SalmonLikeRenderer extends SalmonRenderer {
   private final ResourceLocation texture;

   public SalmonLikeRenderer(Context context, String name) {
      super(context);
      this.texture = TFCFHelpers.animalTexture(name);
   }

   public ResourceLocation getTextureLocation(Salmon salmon) {
      return this.texture;
   }
}
