package com.tingfeng.util.java.base.gis.model;

/**
 * 简化球体地球模型
 *
 * 将地球简化为完美球体，半径固定为 6371000 米。
 *
 * 精度说明：
 * - 忽略地球扁率，将地球视为完美球体
 * - 精度约在 0.3% 以内（与 WGS84 椭球体模型相比）
 * - 适用于快速估算、日常距离计算、性能敏感场景
 *
 * 与 WGS84 模型对比：
 * - WGS84：考虑椭球体形状，不同纬度半径不同，精度最高
 * - Spherical：固定半径，计算更快，精度略低
 */
public final class SphericalEarthModel implements EarthModel {

    /**
     * 固定球体半径，单位：米
     */
    private static final double SPHERE_RADIUS = 6371000.0;

    /**
     * 圆周率
     */
    private static final double PI = Math.PI;

    /**
     * 单例实例
     */
    private static final SphericalEarthModel INSTANCE = new SphericalEarthModel();

    /**
     * 获取单例实例
     *
     * @return SphericalEarthModel 单例
     */
    public static SphericalEarthModel getInstance() {
        return INSTANCE;
    }

    /**
     * 私有构造器，禁止外部实例化
     */
    private SphericalEarthModel() {
    }

    @Override
    public double getRadius(double latitude) {
        // 球体模型中，半径不随纬度变化
        return SPHERE_RADIUS;
    }

    @Override
    public double getDistance(Coordinate pointA, Coordinate pointB) {
        double latAR = Math.toRadians(pointA.getLatitude());
        double latBR = Math.toRadians(pointB.getLatitude());
        double lngAR = Math.toRadians(pointA.getLongitude());
        double lngBR = Math.toRadians(pointB.getLongitude());

        double dLng = Math.abs(lngAR - lngBR);

        // 处理经度差超过 180 度的情况，使用最短路径
        if (dLng > PI) {
            dLng = 2.0 * PI - dLng;
        }

        // 球面距离公式（弧度制）
        double cosValue = Math.sin(latAR) * Math.sin(latBR)
                + Math.cos(latAR) * Math.cos(latBR) * Math.cos(dLng);

        // 确保 cosValue 在 [-1, 1] 范围内，避免 Math.acos 返回 NaN
        cosValue = Math.max(-1.0, Math.min(1.0, cosValue));

        return SPHERE_RADIUS * Math.acos(cosValue);
    }

    @Override
    public String getName() {
        return "Spherical";
    }

    @Override
    public double getAverageRadius() {
        return SPHERE_RADIUS;
    }
}
