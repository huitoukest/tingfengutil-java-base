package com.java.base.threads;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试java8的CompletableFuture
 */
public class CompletableFutureTest {

    public static Integer calc(Integer i) {
        try {
            if (i == 1) {
                Thread.sleep(3000);//任务1耗时3秒
            } else if (i == 5) {
                Thread.sleep(5000);//任务5耗时5秒
            } else if(i == 2){
                //throw new RuntimeException("value is 2"); //此异常会被捕获到，但是在allOf(cfs).join()时会抛出此异常
            }else{
                Thread.sleep(1000);//其它任务耗时1秒
            }
            System.out.println("task线程：" + Thread.currentThread().getName()
                    + "任务i=" + i + ",完成！+" + new Date());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return i;
    }

    @Test
    public void compiletableFutureTest() {
        Long start = System.currentTimeMillis();
        // 结果集
        List<String> list = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        List<Integer> taskList = Arrays.asList(2, 1, 3, 4, 5, 6, 7, 8, 9, 10);
        // 全流式处理转换成CompletableFuture[]+组装成一个无返回值CompletableFuture，join等待执行完毕。返回结果whenComplete获取
        CompletableFuture[] cfs = taskList.stream()
                .map(integer -> CompletableFuture.supplyAsync(() -> calc(integer), executorService)
                        .thenApply(h -> Integer.toString(h))
                        .whenComplete((s, e) -> {
                            System.out.println("任务" + s + "完成!result=" + s + "，异常 e=" + e + "," + new Date());
                            list.add(s);
                        })
                ).toArray(CompletableFuture[]::new);
        // 封装后无返回值，必须自己whenComplete()获取
        CompletableFuture.allOf(cfs).join();
        //返回的结果是混乱的
        System.out.println("list=" + JSON.toJSONString(list) + ",耗时=" + (System.currentTimeMillis() - start));
    }
}
