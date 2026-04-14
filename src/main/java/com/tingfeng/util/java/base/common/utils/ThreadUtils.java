package com.tingfeng.util.java.base.common.utils;

/**
 * 线程操作工具类（已迁移到 concurrent 包）
 *
 * @deprecated 请使用 {@link com.tingfeng.util.java.base.concurrent.ThreadUtils}
 */
@Deprecated
public class ThreadUtils {

    private ThreadUtils() {
    }

    /**
     * @deprecated 请使用 {@link com.tingfeng.util.java.base.concurrent.ThreadUtils#sleep(long)}
     */
    @Deprecated
    public static void sleep(long mills) {
        com.tingfeng.util.java.base.concurrent.ThreadUtils.sleep(mills);
    }

    /**
     * @deprecated 请使用 {@link com.tingfeng.util.java.base.concurrent.ThreadFactoryUtils#newNamedThreadFactory(String, boolean)}
     */
    @Deprecated
    public static java.util.concurrent.ThreadFactory newNamedThreadFactory(String namePrefix, boolean isDaemon) {
        return com.tingfeng.util.java.base.concurrent.ThreadFactoryUtils.newNamedThreadFactory(namePrefix, isDaemon);
    }
}
