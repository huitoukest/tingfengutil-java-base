package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;

/**
 * 地理算子抽象基类
 *
 * 实现 EarthOp 接口，提供：
 * - 参数校验保护方法（requireNonNull、requirePolygon）
 * - area() 的默认实现（基于球面梯形公式，通过 distance() 估算地球半径）
 * - 其余五个方法保持 abstract，由子类实现具体地球模型算法
 */
public abstract class AbstractEarthOp implements EarthOp {

    /**
     * 起点经度为 0、纬度为 0 的参考坐标，用于估算地球半径
     */
    private static final Coordinate REF_EQUATOR_0 = new Coordinate(0, 0);

    /**
     * 经度为 1 度、纬度为 0 的参考坐标
     */
    private static final Coordinate REF_EQUATOR_1 = new Coordinate(0, 1);

    /**
     * 校验坐标参数不为 null
     *
     * @param p 待校验的坐标
     * @throws IllegalArgumentException 如果 p 为 null
     */
    protected static void requireNonNull(Coordinate p) {
        if (p == null) {
            throw new IllegalArgumentException("Coordinate must not be null");
        }
    }

    /**
     * 校验多边形顶点数组有效
     *
     * @param points 多边形顶点数组
     * @throws IllegalArgumentException 如果数组为 null 或顶点数少于 3
     */
    protected static void requirePolygon(double[][] points) {
        if (points == null || points.length < 3) {
            throw new IllegalArgumentException(
                    "Polygon must have at least 3 vertices, actual: " + (points == null ? 0 : points.length));
        }
    }

    @Override
    public double area(double[][] points) {
        requirePolygon(points);

        // 通过 distance() 在赤道上估算地球半径
        // 计算赤道上经度差 1 度对应的弧长，反推半径 R = d / (π/180)
        double meterPerDegree = distance(REF_EQUATOR_0, REF_EQUATOR_1);
        double radius = meterPerDegree / Math.toRadians(1);

        // 球面多边形面积（梯形公式）
        // A = 0.5 * R² * |sum((λ₂ - λ₁) * (2 + sin(φ₁) + sin(φ₂)))|
        double sum = 0;
        int n = points.length;
        for (int i = 0; i < n; i++) {
            double lat1 = Math.toRadians(points[i][0]);
            double lng1 = Math.toRadians(points[i][1]);
            double lat2 = Math.toRadians(points[(i + 1) % n][0]);
            double lng2 = Math.toRadians(points[(i + 1) % n][1]);

            sum += (lng2 - lng1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }
        return Math.abs(sum * radius * radius / 2.0);
    }
}
