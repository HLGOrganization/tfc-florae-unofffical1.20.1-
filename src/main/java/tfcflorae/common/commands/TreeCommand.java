package tfcflorae.common.commands;
import net.minecraft.core.registries.Registries;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.server.command.EnumArgument;
import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.world.feature.tree.TFCFTreeGrower;

public final class TreeCommand {
   public TreeCommand() {
   }

   public static LiteralArgumentBuilder<CommandSourceStack> create() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)Commands.literal("treetfcf").requires(source -> source.hasPermission(2)))
         .then(
            Commands.argument("pos", BlockPosArgument.blockPos())
               .then(
                  ((RequiredArgumentBuilder)Commands.argument("wood", EnumArgument.enumArgument(TFCFWood.class))
                        .then(
                           Commands.argument("variant", EnumArgument.enumArgument(TreeCommand.Variant.class))
                              .executes(
                                 context -> placeTree(
                                       ((CommandSourceStack)context.getSource()).getLevel(),
                                       BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                       (TFCFWood)context.getArgument("wood", TFCFWood.class),
                                       (TreeCommand.Variant)context.getArgument("variant", TreeCommand.Variant.class)
                                    )
                              )
                        ))
                     .executes(
                        context -> placeTree(
                              ((CommandSourceStack)context.getSource()).getLevel(),
                              BlockPosArgument.getLoadedBlockPos(context, "pos"),
                              (TFCFWood)context.getArgument("wood", TFCFWood.class),
                              TreeCommand.Variant.NORMAL
                           )
                     )
               )
         );
   }

   private static int placeTree(ServerLevel world, BlockPos pos, TFCFWood wood, TreeCommand.Variant variant) {
      TFCFTreeGrower tree = wood.tree();
      Registry<ConfiguredFeature<?, ?>> registry = world.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
      ConfiguredFeature<?, ?> feature = variant == TreeCommand.Variant.NORMAL ? tree.getNormalFeature(registry) : tree.getOldGrowthFeature(registry);
      feature.place(world, world.getChunkSource().getGenerator(), world.getRandom(), pos);
      return 1;
   }

   private static enum Variant {
      NORMAL,
      LARGE;

      private Variant() {
      }
   }
}
