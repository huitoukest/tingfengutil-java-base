package com.tingfeng.util.java.base.common.helper;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.utils.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenHelperTest {

    static TokenHelper tokenHelper = new TokenHelper();

    @Test
    public void test(){

        ArrayList<String> list = new ArrayList<>();

        list.add("\\");
        list.add("\\");
        list.add("123456");
        list.add("\\");
        test(true,list);
        System.out.println();
        list.clear();

        list.add("\\,");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        test(true,list);
        System.out.println();
        list.clear();

        list.add("");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        list.add("");
        list.add("");
        list.add("");
        test(true,list);
        System.out.println();
        list.clear();
        //分别存入空串，用户id，过期时间，token类型，空串
        list.add("");
        list.add("");
        list.add("123456");
        list.add("\\,");
        list.add("123456");
        list.add(String.valueOf(System.currentTimeMillis()));
        list.add("2");
        list.add(",,5,6,7,,");
        list.add("");
        list.add("\\");
        list.add("");

        test(true,list);
    }

    public void test(boolean isPrint,ArrayList<String> list) {


        String token  = tokenHelper.getToken(list,"123456");

        List<String> obj = tokenHelper.parseToken(token,(contents)->contents.get(2),(contents)->contents);
        if(isPrint) {
            System.out.println(JSON.toJSONString(obj));
        }
        Assert.assertEquals(JSON.toJSONString(obj),JSON.toJSONString(list));
    }

    @Test
    public void testSpeed(){
        long startTime = System.currentTimeMillis();
        for(int i = 0;i < 10000000;i++){
            if(i % 10000 == 0) {
                test();
            }else {
                test();
            }
        }
        System.out.println("use time:" + (System.currentTimeMillis() - startTime));
    }

    @Test
    public void testSpeed2() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        AtomicInteger value = new AtomicInteger(0);
        for(int i = 0 ; i < 20 ;i++) {
         new Thread(()->{
             for (int j = 0; j < 500000; j++) {
                 if (value.get() % 100000 == 0) {
                     test();
                 } else {
                     test();
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
