package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * 地理相关工具类测试
 */
public class GisUtilsTest {

    /**
     * 测试角度转弧度 - 0度
     */
    @Test
    public void testAngleToRadianZero() {
        double radian = GisUtils.angleToRadian(0);
        Assert.assertEquals("0度应该转换为0弧度", 0.0, radian, 0.0001);
    }

    /**
     * 测试角度转弧度 - 90度
     */
    @Test
    public void testAngleToRadian90() {
        double radian = GisUtils.angleToRadian(90);
        Assert.assertEquals("90度应该转换为π/2弧度", Math.PI / 2, radian, 0.0001);
    }

    /**
     * 测试角度转弧度 - 180度
     */
    @Test
    public void testAngleToRadian180() {
        double radian = GisUtils.angleToRadian(180);
        Assert.assertEquals("180度应该转换为π弧度", Math.PI, radian, 0.0001);
    }

    /**
     * 测试角度转弧度 - 45度
     */
    @Test
    public void testAngleToRadian45() {
        double radian = GisUtils.angleToRadian(45);
        Assert.assertEquals("45度应该转换为π/4弧度", Math.PI / 4, radian, 0.0001);
    }

    /**
     * 测试角度转弧度 - 负角度
     */
    @Test
    public void testAngleToRadianNegative() {
        double radian = GisUtils.angleToRadian(-45);
        Assert.assertEquals("-45度应该转换为π/4弧度（绝对值）", Math.PI / 4, radian, 0.0001);
    }

    /**
     * 测试获取地球半径 - 赤道（0度）
     */
    @Test
    public void testGetRadiusEquator() {
        double radius = GisUtils.getRadius(0);
        Assert.assertEquals("赤道半径应该约为6378137米", 6378137.0, radius, 1.0);
    }

    /**
     * 测试获取地球半径 - 北纬35度
     */
    @Test
    public void testGetRadius35Degrees() {
        double radius = GisUtils.getRadius(35);
        double expectedRadius = 6364090.0; // 约6364.09km
        Assert.assertEquals("北纬35度的半径应该约为6364090米", expectedRadius, radius, 100.0);
    }

    /**
     * 测试获取地球半径 - 北纬90度（极点）
     */
    @Test
    public void testGetRadius90Degrees() {
        double radius = GisUtils.getRadius(90);
        Assert.assertTrue("极点半径应该小于赤道半径", radius < 6378137.0);
        Assert.assertTrue("极点半径应该大于6350000米", radius > 6350000.0);
    }

    /**
     * 测试获取地球半径 - 南纬（负值）
     */
    @Test
    public void testGetRadiusNegativeLatitude() {
        double radius1 = GisUtils.getRadius(30);
        double radius2 = GisUtils.getRadius(-30);
        Assert.assertEquals("相同绝对值的纬度应该有相同的半径", radius1, radius2, 0.1);
    }

    /**
     * 测试计算距离 - 同一地点
     */
    @Test
    public void testGetDistanceSameLocation() {
        double distance = GisUtils.getDistance(39.9042, 116.4074, 39.9042, 116.4074);
        Assert.assertEquals("同一地点的距离应该为0", 0.0, distance, 0.1);
    }

    /**
     * 测试计算距离 - 北京到上海
     */
    @Test
    public void testGetDistanceBeijingToShanghai() {
        // 北京：北纬39.9042度，东经116.4074度
        // 上海：北纬31.2304度，东经121.4737度
        double distance = GisUtils.getDistance(39.9042, 116.4074, 31.2304, 121.4737);
        double expectedDistance = 1068000.0; // 约1068公里
        Assert.assertEquals("北京到上海的距离应该约为1068公里", expectedDistance, distance, 10000.0);
    }

    /**
     * 测试计算距离 - 短距离
     */
    @Test
    public void testGetDistanceShort() {
        // 同一城市内的两个地点
        double distance = GisUtils.getDistance(39.9042, 116.4074, 39.9142, 116.4174);
        Assert.assertTrue("短距离应该大于0", distance > 0);
        Assert.assertTrue("短距离应该小于10公里", distance < 10000.0);
    }

    /**
     * 测试计算距离 - 经度相同
     */
    @Test
    public void testGetDistanceSameLongitude() {
        double distance = GisUtils.getDistance(0, 116.4074, 10, 116.4074);
        Assert.assertTrue("经度相同时的距离应该大于0", distance > 0);
    }

    /**
     * 测试计算距离 - 纬度相同
     */
    @Test
    public void testGetDistanceSameLatitude() {
        double distance = GisUtils.getDistance(39.9042, 116.4074, 39.9042, 126.4074);
        Assert.assertTrue("纬度相同时的距离应该大于0", distance > 0);
    }

    /**
     * 测试计算距离 - 跨越赤道
     */
    @Test
    public void testGetDistanceCrossEquator() {
        double distance = GisUtils.getDistance(10, 0, -10, 0);
        Assert.assertTrue("跨越赤道的距离应该大于0", distance > 0);
    }

    /**
     * 测试计算距离 - 跨越本初子午线
     */
    @Test
    public void testGetDistanceCrossPrimeMeridian() {
        double distance = GisUtils.getDistance(0, 10, 0, -10);
        Assert.assertTrue("跨越本初子午线的距离应该大于0", distance > 0);
    }

    /**
     * 测试计算距离 - 极点附近
     */
    @Test
    public void testGetDistanceNearPole() {
        double distance = GisUtils.getDistance(89, 0, 89, 1);
        Assert.assertTrue("极点附近的距离应该大于0", distance > 0);
    }

    /**
     * 测试计算距离 - 对称性
     */
    @Test
    public void testGetDistanceSymmetry() {
        double distance1 = GisUtils.getDistance(39.9042, 116.4074, 31.2304, 121.4737);
        double distance2 = GisUtils.getDistance(31.2304, 121.4737, 39.9042, 116.4074);
        Assert.assertEquals("距离计算应该是对称的", distance1, distance2, 0.1);
    }

    /**
     * 测试计算距离 - 三角不等式
     */
    @Test
    public void testGetDistanceTriangleInequality() {
        double distanceAB = GisUtils.getDistance(39.9042, 116.4074, 31.2304, 121.4737);
        double distanceBC = GisUtils.getDistance(31.2304, 121.4737, 23.1291, 113.2644);
        double distanceAC = GisUtils.getDistance(39.9042, 116.4074, 23.1291, 113.2644);
        
        Assert.assertTrue("应该满足三角不等式：AB + BC >= AC", distanceAB + distanceBC >= distanceAC - 0.1);
    }

    /**
     * 测试计算距离 - 国际日期变更线
     */
    @Test
    public void testGetDistanceDateLine() {
        double distance = GisUtils.getDistance(0, 179, 0, -179);
        Assert.assertTrue("跨越国际日期变更线的距离应该大于0", distance > 0);
    }

    /**
     * 测试计算距离 - 零经度线
     */
    @Test
    public void testGetDistanceZeroLongitude() {
        double distance = GisUtils.getDistance(0, 0, 0, 10);
        Assert.assertTrue("零经度线上的距离应该大于0", distance > 0);
    }

    /**
     * 测试计算距离 - 零纬度线（赤道）
     */
    @Test
    public void testGetDistanceZeroLatitude() {
        double distance = GisUtils.getDistance(0, 0, 0, 10);
        double distance2 = GisUtils.getDistance(10, 0, 10, 10);
        Assert.assertTrue("赤道上的每度距离应该大于其他纬度上的每度距离", distance > distance2);
    }
}