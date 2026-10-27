package tfcflorae.mixin.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfcflorae.client.model.entity.animation.api.Animated;

@Mixin({PartDefinition.class})
public class PartDefinitionMixin {
   @Shadow
   @Final
   private PartPose partPose;

   public PartDefinitionMixin() {
   }

   @Inject(
      method = {"bake"},
      at = {@At("RETURN")},
      cancellable = true
   )
   private void wb$bake(int i, int j, CallbackInfoReturnable<ModelPart> cir) {
      Animated.of((ModelPart)cir.getReturnValue()).setDefault(this.partPose);
      cir.setReturnValue((ModelPart)cir.getReturnValue());
   }
}
