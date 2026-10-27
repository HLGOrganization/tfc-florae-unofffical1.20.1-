package tfcflorae.mixin.packet;

import net.minecraft.network.CompressionDecoder;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

@Mixin(
   value = {CompressionDecoder.class},
   priority = 1
)
public abstract class CompressionDecoderMixin implements IMixinConfigPlugin {
   public CompressionDecoderMixin() {
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return !ModList.get().isLoaded("connectivity");
   }
}
