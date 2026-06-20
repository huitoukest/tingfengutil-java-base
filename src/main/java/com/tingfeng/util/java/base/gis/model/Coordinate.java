package com.tingfeng.util.java.base.gis.model;

import java.util.Objects;

/**
 * 地理坐标不可变值对象
 *
 * 经纬度范围：
 * - 纬度（latitude）：-90 到 +90 度
 * - 经度（longitude）：-180 到 +180 度
 *
 * 与 double[] 数组相比，Coordinate 提供了：
 * - 类型安全：编译期检查
 * - 语义明确：latitude/longitude 不会混淆
 * - 不可变性：创建后不可修改，线程安全
 * - 范围校验：构造时自动校验，无效坐标抛 IllegalArgumentException
 */
public final class Coordinate {

    /**
     * 纬度范围最小值
     */
    public static final double MIN_LATITUDE = -90.0;

    /**
     * 纬度范围最大值
     */
    public static final double MAX_LATITUDE = 90.0;

    /**
     * 经度范围最小值
     */
    public static final double MIN_LONGITUDE = -180.0;

    /**
     * 经度范围最大值
     */
    public static final double MAX_LONGITUDE = 180.0;

    /**
     * 纬度（latitude），单位：度
     */
    private final double latitude;

    /**
     * 经度（longitude），单位：度
     */
    private final double longitude;

    /**
     * 创建坐标实例，校验经纬度范围
     *
     * @param latitude  纬度，范围 [-90, 90]
     * @param longitude 经度，范围 [-180, 180]
     * @throws IllegalArgumentException 如果纬度或经度超出有效范围
     */
    public Coordinate(double latitude, double longitude) {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new IllegalArgumentException(
                    String.format("纬度必须在 [%f, %f] 范围内，实际值：%f", MIN_LATITUDE, MAX_LATITUDE, latitude));
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new IllegalArgumentException(
                    String.format("经度必须在 [%f, %f] 范围内，实际值：%f", MIN_LONGITUDE, MAX_LONGITUDE, longitude));
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * 获取纬度
     *
     * @return 纬度（度）
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * 获取经度
     *
     * @return 经度（度）
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * 转换为 double 数组，格式 [latitude, longitude]
     *
     * @return double 数组
     */
    public double[] toArray() {
        return new double[]{latitude, longitude};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Coordinate that = (Coordinate) o;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
