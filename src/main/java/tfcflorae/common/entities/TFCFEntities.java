package tfcflorae.common.entities;
import net.minecraft.core.registries.Registries;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import net.dries007.tfc.util.Helpers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.client.TFCFSounds;
import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.common.items.TFCFItems;

public class TFCFEntities {
   public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "tfcflorae");
   public static final Map<TFCFWood, RegistryObject<EntityType<TFCFBoat>>> BOATS = boatEntityMapper();
   // 只保留蚕蛾：青蛙 / 蝌蚪 / 鹦鹉已按用户要求整体移除（实体、模型、渲染、AI、刷怪蛋、音效）
   public static final RegistryObject<EntityType<Silkmoth>> SILKMOTH = register(
      "silk_moth", Builder.of(Silkmoth::new, MobCategory.CREATURE).sized(0.7F, 0.6F).clientTrackingRange(10)
   );



   public TFCFEntities() {
   }

   private static Map<TFCFWood, RegistryObject<EntityType<TFCFBoat>>> boatEntityMapper() {
      Map<TFCFWood, RegistryObject<EntityType<TFCFBoat>>> Map = new HashMap<>();

      for (TFCFWood wood : TFCFWood.class.getEnumConstants()) {
         Map.put(
            wood,
            register(
               "boat/" + wood.name(),
               Builder.<TFCFBoat>of((type, level) -> new TFCFBoat(type, level, wood, (Supplier<? extends Item>)TFCFItems.BOATS.get(wood)), MobCategory.MISC)
                  .sized(1.375F, 0.5625F)
                  .clientTrackingRange(10)
            )
         );
      }

      return Map;
   }

   public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
      event.put((EntityType)SILKMOTH.get(), Silkmoth.createAttributes().build());
   }

   public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, Builder<E> builder) {
      return register(name, builder, true);
   }

   public static <E extends Entity> RegistryObject<EntityType<E>> register(String name, Builder<E> builder, boolean serialize) {
      String id = name.toLowerCase(Locale.ROOT);
      return ENTITIES.register(id, () -> {
         if (!serialize) {
            builder.noSave();
         }

         return builder.build("tfcflorae:" + id);
      });
   }
}
