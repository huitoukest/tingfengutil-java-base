package com.tingfeng.util.java.base.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * BigDecimal 安全除法工具类
 *
 * 提供除零时返回 fallback 的除法运算，避免调用方处理 ArithmeticException；
 * 除不尽时按指定舍入模式（默认 HALF_UP）处理。
 *
 * 与 bean/converter 域 BigNumberConverters 的边界：本类只做数值运算，不涉及类型转换器注册。
 *
 * @see com.tingfeng.util.java.base.bean.converter.defaults.BigNumberConverters
 */
public final class BigDecimalUtils {

    private BigDecimalUtils() {}

    /**
     * 安全除法：除零时返回 fallback，除不尽时按 HALF_UP 舍入到指定小数位
     *
     * 等价于调用 5 参版本并固定 RoundingMode.HALF_UP。
     *
     * @param dividend 被除数，不能为 null
     * @param divisor 除数，不能为 null；为 0 时返回 fallback
     * @param scale 结果保留的小数位数，必须大于等于 0
     * @param fallback 除数为 0 时返回的兜底值，不能为 null
     * @return 除法结果；除数为 0 时返回 fallback
     * @throws IllegalArgumentException dividend/divisor/fallback 为 null 或 scale 小于 0 时抛出
     */
    public static BigDecimal safeDivide(BigDecimal dividend, BigDecimal divisor, int scale, BigDecimal fallback) {
        return safeDivide(dividend, divisor, scale, RoundingMode.HALF_UP, fallback);
    }

    /**
     * 安全除法：除零时返回 fallback，除尽/除不尽均按指定舍入模式处理
     *
     * 与 4 参版本的区别：舍入模式由调用方显式指定，适用于 FLOOR/CEILING/DOWN 等特殊场景。
     *
     * @param dividend 被除数，不能为 null
     * @param divisor 除数，不能为 null；为 0 时返回 fallback
     * @param scale 结果保留的小数位数，必须大于等于 0
     * @param mode 舍入模式，不能为 null
     * @param fallback 除数为 0 时返回的兜底值，不能为 null
     * @return 除法结果；除数为 0 时返回 fallback
     * @throws IllegalArgumentException dividend/divisor/mode/fallback 为 null 或 scale 小于 0 时抛出
     */
    public static BigDecimal safeDivide(BigDecimal dividend, BigDecimal divisor, int scale, RoundingMode mode, BigDecimal fallback) {
        if (dividend == null || divisor == null || mode == null || fallback == null) {
            throw new IllegalArgumentException("dividend, divisor, mode and fallback must not be null");
        }
        if (scale < 0) {
            throw new IllegalArgumentException("scale must be >= 0: " + scale);
        }
        if (divisor.signum() == 0) {
            return fallback;
        }
        return dividend.divide(divisor, scale, mode);
    }
}
