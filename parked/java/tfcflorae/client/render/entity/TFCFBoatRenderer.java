package tfcflorae.client.render.entity;
import com.mojang.math.Axis;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;
import tfcflorae.client.TFCFRenderHelpers;
import tfcflorae.client.model.entity.RaftModel;
import tfcflorae.util.TFCFHelpers;

public class TFCFBoatRenderer extends BoatRenderer {
   private final Pair<ResourceLocation, ListModel<Boat>> location;
   private final String name;
   private final boolean hasRaftModel;

   public static ModelLayerLocation boatName(String name) {
      return TFCFRenderHelpers.modelIdentifier("boat/" + name);
   }

   public TFCFBoatRenderer(Context context, String name, boolean hasRaftModel) {
      super(context, net.minecraft.client.model.geom.ModelLayers.createBoatModelName(net.minecraft.world.entity.vehicle.Boat.Type.OAK), false);
      this.hasRaftModel = hasRaftModel;
      this.name = name;
      this.location = Pair.of(
         TFCFHelpers.identifier("textures/entity/boat/" + name + ".png"),
         hasRaftModel ? new RaftModel(context.bakeLayer(boatName(name))) : new BoatModel(context.bakeLayer(boatName(name)))
      );
   }

   public ResourceLocation getTextureLocation(Boat boat) {
      return (ResourceLocation)this.getModelWithLocationNew(boat).getFirst();
   }

   public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocationNew(Boat boat) {
      return this.location;
   }

   public void render(Boat pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
      pMatrixStack.pushPose();
      pMatrixStack.translate(0.0, 0.375, 0.0);
      pMatrixStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - pEntityYaw));
      float f = (float)pEntity.getHurtTime() - pPartialTicks;
      float f1 = pEntity.getDamage() - pPartialTicks;
      if (f1 < 0.0F) {
         f1 = 0.0F;
      }

      if (f > 0.0F) {
         pMatrixStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(Mth.sin(f) * f * f1 / 10.0F * (float)pEntity.getHurtDir()));
      }

      float f2 = pEntity.getBubbleAngle(pPartialTicks);
      if (!Mth.equal(f2, 0.0F)) {
         pMatrixStack.mulPose(new org.joml.Quaternionf().rotationAxis(pEntity.getBubbleAngle(pPartialTicks), 1.0F, 0.0F, 1.0F));
      }

      Pair<ResourceLocation, ListModel<Boat>> pair = this.getModelWithLocationNew(pEntity);
      ResourceLocation resourcelocation = (ResourceLocation)pair.getFirst();
      ListModel<Boat> listmodel = (ListModel<Boat>)pair.getSecond();
      pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
      pMatrixStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90.0F));
      listmodel.setupAnim(pEntity, pPartialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(listmodel.renderType(resourcelocation));
      listmodel.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      if (!pEntity.isUnderWater() && !this.hasRaftModel) {
         VertexConsumer vertexconsumer1 = pBuffer.getBuffer(RenderType.waterMask());
         if (listmodel instanceof BoatModel waterpatchmodel && waterpatchmodel.waterPatch() != null) {
            waterpatchmodel.waterPatch().render(pMatrixStack, vertexconsumer1, pPackedLight, OverlayTexture.NO_OVERLAY);
         }
      }

      pMatrixStack.popPose();
      // 1.20.1: the nameplate event is fired by the renderer (RenderNameTagEvent),
      // not constructed here, so rendering finishes with the base implementation.
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
   }
}
