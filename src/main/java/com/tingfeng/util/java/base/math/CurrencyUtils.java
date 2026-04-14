package com.tingfeng.util.java.base.math;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

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
     * 数字大写映射
     */
    private static final char[] DIGITS = {'零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'};
    
    /**
     * 单位映射
     */
    private static final String[] UNITS = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟"};
    
    /**
     * 可选的前缀，默认值为空字符串
     */
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
     */
    public static String RMB_YUAN = RMB_YUAN_2;
    
    /**
     * 金额整数部分的结束标识
     */
    private static final String FINISH = "整";
    
    /**
     * 金额格式正则表达式，支持整数或最多两位小数
     */
    private static final Pattern MONEY_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

        /**
         * 转换为中国人民币大写字符串,精确到分
         * <p>
         * 支持整数、小数形式的金额字符串，例如：
         * <ul>
         *   <li>整数："123" → "壹佰贰拾叁元整"</li>
         *   <li>小数："123.45" → "壹佰贰拾叁元肆角伍分"</li>
         *   <li>负数："-123" → "负壹佰贰拾叁元整"</li>
         * </ul>
         * </p>
         * @param money 传入小写数字字符串
         * @return 人民币大写字符串
         * @throws Exception 如果钱数格式错误
         */
        public static String toChinaUpper(String money) throws Exception {
            return toChinaUpper(money, PREFIX, RMB_YUAN);
        }
        
        /**
         * 转换为中国人民币大写字符串,精确到分，支持自定义前缀和元单位
         * <p>
         * 支持整数、小数形式的金额字符串，例如：
         * <ul>
         *   <li>整数："123" → "壹佰贰拾叁元整"</li>
         *   <li>小数："123.45" → "壹佰贰拾叁元肆角伍分"</li>
         *   <li>负数："-123" → "负壹佰贰拾叁元整"</li>
         * </ul>
         * </p>
         * @param money 传入小写数字字符串
         * @param prefix 自定义前缀
         * @param yuanUnit 自定义元单位
         * @return 人民币大写字符串
         * @throws Exception 如果钱数格式错误
         */
        public static String toChinaUpper(String money, String prefix, String yuanUnit) throws Exception {
            // 1. 参数校验
            if (money == null || money.trim().isEmpty()) {
                throw new Exception("钱数不能为空！");
            }

            // 2. 处理符号
            boolean negative = money.startsWith("-");
            String content = negative ? money.substring(1).trim() : money.trim();

            if (!MONEY_PATTERN.matcher(content).matches()) {
                throw new Exception("钱数格式错误！");
            }

            // 3. 转为"分"为单位的long，避免精度问题
            long totalFen = toTotalFen(content);

            // 4. 零元特判
            if (totalFen == 0) {
                return prefix + "零" + yuanUnit + FINISH;
            }

            // 5. 分离元/角分
            long yuan = totalFen / 100;
            int decimal = (int) (totalFen % 100);

            // 6. 分别转换
            String yuanStr = convertYuan(yuan, yuanUnit);
            String decimalStr = convertDecimal(decimal);

            // 7. 组合结果
            String result;
            // 根据人民币大写规范处理特殊情况
            if (yuan == 0 && decimal == 0) {
                // 整数和小数部分都为0
                result = prefix + "零" + yuanUnit + FINISH;
            } else if (yuan == 0 && decimal != 0) {
                // 整数部分为0，小数部分不为0
                // 检查小数部分是否以0开头（即角位为0）
                int jiao = decimal / 10; // 获取角位
                if (jiao == 0) {
                    // 角位为0，如0.02，需要显示"零圆零X分"
                    String decimalPart = convertDecimal(decimal);
                    // 如果小数部分返回的是"零X分"的形式，则直接拼接
                    result = prefix + "零" + yuanUnit + decimalPart;
                } else {
                    // 角位不为0，如0.43，只显示小数部分
                    result = prefix + convertDecimal(decimal);
                }
            } else {
                // 正常情况
                result = prefix + yuanStr + decimalStr;
            }

            return negative ? "负" + result : result;
        }

        /**
         * 将金额字符串转换为以"分"为单位的long值
         * <p>
         * 纯字符串计算，无精度损失，支持整数和最多两位小数的金额格式
         * </p>
         * @param money 金额字符串
         * @return 以"分"为单位的long值
         */
        private static long toTotalFen(String money) {
            int dot = money.indexOf('.');
            if (dot == -1) {
                return Long.parseLong(money) * 100L;
            }
            long yuan = Long.parseLong(money.substring(0, dot));
            String fenPart = money.substring(dot + 1);
            // 补齐或截断到2位
            fenPart = fenPart.length() == 1 ? fenPart + "0" :
                    fenPart.length() > 2 ? fenPart.substring(0, 2) : fenPart;
            return yuan * 100L + Integer.parseInt(fenPart);
        }

        /**
         * 转换整数部分（元）为大写形式
         * <p>
         * 算法：高位→低位遍历，状态机处理零的补位，特殊处理万/亿单位
         * </p>
         * @param yuan 元部分的数值
         * @return 大写形式的元部分，包含单位
         */
    /**
     * 转换整数部分（元）为大写形式
     * <p>
     * 算法：高位→低位遍历，状态机处理零的补位，特殊处理万/亿单位
     * </p>
     * @param yuan 元部分的数值
     * @return 大写形式的元部分，包含单位
     */
    private static String convertYuan(long yuan) {
        return convertYuan(yuan, RMB_YUAN);
    }
    
    /**
     * 转换整数部分（元）为大写形式，支持自定义元单位
     * <p>
     * 算法：高位→低位遍历，状态机处理零的补位，特殊处理万/亿单位
     * </p>
     * @param yuan 元部分的数值
     * @param yuanUnit 自定义元单位
     * @return 大写形式的元部分，包含单位
     */
    private static String convertYuan(long yuan, String yuanUnit) {
        if (yuan == 0) return "零" + yuanUnit;

        String s = String.valueOf(yuan);
        int n = s.length();
        StringBuilder sb = new StringBuilder();
        boolean needZero = false;  // 标记：遇到零后等待非零时补"零"

        for (int i = 0; i < n; i++) {
            int digit = s.charAt(i) - '0';
            int pos = n - 1 - i;  // 从右往左的位置索引
            String unit = (pos < UNITS.length) ? UNITS[pos] : "";

            if (digit == 0) {
                needZero = true;  // 标记零，延迟处理
            } else {
                if (needZero) {
                    sb.append('零');
                    needZero = false;
                }
                sb.append(DIGITS[digit]).append(unit);
            }

            // 万位特殊处理：当亿位存在且万位全为零时不添加万单位
            if (pos == 4 && needZero) {
                // 检查是否有亿位（总长度大于8位）
                if (n > 8) {
                    // 检查万位部分是否全为零
                    boolean isAllZero = true;
                    for (int j = Math.max(0, n - 8); j < Math.min(n, n - 4); j++) {
                        if (s.charAt(j) != '0') {
                            isAllZero = false;
                            break;
                        }
                    }
                    // 如果亿位之后的万位部分全为零，则不添加万单位
                    if (!isAllZero && hasValueInLowerSection(s, i + 1)) {
                        sb.append(unit);
                        needZero = false;
                    }
                } else {
                    // 没有亿位时，正常处理万位
                    if (hasValueInLowerSection(s, i + 1)) {
                        sb.append(unit);
                        needZero = false;
                    }
                }
            } else if (pos == 8 && needZero) {
                // 亿位特殊：即使该位是0，若低位节有值则需添加单位
                if (hasValueInLowerSection(s, i + 1)) {
                    sb.append(unit);
                    needZero = false;
                }
            }
        }

        // 清理末尾冗余零，添加"元"
        trimTrailingZero(sb);
        sb.append(yuanUnit);
        return sb.toString();
    }


        /**
         * 检查从指定位置开始，低位节（4位内）是否有非零数字
         * @param s 数字字符串
         * @param start 起始位置
         * @return 是否有非零数字
         */
        private static boolean hasValueInLowerSection(String s, int start) {
            int end = Math.min(s.length(), start + 4);
            for (int i = start; i < end; i++) {
                if (s.charAt(i) != '0') return true;
            }
            return false;
        }

        /**
         * 移除StringBuilder末尾的'零'字符
         * @param sb 字符串构建器
         */
        private static void trimTrailingZero(StringBuilder sb) {
            while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '零') {
                sb.setLength(sb.length() - 1);
            }
        }

        /**
         * 转换小数部分（角分）为大写形式
         * @param decimal 0-99，表示角分
         * @return 大写形式的小数部分，可能的值：
         *         <ul>
         *           <li>"整" - 角分均为零</li>
         *           <li>"X角" - 只有角位非零</li>
         *           <li>"X角X分" - 角分均非零</li>
         *           <li>"零X分" - 只有分位非零</li>
         *         </ul>
         */
        private static String convertDecimal(int decimal) {
            if (decimal == 0) return FINISH;

            int jiao = decimal / 10;
            int fen = decimal % 10;

            if (jiao != 0) {
                // 角位非零：直接拼接
                StringBuilder sb = new StringBuilder().append(DIGITS[jiao]).append("角");
                if (fen != 0) sb.append(DIGITS[fen]).append("分");
                return sb.toString();
            } else {
                // 角零分非零：返回"零X分"，由主方法在"元"后拼接
                return "零" + DIGITS[fen] + "分";
            }
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
                return String.valueOf((long) money);
            }

            // 有小数：转字符串后移除末尾无意义的0
            return String.valueOf(money).replaceAll("0+$", "").replaceAll("\\.$", "");
        }
}