package com.tingfeng.util.java.base.common.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author wangGang
 * @Date 2020-06-09
 */
public class MapUtilsTest {

    @Test
    public void reversesTest(){
        Map<String, Collection<Integer>> map = MapUtils.newHashMap(Arrays.asList("A","B","C")
                ,Arrays.asList(Arrays.asList(1,2,3)
                        ,Arrays.asList(4,5,6)
                        ,Arrays.asList(7,8,9)));
        Map<Integer,List<String>> reMap = MapUtils.reverses(map);
        assert reMap.get(8).stream().findFirst().get().equals("C");
    }

    @Test
    public void computeTest(){
        Map<String, List<Integer>> map = MapUtils.newHashMap(Arrays.asList("A","B","C")
                ,Arrays.asList(Arrays.asList(1,2,3)
                        ,Arrays.asList(4,5,6)
                        ,Arrays.asList(7,8,9)));
        Map<Integer,String> reMap = MapUtils.compute(map,null,it -> "" + it.size());
        assert reMap.get("B").equals("3");
    }

    @Test
    public void newHashMapATest(){
        Map<Object, Object> objectMap = MapUtils.newHashMap(new Object[]{"A", 1, "B", 2});
        assert objectMap.get("A").equals(1);
    }

    @Test
    public void newHashMapBTest(){
        Map<String, Integer> objectMap = MapUtils.newHashMap(String::valueOf,it -> Integer.valueOf(it.toString()),
                "A", "1", "B", "2");
        assert objectMap.get("B").equals(2);
    }

}
