package com.tingfeng.util.java.base.math;

import org.junit.Assert;
import org.junit.Test;

/**
 * UnitUtils 单元测试
 */
public class UnitUtilsTest {

    @Test
    public void formatBytesTest() {
        // 0 字节
        Assert.assertEquals("0 B", UnitUtils.formatBytes(0));
        // 1KB 边界: 1023 仍为 B
        Assert.assertEquals("1023 B", UnitUtils.formatBytes(1023));
        // 1KB 边界: 1024 进位
        Assert.assertEquals("1 KB", UnitUtils.formatBytes(1024));
        // 带 1 位小数
        Assert.assertEquals("1.5 KB", UnitUtils.formatBytes(1536));
        // 1 MB
        Assert.assertEquals("1 MB", UnitUtils.formatBytes(1024 * 1024));
        // 1.5 MB
        Assert.assertEquals("1.5 MB", UnitUtils.formatBytes(1536L * 1024));
        // 1 GB
        Assert.assertEquals("1 GB", UnitUtils.formatBytes(1024L * 1024 * 1024));
        // 1.5 GB
        Assert.assertEquals("1.5 GB", UnitUtils.formatBytes(1536L * 1024 * 1024));
        // 1 TB
        Assert.assertEquals("1 TB", UnitUtils.formatBytes(1024L * 1024 * 1024 * 1024));
    }

    @Test
    public void formatBytesMaxValueTest() {
        // Long.MAX ≈ 8 EB（double 舍入: 2^63-1 转 double 为 2^63，/2^60 = 8.0）
        Assert.assertEquals("8 EB", UnitUtils.formatBytes(Long.MAX_VALUE));
        // 2^60-1 转 double 舍入为 2^60 → 1 EB
        Assert.assertEquals("1 EB", UnitUtils.formatBytes((1L << 60) - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void formatBytesNegativeTest() {
        UnitUtils.formatBytes(-1);
    }

    @Test
    public void temperatureKnownValuesTest() {
        // 100°C = 212°F = 373.15K
        Assert.assertEquals(212.0, UnitUtils.celsiusToFahrenheit(100), 1e-9);
        Assert.assertEquals(373.15, UnitUtils.celsiusToKelvin(100), 1e-9);
        // 0°C = 32°F = 273.15K
        Assert.assertEquals(32.0, UnitUtils.celsiusToFahrenheit(0), 1e-9);
        Assert.assertEquals(273.15, UnitUtils.celsiusToKelvin(0), 1e-9);
        // -40°C = -40°F（经典重合点）
        Assert.assertEquals(-40.0, UnitUtils.celsiusToFahrenheit(-40), 1e-9);
        // 华氏 → 摄氏
        Assert.assertEquals(0.0, UnitUtils.fahrenheitToCelsius(32), 1e-9);
        Assert.assertEquals(100.0, UnitUtils.fahrenheitToCelsius(212), 1e-9);
        // 华氏 → 开尔文
        Assert.assertEquals(273.15, UnitUtils.fahrenheitToKelvin(32), 1e-9);
        Assert.assertEquals(373.15, UnitUtils.fahrenheitToKelvin(212), 1e-9);
        // 开尔文 → 摄氏
        Assert.assertEquals(0.0, UnitUtils.kelvinToCelsius(273.15), 1e-9);
        Assert.assertEquals(100.0, UnitUtils.kelvinToCelsius(373.15), 1e-9);
        // 开尔文 → 华氏
        Assert.assertEquals(32.0, UnitUtils.kelvinToFahrenheit(273.15), 1e-9);
        Assert.assertEquals(212.0, UnitUtils.kelvinToFahrenheit(373.15), 1e-9);
        // 绝对零度 0K 合法: -273.15°C / -459.67°F
        Assert.assertEquals(-273.15, UnitUtils.kelvinToCelsius(0), 1e-9);
        Assert.assertEquals(-459.67, UnitUtils.kelvinToFahrenheit(0), 1e-9);
    }

    @Test
    public void temperatureRoundTripTest() {
        // 摄氏 → 华氏 → 摄氏 往返
        for (double c : new double[]{-40, 0, 20, 100, 1000}) {
            Assert.assertEquals(c, UnitUtils.fahrenheitToCelsius(UnitUtils.celsiusToFahrenheit(c)), 1e-9);
        }
        // 摄氏 → 开尔文 → 摄氏 往返
        for (double c : new double[]{-273.15, -40, 0, 100}) {
            Assert.assertEquals(c, UnitUtils.kelvinToCelsius(UnitUtils.celsiusToKelvin(c)), 1e-9);
        }
        // 华氏 → 摄氏 → 华氏 往返
        for (double f : new double[]{-459.67, -40, 32, 98.6, 212}) {
            Assert.assertEquals(f, UnitUtils.celsiusToFahrenheit(UnitUtils.fahrenheitToCelsius(f)), 1e-9);
        }
        // 华氏 → 开尔文 → 华氏 往返
        for (double f : new double[]{-40, 32, 212}) {
            Assert.assertEquals(f, UnitUtils.kelvinToFahrenheit(UnitUtils.fahrenheitToKelvin(f)), 1e-9);
        }
        // 开尔文 → 摄氏 → 开尔文 往返
        for (double k : new double[]{0, 273.15, 373.15, 10000}) {
            Assert.assertEquals(k, UnitUtils.celsiusToKelvin(UnitUtils.kelvinToCelsius(k)), 1e-9);
        }
        // 开尔文 → 华氏 → 开尔文 往返
        for (double k : new double[]{0, 273.15, 373.15, 10000}) {
            Assert.assertEquals(k, UnitUtils.fahrenheitToKelvin(UnitUtils.kelvinToFahrenheit(k)), 1e-9);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void kelvinToCelsiusNegativeTest() {
        UnitUtils.kelvinToCelsius(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void kelvinToFahrenheitNegativeTest() {
        UnitUtils.kelvinToFahrenheit(-273.16);
    }
}
