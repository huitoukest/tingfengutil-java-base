package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.common.constant.SystemConstants;
import com.tingfeng.util.java.base.lang.base.JvmSystemInfo;
import com.tingfeng.util.java.base.lang.base.OsSystemInfo;
import com.tingfeng.util.java.base.lang.base.UserSystemInfo;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JVM/OS/用户系统信息与资源信息相关操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link RuntimeUtils} 对外提供统一 API。
 */
class RuntimeInfoOps {

    /**
     * 获取当前操作系统名称
     */
    static String getOSName() {
        return System.getProperty(SystemConstants.OSSystem.NAME).toLowerCase();
    }

    // ==================== 系统属性获取 ====================

    /**
     * 获取JVM系统属性信息
     */
    static JvmSystemInfo getJvmSystemInfo() {
        JvmSystemInfo info = new JvmSystemInfo();
        info.setVersion(System.getProperty(SystemConstants.JVMSystem.VERSION));
        info.setHome(System.getProperty(SystemConstants.JVMSystem.HOME));
        info.setClassPath(System.getProperty(SystemConstants.JVMSystem.CLASS_PATH));
        info.setClassVersion(System.getProperty(SystemConstants.JVMSystem.CLASS_VERSION));
        info.setVmName(System.getProperty(SystemConstants.JVMSystem.VM_NAME));
        info.setVmVersion(System.getProperty(SystemConstants.JVMSystem.VM_VERSION));
        info.setVmVendor(System.getProperty(SystemConstants.JVMSystem.VM_VENDOR));
        info.setRuntimeName(System.getProperty(SystemConstants.JVMSystem.RUNTIME_NAME));
        info.setRuntimeVersion(System.getProperty(SystemConstants.JVMSystem.RUNTIME_VERSION));
        info.setLibraryPath(System.getProperty(SystemConstants.JVMSystem.LIBRARY_PATH));
        return info;
    }

    /**
     * 获取单个JVM系统属性
     */
    static String getJvmProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * 获取操作系统属性信息
     */
    static OsSystemInfo getOsSystemInfo() {
        OsSystemInfo info = new OsSystemInfo();
        info.setName(System.getProperty(SystemConstants.OSSystem.NAME));
        info.setVersion(System.getProperty(SystemConstants.OSSystem.VERSION));
        info.setOsArch(System.getProperty(SystemConstants.OSSystem.ARCH));
        return info;
    }

    /**
     * 获取单个OS系统属性
     */
    static String getOsProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * 获取User和File/IO系统属性信息
     */
    static UserSystemInfo getUserSystemInfo() {
        UserSystemInfo info = new UserSystemInfo();
        info.setUserName(System.getProperty(SystemConstants.UserSystem.NAME));
        info.setUserHome(System.getProperty(SystemConstants.UserSystem.HOME));
        info.setUserDir(System.getProperty(SystemConstants.UserSystem.DIR));
        info.setFileSeparator(System.getProperty(SystemConstants.FileSystem.SEPARATOR));
        info.setPathSeparator(System.getProperty(SystemConstants.FileSystem.PATH_SEPARATOR));
        info.setLineSeparator(System.getProperty(SystemConstants.FileSystem.LINE_SEPARATOR));
        info.setJavaIoTmpdir(System.getProperty(SystemConstants.FileSystem.TMP_DIR));
        return info;
    }

    /**
     * 获取单个User/File系统属性
     */
    static String getUserSystemProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * 获取单个系统属性
     */
    static String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    // ==================== 运行时状态 ====================

    /**
     * 获取当前JVM进程ID（通过JMX方式实现，Java 8兼容）
     * @deprecated 请使用其他方式获取进程ID
     */
    @Deprecated
    static long getCurrentPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int atIndex = name.indexOf('@');
        if (atIndex <= 0) {
            throw new RuntimeException("Cannot parse PID from: " + name);
        }
        return Long.parseLong(name.substring(0, atIndex));
    }

    // ==================== 类加载管理 ====================

    /**
     * 获取类加载信息
     * @return 类加载器信息列表
     */
    static List<Map<String, Object>> getClassLoaderInfo() {
        List<Map<String, Object>> loaderInfoList = new ArrayList<>();
        ClassLoader currentLoader = ClassLoader.getSystemClassLoader();
        int level = 0;
        while (currentLoader != null) {
            Map<String, Object> loaderInfo = new LinkedHashMap<>();
            loaderInfo.put("level", level);
            loaderInfo.put("classLoader", currentLoader.getClass().getName());
            loaderInfo.put("parent", currentLoader.getParent() != null ?
                    currentLoader.getParent().getClass().getName() : null);
            loaderInfoList.add(loaderInfo);
            currentLoader = currentLoader.getParent();
            level++;
        }
        return loaderInfoList;
    }

    /**
     * 获取已加载类数量
     * @return 已加载类数量
     */
    static long getLoadedClassCount() {
        return ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
    }

    /**
     * 获取类加载器层级链
     * @return 类加载器名称列表（从顶层到系统类加载器）
     */
    static List<String> getClassLoaderHierarchy() {
        List<String> hierarchy = new ArrayList<>();
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        while (loader != null) {
            hierarchy.add(loader.getClass().getName());
            loader = loader.getParent();
        }
        return hierarchy;
    }

    // ==================== JVM运行信息 ====================

    /**
     * 获取JVM运行时间（毫秒）
     * @return JVM运行时间
     */
    static long getJvmUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /**
     * 获取JVM启动参数列表
     * @return JVM启动参数列表
     */
    static List<String> getJvmArguments() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments();
    }

    /**
     * 检测死锁线程
     * @return 死锁线程信息列表，无死锁时返回空列表
     */
    static List<Map<String, Object>> getDeadlockThreads() {
        List<Map<String, Object>> deadlockInfo = new ArrayList<>();
        long[] threadIds = ManagementFactory.getThreadMXBean().findDeadlockedThreads();
        if (threadIds != null && threadIds.length > 0) {
            ThreadInfo[] threadInfos = ManagementFactory.getThreadMXBean().getThreadInfo(
                    threadIds, true, true);
            for (ThreadInfo info : threadInfos) {
                if (info != null) {
                    deadlockInfo.add(threadInfoToMap(info, true));
                }
            }
        }
        return deadlockInfo;
    }

    /**
     * 获取所有线程信息
     * @return 线程信息列表
     */
    static List<Map<String, Object>> getAllThreadInfo() {
        List<Map<String, Object>> threadInfoList = new ArrayList<>();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
        if (threadInfos != null) {
            for (ThreadInfo info : threadInfos) {
                if (info != null) {
                    threadInfoList.add(threadInfoToMap(info, false));
                }
            }
        }
        return threadInfoList;
    }

    /**
     * 将ThreadInfo转换为Map
     * @param info 线程信息
     * @param includeLockInfo 是否包含锁信息
     * @return Map
     */
    private static Map<String, Object> threadInfoToMap(ThreadInfo info, boolean includeLockInfo) {
        Map<String, Object> threadMap = new LinkedHashMap<>();
        threadMap.put("id", info.getThreadId());
        threadMap.put("name", info.getThreadName());
        threadMap.put("state", info.getThreadState().name());
        threadMap.put("blockedTime", info.getBlockedTime());
        threadMap.put("waitedTime", info.getWaitedTime());
        if (includeLockInfo) {
            threadMap.put("lockName", info.getLockName());
            threadMap.put("lockOwnerName", info.getLockOwnerName());
        }
        return threadMap;
    }

    /**
     * 获取线程摘要信息
     * @return 包含线程计数的Map
     */
    static Map<String, Integer> getThreadSummary() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("totalStarted", (int) threadMXBean.getTotalStartedThreadCount());
        summary.put("peak", threadMXBean.getPeakThreadCount());
        summary.put("current", threadMXBean.getThreadCount());
        summary.put("daemon", threadMXBean.getDaemonThreadCount());
        return summary;
    }

    // ==================== 系统资源信息 ====================

    /**
     * 获取系统CPU使用率（最近1分钟平均值）
     * @return CPU使用率（0.0 ~ 1.0）
     */
    static double getSystemCpuLoad() {
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osMXBean).getSystemCpuLoad();
        }
        return -1.0;
    }

    /**
     * 获取当前进程CPU使用率
     * @return CPU使用率（0.0 ~ 1.0）
     */
    static double getProcessCpuLoad() {
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osMXBean).getProcessCpuLoad();
        }
        return -1.0;
    }

    /**
     * 获取系统总物理内存（字节）
     * @return 总物理内存，获取失败返回-1
     */
    @SuppressWarnings("unchecked")
    static long getTotalPhysicalMemory() {
        try {
            Class<?> clazz = Class.forName("com.sun.management.OperatingSystemMXBean");
            Object osMXBean = ManagementFactory.getOperatingSystemMXBean();
            if (clazz.isInstance(osMXBean)) {
                java.lang.reflect.Method method = clazz.getMethod("getTotalMemorySize");
                return (Long) method.invoke(osMXBean);
            }
        } catch (Exception e) {
            // 忽略，获取失败返回-1
        }
        return -1;
    }

    /**
     * 获取系统可用物理内存（字节）
     * @return 可用物理内存，获取失败返回-1
     */
    @SuppressWarnings("unchecked")
    static long getAvailablePhysicalMemory() {
        try {
            Class<?> clazz = Class.forName("com.sun.management.OperatingSystemMXBean");
            Object osMXBean = ManagementFactory.getOperatingSystemMXBean();
            if (clazz.isInstance(osMXBean)) {
                java.lang.reflect.Method method = clazz.getMethod("getFreeMemorySize");
                return (Long) method.invoke(osMXBean);
            }
        } catch (Exception e) {
            // 忽略，获取失败返回-1
        }
        return -1;
    }

    /**
     * 获取指定路径的磁盘空间信息
     * @param path 路径（如"/"或"C:"）
     * @return 磁盘空间信息Map，包含total/free/usable（单位：字节）
     */
    static Map<String, Long> getDiskSpace(String path) {
        Map<String, Long> diskSpace = new LinkedHashMap<>();
        try {
            java.io.File file = new java.io.File(path);
            diskSpace.put("total", file.getTotalSpace());
            diskSpace.put("free", file.getFreeSpace());
            diskSpace.put("usable", file.getUsableSpace());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return diskSpace;
    }

}
