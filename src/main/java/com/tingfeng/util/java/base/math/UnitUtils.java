package com.tingfeng.util.java.base.math;

/**
 * 量纲/单位工具类：字节格式化与温度换算
 *
 * formatBytes 采用 1024 进制（B/KB/MB/GB/TB/PB/EB，EB 封顶），
 * 内部以 double 计算：值域不超过 9.2e18，double 相对精度 2^-52
 * ≈ 2.2e-16，对 1 位小数展示无感知，仅作展示用途足够。
 *
 * 温度换算提供摄氏/华氏/开尔文三种温标两两互转共 6 方法，
 * 公式互为逆运算，可安全往返；开尔文输入小于 0（低于绝对零度）
 * 无物理意义，抛 IllegalArgumentException。
 */
public final class UnitUtils {

    /** 1024 进制单位数组，EB 封顶（Long.MAX ≈ 8 EB） */
    private static final String[] BYTE_UNITS = {"B", "KB", "MB", "GB", "TB", "PB", "EB"};

    /** 绝对零度偏移: 0°C = 273.15K */
    private static final double KELVIN_OFFSET = 273.15;

    private UnitUtils() {}

    /**
     * 将字节数格式化为人类可读的 1024 进制字符串
     *
     * 1 位小数展示并去除尾零（1024 → "1 KB"、1536 → "1.5 KB"）；
     * 0 字节返回 "0 B"；最大值 Long.MAX_VALUE 显示为 "8 EB"。
     *
     * @param bytes 字节数，非负
     * @return 格式化结果，如 "1023 B"、"1.5 KB"、"8 EB"
     * @throws IllegalArgumentException bytes 为负数时抛出
     */
    public static String formatBytes(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("bytes must not be negative: " + bytes);
        }
        if (bytes == 0) {
            return "0 B";
        }
        double value = bytes;
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < BYTE_UNITS.length - 1) {
            value /= 1024;
            unitIndex++;
        }
        String formatted = String.format("%.1f", value);
        if (formatted.endsWith(".0")) {
            formatted = formatted.substring(0, formatted.length() - 2);
        }
        return formatted + " " + BYTE_UNITS[unitIndex];
    }

    /**
     * 摄氏度转华氏度: F = C * 9/5 + 32
     *
     * @param celsius 摄氏温度
     * @return 华氏温度
     */
    public static double celsiusToFahrenheit(double celsius) {
        return celsius * 9.0 / 5.0 + 32.0;
    }

    /**
     * 摄氏度转开尔文: K = C + 273.15
     *
     * @param celsius 摄氏温度
     * @return 开尔文温度
     */
    public static double celsiusToKelvin(double celsius) {
        return celsius + KELVIN_OFFSET;
    }

    /**
     * 华氏度转摄氏度: C = (F - 32) * 5/9
     *
     * @param fahrenheit 华氏温度
     * @return 摄氏温度
     */
    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }

    /**
     * 华氏度转开尔文: K = (F - 32) * 5/9 + 273.15
     *
     * @param fahrenheit 华氏温度
     * @return 开尔文温度
     */
    public static double fahrenheitToKelvin(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0 + KELVIN_OFFSET;
    }

    /**
     * 开尔文转摄氏度: C = K - 273.15
     *
     * @param kelvin 开尔文温度，非负（0K 为绝对零度，合法）
     * @return 摄氏温度
     * @throws IllegalArgumentException kelvin 为负数（低于绝对零度）时抛出
     */
    public static double kelvinToCelsius(double kelvin) {
        checkKelvin(kelvin);
        return kelvin - KELVIN_OFFSET;
    }

    /**
     * 开尔文转华氏度: F = (K - 273.15) * 9/5 + 32
     *
     * @param kelvin 开尔文温度，非负（0K 为绝对零度，合法）
     * @return 华氏温度
     * @throws IllegalArgumentException kelvin 为负数（低于绝对零度）时抛出
     */
    public static double kelvinToFahrenheit(double kelvin) {
        checkKelvin(kelvin);
        return (kelvin - KELVIN_OFFSET) * 9.0 / 5.0 + 32.0;
    }

    private static void checkKelvin(double kelvin) {
        if (kelvin < 0) {
            throw new IllegalArgumentException("kelvin must not be negative: " + kelvin);
        }
    }
}
