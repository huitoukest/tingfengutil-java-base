package com.tingfeng.util.java.base.common.helper;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class SimplePoolHelperTest {

    @Test
    public void test(){
           int threadSize = 100;
           SimplePoolHelper<StringBuilder> simplePoolHelper = new SimplePoolHelper<StringBuilder>(10,()->new StringBuilder());
            AtomicInteger atomicInteger = new AtomicInteger(0);
           for(int i = 0;i < threadSize ;i++){
               new Thread(()->{
                   StringBuilder sb = simplePoolHelper.get();
                   sb.setLength(0);
                   sb.append("1");
                   try {
                       Thread.sleep(2);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
                   sb.append("2");
                   System.out.println(sb.toString());
                   simplePoolHelper.release(sb);
                   atomicInteger.incrementAndGet();
               }).start();
           }
           while (atomicInteger.get() < threadSize){
               try {
                   Thread.sleep(5);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           System.out.println("over");
    }
}
