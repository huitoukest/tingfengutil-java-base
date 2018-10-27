package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 专用于测试的工具类
 */
public class TestUtils {

    /**
     *
     * @param thread 启动的线程数量
     * @param cycleCountInThread 线程内的循环次数
     * @param functionVOne 需要执行的业务逻辑,传入cycleCountInThread中每次循环的下标，从0到(cycleCountInThread-1)的
     */
    public static void printTime(int thread, int cycleCountInThread, FunctionVOne<Integer> functionVOne) {
        long startTime = System.currentTimeMillis();
        AtomicInteger value = new AtomicInteger(0);
        for(int i = 0 ; i < thread ;i++) {
            new Thread(()->{
                try{
                    try {
                        for(int  j = 0;j < cycleCountInThread ; j++) {
                            functionVOne.run(j);
                        }
                    } catch (Exception e) {
                        throw new BaseException(e);
                    }
                }finally {
                    value.incrementAndGet();
                }
            }).start();
        }
        while (value.get() < thread){
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("use time:" + (System.currentTimeMillis() - startTime));
    }
}
