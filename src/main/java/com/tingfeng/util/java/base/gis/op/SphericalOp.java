package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;

/**
 * 固定半径球面快速计算算子
 *
 * 将地球简化为完美球体，半径固定为 6371000 米（与旧的 SphericalEarthModel 一致）。
 * 采用 Haversine 公式计算大圆距离，使用标准球面正算、方位角和中点公式。
 * 所有计算使用封闭公式（非迭代），性能最优，适合大批量快速估算场景。
 *
 * 适用场景：
 * - 快速距离估算（亚微秒级每次计算）
 * - 非高精度导航（误差约 0.3%，与 WGS84 椭球模型相比）
 * - 性能敏感的大批量地理计算
 *
 * @see AbstractEarthOp
 * @see EarthOp
 */
public final class SphericalOp extends AbstractEarthOp {

    /**
     * 单例实例
     */
    private static final SphericalOp INSTANCE = new SphericalOp();

    /**
     * 地球平均半径，单位：米
     */
    private static final double R = 6371000.0;

    /**
     * 赤道参考坐标（0°, 0°）
     */
    private static final Coordinate REF_LAT0_LON0 = new Coordinate(0, 0);

    /**
     * 赤道参考坐标（0°, 1°）
     */
    private static final Coordinate REF_LAT0_LON1 = new Coordinate(0, 1);

    /**
     * 获取单例实例
     *
     * @return SphericalOp 单例
     */
    public static SphericalOp getInstance() {
        return INSTANCE;
    }

    /**
     * 私有构造器，禁止外部实例化
     */
    private SphericalOp() {
    }

    @Override
    public String name() {
        return "Spherical";
    }

    /**
     * Haversine 公式计算球面距离
     *
     * <pre>
     * a = sin&sup2;(&Delta;lat/2) + cos(lat1)&middot;cos(lat2)&middot;sin&sup2;(&Delta;lon/2)
     * c = 2 &middot; atan2(&radic;a, &radic;(1-a))
     * d = R &middot; c
     * </pre>
     */
    @Override
    public double distance(Coordinate from, Coordinate to) {
        requireNonNull(from);
        requireNonNull(to);

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

        return R * c;
    }

    /**
     * 球面正算（Direct problem）
     *
     * 已知起点、方位角和距离，推算目标点坐标。基于球面三角形公式。
     * <pre>
     * lat2 = asin(sin(lat1)&middot;cos(d/R) + cos(lat1)&middot;sin(d/R)&middot;cos(azimuth))
     * lon2 = lon1 + atan2(sin(azimuth)&middot;sin(d/R)&middot;cos(lat1), cos(d/R) - sin(lat1)&middot;sin(lat2))
     * </pre>
     * 经度归一化到 [-180, 180) 范围。
     */
    @Override
    public Coordinate direct(Coordinate point, double azimuth, double distance) {
        requireNonNull(point);

        double lat1 = Math.toRadians(point.getLatitude());
        double lon1 = Math.toRadians(point.getLongitude());
        double angDist = distance / R;
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
        if (lon2Deg >= 180.0) {
            lon2Deg -= 360.0;
        } else if (lon2Deg < -180.0) {
            lon2Deg += 360.0;
        }

        return new Coordinate(lat2Deg, lon2Deg);
    }

    /**
     * 大圆初始方位角
     *
     * 计算从起点到终点的初始大圆方位角（正北为 0，顺时针方向）。
     * <pre>
     * azimuth = atan2(sin(&Delta;lon)&middot;cos(lat2), cos(lat1)&middot;sin(lat2) - sin(lat1)&middot;cos(lat2)&middot;cos(&Delta;lon))
     * </pre>
     * 返回值范围 [-&pi;, &pi;]，符合 {@link EarthOp#azimuth(Coordinate, Coordinate)} 接口约定。
     */
    @Override
    public double azimuth(Coordinate from, Coordinate to) {
        requireNonNull(from);
        requireNonNull(to);

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

    /**
     * 球面中点
     *
     * 计算沿大圆路径上距离起点和终点等距的中点坐标。
     * <pre>
     * Bx = cos(lat2)&middot;cos(&Delta;lon)
     * By = cos(lat2)&middot;sin(&Delta;lon)
     * lat3 = atan2(sin(lat1)+sin(lat2), &radic;((cos(lat1)+Bx)&sup2; + By&sup2;))
     * lon3 = lon1 + atan2(By, cos(lat1)+Bx)
     * </pre>
     * 经度归一化到 [-180, 180) 范围。
     */
    @Override
    public Coordinate midpoint(Coordinate from, Coordinate to) {
        requireNonNull(from);
        requireNonNull(to);

        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLon = lon2 - lon1;

        double bx = Math.cos(lat2) * Math.cos(dLon);
        double by = Math.cos(lat2) * Math.sin(dLon);

        double latM = Math.atan2(
                Math.sin(lat1) + Math.sin(lat2),
                Math.sqrt((Math.cos(lat1) + bx) * (Math.cos(lat1) + bx) + by * by));
        double lonM = lon1 + Math.atan2(by, Math.cos(lat1) + bx);

        double latDeg = Math.toDegrees(latM);
        double lonDeg = Math.toDegrees(lonM);

        // 经度归一化到 [-180, 180)
        if (lonDeg >= 180.0) {
            lonDeg -= 360.0;
        } else if (lonDeg < -180.0) {
            lonDeg += 360.0;
        }

        return new Coordinate(latDeg, lonDeg);
    }
}
