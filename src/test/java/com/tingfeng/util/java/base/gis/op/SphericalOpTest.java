package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;
import org.junit.Assert;
import org.junit.Test;

/**
 * SphericalOp 固定半径球面快速计算算子测试
 *
 * 验证基于固定半径 R=6371000m 的纯球面模型：
 * - 相同点距离为 0
 * - 赤道上 1 度距离约为 111194.93 米 (R * pi/180)
 * - 正算/方位角/中点的一致性
 * - 参数校验
 */
public class SphericalOpTest {

    /**
     * 被测实例
     */
    private static final SphericalOp OP = SphericalOp.getInstance();

    /**
     * 测试容差：球面公式精度约 0.5%
     */
    private static final double RELATIVE_TOLERANCE = 0.005;

    /**
     * 赤道上 1 度经度差的理论距离（基于固定半径 R=6371000m），单位：米
     * d = R * pi / 180 = 6371000 * 0.01745329252 ≈ 111194.93
     */
    private static final double EQUATOR_1DEG_DISTANCE = 6371000.0 * Math.PI / 180.0;

    /**
     * 坐标相等判断容差（度）
     */
    private static final double COORDINATE_TOLERANCE = 0.001;

    @Test
    public void testName() {
        Assert.assertEquals("Spherical", OP.name());
    }

    // ---- distance() ----

    /**
     * 相同点返回距离 0
     */
    @Test
    public void testSamePointDistance() {
        Coordinate p = new Coordinate(35.6895, 139.6917);
        double dist = OP.distance(p, p);
        Assert.assertEquals(0.0, dist, 0.0);
    }

    /**
     * 赤道上经度差 1 度的距离约为 111194.93 米
     */
    @Test
    public void testEquatorDegreeDistance() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, 1);
        double dist = OP.distance(from, to);
        double error = Math.abs(dist - EQUATOR_1DEG_DISTANCE) / EQUATOR_1DEG_DISTANCE;
        Assert.assertTrue("Distance " + dist + " exceeds tolerance, error=" + error,
                error < RELATIVE_TOLERANCE);
    }

    /**
     * 经线上纬度差 1 度的距离应与赤道接近
     */
    @Test
    public void testMeridianDegreeDistance() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(1, 0);
        double dist = OP.distance(from, to);
        double error = Math.abs(dist - EQUATOR_1DEG_DISTANCE) / EQUATOR_1DEG_DISTANCE;
        Assert.assertTrue("Meridian distance " + dist + " exceeds tolerance, error=" + error,
                error < RELATIVE_TOLERANCE);
    }

    /**
     * 距离可交换：distance(A,B) == distance(B,A)
     */
    @Test
    public void testDistanceIsSymmetric() {
        Coordinate a = new Coordinate(30.0, 120.0);
        Coordinate b = new Coordinate(31.0, 121.0);
        double d1 = OP.distance(a, b);
        double d2 = OP.distance(b, a);
        Assert.assertEquals(d1, d2, 1e-6);
    }

    /**
     * 距离支持负坐标（南纬/西经）
     */
    @Test
    public void testDistanceWithNegativeCoordinates() {
        Coordinate from = new Coordinate(-33.86, 151.21); // Sydney
        Coordinate to = new Coordinate(-34.05, -118.24);  // Los Angeles
        double dist = OP.distance(from, to);
        Assert.assertTrue("Distance Sydney-LA should be positive", dist > 0);
        Assert.assertTrue("Distance Sydney-LA should be ~12000km", dist > 1e6);
    }

    /**
     * 已知 Haversine 参考值验证：巴黎 → 伦敦
     * Paris: (48.8566, 2.3522), London: (51.5074, -0.1278)
     * 预期距离约 343976 米
     */
    @Test
    public void testKnownHaversineDistance() {
        Coordinate paris = new Coordinate(48.8566, 2.3522);
        Coordinate london = new Coordinate(51.5074, -0.1278);
        double dist = OP.distance(paris, london);
        // Haversine 理论参考值约 343976m，允许 0.5% 误差
        double expected = 343976.0;
        double error = Math.abs(dist - expected) / expected;
        Assert.assertTrue("Paris-London distance " + dist + " exceeds tolerance, error=" + error,
                error < RELATIVE_TOLERANCE);
    }

    // ---- direct() ----

    /**
     * 从赤道正北推算 1 度：应得到 (1°, 0°)
     */
    @Test
    public void testDirectNorth() {
        Coordinate start = new Coordinate(0, 0);
        Coordinate result = OP.direct(start, 0, EQUATOR_1DEG_DISTANCE);
        Assert.assertEquals("Latitude should be ~1 degree", 1.0, result.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Longitude should remain 0", 0.0, result.getLongitude(), COORDINATE_TOLERANCE);
    }

    /**
     * 从赤道正东推算 1 度：应得到 (0°, 1°)
     */
    @Test
    public void testDirectEast() {
        Coordinate start = new Coordinate(0, 0);
        Coordinate result = OP.direct(start, Math.PI / 2, EQUATOR_1DEG_DISTANCE);
        Assert.assertEquals("Latitude should remain ~0", 0.0, result.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Longitude should be ~1 degree", 1.0, result.getLongitude(), COORDINATE_TOLERANCE);
    }

    /**
     * 从赤道正南推算 1 度：应得到 (-1°, 0°)
     */
    @Test
    public void testDirectSouth() {
        Coordinate start = new Coordinate(0, 0);
        Coordinate result = OP.direct(start, Math.PI, EQUATOR_1DEG_DISTANCE);
        Assert.assertEquals("Latitude should be ~-1 degree", -1.0, result.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Longitude should remain 0", 0.0, result.getLongitude(), COORDINATE_TOLERANCE);
    }

    /**
     * 从赤道正西推算 1 度：应得到 (0°, -1°)
     */
    @Test
    public void testDirectWest() {
        Coordinate start = new Coordinate(0, 0);
        Coordinate result = OP.direct(start, -Math.PI / 2, EQUATOR_1DEG_DISTANCE);
        Assert.assertEquals("Latitude should remain ~0", 0.0, result.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Longitude should be ~-1 degree", -1.0, result.getLongitude(), COORDINATE_TOLERANCE);
    }

    /**
     * 距离为 0 时 direct 返回起点
     */
    @Test
    public void testDirectZeroDistance() {
        Coordinate start = new Coordinate(30.0, 120.0);
        Coordinate result = OP.direct(start, 1.0, 0);
        Assert.assertEquals(start.getLatitude(), result.getLatitude(), 1e-10);
        Assert.assertEquals(start.getLongitude(), result.getLongitude(), 1e-10);
    }

    // ---- azimuth() ----

    /**
     * 正北方向方位角为 0
     */
    @Test
    public void testAzimuthNorth() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(1, 0);
        double az = OP.azimuth(from, to);
        Assert.assertEquals("Azimuth north should be 0", 0.0, az, 1e-10);
    }

    /**
     * 正东方向方位角为 pi/2
     */
    @Test
    public void testAzimuthEast() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, 1);
        double az = OP.azimuth(from, to);
        Assert.assertEquals("Azimuth east should be " + (Math.PI / 2),
                Math.PI / 2, az, 1e-6);
    }

    /**
     * 正南方向方位角为 pi（从北半球沿经线走向赤道）
     */
    @Test
    public void testAzimuthSouth() {
        Coordinate from = new Coordinate(1, 0);
        Coordinate to = new Coordinate(0, 0);
        double az = OP.azimuth(from, to);
        Assert.assertEquals("Azimuth south should be " + Math.PI,
                Math.PI, az, 1e-6);
    }

    /**
     * 正西方向方位角为 -pi/2
     */
    @Test
    public void testAzimuthWest() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, -1);
        double az = OP.azimuth(from, to);
        Assert.assertEquals("Azimuth west should be " + (-Math.PI / 2),
                -Math.PI / 2, az, 1e-6);
    }

    /**
     * 相同点方位角应可为任意值，此处仅验证不抛异常
     */
    @Test
    public void testAzimuthSamePoint() {
        Coordinate p = new Coordinate(35.0, 120.0);
        double az = OP.azimuth(p, p);
        // atan2(0, 0) = 0
        Assert.assertEquals(0.0, az, 0.0);
    }

    // ---- midpoint() ----

    /**
     * 赤道上两点的中点在中间
     */
    @Test
    public void testMidpointOnEquator() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, 2);
        Coordinate mid = OP.midpoint(from, to);
        Assert.assertEquals("Midpoint latitude should be 0", 0.0, mid.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Midpoint longitude should be 1", 1.0, mid.getLongitude(), COORDINATE_TOLERANCE);
    }

    /**
     * 经线上两点的中点在中间
     */
    @Test
    public void testMidpointOnMeridian() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(2, 0);
        Coordinate mid = OP.midpoint(from, to);
        Assert.assertEquals("Midpoint latitude should be ~1", 1.0, mid.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Midpoint longitude should be 0", 0.0, mid.getLongitude(), 1e-10);
    }

    /**
     * 相同点的中点为该点自身
     */
    @Test
    public void testMidpointSamePoint() {
        Coordinate p = new Coordinate(30.0, 120.0);
        Coordinate mid = OP.midpoint(p, p);
        Assert.assertEquals(p.getLatitude(), mid.getLatitude(), 1e-10);
        Assert.assertEquals(p.getLongitude(), mid.getLongitude(), 1e-10);
    }

    // ---- area() ----

    /**
     * 三角形面积应大于 0
     */
    @Test
    public void testAreaTriangle() {
        double[][] triangle = {
                {0, 0},
                {0, 1},
                {1, 0}
        };
        double area = OP.area(triangle);
        Assert.assertTrue("Triangle area should be positive", area > 0);
        // 1度×1度的球面三角形面积大致在 ~6.18e9 m²
        Assert.assertTrue("Triangle area should be reasonable", area > 1e9);
        Assert.assertTrue("Triangle area should be reasonable", area < 1e10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAreaWithNull() {
        OP.area(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAreaWithLessThan3() {
        OP.area(new double[][]{{0, 0}, {1, 1}});
    }

    // ---- null checks ----

    @Test(expected = IllegalArgumentException.class)
    public void testDistanceNullFrom() {
        OP.distance(null, new Coordinate(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDistanceNullTo() {
        OP.distance(new Coordinate(0, 0), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDirectNullPoint() {
        OP.direct(null, 0, 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAzimuthNullFrom() {
        OP.azimuth(null, new Coordinate(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAzimuthNullTo() {
        OP.azimuth(new Coordinate(0, 0), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMidpointNullFrom() {
        OP.midpoint(null, new Coordinate(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMidpointNullTo() {
        OP.midpoint(new Coordinate(0, 0), null);
    }
}
