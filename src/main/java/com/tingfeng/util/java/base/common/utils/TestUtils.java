package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVOne;
import com.tingfeng.util.java.base.common.inter.voidfunction.FunctionVTwo;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 专用于测试的工具类
 * @author huitoukest
 */
public class TestUtils {

    /**
     * 线程中的异常会自动捕获，并在执行完毕后抛出；
     * 发生异常后，无法保证所有的任务都执行
     * @param thread 启动的线程数量
     * @param cycleCountInThread 线程内的循环次数
     * @param functionVTwo  参数(当前线程的索引，线程内的循环索引)
     */
    public static void printTime(int thread, int cycleCountInThread, FunctionVTwo<Integer,Integer> functionVTwo) {
        assert thread > 0;
        assert cycleCountInThread > 0;
        long startTime = System.currentTimeMillis();
        AtomicInteger threadCount = new AtomicInteger(0);
        AtomicReference<Throwable> throwableAtomicReference = new AtomicReference<>();
        for(int i = 0 ; i < thread ;i++) {
            int threadNo = i;
            new Thread(()->{
                if(throwableAtomicReference.get() == null) {
                    try {
                        for (int j = 0; j < cycleCountInThread; j++) {
                                functionVTwo.run(threadNo, j);
                        }
                    } catch (Throwable e) {
                        throwableAtomicReference.set(e);
                    }finally {
                        threadCount.incrementAndGet();
                    }
                }
            }).start();
        }
        while (threadCount.get() < thread && throwableAtomicReference.get() == null){
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(throwableAtomicReference.get() != null){
            throw new RuntimeException(throwableAtomicReference.get());
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
