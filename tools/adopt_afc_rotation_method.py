"""Adopt AFC's approach: keep TFC's block types and block entities, extend validBlocks.

AFC does exactly this with a Mixin accessor:

    @Mixin(BlockEntityType.class)
    interface BlockEntityTypeAccessor {
        @Accessor("validBlocks") Set<Block> getValidBlocks();
        @Accessor("validBlocks") @Mutable void setValidBlocks(Set<Block> blocks);
    }

This project has no mixin infrastructure, so the same field replacement is done with
sun.misc.Unsafe (which is not blocked by the final modifier).  Once the blocks are in
TFC's own block entity types, TFC's block classes, block entity classes AND renderers
all apply automatically - no custom types, no custom renderer registration.

This script:
  1. removes the custom rotation block construction from woodMapper
  2. removes the seven custom block entity types
  3. removes the custom renderer registrations
  4. rewrites TFCFRotationBlockEntities to patch validBlocks via Unsafe
"""
import io
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "src", "main", "java", "tfcflorae")
BLOCKS = os.path.join(JAVA, "common", "blocks", "TFCFBlocks.java")
BE = os.path.join(JAVA, "common", "blockentities", "TFCFBlockEntities.java")
CLIENT = os.path.join(JAVA, "client", "ClientEventHandler.java")
ROT = os.path.join(JAVA, "common", "blockentities", "TFCFRotationBlockEntities.java")

# ---------- 1) woodMapper: drop the custom construction ---------------------
s = io.open(BLOCKS, encoding="utf-8").read()
hook = """               // rotation machines need a block entity that lists our blocks; TFC's own
               // types only know its 20 woods, so build these blocks ourselves
               if (isRotationType(type)) {
                  subMap.put(type, register(
                     ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                     () -> createRotationBlock(type, wood),
                     TFCCreativeTabs.WOOD
                  ));
                  continue;
               }
"""
assert hook in s, "wood mapper hook missing"
s = s.replace(hook, "", 1)

# drop the helper methods (isRotationType .. createRotationBlock)
start = s.find("   /** Types whose rendering depends on a block entity (invisible in world otherwise). */")
end = s.find("   private static Map<TFCFWood, Map<BlockType, RegistryObject<Block>>> woodMapper(Class<TFCFWood> enumClass) {")
assert start != -1 and end != -1 and start < end, "helper block not found"
s = s[:start] + s[end:]
io.open(BLOCKS, "w", encoding="utf-8", newline="\n").write(s)
print("woodMapper: back to TFC's own block types")

# ---------- 2) drop the seven custom block entity types --------------------
b = io.open(BE, encoding="utf-8").read()
start = b.find("   // TFC's own rotation block entity types only list its 20 woods")
end = b.find('   public static final RegistryObject<BlockEntityType<LoomBlockEntity>> LOOM = register(')
assert start != -1 and end != -1, "custom block entity block not found"
b = b[:start] + b[end:]
io.open(BE, "w", encoding="utf-8", newline="\n").write(b)
print("TFCFBlockEntities: custom rotation types removed")

# ---------- 3) drop the custom renderer registrations ----------------------
c = io.open(CLIENT, encoding="utf-8").read()
start = c.find("      // Rotation machines: TFC binds its renderers to ITS block entity types only")
end = c.find("      // 1.18.2 shipped a custom boat renderer")
assert start != -1 and end != -1 and start < end, "renderer block not found"
c = c[:start] + c[end:]
io.open(CLIENT, "w", encoding="utf-8", newline="\n").write(c)
print("ClientEventHandler: custom renderer registrations removed")

# ---------- 4) rewrite the validBlocks patch with Unsafe -------------------
rot = '''package tfcflorae.common.blockentities;

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
'''
io.open(ROT, "w", encoding="utf-8", newline="\n").write(rot)
print("TFCFRotationBlockEntities: rewritten with Unsafe field replacement")
