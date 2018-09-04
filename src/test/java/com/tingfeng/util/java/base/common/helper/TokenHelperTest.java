package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.utils.CollectionUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TokenHelperTest {
    static ArrayList<String> list = new ArrayList<>();
    static{
        //分别存入空串，用户id，过期时间，token类型，空串
        list.add("");
        list.add("");
        list.add("123456");
        list.add(String.valueOf(System.currentTimeMillis()));
        list.add("2");
        list.add(",,5,6,7,,");
        list.add("");
        list.add("");
    }

    public static void main(String[] args){
        new TokenHelperTest().testSpeed();
    }

    @Test
    public void test(){
        test(true);
    }

    public void test(boolean isPrint) {
        TokenHelper tokenHelper = new TokenHelper();
        String token  = tokenHelper.getToken(list,"123456");

        List<String> obj = tokenHelper.parseToken(token,(contents)->contents.get(2),(contents)->contents);
        if(isPrint) {
            System.out.println(CollectionUtils.toListString(obj,"$"));
        }
    }

    @Test
    public void testSpeed(){
        long startTime = System.currentTimeMillis();
        for(int i = 0;i < 100000000;i++){
            if(i % 10000 == 0) {
                test(true);
            }else {
                test(false);
            }
        }
        System.out.println("use time:" + (System.currentTimeMillis() - startTime));
    }
}
