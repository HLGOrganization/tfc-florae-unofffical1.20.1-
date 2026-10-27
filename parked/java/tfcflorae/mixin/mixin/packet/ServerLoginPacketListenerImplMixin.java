package tfcflorae.mixin.packet;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

@Mixin(
   value = {ServerLoginPacketListenerImpl.class},
   priority = 1
)
public abstract class ServerLoginPacketListenerImplMixin implements IMixinConfigPlugin {
   public ServerLoginPacketListenerImplMixin() {
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return !ModList.get().isLoaded("connectivity");
   }
}
