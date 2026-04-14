package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.base.JvmSystemInfo;
import com.tingfeng.util.java.base.lang.base.OsSystemInfo;
import com.tingfeng.util.java.base.lang.base.RuntimeStatus;
import com.tingfeng.util.java.base.lang.base.UserSystemInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * RuntimeUtils单元测试
 * @author huitoukest
 */
public class RunTimeUtilsTest {

    // ==================== 系统属性测试 ====================

    @Test
    public void testGetJvmSystemInfo() {
        JvmSystemInfo info = RuntimeUtils.getJvmSystemInfo();
        Assert.assertNotNull(info);
        Assert.assertNotNull(info.getVersion());
        Assert.assertNotNull(info.getHome());
        Assert.assertNotNull(info.getClassPath());
        Assert.assertNotNull(info.getVmName());
    }

    @Test
    public void testGetJvmProperty() {
        String version = RuntimeUtils.getJvmProperty("java.version");
        Assert.assertNotNull(version);
        Assert.assertFalse(version.isEmpty());
    }

    @Test
    public void testGetOsSystemInfo() {
        OsSystemInfo info = RuntimeUtils.getOsSystemInfo();
        Assert.assertNotNull(info);
        Assert.assertNotNull(info.getName());
        Assert.assertNotNull(info.getOsArch());
    }

    @Test
    public void testGetOsProperty() {
        String osName = RuntimeUtils.getOsProperty("os.name");
        Assert.assertNotNull(osName);
        Assert.assertFalse(osName.isEmpty());
    }

    @Test
    public void testGetUserSystemInfo() {
        UserSystemInfo info = RuntimeUtils.getUserSystemInfo();
        Assert.assertNotNull(info);
        Assert.assertNotNull(info.getUserName());
        Assert.assertNotNull(info.getUserHome());
        Assert.assertNotNull(info.getUserDir());
        Assert.assertNotNull(info.getFileSeparator());
        Assert.assertNotNull(info.getPathSeparator());
        Assert.assertNotNull(info.getLineSeparator());
        Assert.assertNotNull(info.getJavaIoTmpdir());
    }

    @Test
    public void testGetUserSystemProperty() {
        String userName = RuntimeUtils.getUserSystemProperty("user.name");
        Assert.assertNotNull(userName);
    }

    @Test
    public void testGetSystemProperty() {
        String javaVersion = RuntimeUtils.getSystemProperty("java.version");
        Assert.assertNotNull(javaVersion);
    }

    // ==================== 运行时状态测试 ====================

    @Test
    public void testGetRuntimeStatus() {
        RuntimeStatus status = RuntimeUtils.getRuntimeStatus();
        Assert.assertNotNull(status);
        Assert.assertTrue(status.getMaxMemory() > 0);
        Assert.assertTrue(status.getTotalMemory() > 0);
        Assert.assertTrue(status.getAvailableProcessors() > 0);
    }

    @Test
    public void testRuntimeStatusComputedValues() {
        RuntimeStatus status = RuntimeUtils.getRuntimeStatus();
        long usedMemory = status.getUsedMemory();
        double usageRatio = status.getMemoryUsageRatio();
        long usableMemory = status.getUsableMemory();

        Assert.assertTrue(usedMemory >= 0);
        Assert.assertTrue(usageRatio >= 0.0 && usageRatio <= 1.0);
        Assert.assertTrue(usableMemory >= 0);
        Assert.assertEquals(status.getMaxMemory() - usedMemory, usableMemory);
    }

    @Test
    public void testIsMemoryOverThreshold() {
        // 阈值为1.0时应该永远返回false
        Assert.assertFalse(RuntimeUtils.isMemoryOverThreshold(1.0));
        // 阈值为0.0时应该永远返回true（如果内存有使用）
        RuntimeUtils.isMemoryOverThreshold(0.0);
    }

    @Test
    public void testGetCurrentPid() {
        long pid = RuntimeUtils.getCurrentPid();
        Assert.assertTrue(pid > 0);
    }

    // ==================== 原有方法测试 ====================

    @Test
    public void testGetOSName() {
        String osName = RuntimeUtils.getOSName();
        Assert.assertNotNull(osName);
        Assert.assertTrue(osName.toLowerCase().contains("windows") ||
                           osName.toLowerCase().contains("linux") ||
                           osName.toLowerCase().contains("mac"));
    }

    @Test
    public void testGetLocalAddress() {
        List<?> addresses = RuntimeUtils.getLocalAddress();
        Assert.assertNotNull(addresses);
    }

    @Test
    public void testGetLocalIpV4Addresses() {
        List<String> ipAddrs = RuntimeUtils.getLocalIpV4Addresses();
        Assert.assertNotNull(ipAddrs);
        Assert.assertTrue(!ipAddrs.isEmpty());
    }

    @Test
    public void testGetLocalIpV4AddressesWithPhysicalAddress() {
        List<String> ipAddrs = RuntimeUtils.getLocalIpV4Addresses(false);
        Assert.assertNotNull(ipAddrs);
    }

    @Test
    public void testGetLocalIpV4AddressesExcludePhysicalAddress() {
        List<String> ipAddrs = RuntimeUtils.getLocalIpV4Addresses(true);
        Assert.assertNotNull(ipAddrs);
        // 过滤后的地址应该是有效的IPv4格式
        for (String ip : ipAddrs) {
            Assert.assertTrue(ip.matches("\\d+\\.\\d+\\.\\d+\\.\\d+"));
        }
    }

    // ==================== 网络信息增强测试 ====================

    @Test
    public void testGetHostName() {
        String hostName = RuntimeUtils.getHostName();
        Assert.assertNotNull(hostName);
        Assert.assertFalse(hostName.isEmpty());
    }

    @Test
    public void testGetMacAddresses() {
        List<String> macAddresses = RuntimeUtils.getMacAddresses();
        Assert.assertNotNull(macAddresses);
        // MAC地址格式验证（00:1A:2B:3C:4D:5E）
        for (String mac : macAddresses) {
            Assert.assertTrue(mac.matches("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}"));
        }
    }

    @Test
    public void testGetLocalIPv6Addresses() {
        List<String> ipv6Addresses = RuntimeUtils.getLocalIPv6Addresses();
        Assert.assertNotNull(ipv6Addresses);
        // IPv6地址应该包含冒号
        for (String ip : ipv6Addresses) {
            Assert.assertTrue(ip.contains(":"));
        }
    }

    @Test
    public void testIsNetworkInterfaceUp() {
        // 测试一个常见网卡名称格式
        RuntimeUtils.isNetworkInterfaceUp("lo");
    }

    // ==================== 内存管理增强测试 ====================

    @Test
    public void testGetMemoryInfo() {
        Map<String, Long> memoryInfo = RuntimeUtils.getMemoryInfo();
        Assert.assertNotNull(memoryInfo);
        Assert.assertTrue(memoryInfo.get("maxMemory") > 0);
        Assert.assertTrue(memoryInfo.get("totalMemory") > 0);
        Assert.assertTrue(memoryInfo.get("freeMemory") >= 0);
        Assert.assertTrue(memoryInfo.get("usedMemory") >= 0);
        Assert.assertTrue(memoryInfo.get("availableProcessors") > 0);
    }

    @Test
    public void testGetMemoryPoolInfo() {
        List<Map<String, Object>> poolInfo = RuntimeUtils.getMemoryPoolInfo();
        Assert.assertNotNull(poolInfo);
        Assert.assertFalse(poolInfo.isEmpty());
        for (Map<String, Object> pool : poolInfo) {
            Assert.assertNotNull(pool.get("name"));
            Assert.assertNotNull(pool.get("type"));
        }
    }

    @Test
    public void testIsMemoryPoolOverThreshold() {
        // 测试不存在的内存池
        boolean result = RuntimeUtils.isMemoryPoolOverThreshold("NonExistentPool", 0.8);
        Assert.assertFalse(result);
    }

    // ==================== 类加载管理测试 ====================

    @Test
    public void testGetClassLoaderInfo() {
        List<Map<String, Object>> loaderInfo = RuntimeUtils.getClassLoaderInfo();
        Assert.assertNotNull(loaderInfo);
        Assert.assertFalse(loaderInfo.isEmpty());
        for (Map<String, Object> info : loaderInfo) {
            Assert.assertNotNull(info.get("level"));
            Assert.assertNotNull(info.get("classLoader"));
        }
    }

    @Test
    public void testGetLoadedClassCount() {
        long count = RuntimeUtils.getLoadedClassCount();
        Assert.assertTrue(count >= 0);
    }

    @Test
    public void testGetClassLoaderHierarchy() {
        List<String> hierarchy = RuntimeUtils.getClassLoaderHierarchy();
        Assert.assertNotNull(hierarchy);
        Assert.assertFalse(hierarchy.isEmpty());
    }

    // ==================== JVM运行信息测试 ====================

    @Test
    public void testGetJvmUptime() {
        long uptime = RuntimeUtils.getJvmUptime();
        Assert.assertTrue(uptime > 0);
    }

    @Test
    public void testGetJvmArguments() {
        List<String> arguments = RuntimeUtils.getJvmArguments();
        Assert.assertNotNull(arguments);
    }

    @Test
    public void testGetDeadlockThreads() {
        List<Map<String, Object>> deadlocks = RuntimeUtils.getDeadlockThreads();
        Assert.assertNotNull(deadlocks);
    }

    @Test
    public void testGetAllThreadInfo() {
        List<Map<String, Object>> threadInfos = RuntimeUtils.getAllThreadInfo();
        Assert.assertNotNull(threadInfos);
        Assert.assertFalse(threadInfos.isEmpty());
        for (Map<String, Object> info : threadInfos) {
            Assert.assertNotNull(info.get("id"));
            Assert.assertNotNull(info.get("name"));
            Assert.assertNotNull(info.get("state"));
        }
    }

    @Test
    public void testGetThreadSummary() {
        Map<String, Integer> summary = RuntimeUtils.getThreadSummary();
        Assert.assertNotNull(summary);
        Assert.assertTrue(summary.get("totalStarted") >= 0);
        Assert.assertTrue(summary.get("peak") >= 0);
        Assert.assertTrue(summary.get("current") >= 0);
        Assert.assertTrue(summary.get("daemon") >= 0);
    }

    // ==================== 系统资源信息测试 ====================

    @Test
    public void testGetSystemCpuLoad() {
        double cpuLoad = RuntimeUtils.getSystemCpuLoad();
        // CPU使用率可能在某些平台上不可用（返回-1.0）
        Assert.assertTrue(cpuLoad >= -1.0 && cpuLoad <= 1.0);
    }

    @Test
    public void testGetProcessCpuLoad() {
        double cpuLoad = RuntimeUtils.getProcessCpuLoad();
        // CPU使用率可能在某些平台上不可用（返回-1.0）
        Assert.assertTrue(cpuLoad >= -1.0 && cpuLoad <= 1.0);
    }

    @Test
    public void testGetTotalPhysicalMemory() {
        long totalMemory = RuntimeUtils.getTotalPhysicalMemory();
        // 在某些平台上可能返回-1（不可用）
        Assert.assertTrue(totalMemory > 0 || totalMemory == -1);
    }

    @Test
    public void testGetAvailablePhysicalMemory() {
        long availableMemory = RuntimeUtils.getAvailablePhysicalMemory();
        // 在某些平台上可能返回-1（不可用）
        Assert.assertTrue(availableMemory > 0 || availableMemory == -1);
    }

    @Test
    public void testGetDiskSpace() {
        Map<String, Long> diskSpace = RuntimeUtils.getDiskSpace("/");
        Assert.assertNotNull(diskSpace);
        Assert.assertTrue(diskSpace.get("total") > 0);
        Assert.assertTrue(diskSpace.get("free") >= 0);
        Assert.assertTrue(diskSpace.get("usable") >= 0);
    }

    @Test
    public void testGetDiskSpaceWindows() {
        // Windows路径测试
        Map<String, Long> diskSpace = RuntimeUtils.getDiskSpace("C:");
        Assert.assertNotNull(diskSpace);
    }
}
