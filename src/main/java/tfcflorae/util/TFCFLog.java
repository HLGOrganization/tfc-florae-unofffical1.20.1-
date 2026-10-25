package tfcflorae.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/**
 * 本模组的诊断日志开关。
 * <p>
 * 树苗生长这类“没有报错但就是不生效”的问题，只能靠在关键节点打日志定位。
 * 默认关闭以免刷屏，三种打开方式（任一即可）：
 * <ul>
 *   <li>JVM 参数 {@code -Dtfcf.debug.sapling=true}</li>
 *   <li>环境变量 {@code TFCF_DEBUG_SAPLING=1}</li>
 *   <li>配置文件 {@code tfcflorae-common.toml} 里的 {@code enableDebug = true}</li>
 * </ul>
 */
public final class TFCFLog {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static Boolean saplingEnabled;

   private TFCFLog() {
   }

   public static boolean sapling() {
      if (saplingEnabled == null) {
         boolean on = Boolean.getBoolean("tfcf.debug.sapling")
            || "1".equals(System.getenv("TFCF_DEBUG_SAPLING"));
         if (!on) {
            try {
               // 复用本模组的 debug 开关；配置类可能在早期尚未加载，失败就当作关闭
               on = Boolean.TRUE.equals(tfcflorae.Config.COMMON.enableDebug.get());
            } catch (Throwable ignored) {
            }
         }
         saplingEnabled = on;
      }
      return saplingEnabled;
   }

   public static void saplingInfo(String fmt, Object... args) {
      LOGGER.info("[TFCF-树苗] " + fmt, args);
   }
}
