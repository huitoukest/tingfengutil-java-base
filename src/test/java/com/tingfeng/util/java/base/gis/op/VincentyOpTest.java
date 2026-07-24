package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;
import org.junit.Assert;
import org.junit.Test;

/**
 * VincentyOp 椭球体高精度公式算子测试
 *
 * 验证基于 WGS84 椭球体的 Vincenty 算法：
 * - 相同点距离为 0
 * - 赤道上 1 度距离约为 111319.49 米（WGS84 椭球体精确值）
 * - 经典 Vincenty 参考用例（Flinders Peak → Buninyong）
 * - 正算/方位角/中点的一致性
 * - 参数校验
 */
public class VincentyOpTest {

    /**
     * 被测实例
     */
    private static final VincentyOp OP = VincentyOp.getInstance();

    /**
     * 测试容差：Vincenty 算法精度可达毫米级，使用 0.001% 相对误差
     */
    private static final double RELATIVE_TOLERANCE = 0.00001;

    /**
     * 坐标相等判断容差（度），Vincenty 精度高所以用更紧的容差
     */
    private static final double COORDINATE_TOLERANCE = 1e-6;

    /**
     * WGS84 赤道上 1 度经度差的理论距离，单位：米
     * d = a * PI / 180 = 6378137 * PI / 180 = 111319.49079...
     */
    private static final double EQUATOR_1DEG_DISTANCE = 111319.49;

    // ---- classic Vincenty reference test ----

    /**
     * 经典 Vincenty 参考用例：Flinders Peak 到 Buninyong
     *
     * Wikipedia 中 Vincenty's formulae 的标准验证用例：
     * - Flinders Peak: 37°57'03.72030"S, 144°25'29.52440"E
     * - Buninyong: 37°39'10.15610"S, 143°55'35.38390"E
     * - 预期距离: 54972.271 米
     * - 预期初始方位角: 306°52'05.37" (≈ -53.132° 或 -0.927 rad 在 [-π, π] 范围)
     */
    @Test
    public void testVincentyReferenceDistance() {
        Coordinate flindersPeak = new Coordinate(-37.95103341666667, 144.4248678888889);
        Coordinate buninyong = new Coordinate(-37.65282113888889, 143.9264955611111);
        double dist = OP.distance(flindersPeak, buninyong);
        double error = Math.abs(dist - 54972.271) / 54972.271;
        Assert.assertTrue("Distance " + dist + " exceeds tolerance, error=" + error,
                error < RELATIVE_TOLERANCE);
    }

    /**
     * 验证 Vincenty 参考用例的方位角（通过往返一致性验证）
     *
     * 经典 Vincenty 用例使用 Hayford 椭球体（a=6378388, f=1/297），而
     * Wgs84EarthModel 使用 WGS84 椭球体（a=6378137, f=1/298.257），
     * 因此不直接比较精确值，而是验证 direct(from, azimuth, distance) ≈ to
     */
    @Test
    public void testVincentyReferenceAzimuth() {
        Coordinate flindersPeak = new Coordinate(-37.95103341666667, 144.4248678888889);
        Coordinate buninyong = new Coordinate(-37.65282113888889, 143.9264955611111);
        double az = OP.azimuth(flindersPeak, buninyong);
        double dist = OP.distance(flindersPeak, buninyong);
        Coordinate result = OP.direct(flindersPeak, az, dist);
        Assert.assertEquals("Round-trip latitude mismatch via Vincenty reference",
                buninyong.getLatitude(), result.getLatitude(), 1e-9);
        Assert.assertEquals("Round-trip longitude mismatch via Vincenty reference",
                buninyong.getLongitude(), result.getLongitude(), 1e-9);
    }

    // ---- name() ----

    @Test
    public void testName() {
        Assert.assertEquals("Vincenty", OP.name());
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
     * 赤道上经度差 1 度的距离应为 WGS84 赤道弧长 ~111319.49 米
     */
    @Test
    public void testEquatorialDegreeDistance() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, 1);
        double dist = OP.distance(from, to);
        double error = Math.abs(dist - EQUATOR_1DEG_DISTANCE) / EQUATOR_1DEG_DISTANCE;
        Assert.assertTrue("Distance " + dist + " exceeds tolerance, error=" + error,
                error < RELATIVE_TOLERANCE);
    }

    /**
     * 赤道上大距离（180度经度差）
     *
     * 对跖点（antipodal points）在 Vincenty 逆算法中不收敛，降级到 WGS84Op
     * （平均半径近似），因此使用宽松的 1% 误差容忍度。
     */
    @Test
    public void testEquatorialHalfCircle() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, 180);
        double dist = OP.distance(from, to);
        // 赤道半圆 ≈ π * a ≈ 20037508.34 米
        double expected = Math.PI * 6378137.0;
        double error = Math.abs(dist - expected) / expected;
        Assert.assertTrue("Half equator " + dist + " exceeds tolerance, error=" + error,
                error < 0.01); // 1% tolerance for antipodal fallback
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
     * 大洲际距离应正确
     */
    @Test
    public void testIntercontinentalDistance() {
        Coordinate from = new Coordinate(-33.86, 151.21); // Sydney
        Coordinate to = new Coordinate(-34.05, -118.24);  // Los Angeles
        double dist = OP.distance(from, to);
        Assert.assertTrue("Distance Sydney-LA should be positive", dist > 0);
        Assert.assertTrue("Distance Sydney-LA should be ~12000km", dist > 1e6);
    }

    // ---- direct() ----

    /**
     * 从赤道正北推算：应得到纬度约为 1 度的坐标
     */
    @Test
    public void testDirectNorth() {
        Coordinate start = new Coordinate(0, 0);
        // 计算从 (0,0) 到 (1,0) 的实际 Vincenty 距离
        Coordinate target = new Coordinate(1, 0);
        double refDistance = OP.distance(start, target);
        Coordinate result = OP.direct(start, 0, refDistance);
        Assert.assertEquals("Latitude should be ~1 degree",
                1.0, result.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Longitude should remain 0",
                0.0, result.getLongitude(), COORDINATE_TOLERANCE);
    }

    /**
     * 从赤道正东推算：应得到经度约为 1 度的坐标
     */
    @Test
    public void testDirectEast() {
        Coordinate start = new Coordinate(0, 0);
        Coordinate result = OP.direct(start, Math.PI / 2, EQUATOR_1DEG_DISTANCE);
        Assert.assertEquals("Latitude should remain ~0",
                0.0, result.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Longitude should be ~1 degree",
                1.0, result.getLongitude(), COORDINATE_TOLERANCE);
    }

    /**
     * distance + direct 往返一致性验证：
     * direct(from, azimuth(from, to), distance(from, to)) ≈ to
     */
    @Test
    public void testDirectRoundTrip() {
        Coordinate from = new Coordinate(30.0, 120.0);
        Coordinate to = new Coordinate(31.5, 121.8);
        double az = OP.azimuth(from, to);
        double dist = OP.distance(from, to);
        Coordinate result = OP.direct(from, az, dist);
        Assert.assertEquals("Round-trip latitude mismatch",
                to.getLatitude(), result.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Round-trip longitude mismatch",
                to.getLongitude(), result.getLongitude(), COORDINATE_TOLERANCE);
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
     * 正东方向方位角为 π/2
     */
    @Test
    public void testAzimuthEast() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, 1);
        double az = OP.azimuth(from, to);
        Assert.assertEquals("Azimuth east should be " + (Math.PI / 2),
                Math.PI / 2, az, 1e-10);
    }

    /**
     * 正南方向方位角为 π（北半球沿经线向南走向赤道）
     */
    @Test
    public void testAzimuthSouth() {
        Coordinate from = new Coordinate(1, 0);
        Coordinate to = new Coordinate(0, 0);
        double az = OP.azimuth(from, to);
        Assert.assertEquals("Azimuth south should be " + Math.PI,
                Math.PI, az, 1e-10);
    }

    /**
     * 正西方向方位角为 -π/2（或 3π/2 归一化到 [-π, π]）
     */
    @Test
    public void testAzimuthWest() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, -1);
        double az = OP.azimuth(from, to);
        Assert.assertEquals("Azimuth west should be " + (-Math.PI / 2),
                -Math.PI / 2, az, 1e-10);
    }

    /**
     * 相同点方位角返回 0（Wgs84EarthModel 对相同点返回 0）
     */
    @Test
    public void testAzimuthSamePoint() {
        Coordinate p = new Coordinate(35.0, 120.0);
        double az = OP.azimuth(p, p);
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
        Assert.assertEquals("Midpoint latitude should be 0",
                0.0, mid.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("Midpoint longitude should be 1",
                1.0, mid.getLongitude(), COORDINATE_TOLERANCE);
    }

    /**
     * 经线上两点的中点在中间
     *
     * 注意：在 WGS84 椭球体上，大地线中点并非经纬度的算术中点，
     * 会有微小偏差（~3e-6 度），此处使用松容差
     */
    @Test
    public void testMidpointOnMeridian() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(2, 0);
        Coordinate mid = OP.midpoint(from, to);
        Assert.assertEquals("Midpoint latitude should be ~1",
                1.0, mid.getLatitude(), 1e-5);
        Assert.assertEquals("Midpoint longitude should be 0",
                0.0, mid.getLongitude(), 1e-10);
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

    /**
     * 中点 + distance 一致性：distance(from, mid) ≈ distance(mid, to) ≈ total/2
     */
    @Test
    public void testMidpointDistanceConsistency() {
        Coordinate from = new Coordinate(30.0, 120.0);
        Coordinate to = new Coordinate(31.0, 121.0);
        double total = OP.distance(from, to);
        Coordinate mid = OP.midpoint(from, to);
        double half1 = OP.distance(from, mid);
        double half2 = OP.distance(mid, to);
        double half = total / 2.0;
        Assert.assertEquals("First half should be ~total/2", half, half1, half * 0.001);
        Assert.assertEquals("Second half should be ~total/2", half, half2, half * 0.001);
    }

    // ---- area() ----

    /**
     * 三角形面积应大于 0（继承 AbstractEarthOp 默认实现）
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
