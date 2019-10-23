package com.tingfeng.util.java.base.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 二进制数相关操作工具
 * @author wanggang
 */
public class BinaryOperationUtils {
    private static final int MAX_POW_VALUE = 64;
    private static final List<Double> binaryPowValues = new ArrayList<>(100);
    static {
        for(int i = 0; i <= MAX_POW_VALUE ;i++){
            binaryPowValues.add(Math.pow(2.0,(double) i));
        }
    }

    /**
     * 求出(baseNum) 和 (以2为底数的x幂的比baseNum小的数） 做与位运算的 低位相同的数字
     * 比如输入 15 -> 返回 [1,2,4,8]
     * @param parentNum
     * @return parentNum包含的2的n次幂数
     */
    public static List<Integer> getSubBinaryNumbers(Integer parentNum){
        if(null == parentNum){
            return Collections.emptyList();
        }
        List<Integer> numbers = new ArrayList<>();
        int  i = 0;
        while(true){
            Double value = getBinaryValueByPow(i);
            if( value <= parentNum){
                i ++;
                int intValue = value.intValue();
                if((intValue & parentNum) == intValue){
                    numbers.add(intValue);
                }
            }else{
                break;
            }
        }
        return numbers;
    }

    /**
     *  用从containsValue到maxValue的的值A与containsValue执行按位与运行，
     *  如果等于计算结果等于containsValue,则将A加入到结果列表中
     * 比如输入:
     *  bitwiseValue = 8;  containsValue = 3 ；则结果返回 [3,7];
     *  bitwiseValue = 7;  containsValue = 1 ；则结果返回 [1,3,5,7];
     * @param maxValue 最大值； null时 返回空List
     * @return containsValue 需要包含的值
     */
    public static List<Integer> getContainBinaryNumbers(int maxValue, Integer containsValue){
        List<Integer> numbers = new ArrayList<>();
        for(int i = containsValue; i <= maxValue ; i ++){
            if((i & containsValue) == containsValue){
                numbers.add(i);
            }
        }
        return numbers;
    }

    /**
     *
     * @param pow 当前以2位底的指数的幂
     * @return 以2位底indexOfPower次方的结果
     */
    public static double getBinaryValueByPow(int pow){
        if(pow > MAX_POW_VALUE){
            throw new RuntimeException("indexOfPower must <= " + MAX_POW_VALUE);
        }
        if(pow < 0) {
            return 1.0 / binaryPowValues.get(pow);
        }
        return binaryPowValues.get(pow);
    }

    /**
     * 获取第一个比currentValue打的2的n次幂的值，否则返回-1;
     * @param currentValue
     * @return
     */
    public static long getFirstThanBinaryValue(long currentValue){
        Double value = binaryPowValues.stream().filter(it -> it >= currentValue).findFirst().orElse(null);
        if(value == null){
            return -1;
        }
        return value.longValue();
    }
}
