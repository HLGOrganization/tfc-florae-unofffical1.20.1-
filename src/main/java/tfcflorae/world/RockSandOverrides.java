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
 * TFC 岩石的沙色改写（用户指定的分配表）。
 * <p>
 * TFC 把「岩石 -> 沙色」写死在 {@link Rock} 枚举里，数据包改不了；
 * 但 {@link RockSettings#register} 的实现是 {@code PRESETS.put(id, settings)}（覆盖），
 * 这里反射取出 TFC 已注册好的记录，只替换 sand / sandstone，其余字段原样保留。
 * <p>
 * 生效前提：{@link SandSurfaceStates} 已把海岸地表状态抢回「按岩石取沙」。
 */
public final class RockSandOverrides
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private RockSandOverrides()
    {
    }

    public static void apply()
    {
        // 白沙：闪长岩、白垩岩、大理岩、安山岩、英安岩
        override(Rock.DIORITE, SandBlockType.WHITE);
        override(Rock.CHALK, SandBlockType.WHITE);
        override(Rock.MARBLE, SandBlockType.WHITE);
        override(Rock.ANDESITE, SandBlockType.WHITE);
        override(Rock.DACITE, SandBlockType.WHITE);
        // 黄沙：石灰岩、板岩、流纹岩、石英岩、片麻岩
        override(Rock.LIMESTONE, SandBlockType.YELLOW);
        override(Rock.SLATE, SandBlockType.YELLOW);
        override(Rock.RHYOLITE, SandBlockType.YELLOW);
        override(Rock.QUARTZITE, SandBlockType.YELLOW);
        override(Rock.GNEISS, SandBlockType.YELLOW);
        // 黑沙：辉长岩、页岩、白云岩、玄武岩
        override(Rock.GABBRO, SandBlockType.BLACK);
        override(Rock.SHALE, SandBlockType.BLACK);
        override(Rock.DOLOMITE, SandBlockType.BLACK);
        override(Rock.BASALT, SandBlockType.BLACK);
        // 粉沙：硅质岩、花岗岩
        override(Rock.CHERT, SandBlockType.PINK);
        override(Rock.GRANITE, SandBlockType.PINK);
        // 绿沙：砾岩、片岩
        override(Rock.CONGLOMERATE, SandBlockType.GREEN);
        override(Rock.SCHIST, SandBlockType.GREEN);
        // 棕沙：粘土岩、千枚岩
        override(Rock.CLAYSTONE, SandBlockType.BROWN);
        override(Rock.PHYLLITE, SandBlockType.BROWN);
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
