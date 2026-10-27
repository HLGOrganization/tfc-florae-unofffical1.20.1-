package tfcflorae.mixin.accessor;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeRenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({RenderType.class})
public interface RenderTypeAccessor {
   @Invoker
   static CompositeRenderType callCreate(String string, VertexFormat vertexFormat, Mode mode, int i, boolean bl, boolean bl2, CompositeState compositeState) {
      throw new UnsupportedOperationException();
   }
}
