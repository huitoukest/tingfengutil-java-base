package com.tingfeng.util.java.base.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 二进制数相关操作工具
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
