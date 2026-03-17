package com.tingfeng.util.java.base.common.utils;

/**
 * 地理相关工具
 */
public class GisUtils {
    /**
     * 赤道半径
     */
    private static final int EQUATOR_RADIUS = 6378137;
    /**
     * 扁率：地球是椭圆
     */
    private static final double F = 1/298.257223563;

    /**
     * 获取半径,单位米
     * 但是，‌地球并不是一个完美的球体，‌而是一个椭球体。‌
     * 为了简化计算，‌我们通常使用地球的平均半径，‌但在不同的纬度上，‌半径会略有不同。‌
     *
     * 对于地球的平均半径，‌我们通常使用6371 km。‌
     * 但是，‌为了更精确地计算特定纬度下的半径，‌我们可以使用以下近似公式：‌
     * R(φ) = R_eq × (1 - f × (2 - f) × sin^2(φ))
     * 其中，‌R_eq 是赤道半径（‌约6378.137 km）‌，‌f 是扁率（‌约1/298.257223563）‌，‌φ 是纬度（‌以弧度为单位）‌。‌
     *
     * 将φ=35°转换为弧度，‌并代入上述公式，‌我们可以得到北纬35°的地球平均半径。‌
     * 计算结果为：‌北纬35°的地球平均半径是 6364.09 km。‌
     * @param lat 纬度,角度,值取 0 ~ 90
     * @return
     */
    public static double getRadius(double lat){
        //计算弧度
        double radian = angleToRadian(lat);
        double sinR = Math.sin(radian);
        double r = EQUATOR_RADIUS * (1 - F * (2 - F) * sinR * sinR);
        return r;
    }

    /**
     * 角度转弧度
     * @param angle
     * @return
     */
    public static double angleToRadian(double angle){
        return Math.abs(angle) / 180 * Math.PI;
    }

    /**
     * 计算距离,执行距离
     * 计算原理参见：https://www.zhihu.com/tardis/bd/art/578159391?source_id=1001
     * 1. 纬度为0-90,经度 0-180
     * 2. 自动转为弧度计算
     * @param latA 纬度A,角度值
     * @param lngA 经度A,角度值
     * @param latB 纬度B,角度值
     * @param lngB 经度B,角度值
     * @return
     */
    public static double getDistance(double latA,double lngA,double latB,double lngB){
        double latAR = angleToRadian(latA);
        double latBR = angleToRadian(latB);
        double lngAR = angleToRadian(lngA);
        double lngBR = angleToRadian(lngB);
        double r = getRadius((latA + latB) / 2);
        double dLng = Math.abs(lngAR - lngBR);
        // 处理经度差超过180度的情况，使用最短路径
        if (dLng > Math.PI) {
            dLng = 2 * Math.PI - dLng;
        }
        double cosValue = Math.sin(latAR) * Math.sin(latBR) + Math.cos(latAR) * Math.cos(latBR)
                * Math.cos(dLng);
        // 确保cosValue在[-1, 1]范围内，避免Math.acos返回NaN
        cosValue = Math.max(-1, Math.min(1, cosValue));
        return r * Math.acos(cosValue);
    }

}