package tfcflorae.audit.mixin;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.GameData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Gives the Florae config its in-memory defaults before dev-only RegisterEvent reads. */
@Mixin(value = GameData.class, remap = false)
public abstract class AuditConfigMixin {
    @Inject(method = "postRegisterEvents", at = @At("HEAD"), remap = false)
    private static void loadAllConfigDefaults(CallbackInfo callback) {
        if (!Boolean.getBoolean("tfcflorae.audit")) return;
        int loaded = 0;
        for (var configs : ConfigTracker.INSTANCE.configSets().values()) {
            for (ModConfig config : configs) {
                if (config.getSpec() instanceof ForgeConfigSpec spec && !spec.isLoaded()) {
                    spec.acceptConfig(CommentedConfig.copy(spec.getSpec()));
                    loaded++;
                }
            }
        }
        System.out.println("[FLORAE-AUDIT] Loaded in-memory defaults for " + loaded + " config specs");
    }
}
