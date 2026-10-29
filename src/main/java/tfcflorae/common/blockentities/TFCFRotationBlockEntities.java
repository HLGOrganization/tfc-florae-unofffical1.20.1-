package tfcflorae.common.blockentities;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import tfcflorae.common.blocks.TFCFBlocks;

/**
 * TFC registers its rotation block entities with a block list built from its own 20
 * woods:
 * <pre>register("water_wheel", WaterWheelBlockEntity::new, woodBlocks(Wood.BlockType.WATER_WHEEL))</pre>
 * A block entity is only created for a block in that list, so machines built from this
 * mod's woods got none - and because WaterWheelBlock reports
 * {@code RenderShape.ENTITYBLOCK_ANIMATED}, a missing block entity means the block is
 * completely invisible, since nothing renders the model.
 * <p>
 * ArborFirmaCraft solves the same problem with
 * {@code @Mixin(BlockEntityType.class) interface BlockEntityTypeAccessor
 * { @Accessor("validBlocks") @Mutable void setValidBlocks(Set<Block>); }}.  This project
 * ships no mixins, so the same field replacement is done with sun.misc.Unsafe, which
 * ignores the final modifier.  Blocks stay TFC's own classes, so TFC's block entities and
 * renderers apply without any extra registration.
 */
public final class TFCFRotationBlockEntities
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Object unsafe;
    private static long validBlocksOffset;

    private TFCFRotationBlockEntities() {}

    public static void extendTFCBlockEntities()
    {
        if (!init()) return;
        patch("water_wheel", TFCBlockEntities.WATER_WHEEL, BlockType.WATER_WHEEL);
        patch("axle", TFCBlockEntities.AXLE, BlockType.AXLE);
        patch("bladed_axle", TFCBlockEntities.BLADED_AXLE, BlockType.BLADED_AXLE);
        patch("encased_axle", TFCBlockEntities.ENCASED_AXLE, BlockType.ENCASED_AXLE);
        patch("clutch", TFCBlockEntities.CLUTCH, BlockType.CLUTCH);
        patch("gear_box", TFCBlockEntities.GEAR_BOX, BlockType.GEAR_BOX);
        patch("windmill", TFCBlockEntities.WINDMILL, BlockType.WINDMILL);
    }

    private static boolean init()
    {
        if (unsafe != null) return true;
        try
        {
            final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            final Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = theUnsafe.get(null);
            final Field field = BlockEntityType.class.getDeclaredField("validBlocks");
            validBlocksOffset = (Long) unsafeClass.getMethod("objectFieldOffset", Field.class)
                .invoke(unsafe, field);
            return true;
        }
        catch (Throwable t)
        {
            LOGGER.error("TFCFlorae: cannot access BlockEntityType.validBlocks", t);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static void patch(String label, RegistryObject<? extends BlockEntityType<?>> holder, BlockType type)
    {
        try
        {
            final BlockEntityType<?> beType = holder.get();
            final Set<Block> current = (Set<Block>) unsafe.getClass()
                .getMethod("getObject", Object.class, long.class)
                .invoke(unsafe, beType, validBlocksOffset);
            final Set<Block> merged = new HashSet<>(current);
            int added = 0;
            for (Map<BlockType, RegistryObject<Block>> parts : TFCFBlocks.WOODS.values())
            {
                final RegistryObject<Block> block = parts.get(type);
                if (block != null && merged.add(block.get())) added++;
            }
            if (added == 0) return;
            unsafe.getClass().getMethod("putObject", Object.class, long.class, Object.class)
                .invoke(unsafe, beType, validBlocksOffset, Collections.unmodifiableSet(merged));
            LOGGER.info("TFCFlorae: added {} blocks to TFC {} block entity type", added, label);
        }
        catch (Throwable t)
        {
            LOGGER.error("TFCFlorae: could not extend the TFC {} block entity type", label, t);
        }
    }
}
