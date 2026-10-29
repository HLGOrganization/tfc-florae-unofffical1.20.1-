package tfcflorae.common.blockentities;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.mixin.BlockEntityTypeAccessor;

/**
 * Adds this mod's wood blocks to TFC's rotation block entity types.
 * <p>
 * TFC builds those lists from its own 20 woods:
 * <pre>register("water_wheel", WaterWheelBlockEntity::new, woodBlocks(Wood.BlockType.WATER_WHEEL))</pre>
 * A block entity is only created for a block in the list, and because
 * {@code WaterWheelBlock} uses {@code RenderShape.ENTITYBLOCK_ANIMATED} a missing block
 * entity means the block is entirely invisible.
 * <p>
 * This is a straight port of ArborFirmaCraft's fix (ModEvents#modifyBlockEntityType with
 * the BlockEntityTypeAccessor mixin): the blocks stay TFC's own block classes, so TFC's
 * block entities, tickers and renderers apply without any extra registration - only the
 * block list is extended.
 */
public final class TFCFRotationBlockEntities
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private TFCFRotationBlockEntities() {}

    public static void extendTFCBlockEntities()
    {
        modifyBlockEntityType("water_wheel", TFCBlockEntities.WATER_WHEEL.get(), BlockType.WATER_WHEEL);
        modifyBlockEntityType("axle", TFCBlockEntities.AXLE.get(), BlockType.AXLE);
        modifyBlockEntityType("bladed_axle", TFCBlockEntities.BLADED_AXLE.get(), BlockType.BLADED_AXLE);
        modifyBlockEntityType("encased_axle", TFCBlockEntities.ENCASED_AXLE.get(), BlockType.ENCASED_AXLE);
        modifyBlockEntityType("clutch", TFCBlockEntities.CLUTCH.get(), BlockType.CLUTCH);
        modifyBlockEntityType("gear_box", TFCBlockEntities.GEAR_BOX.get(), BlockType.GEAR_BOX);
        modifyBlockEntityType("windmill", TFCBlockEntities.WINDMILL.get(), BlockType.WINDMILL);
        // chests work exactly like the rotation machines: TFC's own ChestBlock plus TFC's
        // chest block entity; without this the block entity is never created and the chest
        // cannot be opened.
        modifyBlockEntityType("chest", TFCBlockEntities.CHEST.get(), BlockType.CHEST);
        modifyBlockEntityType("trapped_chest", TFCBlockEntities.TRAPPED_CHEST.get(), BlockType.TRAPPED_CHEST);
        // sluice / tool rack / farmland: TFC's classes, so TFC's block entities, renderers
        // and update logic all apply; only the block list needs our blocks
        modifyBlockEntityType("sluice", TFCBlockEntities.SLUICE.get(), BlockType.SLUICE);
        modifyBlockEntityType("tool_rack", TFCBlockEntities.TOOL_RACK.get(), BlockType.TOOL_RACK);
        // barrel and loom: TFC's classes, so TFC's GUI, data and renderers apply
        modifyBlockEntityType("barrel", TFCBlockEntities.BARREL.get(), BlockType.BARREL);
        modifyBlockEntityType("loom", TFCBlockEntities.LOOM.get(), BlockType.LOOM);
        extendTickCounter();
        extendSoilBlockEntity();
    }

    /** Drying bricks use TFC's TickCounterBlockEntity, so they go into TICK_COUNTER. */
   private static void extendTickCounter()
   {
      try
      {
         final Set<Block> extra = new HashSet<>();
         final Map<?, ?> variants = TFCFBlocks.TFCFSOIL.get(tfcflorae.common.blocks.soil.TFCFSoil.DRYING_BRICKS);
         if (variants != null)
         {
            for (Object value : variants.values())
            {
               if (value instanceof RegistryObject<?> reg && reg.get() instanceof Block block)
               {
                  extra.add(block);
               }
            }
         }
         addBlocks("tick_counter", TFCBlockEntities.TICK_COUNTER.get(), extra);
      }
      catch (Throwable t)
      {
         LOGGER.error("TFCFlorae: could not extend the TFC tick counter block entity type", t);
      }
   }

   /** Farmland blocks are registered per soil variant in TFCFBlocks.TFCFSOIL. */
   private static void extendSoilBlockEntity()
   {
      try
      {
         final Set<Block> extra = new HashSet<>();
         for (Map<?, ?> variants : TFCFBlocks.TFCFSOIL.values())
         {
            for (Object value : variants.values())
            {
               if (value instanceof RegistryObject<?> reg && reg.get() instanceof Block block)
               {
                  extra.add(block);
               }
            }
         }
         addBlocks("farmland", net.dries007.tfc.common.blockentities.TFCBlockEntities.FARMLAND.get(), extra);
      }
      catch (Throwable t)
      {
         LOGGER.error("TFCFlorae: could not extend the TFC farmland block entity type", t);
      }
   }

   private static void addBlocks(String label, BlockEntityType<?> type, Set<Block> extra)
   {
      try
      {
         final Set<Block> blocks = new HashSet<>(((BlockEntityTypeAccessor) type).accessor$getValidBlocks());
         final int before = blocks.size();
         blocks.addAll(extra);
         if (blocks.size() == before)
         {
            return;
         }
         ((BlockEntityTypeAccessor) type).accessor$setValidBlocks(blocks);
         LOGGER.info("TFCFlorae: added {} blocks to TFC {} block entity type", blocks.size() - before, label);
      }
      catch (Throwable t)
      {
         LOGGER.error("TFCFlorae: could not extend the TFC {} block entity type", label, t);
      }
   }

   private static void modifyBlockEntityType(String label, BlockEntityType<?> type, BlockType blockType)
    {
        try
        {
            final Stream<Block> extra = TFCFBlocks.WOODS.values().stream()
                .map(map -> map.get(blockType))
                .filter(java.util.Objects::nonNull)
                .map(RegistryObject::get);
            final Set<Block> blocks = new HashSet<>(((BlockEntityTypeAccessor) type).accessor$getValidBlocks());
            final int before = blocks.size();
            blocks.addAll(extra.toList());
            if (blocks.size() == before)
            {
                return;
            }
            ((BlockEntityTypeAccessor) type).accessor$setValidBlocks(blocks);
            LOGGER.info("TFCFlorae: added {} blocks to TFC {} block entity type", blocks.size() - before, label);
        }
        catch (Throwable t)
        {
            LOGGER.error("TFCFlorae: could not extend the TFC {} block entity type", label, t);
        }
    }
}
