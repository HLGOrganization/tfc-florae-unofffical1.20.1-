package tfcflorae.world;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.dries007.tfc.common.blocks.SandstoneBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.settings.RockSettings;

/**
 * TFC 岩石的沙色改写。
 * <p>
 * TFC 把「岩石 -> 沙色」写死在 {@link Rock} 枚举里（{@code SandBlockType} 字段），数据包改不了。
 * 沙块最终由 {@link RockSettings#sand()} 提供，而 {@code RockSettings.register} 的实现是
 * {@code PRESETS.put(id, settings)}（会覆盖）。
 * <p>
 * 这里用反射取出 TFC 已注册好的 {@link RockSettings}，只把 sand / sandstone 换成目标颜色，
 * 其余字段（raw / hardened / gravel / cobble / spike / loose / mossyLoose）原样保留，
 * 再覆盖回去。不需要 mixin。
 */
public final class RockSandOverrides
{
    private static final Logger LOGGER = LogUtils.getLogger();

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

    @SuppressWarnings("unchecked")
    private static void override(Rock rock, SandBlockType sandType)
    {
        final ResourceLocation id = Helpers.identifier(rock.name().toLowerCase(Locale.ROOT));
        final Block sand = TFCBlocks.SAND.get(sandType).get();
        final Block sandstone = TFCBlocks.SANDSTONE.get(sandType).get(SandstoneBlockType.RAW).get();

        try
        {
            final Field field = RockSettings.class.getDeclaredField("PRESETS");
            field.setAccessible(true);
            final Map<ResourceLocation, RockSettings> presets = (Map<ResourceLocation, RockSettings>) field.get(null);
            final RockSettings old = presets.get(id);
            if (old == null)
            {
                LOGGER.warn("[TFCFlorae] RockSettings 里找不到 {}，跳过沙色改写", id);
                return;
            }
            presets.put(id, new RockSettings(
                old.raw(), old.hardened(), old.gravel(), old.cobble(),
                sand, sandstone,
                old.spike(), old.loose(), old.mossyLoose()
            ));
            LOGGER.info("[TFCFlorae] 沙色改写 {} -> {}", id, sandType);
        }
        catch (Throwable t)
        {
            LOGGER.error("[TFCFlorae] 沙色改写失败 {} -> {}", id, sandType, t);
        }
    }
}
