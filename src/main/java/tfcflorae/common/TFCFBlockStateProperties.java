package tfcflorae.common;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Block state properties that TFC 3.x removed but which TFC Florae's content still uses.
 * <p>
 * 1.18.2 exposed these through {@code net.dries007.tfc.common.blocks.TFCBlockStateProperties};
 * in 1.20.1 that class no longer carries them, so the mod declares its own. The names and
 * ranges are chosen to match TFC's previous values so blockstate JSON only needs the
 * property name (not a range) to line up.
 */
public final class TFCFBlockStateProperties
{
    /** Marks a bamboo log as naturally generated (TFC 1.18.2: {@code NATURAL}). */
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    /**
     * Decay distance property ladder used by the mod's leaves blocks.
     * TFC 1.18.2 exposed {@code DISTANCES} as an array indexed by distance - 7.
     */
    public static final IntegerProperty[] DISTANCES = {
        IntegerProperty.create("distance", 1, 9)
    };

    /** Convenience alias: distance property for leaves-like blocks. */
    public static final IntegerProperty DISTANCE = DISTANCES[0];

    /** Re-export so call sites that referenced the vanilla property keep working. */
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;

    private TFCFBlockStateProperties() {}
}
