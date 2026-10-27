package tfcflorae.client.render.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import tfcflorae.client.model.entity.TadpoleModel;
import tfcflorae.common.entities.Tadpole;

public class TadpoleRenderer extends MobRenderer<Tadpole, TadpoleModel<Tadpole>> {
   public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("tfcflorae", "tadpole"), "main");
   private static final ResourceLocation TEXTURE = new ResourceLocation("tfcflorae", "textures/entity/animal/tadpole/tadpole.png");

   public TadpoleRenderer(Context context) {
      super(context, new TadpoleModel(context.bakeLayer(MODEL_LAYER)), 0.14F);
   }

   public ResourceLocation getTextureLocation(Tadpole entity) {
      return TEXTURE;
   }
}
