package tfcflorae.world;

import java.util.Locale;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.SandstoneBlockType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.settings.RockSettings;

/**
 * TFC 岩石的沙色改写。
 * <p>
 * TFC 把「岩石 -> 沙色」写死在 {@link Rock} 枚举里（{@code SandBlockType} 字段），无法用数据包修改；
 * 但 {@link RockSettings#register(ResourceLocation, RockSettings)} 的实现是
 * {@code PRESETS.put(id, settings)}（会覆盖），因此可以在所有模组 common setup 结束后
 * 重新注册一份 {@link RockSettings}，把 sand / sandstone 换成目标颜色。
 * <p>
 * 不需要 mixin。
 */
public final class RockSandOverrides
{
    private RockSandOverrides()
    {
    }

    public static void apply()
    {
        // 安山岩、英安岩 -> 白沙
        override(Rock.ANDESITE, SandBlockType.WHITE);
        override(Rock.DACITE, SandBlockType.WHITE);
        // 花岗岩 -> 粉沙
        override(Rock.GRANITE, SandBlockType.PINK);
        // 板岩、黏土岩、砾岩 -> 黄沙
        override(Rock.SLATE, SandBlockType.YELLOW);
        override(Rock.CLAYSTONE, SandBlockType.YELLOW);
        override(Rock.CONGLOMERATE, SandBlockType.YELLOW);
    }

    private static void override(Rock rock, SandBlockType sand)
    {
        final ResourceLocation id = Helpers.identifier(rock.name().toLowerCase(Locale.ROOT));
        RockSettings.register(id, new RockSettings(
            block(rock, BlockType.RAW),
            block(rock, BlockType.HARDENED),
            block(rock, BlockType.GRAVEL),
            block(rock, BlockType.COBBLE),
            TFCBlocks.SAND.get(sand).get(),
            TFCBlocks.SANDSTONE.get(sand).get(SandstoneBlockType.RAW).get(),
            required(rock, BlockType.SPIKE),
            required(rock, BlockType.LOOSE),
            Optional.empty()
        ));
    }

    private static Block block(Rock rock, BlockType type)
    {
        final java.util.function.Supplier<? extends Block> supplier = rock.getBlock(type);
        return supplier == null ? null : supplier.get();
    }

    private static Optional<Block> required(Rock rock, BlockType type)
    {
        return Optional.ofNullable(block(rock, type));
    }
}
