package tfcflorae.mixin.packet;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(
   value = {ServerGamePacketListenerImpl.class},
   priority = 1
)
public abstract class ServerGamePacketListenerImplMixin implements IMixinConfigPlugin {
   public ServerGamePacketListenerImplMixin() {
   }

   @ModifyConstant(
      method = {"tick"},
      constant = {@Constant(
         longValue = 15000L,
         ordinal = 0
      )}
   )
   private long modifyConstant(long value) {
      return Long.MAX_VALUE;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return !ModList.get().isLoaded("connectivity");
   }
}
