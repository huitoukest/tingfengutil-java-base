package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;

/**
 * 地理算子接口
 *
 * 定义地理计算的核心算子契约，包括距离、方位角、目标点推算、中点计算和
 * 多边形面积计算。所有方法基于地理坐标（经纬度），使用地球模型进行球面计算。
 *
 * 各方法输入输出说明：
 * - 距离相关：distance/direct/area 返回结果单位均为米
 * - 角度相关：azimuth 返回弧度值，范围 [-π, π]
 * - 坐标输入：所有 Coordinate 参数均不可为 null
 */
public interface EarthOp {

    /**
     * 获取算子名称标识
     *
     * @return 算子名称，如 "WGS84"、"Spherical" 等
     */
    String name();

    /**
     * 计算两点间的球面距离
     *
     * 使用地球模型计算起点到终点的大圆路径距离。
     *
     * @param from 起点坐标，不可为 null
     * @param to   终点坐标，不可为 null
     * @return 两点之间的距离，单位：米
     */
    double distance(Coordinate from, Coordinate to);

    /**
     * 已知起点、方位角和距离，推算目标点坐标
     *
     * @param point    起点坐标，不可为 null
     * @param azimuth  方位角（弧度），正北为 0，顺时针方向
     * @param distance 从起点到目标点的距离，单位：米
     * @return 目标点坐标
     */
    Coordinate direct(Coordinate point, double azimuth, double distance);

    /**
     * 计算起点到终点的初始方位角
     *
     * 初始方位角指从起点出发沿大圆路径到终点的初始方向角。
     *
     * @param from 起点坐标，不可为 null
     * @param to   终点坐标，不可为 null
     * @return 初始方位角，单位：弧度，范围 [-π, π]
     */
    double azimuth(Coordinate from, Coordinate to);

    /**
     * 计算起点到终点的大地线中点
     *
     * 中点为沿大圆路径上距离起点和终点等距的点。
     *
     * @param from 起点坐标，不可为 null
     * @param to   终点坐标，不可为 null
     * @return 中点坐标
     */
    Coordinate midpoint(Coordinate from, Coordinate to);

    /**
     * 计算多边形面积
     *
     * 输入为多边形顶点数组，格式为 [[latitude, longitude], ...]，
     * 顶点按顺序排列（顺时针或逆时针），首尾不需重复。
     *
     * @param points 多边形顶点坐标数组，格式 [[lat, lng], ...]，至少 3 个顶点
     * @return 多边形面积，单位：平方米
     * @throws IllegalArgumentException 如果顶点数少于 3 个
     */
    double area(double[][] points);
}
