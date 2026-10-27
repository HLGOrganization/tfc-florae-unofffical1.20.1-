package tfcflorae;
import tfcflorae.common.TFCFCreativeTabs;

import com.mojang.logging.LogUtils;
import net.dries007.tfc.util.Helpers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import tfcflorae.client.ClientEventHandler;
import tfcflorae.client.TFCFSounds;
import tfcflorae.client.particle.TFCFParticles;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.commands.TFCFCommands;
import tfcflorae.common.container.TFCFContainerTypes;
import tfcflorae.common.entities.TFCFEntities;
import tfcflorae.common.entities.TFCFFaunas;
import tfcflorae.common.entities.ai.TFCFBrain;
import tfcflorae.common.fluid.TFCFFluids;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.common.recipes.TFCFRecipeSerializers;
import tfcflorae.common.recipes.TFCFRecipeTypes;
import tfcflorae.common.recipes.ingredients.TFCFIngredients;
import tfcflorae.util.TFCFDispenserBehaviors;
import tfcflorae.util.TFCFInteractionManager;
import tfcflorae.world.feature.TFCFFeatures;

@Mod("tfcflorae")
public class TFCFlorae {
   public static final String MOD_ID = "tfcflorae";
   public static final String MOD_NAME = "TFCFlorae";
   public static final String MOD_VERSION = "${version}";
   public static final Logger LOGGER = LogUtils.getLogger();

   public TFCFlorae() {
      LOGGER.info("Initializing TFC Florae");
      LOGGER.info(
         "Options: Assertions Enabled = {}, Boostrap = {}, Test = {}, Debug Logging = {}",
         new Object[]{Helpers.ASSERTIONS_ENABLED, Helpers.BOOTSTRAP_ENVIRONMENT, Helpers.TEST_ENVIRONMENT, LOGGER.isDebugEnabled()}
      );
      IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
      bus.addListener(this::setup);
      bus.addListener(TFCFEntities::onEntityAttributeCreation);
      TFCFItems.ITEMS.register(bus);
      TFCFBlocks.BLOCKS.register(bus);
      TFCFContainerTypes.CONTAINERS.register(bus);
      TFCFEntities.ENTITIES.register(bus);
      TFCFFluids.FLUIDS.register(bus);
            TFCFFluids.FLUID_TYPES.register(bus);
      TFCFRecipeTypes.RECIPE_TYPES.register(bus);
      TFCFRecipeSerializers.RECIPE_SERIALIZERS.register(bus);
      TFCFSounds.SOUNDS.register(bus);
      TFCFParticles.PARTICLE_TYPES.register(bus);
      TFCFBlockEntities.BLOCK_ENTITIES.register(bus);
      TFCFCreativeTabs.TABS.register(bus);
      // 世界生成：专用特征与竹类树装饰器；普通树使用 TFC 原生实现
      TFCFFeatures.FEATURES.register(bus);
      TFCFFeatures.LEAF_DECOR.register(bus);
      TFCFBrain.registerAll(bus);
      Config.init();
      TFCFForgeEventHandler.init();
      if (FMLEnvironment.dist == Dist.CLIENT) {
         ClientEventHandler.init();
      }
   }

   public void setup(FMLCommonSetupEvent event) {
      LOGGER.info("TFCFlorae Common Setup");
      TFCFRock.registerDefaultRocks();
      event.enqueueWork(() -> {
         TFCFIngredients.registerIngredientTypes();
         TFCFCommands.registerSuggestionProviders();
         TFCFInteractionManager.init();
         TFCFDispenserBehaviors.registerDispenserBehaviors();
         TFCFBlocks.registerFlowerPotFlowers();
         TFCFFaunas.registerSpawnPlacements();
      });
   }
}
