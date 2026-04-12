package com.tingfeng.util.java.base.common.utils.process;

import java.lang.reflect.Field;

/**
 * 进程包装类，实现AutoCloseable自动关闭
 * @author huitoukest
 */
public class ProcessWrapper implements AutoCloseable {
    private final java.lang.Process process;
    private final long pid;

    public ProcessWrapper(java.lang.Process process) {
        this.process = process;
        this.pid = getPid(process);
    }

    public static long getPid(java.lang.Process process) {
        try {
            Field pidField = process.getClass().getDeclaredField("pid");
            pidField.setAccessible(true);
            return (long) pidField.get(process);
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public long getPid() {
        return pid;
    }

    public java.lang.Process getProcess() {
        return process;
    }

    /**
     * 关闭进程（实现AutoCloseable）
     * 默认等待10秒优雅关闭，超时后强制终止
     */
    @Override
    public void close() {
        gracefulShutdown(process, 10_000);
    }

    /**
     * 强制关闭进程
     */
    public void forceClose() {
        destroyForcibly(process);
    }

    /**
     * 梯度终止进程
     * @param process 要终止的进程
     * @param timeoutMs 总超时时间
     */
    public void gracefulShutdown(java.lang.Process process, long timeoutMs) {
        process.destroy();
        if (waitForExit(process, timeoutMs)) {
            return;
        }
        process.destroy();
        if (waitForExit(process, timeoutMs / 2)) {
            return;
        }
        destroyForcibly(process);
    }

    /**
     * 强制终止进程（Java 8兼容）
     */
    private boolean destroyForcibly(java.lang.Process process) {
        process.destroy();
        if (process.isAlive()) {
            // Java 9+ 才有 destroyForcibly()，Java 8 兼容处理
            try {
                // 尝试调用 destroyForcibly（Java 9+）
                java.lang.reflect.Method method = process.getClass().getMethod("destroyForcibly");
                return (boolean) method.invoke(process);
            } catch (Exception e) {
                // Java 8: destroy 可能不足以终止，再次尝试
                return !process.isAlive();
            }
        }
        return true;
    }

    private boolean waitForExit(java.lang.Process process, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive()) {
                return true;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    /**
     * 获取当前JVM进程ID
     * @return PID
     */
    public static long currentPid() {
        return ProcessUtils.currentPid();
    }
}
