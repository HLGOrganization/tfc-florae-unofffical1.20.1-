package tfcflorae.mixin.world.surface;

import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tfcflorae.interfaces.TFCBiomesMixinInterface;

@Mixin({SurfaceStates.class})
public final class SurfaceStatesMixin {
   @Unique
   private static TFCBiomes staticBiomes = new TFCBiomes();
   @Unique
   private static final BiomeExtension THERMAL_CANYONS = ((TFCBiomesMixinInterface)staticBiomes).getStaticThermalCanyons();
   @Shadow
   @Mutable
   @Final
   public static final SurfaceState WATER = context -> THERMAL_CANYONS == TFCBiomes.getExtension(context.level(), context.biome())
         ? TFCFluids.SPRING_WATER.createSourceBlock()
         : (context.salty() ? TFCFluids.SALT_WATER.createSourceBlock() : Fluids.WATER.defaultFluidState().createLegacyBlock());

   public SurfaceStatesMixin() {
   }
}
