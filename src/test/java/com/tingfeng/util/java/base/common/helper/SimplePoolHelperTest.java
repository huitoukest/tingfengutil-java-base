package com.tingfeng.util.java.base.common.helper;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SimplePoolHelperTest {

    @Test
    public void test() throws InterruptedException {
           int threadSize = 20;
           SimplePoolHelper<StringBuilder> simplePoolHelper = new SimplePoolHelper<StringBuilder>(10,()->new StringBuilder());
           CountDownLatch latch = new CountDownLatch(threadSize);
           
           for(int i = 0;i < threadSize ;i++){
               new Thread(()->{
                   try {
                       StringBuilder sb = simplePoolHelper.get();
                       sb.setLength(0);
                       sb.append("1");
                       try {
                           Thread.sleep(2);
                       } catch (InterruptedException e) {
                           Thread.currentThread().interrupt();
                           e.printStackTrace();
                       }
                       sb.append("2");
                       System.out.println(sb.toString());
                       simplePoolHelper.release(sb);
                   } finally {
                       latch.countDown();
                   }
               }).start();
           }
           
           // 等待所有线程完成，最多等待10秒
           boolean completed = latch.await(10, TimeUnit.SECONDS);
           if (!completed) {
               System.out.println("测试超时，强制结束");
           }
           System.out.println("over");
    }
}