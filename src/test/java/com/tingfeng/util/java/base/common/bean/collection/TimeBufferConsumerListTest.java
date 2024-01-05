package com.tingfeng.util.java.base.common.bean.collection;

import com.tingfeng.util.java.base.common.utils.RandomUtils;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class TimeBufferConsumerListTest {

    @Test
    public void test() throws InterruptedException {
        AtomicInteger consumerSize = new AtomicInteger(0);
        TimeBufferConsumerList<Integer> timeBufferConsumerList = new TimeBufferConsumerList<>(10,64,300,list -> {
            int size = list.size();
            System.out.println("Buffer1,当前时间:" + System.currentTimeMillis() +",当前消费数量:" + size + ",消费总数:" + consumerSize.addAndGet(size));
        });
        TimeBufferConsumerList<Integer> timeBufferConsumerList2 = new TimeBufferConsumerList<>(10,64,300,list -> {
            int size = list.size();
            System.out.println("Buffer2,当前时间:" + System.currentTimeMillis() +",当前消费数量:" + size + ",消费总数:" + consumerSize.addAndGet(size));
        });
        int thread = 20;
        int cycle = 100;
        int total = thread * cycle;
        TestUtils.printTime(thread,cycle, index -> {
            try {
                Thread.sleep(RandomUtils.randomInt(2,50));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeBufferConsumerList.add(1);
            timeBufferConsumerList2.add(1);
        });

        while (consumerSize.intValue() < total){
            Thread.sleep(1);
        }
        Thread.sleep(1000);
        Assert.assertEquals(total * 2,consumerSize.intValue());
    }
}