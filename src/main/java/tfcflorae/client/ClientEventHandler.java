package tfcflorae.client;
import net.minecraftforge.client.event.ModelEvent;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;
import net.dries007.tfc.client.TFCColors;
import net.dries007.tfc.client.render.blockentity.AnvilBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.BarrelBlockEntityRenderer;
import net.dries007.tfc.client.render.blockentity.SluiceBlockEntityRenderer;
import net.dries007.tfc.client.screen.KnappingScreen;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockCategory.ItemType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.LecternRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.config.ModConfigEvent.Reloading;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.client.model.entity.RaftModel;
import tfcflorae.client.model.entity.SilkmothModel;
import tfcflorae.client.particle.FallingLeafParticle;
import tfcflorae.client.particle.SporeParticle;
import tfcflorae.client.particle.TFCFParticles;
import tfcflorae.client.particle.WaterFlowParticle;
import tfcflorae.client.render.blockentity.MineralSheetBlockEntityRenderer;
import tfcflorae.client.render.entity.CodLikeRenderer;
import tfcflorae.client.render.entity.SalmonLikeRenderer;
import tfcflorae.client.render.entity.TFCFSimpleMobRenderer;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.Mineral;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.common.blocks.wood.TFCFWood;
import tfcflorae.common.container.TFCFContainerTypes;
import tfcflorae.common.entities.Silkmoth;
import tfcflorae.common.entities.TFCFEntities;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.util.TFCFHelpers;

public class ClientEventHandler {
   public ClientEventHandler() {
   }

   public static void init() {
      IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
      bus.addListener(ClientEventHandler::clientSetup);
      bus.addListener(ClientEventHandler::onConfigReload);
      bus.addListener(ClientEventHandler::registerModelLoaders);
      bus.addListener(ClientEventHandler::registerColorHandlerBlocks);
      bus.addListener(ClientEventHandler::registerColorHandlerItems);
      bus.addListener(ClientEventHandler::registerParticleFactories);
      bus.addListener(ClientEventHandler::registerClientReloadListeners);
      bus.addListener(ClientEventHandler::registerEntityRenderers);
      bus.addListener(ClientEventHandler::registerLayerDefinitions);
      // 1.20.1: the TextureStitchEvent was replaced by atlas source JSON, so no listener.
   }

   public static void clientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(
         () -> {
            // 打制界面统一成一个菜单（与 TFC 一致），具体材质由 KnappingType 决定
            MenuScreens.register((MenuType)TFCFContainerTypes.KNAPPING.get(), KnappingScreen::new);
            TFCFBlocks.WOODS
               .values()
               .forEach(
                  map -> ItemProperties.register(
                        ((Block)map.get(BlockType.BARREL).get()).asItem(),
                        Helpers.identifier("sealed"),
                        (stack, level, entity, unused) -> stack.hasTag() ? 1.0F : 0.0F
                     )
               );
         }
      );
      RenderType solid = RenderType.solid();
      RenderType cutout = RenderType.cutout();
      RenderType cutoutMipped = RenderType.cutoutMipped();
      RenderType translucent = RenderType.translucent();
      TFCFBlocks.WOODS
         .values()
         .forEach(
            map -> {
               Stream.of(
                     BlockType.SAPLING,
                     BlockType.DOOR,
                     BlockType.TRAPDOOR,
                     BlockType.FENCE,
                     BlockType.FENCE_GATE,
                     BlockType.BUTTON,
                     BlockType.PRESSURE_PLATE,
                     BlockType.SLAB,
                     BlockType.STAIRS,
                     BlockType.TWIG,
                     BlockType.BARREL,
                     BlockType.SCRIBING_TABLE,
                     BlockType.SEWING_TABLE,
                     BlockType.JAR_SHELF,
                     BlockType.POTTED_SAPLING,
                     // these three carry the镂空 axle casing overlay (axle_casing*.png is
                     // ~70% transparent); without cutout the transparent part renders black,
                     // which made the placed gear box / clutch / encased axle look wrong.
                     // TFC puts the same three in its cutout list.
                     BlockType.ENCASED_AXLE,
                     BlockType.CLUTCH,
                     BlockType.GEAR_BOX
                  )
                  .forEach(type -> ItemBlockRenderTypes.setRenderLayer((Block)((RegistryObject)map.get(type)).get(), cutout));
               Stream.of(BlockType.FALLEN_LEAVES)
                  .forEach(
                     type -> ItemBlockRenderTypes.setRenderLayer(
                           (Block)((RegistryObject)map.get(type)).get(), layer -> Minecraft.useFancyGraphics() ? layer == cutoutMipped : layer == solid
                        )
                  );
            }
         );
      TFCFBlocks.WOODS
         .forEach(
            (key, value) -> {
               // 所有木材的树叶都必须设为 cutoutMipped。
               // 否则贴图里的透明像素会被当作不透明渲染，镶空处显示为黑色。
               // 原先这里用 !isFruitTree() && !isMangrove() 把果树/红树排除在外，
               // 导致嘉宝树等果树的树叶镶空处发黑。
               if (value.get(BlockType.LEAVES) != null) {
                  ItemBlockRenderTypes.setRenderLayer((Block)((RegistryObject)value.get(BlockType.LEAVES)).get(), cutoutMipped);
               }
            }
         );
      TFCFBlocks.LEAVES_ONLY
         .values()
         .forEach(
            map -> Stream.of(BlockType.LEAVES)
                  .forEach(
                     type -> ItemBlockRenderTypes.setRenderLayer(
                           (Block)map.get(), layer -> Minecraft.useFancyGraphics() ? layer == cutoutMipped : layer == cutoutMipped
                        )
                  )
         );
      TFCFBlocks.WOODS_SEASONAL_LEAVES
         .values()
         .forEach(
            map -> Stream.of(BlockType.LEAVES)
                  .forEach(
                     type -> ItemBlockRenderTypes.setRenderLayer(
                           (Block)map.get(), layer -> Minecraft.useFancyGraphics() ? layer == cutoutMipped : layer == cutoutMipped
                        )
                  )
         );
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.CHARRED_TREE_TWIG.get(), cutout);
      TFCFBlocks.MANGROVE_ROOTS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.CLAY_LARGE_VESSELS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.PODZOL).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL
         .get(TFCFSoil.SPARSE_EARTHENWARE_CLAY_GRASS)
         .values()
         .forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL
         .get(TFCFSoil.DENSE_EARTHENWARE_CLAY_GRASS)
         .values()
         .forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.PODZOL).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL
         .get(TFCFSoil.SPARSE_EARTHENWARE_CLAY_GRASS)
         .values()
         .forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL
         .get(TFCFSoil.DENSE_EARTHENWARE_CLAY_GRASS)
         .values()
         .forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL
         .get(TFCFSoil.SPARSE_STONEWARE_CLAY_GRASS)
         .values()
         .forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.SPARSE_BOG_IRON_GRASS.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.DENSE_BOG_IRON_GRASS.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.BOG_IRON_GRASS.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.ROOTED_BOG_IRON.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.MYCELIUM_BOG_IRON.get(), cutout);
      TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.ROOTED_DIRT).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.MYCELIUM_DIRT).values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.JOSHUA_TRUNK.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.JOSHUA_LEAVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.WOODS_SEASONAL_LEAVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.ROOT_SPIKES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.PLANTS.forEach((key, reg) -> {
         if (key.isVine()) {
            ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), translucent);
         } else {
            ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout);
         }
      });
      TFCFBlocks.FRUITING_PLANTS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.POTTED_PLANTS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      // 凝灰岩的岩石表是空表（整套方块已移除），取 SPIKE/AQUEDUCT 会拿到 null，必须判空
      TFCFBlocks.TFCF_ROCK_BLOCKS.values().forEach(map -> {
         java.util.Set.of(net.dries007.tfc.common.blocks.rock.Rock.BlockType.SPIKE, net.dries007.tfc.common.blocks.rock.Rock.BlockType.AQUEDUCT)
            .forEach(type -> {
               RegistryObject<Block> reg = map.get(type);
               if (reg != null) {
                  ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout);
               }
            });
      });
      TFCFBlocks.ROCK_BLOCKS.values().forEach(map -> {
      });
      TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.values().forEach(map -> {
      });
      TFCFBlocks.DRIPSTONE_BLOCKS.values().forEach(map -> {
         RegistryObject<Block> reg = map.get(net.dries007.tfc.common.blocks.rock.Rock.BlockType.SPIKE);
         if (reg != null) {
            ItemBlockRenderTypes.setRenderLayer(reg.get(), cutout);
         }
      });
      TFCFBlocks.GEYSER_TFC.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.GEYSER_TFCF.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      // 矿石相关表按岩石过滤后，个别键可能整块缺失（例如凝灰岩不生成矿石）；
      // 下面每层遍历都先判空，避免空表 / 缺失键导致 NPE。
      TFCFBlocks.ORES.values().forEach(map -> {
         if (map == null) {
            return;
         }
         map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      });
      TFCFBlocks.GRADED_ORES.values().forEach(map -> {
         if (map == null) {
            return;
         }
         map.values().forEach(inner -> {
            if (inner == null) {
               return;
            }
            inner.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
         });
      });
      TFCFBlocks.ORE_DEPOSITS.values().forEach(map -> {
         if (map == null) {
            return;
         }
         map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      });
      TFCFBlocks.ORES_TFC_ROCK.values().forEach(map -> {
         if (map == null) {
            return;
         }
         map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      });
      TFCFBlocks.GRADED_ORES_TFC_ROCK.values().forEach(map -> {
         if (map == null) {
            return;
         }
         map.values().forEach(inner -> {
            if (inner == null) {
               return;
            }
            inner.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
         });
      });
      TFCFBlocks.ORES_TFCF_ROCK.values().forEach(map -> {
         if (map == null) {
            return;
         }
         map.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      });
      TFCFBlocks.GRADED_ORES_TFCF_ROCK.values().forEach(map -> {
         if (map == null) {
            return;
         }
         map.values().forEach(inner -> {
            if (inner == null) {
               return;
            }
            inner.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
         });
      });
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.GLOWSTONE.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.GLOWSTONE_BUDDING.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.GLOWSTONE_CLUSTER.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.LARGE_GLOWSTONE_BUD.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.MEDIUM_GLOWSTONE_BUD.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.SMALL_GLOWSTONE_BUD.get(), cutout);
      TFCFBlocks.CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.BUDDING_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.CLUSTER_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.LARGE_BUD_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.MEDIUM_BUD_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.SMALL_BUD_CRYSTAL.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.SPIDER_EGG.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.SPIDER_EGGS.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.LARGE_SPIDER_EGG.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.WEBBED_GLOW_BLOCK.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.WEBBED_TORCH_BLOCK.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.WEBBED_CHEST.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.CREEPING_WEBS.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.HANGING_SPIDER_WEB_SLENDER.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.HANGING_SPIDER_WEB_THICK.get(), cutout);
      TFCFBlocks.SPARSE_SAND_GRASS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.DENSE_SAND_GRASS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.SAND_GRASS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.BAMBOO_LOGS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.STRIPPED_BAMBOO_LOGS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
      TFCFBlocks.BAMBOO_LEAVES.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      TFCFBlocks.BAMBOO_SAPLINGS.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutoutMipped));
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.VANILLA_BAMBOO_LOGS.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.VANILLA_STRIPPED_BAMBOO_LOGS.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.MINERAL_SHEET.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.SILKMOTH_NEST.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.OCHRE_FROGLIGHT.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.VERDANT_FROGLIGHT.get(), cutout);
      ItemBlockRenderTypes.setRenderLayer((Block)TFCFBlocks.PEARLESCENT_FROGLIGHT.get(), cutout);
      TFCFBlocks.GROUNDCOVER.values().forEach(reg -> ItemBlockRenderTypes.setRenderLayer((Block)reg.get(), cutout));
   }

   public static void registerEntityRenderers(RegisterRenderers event) {
      // 1.18.2 shipped a custom boat renderer; the 1.20.1 rewrite was deferred, so the mod's
      // boats fall back to the vanilla renderer (custom visuals only, no behaviour loss).
      for (TFCFWood wood : TFCFWood.VALUES) {
         event.registerEntityRenderer((EntityType) TFCFEntities.BOATS.get(wood).get(),
            ctx -> new net.minecraft.client.renderer.entity.BoatRenderer(ctx, false));
      }

      event.registerEntityRenderer(
         (EntityType)TFCFEntities.SILKMOTH.get(),
         ctx -> new TFCFSimpleMobRenderer<Silkmoth, SilkmothModel>(ctx, new SilkmothModel(ctx.bakeLayer(TFCFRenderHelpers.modelIdentifier("silk_moth"))), "silk_moth", 0.5F, false, 1.0F, true, false, Silkmoth::getTextureLocation)
      );
      // 青蛙 / 蝌蚪 / 鹦鹉的渲染器已随实体移除
      // 淡水鱼的渲染器已随鱼种一起移除（Fish 枚举已清空，无实体注册）
            // 告示牌与工具架的渲染器在 1.20.1 移植时被搁置（见下方已清理的 TODO），
      // 导致告示牌文字/板材与工具架上的工具都不显示。此处补回。
      event.registerBlockEntityRenderer(TFCFBlockEntities.SIGN.get(), SignRenderer::new);
      event.registerBlockEntityRenderer(TFCFBlockEntities.TOOL_RACK.get(),
         ctx -> new net.dries007.tfc.client.render.blockentity.ToolRackBlockEntityRenderer());
      event.registerBlockEntityRenderer(TFCFBlockEntities.LECTERN.get(), LecternRenderer::new);
      event.registerBlockEntityRenderer(TFCFBlockEntities.SLUICE.get(), ctx -> new SluiceBlockEntityRenderer());
      event.registerBlockEntityRenderer(TFCFBlockEntities.MINERAL_SHEET.get(), ctx -> new MineralSheetBlockEntityRenderer());
   }

   public static void registerLayerDefinitions(RegisterLayerDefinitions event) {
      // 1.20.1 起船沿用原版 BoatRenderer（见 registerEntityRenderers 的说明），
      // 因此不需要为每木材注册独立的船体层定义；signLayer 由 SignRenderer 自行创建。
      // 保留这些调用无副作用，但为清晰起见不在此重复注册。


      event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("silk_moth"), SilkmothModel::createBodyLayer);
      // 青蛙 / 蝌蚪 / 鹦鹉的模型层已随实体移除
      event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("largemouth_bass"), SalmonModel::createBodyLayer);
      event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("smallmouth_bass"), SalmonModel::createBodyLayer);
      event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("lake_trout"), SalmonModel::createBodyLayer);
      event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("rainbow_trout"), SalmonModel::createBodyLayer);
      event.registerLayerDefinition(TFCFRenderHelpers.modelIdentifier("crappie"), CodModel::createBodyLayer);
   }

   public static void onConfigReload(Reloading event) {
   }

   public static void registerModelLoaders(ModelEvent.RegisterAdditional event) {
      // 船体自定义模型加载器已随原版 BoatRenderer 方案移除，此处无需额外加载。
   }

   public static void registerColorHandlerBlocks(net.minecraftforge.client.event.RegisterColorHandlersEvent.Block event) {
      BlockColors registry = event.getBlockColors();
      BlockColor grassColor = (state, worldIn, pos, tintIndex) -> TFCColors.getGrassColor(pos, tintIndex);
      BlockColor tallGrassColor = (state, worldIn, pos, tintIndex) -> TFCColors.getTallGrassColor(pos, tintIndex);
      BlockColor foliageColor = (state, worldIn, pos, tintIndex) -> TFCColors.getFoliageColor(pos, tintIndex);
      BlockColor seasonalFoliageColor = (state, worldIn, pos, tintIndex) -> TFCColors.getSeasonalFoliageColor(pos, tintIndex, 0);
      TFCFBlocks.TFCSOIL.get(TFCFSoil.PODZOL).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_GRASS).values().forEach(reg -> registry.register(tallGrassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DRY_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.PODZOL).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_GRASS).values().forEach(reg -> registry.register(tallGrassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_EARTHENWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRY_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.SPARSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.TFCFSOIL.get(TFCFSoil.DENSE_STONEWARE_CLAY_GRASS).values().forEach(reg -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      registry.register(grassColor, new Block[]{(Block)TFCFBlocks.SPARSE_BOG_IRON_GRASS.get()});
      registry.register(grassColor, new Block[]{(Block)TFCFBlocks.DENSE_BOG_IRON_GRASS.get()});
      registry.register(grassColor, new Block[]{(Block)TFCFBlocks.BOG_IRON_GRASS.get()});
      TFCFBlocks.WOODS
         .forEach(
            (wood, reg) -> {
               // 所有木材的树叶与落叶都需要 tint。
               // 原先果树/红树只给落叶上色，树叶没有 BlockColor，
               // 而贴图是灰度的，于是显示成灰白。
               final BlockColor color = wood.isConifer() ? foliageColor : seasonalFoliageColor;
               if (reg.get(BlockType.LEAVES) != null) {
                  registry.register(color, new Block[]{(Block)((RegistryObject)reg.get(BlockType.LEAVES)).get()});
               }
               if (reg.get(BlockType.FALLEN_LEAVES) != null) {
                  registry.register(color, new Block[]{(Block)((RegistryObject)reg.get(BlockType.FALLEN_LEAVES)).get()});
               }
            }
         );
      TFCFBlocks.LEAVES_ONLY
         .forEach((wood, reg) -> registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, new Block[]{(Block)reg.get(), (Block)reg.get()}));
      TFCFBlocks.JOSHUA_LEAVES
         .forEach((wood, reg) -> registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, new Block[]{(Block)reg.get(), (Block)reg.get()}));
      TFCFBlocks.WOODS_SEASONAL_LEAVES
         .forEach((wood, reg) -> registry.register(wood.isConifer() ? foliageColor : seasonalFoliageColor, new Block[]{(Block)reg.get(), (Block)reg.get()}));
      TFCFBlocks.PLANTS
         .forEach(
            (plant, reg) -> registry.register(
                  plant.isConifer()
                     ? foliageColor
                     : (plant.isTallGrass() ? tallGrassColor : (plant.isSeasonal() ? seasonalFoliageColor : (plant.isFoliage() ? foliageColor : grassColor))),
                  new Block[]{(Block)reg.get()}
               )
         );
      TFCFBlocks.FRUITING_PLANTS
         .forEach(
            (plant, reg) -> registry.register(
                  plant.isConifer()
                     ? foliageColor
                     : (plant.isTallGrass() ? tallGrassColor : (plant.isSeasonal() ? seasonalFoliageColor : (plant.isFoliage() ? foliageColor : grassColor))),
                  new Block[]{(Block)reg.get()}
               )
         );
      TFCFBlocks.POTTED_PLANTS.forEach((plant, reg) -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.SPARSE_SAND_GRASS.forEach((grass, reg) -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.DENSE_SAND_GRASS.forEach((grass, reg) -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.SAND_GRASS.forEach((grass, reg) -> registry.register(grassColor, new Block[]{(Block)reg.get()}));
      TFCFBlocks.BAMBOO_LEAVES.forEach((plant, reg) -> registry.register(seasonalFoliageColor, new Block[]{(Block)reg.get()}));
   }

   public static void registerColorHandlerItems(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
      ItemColors registry = event.getItemColors();
      // 与 TFC 原版一致：物品图标一律用 getFoliageColor / getGrassColor（坐标传 null 即可）。
      // 之前对非针叶树叶使用了季节气调色板的查询接口，而那个接口依赖真实坐标，
      // 物品上下文没有坐标，取不到季节数据，图标就渲染成灰白。
      ItemColor grassColor = (stack, tintIndex) -> TFCColors.getGrassColor(null, tintIndex);
      ItemColor foliageColor = (stack, tintIndex) -> TFCColors.getFoliageColor(null, tintIndex);
      TFCFBlocks.WOODS.forEach((wood, value) -> {
         if (value.get(BlockType.FALLEN_LEAVES) != null) {
            registry.register(foliageColor, new ItemLike[]{(ItemLike)value.get(BlockType.FALLEN_LEAVES).get()});
         }
         if (value.get(BlockType.LEAVES) != null) {
            registry.register(foliageColor, new ItemLike[]{(ItemLike)value.get(BlockType.LEAVES).get()});
         }
      });
      TFCFBlocks.LEAVES_ONLY.forEach((key, value) -> registry.register(foliageColor, new ItemLike[]{(ItemLike)value.get(), (ItemLike)value.get()}));
      TFCFBlocks.JOSHUA_LEAVES.forEach((key, value) -> registry.register(foliageColor, new ItemLike[]{(ItemLike)value.get(), (ItemLike)value.get()}));
      TFCFBlocks.WOODS_SEASONAL_LEAVES.forEach((key, value) -> registry.register(foliageColor, new ItemLike[]{(ItemLike)value.get(), (ItemLike)value.get()}));
      TFCFBlocks.PLANTS.forEach((plant, reg) -> {
         if (plant.isItemTinted()) {
            registry.register(plant.isSeasonal() ? foliageColor : grassColor, new ItemLike[]{(ItemLike)reg.get()});
         }
      });
      TFCFBlocks.FRUITING_PLANTS.forEach((plant, reg) -> {
         if (plant.isItemTinted()) {
            registry.register(plant.isSeasonal() ? foliageColor : grassColor, new ItemLike[]{(ItemLike)reg.get()});
         }
      });
      TFCFBlocks.BAMBOO_LEAVES.forEach((plant, reg) -> registry.register(foliageColor, new ItemLike[]{(ItemLike)reg.get()}));
   }

   public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
   }

   public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
      ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
      particleEngine.register((ParticleType)TFCFParticles.WATER_FLOW.get(), WaterFlowParticle.Provider::new);
      particleEngine.register((ParticleType)TFCFParticles.FALLING_LEAF.get(), set -> new FallingLeafParticle.Provider(set, true));
      particleEngine.register((ParticleType)TFCFParticles.FALLING_SPORE.get(), set -> new SporeParticle.Provider(set, true));
   }

   // TODO(resources): 1.20.1 removed TextureStitchEvent; chest/sign sprites are now
   // declared in the atlas source files (assets/minecraft/atlases/blocks.json).
}
