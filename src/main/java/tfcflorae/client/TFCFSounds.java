package tfcflorae.client;
import net.minecraft.core.registries.Registries;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.dries007.tfc.util.Helpers;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.util.ForgeSoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.util.TFCFHelpers;

public final class TFCFSounds {
   public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "tfcflorae");
   // 青蛙 / 蝌蚪 / 鹦鹉音效已随实体移除；蚕蛾与鱼类（已清空）保留结构

   public static final RegistryObject<SoundEvent> BRUSHING = create("item.brushing.brushing");
   public static final ForgeSoundType MUD = createBlock("mud");
   public static final ForgeSoundType MUD_BRICKS = createBlock("mud_bricks");
   public static final ForgeSoundType PACKED_MUD = createBlock("packed_mud");
   public static final ForgeSoundType MANGROVE_ROOTS = createBlock("mangrove_roots");
   public static final ForgeSoundType MUDDY_MANGROVE_ROOTS = createBlock("muddy_mangrove_roots");
   public static final ForgeSoundType FROGLIGHT = createBlock("froglight");

   public TFCFSounds() {
   }

   public static RegistryObject<SoundEvent> create(String name) {
      return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(TFCFHelpers.identifier(name)));
   }

   public static Optional<Supplier<SoundEvent>> createOptional(String name, boolean present) {
      return Optional.ofNullable(present ? create(name) : null);
   }

   public static ForgeSoundType createBlock(String name) {
      return new ForgeSoundType(
         1.0F,
         1.0F,
         create("block.%s.break".formatted(name)),
         create("block.%s.step".formatted(name)),
         create("block.%s.place".formatted(name)),
         create("block.%s.hit".formatted(name)),
         create("block.%s.fall".formatted(name))
      );
   }

   public static TFCFSounds.EntitySound createEntity(String name, boolean attack, boolean sleep) {
      return new TFCFSounds.EntitySound(
         create("entity.%s.ambient".formatted(name)),
         create("entity.%s.death".formatted(name)),
         create("entity.%s.hurt".formatted(name)),
         create("entity.%s.step".formatted(name)),
         createOptional("entity.%s.attack".formatted(name), attack),
         createOptional("entity.%s.sleep".formatted(name), sleep)
      );
   }


   public static record EntitySound(
      Supplier<SoundEvent> ambient,
      Supplier<SoundEvent> death,
      Supplier<SoundEvent> hurt,
      Supplier<SoundEvent> step,
      Optional<Supplier<SoundEvent>> attack,
      Optional<Supplier<SoundEvent>> sleep
   ) {
      public EntitySound(Supplier<SoundEvent> ambient, Supplier<SoundEvent> death, Supplier<SoundEvent> hurt, Supplier<SoundEvent> step) {
         this(ambient, death, hurt, step, Optional.empty(), Optional.empty());
      }
   }

}
