package tfcflorae.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import tfcflorae.client.TFCFRenderHelpers;
import tfcflorae.client.model.entity.SilkmothModel;
import tfcflorae.common.entities.Silkmoth;
import tfcflorae.util.TFCFHelpers;

public class SilkmothRenderer extends TFCFSimpleMobRenderer<Silkmoth, SilkmothModel> {
   public SilkmothRenderer(Context ctx) {
      super(ctx, new SilkmothModel(TFCFRenderHelpers.bakeSimple(ctx, "silk_moth")), "silk_moth", 0.4F, false, 0.8F, false, false, null);
   }

   public ResourceLocation getTextureLocation(Silkmoth entity) {
      return TFCFHelpers.identifier("textures/entity/animal/silk_moth.png");
   }
}
