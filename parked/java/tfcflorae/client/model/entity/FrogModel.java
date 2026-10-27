package tfcflorae.client.model.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import tfcflorae.client.model.entity.animation.FrogAnimations;
import tfcflorae.client.model.entity.animation.api.Animated;
import tfcflorae.client.model.entity.animation.api.AnimatedModel;
import tfcflorae.common.entities.Frog;

public class FrogModel<T extends Frog> extends AnimatedModel<T> {
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart eyes;
   private final ModelPart tongue;
   private final ModelPart leftArm;
   private final ModelPart rightArm;
   private final ModelPart leftLeg;
   private final ModelPart rightLeg;
   private final ModelPart croakingBody;

   public FrogModel(ModelPart root) {
      this.root = root.getChild("root");
      this.body = this.root.getChild("body");
      this.head = this.body.getChild("head");
      this.eyes = this.head.getChild("eyes");
      this.tongue = this.body.getChild("tongue");
      this.leftArm = this.body.getChild("left_arm");
      this.rightArm = this.body.getChild("right_arm");
      this.leftLeg = this.root.getChild("left_leg");
      this.rightLeg = this.root.getChild("right_leg");
      this.croakingBody = this.body.getChild("croaking_body");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition mesh = new MeshDefinition();
      PartDefinition root = mesh.getRoot();
      PartDefinition bone = root.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition body = bone.addOrReplaceChild(
         "body",
         CubeListBuilder.create()
            .texOffs(3, 1)
            .addBox(-3.5F, -2.0F, -8.0F, 7.0F, 3.0F, 9.0F)
            .texOffs(23, 22)
            .addBox(-3.5F, -1.0F, -8.0F, 7.0F, 0.0F, 9.0F),
         PartPose.offset(0.0F, -2.0F, 4.0F)
      );
      PartDefinition head = body.addOrReplaceChild(
         "head",
         CubeListBuilder.create()
            .texOffs(23, 13)
            .addBox(-3.5F, -1.0F, -7.0F, 7.0F, 0.0F, 9.0F)
            .texOffs(0, 13)
            .addBox(-3.5F, -2.0F, -7.0F, 7.0F, 3.0F, 9.0F),
         PartPose.offset(0.0F, -2.0F, -1.0F)
      );
      PartDefinition eyes = head.addOrReplaceChild("eyes", CubeListBuilder.create(), PartPose.offset(-0.5F, 0.0F, 2.0F));
      eyes.addOrReplaceChild(
         "right_eye", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-1.5F, -3.0F, -6.5F)
      );
      eyes.addOrReplaceChild(
         "left_eye", CubeListBuilder.create().texOffs(0, 5).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(2.5F, -3.0F, -6.5F)
      );
      body.addOrReplaceChild(
         "croaking_body",
         CubeListBuilder.create().texOffs(26, 5).addBox(-3.5F, -0.1F, -2.9F, 7.0F, 2.0F, 3.0F, new CubeDeformation(-0.1F)),
         PartPose.offset(0.0F, -1.0F, -5.0F)
      );
      body.addOrReplaceChild(
         "tongue", CubeListBuilder.create().texOffs(17, 13).addBox(-2.0F, 0.0F, -7.1F, 4.0F, 0.0F, 7.0F), PartPose.offset(0.0F, -1.01F, 1.0F)
      );
      PartDefinition left_arm = body.addOrReplaceChild(
         "left_arm", CubeListBuilder.create().texOffs(0, 32).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 3.0F), PartPose.offset(4.0F, -1.0F, -6.5F)
      );
      left_arm.addOrReplaceChild(
         "left_hand", CubeListBuilder.create().texOffs(18, 40).addBox(-4.0F, 0.01F, -4.0F, 8.0F, 0.0F, 8.0F), PartPose.offset(0.0F, 3.0F, -1.0F)
      );
      PartDefinition right_arm = body.addOrReplaceChild(
         "right_arm", CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 3.0F), PartPose.offset(-4.0F, -1.0F, -6.5F)
      );
      right_arm.addOrReplaceChild(
         "right_hand", CubeListBuilder.create().texOffs(2, 40).addBox(-4.0F, 0.01F, -5.0F, 8.0F, 0.0F, 8.0F), PartPose.offset(0.0F, 3.0F, 0.0F)
      );
      PartDefinition left_leg = bone.addOrReplaceChild(
         "left_leg", CubeListBuilder.create().texOffs(14, 25).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 3.0F, 4.0F), PartPose.offset(3.5F, -3.0F, 4.0F)
      );
      left_leg.addOrReplaceChild(
         "left_foot", CubeListBuilder.create().texOffs(2, 32).addBox(-4.0F, 0.01F, -4.0F, 8.0F, 0.0F, 8.0F), PartPose.offset(2.0F, 3.0F, 0.0F)
      );
      PartDefinition right_leg = bone.addOrReplaceChild(
         "right_leg", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 3.0F, 4.0F), PartPose.offset(-3.5F, -3.0F, 4.0F)
      );
      right_leg.addOrReplaceChild(
         "right_foot", CubeListBuilder.create().texOffs(18, 32).addBox(-4.0F, 0.01F, -4.0F, 8.0F, 0.0F, 8.0F), PartPose.offset(-2.0F, 3.0F, 0.0F)
      );
      return LayerDefinition.create(mesh, 48, 48);
   }

   public void setupAnim(T entity, float angle, float distance, float animationProgress, float yaw, float pitch) {
      this.root.getAllParts().forEach(Animated::resetToDefault);
      float speedMultiplier = Math.min((float)entity.getDeltaMovement().lengthSqr() * 200.0F, 8.0F);
      this.animate(entity.longJumpingAnimationState, FrogAnimations.LONG_JUMPING, animationProgress);
      this.animate(entity.croakingAnimationState, FrogAnimations.CROAKING, animationProgress);
      this.animate(entity.usingTongueAnimationState, FrogAnimations.USING_TONGUE, animationProgress);
      this.animate(entity.walkingAnimationState, FrogAnimations.WALKING, animationProgress, speedMultiplier);
      this.animate(entity.swimmingAnimationState, FrogAnimations.SWIMMING, animationProgress);
      this.animate(entity.idlingInWaterAnimationState, FrogAnimations.IDLING_IN_WATER, animationProgress);
      this.croakingBody.visible = entity.croakingAnimationState.isRunning();
   }

   public ModelPart root() {
      return this.root;
   }
}
