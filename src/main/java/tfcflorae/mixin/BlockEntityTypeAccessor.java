package tfcflorae.mixin;

import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Same accessor ArborFirmaCraft uses (afc.mixins.json -> BlockEntityTypeAccessor).
 * TFC builds its rotation block entity block lists from its own 20 woods, so blocks
 * from this mod's woods are not in them and never get a block entity - and since
 * WaterWheelBlock renders with RenderShape.ENTITYBLOCK_ANIMATED, no block entity means
 * nothing is drawn at all.
 * <p>
 * {@code @Mutable} on the setter removes the final modifier so the whole set can be
 * replaced at runtime; the blocks stay TFC's own classes, so TFC's block entities and
 * renderers apply unchanged.
 */
@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor
{
    @Accessor("validBlocks")
    Set<Block> accessor$getValidBlocks();

    @Accessor("validBlocks")
    @Mutable
    void accessor$setValidBlocks(Set<Block> blocks);
}
