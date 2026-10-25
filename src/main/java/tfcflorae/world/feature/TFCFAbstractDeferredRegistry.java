package tfcflorae.world.feature;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class TFCFAbstractDeferredRegistry<T> {
   protected final ResourceKey<? extends Registry<T>> registryKey;
   protected final String modId;

   public TFCFAbstractDeferredRegistry(ResourceKey<? extends Registry<T>> registryKey, String modId) {
      this.registryKey = registryKey;
      this.modId = modId;
   }

   public abstract <E extends T> Supplier<E> register(String var1, Supplier<E> var2);

   public abstract void register(IEventBus var1);
}
