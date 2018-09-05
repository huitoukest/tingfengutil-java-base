package com.tingfeng.util.java.base.common.helper;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class FixedPoolHelperTest {

    @Test
    public void fixedPoolHelperTest() throws InterruptedException {
        FixedPoolHelper<StringBuilder> fixedPoolHelper = new FixedPoolHelper(100,()->new StringBuilder());
        long startTime = System.currentTimeMillis();
        AtomicInteger value = new AtomicInteger(0);
        for(int i = 0 ; i < 100 ;i++) {
            new Thread(()->{
                for (int j = 0; j < 1000; j++) {
                    String s = fixedPoolHelper.run((sb)->{
                        sb.setLength(0);
                        sb.append("100");
                        Thread.sleep(1);
                        sb.append(123);
                        return sb.toString();
                    });
                    if(j % 100 == 0){
                        System.out.println(s);
                    }
                    value.incrementAndGet();
                }
            }).start();
        }
        while (value.get() < 20 * 500000){
            Thread.sleep(5);
        }
        System.out.println("use time:" + (System.currentTimeMillis() - startTime));
    }


}
