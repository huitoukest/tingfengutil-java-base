package com.tingfeng.util.java.base.collection;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @Author huitoukest
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

    @Test
    public void getValueTest() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);

        // key exists -> return value
        Assert.assertEquals(Integer.valueOf(1), MapUtils.getValue(map, "a"));

        // key absent -> return null
        Assert.assertNull(MapUtils.getValue(map, "b"));
    }

    @Test(expected = NullPointerException.class)
    public void getValueWithNullMap() {
        MapUtils.getValue(null, "a");
    }

    @Test
    public void getOrDefaultWithValueTest() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");

        // key exists -> return value
        Assert.assertEquals("1", MapUtils.getOrDefault(map, "a", "default"));

        // key absent -> return default
        Assert.assertEquals("default", MapUtils.getOrDefault(map, "b", "default"));
    }

    @Test(expected = NullPointerException.class)
    public void getOrDefaultWithValueWithNullMap() {
        MapUtils.getOrDefault(null, "a", "x");
    }

    @Test
    public void getOrDefaultWithSupplierTest() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        Supplier<String> defaultSupplier = () -> "default";

        // key exists -> return value, supplier not evaluated for result
        Assert.assertEquals("1", MapUtils.getOrDefault(map, "a", defaultSupplier));

        // key absent -> return supplier value
        Assert.assertEquals("default", MapUtils.getOrDefault(map, "b", defaultSupplier));

        // supplier returns null
        Assert.assertNull(MapUtils.getOrDefault(map, "c", (Supplier<String>) () -> null));
    }

    @Test(expected = NullPointerException.class)
    public void getOrDefaultWithSupplierWithNullMap() {
        Map<String, String> nullMap = null;
        MapUtils.getOrDefault(nullMap, "a", (Supplier<String>) () -> "x");
    }

    @Test
    public void putIfAbsentTest() {
        Map<String, String> map = new HashMap<>();

        // key absent -> put and return null
        String result = MapUtils.putIfAbsent(map, "a", "1");
        Assert.assertNull(result);
        Assert.assertEquals("1", map.get("a"));

        // key exists -> return existing, do not replace
        result = MapUtils.putIfAbsent(map, "a", "2");
        Assert.assertEquals("1", result);
        Assert.assertEquals("1", map.get("a"));
    }

    @Test(expected = NullPointerException.class)
    public void putIfAbsentWithNullMap() {
        MapUtils.putIfAbsent(null, "a", "1");
    }

    @Test
    public void computeIfAbsentTest() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");

        // key exists -> return existing, do not call function
        String result = MapUtils.computeIfAbsent(map, "a", k -> "2");
        Assert.assertEquals("1", result);
        Assert.assertEquals("1", map.get("a"));

        // key absent -> compute and put
        result = MapUtils.computeIfAbsent(map, "b", k -> k.toUpperCase());
        Assert.assertEquals("B", result);
        Assert.assertEquals("B", map.get("b"));
    }

    @Test(expected = NullPointerException.class)
    public void computeIfAbsentWithNullMap() {
        MapUtils.computeIfAbsent(null, "a", k -> "1");
    }

    @Test(expected = NullPointerException.class)
    public void computeIfAbsentWithNullFunction() {
        MapUtils.computeIfAbsent(new HashMap<>(), "a", null);
    }

    @Test
    public void newHashMapFromListsTest() {
        List<String> keys = Arrays.asList("x", "y", "z");
        List<Integer> values = Arrays.asList(10, 20, 30);
        Map<String, Integer> map = MapUtils.newHashMap(keys, values);
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(Integer.valueOf(10), map.get("x"));
        Assert.assertEquals(Integer.valueOf(20), map.get("y"));
        Assert.assertEquals(Integer.valueOf(30), map.get("z"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void newHashMapFromListsWithDifferentSize() {
        MapUtils.newHashMap(Arrays.asList("a", "b"), Arrays.asList(1));
    }

    @Test
    public void newHashMapFromArraysTest() {
        String[] keys = {"a", "b"};
        Integer[] values = {1, 2};
        Map<String, Integer> map = MapUtils.newHashMap(keys, values);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(Integer.valueOf(1), map.get("a"));
        Assert.assertEquals(Integer.valueOf(2), map.get("b"));
    }

    @Test
    public void newHashMapFromArraysWithSingleValueTest() {
        String[] keys = {"a", "b", "c"};
        Map<String, Integer> map = MapUtils.newHashMap(keys, 99);
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(Integer.valueOf(99), map.get("a"));
        Assert.assertEquals(Integer.valueOf(99), map.get("b"));
        Assert.assertEquals(Integer.valueOf(99), map.get("c"));
    }

    @Test
    public void newHashMapFromListWithSingleValueTest() {
        Map<String, Integer> map = MapUtils.newHashMap(Arrays.asList("a", "b"), 42);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(Integer.valueOf(42), map.get("a"));
        Assert.assertEquals(Integer.valueOf(42), map.get("b"));
    }

    @Test
    public void newHashMapWithSingleKVTest() {
        Map<String, Integer> map = MapUtils.newHashMap("key", 123);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(Integer.valueOf(123), map.get("key"));
    }

    @Test
    public void reverseTest() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);

        Map<Integer, String> reversed = MapUtils.reverse(map, (k1, k2) -> k1);
        Assert.assertEquals(3, reversed.size());
        Assert.assertEquals("a", reversed.get(1));
        Assert.assertEquals("b", reversed.get(2));
        Assert.assertEquals("c", reversed.get(3));
    }

    @Test
    public void reverseWithMergeTest() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 1);

        Map<Integer, String> reversed = MapUtils.reverse(map, (k1, k2) -> k1 + "," + k2);
        Assert.assertEquals(1, reversed.size());
        Assert.assertTrue(reversed.get(1).contains("a"));
        Assert.assertTrue(reversed.get(1).contains("b"));
    }

    @Test
    public void computeWithBothMappersTest() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);

        Map<Integer, String> computed = MapUtils.compute(map,
                k -> Integer.valueOf(k.charAt(0) - 'a' + 1),
                v -> "v" + v);
        Assert.assertEquals(2, computed.size());
        Assert.assertEquals("v1", computed.get(1));
        Assert.assertEquals("v2", computed.get(2));
    }
}
