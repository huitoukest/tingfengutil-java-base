package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Test
    public void shuffle() {
        List<Integer> integers = IntStream.range(0, 100).mapToObj(Integer::valueOf).collect(Collectors.toList());
        List<Integer> newIntegers = CollectionUtils.shuffle(integers);
        String oldValue = CollectionUtils.join(integers);
        String newValue = CollectionUtils.join(newIntegers);
        Assert.assertEquals(integers.size(), newIntegers.size());
        Assert.assertNotEquals(oldValue, newValue);
        Assert.assertTrue(CollectionUtils.eq(integers, newIntegers.stream().sorted().collect(Collectors.toList())));
    }

    @Test
    public void testGetListFromArray() {
        Integer[] array = {1, 2, 3, 4, 5};
        List<Integer> list = CollectionUtils.getList(array);
        Assert.assertNotNull(list);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(Integer.valueOf(1), list.get(0));
        Assert.assertEquals(Integer.valueOf(5), list.get(4));

        // 测试null数组
        Assert.assertNull(CollectionUtils.getList(null));
    }

    @Test
    public void testGetListFromString() {
        String sourceString = "1,2,3,4,5";
        List<Integer> list = CollectionUtils.getList(sourceString, ",", Integer::parseInt);
        Assert.assertNotNull(list);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(Integer.valueOf(1), list.get(0));
        Assert.assertEquals(Integer.valueOf(5), list.get(4));

        // 测试空字符串
        List<Integer> emptyList = CollectionUtils.getList("", ",", Integer::parseInt);
        Assert.assertNotNull(emptyList);
        Assert.assertTrue(emptyList.isEmpty());
    }

    @Test
    public void testGetStringList() {
        String sourceString = "a,b,c,d,e";
        List<String> list = CollectionUtils.getStringList(sourceString, ",");
        Assert.assertNotNull(list);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("e", list.get(4));

        // 测试默认分隔符
        List<String> defaultList = CollectionUtils.getStringList("a,b,c");
        Assert.assertNotNull(defaultList);
        Assert.assertEquals(3, defaultList.size());
    }

    @Test
    public void testGetIntegerList() {
        String sourceString = "1,2,3,4,5";
        List<Integer> list = CollectionUtils.getIntegerList(sourceString, ",");
        Assert.assertNotNull(list);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(Integer.valueOf(1), list.get(0));
        Assert.assertEquals(Integer.valueOf(5), list.get(4));

        // 测试默认分隔符
        List<Integer> defaultList = CollectionUtils.getIntegerList("1,2,3");
        Assert.assertNotNull(defaultList);
        Assert.assertEquals(3, defaultList.size());
    }

    @Test
    public void testGetLongList() {
        String sourceString = "1,2,3,4,5";
        List<Long> list = CollectionUtils.getLongList(sourceString, ",");
        Assert.assertNotNull(list);
        Assert.assertEquals(5, list.size());
        Assert.assertEquals(Long.valueOf(1), list.get(0));
        Assert.assertEquals(Long.valueOf(5), list.get(4));

        // 测试默认分隔符
        List<Long> defaultList = CollectionUtils.getLongList("1,2,3");
        Assert.assertNotNull(defaultList);
        Assert.assertEquals(3, defaultList.size());
    }

    @Test
    public void testJoinWithDefaultSeparator() {
        List<String> list = Arrays.asList("a", "b", "c");
        String result = CollectionUtils.join(list);
        Assert.assertEquals("a,b,c", result);

        // 测试空集合
        List<String> emptyList = new ArrayList<>();
        Assert.assertEquals("", CollectionUtils.join(emptyList));
    }

    @Test
    public void testIsContain() {
        List<String> list = Arrays.asList("a", "b", "c");
        Assert.assertTrue(CollectionUtils.isContain(list, "a"));
        Assert.assertFalse(CollectionUtils.isContain(list, "d"));
    }

    @Test
    public void testIsContainAny() {
        List<User> users = Arrays.asList(
                new User(1, "Alice"),
                new User(2, "Bob"),
                new User(3, "Charlie")
        );

        List<Order> orders = Arrays.asList(
                new Order(1, "Order1"),
                new Order(4, "Order4")
        );

        boolean result = CollectionUtils.isContainAny(
                users, 
                orders, 
                User::getId, 
                Order::getUserId
        );
        Assert.assertTrue(result);

        // 测试不包含的情况
        List<Order> noMatchOrders = Arrays.asList(
                new Order(4, "Order4"),
                new Order(5, "Order5")
        );
        boolean noMatchResult = CollectionUtils.isContainAny(
                users, 
                noMatchOrders, 
                User::getId, 
                Order::getUserId
        );
        Assert.assertFalse(noMatchResult);
    }

    @Test
    public void testEqList() {
        List<Integer> listA = Arrays.asList(1, 2, 3);
        List<Integer> listB = Arrays.asList(1, 2, 3);
        List<Integer> listC = Arrays.asList(1, 2, 4);

        Assert.assertTrue(CollectionUtils.eq(listA, listB));
        Assert.assertFalse(CollectionUtils.eq(listA, listC));

        // 测试自定义比较函数
        List<String> strListA = Arrays.asList("1", "2", "3");
        List<Integer> strListB = Arrays.asList(1, 2, 3);
        boolean customEq = CollectionUtils.eq(
                strListA, 
                strListB, 
                (a, b) -> Integer.parseInt(a) == b
        );
        Assert.assertTrue(customEq);
    }

    @Test
    public void testEqSet() {
        Set<Integer> setA = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> setB = new HashSet<>(Arrays.asList(3, 2, 1));
        Set<Integer> setC = new HashSet<>(Arrays.asList(1, 2, 4));

        Assert.assertTrue(CollectionUtils.eq(setA, setB));
        Assert.assertFalse(CollectionUtils.eq(setA, setC));
    }

    @Test
    public void testCreateSet() {
        Set<Integer> set = CollectionUtils.createSet(1, 2, 3, 4, 5);
        Assert.assertNotNull(set);
        Assert.assertEquals(5, set.size());
        Assert.assertTrue(set.contains(1));
        Assert.assertTrue(set.contains(5));
    }

    @Test
    public void testSplit() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> splitList = CollectionUtils.split(list, 3);
        Assert.assertNotNull(splitList);
        Assert.assertEquals(4, splitList.size());
        Assert.assertEquals(3, splitList.get(0).size());
        Assert.assertEquals(3, splitList.get(1).size());
        Assert.assertEquals(3, splitList.get(2).size());
        Assert.assertEquals(1, splitList.get(3).size());
    }

    @Test
    public void testMergeToListWithCollections() {
        List<Integer> list1 = Arrays.asList(1, 2, 3);
        List<Integer> list2 = Arrays.asList(4, 5, 6);
        List<Integer> merged = CollectionUtils.mergeToList(list1, list2);
        Assert.assertNotNull(merged);
        Assert.assertEquals(6, merged.size());
        Assert.assertTrue(merged.containsAll(list1));
        Assert.assertTrue(merged.containsAll(list2));
    }

    @Test
    public void testFindFirst() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Integer result = CollectionUtils.findFirst(list, i -> i > 3, 0);
        Assert.assertEquals(Integer.valueOf(4), result);

        // 测试没有匹配的情况
        Integer noMatchResult = CollectionUtils.findFirst(list, i -> i > 10, 0);
        Assert.assertEquals(Integer.valueOf(0), noMatchResult);

        // 测试默认方法
        Integer firstResult = CollectionUtils.findFirst(list);
        Assert.assertEquals(Integer.valueOf(1), firstResult);
    }

    @Test
    public void testFindAny() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Integer result = CollectionUtils.findAny(list, i -> i > 3, 0);
        Assert.assertTrue(result > 3);

        // 测试没有匹配的情况
        Integer noMatchResult = CollectionUtils.findAny(list, i -> i > 10, 0);
        Assert.assertEquals(Integer.valueOf(0), noMatchResult);

        // 测试默认方法
        Integer anyResult = CollectionUtils.findAny(list);
        Assert.assertTrue(list.contains(anyResult));
    }

    @Test
    public void testToString() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        String result = CollectionUtils.toString(
                list, 
                "[", 
                "]", 
                ", ", 
                (item, index) -> String.valueOf(item)
        );
        Assert.assertEquals("[1, 2, 3, 4, 5]", result);

        // 测试简化版
        String simpleResult = CollectionUtils.toString(
                list, 
                "[", 
                "]", 
                ", ",
                it -> String.valueOf(it)
        );
        Assert.assertEquals("[1, 2, 3, 4, 5]", simpleResult);
    }

    @Test
    public void testJoinCollections() {
        List<User> users = Arrays.asList(
                new User(1, "Alice"),
                new User(2, "Bob")
        );

        List<Order> orders = Arrays.asList(
                new Order(1, "Order1"),
                new Order(2, "Order2")
        );

        Map<User, Order> result = CollectionUtils.join(
                users, 
                orders, 
                User::getId, 
                Order::getUserId
        );
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Order1", result.get(users.get(0)).getName());
        Assert.assertEquals("Order2", result.get(users.get(1)).getName());
    }

    @Test
    public void testGetListFromCollection() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        List<String> result = CollectionUtils.getList(list, String::valueOf);
        Assert.assertNotNull(result);
        Assert.assertEquals(5, result.size());
        Assert.assertEquals("1", result.get(0));
        Assert.assertEquals("5", result.get(4));
    }

    @Test
    public void testToMap() {
        List<User> users = Arrays.asList(
                new User(1, "Alice"),
                new User(2, "Bob")
        );
        Map<Integer, User> result = CollectionUtils.toMap(users, User::getId);
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Alice", result.get(1).getName());
        Assert.assertEquals("Bob", result.get(2).getName());
    }

    @Test
    public void testMergeToCollection() {
        List<Integer> target = new ArrayList<>();
        List<Integer> list1 = Arrays.asList(1, 2, 3);
        Integer[] array1 = {4, 5, 6};
        CollectionUtils.mergeToCollection(target, false, list1, array1, 7);
        Assert.assertEquals(7, target.size());
        Assert.assertTrue(target.containsAll(list1));
        Assert.assertTrue(target.contains(4));
        Assert.assertTrue(target.contains(5));
        Assert.assertTrue(target.contains(6));
        Assert.assertTrue(target.contains(7));

        // 测试去重
        List<Integer> target2 = new ArrayList<>();
        CollectionUtils.mergeToCollection(target2, true, 1, 2, 1, 3);
        Assert.assertEquals(3, target2.size());
        Assert.assertTrue(target2.contains(1));
        Assert.assertTrue(target2.contains(2));
        Assert.assertTrue(target2.contains(3));
    }

    // 辅助类
    static class User {
        private int id;
        private String name;

        public User(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    static class Order {
        private int userId;
        private String name;

        public Order(int userId, String name) {
            this.userId = userId;
            this.name = name;
        }

        public int getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }
    }
}