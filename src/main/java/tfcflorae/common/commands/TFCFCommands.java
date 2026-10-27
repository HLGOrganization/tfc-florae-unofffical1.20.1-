package tfcflorae.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.function.Supplier;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraftforge.common.util.Lazy;
import tfcflorae.util.TFCFHelpers;

public final class TFCFCommands {
   public static final Supplier<SuggestionProvider<CommandSourceStack>> TFC_BIOMES = register(
      "available_biomes", (context, builder) -> SharedSuggestionProvider.suggestResource(TFCBiomes.getExtensionKeys(), builder)
   );

   public TFCFCommands() {
   }

   public static void registerSuggestionProviders() {
      TFC_BIOMES.get();
   }

   public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
      // 树木生成已整体移除，/treetfcf 一并删除。保留 /tfcflorae 根命令。
      dispatcher.register((LiteralArgumentBuilder)Commands.literal("tfcflorae"));
   }

   public static <S extends SharedSuggestionProvider> Supplier<SuggestionProvider<S>> register(String id, SuggestionProvider<SharedSuggestionProvider> provider) {
      return Lazy.of(() -> SuggestionProviders.register(TFCFHelpers.identifier(id), provider));
   }
}
