package com.tingfeng.util.java.base;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志工具类
 * <p>
 * 自动检测 slf4j 是否可用：
 * - 有 slf4j：委托给 slf4j
 * - 无 slf4j：使用 System.out
 * <p>
 * 提供场景化日志方法和通用日志方法
 */
public final class LogUtils {

    private static final boolean HAS_SLF4J;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static org.slf4j.Logger slf4jLogger;

    static {
        HAS_SLF4J = checkSlf4j();
        if (HAS_SLF4J) {
            slf4jLogger = org.slf4j.LoggerFactory.getLogger(LogUtils.class);
        }
    }

    private static boolean checkSlf4j() {
        try {
            Class.forName("org.slf4j.Logger");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // ==================== 基础日志方法 ====================

    public static void debug(String msg) {
        if (HAS_SLF4J) {
            slf4jLogger.debug(msg);
        } else {
            System.out.println(format("[DEBUG]", msg));
        }
    }

    public static void debug(String format, Object... args) {
        debug(String.format(format, args));
    }

    public static void info(String msg) {
        if (HAS_SLF4J) {
            slf4jLogger.info(msg);
        } else {
            System.out.println(format("[INFO]", msg));
        }
    }

    public static void info(String format, Object... args) {
        info(String.format(format, args));
    }

    public static void warn(String msg) {
        if (HAS_SLF4J) {
            slf4jLogger.warn(msg);
        } else {
            System.out.println(format("[WARN]", msg));
        }
    }

    public static void warn(String format, Object... args) {
        warn(String.format(format, args));
    }

    public static void error(String msg) {
        if (HAS_SLF4J) {
            slf4jLogger.error(msg);
        } else {
            System.err.println(format("[ERROR]", msg));
        }
    }

    public static void error(String format, Object... args) {
        error(String.format(format, args));
    }

    public static void error(String msg, Throwable t) {
        if (HAS_SLF4J) {
            slf4jLogger.error(msg, t);
        } else {
            System.err.println(format("[ERROR]", msg));
            t.printStackTrace(System.err);
        }
    }

    // ==================== 场景化日志方法 ====================

    /**
     * 方法入口日志
     */
    public static void methodEnter(String className, String method, Object... params) {
        String paramsStr = params.length > 0 ? " with params: " + formatParams(params) : "";
        debug("[ENTER] %s.%s()%s", className, method, paramsStr);
    }

    /**
     * 方法返回日志
     */
    public static void methodReturn(String className, String method, Object result) {
        debug("[RETURN] %s.%s() => %s", className, method, result);
    }

    /**
     * 业务操作日志
     */
    public static void biz(String msg) {
        info("[BIZ] %s", msg);
    }

    public static void biz(String format, Object... args) {
        biz(String.format(format, args));
    }

    /**
     * 性能日志
     */
    public static void performance(String operation, long costMs) {
        if (costMs > 1000) {
            warn("[PERF] %s took %d ms (slow)", operation, costMs);
        } else {
            debug("[PERF] %s took %d ms", operation, costMs);
        }
    }

    /**
     * 调试日志（带标签）
     */
    public static void debug(String tag, String msg) {
        debug("[%s] %s", tag, msg);
    }

    /**
     * 调试日志（带标签和格式化）
     */
    public static void debug(String tag, String format, Object... args) {
        debug(String.format("[%s] " + format, tag, args));
    }

    /**
     * 通用的带级别前缀的日志
     */
    public static void log(String level, String msg) {
        switch (level.toUpperCase()) {
            case "DEBUG":
                debug(msg);
                break;
            case "INFO":
                info(msg);
                break;
            case "WARN":
                warn(msg);
                break;
            case "ERROR":
                error(msg);
                break;
            default:
                info(msg);
        }
    }

    // ==================== 私有辅助方法 ====================

    private static String format(String level, String msg) {
        return String.format("%s [%s] %s", DATE_FORMAT.format(new Date()), level, msg);
    }

    private static String formatParams(Object... params) {
        if (params == null || params.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Object p = params[i];
            sb.append(p == null ? "null" : p.toString());
        }
        return sb.toString();
    }

    private LogUtils() {
        // 私有构造器，禁止实例化
    }
}
