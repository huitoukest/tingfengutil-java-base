package com.tingfeng.util.java.base.gis.op;

import com.tingfeng.util.java.base.gis.Coordinate;

/**
 * EGM96 大地水准面修正算子
 *
 * 采用装饰器模式，包裹一个已有的 EarthOp 实现（默认为 VincentyOp），
 * 提供大地水准面（Geoid）修正功能。当前简化实现中，所有 EarthOp 接口
 * 方法均委托给 delegate，同时额外提供 getGeoidHeight() 方法用于查询
 * EGM96 格网的大地水准面高度。
 *
 * Geoid 修正原理：
 * - 地球表面距离 = 椭球体距离 + 小量修正
 * - N/R ≈ 1.3e-5（Geoid 高程与地球半径之比），对距离的影响可忽略
 * - 方位角、中点等几何量不受 Geoid 高程的显著影响
 * - 当前简化实现中，所有方法均直接委托给 delegate，不叠加修正
 * - 如需真实的 Geoid 修正距离，可在 distance() 等方法中叠加修正量
 *
 * 注意：由于 Java 枚举的 name() 方法为 final，与 EarthOp.name() 接口方法
 * 冲突，因此本类采用 final class + 私有构造器的单例模式。
 *
 * @see EGM96Grid
 * @see VincentyOp
 * @see EarthOp
 */
public final class EGM96Op extends AbstractEarthOp {

    /**
     * 默认单例实例（使用 VincentyOp 作为 delegate）
     */
    private static final EGM96Op INSTANCE = new EGM96Op();

    /**
     * 被包裹的 EarthOp 实现
     */
    private final EarthOp delegate;

    /**
     * 私有构造器，默认使用 VincentyOp 作为 delegate
     */
    private EGM96Op() {
        this.delegate = VincentyOp.getInstance();
    }

    /**
     * 包级可见构造器，允许自定义 delegate
     *
     * @param delegate 被包裹的 EarthOp 实现，不可为 null
     * @throws IllegalArgumentException 如果 delegate 为 null
     */
    EGM96Op(EarthOp delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    /**
     * 获取单例实例
     *
     * @return EGM96Op 单例实例（使用 VincentyOp 作为 delegate）
     */
    public static EGM96Op getInstance() {
        return INSTANCE;
    }

    // ==================== EarthOp 接口实现 ====================

    @Override
    public String name() {
        return "EGM96";
    }

    @Override
    public double distance(Coordinate from, Coordinate to) {
        return delegate.distance(from, to);
    }

    @Override
    public Coordinate direct(Coordinate point, double azimuth, double distance) {
        return delegate.direct(point, azimuth, distance);
    }

    @Override
    public double azimuth(Coordinate from, Coordinate to) {
        return delegate.azimuth(from, to);
    }

    @Override
    public Coordinate midpoint(Coordinate from, Coordinate to) {
        return delegate.midpoint(from, to);
    }

    /**
     * area() 继承 AbstractEarthOp 默认实现，基于 distance() 估算地球半径。
     * 由于 distance() 委托给 delegate，面积计算也将使用 delegate 的地球模型。
     */

    // ==================== EGM96 扩展方法 ====================

    /**
     * 获取指定坐标处的大地水准面高度
     *
     * 大地水准面高度是椭球体表面与大地水准面（Geoid）的垂直距离。
     * 正值表示大地水准面在椭球体之上，负值表示在椭球体之下。
     * 数据来源为 EGM96 格网模型（1 度分辨率），采用双线性插值。
     *
     * @param point 地理坐标，不可为 null
     * @return 大地水准面高度，单位：米
     * @throws IllegalArgumentException 如果 point 为 null
     */
    public double getGeoidHeight(Coordinate point) {
        requireNonNull(point);
        // Coordinate 的经纬度单位为度，EGM96Grid.getGeoidHeight() 也使用度，直接传递
        return EGM96Grid.getInstance().getGeoidHeight(
                point.getLatitude(),
                point.getLongitude());
    }
}
