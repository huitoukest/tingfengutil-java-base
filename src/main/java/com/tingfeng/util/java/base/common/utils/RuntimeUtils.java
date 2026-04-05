package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.JvmSystemInfo;
import com.tingfeng.util.java.base.common.bean.OsSystemInfo;
import com.tingfeng.util.java.base.common.bean.RuntimeStatus;
import com.tingfeng.util.java.base.common.bean.UserSystemInfo;
import com.tingfeng.util.java.base.common.constant.SystemConstants;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
                .filter(it -> !excludePhysicalAddress || RegExpUtils.isMatch(it, RegExpUtils.PatternStr.IP_V4, true))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取本地IPv4地址列表（不包含物理地址）
     */
    public static List<String> getLocalIpV4Addresses() {
        return getLocalIpV4Addresses(true);
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
     */
    public static long getCurrentPid() {
        String name = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        int atIndex = name.indexOf('@');
        if (atIndex <= 0) {
            throw new IllegalStateException("Cannot parse PID from: " + name);
        }
        return Long.parseLong(name.substring(0, atIndex));
    }
}
