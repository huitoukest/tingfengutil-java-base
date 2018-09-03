package com.tingfeng.util.java.base.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 二进制数相关操作工具
 */
public class BinaryOperationUtils {
    private static final List<Double> binaryPowValues = new ArrayList<>(10);
    static {
        for(int i = 0; i <= 30 ;i++){
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
    public static double getBinaryValueByPow(Integer pow){
        if(pow > 30){
            throw new RuntimeException("indexOfPower must <= 30");
        }
        if(pow < 0) {
            return 1.0 / binaryPowValues.get(pow);
        }
        return binaryPowValues.get(pow);
    }
}
