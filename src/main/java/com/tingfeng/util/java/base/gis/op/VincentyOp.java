package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;

/**
 * Vincenty 椭球体高精度公式算子
 *
 * 基于 Vincenty, T. (1975) 的椭球体大地线正解（Direct Problem）和逆解
 * （Inverse Problem）公式，使用 WGS84 椭球参数，精度可达毫米级。
 * 实现 EarthOp 接口，提供高精度地理计算。
 *
 * 算法特点：
 * - 逆算法（distance/azimuth）：迭代求解大地线长度，最大 200 次迭代，收敛阈值 1e-12
 * - 正算法（direct）：迭代求解目标点坐标
 * - 不收敛或数值异常时降级到 WGS84Op（平均半径球面近似）
 * - 对跖点（antipodal points）附近可能不收敛，属于正常退化
 *
 * WGS84 椭球参数：
 * - 赤道半径（长半轴）a = 6378137 米
 * - 扁率 f = 1/298.257223563
 * - 极点半径（短半轴）b = a x (1 - f) = 6356752.314245 米
 *
 * 适用场景：
 * - 高精度导航与测量（毫米级精度）
 * - 大地测量学计算
 * - 需要最高精度距离/方位角的场景
 *
 * 注：由于 Java 枚举的 name() 方法为 final，与 EarthOp.name() 接口方法
 * 冲突，因此本类采用 final class + 私有构造器的单例模式。
 *
 * @see WGS84Op
 */
public final class VincentyOp extends AbstractEarthOp {

    /**
     * WGS84 赤道半径（长半轴），单位：米
     */
    private static final double EQUATOR_RADIUS = 6378137.0;

    /**
     * WGS84 扁率
     */
    private static final double FLATTENING = 1.0 / 298.257223563;

    /**
     * Vincenty 逆算法最大迭代次数
     */
    private static final int VINCENTY_MAX_ITERATIONS = 200;

    /**
     * Vincenty 逆算法收敛阈值（弧度）
     */
    private static final double VINCENTY_CONVERGENCE_THRESHOLD = 1e-12;

    /**
     * 单例实例
     */
    private static final VincentyOp INSTANCE = new VincentyOp();

    /**
     * 获取单例实例
     *
     * @return VincentyOp 单例
     */
    public static VincentyOp getInstance() {
        return INSTANCE;
    }

    /**
     * 私有构造器，禁止外部实例化
     */
    private VincentyOp() {
    }

    // ==================== EarthOp 接口实现 ====================

    @Override
    public String name() {
        return "Vincenty";
    }

    @Override
    public double distance(Coordinate from, Coordinate to) {
        requireNonNull(from);
        requireNonNull(to);
        return distanceByVincenty(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude());
    }

    @Override
    public Coordinate direct(Coordinate point, double azimuth, double distance) {
        requireNonNull(point);
        return directByVincenty(
                point.getLatitude(), point.getLongitude(), azimuth, distance);
    }

    @Override
    public double azimuth(Coordinate from, Coordinate to) {
        requireNonNull(from);
        requireNonNull(to);
        // azimuthByVincenty 返回 [0, 2π)，转换为 EarthOp 接口约定的 [-π, π] 范围
        double az = azimuthByVincenty(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude());
        if (az > Math.PI) {
            az -= 2.0 * Math.PI;
        }
        return az;
    }

    @Override
    public Coordinate midpoint(Coordinate from, Coordinate to) {
        requireNonNull(from);
        requireNonNull(to);
        return midpointByVincenty(
                from.getLatitude(), from.getLongitude(),
                to.getLatitude(), to.getLongitude());
    }

    /**
     * area() 继承 AbstractEarthOp 默认实现，基于 distance() 估算地球半径
     */

    // ==================== Vincenty 逆算法 ====================

    /**
     * 使用 Vincenty 逆算法计算两点之间的距离（高精度椭球体距离）
     *
     * 基于 Vincenty (1975) 的椭球体大地线逆解公式，精度可达毫米级。
     * 适用于 WGS84 椭球体模型的精确距离计算。
     *
     * 注意事项：
     * - 对跖点（antipodal points）附近可能不收敛，此时降级到 WGS84Op
     * - 相同点直接返回 0
     * - 计算成本高于球面公式，约 5-10 倍
     *
     * @param lat1 起点纬度（度）
     * @param lon1 起点经度（度）
     * @param lat2 终点纬度（度）
     * @param lon2 终点经度（度）
     * @return 两点之间的距离（米）
     */
    private double distanceByVincenty(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double lambda1 = Math.toRadians(lon1);
        double lambda2 = Math.toRadians(lon2);

        // 完全相同点，距离为 0
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }

        double a = EQUATOR_RADIUS;
        double f = FLATTENING;
        double b = a * (1.0 - f);

        // 归化纬度（reduced latitude）
        double tanU1 = (1.0 - f) * Math.tan(phi1);
        double tanU2 = (1.0 - f) * Math.tan(phi2);
        double cosU1 = 1.0 / Math.sqrt(1.0 + tanU1 * tanU1);
        double sinU1 = tanU1 * cosU1;
        double cosU2 = 1.0 / Math.sqrt(1.0 + tanU2 * tanU2);
        double sinU2 = tanU2 * cosU2;

        double L = lambda2 - lambda1;
        double lambda = L;

        double sinLambda;
        double cosLambda;
        double sinSigma;
        double cosSigma;
        double sigma;
        double sinAlpha;
        double cos2Alpha;
        double cos2SigmaM;
        double C;

        int iteration = 0;
        double lambdaPrev;
        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt(
                    (cosU2 * sinLambda) * (cosU2 * sinLambda)
                            + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
                            * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cos2Alpha = 1.0 - sinAlpha * sinAlpha;

            // 赤道上的点（cos2Alpha 接近 0），cos2SigmaM 设为 0
            if (cos2Alpha < 1e-12) {
                cos2SigmaM = 0.0;
            } else {
                cos2SigmaM = cosSigma - 2.0 * sinU1 * sinU2 / cos2Alpha;
            }

            C = f / 16.0 * cos2Alpha * (4.0 + f * (4.0 - 3.0 * cos2Alpha));

            lambdaPrev = lambda;
            lambda = L + (1.0 - C) * f * sinAlpha
                    * (sigma + C * sinSigma
                    * (cos2SigmaM + C * cosSigma
                    * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)));
            iteration++;
        } while (Math.abs(lambda - lambdaPrev) > VINCENTY_CONVERGENCE_THRESHOLD
                && iteration < VINCENTY_MAX_ITERATIONS);

        // 不收敛时降级到 WGS84Op
        if (iteration >= VINCENTY_MAX_ITERATIONS) {
            return WGS84Op.getInstance().distance(
                    new Coordinate(lat1, lon1), new Coordinate(lat2, lon2));
        }

        double u2 = cos2Alpha * (a * a - b * b) / (b * b);
        double A = 1.0 + u2 / 16384.0 * (4096.0 + u2 * (-768.0 + u2 * (320.0 - 175.0 * u2)));
        double B = u2 / 1024.0 * (256.0 + u2 * (-128.0 + u2 * (74.0 - 47.0 * u2)));

        double deltaSigma = B * sinSigma * (cos2SigmaM
                + B / 4.0 * (cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)
                - B / 6.0 * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma)
                * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)));

        double s = b * A * (sigma - deltaSigma);

        // NaN/Infinity 保护
        if (Double.isNaN(s) || Double.isInfinite(s)) {
            return WGS84Op.getInstance().distance(
                    new Coordinate(lat1, lon1), new Coordinate(lat2, lon2));
        }

        return s;
    }

    // ==================== Vincenty 正算法 ====================

    /**
     * 使用 Vincenty 正算法计算目标点（高精度椭球体目标点）
     *
     * 基于 Vincenty (1975) 的椭球体大地线正解公式，精度可达毫米级。
     * 适用于 WGS84 椭球体模型的高精度目标点计算。
     *
     * 注意事项：
     * - 对跖点附近可能不收敛，此时降级到 WGS84Op
     * - 相同点直接返回起点
     * - 经度自动归一化到 [-180, 180) 范围
     *
     * @param lat1     起点纬度（度）
     * @param lon1     起点经度（度）
     * @param azimuth  初始方位角（弧度，正北为 0，顺时针）
     * @param distance 距离（米）
     * @return 目标点 Coordinate
     */
    private Coordinate directByVincenty(double lat1, double lon1, double azimuth, double distance) {
        // 距离为零时返回起点
        if (distance <= 0.0) {
            return new Coordinate(lat1, lon1);
        }

        double phi1 = Math.toRadians(lat1);
        double lambda1 = Math.toRadians(lon1);
        double alpha1 = azimuth;

        double a = EQUATOR_RADIUS;
        double f = FLATTENING;
        double b = a * (1.0 - f);

        // 归化纬度（reduced latitude）
        double tanU1 = (1.0 - f) * Math.tan(phi1);
        double cosU1 = 1.0 / Math.sqrt(1.0 + tanU1 * tanU1);
        double sinU1 = tanU1 * cosU1;

        // 起点在赤道的特殊处理：tanU1 = 0，cosU1 = 1，sinU1 = 0，公式仍然有效

        // sigma1 = 从赤道到起点的大地线角距离
        double sigma1 = Math.atan2(tanU1, Math.cos(alpha1));
        double sinAlpha = cosU1 * Math.sin(alpha1);
        double cos2Alpha = 1.0 - sinAlpha * sinAlpha;

        // u2 = 辅助量
        double u2 = cos2Alpha * (a * a - b * b) / (b * b);
        double A = 1.0 + u2 / 16384.0 * (4096.0 + u2 * (-768.0 + u2 * (320.0 - 175.0 * u2)));
        double B = u2 / 1024.0 * (256.0 + u2 * (-128.0 + u2 * (74.0 - 47.0 * u2)));

        // 初始 sigma
        double sigma = distance / (b * A);
        double sinSigma;
        double cosSigma;
        double cos2SigmaM;
        double sigmaPrev;

        int iteration = 0;
        boolean converged = false;
        do {
            sinSigma = Math.sin(sigma);
            cosSigma = Math.cos(sigma);
            cos2SigmaM = Math.cos(2.0 * sigma1 + sigma);

            double deltaSigma = B * sinSigma * (cos2SigmaM
                    + B / 4.0 * (cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)
                    - B / 6.0 * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma)
                    * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)));

            sigmaPrev = sigma;
            sigma = distance / (b * A) + deltaSigma;
            iteration++;

            if (Math.abs(sigma - sigmaPrev) <= VINCENTY_CONVERGENCE_THRESHOLD) {
                converged = true;
                break;
            }
        } while (iteration < VINCENTY_MAX_ITERATIONS);

        // 不收敛时降级到 WGS84Op
        if (!converged) {
            return WGS84Op.getInstance().direct(
                    new Coordinate(lat1, lon1), azimuth, distance);
        }

        // 计算目标点纬度 phi2
        double tmpY = sinU1 * sinSigma - cosU1 * cosSigma * Math.cos(alpha1);
        double lat2 = Math.atan2(
                sinU1 * cosSigma + cosU1 * sinSigma * Math.cos(alpha1),
                (1.0 - f) * Math.sqrt(sinAlpha * sinAlpha + tmpY * tmpY));

        // 计算经度差 lambda
        double lambda = Math.atan2(
                sinSigma * Math.sin(alpha1),
                cosU1 * cosSigma - sinU1 * sinSigma * Math.cos(alpha1));

        // C = f/16 * cos2Alpha * (4 + f * (4 - 3 * cos2Alpha))
        double C = f / 16.0 * cos2Alpha * (4.0 + f * (4.0 - 3.0 * cos2Alpha));

        // L = lambda - (1-C) * f * sinAlpha * (sigma + C * sinSigma
        //     * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM^2)))
        double L = lambda - (1.0 - C) * f * sinAlpha
                * (sigma + C * sinSigma
                * (cos2SigmaM + C * cosSigma
                * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)));

        double lon2 = lambda1 + L;

        // 转换为度
        double lat2Deg = Math.toDegrees(lat2);
        double lon2Deg = Math.toDegrees(lon2);

        // 如果结果无效，降级到 WGS84Op
        if (Double.isNaN(lat2Deg) || Double.isInfinite(lat2Deg)
                || Double.isNaN(lon2Deg) || Double.isInfinite(lon2Deg)) {
            return WGS84Op.getInstance().direct(
                    new Coordinate(lat1, lon1), azimuth, distance);
        }

        // 经度归一化到 [-180, 180)
        if (lon2Deg > 180.0) {
            lon2Deg -= 360.0;
        } else if (lon2Deg < -180.0) {
            lon2Deg += 360.0;
        }

        return new Coordinate(lat2Deg, lon2Deg);
    }

    // ==================== Vincenty 方位角计算 ====================

    /**
     * 使用 Vincenty 逆算法计算起点到终点的初始方位角（高精度椭球体方位角）
     *
     * 基于 Vincenty 逆算法，在迭代求解大地线长度的同时计算初始方位角 alpha1。
     * 精度可达毫米级，适用于 WGS84 椭球体模型的高精度方位角计算。
     *
     * 方位角定义：正北为 0 弧度，顺时针递增，返回值在 [0, 2pi) 范围。
     *
     * 注意事项：
     * - 对跖点（antipodal points）附近可能不收敛，此时降级到 WGS84Op
     * - 相同点返回 0
     * - 如需 [-pi, pi] 范围，请调用 EarthOp.azimuth() 接口方法
     *
     * @param lat1 起点纬度（度）
     * @param lon1 起点经度（度）
     * @param lat2 终点纬度（度）
     * @param lon2 终点经度（度）
     * @return 初始方位角（弧度，取值范围 [0, 2pi)）
     */
    private double azimuthByVincenty(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double lambda1 = Math.toRadians(lon1);
        double lambda2 = Math.toRadians(lon2);

        double a = EQUATOR_RADIUS;
        double f = FLATTENING;

        // 归化纬度（reduced latitude）
        double tanU1 = (1.0 - f) * Math.tan(phi1);
        double tanU2 = (1.0 - f) * Math.tan(phi2);
        double cosU1 = 1.0 / Math.sqrt(1.0 + tanU1 * tanU1);
        double sinU1 = tanU1 * cosU1;
        double cosU2 = 1.0 / Math.sqrt(1.0 + tanU2 * tanU2);
        double sinU2 = tanU2 * cosU2;

        double L = lambda2 - lambda1;
        double lambda = L;

        double sinLambda;
        double cosLambda;
        double sinSigma;
        double cosSigma;
        double sigma;
        double sinAlpha;
        double cos2Alpha;
        double cos2SigmaM;
        double C;

        int iteration = 0;
        double lambdaPrev;
        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt(
                    (cosU2 * sinLambda) * (cosU2 * sinLambda)
                            + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
                            * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));

            // 相同点或对跖点，方位角无意义，返回 0
            if (sinSigma < 1e-12) {
                return 0.0;
            }

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cos2Alpha = 1.0 - sinAlpha * sinAlpha;

            if (cos2Alpha < 1e-12) {
                cos2SigmaM = 0.0;
            } else {
                cos2SigmaM = cosSigma - 2.0 * sinU1 * sinU2 / cos2Alpha;
            }

            C = f / 16.0 * cos2Alpha * (4.0 + f * (4.0 - 3.0 * cos2Alpha));

            lambdaPrev = lambda;
            lambda = L + (1.0 - C) * f * sinAlpha
                    * (sigma + C * sinSigma
                    * (cos2SigmaM + C * cosSigma
                    * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)));
            iteration++;
        } while (Math.abs(lambda - lambdaPrev) > VINCENTY_CONVERGENCE_THRESHOLD
                && iteration < VINCENTY_MAX_ITERATIONS);

        // 不收敛时降级到 WGS84Op，归一化到 [0, 2pi) 保持内部一致性
        if (iteration >= VINCENTY_MAX_ITERATIONS) {
            double fallbackAz = WGS84Op.getInstance().azimuth(
                    new Coordinate(lat1, lon1), new Coordinate(lat2, lon2));
            return fallbackAz >= 0.0 ? fallbackAz : fallbackAz + 2.0 * Math.PI;
        }

        // alpha1 = atan2(cosU2 * sinLambda, cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
        double azimuth = Math.atan2(
                cosU2 * sinLambda,
                cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);

        // NaN 保护
        if (Double.isNaN(azimuth) || Double.isInfinite(azimuth)) {
            double fallbackAz = WGS84Op.getInstance().azimuth(
                    new Coordinate(lat1, lon1), new Coordinate(lat2, lon2));
            return fallbackAz >= 0.0 ? fallbackAz : fallbackAz + 2.0 * Math.PI;
        }

        // 归一化到 [0, 2pi)
        return azimuth >= 0.0 ? azimuth : azimuth + 2.0 * Math.PI;
    }

    // ==================== Vincenty 中点计算 ====================

    /**
     * 使用 Vincenty 算法计算大地线中点坐标（高精度椭球体中点）
     *
     * 计算原理：先通过 Vincenty 逆算法计算总距离和初始方位角，
     * 再通过 Vincenty 正算法沿初始方位角前进一半距离得到中点。
     *
     * 注意事项：
     * - 对跖点附近可能不收敛，此时降级到 WGS84Op
     * - 相同点直接返回该点
     * - 经度自动归一化到 [-180, 180) 范围
     *
     * @param lat1 起点纬度（度）
     * @param lon1 起点经度（度）
     * @param lat2 终点纬度（度）
     * @param lon2 终点经度（度）
     * @return 大地线中点坐标
     */
    private Coordinate midpointByVincenty(double lat1, double lon1, double lat2, double lon2) {
        double azimuth = azimuthByVincenty(lat1, lon1, lat2, lon2);
        double distance = distanceByVincenty(lat1, lon1, lat2, lon2);

        // 距离为零或无效时返回起点
        if (distance <= 0.0 || Double.isNaN(distance) || Double.isInfinite(distance)) {
            return new Coordinate(lat1, lon1);
        }

        return directByVincenty(lat1, lon1, azimuth, distance / 2.0);
    }
}
