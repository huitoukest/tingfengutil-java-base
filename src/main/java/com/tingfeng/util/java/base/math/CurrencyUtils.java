package com.tingfeng.util.java.base.math;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * 货币处理工具类
 * <p>
 * 提供人民币大写转换、货币格式化等功能
 * </p>
 * @author huitoukest
 * @since 0.2.6
 */
public class CurrencyUtils {

    // ========== 常量定义 ==========
    
    /**
     * 可选的前缀，默认值为空字符串
     *
     * 已废弃：全局可变状态存在并发修改导致输出漂移的风险，
     * 建议使用 {@link #toChinaUpper(String, String, String)} 显式传入前缀
     *
     * @deprecated 使用 {@link #toChinaUpper(String, String, String)} 替代
     */
    @Deprecated
    public static String PREFIX = "";
    
    /**
     * 人民币前缀
     */
    public static final String RMB_PREFIX = "人民币";
    
    /**
     * 人民币单位：圆（人民币和增值税上使用）
     */
    public static final String RMB_YUAN_1 = "圆";
    
    /**
     * 人民币单位：元（法定单位,银行柜台业务等常用）
     */
    public static final String RMB_YUAN_2 = "元";
    
    /**
     * 当前使用的人民币单位，默认使用元
     *
     * 已废弃：全局可变状态存在并发修改导致输出漂移的风险，
     * 建议使用 {@link #toChinaUpper(String, String, String)} 显式传入元单位
     *
     * @deprecated 使用 {@link #toChinaUpper(String, String, String)} 替代
     */
    @Deprecated
    public static String RMB_YUAN = RMB_YUAN_2;

    /**
     * 转换为中国人民币大写字符串,精确到分
     *
     * 支持整数、小数形式的金额字符串，例如：
     * - 整数："123" → "壹佰贰拾叁元整"
     * - 小数："123.45" → "壹佰贰拾叁元肆角伍分"
     * - 负数："-123" → "负壹佰贰拾叁元整"
     *
     * @param money 传入小写数字字符串
     * @return 人民币大写字符串
     * @throws Exception 如果钱数格式错误
     */
    public static String toChinaUpper(String money) throws Exception {
        return RMBUpperOps.toChinaUpper(money);
    }

    /**
     * 转换为中国人民币大写字符串,精确到分，支持自定义前缀和元单位
     *
     * 支持整数、小数形式的金额字符串，例如：
     * - 整数："123" → "壹佰贰拾叁元整"
     * - 小数："123.45" → "壹佰贰拾叁元肆角伍分"
     * - 负数："-123" → "负壹佰贰拾叁元整"
     *
     * @param money 传入小写数字字符串
     * @param prefix 自定义前缀
     * @param yuanUnit 自定义元单位
     * @return 人民币大写字符串
     * @throws Exception 如果钱数格式错误
     */
    public static String toChinaUpper(String money, String prefix, String yuanUnit) throws Exception {
        return RMBUpperOps.toChinaUpper(money, prefix, yuanUnit);
    }

        /**
         * 货币转字符串
         * <p>
         * 支持自定义格式和默认格式
         * </p>
         * @param money 金额
         * @param style 样式，支持：
         *              <ul>
         *                <li>"default" - 默认样式，智能格式化（去尾零、整数不输出小数点）</li>
         *                <li>自定义格式，如"#.00"、"#,###.00"等</li>
         *              </ul>
         * @return 格式化后的字符串
         */
        public static String formatMoneyString(double money, String style) {
            if (style == null || style.isEmpty()) {
                return String.valueOf(money);
            }

            if ("default".equalsIgnoreCase(style)) {
                return formatDefault(money);
            }

            try {
                return new DecimalFormat(style).format(money);
            } catch (IllegalArgumentException e) {
                // 格式异常时降级返回原始值
                return String.valueOf(money);
            }
        }

        /**
         * default样式：智能格式化（去尾零、整数不输出小数点）
         * @param money 金额
         * @return 格式化后的字符串
         */
        private static String formatDefault(double money) {
            if (money == 0.0) return "";

            // 判断是否为整数
            if (money == Math.floor(money) && !Double.isInfinite(money)) {
                // 大整数（如 1e19）经 BigDecimal 精确输出，避免 (long) 强转饱和为 Long.MAX_VALUE
                return BigDecimal.valueOf(money).toBigInteger().toString();
            }

            // 有小数：转字符串后移除末尾无意义的0
            return String.valueOf(money).replaceAll("0+$", "").replaceAll("\\.$", "");
        }

        /**
         * 将分转换为元字符串，含千分位与两位小数
         *
         * 示例：
         * - 123456 分 → "1,234.56"
         * - -123456 分 → "-1,234.56"
         * - 0 分 → "0.00"
         *
         * 千分位分隔符与小数点符号固定使用 Locale.US，
         * 避免默认 Locale（如 de_DE 千分位/小数点互换）下符号漂移
         *
         * @param fen 金额（单位：分）
         * @return 格式化后的元字符串
         */
        public static String fenToYuanString(long fen) {
            BigDecimal yuan = BigDecimal.valueOf(fen, 2);
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
            decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
            return decimalFormat.format(yuan);
        }

    /**
     * 计算等额本息月供金额
     *
     * 标准等额本息公式：pmt = P * r * (1+r)^n / ((1+r)^n - 1)
     * - P 为本金，r 为月利率（年利率/12），n 为期数
     * - 零利率特判：r == 0 时直接返回 principal / months，避免 (1+r)^n - 1 除零
     * - 月利率精度 scale = 8（HALF_UP）；结果金额精度 scale = 2（HALF_UP，金额分惯例，与 fenToYuanString 对齐）
     *
     * @param annualRate 年利率（BigDecimal，如 0.049 表示 4.9%）
     * @param months 期数（月）
     * @param principal 本金
     * @return 等额本息月供金额（保留 2 位小数）
     * @throws IllegalArgumentException 任一参数为 null、年利率为负、期数 ≤ 0、本金为负
     */
    public static BigDecimal pmt(BigDecimal annualRate, int months, BigDecimal principal) {
        if (annualRate == null || principal == null) {
            throw new IllegalArgumentException("annualRate 与 principal 不能为 null");
        }
        if (annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("年利率不能为负数");
        }
        if (months <= 0) {
            throw new IllegalArgumentException("期数必须大于 0");
        }
        if (principal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("本金不能为负数");
        }
        BigDecimal monthRate = annualRate.divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP);
        if (monthRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }
        BigDecimal one = BigDecimal.ONE;
        BigDecimal powN = one.add(monthRate).pow(months);
        return principal.multiply(monthRate).multiply(powN)
                .divide(powN.subtract(one), 2, RoundingMode.HALF_UP);
    }

    /**
     * 计算涨跌幅百分比
     *
     * 返回值语义：12.5 表示 12.5%
     * - oldValue 为 0 时：newValue == 0 → 0.0；newValue > 0 → +∞；newValue < 0 → -∞（从 0 涨/跌语义显式）
     * - 其余情况：(newValue - oldValue) / oldValue * 100
     *
     * @param oldValue 原值
     * @param newValue 新值
     * @return 涨跌幅百分数值（12.5 = 12.5%）
     */
    public static double percentChange(double oldValue, double newValue) {
        if (oldValue == 0.0) {
            if (newValue == 0.0) {
                return 0.0;
            }
            return newValue > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }
        return (newValue - oldValue) / oldValue * 100;
    }
}