package com.tingfeng.util.java.base.math;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * 任意进制转换操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link MathUtils} 对外提供统一 API。
 */
class RadixOps {
    /** 最大支持进制数 */
    private static final int MAX_RADIX = 256;
    /** 默认最大进制数（62进制） */
    private static final int DEFAULT_MAX_RADIX = 62;
    /** 十进制基数常量 */
    private static final int RADIX_TEN = 10;
    /** 初始化62进制数据，索引位置代表字符的数值，比如A代表10，z代表61等 */
    private static final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    /** StringBuilder初始容量 */
    private static final int INITIAL_STRING_BUILDER_CAPACITY = 32;
    /** ASCII映射数组大小（0-255） */
    private static final int ASCII_MAP_SIZE = 256;

    private RadixOps() {
    }

    /**
     * 两个指定进制数的相加（自动计算字符映射）
     * @param valueA 第一个数值的字符数组
     * @param valueB 第二个数值的字符数组
     * @param radixValues 进制采用的基数数组
     */
    static char[] addPositiveInteger(char[] valueA, char[] valueB, char[] radixValues) {
        return addPositiveInteger(valueA, valueB, radixValues, getValueCharMaps(radixValues));
    }

    /**
     * 两个指定进制数的相加（使用预计算的字符映射）
     * @param valueA 第一个数值的字符数组
     * @param valueB 第二个数值的字符数组
     * @param radixValues 进制采用的基数数组,索引映射为char字符
     * @param valueCharMaps 字符到值得映射，使用字符的ascii码的值作为索引，值就是其真实代表的值
     */
    static char[] addPositiveInteger(char[] valueA, char[] valueB, char[] radixValues, int[] valueCharMaps) {
        int radix = radixValues.length;
        char[] minChars = valueA;
        char[] maxChars = valueB;
        // 保证maxChars更长
        if (valueA.length > valueB.length) {
            maxChars = valueA;
            minChars = valueB;
        }
        int[] result = new int[maxChars.length];
        // 逐个相加
        int tmpIndex = maxChars.length - minChars.length;
        for (int i = 0, j = 0; i < maxChars.length; i++) {
            if (i < tmpIndex) {
                result[i] = toMappedValue(maxChars[i], valueCharMaps);
            } else {
                result[i] = toMappedValue(minChars[j], valueCharMaps) + toMappedValue(maxChars[i], valueCharMaps);
                j++;
            }
        }
        // 处理进位
        int upper = 0;
        for (int i = maxChars.length - 1; i >= 0; i--) {
            upper += result[i];
            if (upper >= radix) {
                int modValue = upper % radix;
                upper = upper / radix;
                result[i] = modValue;
            } else {
                result[i] = upper;
                upper = 0;
            }
        }
        int resultIndex = result.length + 1;
        char[] resultChars = new char[resultIndex];
        for (int i = 1; i < resultIndex; i++) {
            resultChars[i] = radixValues[result[i - 1]];
        }
        resultChars[0] = radixValues[upper];
        // 除去首位的0（扫描须含最高结果位，否则单位数不进位加法会误判为全0返回空数组）
        int startIndex = IntStream.range(0, resultIndex).filter(it -> resultChars[it] != radixValues[0]).findFirst().orElse(-1);
        if (startIndex < 0) {
            return new char[]{};
        } else if (startIndex == 0) {
            return resultChars;
        } else {
            char[] re = new char[resultIndex - startIndex];
            System.arraycopy(resultChars, startIndex, re, 0, re.length);
            return re;
        }
    }

    /**
     * 根据进制基数数组生成字符到数值的映射表
     * @param radixValues 进制基数数组，索引为字符位置，值为对应数值
     * @return 字符到数值的映射数组，使用ASCII码作为索引
     */
    static int[] getValueCharMaps(char[] radixValues) {
        int[] valueCharMaps = new int[ASCII_MAP_SIZE];
        // 未映射位置填 -1，区分"未映射"与"值为0"，避免非法字符静默按0处理
        Arrays.fill(valueCharMaps, -1);
        for (int i = 0; i < radixValues.length; i++) {
            if (radixValues[i] >= ASCII_MAP_SIZE) {
                throw new IllegalArgumentException("不支持非 ASCII 字符: " + radixValues[i]);
            }
            valueCharMaps[radixValues[i]] = i;
        }
        return valueCharMaps;
    }

    /**
     * 将字符按映射表转换为数值，字符未映射或超出 ASCII 范围时抛异常
     * @param c 待转换字符
     * @param valueCharMaps 字符到数值的映射表
     * @return 字符对应的数值
     */
    private static int toMappedValue(char c, int[] valueCharMaps) {
        int value = c < ASCII_MAP_SIZE ? valueCharMaps[c] : -1;
        if (value < 0) {
            throw new IllegalArgumentException("包含非法字符: " + c);
        }
        return value;
    }

    /**
     * 任意进制字符串转换为另一种进制字符串（使用默认62进制字符集）
     * @param str 待转换的字符串
     * @param srcRadix 源进制
     * @param toRadix 目标进制
     * @return 转换后的字符串
     */
    static String toRadix(String str, int srcRadix, int toRadix) {
        if (toRadix > DEFAULT_MAX_RADIX) {
            throw new RuntimeException("default max radix is " + DEFAULT_MAX_RADIX);
        }
        return toRadix(str, CHARS, srcRadix, toRadix);
    }

    /**
     * 十进制字符串转换为指定进制字符串（使用默认62进制字符集）
     * @param str 十进制字符串
     * @param toRadix 目标进制
     * @return 转换后的字符串
     */
    static String toRadix(String str, int toRadix) {
        return toRadix(str, RADIX_TEN, toRadix);
    }

    /**
     * 十进制字符串转换为指定进制字符串
     * @param str 十进制字符串
     * @param radixValues 目标进制字符集
     * @return 转换后的字符串
     */
    static String toRadix(String str, char[] radixValues) {
        return toRadix(str, radixValues, RADIX_TEN, radixValues.length);
    }

    /**
     * 任意进制数据转换为另一种进制
     * @param str 待转换的字符串
     * @param radixValues 目标进制字符集
     * @param srcRadix 源进制
     * @param toRadix 目标进制
     * @return 转换后的字符串
     */
    static String toRadix(String str, char[] radixValues, int srcRadix, int toRadix) {
        toRadix = Math.min(radixValues.length, toRadix);
        // radix<2 时除留余数法死循环（number.divide(1) 永不减小），min 裁剪后、相等检查前统一拦截
        if (toRadix < 2) {
            throw new IllegalArgumentException("radix must be >= 2: " + toRadix);
        }
        if (srcRadix == toRadix) {
            return str;
        }
        if (toRadix == RADIX_TEN) {
            return toDecimal(str, radixValues, srcRadix).toString();
        }
        if (toRadix > MAX_RADIX) {
            throw new RuntimeException("max radix is " + MAX_RADIX);
        }
        BigInteger number;
        if (srcRadix != RADIX_TEN) {
            number = toDecimal(str, radixValues, srcRadix);
        } else {
            number = new BigInteger(str);
        }
        StringBuilder sb = new StringBuilder(INITIAL_STRING_BUILDER_CAPACITY);
        BigInteger radixInt = BigInteger.valueOf(toRadix);
        // 使用除留余数法进行进制转换
        while (number.compareTo(BigInteger.ZERO) > 0) {
            int remainder = number.mod(radixInt).intValue();
            sb.append(radixValues[remainder]);
            number = number.divide(radixInt);
        }
        return sb.reverse().toString();
    }

    /**
     * 将任意进制字符串解码为十进制BigInteger（使用指定字符集）
     * @param str 数字字符串，其进制由radixValues长度决定
     * @param radixValues 进制字符集
     * @return 十进制BigInteger
     */
    static BigInteger toDecimal(String str, char[] radixValues) {
        return toDecimal(str, radixValues, radixValues.length);
    }

    /**
     * 将任意进制字符串解码为十进制BigInteger
     * @param str 数字字符串，其进制值等同于baseChars的长度
     * @param radixValues 进制字符集
     * @param srcRadix 源进制
     * @return 十进制BigInteger
     */
    static BigInteger toDecimal(String str, char[] radixValues, int srcRadix) {
        int[] valueCharMaps = getValueCharMaps(radixValues);
        int radix = Math.min(radixValues.length, srcRadix);

        if (radix > MAX_RADIX) {
            throw new RuntimeException("max radix is " + MAX_RADIX);
        }
        // 去掉前导0，手动遍历找到第一个非0字符的位置
        int startIndex = 0;
        while (startIndex < str.length() - 1 && str.charAt(startIndex) == '0') {
            startIndex++;
        }
        String trimmed = str.substring(startIndex);

        BigInteger result = BigInteger.ZERO;
        BigInteger baseValue = BigInteger.ONE;
        BigInteger radixInt = BigInteger.valueOf(radix);
        for (int i = trimmed.length() - 1; i >= 0; i--) {
            int positionValue = toMappedValue(trimmed.charAt(i), valueCharMaps);
            result = result.add(baseValue.multiply(BigInteger.valueOf(positionValue)));
            baseValue = baseValue.multiply(radixInt);
        }
        return result;
    }
}
