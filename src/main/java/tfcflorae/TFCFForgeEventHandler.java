package tfcflorae;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nonnull;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.LooseRockBlock;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.blocks.soil.Colors;
import tfcflorae.common.blocks.soil.TFCFRockSand;
import tfcflorae.common.blocks.soil.TFCFRockSoil;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.common.commands.TFCFCommands;
import tfcflorae.common.items.TFCFItems;

public class TFCFForgeEventHandler {
   private static final Logger LOGGER = LogUtils.getLogger();

   public TFCFForgeEventHandler() {
   }

   public static void init() {
      IEventBus bus = MinecraftForge.EVENT_BUS;
      IEventBus busMod = FMLJavaModLoadingContext.get().getModEventBus();
      bus.addListener(TFCFForgeEventHandler::onItemExpireEvent);
      bus.addListener(TFCFForgeEventHandler::onPlayerRightClickBlock);
      bus.addListener(TFCFForgeEventHandler::registerCommands);
      busMod.addListener(TFCFForgeEventHandler::onPackFinder);
   }

   public static void onPackFinder(AddPackFindersEvent event) {
      // 1.18.2 built a bespoke PathResourcePack here so the mod could override its own assets.
      // Forge 1.20.1 replaced that with AddPackFindersEvent#addPackFinders, and
      // PathResourcePack is no longer public API. Route B ships the mod's resources directly
      // instead of layering an override pack, so there is nothing to inject.
   }

   public static void registerCommands(RegisterCommandsEvent event) {
      LOGGER.debug("Registering TFC Florae Commands");
      TFCFCommands.registerCommands(event.getDispatcher());
   }

   public static void onItemExpireEvent(ItemExpireEvent event) {
      if (event.getEntity().isAlive()) {
         ItemEntity item = event.getEntity();
         ItemStack itemStack = item.getItem();
         Level level = item.level();
         BlockPos pos = item.blockPosition();
         if ((level.getBlockState(pos).isAir() || Helpers.isBlock(level.getBlockState(pos), TFCFTags.Blocks.SAPLING_CAN_REPLACE))
            && (
               Helpers.isItem(itemStack, TFCFTags.Items.SAPLINGS)
                  || Block.byItem(itemStack.getItem()) instanceof TFCSaplingBlock
            )) {
            Block block = Block.byItem(itemStack.getItem());
            if (block != null && block != Blocks.AIR && block.defaultBlockState().canSurvive(level, pos) && level.setBlockAndUpdate(pos, block.defaultBlockState())) {
               itemStack.shrink(1);
               event.getEntity().setItem(itemStack);
               if (event.getEntity().getItem().isEmpty()) {
                  event.getEntity().remove(RemovalReason.KILLED);
               } else {
                  event.setExtraLife(3000);
                  event.setCanceled(true);
               }
            }
         }
      }
   }

   public static void onPlayerRightClickBlock(RightClickBlock event) {
      Level level = event.getLevel();
      BlockPos pos = event.getPos();
      BlockState state = level.getBlockState(pos);
      Block block = state.getBlock();

      // Create's tree fertilizer grows trees in a temporary PlacementSimulationServerLevel.
      // TFC's tree features need the real ServerLevel (and its chunk data/template manager),
      // so that simulation can capture only the first trunk block. Handle TFC saplings here
      // before Create's item useOn path and grow directly in the actual world.
      if (level instanceof ServerLevel server
         && BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString().equals("create:tree_fertilizer")
         && block instanceof BonemealableBlock bonemealable
         && block instanceof TFCSaplingBlock) {
         bonemealable.performBonemeal(server, server.random, pos, state);
         if (event.getEntity() != null && !event.getEntity().isCreative()) {
            event.getItemStack().shrink(1);
         }
         event.setCancellationResult(InteractionResult.SUCCESS);
         event.setCanceled(true);
         return;
      }

      for (Variant variant : Variant.values()) {
         // packed_mud 不再注册，泥巴铲平的转换一并去掉
         if (block == ((RegistryObject)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant)).get()
            && event.getHand() == InteractionHand.MAIN_HAND
            && event.getItemStack().canPerformAction(ToolActions.SHOVEL_FLATTEN)) {
            level.setBlockAndUpdate(pos, ((Block)TFCFBlocks.TFCSOIL.get(TFCFSoil.COMPACT_DIRT).get(variant).get()).defaultBlockState());
            event.setCancellationResult(InteractionResult.SUCCESS);
         }
      }

      for (TFCFSoil.TFCFVariant variant : TFCFSoil.TFCFVariant.values()) {
         if (block == TFCFBlocks.TFCFSOIL.get(TFCFSoil.DIRT).get(variant).get()
            && event.getHand() == InteractionHand.MAIN_HAND
            && event.getItemStack().canPerformAction(ToolActions.SHOVEL_FLATTEN)) {
            level.setBlockAndUpdate(pos, ((Block)TFCFBlocks.TFCFSOIL.get(TFCFSoil.COMPACT_DIRT).get(variant).get()).defaultBlockState());
            event.setCancellationResult(InteractionResult.SUCCESS);
         }
      }

      for (Rock rockTFC : Rock.values()) {
         for (Variant variantx : Variant.values()) {
            if (block == ((RegistryObject)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variantx)).get()
               && Helpers.isItem(
                  event.getItemStack(), ((Block)((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(rockTFC)).get(BlockType.LOOSE)).get()).asItem()
               )) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.TFCROCKSOIL.get(TFCFRockSoil.ROCKIER_COMPACT_DIRT).get(variantx).get(rockTFC).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }

            if (block == net.minecraft.world.level.block.Blocks.AIR
               && Helpers.isItem(event.getItemStack(), (Item)TFCFItems.SOIL_PILE_TFC.get(variantx).get())) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.TFCROCKSOIL.get(TFCFRockSoil.ROCKIER_COMPACT_DIRT).get(variantx).get(rockTFC).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }
         }

         for (TFCFSoil.TFCFVariant variantx : TFCFSoil.TFCFVariant.values()) {
            if (block == TFCFBlocks.TFCFSOIL.get(TFCFSoil.DIRT).get(variantx).get()
               && Helpers.isItem(
                  event.getItemStack(), ((Block)((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(rockTFC)).get(BlockType.LOOSE)).get()).asItem()
               )) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.TFCFROCKSOIL.get(TFCFRockSoil.ROCKIER_COMPACT_DIRT).get(variantx).get(rockTFC).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }

            if (block == net.minecraft.world.level.block.Blocks.AIR
               && Helpers.isItem(event.getItemStack(), (Item)TFCFItems.SOIL_PILE_TFCF.get(variantx).get())) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.TFCFROCKSOIL.get(TFCFRockSoil.ROCKIER_COMPACT_DIRT).get(variantx).get(rockTFC).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }
         }

         for (Colors sandColor : Colors.values()) {
            if (block != ((RegistryObject)TFCBlocks.SAND.get(sandColor.toSandTFC(true))).get() && !isTfcfSandBlock(sandColor, block)) {
               if (block == net.minecraft.world.level.block.Blocks.AIR
                  && isSandLayerItem(sandColor, event.getItemStack())) {
                  event.getItemStack().shrink(1);
                  BlockState placedBlock = ((Block)TFCFBlocks.ROCKY_SAND_TFC.get(TFCFRockSand.SANDY_TILES).get(sandColor).get(rockTFC).get()).defaultBlockState();
                  level.setBlockAndUpdate(pos, placedBlock);
                  Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
                  event.setCancellationResult(InteractionResult.SUCCESS);
                  break;
               }
            } else if (Helpers.isItem(
               event.getItemStack(), ((Block)((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(rockTFC)).get(BlockType.LOOSE)).get()).asItem()
            )) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.ROCKY_SAND_TFC.get(TFCFRockSand.ROCKY).get(sandColor).get(rockTFC).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }
         }
      }

      for (TFCFRock rockTFCF : TFCFRock.values()) {
         for (Variant variantx : Variant.values()) {
            if (block == ((RegistryObject)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variantx)).get()
               && Helpers.isItem(event.getItemStack(), ((Block)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(BlockType.LOOSE).get()).asItem())) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.TFCROCKSOIL2.get(TFCFRockSoil.ROCKIER_COMPACT_DIRT).get(variantx).get(rockTFCF).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }

            if (block == net.minecraft.world.level.block.Blocks.AIR
               && Helpers.isItem(event.getItemStack(), (Item)TFCFItems.SOIL_PILE_TFC.get(variantx).get())) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.TFCROCKSOIL2.get(TFCFRockSoil.ROCKIER_COMPACT_DIRT).get(variantx).get(rockTFCF).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }
         }

         for (TFCFSoil.TFCFVariant variantx : TFCFSoil.TFCFVariant.values()) {
            if (block == TFCFBlocks.TFCFSOIL.get(TFCFSoil.DIRT).get(variantx).get()
               && Helpers.isItem(event.getItemStack(), ((Block)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(BlockType.LOOSE).get()).asItem())) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.TFCFROCKSOIL2.get(TFCFRockSoil.ROCKIER_COMPACT_DIRT).get(variantx).get(rockTFCF).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }

            if (block == net.minecraft.world.level.block.Blocks.AIR
               && Helpers.isItem(event.getItemStack(), (Item)TFCFItems.SOIL_PILE_TFCF.get(variantx).get())) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.TFCFROCKSOIL2.get(TFCFRockSoil.ROCKIER_COMPACT_DIRT).get(variantx).get(rockTFCF).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }
         }

         for (Colors sandColorx : Colors.values()) {
            if (block != ((RegistryObject)TFCBlocks.SAND.get(sandColorx.toSandTFC(true))).get() && !isTfcfSandBlock(sandColorx, block)) {
               if (block == net.minecraft.world.level.block.Blocks.AIR
                  && isSandLayerItem(sandColorx, event.getItemStack())) {
                  event.getItemStack().shrink(1);
                  BlockState placedBlock = ((Block)TFCFBlocks.ROCKY_SAND_TFCF.get(TFCFRockSand.SANDY_TILES).get(sandColorx).get(rockTFCF).get()).defaultBlockState();
                  level.setBlockAndUpdate(pos, placedBlock);
                  Helpers.playSound(level, pos, SoundType.ROOTS.getPlaceSound());
                  event.setCancellationResult(InteractionResult.SUCCESS);
                  break;
               }
            } else if (Helpers.isItem(event.getItemStack(), ((Block)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(BlockType.LOOSE).get()).asItem())) {
               event.getItemStack().shrink(1);
               BlockState placedBlock = ((Block)TFCFBlocks.ROCKY_SAND_TFCF.get(TFCFRockSand.ROCKY).get(sandColorx).get(rockTFCF).get()).defaultBlockState();
               level.setBlockAndUpdate(pos, placedBlock);
               Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
               event.setCancellationResult(InteractionResult.SUCCESS);
               break;
            }
         }
      }

      event.setCancellationResult(InteractionResult.PASS);
   }

   private static boolean isTfcfSandBlock(Colors color, Block block) {
      RegistryObject<Block> ref = (RegistryObject<Block>)TFCFBlocks.SAND.get(color);
      return ref != null && block == (Block)ref.get();
   }

   private static boolean isSandLayerItem(Colors color, ItemStack stack) {
      RegistryObject<Block> ref = (RegistryObject<Block>)TFCFBlocks.SAND_LAYERS.get(color);
      return ref != null && Helpers.isItem(stack, ((Block)ref.get()).asItem());
   }
}
