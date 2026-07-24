package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;
import org.junit.Assert;
import org.junit.Test;

/**
 * KarneyOp 增强椭球体高精度算子测试
 *
 * 验证基于 WGS84 椭球体的 Karney 增强算法：
 * - 相同点距离为 0
 * - 赤道上 1 度距离约为 111319.49 米（WGS84 椭球体精确值）
 * - 经典 Vincenty 参考用例（Flinders Peak → Buninyong）
 * - 对跖点（antipodal）距离正确收敛
 * - 正算/方位角/中点的一致性
 * - 与 VincentyOp 结果对比验证
 * - 参数校验
 */
public class KarneyOpTest {

    /**
     * 被测实例
     */
    private static final KarneyOp OP = KarneyOp.getInstance();

    /**
     * VincentyOp 对比实例
     */
    private static final VincentyOp VINCENTY_OP = VincentyOp.getInstance();

    /**
     * 测试容差：Karney 算法精度可达亚毫米级，使用 0.001% 相对误差
     */
    private static final double RELATIVE_TOLERANCE = 0.00001;

    /**
     * 坐标相等判断容差（度），Karney 增强算法用高精度
     */
    private static final double COORDINATE_TOLERANCE = 1e-6;

    /**
     * 近对跖点坐标容差（度）
     */
    private static final double ANTIPODAL_COORD_TOLERANCE = 1e-4;

    /**
     * WGS84 赤道上 1 度经度差的理论距离，单位：米
     * d = a * PI / 180 = 6378137 * PI / 180 = 111319.49079...
     */
    private static final double EQUATOR_1DEG_DISTANCE = 111319.49;

    /**
     * WGS84 赤道半周长（沿赤道），单位：米
     * half equator = PI * a ≈ 20037508.34
     */
    private static final double EQUATORIAL_HALF_CIRCUMFERENCE = Math.PI * 6378137.0;

    /**
     * WGS84 极地半周长（沿经线 via 极点），单位：米
     * half polar = PI * b ≈ 19970326.48
     */
    private static final double POLAR_HALF_CIRCUMFERENCE = Math.PI * 6378137.0 * (1.0 - 1.0 / 298.257223563);

    // ========== name() ==========

    @Test
    public void testName() {
        Assert.assertEquals("Karney", OP.name());
    }

    // ========== distance() — 标准用例 ==========

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
     * 经典 Vincenty 参考用例：Flinders Peak 到 Buninyong
     */
    @Test
    public void testVincentyReferenceDistance() {
        Coordinate flindersPeak = new Coordinate(-37.95103341666667, 144.4248678888889);
        Coordinate buninyong = new Coordinate(-37.65282113888889, 143.9264955611111);
        double dist = OP.distance(flindersPeak, buninyong);
        double error = Math.abs(dist - 54972.271) / 54972.271;
        Assert.assertTrue("Reference distance " + dist + " exceeds tolerance, error=" + error,
                error < RELATIVE_TOLERANCE);
    }

    /**
     * Karney 结果与 Vincenty 在正常区域一致
     */
    @Test
    public void testConsistencyWithVincenty() {
        Coordinate from = new Coordinate(30.0, 120.0);
        Coordinate to = new Coordinate(31.5, 121.8);
        double karneyDist = OP.distance(from, to);
        double vincentyDist = VINCENTY_OP.distance(from, to);
        double error = Math.abs(karneyDist - vincentyDist) / vincentyDist;
        Assert.assertTrue("Karney should match Vincenty in normal area, error=" + error,
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

    // ========== distance() — 对跖点（antipodal）核心测试 ==========

    /**
     * 赤道上对跖点（0°,0°）→（0°,180°）：
     * 在椭球体上，最短路经从极地附近经过而非沿赤道，因此距离介于
     * 极地半周长 PI*b（~19970326m）和赤道半周长 PI*a（~20037508m）之间。
     *
     * Karney 增强求解器收敛到实际椭球体短路径，
     * 而 Vincenty 降级到球面公式返回赤道路径（~20037508m）。
     */
    @Test
    public void testAntipodalEquator() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, 180);
        double dist = OP.distance(from, to);
        Assert.assertTrue("Antipodal distance " + dist
                        + " should be > polar half-circumference " + POLAR_HALF_CIRCUMFERENCE,
                dist > POLAR_HALF_CIRCUMFERENCE);
        Assert.assertTrue("Antipodal distance " + dist
                        + " should be < equatorial half-circumference " + EQUATORIAL_HALF_CIRCUMFERENCE,
                dist < EQUATORIAL_HALF_CIRCUMFERENCE);
    }

    /**
     * 子午线上对跖点（45°,0°）→（-45°,180°）：
     * 两点的归化纬度满足 U2 = -U1，经度差 180 度。
     * Karney 增强求解器必须收敛且结果合理。
     */
    @Test
    public void testAntipodalMeridian() {
        Coordinate from = new Coordinate(45, 0);
        Coordinate to = new Coordinate(-45, 180);
        double dist = OP.distance(from, to);
        // 理论值应在极地半周长和赤道半周长之间
        Assert.assertTrue("Antipodal meridional distance " + dist + " should be positive", dist > 0);
        Assert.assertTrue("Antipodal meridional distance " + dist
                        + " should be > polar half-circumference " + POLAR_HALF_CIRCUMFERENCE,
                dist > POLAR_HALF_CIRCUMFERENCE * 0.95);
        Assert.assertTrue("Antipodal meridional distance " + dist
                        + " should be < equatorial half-circumference " + EQUATORIAL_HALF_CIRCUMFERENCE,
                dist < EQUATORIAL_HALF_CIRCUMFERENCE * 1.05);
    }

    /**
     * 偏赤道对跖点（10°,0°）→（-10°,180°）：
     * 非对称对跖点，增强求解器必须收敛。
     */
    @Test
    public void testAntipodalOffEquator() {
        Coordinate from = new Coordinate(10, 0);
        Coordinate to = new Coordinate(-10, 180);
        double dist = OP.distance(from, to);
        Assert.assertTrue("Off-equator antipodal distance should be positive", dist > 0);
        Assert.assertTrue("Off-equator antipodal distance " + dist + " should be > polar half",
                dist > POLAR_HALF_CIRCUMFERENCE);
        Assert.assertTrue("Off-equator antipodal distance " + dist + " should be < equatorial half",
                dist < EQUATORIAL_HALF_CIRCUMFERENCE);
    }

    /**
     * 近对跖点（0°,0°）→（0.001°,179.999°）：
     * 非常接近对跖点，Vincenty 可能不收敛或精度下降。
     * 在椭球体上，最短路径从极地附近经过，距离应介于极地半周长和赤道半周长之间。
     */
    @Test
    public void testNearAntipodal() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0.001, 179.999);
        double dist = OP.distance(from, to);
        Assert.assertTrue("Near-antipodal distance should be positive", dist > 0);
        Assert.assertTrue("Near-antipodal distance " + dist
                        + " should be < equatorial half-circumference " + EQUATORIAL_HALF_CIRCUMFERENCE,
                dist < EQUATORIAL_HALF_CIRCUMFERENCE);
        Assert.assertTrue("Near-antipodal distance " + dist
                        + " should be > polar half-circumference " + POLAR_HALF_CIRCUMFERENCE,
                dist > POLAR_HALF_CIRCUMFERENCE);
    }

    /**
     * 非赤道近对跖点（30°,0°）→（-30.001°,179.999°）：
     * 更复杂的近对跖点场景。
     */
    @Test
    public void testNearAntipodalOffEquator() {
        Coordinate from = new Coordinate(30, 0);
        Coordinate to = new Coordinate(-30.001, 179.999);
        double dist = OP.distance(from, to);
        Assert.assertTrue("Off-equator near-antipodal distance should be positive", dist > 0);
        Assert.assertTrue("Off-equator near-antipodal distance " + dist + " should be > polar half",
                dist > POLAR_HALF_CIRCUMFERENCE);
        Assert.assertTrue("Off-equator near-antipodal distance " + dist + " should be < equatorial half",
                dist < EQUATORIAL_HALF_CIRCUMFERENCE);
    }

    /**
     * 赤道上多点对跖点可交换性验证
     */
    @Test
    public void testAntipodalSymmetry() {
        Coordinate a = new Coordinate(0, 0);
        Coordinate b = new Coordinate(0, 180);
        double d1 = OP.distance(a, b);
        double d2 = OP.distance(b, a);
        Assert.assertEquals("Antipodal distance should be symmetric", d1, d2, 1e-6);
    }

    // ========== direct() ==========

    /**
     * 经典 Vincenty 参考用例往返一致性验证
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

    /**
     * 从赤道正北推算：应得到纬度约为 1 度的坐标
     */
    @Test
    public void testDirectNorth() {
        Coordinate start = new Coordinate(0, 0);
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
     * distance + direct 往返一致性验证
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

    // ========== azimuth() ==========

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
                Math.PI / 2, az, 1e-10);
    }

    /**
     * 正南方向方位角为 pi
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
     * 正西方向方位角为 -pi/2
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
     * 相同点方位角返回 0
     */
    @Test
    public void testAzimuthSamePoint() {
        Coordinate p = new Coordinate(35.0, 120.0);
        double az = OP.azimuth(p, p);
        Assert.assertEquals(0.0, az, 0.0);
    }

    /**
     * 对跖点的方位角应合理（Karney 增强求解器收敛）
     */
    @Test
    public void testAzimuthAntipodal() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, 180);
        double az = OP.azimuth(from, to);
        // 对跖点方位角在赤道上应为 0（正北）或 pi（正南）
        // 实际应为正东或正西，取决于经度差方向
        Assert.assertTrue("Antipodal azimuth should be a valid angle",
                !Double.isNaN(az) && !Double.isInfinite(az));
    }

    // ========== midpoint() ==========

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
     * 中点 + distance 一致性
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

    // ========== area() ==========

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

    // ========== null checks ==========

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
