package tfcflorae.client.particle;
import net.minecraft.core.registries.Registries;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleOptions.Deserializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class TFCFParticles {
   public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, "tfcflorae");
   public static final RegistryObject<SimpleParticleType> WATER_FLOW = register("water_flow");
   public static final RegistryObject<ParticleType<BlockParticleOption>> FALLING_LEAF = register(
      "falling_leaf", BlockParticleOption.DESERIALIZER, BlockParticleOption::codec
   );
   public static final RegistryObject<ParticleType<BlockParticleOption>> FALLING_SPORE = register(
      "falling_spore", BlockParticleOption.DESERIALIZER, BlockParticleOption::codec
   );

   public TFCFParticles() {
   }

   private static <T extends ParticleOptions> RegistryObject<ParticleType<T>> register(
      String name, Deserializer<T> deserializer, Function<ParticleType<T>, Codec<T>> codec
   ) {
      return PARTICLE_TYPES.register(name, () -> new ParticleType<T>(false, deserializer) {
            public Codec<T> codec() {
               return (Codec<T>)codec.apply(this);
            }
         });
   }

   private static RegistryObject<SimpleParticleType> register(String name) {
      return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(false));
   }
}
