package tfcflorae.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import tfcflorae.common.entities.Silkmoth;

public class SilkmothModel extends EntityModel<Silkmoth> {
   private final ModelPart legsL;
   private final ModelPart cube_r1;
   private final ModelPart cube_r2;
   private final ModelPart cube_r3;
   private final ModelPart legsR;
   private final ModelPart cube_r4;
   private final ModelPart cube_r5;
   private final ModelPart cube_r6;
   private final ModelPart head_pivot;
   private final ModelPart tendril_r_r1;
   private final ModelPart tendril_r_r2;
   private final ModelPart bb_main;
   private final ModelPart wingR_r1;
   private final ModelPart wingL_r1;
   private final ModelPart abdomen_r1;

   public static LayerDefinition createBodyLayer() {
      MeshDefinition modelData = new MeshDefinition();
      PartDefinition modelPartData = modelData.getRoot();
      PartDefinition legsL = modelPartData.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(0, 0), PartPose.offsetAndRotation(1.5F, 19.9F, -0.45F, 0.0F, 0.0F, 0.6981F)
      );
      legsL.addOrReplaceChild(
         "cube_r1",
         CubeListBuilder.create().texOffs(0, 13).addBox(0.0216F, 0.0F, -0.5976F, 3.0F, 0.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.2182F, 0.3927F)
      );
      legsL.addOrReplaceChild(
         "cube_r2",
         CubeListBuilder.create().texOffs(0, 15).addBox(0.0F, 0.0F, -0.6F, 3.0F, 0.0F, 1.0F),
         PartPose.offsetAndRotation(0.5F, 0.1F, -0.05F, 0.0F, 0.0F, 0.3927F)
      );
      legsL.addOrReplaceChild(
         "cube_r3",
         CubeListBuilder.create().texOffs(0, 14).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.9F, 0.0F, -0.2182F, 0.3927F)
      );
      PartDefinition legsR = modelPartData.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(0, 0), PartPose.offsetAndRotation(-1.5F, 19.9F, -0.55F, 0.0F, 3.1416F, -0.6545F)
      );
      legsR.addOrReplaceChild(
         "cube_r4",
         CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.2182F, 0.3927F)
      );
      legsR.addOrReplaceChild(
         "cube_r5",
         CubeListBuilder.create().texOffs(0, 11).addBox(0.0F, 0.0F, -0.4F, 3.0F, 0.0F, 1.0F),
         PartPose.offsetAndRotation(0.5F, 0.1F, -0.05F, 0.0F, 0.0F, 0.3927F)
      );
      legsR.addOrReplaceChild(
         "cube_r6",
         CubeListBuilder.create().texOffs(0, 12).addBox(0.0216F, 0.0F, -0.4024F, 3.0F, 0.0F, 1.0F),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.9F, 0.0F, -0.2182F, 0.3927F)
      );
      PartDefinition head_pivot = modelPartData.addOrReplaceChild(
         "head", CubeListBuilder.create().texOffs(15, 10).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 18.0F, -3.0F)
      );
      head_pivot.addOrReplaceChild(
         "tendril_r_r1",
         CubeListBuilder.create().mirror().texOffs(23, 0).addBox(-1.5F, -5.0F, 0.0F, 3.0F, 5.0F, 0.0F),
         PartPose.offsetAndRotation(1.0F, -1.15F, -1.0F, 0.0F, 0.0F, 0.3927F)
      );
      head_pivot.addOrReplaceChild(
         "tendril_r_r2",
         CubeListBuilder.create().texOffs(23, 0).addBox(-1.5F, -5.0F, 0.0F, 3.0F, 5.0F, 0.0F),
         PartPose.offsetAndRotation(-1.0F, -1.15F, -1.0F, 0.0F, 0.0F, -0.3927F)
      );
      PartDefinition bb_main = modelPartData.addOrReplaceChild(
         "body", CubeListBuilder.create().texOffs(19, 19).addBox(-2.5F, -8.5F, -3.0F, 5.0F, 5.0F, 3.0F), PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      bb_main.addOrReplaceChild(
         "right_wing",
         CubeListBuilder.create().mirror().texOffs(0, 5).addBox(-7.0F, 0.0F, -3.0F, 9.0F, 0.0F, 5.0F),
         PartPose.offsetAndRotation(-1.5F, -6.5F, 0.5F, 0.0F, 0.0F, 0.3927F)
      );
      bb_main.addOrReplaceChild(
         "left_wing",
         CubeListBuilder.create().texOffs(0, 5).addBox(-2.0F, 0.0F, -3.0F, 9.0F, 0.0F, 5.0F),
         PartPose.offsetAndRotation(1.5F, -6.5F, 0.5F, 0.0F, 0.0F, -0.3927F)
      );
      bb_main.addOrReplaceChild(
         "abdomen_r1",
         CubeListBuilder.create().texOffs(0, 10).addBox(-3.0F, -4.0F, -1.0F, 4.0F, 4.0F, 7.0F),
         PartPose.offsetAndRotation(1.0F, -3.9F, 0.0F, -0.3927F, 0.0F, 0.0F)
      );
      return LayerDefinition.create(modelData, 64, 64);
   }

   public SilkmothModel(ModelPart modelPart) {
      this.legsL = modelPart.getChild("left_leg");
      this.cube_r1 = this.legsL.getChild("cube_r1");
      this.cube_r2 = this.legsL.getChild("cube_r2");
      this.cube_r3 = this.legsL.getChild("cube_r3");
      this.legsR = modelPart.getChild("right_leg");
      this.cube_r4 = this.legsR.getChild("cube_r4");
      this.cube_r5 = this.legsR.getChild("cube_r5");
      this.cube_r6 = this.legsR.getChild("cube_r6");
      this.head_pivot = modelPart.getChild("head");
      this.tendril_r_r1 = this.head_pivot.getChild("tendril_r_r1");
      this.tendril_r_r2 = this.head_pivot.getChild("tendril_r_r2");
      this.bb_main = modelPart.getChild("body");
      this.wingR_r1 = this.bb_main.getChild("right_wing");
      this.wingL_r1 = this.bb_main.getChild("left_wing");
      this.abdomen_r1 = this.bb_main.getChild("abdomen_r1");
   }

   public void setupAnim(Silkmoth entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
      this.wingR_r1.zRot = Mth.sin(animationProgress * 2.0F) * 0.4F + 0.3927F;
      this.wingL_r1.zRot = -this.wingR_r1.zRot;
      this.head_pivot.xRot = Mth.sin(animationProgress * 0.03F) * 0.1F;
      this.tendril_r_r1.zRot = Mth.sin(animationProgress * 0.07F) * 0.2F + 0.3927F;
      this.tendril_r_r2.zRot = -this.tendril_r_r1.zRot;
      this.abdomen_r1.xRot = Mth.sin(animationProgress * 0.05F) * 0.1F - 0.3927F;
      this.legsR.zRot = Mth.sin(animationProgress * 0.07F) * 0.1F - 0.6545F;
      this.legsL.zRot = -this.legsR.zRot;
   }

   public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
      this.bb_main.render(matrices, vertices, light, overlay);
      this.head_pivot.render(matrices, vertices, light, overlay);
      this.legsL.render(matrices, vertices, light, overlay);
      this.legsR.render(matrices, vertices, light, overlay);
   }
}
