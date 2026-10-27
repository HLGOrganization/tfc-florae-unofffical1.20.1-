package tfcflorae.mixin.packet;

import net.minecraft.network.PacketEncoder;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

@Mixin(
   value = {PacketEncoder.class},
   priority = 1
)
public abstract class PacketEncoderMixin implements IMixinConfigPlugin {
   public PacketEncoderMixin() {
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return !ModList.get().isLoaded("connectivity");
   }
}
