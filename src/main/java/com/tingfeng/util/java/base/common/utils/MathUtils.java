package com.tingfeng.util.java.base.common.utils;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Author wangGang
 * @Description //TODO
 * @Date 2019-05-08 17:09
 **/
public class MathUtils {
    private static final int MAX_RADIX = 256;
    /**
     * 其余进制转为十进制的时候，进位的临界值
     */
    private static final int CARRY_VALUE = MAX_RADIX * MAX_RADIX * MAX_RADIX;
    /**
     * 初始化 62 进制数据，索引位置代表字符的数值，比如 A代表10，z代表61等
     */
    private static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static int scale = 62;

    /**
     * 将数字转为62进制
     *
     * @param num    Long 型数字
     * @return 62进制字符串
     */
    public static String encode(long num) {
        StringBuilder sb = new StringBuilder();
        int remainder = 0;
        while (num > scale - 1) {
            /**
             * 对 scale 进行求余，然后将余数追加至 sb 中，由于是从末位开始追加的，因此最后需要反转（reverse）字符串
             */
            remainder = Long.valueOf(num % scale).intValue();
            sb.append(chars.charAt(remainder));

            num = num / scale;
        }
        sb.append(chars.charAt(Long.valueOf(num).intValue()));
        return sb.reverse().toString();
    }

    /**
     * @param  str  数字的内容,其进制值等同于baseChars的长度
     * @param  baseChars
     * @return 默认解码为10进制数据
     */
    public static String toNumber(String str,char[] baseChars) {
        /**
         * 将 0 开头的字符串进行替换
         */
        str = str.replace("^0*", "");
        int radix = baseChars.length;
        if(radix > MAX_RADIX){
            throw new RuntimeException("max radix is " + MAX_RADIX);
        }
        /**
         * 字符以及对应所代表的值,从0开始
         */
        Map<Character,Integer> valueRadixMap = IntStream.range(0,radix).mapToObj(it -> Integer.valueOf(it))
                .collect(Collectors.toMap(it -> baseChars[it],it -> it));
        StringBuilder sb = new StringBuilder(str.length() * 2);
        int tmpValue = 0;
        for (int i = 0; i < str.length(); i++) {
            int positionValue = valueRadixMap.get(str.charAt(i));
            tmpValue = tmpValue * radix;
            tmpValue = tmpValue + positionValue;

            while(tmpValue > CARRY_VALUE){
                sb.append(tmpValue);
            }
        }

        return sb.toString();
    }
}
