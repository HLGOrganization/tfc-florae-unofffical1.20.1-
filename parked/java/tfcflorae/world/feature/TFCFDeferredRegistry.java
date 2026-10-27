package tfcflorae.world.feature;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class TFCFDeferredRegistry<T> extends TFCFAbstractDeferredRegistry<T> {
   private final DeferredRegister<T> deferredRegistry;

   public TFCFDeferredRegistry(Registry<T> registry, String modId) {
      super(registry, modId);
      this.deferredRegistry = DeferredRegister.create(registry.key(), modId);
   }

   public static <T> TFCFAbstractDeferredRegistry<T> create(Registry<T> key, String modId) {
      return new TFCFDeferredRegistry(key, modId);
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
