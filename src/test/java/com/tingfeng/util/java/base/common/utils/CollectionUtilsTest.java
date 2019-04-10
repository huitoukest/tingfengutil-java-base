package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @Author wangGang
 * @Description //TODO
 * @Date 2019-04-10 15:38
 **/
public class CollectionUtilsTest {

    @Test
    public void joinTest(){
        List<String> list = Arrays.asList("aaa","bbb");
        String str = CollectionUtils.join(list,"");
        Assert.assertEquals("aaabbb",str);
    }

}
