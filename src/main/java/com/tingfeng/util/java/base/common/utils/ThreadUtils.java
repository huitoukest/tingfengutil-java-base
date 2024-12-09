package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.thread.NamedThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * 线程相关的工具
 */
public class ThreadUtils {
    /**
     * sleep mills
     * @param mills
     */
    public static void sleep(long mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建线程工厂
     * @param namePrefix 线程的名称前缀
     * @param isDaemon 是否守护线程
     * @return
     */
    public static ThreadFactory newNamedThreadFactory(String namePrefix,boolean isDaemon){
        return new NamedThreadFactory(namePrefix, isDaemon);
    }
}
