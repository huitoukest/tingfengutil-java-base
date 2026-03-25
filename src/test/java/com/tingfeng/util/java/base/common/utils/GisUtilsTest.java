package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * 地理相关工具类测试
 */
public class GisUtilsTest {

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
        double expectedRadius = 6371109.0; // 约6371.11km
        Assert.assertEquals("北纬35度的半径应该约为6371109米", expectedRadius, radius, 100.0);
    }

    /**
     * 测试获取地球半径 - 北纬90度（极点）
     */
    @Test
    public void testGetRadius90Degrees() {
        double radius = GisUtils.getRadius(90);
        double expectedRadius = 6356752.0; // WGS84极点半径
        Assert.assertEquals("极点半径应该约为6356752米", expectedRadius, radius, 1.0);
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

    /**
     * 测试计算方位角 - 正北方向
     */
    @Test
    public void testGetBearingNorth() {
        double bearing = GisUtils.getBearing(39.9042, 116.4074, 40.9042, 116.4074);
        Assert.assertEquals("正北方向的方位角应该约为0度", 0.0, bearing, 1.0);
    }

    /**
     * 测试计算方位角 - 正东方向
     */
    @Test
    public void testGetBearingEast() {
        double bearing = GisUtils.getBearing(39.9042, 116.4074, 39.9042, 117.4074);
        Assert.assertEquals("正东方向的方位角应该约为90度", 90.0, bearing, 1.0);
    }

    /**
     * 测试计算方位角 - 正南方向
     */
    @Test
    public void testGetBearingSouth() {
        double bearing = GisUtils.getBearing(39.9042, 116.4074, 38.9042, 116.4074);
        Assert.assertEquals("正南方向的方位角应该约为180度", 180.0, bearing, 1.0);
    }

    /**
     * 测试计算方位角 - 正西方向
     */
    @Test
    public void testGetBearingWest() {
        double bearing = GisUtils.getBearing(39.9042, 116.4074, 39.9042, 115.4074);
        Assert.assertEquals("正西方向的方位角应该约为270度", 270.0, bearing, 1.0);
    }

    /**
     * 测试计算中点
     */
    @Test
    public void testGetMidpoint() {
        double[] midpoint = GisUtils.getMidpoint(39.9042, 116.4074, 31.2304, 121.4737);
        Assert.assertNotNull("中点坐标不应为null", midpoint);
        Assert.assertEquals("中点坐标数组长度应该为2", 2, midpoint.length);
        Assert.assertTrue("中点纬度应该在两点之间", midpoint[0] > 31.2304 && midpoint[0] < 39.9042);
        
        double distance1 = GisUtils.getDistance(39.9042, 116.4074, midpoint[0], midpoint[1]);
        double distance2 = GisUtils.getDistance(midpoint[0], midpoint[1], 31.2304, 121.4737);
        Assert.assertTrue("中点到两点的距离都应该大于0", distance1 > 0 && distance2 > 0);
    }

    /**
     * 测试计算目标点 - 向北移动
     */
    @Test
    public void testGetDestinationPointNorth() {
        double[] destination = GisUtils.getDestinationPoint(39.9042, 116.4074, 0, 1000);
        Assert.assertNotNull("目标点坐标不应为null", destination);
        Assert.assertEquals("目标点坐标数组长度应该为2", 2, destination.length);
        Assert.assertTrue("向北移动后纬度应该增加", destination[0] > 39.9042);
        Assert.assertEquals("向北移动时经度应该基本不变", 116.4074, destination[1], 0.01);
    }

    /**
     * 测试计算目标点 - 向东移动
     */
    @Test
    public void testGetDestinationPointEast() {
        double[] destination = GisUtils.getDestinationPoint(39.9042, 116.4074, 90, 1000);
        Assert.assertNotNull("目标点坐标不应为null", destination);
        Assert.assertEquals("目标点坐标数组长度应该为2", 2, destination.length);
        Assert.assertEquals("向东移动时纬度应该基本不变", 39.9042, destination[0], 0.01);
        Assert.assertTrue("向东移动后经度应该增加", destination[1] > 116.4074);
    }

    /**
     * 测试计算多边形面积 - 三角形
     */
    @Test
    public void testGetPolygonAreaTriangle() {
        double[][] triangle = {
            {39.9042, 116.4074},
            {39.9142, 116.4174},
            {39.9042, 116.4174}
        };
        double area = GisUtils.getPolygonArea(triangle);
        Assert.assertTrue("三角形面积应该大于0", area > 0);
    }

    /**
     * 测试计算多边形面积 - 矩形
     */
    @Test
    public void testGetPolygonAreaRectangle() {
        double[][] rectangle = {
            {39.9042, 116.4074},
            {39.9142, 116.4074},
            {39.9142, 116.4174},
            {39.9042, 116.4174}
        };
        double area = GisUtils.getPolygonArea(rectangle);
        Assert.assertTrue("矩形面积应该大于0", area > 0);
    }

    /**
     * 测试计算多边形面积 - 空数组
     */
    @Test
    public void testGetPolygonAreaEmpty() {
        double[][] empty = {};
        double area = GisUtils.getPolygonArea(empty);
        Assert.assertEquals("空多边形的面积应该为0", 0.0, area, 0.0);
    }

    /**
     * 测试计算路径总距离
     */
    @Test
    public void testGetPathDistance() {
        double[][] path = {
            {39.9042, 116.4074},
            {39.9142, 116.4174},
            {39.9242, 116.4274}
        };
        double distance = GisUtils.getPathDistance(path);
        Assert.assertTrue("路径总距离应该大于0", distance > 0);
    }

    /**
     * 测试计算路径总距离 - 空数组
     */
    @Test
    public void testGetPathDistanceEmpty() {
        double[][] empty = {};
        double distance = GisUtils.getPathDistance(empty);
        Assert.assertEquals("空路径的距离应该为0", 0.0, distance, 0.0);
    }

    /**
     * 测试计算路径总距离 - 单点
     */
    @Test
    public void testGetPathDistanceSinglePoint() {
        double[][] singlePoint = {{39.9042, 116.4074}};
        double distance = GisUtils.getPathDistance(singlePoint);
        Assert.assertEquals("单点路径的距离应该为0", 0.0, distance, 0.0);
    }

    /**
     * 测试判断点是否在多边形内 - 点在多边形内
     */
    @Test
    public void testIsPointInPolygonInside() {
        double[][] polygon = {
            {39.9042, 116.4074},
            {39.9142, 116.4074},
            {39.9142, 116.4174},
            {39.9042, 116.4174}
        };
        boolean inside = GisUtils.isPointInPolygon(39.9092, 116.4124, polygon);
        Assert.assertTrue("点应该在多边形内", inside);
    }

    /**
     * 测试判断点是否在多边形内 - 点在多边形外
     */
    @Test
    public void testIsPointInPolygonOutside() {
        double[][] polygon = {
            {39.9042, 116.4074},
            {39.9142, 116.4074},
            {39.9142, 116.4174},
            {39.9042, 116.4174}
        };
        boolean inside = GisUtils.isPointInPolygon(39.9242, 116.4274, polygon);
        Assert.assertFalse("点应该在多边形外", inside);
    }

    /**
     * 测试判断点是否在多边形内 - 空多边形
     */
    @Test
    public void testIsPointInPolygonEmpty() {
        double[][] empty = {};
        boolean inside = GisUtils.isPointInPolygon(39.9092, 116.4124, empty);
        Assert.assertFalse("空多边形应该返回false", inside);
    }

    /**
     * 测试计算点到线段的距离 - 点在线段上
     */
    @Test
    public void testGetDistanceToLineOnLine() {
        double distance = GisUtils.getDistanceToLine(39.9092, 116.4124, 39.9042, 116.4074, 39.9142, 116.4174);
        Assert.assertTrue("点到线段的距离应该大于0", distance > 0);
    }

    /**
     * 测试计算点到线段的距离 - 点在线段端点
     */
    @Test
    public void testGetDistanceToLineAtEndpoint() {
        double distance = GisUtils.getDistanceToLine(39.9042, 116.4074, 39.9042, 116.4074, 39.9142, 116.4174);
        Assert.assertEquals("点在线段端点时距离应该为0", 0.0, distance, 0.1);
    }

    /**
     * 测试十进制转度分秒
     */
    @Test
    public void testDecimalToDMS() {
        double[] dms = GisUtils.decimalToDMS(116.404);
        Assert.assertEquals("度应该为116", 116.0, dms[0], 0.0);
        Assert.assertEquals("分应该为24", 24.0, dms[1], 0.0);
        Assert.assertEquals("秒应该约为14.4", 14.4, dms[2], 0.1);
    }

    /**
     * 测试度分秒转十进制
     */
    @Test
    public void testDmsToDecimal() {
        double decimal = GisUtils.dmsToDecimal(116, 24, 14.4);
        Assert.assertEquals("十进制应该约为116.404", 116.404, decimal, 0.001);
    }

    /**
     * 测试十进制转度分秒字符串
     */
    @Test
    public void testDecimalToDMSString() {
        String dmsString = GisUtils.decimalToDMSString(116.404);
        Assert.assertNotNull("度分秒字符串不应为null", dmsString);
        Assert.assertTrue("度分秒字符串应该包含度符号", dmsString.contains("°"));
        Assert.assertTrue("度分秒字符串应该包含分符号", dmsString.contains("'"));
        Assert.assertTrue("度分秒字符串应该包含秒符号", dmsString.contains("\""));
    }

    /**
     * 测试WGS84转GCJ02
     */
    @Test
    public void testWgs84ToGcj02() {
        double[] gcj02 = GisUtils.wgs84ToGcj02(39.9042, 116.4074);
        Assert.assertNotNull("GCJ02坐标不应为null", gcj02);
        Assert.assertEquals("GCJ02坐标数组长度应该为2", 2, gcj02.length);
        Assert.assertTrue("GCJ02纬度应该与WGS84纬度不同", Math.abs(gcj02[0] - 39.9042) > 0.001);
        Assert.assertTrue("GCJ02经度应该与WGS84经度不同", Math.abs(gcj02[1] - 116.4074) > 0.001);
    }

    /**
     * 测试GCJ02转WGS84
     */
    @Test
    public void testGcj02ToWgs84() {
        double[] wgs84 = GisUtils.gcj02ToWgs84(39.9042, 116.4074);
        Assert.assertNotNull("WGS84坐标不应为null", wgs84);
        Assert.assertEquals("WGS84坐标数组长度应该为2", 2, wgs84.length);
    }

    /**
     * 测试WGS84转GCJ02再转回WGS84
     */
    @Test
    public void testWgs84Gcj02RoundTrip() {
        double originalLat = 39.9042;
        double originalLng = 116.4074;
        double[] gcj02 = GisUtils.wgs84ToGcj02(originalLat, originalLng);
        double[] wgs84 = GisUtils.gcj02ToWgs84(gcj02[0], gcj02[1]);
        Assert.assertEquals("往返转换后纬度应该接近原值", originalLat, wgs84[0], 0.0001);
        Assert.assertEquals("往返转换后经度应该接近原值", originalLng, wgs84[1], 0.0001);
    }

    /**
     * 测试GCJ02转BD09
     */
    @Test
    public void testGcj02ToBd09() {
        double[] bd09 = GisUtils.gcj02ToBd09(39.9042, 116.4074);
        Assert.assertNotNull("BD09坐标不应为null", bd09);
        Assert.assertEquals("BD09坐标数组长度应该为2", 2, bd09.length);
        Assert.assertTrue("BD09纬度应该与GCJ02纬度不同", Math.abs(bd09[0] - 39.9042) > 0.001);
        Assert.assertTrue("BD09经度应该与GCJ02经度不同", Math.abs(bd09[1] - 116.4074) > 0.001);
    }

    /**
     * 测试BD09转GCJ02
     */
    @Test
    public void testBd09ToGcj02() {
        double[] gcj02 = GisUtils.bd09ToGcj02(39.9042, 116.4074);
        Assert.assertNotNull("GCJ02坐标不应为null", gcj02);
        Assert.assertEquals("GCJ02坐标数组长度应该为2", 2, gcj02.length);
    }

    /**
     * 测试GCJ02转BD09再转回GCJ02
     */
    @Test
    public void testGcj02Bd09RoundTrip() {
        double originalLat = 39.9042;
        double originalLng = 116.4074;
        double[] bd09 = GisUtils.gcj02ToBd09(originalLat, originalLng);
        double[] gcj02 = GisUtils.bd09ToGcj02(bd09[0], bd09[1]);
        Assert.assertEquals("往返转换后纬度应该接近原值", originalLat, gcj02[0], 0.0001);
        Assert.assertEquals("往返转换后经度应该接近原值", originalLng, gcj02[1], 0.0001);
    }

    /**
     * 测试WGS84转BD09
     */
    @Test
    public void testWgs84ToBd09() {
        double[] bd09 = GisUtils.wgs84ToBd09(39.9042, 116.4074);
        Assert.assertNotNull("BD09坐标不应为null", bd09);
        Assert.assertEquals("BD09坐标数组长度应该为2", 2, bd09.length);
        Assert.assertTrue("BD09纬度应该与WGS84纬度不同", Math.abs(bd09[0] - 39.9042) > 0.001);
        Assert.assertTrue("BD09经度应该与WGS84经度不同", Math.abs(bd09[1] - 116.4074) > 0.001);
    }

    /**
     * 测试BD09转WGS84
     */
    @Test
    public void testBd09ToWgs84() {
        double[] wgs84 = GisUtils.bd09ToWgs84(39.9042, 116.4074);
        Assert.assertNotNull("WGS84坐标不应为null", wgs84);
        Assert.assertEquals("WGS84坐标数组长度应该为2", 2, wgs84.length);
    }

    /**
     * 测试WGS84转BD09再转回WGS84
     */
    @Test
    public void testWgs84Bd09RoundTrip() {
        double originalLat = 39.9042;
        double originalLng = 116.4074;
        double[] bd09 = GisUtils.wgs84ToBd09(originalLat, originalLng);
        double[] wgs84 = GisUtils.bd09ToWgs84(bd09[0], bd09[1]);
        Assert.assertEquals("往返转换后纬度应该接近原值", originalLat, wgs84[0], 0.0001);
        Assert.assertEquals("往返转换后经度应该接近原值", originalLng, wgs84[1], 0.0001);
    }

    /**
     * 测试坐标转换 - 中国境外坐标
     */
    @Test
    public void testCoordinateConversionOutsideChina() {
        double[] wgs84ToGcj02 = GisUtils.wgs84ToGcj02(0, 0);
        Assert.assertEquals("中国境外坐标转换后纬度应该不变", 0.0, wgs84ToGcj02[0], 0.0);
        Assert.assertEquals("中国境外坐标转换后经度应该不变", 0.0, wgs84ToGcj02[1], 0.0);
    }

    /**
     * 测试坐标转换 - 完整转换链
     */
    @Test
    public void testFullConversionChain() {
        double originalLat = 39.9042;
        double originalLng = 116.4074;
        
        double[] gcj02 = GisUtils.wgs84ToGcj02(originalLat, originalLng);
        double[] bd09 = GisUtils.gcj02ToBd09(gcj02[0], gcj02[1]);
        double[] backToGcj02 = GisUtils.bd09ToGcj02(bd09[0], bd09[1]);
        double[] backToWgs84 = GisUtils.gcj02ToWgs84(backToGcj02[0], backToGcj02[1]);
        
        Assert.assertEquals("完整转换链后纬度应该接近原值", originalLat, backToWgs84[0], 0.0001);
        Assert.assertEquals("完整转换链后经度应该接近原值", originalLng, backToWgs84[1], 0.0001);
    }
}