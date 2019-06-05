package com.tingfeng.util.java.base.common.helper;

import com.alibaba.fastjson.JSON;
import com.tingfeng.util.java.base.common.utils.CollectionUtils;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TokenHelperTest {

    static TokenHelper tokenHelper = new TokenHelper();

    @Test
    public void test(){
        test(true);
    }

    private void test(boolean  isPrint){
        ArrayList<String> list = new ArrayList<>();

        list.add("\\,");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        test(isPrint,list);
        list.clear();

        list.add("\\");
        list.add("\\");
        list.add("123456");
        list.add("\\");
        test(isPrint,list);
        list.clear();

        list.add("");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        list.add("");
        list.add("");
        list.add("");
        test(isPrint,list);
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

        test(isPrint,list);

        if(isPrint){
            System.out.println();
        }
    }

    public void test(boolean isPrint,ArrayList<String> list) {
        String token  = tokenHelper.getToken(list,"123456");
        List<String> obj = tokenHelper.parseToken(token, (contents) -> contents.get(2) ,(contents) -> contents);
        if(isPrint) {
            System.out.println(JSON.toJSONString(obj));
        }
        Assert.assertEquals(JSON.toJSONString(obj),JSON.toJSONString(list));
    }

    /**
     * 100w 8s，每秒12w 个
     */
    @Test
    public void testSpeed(){
        ArrayList<String> list = new ArrayList<>();
        list.add("\\,");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        long startTime = System.currentTimeMillis();
        for(int i = 0;i < 1000000 ;i++){
            if(i % 100000 == 0) {
                test(true,list);
            }else {
                test(false,list);
            }
        }
        System.out.println("use time:" + (System.currentTimeMillis() - startTime));
    }

    /**
     *  100w 数据 3s ，30w/s
     * @throws InterruptedException
     */
    @Test
    public void testSpeed2() throws InterruptedException {
        ArrayList<String> list = new ArrayList<>();
        list.add("\\,");
        list.add("\\");
        list.add("123456");
        list.add("\\,");
        TestUtils.printTime(5,200000,integer -> {
            if (integer % 100000 == 0) {
                test(true,list);
            } else {
                test(false,list);
            }
        });
    }
}
