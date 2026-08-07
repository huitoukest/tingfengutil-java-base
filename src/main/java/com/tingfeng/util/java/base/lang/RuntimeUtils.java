package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.base.JvmSystemInfo;
import com.tingfeng.util.java.base.lang.base.OsSystemInfo;
import com.tingfeng.util.java.base.lang.base.RuntimeStatus;
import com.tingfeng.util.java.base.lang.base.UserSystemInfo;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

/**
 * Runtime相关工具类（存量兼容门面）
 *
 * 全部方法委托至拆分后的实现类，签名与行为与拆分前完全一致；实现类为包级私有，不对外暴露：
 * RuntimeNetOps（网络族）、RuntimeMemOps（内存族）、RuntimeInfoOps（信息族）。
 *
 * @author huitoukest
 */
public class RuntimeUtils {

    private RuntimeUtils() {

    }

    /** 获取当前操作系统名称，同 {@link RuntimeInfoOps#getOSName()} */
    public static String getOSName() {
        return RuntimeInfoOps.getOSName();
    }

    /** 获取本地所有IP地址，同 {@link RuntimeNetOps#getLocalAddress()} */
    public static List<InetAddress> getLocalAddress() {
        return RuntimeNetOps.getLocalAddress();
    }

    /** 获取本地IPv4地址列表（按参数决定是否排除回环/虚拟网卡地址），同 {@link RuntimeNetOps#getLocalIpV4Addresses(boolean)} */
    public static List<String> getLocalIpV4Addresses(boolean excludePhysicalAddress) {
        return RuntimeNetOps.getLocalIpV4Addresses(excludePhysicalAddress);
    }

    /** 获取本地IPv4地址列表（不包含物理地址），同 {@link RuntimeNetOps#getLocalIpV4Addresses()} */
    public static List<String> getLocalIpV4Addresses() {
        return RuntimeNetOps.getLocalIpV4Addresses();
    }

    /** 获取主机名，同 {@link RuntimeNetOps#getHostName()} */
    public static String getHostName() {
        return RuntimeNetOps.getHostName();
    }

    /** 获取本机MAC地址列表，同 {@link RuntimeNetOps#getMacAddresses()} */
    public static List<String> getMacAddresses() {
        return RuntimeNetOps.getMacAddresses();
    }

    /** 获取本地IPv6地址列表，同 {@link RuntimeNetOps#getLocalIPv6Addresses()} */
    public static List<String> getLocalIPv6Addresses() {
        return RuntimeNetOps.getLocalIPv6Addresses();
    }

    /** 判断指定网卡是否启用，同 {@link RuntimeNetOps#isNetworkInterfaceUp(String)} */
    public static boolean isNetworkInterfaceUp(String interfaceName) {
        return RuntimeNetOps.isNetworkInterfaceUp(interfaceName);
    }

    /** 获取JVM系统属性信息，同 {@link RuntimeInfoOps#getJvmSystemInfo()} */
    public static JvmSystemInfo getJvmSystemInfo() {
        return RuntimeInfoOps.getJvmSystemInfo();
    }

    /** 获取单个JVM系统属性，同 {@link RuntimeInfoOps#getJvmProperty(String)} */
    public static String getJvmProperty(String key) {
        return RuntimeInfoOps.getJvmProperty(key);
    }

    /** 获取操作系统属性信息，同 {@link RuntimeInfoOps#getOsSystemInfo()} */
    public static OsSystemInfo getOsSystemInfo() {
        return RuntimeInfoOps.getOsSystemInfo();
    }

    /** 获取单个OS系统属性，同 {@link RuntimeInfoOps#getOsProperty(String)} */
    public static String getOsProperty(String key) {
        return RuntimeInfoOps.getOsProperty(key);
    }

    /** 获取User和File/IO系统属性信息，同 {@link RuntimeInfoOps#getUserSystemInfo()} */
    public static UserSystemInfo getUserSystemInfo() {
        return RuntimeInfoOps.getUserSystemInfo();
    }

    /** 获取单个User/File系统属性，同 {@link RuntimeInfoOps#getUserSystemProperty(String)} */
    public static String getUserSystemProperty(String key) {
        return RuntimeInfoOps.getUserSystemProperty(key);
    }

    /** 获取单个系统属性，同 {@link RuntimeInfoOps#getSystemProperty(String)} */
    public static String getSystemProperty(String key) {
        return RuntimeInfoOps.getSystemProperty(key);
    }

    /** 获取当前运行时状态，同 {@link RuntimeMemOps#getRuntimeStatus()} */
    public static RuntimeStatus getRuntimeStatus() {
        return RuntimeMemOps.getRuntimeStatus();
    }

    /** 检查内存使用率是否超过阈值，同 {@link RuntimeMemOps#isMemoryOverThreshold(double)} */
    public static boolean isMemoryOverThreshold(double threshold) {
        return RuntimeMemOps.isMemoryOverThreshold(threshold);
    }

    /** 建议JVM进行垃圾回收，同 {@link RuntimeMemOps#gc()} */
    public static void gc() {
        RuntimeMemOps.gc();
    }

    /** 获取当前JVM进程ID（通过JMX方式实现，Java 8兼容），同 {@link RuntimeInfoOps#getCurrentPid()} */
    @Deprecated
    public static long getCurrentPid() {
        return RuntimeInfoOps.getCurrentPid();
    }

    /** 获取详细内存信息，同 {@link RuntimeMemOps#getMemoryInfo()} */
    public static Map<String, Long> getMemoryInfo() {
        return RuntimeMemOps.getMemoryInfo();
    }

    /** 获取各内存池信息，同 {@link RuntimeMemOps#getMemoryPoolInfo()} */
    public static List<Map<String, Object>> getMemoryPoolInfo() {
        return RuntimeMemOps.getMemoryPoolInfo();
    }

    /** 检查指定内存池使用率是否超过阈值，同 {@link RuntimeMemOps#isMemoryPoolOverThreshold(String, double)} */
    public static boolean isMemoryPoolOverThreshold(String poolName, double threshold) {
        return RuntimeMemOps.isMemoryPoolOverThreshold(poolName, threshold);
    }

    /** 获取类加载信息，同 {@link RuntimeInfoOps#getClassLoaderInfo()} */
    public static List<Map<String, Object>> getClassLoaderInfo() {
        return RuntimeInfoOps.getClassLoaderInfo();
    }

    /** 获取已加载类数量，同 {@link RuntimeInfoOps#getLoadedClassCount()} */
    public static long getLoadedClassCount() {
        return RuntimeInfoOps.getLoadedClassCount();
    }

    /** 获取类加载器层级链，同 {@link RuntimeInfoOps#getClassLoaderHierarchy()} */
    public static List<String> getClassLoaderHierarchy() {
        return RuntimeInfoOps.getClassLoaderHierarchy();
    }

    /** 获取JVM运行时间（毫秒），同 {@link RuntimeInfoOps#getJvmUptime()} */
    public static long getJvmUptime() {
        return RuntimeInfoOps.getJvmUptime();
    }

    /** 获取JVM启动参数列表，同 {@link RuntimeInfoOps#getJvmArguments()} */
    public static List<String> getJvmArguments() {
        return RuntimeInfoOps.getJvmArguments();
    }

    /** 检测死锁线程，同 {@link RuntimeInfoOps#getDeadlockThreads()} */
    public static List<Map<String, Object>> getDeadlockThreads() {
        return RuntimeInfoOps.getDeadlockThreads();
    }

    /** 获取所有线程信息，同 {@link RuntimeInfoOps#getAllThreadInfo()} */
    public static List<Map<String, Object>> getAllThreadInfo() {
        return RuntimeInfoOps.getAllThreadInfo();
    }

    /** 获取线程摘要信息，同 {@link RuntimeInfoOps#getThreadSummary()} */
    public static Map<String, Integer> getThreadSummary() {
        return RuntimeInfoOps.getThreadSummary();
    }

    /** 获取系统CPU使用率（最近1分钟平均值），同 {@link RuntimeInfoOps#getSystemCpuLoad()} */
    public static double getSystemCpuLoad() {
        return RuntimeInfoOps.getSystemCpuLoad();
    }

    /** 获取当前进程CPU使用率，同 {@link RuntimeInfoOps#getProcessCpuLoad()} */
    public static double getProcessCpuLoad() {
        return RuntimeInfoOps.getProcessCpuLoad();
    }

    /** 获取系统总物理内存（字节），同 {@link RuntimeInfoOps#getTotalPhysicalMemory()} */
    public static long getTotalPhysicalMemory() {
        return RuntimeInfoOps.getTotalPhysicalMemory();
    }

    /** 获取系统可用物理内存（字节），同 {@link RuntimeInfoOps#getAvailablePhysicalMemory()} */
    public static long getAvailablePhysicalMemory() {
        return RuntimeInfoOps.getAvailablePhysicalMemory();
    }

    /** 获取指定路径的磁盘空间信息，同 {@link RuntimeInfoOps#getDiskSpace(String)} */
    public static Map<String, Long> getDiskSpace(String path) {
        return RuntimeInfoOps.getDiskSpace(path);
    }

}
