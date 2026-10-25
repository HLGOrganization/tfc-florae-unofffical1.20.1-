package tfcflorae.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;
import tfcflorae.client.TFCFRenderHelpers;
import tfcflorae.util.TFCFHelpers;

public class TFCFSimpleMobRenderer<T extends Mob, M extends EntityModel<T>> extends MobRenderer<T, M> {
   private final ResourceLocation texture;
   @Nullable
   private final ResourceLocation babyTexture;
   @Nullable
   private final Function<T, ResourceLocation> textureGetter;
   private final boolean doesFlop;
   private final float scale;

   public TFCFSimpleMobRenderer(
      Context ctx,
      M model,
      String name,
      float shadow,
      boolean flop,
      float scale,
      boolean hasBabyTexture,
      boolean itemInMouth,
      @Nullable Function<T, ResourceLocation> textureGetter
   ) {
      super(ctx, model, shadow);
      this.doesFlop = flop;
      this.texture = TFCFHelpers.animalTexture(name);
      this.babyTexture = hasBabyTexture ? TFCFHelpers.animalTexture(name + "_young") : null;
      this.textureGetter = textureGetter != null ? textureGetter : e -> this.babyTexture != null && e.isBaby() ? this.babyTexture : this.texture;
      this.scale = scale;
   }

   protected void setupRotations(T entity, PoseStack poseStack, float ageInTicks, float yaw, float partialTicks) {
      super.setupRotations(entity, poseStack, ageInTicks, yaw, partialTicks);
      if (this.doesFlop) {
         poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(Mth.sin(0.6F * ageInTicks)));
         if (!entity.isInWater()) {
            poseStack.translate(0.1F, 0.1F, -0.1F);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0F));
         }
      }
   }

   protected void scale(T entity, PoseStack poseStack, float scale) {
      float amount = entity.isBaby() ? this.scale * 0.7F : this.scale;
      if (entity instanceof TFCAnimalProperties animal) {
         amount *= animal.getAgeScale();
      }

      poseStack.scale(amount, amount, amount);
      super.scale(entity, poseStack, scale);
   }

   public ResourceLocation getTextureLocation(T entity) {
      return this.textureGetter.apply(entity);
   }

   public static class Builder<T extends Mob, M extends EntityModel<T>> {
      private final Context ctx;
      private final Function<ModelPart, M> model;
      private final String name;
      private float shadow = 0.3F;
      private boolean flop = false;
      private float scale = 1.0F;
      private boolean hasBabyTexture = false;
      private boolean itemInMouth = false;
      @Nullable
      private Function<T, ResourceLocation> textureGetter = null;

      public Builder(Context ctx, Function<ModelPart, M> model, String name) {
         this.ctx = ctx;
         this.model = model;
         this.name = name;
      }

      public TFCFSimpleMobRenderer.Builder<T, M> flops() {
         this.flop = true;
         return this;
      }

      public TFCFSimpleMobRenderer.Builder<T, M> shadow(float size) {
         this.shadow = size;
         return this;
      }

      public TFCFSimpleMobRenderer.Builder<T, M> scale(float scale) {
         this.scale = scale;
         return this;
      }

      public TFCFSimpleMobRenderer.Builder<T, M> hasBabyTexture() {
         this.hasBabyTexture = true;
         return this;
      }

      public TFCFSimpleMobRenderer.Builder<T, M> mouthy() {
         this.itemInMouth = true;
         return this;
      }

      public TFCFSimpleMobRenderer.Builder<T, M> texture(Function<T, ResourceLocation> getter) {
         this.textureGetter = getter;
         return this;
      }

      public TFCFSimpleMobRenderer<T, M> build() {
         return new TFCFSimpleMobRenderer<>(
            this.ctx,
            this.model.apply(TFCFRenderHelpers.bakeSimple(this.ctx, this.name)),
            this.name,
            this.shadow,
            this.flop,
            this.scale,
            this.hasBabyTexture,
            this.itemInMouth,
            this.textureGetter
         );
      }
   }
}
