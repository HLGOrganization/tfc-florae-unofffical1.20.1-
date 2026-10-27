package tfcflorae.world.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TFCFCarvers {
   public static final DeferredRegister<WorldCarver<?>> CARVERS = DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, "tfcflorae");
   public static final RegistryObject<CavernCaverFeature> DEEP_CAVES = register("deep_caves", CavernCaverFeature::new, CaveCarverConfiguration.CODEC);

   public TFCFCarvers() {
   }

   private static <C extends CarverConfiguration, WC extends WorldCarver<C>> RegistryObject<WC> register(
      String name, Function<Codec<C>, WC> factory, Codec<C> codec
   ) {
      return CARVERS.register(name, () -> factory.apply(codec));
   }
}
