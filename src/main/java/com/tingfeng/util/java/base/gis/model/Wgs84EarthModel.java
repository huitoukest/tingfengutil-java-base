package com.tingfeng.util.java.base.gis.model;

/**
 * WGS84 地球椭球体模型
 *
 * WGS84（World Geodetic System 1984）是国际标准坐标系，GPS 设备使用的原始坐标系统。
 *
 * 模型参数：
 * - 赤道半径（长半轴）a = 6378137 米
 * - 极点半径（短半轴）b = a × (1 - f) = 6356752.314245 米
 * - 扁率 f = 1/298.257223563
 *
 * 精度说明：
 * - 适用于 Google 地图（国外）、OSM、ArcGIS 等国际标准地图
 * - 计算结果精确到米级别
 * - 比球体模型计算量稍大
 */
public final class Wgs84EarthModel implements EarthModel {

    /**
     * 赤道半径（长半轴），单位：米
     */
    private static final double EQUATOR_RADIUS = 6378137.0;

    /**
     * 扁率
     */
    private static final double FLATTENING = 1.0 / 298.257223563;

    /**
     * 圆周率
     */
    private static final double PI = Math.PI;

    /**
     * 单例实例
     */
    private static final Wgs84EarthModel INSTANCE = new Wgs84EarthModel();

    /**
     * 获取单例实例
     *
     * @return Wgs84EarthModel 单例
     */
    public static Wgs84EarthModel getInstance() {
        return INSTANCE;
    }

    /**
     * 私有构造器，禁止外部实例化
     */
    private Wgs84EarthModel() {
    }

    @Override
    public double getRadius(double latitude) {
        double radian = Math.toRadians(latitude);
        double cosR = Math.cos(radian);
        double sinR = Math.sin(radian);
        double a = EQUATOR_RADIUS;
        double b = EQUATOR_RADIUS * (1.0 - FLATTENING);
        return Math.sqrt(a * a * cosR * cosR + b * b * sinR * sinR);
    }

    @Override
    public double getDistance(Coordinate pointA, Coordinate pointB) {
        double latAR = Math.toRadians(pointA.getLatitude());
        double latBR = Math.toRadians(pointB.getLatitude());
        double lngAR = Math.toRadians(pointA.getLongitude());
        double lngBR = Math.toRadians(pointB.getLongitude());

        double r = getRadius((pointA.getLatitude() + pointB.getLatitude()) / 2.0);
        double dLng = Math.abs(lngAR - lngBR);

        // 处理经度差超过 180 度的情况，使用最短路径
        if (dLng > PI) {
            dLng = 2.0 * PI - dLng;
        }

        double cosValue = Math.sin(latAR) * Math.sin(latBR)
                + Math.cos(latAR) * Math.cos(latBR) * Math.cos(dLng);

        // 确保 cosValue 在 [-1, 1] 范围内，避免 Math.acos 返回 NaN
        cosValue = Math.max(-1.0, Math.min(1.0, cosValue));

        return r * Math.acos(cosValue);
    }

    @Override
    public String getName() {
        return "WGS84";
    }

    @Override
    public double getAverageRadius() {
        // WGS84 平均半径约为 6371008.8 米
        return (2.0 * EQUATOR_RADIUS + EQUATOR_RADIUS * (1.0 - FLATTENING)) / 3.0;
    }
}
