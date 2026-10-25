package tfcflorae.world.feature;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class TFCFDeferredRegistry<T> extends TFCFAbstractDeferredRegistry<T> {
   private final DeferredRegister<T> deferredRegistry;

   // 1.20.1：DeferredRegister.create 接受 ResourceKey<? extends Registry<T>>，
   // 而 Registries.TRUNK_PLACER_TYPE 本身就是 ResourceKey，故这里统一用 ResourceKey。
   public TFCFDeferredRegistry(ResourceKey<? extends Registry<T>> registryKey, String modId) {
      super(registryKey, modId);
      this.deferredRegistry = DeferredRegister.create(registryKey, modId);
   }

   public static <T> TFCFAbstractDeferredRegistry<T> create(ResourceKey<? extends Registry<T>> key, String modId) {
      return new TFCFDeferredRegistry<>(key, modId);
   }

   @Override
   public <E extends T> Supplier<E> register(String key, Supplier<E> entry) {
      return this.deferredRegistry.register(key, entry);
   }

   @Override
   public void register(IEventBus bus) {
      this.deferredRegistry.register(bus);
   }
}
