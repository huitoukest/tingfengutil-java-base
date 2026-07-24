package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;
import org.junit.Assert;
import org.junit.Test;

/**
 * AbstractEarthOp 参数校验测试
 *
 * 验证 base 类的 requireNonNull 和 requirePolygon 保护方法按预期工作。
 */
public class AbstractEarthOpTest {

    /**
     * 创建一个最小实现的 AbstractEarthOp 子类，用于触发校验方法
     */
    private static final AbstractEarthOp STUB_OP = new AbstractEarthOp() {
        @Override
        public String name() {
            return "Stub";
        }

        @Override
        public double distance(Coordinate from, Coordinate to) {
            return 0;
        }

        @Override
        public Coordinate direct(Coordinate point, double azimuth, double distance) {
            return point;
        }

        @Override
        public double azimuth(Coordinate from, Coordinate to) {
            return 0;
        }

        @Override
        public Coordinate midpoint(Coordinate from, Coordinate to) {
            return from;
        }
    };

    /**
     * requireNonNull：传入 null 应抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRequireNonNullWithNull() {
        AbstractEarthOp.requireNonNull(null);
    }

    /**
     * requireNonNull：传入有效坐标不应抛出异常
     */
    @Test
    public void testRequireNonNullWithValid() {
        AbstractEarthOp.requireNonNull(new Coordinate(30, 120));
        // 无异常即通过
    }

    /**
     * requirePolygon：传入 null 应抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRequirePolygonWithNull() {
        AbstractEarthOp.requirePolygon(null);
    }

    /**
     * requirePolygon：顶点数 < 3 应抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRequirePolygonWithLessThan3() {
        AbstractEarthOp.requirePolygon(new double[][]{{30, 120}, {31, 121}});
    }

    /**
     * requirePolygon：正好 3 个顶点不应抛出异常
     */
    @Test
    public void testRequirePolygonWith3Vertices() {
        AbstractEarthOp.requirePolygon(new double[][]{
                {30, 120}, {31, 121}, {32, 122}
        });
        // 无异常即通过
    }

    /**
     * requirePolygon：多个顶点不应抛出异常
     */
    @Test
    public void testRequirePolygonWithManyVertices() {
        AbstractEarthOp.requirePolygon(new double[][]{
                {30, 120}, {31, 121}, {32, 122}, {33, 123}, {34, 124}
        });
        // 无异常即通过
    }

    /**
     * area()：传入 null 应抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAreaWithNull() {
        STUB_OP.area(null);
    }

    /**
     * area()：顶点数 < 3 应抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAreaWithLessThan3() {
        STUB_OP.area(new double[][]{{30, 120}, {31, 121}});
    }

}
