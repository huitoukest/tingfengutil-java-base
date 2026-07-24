package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;

/**
 * WGS84 椭球基本公式算子
 *
 * 基于 WGS84 椭球体参数，使用平均半径 R=(2a+b)/3 进行球面近似计算。
 * 采用 Haversine 公式计算距离，使用标准球面公式进行正算、方位角和
 * 中点计算。与 VincentyOp 不同，本算子使用非迭代的封闭公式，计算
 * 速度快但精度约为米级。
 *
 * WGS84 椭球参数：
 * - 赤道半径 a = 6378137.0 米
 * - 扁率 f = 1/298.257223563
 * - 极点半径 b = a * (1-f) = 6356752.314245 米
 * - 平均半径 R = (2a + b) / 3 ≈ 6371008.77 米
 *
 * 适用场景：
 * - 快速距离估算（毫米级耗时）
 * - 非高精度导航（米级误差可接受）
 * - 性能敏感的大批量计算
 *
 * 注：由于 Java 枚举的 name() 方法为 final，与 EarthOp.name() 接口方法
 * 冲突，因此本类采用 final class + 私有构造器的单例模式。
 */
public final class WGS84Op implements EarthOp {

    /**
     * 单例实例
     */
    private static final WGS84Op INSTANCE = new WGS84Op();

    /**
     * WGS84 赤道半径（长半轴），单位：米
     */
    private static final double EQUATOR_RADIUS = 6378137.0;

    /**
     * WGS84 扁率
     */
    private static final double FLATTENING = 1.0 / 298.257223563;

    /**
     * WGS84 极点半径（短半轴），单位：米
     */
    private static final double POLAR_RADIUS = EQUATOR_RADIUS * (1.0 - FLATTENING);

    /**
     * WGS84 平均半径，使用等积球面近似 R = (2a + b) / 3，单位：米
     */
    private static final double MEAN_RADIUS = (2.0 * EQUATOR_RADIUS + POLAR_RADIUS) / 3.0;

    /**
     * 获取单例实例
     *
     * @return WGS84Op 单例
     */
    public static WGS84Op getInstance() {
        return INSTANCE;
    }

    /**
     * 私有构造器，禁止外部实例化
     */
    private WGS84Op() {
    }

    @Override
    public String name() {
        return "WGS84";
    }

    @Override
    public double distance(Coordinate from, Coordinate to) {
        AbstractEarthOp.requireNonNull(from);
        AbstractEarthOp.requireNonNull(to);

        // Haversine 公式计算球面距离
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double sinHalfLat = Math.sin(dLat / 2.0);
        double sinHalfLon = Math.sin(dLon / 2.0);

        double a = sinHalfLat * sinHalfLat
                + Math.cos(lat1) * Math.cos(lat2) * sinHalfLon * sinHalfLon;
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return MEAN_RADIUS * c;
    }

    @Override
    public Coordinate direct(Coordinate point, double azimuth, double distance) {
        AbstractEarthOp.requireNonNull(point);

        double lat1 = Math.toRadians(point.getLatitude());
        double lon1 = Math.toRadians(point.getLongitude());
        double angDist = distance / MEAN_RADIUS;
        double cosAngDist = Math.cos(angDist);
        double sinAngDist = Math.sin(angDist);
        double cosAzimuth = Math.cos(azimuth);
        double sinAzimuth = Math.sin(azimuth);
        double cosLat1 = Math.cos(lat1);
        double sinLat1 = Math.sin(lat1);

        double lat2Rad = Math.asin(sinLat1 * cosAngDist
                + cosLat1 * sinAngDist * cosAzimuth);

        double lon2Rad = lon1 + Math.atan2(
                sinAzimuth * sinAngDist * cosLat1,
                cosAngDist - sinLat1 * Math.sin(lat2Rad));

        double lat2Deg = Math.toDegrees(lat2Rad);
        double lon2Deg = Math.toDegrees(lon2Rad);

        // 经度归一化到 [-180, 180)
        if (lon2Deg > 180.0) {
            lon2Deg -= 360.0;
        } else if (lon2Deg < -180.0) {
            lon2Deg += 360.0;
        }

        return new Coordinate(lat2Deg, lon2Deg);
    }

    @Override
    public double azimuth(Coordinate from, Coordinate to) {
        AbstractEarthOp.requireNonNull(from);
        AbstractEarthOp.requireNonNull(to);

        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLon = lon2 - lon1;

        return Math.atan2(
                Math.sin(dLon) * Math.cos(lat2),
                Math.cos(lat1) * Math.sin(lat2)
                        - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon));
    }

    @Override
    public Coordinate midpoint(Coordinate from, Coordinate to) {
        AbstractEarthOp.requireNonNull(from);
        AbstractEarthOp.requireNonNull(to);

        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLon = lon2 - lon1;

        // 球面中点公式
        double bx = Math.cos(lat2) * Math.cos(dLon);
        double by = Math.cos(lat2) * Math.sin(dLon);

        double latM = Math.atan2(
                Math.sin(lat1) + Math.sin(lat2),
                Math.sqrt((Math.cos(lat1) + bx) * (Math.cos(lat1) + bx) + by * by));
        double lonM = lon1 + Math.atan2(by, Math.cos(lat1) + bx);

        double latDeg = Math.toDegrees(latM);
        double lonDeg = Math.toDegrees(lonM);

        // 经度归一化到 [-180, 180)
        if (lonDeg > 180.0) {
            lonDeg -= 360.0;
        } else if (lonDeg < -180.0) {
            lonDeg += 360.0;
        }

        return new Coordinate(latDeg, lonDeg);
    }

    @Override
    public double area(double[][] points) {
        AbstractEarthOp.requirePolygon(points);

        // 通过赤道上经度 1 度对应的弧长反推有效半径
        Coordinate ref0 = new Coordinate(0, 0);
        Coordinate ref1 = new Coordinate(0, 1);
        double meterPerDegree = distance(ref0, ref1);
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
