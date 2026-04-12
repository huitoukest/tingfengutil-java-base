package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.JvmSystemInfo;
import com.tingfeng.util.java.base.common.bean.OsSystemInfo;
import com.tingfeng.util.java.base.common.bean.RuntimeStatus;
import com.tingfeng.util.java.base.common.bean.UserSystemInfo;
import com.tingfeng.util.java.base.common.constant.SystemConstants;

import java.lang.management.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Runtime相关工具类
 * @author huitoukest
 */
public class RuntimeUtils {

    /**
     * 获取当前操作系统名称
     */
    public static String getOSName() {
        return System.getProperty(SystemConstants.OSSystem.NAME).toLowerCase();
    }

    /**
     * 获取本地所有IP地址
     */
    public static List<InetAddress> getLocalAddress() {
        List<InetAddress> addressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> inetAddressIterator = ni.getInetAddresses();
                while (inetAddressIterator.hasMoreElements()) {
                    addressList.add(inetAddressIterator.nextElement());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return addressList;
    }

    /**
     * 获取本地IPv4地址列表
     * @param excludePhysicalAddress 是否排除物理地址（如MAC地址对应的IP地址）
     */
    public static List<String> getLocalIpV4Addresses(boolean excludePhysicalAddress) {
        List<InetAddress> addresses = getLocalAddress();
        return addresses.stream()
                .map(InetAddress::getHostAddress)
                .filter(it -> !excludePhysicalAddress || isValidIPv4(it))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 判断是否是有效的IPv4地址
     * 使用Inet4Address验证，避免正则匹配的性能开销
     * @param ip IP地址字符串
     * @return 是否有效IPv4
     */
    private static boolean isValidIPv4(String ip) {
        if (ip == null) {
            return false;
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address instanceof Inet4Address && !address.isLoopbackAddress();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取本地IPv4地址列表（不包含物理地址）
     */
    public static List<String> getLocalIpV4Addresses() {
        return getLocalIpV4Addresses(true);
    }

    // ==================== 网络信息增强 ====================

    /**
     * 获取主机名
     * @return 主机名
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取本机MAC地址列表
     * @return MAC地址列表（格式如：00:1A:2B:3C:4D:5E）
     */
    public static List<String> getMacAddresses() {
        List<String> macAddressList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    byte[] hardwareAddress = ni.getHardwareAddress();
                    if (hardwareAddress != null && hardwareAddress.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < hardwareAddress.length; i++) {
                            sb.append(String.format("%02X%s", hardwareAddress[i],
                                    (i < hardwareAddress.length - 1) ? ":" : ""));
                        }
                        macAddressList.add(sb.toString());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return macAddressList;
    }

    /**
     * 获取本地IPv6地址列表
     * @return IPv6地址列表
     */
    public static List<String> getLocalIPv6Addresses() {
        List<InetAddress> addresses = getLocalAddress();
        return addresses.stream()
                .map(InetAddress::getHostAddress)
                .filter(it -> it != null && it.contains(":"))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 判断指定网卡是否启用
     * @param interfaceName 网卡名称（如"eth0", "en0"）
     * @return 是否启用
     */
    public static boolean isNetworkInterfaceUp(String interfaceName) {
        try {
            NetworkInterface ni = NetworkInterface.getByName(interfaceName);
            return ni != null && ni.isUp();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== 系统属性获取 ====================

    /**
     * 获取JVM系统属性信息
     */
    public static JvmSystemInfo getJvmSystemInfo() {
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
    public static String getJvmProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * 获取操作系统属性信息
     */
    public static OsSystemInfo getOsSystemInfo() {
        OsSystemInfo info = new OsSystemInfo();
        info.setName(System.getProperty(SystemConstants.OSSystem.NAME));
        info.setVersion(System.getProperty(SystemConstants.OSSystem.VERSION));
        info.setOsArch(System.getProperty(SystemConstants.OSSystem.ARCH));
        return info;
    }

    /**
     * 获取单个OS系统属性
     */
    public static String getOsProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * 获取User和File/IO系统属性信息
     */
    public static UserSystemInfo getUserSystemInfo() {
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
    public static String getUserSystemProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * 获取单个系统属性
     */
    public static String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    // ==================== 运行时状态 ====================

    /**
     * 获取当前运行时状态
     */
    public static RuntimeStatus getRuntimeStatus() {
        Runtime runtime = Runtime.getRuntime();
        RuntimeStatus status = new RuntimeStatus();
        status.setMaxMemory(runtime.maxMemory());
        status.setTotalMemory(runtime.totalMemory());
        status.setFreeMemory(runtime.freeMemory());
        status.setAvailableProcessors(runtime.availableProcessors());
        return status;
    }

    /**
     * 检查内存使用率是否超过阈值
     * @param threshold 阈值（0.0 ~ 1.0），例如0.8表示80%
     */
    public static boolean isMemoryOverThreshold(double threshold) {
        RuntimeStatus status = getRuntimeStatus();
        return status.getMemoryUsageRatio() > threshold;
    }

    /**
     * 建议JVM进行垃圾回收
     * 注意：只是建议，实际是否执行由JVM决定
     */
    public static void gc() {
        Runtime.getRuntime().gc();
    }

    /**
     * 获取当前JVM进程ID（通过JMX方式实现，Java 8兼容）
     * @deprecated 请使用 {@link com.tingfeng.util.java.base.common.utils.process.ProcessUtils#currentPid()}
     */
    @Deprecated
    public static long getCurrentPid() {
        return com.tingfeng.util.java.base.common.utils.process.ProcessUtils.currentPid();
    }

    // ==================== 内存管理增强 ====================

    /**
     * 获取详细内存信息
     * @return 内存信息Map，包含max/total/free/used等
     */
    public static Map<String, Long> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Long> memoryInfo = new LinkedHashMap<>();
        memoryInfo.put("maxMemory", runtime.maxMemory());
        memoryInfo.put("totalMemory", runtime.totalMemory());
        memoryInfo.put("freeMemory", runtime.freeMemory());
        memoryInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        memoryInfo.put("availableProcessors", (long) runtime.availableProcessors());
        return memoryInfo;
    }

    /**
     * 获取各内存池信息
     * @return 内存池信息列表
     */
    public static List<Map<String, Object>> getMemoryPoolInfo() {
        List<Map<String, Object>> poolInfoList = new ArrayList<>();
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            Map<String, Object> poolInfo = new LinkedHashMap<>();
            poolInfo.put("name", pool.getName());
            poolInfo.put("type", pool.getType().name());
            MemoryUsage usage = pool.getUsage();
            if (usage != null) {
                poolInfo.put("used", usage.getUsed());
                poolInfo.put("committed", usage.getCommitted());
                poolInfo.put("max", usage.getMax());
            }
            poolInfoList.add(poolInfo);
        }
        return poolInfoList;
    }

    /**
     * 检查指定内存池使用率是否超过阈值
     * @param poolName 内存池名称（如"Tenured Gen", "Metaspace"）
     * @param threshold 阈值（0.0 ~ 1.0）
     * @return 是否超过阈值
     */
    public static boolean isMemoryPoolOverThreshold(String poolName, double threshold) {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getName().equals(poolName)) {
                MemoryUsage usage = pool.getUsage();
                if (usage != null && usage.getMax() > 0) {
                    return (double) usage.getUsed() / usage.getMax() > threshold;
                }
            }
        }
        return false;
    }

    // ==================== 类加载管理 ====================

    /**
     * 获取类加载信息
     * @return 类加载器信息列表
     */
    public static List<Map<String, Object>> getClassLoaderInfo() {
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
    public static long getLoadedClassCount() {
        return ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
    }

    /**
     * 获取类加载器层级链
     * @return 类加载器名称列表（从顶层到系统类加载器）
     */
    public static List<String> getClassLoaderHierarchy() {
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
    public static long getJvmUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /**
     * 获取JVM启动参数列表
     * @return JVM启动参数列表
     */
    public static List<String> getJvmArguments() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments();
    }

    /**
     * 检测死锁线程
     * @return 死锁线程信息列表，无死锁时返回空列表
     */
    public static List<Map<String, Object>> getDeadlockThreads() {
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
    public static List<Map<String, Object>> getAllThreadInfo() {
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
    public static Map<String, Integer> getThreadSummary() {
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
    public static double getSystemCpuLoad() {
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
    public static double getProcessCpuLoad() {
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
    public static long getTotalPhysicalMemory() {
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
    public static long getAvailablePhysicalMemory() {
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
    public static Map<String, Long> getDiskSpace(String path) {
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
