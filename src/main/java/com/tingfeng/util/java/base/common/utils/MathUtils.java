package com.tingfeng.util.java.base.common.utils;

import java.math.BigInteger;
import java.util.stream.IntStream;

/**
 * 第一版，通过与十进制之间的互转实现各个任意进制之间的互转
 * 第二版，先实现任意指定进制的大数的计算工具，然后直接实现任意进制的互转
 * @author huitoukest
 **/
public class MathUtils {
    private static final int MAX_RADIX = 256;
    /**
     * 初始化 62 进制数据，索引位置代表字符的数值，比如 A代表10，z代表61等
     */
    private static final char[] chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static int DEFAULT_MAX_RADIX = 62;

    /**
     * 两个指定进制数的相加
     * @param valueA
     * @param valueB
     * @param radixValues  进制采用的基数数组
     */
    public static char[] addPositiveInteger(char[] valueA,char[] valueB,char[] radixValues) {
        int[] valueCharMaps = getValueCharMaps(radixValues);
        return addPositiveInteger(valueA,valueB,radixValues,valueCharMaps);
    }

    /**
     * 两个指定进制数的相加
     * @param valueA
     * @param valueB
     * @param radixValues   进制采用的基数数组,索引映射为char字符
     * @param valueCharMaps 字符到值得映射，使用字符的ascii码的值作为索引，值就是其真实代表的值
     */
    public static char[] addPositiveInteger(char[] valueA,char[] valueB,char[] radixValues,int[] valueCharMaps){
        int radix = radixValues.length;
        char[] minChars = valueA;
        char[] maxChars = valueB;
        /**
         * 保证maxChars更长
         */
        if(valueA.length > valueB.length){
            maxChars = valueA;
            minChars = valueB;
        }
        int[] result = new int[maxChars.length];
        /**
         * 逐个相加
         */
        int tmpIndex = maxChars.length - minChars.length;
        for(int i = 0,j = 0 ; i < maxChars.length  ; i++){
            if(i < tmpIndex){
                result[i] = valueCharMaps[maxChars[i]];
            }else{
                result[i] = valueCharMaps[minChars[j]] + valueCharMaps[maxChars[i]];
                j ++;
            }
        }
        /**
         * 处理进位
         */
        int upper = 0;
        for(int i = maxChars.length - 1; i >= 0 ; i --){
            upper += result[i];
            if(upper >= radix){
                int modValue = upper % radix;
                upper = upper / radix;
                result[i] = modValue;
            }else{
                result[i] = upper;
                upper = 0;
            }
        }
        int resultIndex = result.length + 1;
        char[] resultChars = new char[resultIndex];
        for(int i = 1; i < resultIndex ; i++){
            resultChars[i] = radixValues[result[i - 1]];
        }
        resultChars[0] = radixValues[upper];
        /**
         * 除去首位的0
         */
        int startIndex = IntStream.range(0,resultIndex -1).filter(it -> resultChars[it] != radixValues[0] ).findFirst().orElse(-1);
        if(startIndex < 0){
            return new char[]{};
        }else if(startIndex == 0){
            return resultChars;
        }else{
            char[]  re = new char[resultIndex - startIndex];
            System.arraycopy(resultChars,startIndex,re,0,re.length);
            return re;
        }
    }

    /**
     * @param radixValues
     * @return
     */
    public static int[] getValueCharMaps(char[] radixValues) {
           int[] valueCharMaps = new int[256];
           for(int i = 0 ; i < radixValues.length ; i ++){
               valueCharMaps[radixValues[i]] = i;
           }
           return valueCharMaps;
    }

    /**
     * @param str
     * @param srcRadix
     * @param toRadix
     * @return
     */
    public static String toRadix(String str,int srcRadix,int toRadix) {
        if(toRadix > DEFAULT_MAX_RADIX){
            throw new RuntimeException("default max radix is " + DEFAULT_MAX_RADIX);
        }
        return toRadix(str,chars,srcRadix,toRadix);
    }


    /**
     * 默认十进制转为其余进制
     * @param str
     * @param toRadix
     * @return
     */
    public static String toRadix(String str,int toRadix) {
        return toRadix(str,10,toRadix);
    }

    /**
     * 默认十进制转为其余进制
     * @param str
     * @param radixValues
     * @return
     */
    public static String toRadix(String str,char[] radixValues) {
        return toRadix(str,radixValues,10,radixValues.length);
    }

    /**
     * 十进制的数据转为其它指定进制,指定进制计数的值
     * @param str
     * @param radixValues 目标进制基数
     * @param srcRadix 来源数据的进制数
     * @param toRadix 目标进制
     * @return
     */
    public static String toRadix(String str,char[] radixValues,int srcRadix,int toRadix) {
        toRadix = Math.min(radixValues.length,toRadix);
        if(srcRadix == toRadix){
            return str;
        }
        if(toRadix == 10){
            return toDecimal(str,radixValues,srcRadix).toString();
        }
        if(toRadix > MAX_RADIX){
            throw new RuntimeException("max radix is " + MAX_RADIX);
        }
        BigInteger number = null;
        if(srcRadix != 10){
            number = toDecimal(str,radixValues,srcRadix);
        }else {
            number = new BigInteger(str);
        }
        StringBuilder sb = new StringBuilder(32);
        BigInteger radixInt = new BigInteger(String.valueOf(toRadix));
        /**
         * 余数
         */
        int remainder = 0;
        while (number.compareTo(BigInteger.ZERO) > 0) {
            /**
             * 用求余数的方法来实现
             */
            remainder = number.mod(radixInt).intValue();
            sb.append(radixValues[remainder]);
            number = number.divide(radixInt);
        }
        return sb.reverse().toString();

    }

    /**
     *
     * @param  str  数字的内容,其进制值等同于baseChars的长度
     * @return 默认解码为10进制数据
     */
    public static BigInteger toDecimal(String str,char[] radixValues) {
        return toDecimal(str,radixValues,radixValues.length);
    }

    /**
     *
     * @param  str  数字的内容,其进制值等同于baseChars的长度
     * @return 默认解码为10进制数据
     */
    public static BigInteger toDecimal(String str,char[] radixValues,int srcRadix) {
        int[] valueCharMaps = getValueCharMaps(radixValues);
        int radix = Math.min(radixValues.length,srcRadix);
        /**
         * 将 0 开头的字符串进行替换
         */
        str = str.replace("^0*", "");

        if(radix > MAX_RADIX){
            throw new RuntimeException("max radix is " + MAX_RADIX);
        }
        BigInteger result = new BigInteger("0");
        BigInteger baseValue = new BigInteger("1");
        BigInteger radixInt = new BigInteger(String.valueOf(radix));
        for (int i = str.length() - 1; i >= 0 ; i--) {
            int positionValue = valueCharMaps[str.charAt(i)];
            result = result.add(baseValue.multiply(new BigInteger(String.valueOf(positionValue))));
            baseValue = baseValue.multiply(radixInt);
        }
        return result;
    }

    /**
     * 最大公约数
     * @param a
     * @param b
     * @return
     */
    public static int gcd(int a, int b){
        int r;
        while(b > 0){
            r = a % b;
            a = b;
            b = r;
        }
        return a;
    }

    /**
     * 求最小公倍数
     * @param m
     * @param n
     * @return
     */
    public static int lcm(int m, int n) {
        int gcdValue = gcd(m , n);
        return m / gcdValue * n;
    }

    /**
     * 最大公约数
     * @param a
     * @param b
     * @return
     */
    public static long gcd(long a, long b){
        long r;
        while(b > 0){
            r = a % b;
            a = b;
            b = r;
        }
        return a;
    }

    /**
     * 求最小公倍数
     * @param m
     * @param n
     * @return
     */
    public static long lcm(long m, long n) {
        long gcdValue = gcd(m , n);
        return m / gcdValue * n;
    }
}
