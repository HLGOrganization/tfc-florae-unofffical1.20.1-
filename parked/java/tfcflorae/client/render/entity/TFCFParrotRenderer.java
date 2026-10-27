package tfcflorae.client.render.entity;
import java.util.HashMap;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import tfcflorae.client.model.entity.TFCFParrotModel;
import tfcflorae.common.entities.TFCFParrot;

public class TFCFParrotRenderer extends TFCFSimpleMobRenderer<TFCFParrot, TFCFParrotModel> {
   public static final ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(new ResourceLocation("tfcflorae", "parrot"), "main");
   private static final Map<TFCFParrot.Variant, ResourceLocation> TEXTURES = Util.make(new HashMap<TFCFParrot.Variant, ResourceLocation>(), hashMap -> {
         for (TFCFParrot.Variant variant : TFCFParrot.Variant.values()) {
            hashMap.put(variant, new ResourceLocation("tfcflorae", String.format("textures/entity/animal/parrot/%s.png", variant.getName())));
         }
      }
   );

   public TFCFParrotRenderer(Context context) {
      super(context, new TFCFParrotModel(context.bakeLayer(MODEL_LAYER)), "parrot", 0.3F, false, 1.0F, false, false, null);
   }

   public ResourceLocation getTextureLocation(TFCFParrot parrot) {
      return TEXTURES.get(parrot.getParrotVariant());
   }

   public float getBob(TFCFParrot parrot, float delta) {
      float f = Mth.lerp(delta, parrot.oFlap, parrot.flap);
      float f1 = Mth.lerp(delta, parrot.oFlapSpeed, parrot.flapSpeed);
      return (Mth.sin(f) + 1.0F) * f1;
   }
}
