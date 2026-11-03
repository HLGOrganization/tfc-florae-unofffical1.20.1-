package tfcflorae.world;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;

/**
 * 把海岸 / 沙岸的沙色改回「按当地岩石取沙」。
 * <p>
 * TFE 用两个 mixin 接管了地表：{@code SurfaceStatesMixin} 把 TFC {@link SurfaceStates} 的海岸状态
 * 换成 {@code NTESurfaceStates.*}；{@code NormalSurfaceBuilderMixin} 改写地表构建流程读 TFE 自己的状态。
 * 反编译确认的实际链条（TFE 2.3.0）：
 * <pre>
 * ShorelineSurfaceBuilder.SANDY  -> NTESurfaceStates.SHORE_SAND / SHORE_SANDSTONE
 * ShorelineSurfaceBuilder.NORMAL -> NTESurfaceStates.SHORE_SURFACE / SHORE_UNDERLAYER
 * SHORE_SURFACE = BEACH_MIX_NOISE &gt; lerp(温度) ? GRAVEL : SHORE_SAND
 * SHORE_SAND    = SAND_VARIANT_NOISE 在 RARE / YELLOW / BROWN / RED 之间挑
 * </pre>
 * 全程与岩石无关，所以海岸必然是固定那几种沙。
 * <p>
 * 这些字段都是 {@code public static final}，{@code Field.set} 会抛
 * {@code IllegalAccessException: Can not set static final}，必须用 {@code sun.misc.Unsafe} 写入。
 */
public final class SandSurfaceStates
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] TFC_FIELDS = {
        "SHORE_SAND", "RARE_SHORE_SAND", "SHORE_SANDSTONE", "RARE_SHORE_SANDSTONE"
    };

    private static Object UNSAFE;
    private static Method STATIC_FIELD_BASE;
    private static Method STATIC_FIELD_OFFSET;
    private static Method PUT_OBJECT;

    private SandSurfaceStates()
    {
    }

    public static void apply()
    {
        for (final String name : TFC_FIELDS)
        {
            writeStatic(SurfaceStates.class, name, name.endsWith("SANDSTONE") ? SandSurfaceStates::sandstoneState : SandSurfaceStates::sandState);
        }

        // TFE 那一套：先换 SANDSTONE 结尾的，再换其余含 SAND 的
        writeAll(SurfaceStates.class, true);
        writeAll(SurfaceStates.class, false);
        writeAll(loadTfe(), true);
        writeAll(loadTfe(), false);
    }

    private static Class<?> loadTfe()
    {
        try
        {
            return Class.forName("com.newterraearth.tfe.world.surface.NTESurfaceStates", true, SandSurfaceStates.class.getClassLoader());
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    private static void writeAll(Class<?> clazz, boolean stone)
    {
        if (clazz == null)
        {
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
            if (name.endsWith("SANDSTONE") != stone)
            {
                continue;
            }
            if (!stone && !name.contains("SAND"))
            {
                continue;
            }
            final SurfaceState state = stone ? SandSurfaceStates::sandstoneState : SandSurfaceStates::sandState;
            if (writeStatic(clazz, name, state))
            {
                changed++;
            }
        }
        LOGGER.info("[TFCFlorae] {}.{} 改写 {} 个", clazz.getSimpleName(), stone ? "SANDSTONE" : "SAND", changed);
    }

    private static BlockState sandState(SurfaceBuilderContext context)
    {
        return context.getRock().sand().defaultBlockState();
    }

    private static BlockState sandstoneState(SurfaceBuilderContext context)
    {
        return context.getRock().sandstone().defaultBlockState();
    }

    /** 用 Unsafe 写 static final；Unsafe 不可用则退回 Field.set。 */
    private static boolean writeStatic(Class<?> clazz, String fieldName, SurfaceState value)
    {
        try
        {
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            if (unsafeWrite(field, value))
            {
                return true;
            }
            field.set(null, value);
            return true;
        }
        catch (Throwable t)
        {
            LOGGER.error("[TFCFlorae] 地表状态改写失败 {}.{}", clazz.getSimpleName(), fieldName, t);
            return false;
        }
    }

    private static boolean unsafeWrite(Field field, Object value) throws Exception
    {
        if (UNSAFE == null)
        {
            final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            final Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = theUnsafe.get(null);
            STATIC_FIELD_BASE = unsafeClass.getMethod("staticFieldBase", Field.class);
            STATIC_FIELD_OFFSET = unsafeClass.getMethod("staticFieldOffset", Field.class);
            PUT_OBJECT = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
        }
        final Object base = STATIC_FIELD_BASE.invoke(UNSAFE, field);
        final long offset = (Long) STATIC_FIELD_OFFSET.invoke(UNSAFE, field);
        PUT_OBJECT.invoke(UNSAFE, base, offset, value);
        return true;
    }
}
