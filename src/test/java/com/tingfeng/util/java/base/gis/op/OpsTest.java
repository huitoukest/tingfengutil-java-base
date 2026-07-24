package com.tingfeng.util.java.base.gis.op;

import org.junit.Assert;
import org.junit.Test;

/**
 * Ops 算子注册中心和通用工具类测试
 *
 * 验证：
 * - 坐标校验方法（合法/非法值）
 * - 单位转换方法（米/公里互转）
 * - 经度归一化方法（正负数、边界值）
 * - 算子和角度转换的基础功能
 */
public class OpsTest {

    // ========== validateLatitude ==========

    /**
     * 合法纬度：边界值 0
     */
    @Test
    public void testValidateLatitudeZero() {
        Ops.validateLatitude(0);
    }

    /**
     * 合法纬度：最大值 90
     */
    @Test
    public void testValidateLatitudeMax() {
        Ops.validateLatitude(90);
    }

    /**
     * 合法纬度：最小值 -90
     */
    @Test
    public void testValidateLatitudeMin() {
        Ops.validateLatitude(-90);
    }

    /**
     * 合法纬度：中间值
     */
    @Test
    public void testValidateLatitudeMid() {
        Ops.validateLatitude(45.5);
        Ops.validateLatitude(-45.5);
    }

    /**
     * 非法纬度：超出最大值
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValidateLatitudeExceedMax() {
        Ops.validateLatitude(90.1);
    }

    /**
     * 非法纬度：低于最小值
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValidateLatitudeBelowMin() {
        Ops.validateLatitude(-90.1);
    }

    // ========== validateLongitude ==========

    /**
     * 合法经度：边界值 0
     */
    @Test
    public void testValidateLongitudeZero() {
        Ops.validateLongitude(0);
    }

    /**
     * 合法经度：最大值 180
     */
    @Test
    public void testValidateLongitudeMax() {
        Ops.validateLongitude(180);
    }

    /**
     * 合法经度：最小值 -180
     */
    @Test
    public void testValidateLongitudeMin() {
        Ops.validateLongitude(-180);
    }

    /**
     * 合法经度：中间值
     */
    @Test
    public void testValidateLongitudeMid() {
        Ops.validateLongitude(120.5);
        Ops.validateLongitude(-120.5);
    }

    /**
     * 非法经度：超出最大值
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValidateLongitudeExceedMax() {
        Ops.validateLongitude(180.1);
    }

    /**
     * 非法经度：低于最小值
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValidateLongitudeBelowMin() {
        Ops.validateLongitude(-180.1);
    }

    // ========== validateCoordinate ==========

    /**
     * 合法坐标
     */
    @Test
    public void testValidateCoordinateValid() {
        Ops.validateCoordinate(30, 120);
        Ops.validateCoordinate(-90, -180);
    }

    /**
     * 非法坐标：纬度超出
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValidateCoordinateInvalidLat() {
        Ops.validateCoordinate(91, 0);
    }

    /**
     * 非法坐标：经度超出
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValidateCoordinateInvalidLon() {
        Ops.validateCoordinate(0, 181);
    }

    // ========== toRadians ==========

    /**
     * 0 度转为 0 弧度
     */
    @Test
    public void testToRadiansZero() {
        Assert.assertEquals(0, Ops.toRadians(0), 1e-15);
    }

    /**
     * 90 度转为 PI/2
     */
    @Test
    public void testToRadiansNinety() {
        Assert.assertEquals(Math.PI / 2, Ops.toRadians(90), 1e-15);
    }

    /**
     * 180 度转为 PI
     */
    @Test
    public void testToRadiansOneEighty() {
        Assert.assertEquals(Math.PI, Ops.toRadians(180), 1e-15);
    }

    /**
     * -90 度转为 -PI/2
     */
    @Test
    public void testToRadiansNegative() {
        Assert.assertEquals(-Math.PI / 2, Ops.toRadians(-90), 1e-15);
    }

    // ========== toDegrees ==========

    /**
     * 0 弧度转为 0 度
     */
    @Test
    public void testToDegreesZero() {
        Assert.assertEquals(0, Ops.toDegrees(0), 1e-15);
    }

    /**
     * PI/2 转为 90 度
     */
    @Test
    public void testToDegreesPiHalf() {
        Assert.assertEquals(90, Ops.toDegrees(Math.PI / 2), 1e-15);
    }

    /**
     * PI 转为 180 度
     */
    @Test
    public void testToDegreesPi() {
        Assert.assertEquals(180, Ops.toDegrees(Math.PI), 1e-15);
    }

    /**
     * -PI/2 转为 -90 度
     */
    @Test
    public void testToDegreesNegative() {
        Assert.assertEquals(-90, Ops.toDegrees(-Math.PI / 2), 1e-15);
    }

    // ========== metersToKilometers ==========

    /**
     * 0 米转为 0 公里
     */
    @Test
    public void testMetersToKmZero() {
        Assert.assertEquals(0, Ops.metersToKilometers(0), 1e-15);
    }

    /**
     * 1000 米转为 1 公里
     */
    @Test
    public void testMetersToKmPositive() {
        Assert.assertEquals(1, Ops.metersToKilometers(1000), 1e-15);
    }

    /**
     * 1500 米转为 1.5 公里
     */
    @Test
    public void testMetersToKmFraction() {
        Assert.assertEquals(1.5, Ops.metersToKilometers(1500), 1e-15);
    }

    /**
     * -1000 米转为 -1 公里
     */
    @Test
    public void testMetersToKmNegative() {
        Assert.assertEquals(-1, Ops.metersToKilometers(-1000), 1e-15);
    }

    // ========== kilometersToMeters ==========

    /**
     * 0 公里转为 0 米
     */
    @Test
    public void testKmToMetersZero() {
        Assert.assertEquals(0, Ops.kilometersToMeters(0), 1e-15);
    }

    /**
     * 1 公里转为 1000 米
     */
    @Test
    public void testKmToMetersPositive() {
        Assert.assertEquals(1000, Ops.kilometersToMeters(1), 1e-15);
    }

    /**
     * 1.5 公里转为 1500 米
     */
    @Test
    public void testKmToMetersFraction() {
        Assert.assertEquals(1500, Ops.kilometersToMeters(1.5), 1e-15);
    }

    /**
     * -1 公里转为 -1000 米
     */
    @Test
    public void testKmToMetersNegative() {
        Assert.assertEquals(-1000, Ops.kilometersToMeters(-1), 1e-15);
    }

    // ========== normalizeLongitude ==========

    /**
     * 正数经度：120 保持不变
     */
    @Test
    public void testNormalizeLonPositive() {
        Assert.assertEquals(120, Ops.normalizeLongitude(120), 1e-15);
    }

    /**
     * 负数经度：-120 保持不变
     */
    @Test
    public void testNormalizeLonNegative() {
        Assert.assertEquals(-120, Ops.normalizeLongitude(-120), 1e-15);
    }

    /**
     * 0 度保持不变
     */
    @Test
    public void testNormalizeLonZero() {
        Assert.assertEquals(0, Ops.normalizeLongitude(0), 1e-15);
    }

    /**
     * 190 度映射为 -170 度
     */
    @Test
    public void testNormalizeLonPositiveWrap() {
        Assert.assertEquals(-170, Ops.normalizeLongitude(190), 1e-15);
    }

    /**
     * -190 度映射为 170 度
     */
    @Test
    public void testNormalizeLonNegativeWrap() {
        Assert.assertEquals(170, Ops.normalizeLongitude(-190), 1e-15);
    }

    /**
     * 上边界：180 度映射为 -180 度
     */
    @Test
    public void testNormalizeLonUpperBound() {
        Assert.assertEquals(-180, Ops.normalizeLongitude(180), 1e-15);
    }

    /**
     * 下边界：-180 度保持不变
     */
    @Test
    public void testNormalizeLonLowerBound() {
        Assert.assertEquals(-180, Ops.normalizeLongitude(-180), 1e-15);
    }

    /**
     * 整圆：360 度映射为 0 度
     */
    @Test
    public void testNormalizeLonFullCircle() {
        Assert.assertEquals(0, Ops.normalizeLongitude(360), 1e-15);
    }

    /**
     * 负整圆：-360 度映射为 0 度
     */
    @Test
    public void testNormalizeLonNegativeFullCircle() {
        Assert.assertEquals(0, Ops.normalizeLongitude(-360), 1e-15);
    }

    /**
     * 大正数：370 度映射为 10 度
     */
    @Test
    public void testNormalizeLonLargePositive() {
        Assert.assertEquals(10, Ops.normalizeLongitude(370), 1e-15);
    }

    /**
     * 大负数：-370 度映射为 -10 度
     */
    @Test
    public void testNormalizeLonLargeNegative() {
        Assert.assertEquals(-10, Ops.normalizeLongitude(-370), 1e-15);
    }

    // ========== normalizeLongitudePositive ==========

    /**
     * 正数经度：120 保持不变
     */
    @Test
    public void testNormalizeLonPosPositive() {
        Assert.assertEquals(120, Ops.normalizeLongitudePositive(120), 1e-15);
    }

    /**
     * 负数经度：-120 映射为 240 度
     */
    @Test
    public void testNormalizeLonPosNegative() {
        Assert.assertEquals(240, Ops.normalizeLongitudePositive(-120), 1e-15);
    }

    /**
     * 0 度保持不变
     */
    @Test
    public void testNormalizeLonPosZero() {
        Assert.assertEquals(0, Ops.normalizeLongitudePositive(0), 1e-15);
    }

    /**
     * 180 度保持不变
     */
    @Test
    public void testNormalizeLonPosUpperBound() {
        Assert.assertEquals(180, Ops.normalizeLongitudePositive(180), 1e-15);
    }

    /**
     * 整圆：360 度映射为 0 度
     */
    @Test
    public void testNormalizeLonPosFullCircle() {
        Assert.assertEquals(0, Ops.normalizeLongitudePositive(360), 1e-15);
    }

    /**
     * 负数环绕：-190 映射为 170 度
     */
    @Test
    public void testNormalizeLonPosNegativeWrap() {
        Assert.assertEquals(170, Ops.normalizeLongitudePositive(-190), 1e-15);
    }

    // ========== 算子实例 ==========

    /**
     * WGS84 算子实例可访问
     */
    @Test
    public void testWGS84Instance() {
        Assert.assertNotNull(Ops.WGS84);
        Assert.assertEquals("WGS84", Ops.WGS84.name());
    }

    /**
     * Vincenty 算子实例可访问
     */
    @Test
    public void testVincentyInstance() {
        Assert.assertNotNull(Ops.VINCENTY);
        Assert.assertEquals("Vincenty", Ops.VINCENTY.name());
    }

    /**
     * Karney 算子实例可访问
     */
    @Test
    public void testKarneyInstance() {
        Assert.assertNotNull(Ops.KARNEY);
        Assert.assertEquals("Karney", Ops.KARNEY.name());
    }
}
