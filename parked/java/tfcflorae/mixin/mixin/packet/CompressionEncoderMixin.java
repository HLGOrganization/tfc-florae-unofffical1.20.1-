package tfcflorae.mixin.packet;

import net.minecraft.network.CompressionEncoder;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

@Mixin(
   value = {CompressionEncoder.class},
   priority = 1
)
public abstract class CompressionEncoderMixin implements IMixinConfigPlugin {
   public CompressionEncoderMixin() {
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return !ModList.get().isLoaded("connectivity");
   }
}
