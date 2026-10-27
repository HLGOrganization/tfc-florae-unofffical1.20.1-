package tfcflorae.mixin.packet;

import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(
   value = {ServerboundCustomPayloadPacket.class},
   priority = 1
)
public abstract class ServerboundCustomPayloadPacketMixin implements IMixinConfigPlugin {
   public ServerboundCustomPayloadPacketMixin() {
   }

   @ModifyConstant(
      method = {"<init>(Lnet/minecraft/network/FriendlyByteBuf;)V"},
      constant = {@Constant(
         intValue = 32767
      )}
   )
   private int modifyConstant(int value) {
      return Integer.MAX_VALUE;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return !ModList.get().isLoaded("connectivity");
   }
}
