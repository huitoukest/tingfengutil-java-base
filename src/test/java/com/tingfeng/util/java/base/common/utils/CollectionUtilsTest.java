package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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


    @Test
    public void getOneTest(){
        Set<Integer> set = Arrays.asList(100,200).stream().collect(Collectors.toSet());
        Assert.assertTrue(set.contains(CollectionUtils.getOne(set)));
    }

    @Test
    public void getFirstTest(){
        List<Integer> a = Arrays.asList(100,200);
        Assert.assertTrue(CollectionUtils.getFirst(a).equals(100));
    }

    @Test
    public void getLastTest(){
        List<Integer> a = Arrays.asList(100,200);
        Assert.assertTrue(CollectionUtils.getLast(a).equals(200));
    }

    @Test
    public void mergeToList() {
        List<Integer> a = Arrays.asList(100,200);
        Integer[] b = new Integer[]{1,2,3};
        Assert.assertEquals(null,CollectionUtils.mergeToList(false,null));
        Assert.assertEquals("4",CollectionUtils.join(CollectionUtils.mergeToList(false,4)));
        Assert.assertEquals("100,200",CollectionUtils.join(CollectionUtils.mergeToList(false,a)));
        Assert.assertEquals("100,200,1,2,3,4",CollectionUtils.join(CollectionUtils.mergeToList(false,a,b,4)));
        Assert.assertEquals("100,200,1,2,3,4",CollectionUtils.join(CollectionUtils.mergeToList(false,Arrays.asList(a,b),4)));
        Assert.assertEquals("100,200,1,2,3,4",CollectionUtils.join(CollectionUtils.mergeToList(true,new Object[]{a,b,4},4)));
    }

}
