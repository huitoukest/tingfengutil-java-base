package com.tingfeng.util.java.base.common.utils;

import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CombinationUtilsTest {

    @Test
    public void combinationTest(){

        List<Collection<Integer>> values = CombinationUtils.combination(Arrays.asList(Integer.valueOf(1),2,3,4),1);
        Assert.assertEquals("[[1],[2],[3],[4]]",JSON.toJSONString(values));

        values = CombinationUtils.combination(Arrays.asList(Integer.valueOf(1),2,3,4),2);
        Assert.assertEquals("[[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]",JSON.toJSONString(values));

        values = CombinationUtils.combination(Arrays.asList(Integer.valueOf(1),2,3,4),3);
        Assert.assertEquals("[[1,2,3],[1,2,4],[1,3,4],[2,3,4]]",JSON.toJSONString(values));

        values = CombinationUtils.combination(Arrays.asList(Integer.valueOf(1),2,3,4),4);
        Assert.assertEquals("[[1,2,3,4]]",JSON.toJSONString(values));

        values = CombinationUtils.combination(Arrays.asList(Integer.valueOf(1),2,3,4),0);
        Assert.assertEquals("[]",JSON.toJSONString(values));

        values = CombinationUtils.combination(Arrays.asList(Integer.valueOf(1),2,3,4),5);
        Assert.assertEquals("[]",JSON.toJSONString(values));
    }
}
