package tfcflorae.client.render.entity;

import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cod;
import tfcflorae.util.TFCFHelpers;

public class CodLikeRenderer extends CodRenderer {
   private final ResourceLocation texture;

   public CodLikeRenderer(Context context, String name) {
      super(context);
      this.texture = TFCFHelpers.animalTexture(name);
   }

   public ResourceLocation getTextureLocation(Cod cod) {
      return this.texture;
   }
}
