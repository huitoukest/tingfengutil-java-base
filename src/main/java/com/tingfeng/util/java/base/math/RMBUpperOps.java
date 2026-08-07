package com.tingfeng.util.java.base.math;

import java.util.regex.Pattern;

/**
 * 人民币大写转换实现。
 *
 * 包级私有，不对外暴露。通过 {@link CurrencyUtils} 对外提供统一 API。
 * 所有方法参数化（prefix/yuanUnit 由调用方显式传入），不读取全局可变状态，
 * 避免 {@link CurrencyUtils#PREFIX}/{@link CurrencyUtils#RMB_YUAN} 被并发修改导致输出漂移。
 */
class RMBUpperOps {

    /**
     * 数字大写映射
     */
    private static final char[] DIGITS = {'零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'};

    /**
     * 节单位映射：每 4 位为一节，共 5 节（个/万/亿/万亿/亿亿），
     * 覆盖 long 分全范围（元整数部分最多 17 位 = 1 个亿亿节 + 1 个万亿节 + 1 个亿节）
     */
    private static final String[] SECTION_UNITS = {"", "万", "亿", "万亿", "亿亿"};

    /**
     * 节内位单位：索引为位数（0 个位 / 1 拾位 / 2 佰位 / 3 仟位）
     */
    private static final String[] SECTION_POS_UNITS = {"", "拾", "佰", "仟"};

    /**
     * 金额整数部分的结束标识
     */
    private static final String FINISH = "整";

    /**
     * 金额格式正则表达式，支持整数或最多两位小数
     */
    private static final Pattern MONEY_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    /**
     * 默认前缀
     */
    private static final String DEFAULT_PREFIX = "";

    /**
     * 默认人民币单位（法定单位，银行柜台业务等常用）
     */
    private static final String DEFAULT_YUAN_UNIT = "元";

    private RMBUpperOps() {
    }

    /**
     * 转换为中国人民币大写字符串，精确到分，使用默认前缀与默认元单位
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
    static String toChinaUpper(String money) throws Exception {
        return toChinaUpper(money, DEFAULT_PREFIX, DEFAULT_YUAN_UNIT);
    }

    /**
     * 转换为中国人民币大写字符串，精确到分，支持自定义前缀和元单位
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
    static String toChinaUpper(String money, String prefix, String yuanUnit) throws Exception {
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
     *
     * 纯字符串计算，无精度损失，支持整数和最多两位小数的金额格式；
     * 超过 long 分可表示范围（元部分乘 100 溢出或数字串超长）时抛出明确异常
     *
     * @param money 金额字符串
     * @return 以"分"为单位的long值
     * @throws IllegalArgumentException 金额超出 long 分支持范围
     */
    private static long toTotalFen(String money) {
        int dot = money.indexOf('.');
        try {
            if (dot == -1) {
                return Math.multiplyExact(Long.parseLong(money), 100L);
            }
            long yuan = Long.parseLong(money.substring(0, dot));
            String fenPart = money.substring(dot + 1);
            // 补齐或截断到2位
            fenPart = fenPart.length() == 1 ? fenPart + "0" :
                    fenPart.length() > 2 ? fenPart.substring(0, 2) : fenPart;
            return Math.addExact(Math.multiplyExact(yuan, 100L), Integer.parseInt(fenPart));
        } catch (NumberFormatException e) {
            // 超长数字串 parseLong 溢出
            throw new IllegalArgumentException("金额超出支持范围", e);
        } catch (ArithmeticException e) {
            // 元部分乘 100 或加角分溢出 long 分范围
            throw new IllegalArgumentException("金额超出支持范围", e);
        }
    }

    /**
     * 转换整数部分（元）为大写形式，支持自定义元单位
     *
     * 算法：每 4 位一节（个/万/亿/万亿/亿亿，共 5 节），由高节向低节循环：
     * - 节内：拾佰仟位转换，零延迟补位
     * - 节间：当前节与上一输出节之间存在全零节，或当前节内有前导零时补"零"
     * - 节单位按节序号从 SECTION_UNITS 取值，不再受固定长度单位表限制
     *
     * @param yuan 元部分的数值
     * @param yuanUnit 自定义元单位
     * @return 大写形式的元部分，包含单位
     */
    private static String convertYuan(long yuan, String yuanUnit) {
        if (yuan == 0) {
            return "零" + yuanUnit;
        }

        String s = String.valueOf(yuan);
        int n = s.length();
        // 每 4 位一节，节数上限 5（亿亿节）覆盖 long 分全范围
        int sectionCount = (n + 3) / 4;
        StringBuilder sb = new StringBuilder();
        boolean needZero = false;  // 标记：存在全零节，等待后续非零节补"零"

        for (int sec = sectionCount - 1; sec >= 0; sec--) {
            int start = Math.max(0, n - (sec + 1) * 4);
            int end = n - sec * 4;
            int sectionValue = Integer.parseInt(s.substring(start, end));

            if (sectionValue == 0) {
                // 全零节：跳过并标记，若更低节有值则需补"零"
                needZero = true;
                continue;
            }

            // 节间补零：中间隔了全零节（needZero），或当前节有前导零（值小于1000）
            if ((needZero || sectionValue < 1000) && sb.length() > 0) {
                sb.append('零');
            }
            needZero = false;
            sb.append(convertSection(sectionValue)).append(SECTION_UNITS[sec]);
        }

        sb.append(yuanUnit);
        return sb.toString();
    }

    /**
     * 转换一节（4 位）为大写形式，含节内零补位
     *
     * 定宽 4 位处理：节内前导零不补，仅已输出数字与后续数字之间的零补"零"
     *
     * @param sectionValue 节值，0-9999
     * @return 节的大写形式，不含节单位
     */
    private static String convertSection(int sectionValue) {
        if (sectionValue == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean needZero = false;
        for (int i = 3; i >= 0; i--) {
            int digit = (sectionValue / pow10(i)) % 10;
            if (digit == 0) {
                needZero = true;
            } else {
                if (needZero && sb.length() > 0) {
                    sb.append('零');
                }
                sb.append(DIGITS[digit]).append(SECTION_POS_UNITS[i]);
                needZero = false;
            }
        }
        return sb.toString();
    }

    /**
     * 计算 10 的 n 次方
     *
     * @param n 指数，0-3
     * @return 10 的 n 次方
     */
    private static int pow10(int n) {
        int result = 1;
        for (int i = 0; i < n; i++) {
            result *= 10;
        }
        return result;
    }

    /**
     * 转换小数部分（角分）为大写形式
     *
     * @param decimal 0-99，表示角分
     * @return 大写形式的小数部分，可能的值：
     *         - "整" - 角分均为零
     *         - "X角" - 只有角位非零
     *         - "X角X分" - 角分均非零
     *         - "零X分" - 只有分位非零
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
}
