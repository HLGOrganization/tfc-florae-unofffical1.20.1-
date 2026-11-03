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
