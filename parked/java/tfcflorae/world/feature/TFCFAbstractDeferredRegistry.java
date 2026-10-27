package tfcflorae.world.feature;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;

public abstract class TFCFAbstractDeferredRegistry<T> {
   protected final Registry<T> registry;
   protected final String modId;

   public TFCFAbstractDeferredRegistry(Registry<T> registry, String modId) {
      this.registry = registry;
      this.modId = modId;
   }

   public abstract <E extends T> Supplier<E> register(String var1, Supplier<E> var2);

   public abstract void register(IEventBus var1);
}
