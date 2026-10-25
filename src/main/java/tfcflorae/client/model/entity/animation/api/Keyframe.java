package tfcflorae.client.model.entity.animation.api;

import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record Keyframe(float timestamp, Vector3f target, Transformation.Interpolation interpolation) {
}
