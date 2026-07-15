package com.tingfeng.util.java.base.gis.model;

/**
 * 地球模型策略接口
 *
 * 提供不同地球模型的抽象，支持：
 * - WGS84：国际标准坐标系，使用椭球体模型，精度最高，适用于 GPS 和国际地图
 * - Spherical：简化球体模型，半径固定为 6371000 米，计算速度更快但精度较低
 *
 * 精度差异说明：
 * - WGS84 椭球体模型：考虑地球扁率，计算结果更精确，但计算量较大
 * - 球体模型：忽略地球扁率，将地球简化为完美球体，精度约在 0.3% 以内
 *
 * 典型应用场景：
 * - WGS84：GPS 导航、测绘、航空等高精度需求
 * - Spherical：快速估算、日常距离计算、性能敏感场景
 */
public interface EarthModel {

    /**
     * 获取指定纬度处的地球半径
     *
     * @param latitude 纬度（度）
     * @return 该纬度处的地球半径（米）
     */
    double getRadius(double latitude);

    /**
     * 计算两点之间的距离
     *
     * @param pointA 起点坐标
     * @param pointB 终点坐标
     * @return 两点之间的距离（米）
     */
    double getDistance(Coordinate pointA, Coordinate pointB);

    /**
     * 获取地球模型名称
     *
     * @return 模型名称，如 "WGS84" 或 "Spherical"
     */
    String getName();

    /**
     * 获取平均地球半径
     *
     * @return 平均半径（米）
     */
    double getAverageRadius();
}
