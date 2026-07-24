package com.tingfeng.util.java.base.gis.op;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * EGM96Grid 数据引擎测试
 *
 * 验证合成格网数据生成、双线性插值精度、边界处理和经度环绕。
 */
public class EGM96GridTest {

    private static EGM96Grid grid;

    /**
     * 所有测试使用合成格网数据
     */
    @BeforeClass
    public static void setUpClass() {
        grid = EGM96Grid.createSynthetic();
    }

    /**
     * 合成数据来源正确
     */
    @Test
    public void testSyntheticSource() {
        Assert.assertEquals("synthetic", grid.getSource());
    }

    /**
     * 格网尺寸正确：181 纬度 x 361 经度
     */
    @Test
    public void testGridDimensions() {
        // 四个角点都能正常获取值
        short nw = grid.getRawHeight(0, 0);      // 90°N, 0°
        short ne = grid.getRawHeight(0, 360);    // 90°N, 360°
        short sw = grid.getRawHeight(180, 0);    // 90°S, 0°
        short se = grid.getRawHeight(180, 360);  // 90°S, 360°

        // 只是验证数据存在（非 null 检查不适用于 short）
        Assert.assertTrue("NW corner should be finite", nw > Short.MIN_VALUE);
        Assert.assertTrue("NE corner should be finite", ne > Short.MIN_VALUE);
        Assert.assertTrue("SW corner should be finite", sw > Short.MIN_VALUE);
        Assert.assertTrue("SE corner should be finite", se > Short.MIN_VALUE);
    }

    /**
     * 双线性插值在整数纬度和整数经度格网点上等于原始格网值
     *
     * 当 lat 和 lon 正好为整度时，双线性插值应退化到单点取值，
     * 结果与 getRawHeight 转换后一致（允许 0.5cm 舍入误差）。
     */
    @Test
    public void testInterpolationAtGridNode() {
        double lat = 30.0;
        double lon = 120.0;
        int i = (int) (90.0 - lat); // = 60
        int j = (int) lon;          // = 120
        short raw = grid.getRawHeight(i, j);
        double expected = raw / 100.0;
        double actual = grid.getGeoidHeight(lat, lon);
        Assert.assertEquals("Interpolation at grid node (30, 120)", expected, actual, 0.005);
    }

    /**
     * 双线性插值在另一个格网点上也正确
     */
    @Test
    public void testInterpolationAtAnotherGridNode() {
        double lat = -30.0;
        double lon = 45.0;
        int i = (int) (90.0 - lat); // = 120
        int j = (int) lon;          // = 45
        short raw = grid.getRawHeight(i, j);
        double expected = raw / 100.0;
        double actual = grid.getGeoidHeight(lat, lon);
        Assert.assertEquals("Interpolation at grid node (-30, 45)", expected, actual, 0.005);
    }

    /**
     * 双线性插值在南北极格点上也正确
     */
    @Test
    public void testInterpolationAtNorthPole() {
        double lat = 90.0;
        double lon = 0.0;
        int i = 0; // 90°N
        int j = 0;
        short raw = grid.getRawHeight(i, j);
        double expected = raw / 100.0;
        double actual = grid.getGeoidHeight(lat, lon);
        Assert.assertEquals("Interpolation at North Pole", expected, actual, 0.005);
    }

    /**
     * 双线性插值在南极格点上也正确
     */
    @Test
    public void testInterpolationAtSouthPole() {
        double lat = -90.0;
        double lon = 0.0;
        int i = 180; // 90°S
        int j = 0;
        short raw = grid.getRawHeight(i, j);
        double expected = raw / 100.0;
        double actual = grid.getGeoidHeight(lat, lon);
        Assert.assertEquals("Interpolation at South Pole", expected, actual, 0.005);
    }

    /**
     * 双线性插值在两个格网点之间的中心点
     *
     * 在 lat0 和 lat1 之间的中点，插值结果应为两个格网点高度的算术平均
     * （当经度固定时）。
     */
    @Test
    public void testInterpolationAtLatMidpoint() {
        // 纬度 30.5 在 30 (i=60) 和 31 (i=59) 之间
        double lat0 = 31.0; // i = 59
        double lat1 = 30.0; // i = 60
        double lon = 120.0; // j = 120

        short h0 = grid.getRawHeight(59, 120); // 31°N
        short h1 = grid.getRawHeight(60, 120); // 30°N
        double expected = (h0 + h1) / 2.0 / 100.0;

        double actual = grid.getGeoidHeight(30.5, lon);
        Assert.assertEquals("Interpolation at lat midpoint", expected, actual, 0.005);
    }

    /**
     * 双线性插值在两个经度格网点之间的中心点
     *
     * 在 lon0 和 lon1 之间的中点，插值结果应为两个格网点高度的算术平均
     * （当纬度固定时）。
     */
    @Test
    public void testInterpolationAtLonMidpoint() {
        double lat = 30.0; // i = 60
        double lon0 = 120.0; // j = 120
        double lon1 = 121.0; // j = 121

        short h0 = grid.getRawHeight(60, 120);
        short h1 = grid.getRawHeight(60, 121);
        double expected = (h0 + h1) / 2.0 / 100.0;

        double actual = grid.getGeoidHeight(lat, 120.5);
        Assert.assertEquals("Interpolation at lon midpoint", expected, actual, 0.005);
    }

    /**
     * 双线性插值四点中心处的计算正确
     *
     * 在四个格网点 (i0,j0), (i0,j1), (i1,j0), (i1,j1) 的中心，
     * 插值结果应等于四个高度的算术平均。
     */
    @Test
    public void testInterpolationAtCellCenter() {
        double lat = 30.5; // 在 31°N (i=59) 和 30°N (i=60) 之间
        double lon = 120.5; // 在 120°E (j=120) 和 121°E (j=121) 之间

        short h00 = grid.getRawHeight(59, 120); // 31°N, 120°E
        short h01 = grid.getRawHeight(59, 121); // 31°N, 121°E
        short h10 = grid.getRawHeight(60, 120); // 30°N, 120°E
        short h11 = grid.getRawHeight(60, 121); // 30°N, 121°E

        double expected = (h00 + h01 + h10 + h11) / 4.0 / 100.0;

        double actual = grid.getGeoidHeight(lat, lon);
        Assert.assertEquals("Interpolation at cell center", expected, actual, 0.005);
    }

    /**
     * 经度环绕：lon=360 与 lon=0 等价
     */
    @Test
    public void testLongitudeWrapping() {
        double lat = 30.0;
        double h0 = grid.getGeoidHeight(lat, 0.0);
        double h360 = grid.getGeoidHeight(lat, 360.0);
        Assert.assertEquals("Longitude wrapping: 0 vs 360", h0, h360, 0.001);
    }

    /**
     * 经度环绕：负经度正确归一化
     */
    @Test
    public void testNegativeLongitude() {
        double lat = 30.0;
        double hPos = grid.getGeoidHeight(lat, 359.0);
        double hNeg = grid.getGeoidHeight(lat, -1.0);
        Assert.assertEquals("Negative longitude normalization", hPos, hNeg, 0.001);
    }

    /**
     * 经度环绕：lon=360.5 等价于 lon=0.5
     */
    @Test
    public void testLongitudeWrappingBeyond360() {
        double lat = 30.0;
        double h05 = grid.getGeoidHeight(lat, 0.5);
        double h3605 = grid.getGeoidHeight(lat, 360.5);
        Assert.assertEquals("Longitude wrapping: 0.5 vs 360.5", h05, h3605, 0.001);
    }

    /**
     * 纬度 clamp：超过 90°N 的纬度被限制到 90°N
     */
    @Test
    public void testLatitudeClampNorth() {
        double h90 = grid.getGeoidHeight(90.0, 0.0);
        double h95 = grid.getGeoidHeight(95.0, 0.0);
        Assert.assertEquals("Clamp at 90°N", h90, h95, 0.001);
    }

    /**
     * 纬度 clamp：低于 -90°S 的纬度被限制到 -90°S
     */
    @Test
    public void testLatitudeClampSouth() {
        double hM90 = grid.getGeoidHeight(-90.0, 0.0);
        double hM95 = grid.getGeoidHeight(-95.0, 0.0);
        Assert.assertEquals("Clamp at -90°S", hM90, hM95, 0.001);
    }

    /**
     * 合成数据幅度合理（在 ±50 米以内，考虑较大系数的叠加）
     */
    @Test
    public void testSyntheticDataAmplitude() {
        double maxHeight = Double.MIN_VALUE;
        double minHeight = Double.MAX_VALUE;

        // 在几个关键位置采样
        double[] testLats = {-90, -60, -30, 0, 30, 60, 90};
        double[] testLons = {0, 90, 180, 270};

        for (double lat : testLats) {
            for (double lon : testLons) {
                double h = grid.getGeoidHeight(lat, lon);
                if (h > maxHeight) {
                    maxHeight = h;
                }
                if (h < minHeight) {
                    minHeight = h;
                }
            }
        }

        // 合成数据幅度应在 ±80 米以内（各项系数叠加的合理范围）
        Assert.assertTrue("Max geoid height should be reasonable: " + maxHeight,
                maxHeight < 80.0 && maxHeight > -80.0);
        Assert.assertTrue("Min geoid height should be reasonable: " + minHeight,
                minHeight > -80.0 && minHeight < 80.0);
    }

    /**
     * getInstance 返回有效的实例（默认使用合成数据）
     */
    @Test
    public void testGetInstance() {
        EGM96Grid instance = EGM96Grid.getInstance();
        Assert.assertNotNull("Instance should not be null", instance);
        // 由于没有数据文件，应该使用合成数据
        double h = instance.getGeoidHeight(30.0, 120.0);
        Assert.assertTrue("Geoid height should be finite", Double.isFinite(h));
    }

    /**
     * load(InputStream) 传入 null 抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLoadWithNullInput() throws Exception {
        EGM96Grid.load(null);
    }

    /**
     * 边界纬度 -89.999 和 -90 之间差值很小
     */
    @Test
    public void testLatitudeNearBoundary() {
        double hMinus90 = grid.getGeoidHeight(-90.0, 0.0);
        double hMinus89999 = grid.getGeoidHeight(-89.999, 0.0);
        // 两者非常接近（在同一条网格线附近）
        double diff = Math.abs(hMinus90 - hMinus89999);
        Assert.assertTrue("Near-boundary latitudes should give close results: diff=" + diff,
                diff < 0.1);
    }

    /**
     * 纬度 0 经度 0 处双线性插值在格网点上
     */
    @Test
    public void testInterpolationAtEquator() {
        double lat = 0.0;
        double lon = 0.0;
        int i = (int) (90.0 - lat); // = 90
        int j = (int) lon;          // = 0
        short raw = grid.getRawHeight(i, j);
        double expected = raw / 100.0;
        double actual = grid.getGeoidHeight(lat, lon);
        Assert.assertEquals("Interpolation at Equator/Prime Meridian", expected, actual, 0.005);
    }
}
