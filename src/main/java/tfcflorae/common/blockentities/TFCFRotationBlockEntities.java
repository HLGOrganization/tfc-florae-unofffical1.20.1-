package tfcflorae.common.blockentities;

import java.lang.reflect.Field;
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
import tfcflorae.common.blocks.wood.TFCFWood;

/**
 * TFC registers its mechanical block entities with a fixed block list built from its own
 * 20 woods:
 * <pre>register("water_wheel", WaterWheelBlockEntity::new, woodBlocks(Wood.BlockType.WATER_WHEEL))</pre>
 * A block entity is only created when its block is in that list, so every water wheel,
 * axle, windmill ... built from this mod's 66 woods got no block entity, no renderer -
 * and therefore rendered as nothing at all (invisible in world).
 * <p>
 * The list is final, so the blocks are appended here through reflection at common setup.
 */
public final class TFCFRotationBlockEntities
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private TFCFRotationBlockEntities() {}

    public static void extendTFCBlockEntities()
    {
        extend("water_wheel", TFCBlockEntities.WATER_WHEEL, BlockType.WATER_WHEEL);
        extend("axle", TFCBlockEntities.AXLE, BlockType.AXLE);
        extend("bladed_axle", TFCBlockEntities.BLADED_AXLE, BlockType.BLADED_AXLE);
        extend("encased_axle", TFCBlockEntities.ENCASED_AXLE, BlockType.ENCASED_AXLE);
        extend("clutch", TFCBlockEntities.CLUTCH, BlockType.CLUTCH);
        extend("gear_box", TFCBlockEntities.GEAR_BOX, BlockType.GEAR_BOX);
        extend("windmill", TFCBlockEntities.WINDMILL, BlockType.WINDMILL);
    }

    private static void extend(String label, RegistryObject<? extends BlockEntityType<?>> holder, BlockType type)
    {
        try
        {
            final BlockEntityType<?> beType = holder.get();
            final Set<Block> extra = new HashSet<>();
            for (Map<BlockType, RegistryObject<Block>> parts : TFCFBlocks.WOODS.values())
            {
                final RegistryObject<Block> block = parts.get(type);
                if (block != null)
                {
                    extra.add(block.get());
                }
            }
            if (extra.isEmpty())
            {
                return;
            }

            final Field field = BlockEntityType.class.getDeclaredField("validBlocks");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Set<Block> current = (Set<Block>) field.get(beType);
            final Set<Block> merged = new HashSet<>(current);
            merged.addAll(extra);
            field.set(beType, java.util.Collections.unmodifiableSet(merged));
            LOGGER.info("TFCFlorae: added {} blocks to TFC {} block entity type", extra.size(), label);
        }
        catch (Throwable t)
        {
            LOGGER.error("TFCFlorae: could not extend the TFC {} block entity type", label, t);
        }
    }
}
