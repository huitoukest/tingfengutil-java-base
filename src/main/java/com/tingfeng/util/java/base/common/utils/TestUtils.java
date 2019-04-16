package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVTwo;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 专用于测试的工具类
 */
public class TestUtils {

    /**
     *
     * @param thread 启动的线程数量
     * @param cycleCountInThread 线程内的循环次数
     * @param functionVTwo  参数(当前线程的索引，线程内的循环索引)
     */
    public static void printTime(int thread, int cycleCountInThread, FunctionVTwo<Integer,Integer> functionVTwo) {
        long startTime = System.currentTimeMillis();
        AtomicInteger value = new AtomicInteger(0);
        for(int i = 0 ; i < thread ;i++) {
            int threadNo = i;
            new Thread(()->{
                try{
                    try {
                        for(int  j = 0;j < cycleCountInThread ; j++) {
                            functionVTwo.run(threadNo,j);
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
    /**
     *
     * @param thread 启动的线程数量
     * @param cycleCountInThread 线程内的循环次数
     * @param functionVOne 需要执行的业务逻辑,传入cycleCountInThread中每次循环的下标，从0到(cycleCountInThread-1)的
     */
    public static void printTime(int thread, int cycleCountInThread, FunctionVOne<Integer> functionVOne) {
        printTime(thread,cycleCountInThread,(threadNo,indexInThread) -> functionVOne.accept(indexInThread));
    }
}
