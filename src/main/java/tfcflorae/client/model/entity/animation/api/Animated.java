package tfcflorae.client.model.entity.animation.api;

import org.joml.Vector3f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

public interface Animated {
   static Animated of(ModelPart part) {
      return Animated.class.cast(part);
   }

   static void translate(ModelPart part, Vector3f vec) {
      part.x = part.x + vec.x();
      part.y = part.y + vec.y();
      part.z = part.z + vec.z();
   }

   static void rotate(ModelPart part, Vector3f vec) {
      part.xRot = part.xRot + vec.x();
      part.yRot = part.yRot + vec.y();
      part.zRot = part.zRot + vec.z();
   }

   static void scaleY(ModelPart part, Vector3f vec) {
      of(part).scaleXTo(vec.x());
      of(part).scaleYTo(vec.y());
      of(part).scaleZTo(vec.z());
   }

   PartPose resetToDefault();

   void setDefault(PartPose var1);

   static void resetToDefault(ModelPart part) {
      part.loadPose(of(part).resetToDefault());
   }

   float scaleX();

   void scaleX(float var1);

   void scaleXTo(float var1);

   float scaleY();

   void scaleY(float var1);

   void scaleYTo(float var1);

   float scaleZ();

   void scaleZ(float var1);

   void scaleZTo(float var1);
}
