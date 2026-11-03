package tfcflorae.world;

import java.lang.reflect.Field;

import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;

/**
 * 抢回海岸 / 沙岸的沙色控制权。
 * <p>
 * TFE 的 {@code SurfaceStatesMixin} 在 {@link SurfaceStates} 的静态初始化末尾把
 * SHORE_SAND / RARE_SHORE_SAND / SHORE_SANDSTONE / RARE_SHORE_SANDSTONE
 * 全部替换成 {@code NTESurfaceStates.*}（噪声挑色板，与岩石无关）。
 * <p>
 * 我们在建世界前用反射把这 4 个静态字段重新指回「按当地岩石取沙」的实现，
 * 行为与 TFC 原本一致，于是 {@link RockSandOverrides} 对岩石沙色的改写才会生效。
 */
public final class SandSurfaceStates
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private SandSurfaceStates()
    {
    }

    public static void apply()
    {
        final SurfaceState sand = SandSurfaceStates::sandState;
        final SurfaceState sandstone = SandSurfaceStates::sandstoneState;

        override("SHORE_SAND", sand);
        override("RARE_SHORE_SAND", sand);
        override("SHORE_SANDSTONE", sandstone);
        override("RARE_SHORE_SANDSTONE", sandstone);

        // TFE 的 NormalSurfaceBuilderMixin 读的是它自己 NTESurfaceStates 里的状态，
        // 只改 TFC 的字段不够，必须把 TFE 那一套也换掉。
        overrideTfe(sand, sandstone);
    }

    /**
     * 把 TFE {@code NTESurfaceStates} 里所有以 SAND / SANDSTONE 结尾的静态状态替换成按岩石取沙。
     * TFE 未安装时静默跳过。
     */
    private static void overrideTfe(SurfaceState sand, SurfaceState sandstone)
    {
        final Class<?> clazz;
        try
        {
            clazz = Class.forName("com.newterraearth.tfe.world.surface.NTESurfaceStates");
        }
        catch (Throwable ignored)
        {
            LOGGER.info("[TFCFlorae] 未找到 TFE 的 NTESurfaceStates，跳过");
            return;
        }

        int changed = 0;
        for (final Field field : clazz.getDeclaredFields())
        {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.getType() != SurfaceState.class)
            {
                continue;
            }
            final String name = field.getName();
            final SurfaceState replacement;
            if (name.endsWith("SANDSTONE"))
            {
                replacement = sandstone;
            }
            else if (name.endsWith("SAND"))
            {
                replacement = sand;
            }
            else
            {
                continue;
            }
            try
            {
                field.setAccessible(true);
                field.set(null, replacement);
                changed++;
            }
            catch (Throwable t)
            {
                LOGGER.error("[TFCFlorae] 改写 TFE 地表状态失败 {}", name, t);
            }
        }
        LOGGER.info("[TFCFlorae] 已改写 TFE 的 NTESurfaceStates 沙状态 {} 个", changed);
    }

    private static BlockState sandState(SurfaceBuilderContext context)
    {
        return context.getRock().sand().defaultBlockState();
    }

    private static BlockState sandstoneState(SurfaceBuilderContext context)
    {
        return context.getRock().sandstone().defaultBlockState();
    }

    private static void override(String fieldName, SurfaceState state)
    {
        try
        {
            final Field field = SurfaceStates.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, state);
            LOGGER.info("[TFCFlorae] 海岸地表状态抢回 SurfaceStates.{} -> 按岩石取沙", fieldName);
        }
        catch (Throwable t)
        {
            LOGGER.error("[TFCFlorae] 海岸地表状态抢回失败 {}", fieldName, t);
        }
    }
}
