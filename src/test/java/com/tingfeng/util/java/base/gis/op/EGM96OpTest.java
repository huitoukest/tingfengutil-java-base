package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;
import org.junit.Assert;
import org.junit.Test;

/**
 * EGM96Op 大地水准面修正算子测试
 *
 * 验证装饰器模式实现：
 * - name() 返回 "EGM96"
 * - 所有 EarthOp 方法委托给 delegate（与 VincentyOp 结果一致）
 * - getGeoidHeight() 返回合理值
 * - 参数校验
 */
public class EGM96OpTest {

    /**
     * 被测实例
     */
    private static final EGM96Op OP = EGM96Op.getInstance();

    /**
     * 用于对比的 VincentyOp 实例
     */
    private static final VincentyOp VINCENTY = VincentyOp.getInstance();

    /**
     * 坐标相等判断容差（度）
     */
    private static final double COORDINATE_TOLERANCE = 1e-10;

    // ---- name() ----

    /**
     * name() 应返回 "EGM96"
     */
    @Test
    public void testName() {
        Assert.assertEquals("EGM96", OP.name());
    }

    // ---- distance() 委托验证 ----

    /**
     * distance() 结果应与 VincentyOp 一致
     */
    @Test
    public void testDistanceDelegatesToVincenty() {
        Coordinate from = new Coordinate(35.6895, 139.6917);
        Coordinate to = new Coordinate(34.0522, -118.2437);
        double expected = VINCENTY.distance(from, to);
        double actual = OP.distance(from, to);
        Assert.assertEquals("EGM96Op distance should match VincentyOp", expected, actual, 0.0);
    }

    /**
     * distance() 委托：相同点返回 0
     */
    @Test
    public void testDistanceSamePoint() {
        Coordinate p = new Coordinate(30.0, 120.0);
        double dist = OP.distance(p, p);
        Assert.assertEquals(0.0, dist, 0.0);
    }

    /**
     * distance() 委托：赤道上 1 度经度差
     */
    @Test
    public void testDistanceEquatorialDegree() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(0, 1);
        double expected = VINCENTY.distance(from, to);
        double actual = OP.distance(from, to);
        Assert.assertEquals("EGM96Op equatorial distance should match VincentyOp",
                expected, actual, 0.0);
    }

    // ---- direct() 委托验证 ----

    /**
     * direct() 结果应与 VincentyOp 一致
     */
    @Test
    public void testDirectDelegatesToVincenty() {
        Coordinate start = new Coordinate(30.0, 120.0);
        double azimuth = 0.0; // 正北
        double distance = 100000.0; // 100km
        Coordinate expected = VINCENTY.direct(start, azimuth, distance);
        Coordinate actual = OP.direct(start, azimuth, distance);
        Assert.assertEquals("EGM96Op direct latitude should match VincentyOp",
                expected.getLatitude(), actual.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("EGM96Op direct longitude should match VincentyOp",
                expected.getLongitude(), actual.getLongitude(), COORDINATE_TOLERANCE);
    }

    /**
     * direct() 委托：赤道正东推算
     */
    @Test
    public void testDirectEast() {
        Coordinate start = new Coordinate(0, 0);
        Coordinate result = OP.direct(start, Math.PI / 2, 111319.49);
        Assert.assertEquals("Latitude should remain ~0",
                0.0, result.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertTrue("Longitude should be ~1 degree",
                result.getLongitude() > 0.9 && result.getLongitude() < 1.1);
    }

    // ---- azimuth() 委托验证 ----

    /**
     * azimuth() 结果应与 VincentyOp 一致
     */
    @Test
    public void testAzimuthDelegatesToVincenty() {
        Coordinate from = new Coordinate(30.0, 120.0);
        Coordinate to = new Coordinate(31.0, 121.0);
        double expected = VINCENTY.azimuth(from, to);
        double actual = OP.azimuth(from, to);
        Assert.assertEquals("EGM96Op azimuth should match VincentyOp", expected, actual, 0.0);
    }

    /**
     * azimuth() 委托：正北
     */
    @Test
    public void testAzimuthNorth() {
        Coordinate from = new Coordinate(0, 0);
        Coordinate to = new Coordinate(1, 0);
        double az = OP.azimuth(from, to);
        Assert.assertEquals("Azimuth north should be 0", 0.0, az, 1e-10);
    }

    // ---- midpoint() 委托验证 ----

    /**
     * midpoint() 结果应与 VincentyOp 一致
     */
    @Test
    public void testMidpointDelegatesToVincenty() {
        Coordinate from = new Coordinate(30.0, 120.0);
        Coordinate to = new Coordinate(31.0, 121.0);
        Coordinate expected = VINCENTY.midpoint(from, to);
        Coordinate actual = OP.midpoint(from, to);
        Assert.assertEquals("EGM96Op midpoint latitude should match VincentyOp",
                expected.getLatitude(), actual.getLatitude(), COORDINATE_TOLERANCE);
        Assert.assertEquals("EGM96Op midpoint longitude should match VincentyOp",
                expected.getLongitude(), actual.getLongitude(), COORDINATE_TOLERANCE);
    }

    // ---- area() 委托验证 ----

    /**
     * area() 结果应与 VincentyOp 一致（继承 AbstractEarthOp，内部调用 distance()）
     */
    @Test
    public void testAreaDelegatesToVincenty() {
        double[][] triangle = {
                {0, 0},
                {0, 1},
                {1, 0}
        };
        double expected = VINCENTY.area(triangle);
        double actual = OP.area(triangle);
        Assert.assertEquals("EGM96Op area should match VincentyOp", expected, actual, 1e-6);
    }

    // ---- getGeoidHeight() ----

    /**
     * getGeoidHeight() 返回有限值
     */
    @Test
    public void testGetGeoidHeightReturnsFinite() {
        Coordinate point = new Coordinate(30.0, 120.0);
        double height = OP.getGeoidHeight(point);
        Assert.assertTrue("Geoid height should be finite", Double.isFinite(height));
    }

    /**
     * getGeoidHeight() 在不同位置应返回不同值（非平凡）
     */
    @Test
    public void testGetGeoidHeightVariesByLocation() {
        double h1 = OP.getGeoidHeight(new Coordinate(30.0, 120.0));
        double h2 = OP.getGeoidHeight(new Coordinate(-30.0, 120.0));
        Assert.assertNotEquals("Geoid height should differ at different latitudes",
                h1, h2, 1e-6);
    }

    /**
     * getGeoidHeight() 在赤道和经度 0 处返回合理值
     */
    @Test
    public void testGetGeoidHeightAtEquator() {
        double height = OP.getGeoidHeight(new Coordinate(0, 0));
        Assert.assertTrue("Geoid height should be in reasonable range (-80 to 80 m)",
                height > -80.0 && height < 80.0);
    }

    /**
     * getGeoidHeight() 在北极处返回有限值
     */
    @Test
    public void testGetGeoidHeightAtNorthPole() {
        double height = OP.getGeoidHeight(new Coordinate(90.0, 0));
        Assert.assertTrue("Geoid height at North Pole should be finite",
                Double.isFinite(height));
    }

    /**
     * getGeoidHeight() 在南极处返回有限值
     */
    @Test
    public void testGetGeoidHeightAtSouthPole() {
        double height = OP.getGeoidHeight(new Coordinate(-90.0, 0));
        Assert.assertTrue("Geoid height at South Pole should be finite",
                Double.isFinite(height));
    }

    // ---- 参数校验 ----

    /**
     * getGeoidHeight() 传入 null 应抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetGeoidHeightNull() {
        OP.getGeoidHeight(null);
    }

    /**
     * 自定义 delegate 构造器：传入 null 应抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullDelegate() {
        new EGM96Op(null);
    }

    /**
     * 自定义 delegate 构造器：传入 SphericalOp 应正常工作
     *
     * name() 始终返回 "EGM96"（装饰器自身标识），但 distance() 应使用
     * delegate 的算法（SphericalOp 的球面近似值应不同于 VincentyOp 的椭球体值）
     */
    @Test
    public void testCustomDelegate() {
        EGM96Op custom = new EGM96Op(SphericalOp.getInstance());
        Assert.assertEquals("EGM96", custom.name());
        // verification: should delegate to SphericalOp, not throw
        double dist = custom.distance(
                new Coordinate(0, 0), new Coordinate(0, 1));
        Assert.assertTrue("Distance should be positive via spherical delegate", dist > 0);
        // 使用 SphericalOp 的距离应与默认的 VincentyOp 距离不同
        double vincentyDist = VINCENTY.distance(
                new Coordinate(0, 0), new Coordinate(0, 1));
        Assert.assertNotEquals("Spherical delegate distance should differ from Vincenty",
                vincentyDist, dist, 1.0);
    }

    // ---- Ops.EGM96 常量 ----

    /**
     * Ops.EGM96 应为 EGM96Op 实例
     */
    @Test
    public void testOpsEGM96Constant() {
        Assert.assertTrue("Ops.EGM96 should be an EGM96Op instance",
                Ops.EGM96 instanceof EGM96Op);
        Assert.assertEquals("EGM96", Ops.EGM96.name());
    }
}
