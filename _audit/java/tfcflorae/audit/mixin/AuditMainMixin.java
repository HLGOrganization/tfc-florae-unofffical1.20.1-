package tfcflorae.audit.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Main;
import net.minecraftforge.server.loading.ServerModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pure mod bootstrap; the original server main is never executed. */
@Mixin(Main.class)
public abstract class AuditMainMixin {
    @Inject(method = "main", at = @At("HEAD"), cancellable = true, remap = false)
    private static void auditBootstrapOnly(String[] args, CallbackInfo callback) {
        if (!Boolean.getBoolean("tfcflorae.audit")) return;
        callback.cancel();
        try {
            System.out.println("[FLORAE-AUDIT] Pure registry bootstrap; original server main cancelled");
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
            Bootstrap.validate();
            ServerModLoader.load();
            System.err.println("[FLORAE-AUDIT] Load complete listener did not terminate the probe");
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.setProperty("tfcflorae.audit.mod_load_error", failure.toString());
            tfcflorae.audit.DataPackProbe.run();
        }
        System.exit(3);
    }
}
