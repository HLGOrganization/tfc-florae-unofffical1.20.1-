package tfcflorae.mixin.packet;

import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

@Mixin(
   value = {ClientboundCustomPayloadPacket.class},
   priority = 1
)
public abstract class ClientboundCustomPayloadPacketMixin implements IMixinConfigPlugin {
   public ClientboundCustomPayloadPacketMixin() {
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return !ModList.get().isLoaded("connectivity");
   }
}
