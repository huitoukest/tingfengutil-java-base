package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;

/**
 * 地理算子注册中心和通用工具类
 *
 * 提供常用的地球算子实例（单例）和地理计算通用工具方法，
 * 包括坐标校验、单位转换、经度归一化等。
 *
 * 可用算子实例：
 * - {@link #WGS84}：WGS84 椭球基本公式算子，Haversine 公式，米级精度，快速
 * - {@link #VINCENTY}：Vincenty 椭球体高精度公式算子，毫米级精度
 * - {@link #KARNEY}：Karney 增强椭球体算子，亚毫米级精度，全局收敛
 * - {@link #SPHERICAL}：固定半径球面快速计算算子，封闭公式，性能最优
 * - {@link #EGM96}：EGM96 大地水准面修正算子，基于 Vincenty 椭球体 + Geoid 修正
 */
public final class Ops {

    // ========== 默认算子实例 ==========

    /**
     * WGS84 椭球基本公式算子
     *
     * 使用平均半径 R=(2a+b)/3 和 Haversine 公式，计算速度快，米级精度。
     */
    public static final EarthOp WGS84 = WGS84Op.getInstance();

    /**
     * Vincenty 椭球体高精度公式算子
     *
     * 基于 Vincenty 迭代算法，毫米级精度，对跖点附近降级到球面公式。
     */
    public static final EarthOp VINCENTY = VincentyOp.getInstance();

    /**
     * Karney 增强椭球体高精度算子
     *
     * 采用牛顿-拉夫森增强的 Vincenty 型迭代，亚毫米级精度，全局收敛。
     */
    public static final EarthOp KARNEY = KarneyOp.getInstance();

    /**
     * Spherical 球面公式算子
     *
     * 使用固定半径（R=6371000m）的纯球面模型，基于 Haversine 公式，
     * 适用于快速估算、大批量计算等性能敏感场景。
     */
    public static final EarthOp SPHERICAL = SphericalOp.getInstance();

    /**
     * EGM96 大地水准面修正算子
     *
     * 采用装饰器模式包裹 VincentyOp，提供 EGM96 大地水准面（Geoid）修正。
     * 当前简化实现中，所有 EarthOp 接口方法委托给 VincentyOp，
     * 同时额外提供 getGeoidHeight() 方法查询 EGM96 格网数据。
     */
    public static final EarthOp EGM96 = EGM96Op.getInstance();

    // ========== 构造器 ==========

    /**
     * 私有构造器，禁止外部实例化
     */
    private Ops() {
    }

    // ========== 坐标校验 ==========

    /**
     * 校验纬度值是否在有效范围内
     *
     * @param lat 纬度值（度）
     * @throws IllegalArgumentException 如果纬度超出 [-90, 90] 范围
     */
    public static void validateLatitude(double lat) {
        if (lat < Coordinate.MIN_LATITUDE || lat > Coordinate.MAX_LATITUDE) {
            throw new IllegalArgumentException(
                    String.format("纬度必须在 [%.0f, %.0f] 范围内，实际值：%f",
                            Coordinate.MIN_LATITUDE, Coordinate.MAX_LATITUDE, lat));
        }
    }

    /**
     * 校验经度值是否在有效范围内
     *
     * @param lon 经度值（度）
     * @throws IllegalArgumentException 如果经度超出 [-180, 180] 范围
     */
    public static void validateLongitude(double lon) {
        if (lon < Coordinate.MIN_LONGITUDE || lon > Coordinate.MAX_LONGITUDE) {
            throw new IllegalArgumentException(
                    String.format("经度必须在 [%.0f, %.0f] 范围内，实际值：%f",
                            Coordinate.MIN_LONGITUDE, Coordinate.MAX_LONGITUDE, lon));
        }
    }

    /**
     * 校验经纬度值是否在有效范围内
     *
     * @param lat 纬度值（度）
     * @param lon 经度值（度）
     * @throws IllegalArgumentException 如果纬度或经度超出有效范围
     */
    public static void validateCoordinate(double lat, double lon) {
        validateLatitude(lat);
        validateLongitude(lon);
    }

    // ========== 单位转换 ==========

    /**
     * 将角度转换为弧度
     *
     * @param degrees 角度值（度）
     * @return 弧度值
     */
    public static double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    /**
     * 将弧度转换为角度
     *
     * @param radians 弧度值
     * @return 角度值（度）
     */
    public static double toDegrees(double radians) {
        return Math.toDegrees(radians);
    }

    /**
     * 将米转换为公里
     *
     * @param meters 距离，单位：米
     * @return 距离，单位：公里
     */
    public static double metersToKilometers(double meters) {
        return meters / 1000.0;
    }

    /**
     * 将公里转换为米
     *
     * @param km 距离，单位：公里
     * @return 距离，单位：米
     */
    public static double kilometersToMeters(double km) {
        return km * 1000.0;
    }

    // ========== 经度归一化 ==========

    /**
     * 经度归一化到 [-180, 180) 范围
     *
     * 处理正负数和边界情况：
     * - 180 度映射为 -180 度
     * - 360 度映射为 0 度
     * - 大于 360 的值循环映射
     *
     * @param lon 原始经度（度）
     * @return 归一化后的经度，范围 [-180, 180)
     */
    public static double normalizeLongitude(double lon) {
        double normalized = lon % 360.0;
        if (normalized >= 180.0) {
            normalized -= 360.0;
        } else if (normalized < -180.0) {
            normalized += 360.0;
        }
        return normalized;
    }

    /**
     * 经度归一化到 [0, 360) 范围
     *
     * 处理正负数和边界情况：
     * - -90 度映射为 270 度
     * - 0 度映射为 0 度
     * - 大于 360 的值循环映射
     *
     * @param lon 原始经度（度）
     * @return 归一化后的经度，范围 [0, 360)
     */
    public static double normalizeLongitudePositive(double lon) {
        double normalized = lon % 360.0;
        if (normalized < 0) {
            normalized += 360.0;
        }
        return normalized;
    }
}
