package com.tingfeng.util.java.base.gis;

/**
 * 地理相关工具
 * 
 * 坐标系说明：
 * - WGS84：国际标准坐标系，GPS设备获取的原始坐标，适用于Google地图（国外）、OSM、ArcGIS等
 * - GCJ02：国测局坐标系（火星坐标系），中国国测局制定的加密坐标系，适用于高德地图、腾讯地图、Google地图（国内）
 * - BD09：百度坐标系，在GCJ02基础上进行二次加密，仅适用于百度地图
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
     * 圆周率
     */
    private static final double PI = 3.1415926535897932384626;
    
    /**
     * 百度坐标系转换参数
     */
    private static final double X_PI = 3.14159265358979324 * 3000.0 / 180.0;
    
    /**
     * WGS84转GCJ02的转换参数
     */
    private static final double SEMI_MAJOR = 6378245.0;
    private static final double ECCENTRICITY_SQUARED = 0.00669342162296594323;

    /**
     * 获取半径,单位米
     * 地球是一个椭球体，在不同纬度上，从地心到表面的距离不同。
     *
     * 使用WGS84椭球体参数：
     * - 赤道半径（长半轴）a = 6378137米
     * - 极点半径（短半轴）b = a × (1 - f) = 6356752.314245米
     * - 扁率 f = 1/298.257223563
     *
     * 椭球体半径公式：R(φ) = √(a²cos²φ + b²sin²φ)
     * 其中 φ 是纬度（以弧度为单位）
     *
     * 计算结果示例：
     * - 赤道（0°）：R = 6378137米
     * - 北纬35°：R ≈ 6371109米
     * - 极点（90°）：R = 6356752米
     * @param lat 纬度,角度,值取 0 ~ 90
     * @return 指定纬度处的地球半径（米）
     */
    public static double getRadius(double lat){
        double radian = Math.toRadians(lat);
        double cosR = Math.cos(radian);
        double sinR = Math.sin(radian);
        double a = EQUATOR_RADIUS;
        double b = EQUATOR_RADIUS * (1 - F);
        double r = Math.sqrt(a * a * cosR * cosR + b * b * sinR * sinR);
        return r;
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
     * @return 两点之间的距离（米）
     */
    public static double getDistance(double latA,double lngA,double latB,double lngB){
        double latAR = Math.toRadians(latA);
        double latBR = Math.toRadians(latB);
        double lngAR = Math.toRadians(lngA);
        double lngBR = Math.toRadians(lngB);
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

    /**
     * 计算两点间的方位角（初始方位角）
     * @param latA 起点纬度，角度值
     * @param lngA 起点经度，角度值
     * @param latB 终点纬度，角度值
     * @param lngB 终点经度，角度值
     * @return 方位角（角度，0-360度，正北为0度）
     */
    public static double getBearing(double latA, double lngA, double latB, double lngB) {
        double latAR = Math.toRadians(latA);
        double latBR = Math.toRadians(latB);
        double dLng = Math.toRadians(lngB - lngA);
        
        double y = Math.sin(dLng) * Math.cos(latBR);
        double x = Math.cos(latAR) * Math.sin(latBR) - Math.sin(latAR) * Math.cos(latBR) * Math.cos(dLng);
        
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    /**
     * 计算两点间的中点
     * @param latA 点A纬度，角度值
     * @param lngA 点A经度，角度值
     * @param latB 点B纬度，角度值
     * @param lngB 点B经度，角度值
     * @return 中点坐标 [纬度, 经度]
     */
    public static double[] getMidpoint(double latA, double lngA, double latB, double lngB) {
        double latAR = Math.toRadians(latA);
        double latBR = Math.toRadians(latB);
        double dLng = Math.toRadians(lngB - lngA);
        
        double bx = Math.cos(latBR) * Math.cos(dLng);
        double by = Math.cos(latBR) * Math.sin(dLng);
        
        double latMR = Math.atan2(Math.sin(latAR) + Math.sin(latBR),
                Math.sqrt((Math.cos(latAR) + bx) * (Math.cos(latAR) + bx) + by * by));
        double lngMR = latAR + Math.atan2(by, Math.cos(latAR) + bx);
        
        return new double[]{Math.toDegrees(latMR), Math.toDegrees(lngMR)};
    }

    /**
     * 根据起点、方位角和距离计算目标点
     * @param lat 起点纬度，角度值
     * @param lng 起点经度，角度值
     * @param bearing 方位角，角度值（0-360度，正北为0度）
     * @param distance 距离，单位：米
     * @return 目标点坐标 [纬度, 经度]
     */
    public static double[] getDestinationPoint(double lat, double lng, double bearing, double distance) {
        double latR = Math.toRadians(lat);
        double lngR = Math.toRadians(lng);
        double bearingR = Math.toRadians(bearing);
        double r = getRadius(lat);
        
        double latDR = Math.asin(Math.sin(latR) * Math.cos(distance / r) +
                Math.cos(latR) * Math.sin(distance / r) * Math.cos(bearingR));
        double lngDR = lngR + Math.atan2(Math.sin(bearingR) * Math.sin(distance / r) * Math.cos(latR),
                Math.cos(distance / r) - Math.sin(latR) * Math.sin(latDR));
        
        return new double[]{Math.toDegrees(latDR), Math.toDegrees(lngDR)};
    }

    /**
     * 计算多边形面积（使用球面几何公式）
     * @param points 多边形顶点坐标数组，格式：[[lat1, lng1], [lat2, lng2], ...]
     * @return 面积，单位：平方米
     */
    public static double getPolygonArea(double[][] points) {
        if (points == null || points.length < 3) {
            return 0;
        }
        
        double area = 0;
        int n = points.length;
        double r = EQUATOR_RADIUS;
        
        for (int i = 0; i < n; i++) {
            double lat1 = Math.toRadians(points[i][0]);
            double lng1 = Math.toRadians(points[i][1]);
            double lat2 = Math.toRadians(points[(i + 1) % n][0]);
            double lng2 = Math.toRadians(points[(i + 1) % n][1]);
            
            area += (lng2 - lng1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }
        
        area = Math.abs(area * r * r / 2.0);
        return area;
    }

    /**
     * 计算路径总距离
     * @param points 路径点坐标数组，格式：[[lat1, lng1], [lat2, lng2], ...]
     * @return 总距离，单位：米
     */
    public static double getPathDistance(double[][] points) {
        if (points == null || points.length < 2) {
            return 0;
        }
        
        double totalDistance = 0;
        for (int i = 0; i < points.length - 1; i++) {
            totalDistance += getDistance(points[i][0], points[i][1], points[i + 1][0], points[i + 1][1]);
        }
        
        return totalDistance;
    }

    /**
     * 判断点是否在多边形内（射线法）
     * @param lat 点的纬度
     * @param lng 点的经度
     * @param polygon 多边形顶点坐标数组，格式：[[lat1, lng1], [lat2, lng2], ...]
     * @return true-在多边形内，false-在多边形外
     */
    public static boolean isPointInPolygon(double lat, double lng, double[][] polygon) {
        if (polygon == null || polygon.length < 3) {
            return false;
        }
        
        boolean inside = false;
        int n = polygon.length;
        
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double latI = polygon[i][0];
            double lngI = polygon[i][1];
            double latJ = polygon[j][0];
            double lngJ = polygon[j][1];
            
            if (((latI > lat) != (latJ > lat)) &&
                    (lng < (lngJ - lngI) * (lat - latI) / (latJ - latI) + lngI)) {
                inside = !inside;
            }
        }
        
        return inside;
    }

    /**
     * 计算点到线段的最短距离
     * @param lat 点的纬度
     * @param lng 点的经度
     * @param lat1 线段起点纬度
     * @param lng1 线段起点经度
     * @param lat2 线段终点纬度
     * @param lng2 线段终点经度
     * @return 最短距离，单位：米
     */
    public static double getDistanceToLine(double lat, double lng, double lat1, double lng1, double lat2, double lng2) {
        double d13 = getDistance(lat, lng, lat1, lng1);
        double d23 = getDistance(lat, lng, lat2, lng2);
        double d12 = getDistance(lat1, lng1, lat2, lng2);
        
        if (d13 * d13 >= d23 * d23 + d12 * d12) {
            return d23;
        }
        if (d23 * d23 >= d13 * d13 + d12 * d12) {
            return d13;
        }
        
        double p = (d13 + d23 + d12) / 2;
        double area = Math.sqrt(p * (p - d13) * (p - d23) * (p - d12));
        return 2 * area / d12;
    }

    /**
     * 十进制经纬度转度分秒格式
     * @param decimal 十进制经纬度
     * @return 度分秒数组 [度, 分, 秒]
     */
    public static double[] decimalToDMS(double decimal) {
        double degrees = Math.floor(decimal);
        double minutes = Math.floor((decimal - degrees) * 60);
        double seconds = ((decimal - degrees) * 60 - minutes) * 60;
        return new double[]{degrees, minutes, seconds};
    }

    /**
     * 度分秒格式转十进制经纬度
     * @param degrees 度
     * @param minutes 分
     * @param seconds 秒
     * @return 十进制经纬度
     */
    public static double dmsToDecimal(double degrees, double minutes, double seconds) {
        return degrees + minutes / 60.0 + seconds / 3600.0;
    }

    /**
     * 十进制经纬度转度分秒字符串
     * @param decimal 十进制经纬度
     * @return 度分秒字符串，格式：116°24'12.36"
     */
    public static String decimalToDMSString(double decimal) {
        double[] dms = decimalToDMS(decimal);
        return String.format("%.0f°%.0f'%.2f\"", dms[0], dms[1], dms[2]);
    }

    /**
     * WGS84转GCJ02（火星坐标系）
     * 适用地图：高德地图、腾讯地图、Google地图（国内）
     * @param lat 纬度
     * @param lng 经度
     * @return GCJ02坐标 [纬度, 经度]
     */
    public static double[] wgs84ToGcj02(double lat, double lng) {
        if (outOfChina(lat, lng)) {
            return new double[]{lat, lng};
        }
        
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ECCENTRICITY_SQUARED * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        
        dLat = (dLat * 180.0) / ((SEMI_MAJOR * (1 - ECCENTRICITY_SQUARED)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (SEMI_MAJOR / sqrtMagic * Math.cos(radLat) * PI);
        
        double mgLat = lat + dLat;
        double mgLng = lng + dLng;
        
        return new double[]{mgLat, mgLng};
    }

    /**
     * GCJ02转WGS84
     * 适用地图：从高德、腾讯等地图转换回国际标准坐标
     * @param lat 纬度
     * @param lng 经度
     * @return WGS84坐标 [纬度, 经度]
     */
    public static double[] gcj02ToWgs84(double lat, double lng) {
        if (outOfChina(lat, lng)) {
            return new double[]{lat, lng};
        }
        
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - ECCENTRICITY_SQUARED * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        
        dLat = (dLat * 180.0) / ((SEMI_MAJOR * (1 - ECCENTRICITY_SQUARED)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0) / (SEMI_MAJOR / sqrtMagic * Math.cos(radLat) * PI);
        
        double mgLat = lat + dLat;
        double mgLng = lng + dLng;
        
        return new double[]{lat * 2 - mgLat, lng * 2 - mgLng};
    }

    /**
     * GCJ02转BD09（百度坐标系）
     * 适用地图：从高德、腾讯等地图转换到百度地图
     * @param lat 纬度
     * @param lng 经度
     * @return BD09坐标 [纬度, 经度]
     */
    public static double[] gcj02ToBd09(double lat, double lng) {
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
        
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        
        return new double[]{bdLat, bdLng};
    }

    /**
     * BD09转GCJ02
     * 适用地图：从百度地图转换到高德、腾讯等地图
     * @param lat 纬度
     * @param lng 经度
     * @return GCJ02坐标 [纬度, 经度]
     */
    public static double[] bd09ToGcj02(double lat, double lng) {
        double x = lng - 0.0065;
        double y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        
        double gcjLng = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);
        
        return new double[]{gcjLat, gcjLng};
    }

    /**
     * WGS84转BD09（百度坐标系）
     * 适用地图：从GPS设备转换到百度地图
     * @param lat 纬度
     * @param lng 经度
     * @return BD09坐标 [纬度, 经度]
     */
    public static double[] wgs84ToBd09(double lat, double lng) {
        double[] gcj02 = wgs84ToGcj02(lat, lng);
        return gcj02ToBd09(gcj02[0], gcj02[1]);
    }

    /**
     * BD09转WGS84
     * 适用地图：从百度地图转换回GPS设备坐标
     * @param lat 纬度
     * @param lng 经度
     * @return WGS84坐标 [纬度, 经度]
     */
    public static double[] bd09ToWgs84(double lat, double lng) {
        double[] gcj02 = bd09ToGcj02(lat, lng);
        return gcj02ToWgs84(gcj02[0], gcj02[1]);
    }

    /**
     * 判断坐标是否在中国境外
     * @param lat 纬度
     * @param lng 经度
     * @return true-在中国境外，false-在中国境内
     */
    private static boolean outOfChina(double lat, double lng) {
        if (lng < 72.004 || lng > 137.8347) {
            return true;
        }
        if (lat < 0.8293 || lat > 55.8271) {
            return true;
        }
        return false;
    }

    /**
     * 纬度转换函数（用于WGS84转GCJ02）
     * @param lng 经度偏移
     * @param lat 纬度偏移
     * @return 纬度转换值
     */
    private static double transformLat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320.0 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 经度转换函数（用于WGS84转GCJ02）
     * @param lng 经度偏移
     * @param lat 纬度偏移
     * @return 经度转换值
     */
    private static double transformLng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

}