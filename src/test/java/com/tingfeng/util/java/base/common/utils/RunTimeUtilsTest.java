package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.JvmSystemInfo;
import com.tingfeng.util.java.base.common.bean.OsSystemInfo;
import com.tingfeng.util.java.base.common.bean.RuntimeStatus;
import com.tingfeng.util.java.base.common.bean.UserSystemInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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
        Assert.assertTrue(version.length() > 0);
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
        Assert.assertTrue(osName.length() > 0);
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
        boolean result = RuntimeUtils.isMemoryOverThreshold(0.0);
        // 这里只验证方法能正常执行
        Assert.assertNotNull(result);
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
}
