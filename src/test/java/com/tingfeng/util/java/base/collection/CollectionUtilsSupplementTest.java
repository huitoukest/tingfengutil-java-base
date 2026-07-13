package com.tingfeng.util.java.base.collection;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * CollectionUtils 补充测试 —— 覆盖 9 项变更的 null guard 及新增 join(distinct) 重载
 */
public class CollectionUtilsSupplementTest {

    // ============================================================
    // 1. join(Collection, String, boolean distinct) —— 新增 distinct 重载
    // ============================================================

    @Test
    public void joinDistinct_dedup() {
        List<String> list = Arrays.asList("a", "b", "a", "c", "b");
        String result = CollectionUtils.join(list, ",", true);
        Assert.assertEquals("a,b,c", result);
    }

    @Test
    public void joinDistinct_nonDistinct() {
        List<String> list = Arrays.asList("a", "b", "a", "c", "b");
        String result = CollectionUtils.join(list, ",", false);
        Assert.assertEquals("a,b,a,c,b", result);
    }

    @Test
    public void joinDistinct_emptyCollection() {
        List<String> empty = new ArrayList<>();
        Assert.assertEquals("", CollectionUtils.join(empty, ",", true));
        Assert.assertEquals("", CollectionUtils.join(empty, ",", false));
    }

    @Test
    public void joinDistinct_nullCollection() {
        Assert.assertEquals("", CollectionUtils.join(null, ",", true));
        Assert.assertEquals("", CollectionUtils.join(null, ",", false));
    }

    @Test
    public void joinDistinct_nullSeparator() {
        List<String> list = Arrays.asList("a", "b", "c");
        // null separator should be treated as ""
        String result = CollectionUtils.join(list, null, true);
        Assert.assertEquals("abc", result);
    }

    @Test
    public void joinDistinct_nullElement() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add(null);
        list.add("b");
        // LinkedHashSet keeps one null; sb.append(null) produces "null"
        String distinctResult = CollectionUtils.join(list, ",", true);
        Assert.assertEquals("a,null,b", distinctResult);
    }

    // ============================================================
    // 2. isContain(Collection, Object) —— null guard
    // ============================================================

    @Test
    public void isContain_nullCollection() {
        Assert.assertFalse(CollectionUtils.isContain(null, "a"));
    }

    @Test
    public void isContain_nullObjInCollection() {
        List<String> list = Arrays.asList("a", null, "b");
        Assert.assertTrue(CollectionUtils.isContain(list, null));
    }

    // ============================================================
    // 3. isContainAny(Collection, Collection, Function, Function) —— null guard
    // ============================================================

    @Test
    public void isContainAny_firstNull() {
        List<String> listB = Arrays.asList("a", "b");
        Assert.assertFalse(CollectionUtils.isContainAny(null, listB,
                Function.identity(), Function.identity()));
    }

    @Test
    public void isContainAny_secondNull() {
        List<String> listA = Arrays.asList("a", "b");
        Assert.assertFalse(CollectionUtils.isContainAny(listA, null,
                Function.identity(), Function.identity()));
    }

    @Test
    public void isContainAny_bothNull() {
        Assert.assertFalse(CollectionUtils.isContainAny(null, null,
                Function.identity(), Function.identity()));
    }

    @Test
    public void isContainAny_intersection() {
        List<Integer> listA = Arrays.asList(1, 2, 3);
        List<Integer> listB = Arrays.asList(3, 4, 5);
        Assert.assertTrue(CollectionUtils.isContainAny(listA, listB,
                Function.identity(), Function.identity()));
    }

    @Test
    public void isContainAny_noIntersection() {
        List<Integer> listA = Arrays.asList(1, 2, 3);
        List<Integer> listB = Arrays.asList(4, 5, 6);
        Assert.assertFalse(CollectionUtils.isContainAny(listA, listB,
                Function.identity(), Function.identity()));
    }

    // ============================================================
    // 4. eq(List, List) —— null guard
    // ============================================================

    @Test
    public void eqList_bothNull() {
        Assert.assertTrue(CollectionUtils.eq(null, (List) null));
    }

    @Test
    public void eqList_firstNull() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Assert.assertFalse(CollectionUtils.eq(null, list));
    }

    @Test
    public void eqList_secondNull() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Assert.assertFalse(CollectionUtils.eq(list, null));
    }

    @Test
    public void eqList_equal() {
        List<Integer> a = Arrays.asList(1, 2, 3);
        List<Integer> b = Arrays.asList(1, 2, 3);
        Assert.assertTrue(CollectionUtils.eq(a, b));
    }

    @Test
    public void eqList_notEqual() {
        List<Integer> a = Arrays.asList(1, 2, 3);
        List<Integer> b = Arrays.asList(1, 2, 4);
        Assert.assertFalse(CollectionUtils.eq(a, b));
    }

    // ============================================================
    // 5. eq(Set, Set) —— null guard
    // ============================================================

    @Test
    public void eqSet_bothNull() {
        Assert.assertTrue(CollectionUtils.eq((Set) null, (Set) null));
    }

    @Test
    public void eqSet_firstNull() {
        Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3));
        Assert.assertFalse(CollectionUtils.eq(null, set));
    }

    @Test
    public void eqSet_secondNull() {
        Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3));
        Assert.assertFalse(CollectionUtils.eq(set, null));
    }

    @Test
    public void eqSet_equal() {
        Set<Integer> a = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> b = new HashSet<>(Arrays.asList(3, 2, 1));
        Assert.assertTrue(CollectionUtils.eq(a, b));
    }

    @Test
    public void eqSet_notEqual() {
        Set<Integer> a = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> b = new HashSet<>(Arrays.asList(1, 2, 4));
        Assert.assertFalse(CollectionUtils.eq(a, b));
    }

    // ============================================================
    // 6. split(List, int) —— null guard
    // ============================================================

    @Test
    public void split_nullList() {
        List<List<Integer>> result = CollectionUtils.split(null, 3);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void split_normal() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<List<Integer>> result = CollectionUtils.split(list, 3);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(Arrays.asList(1, 2, 3), result.get(0));
        Assert.assertEquals(Arrays.asList(4, 5, 6), result.get(1));
        Assert.assertEquals(Arrays.asList(7, 8, 9), result.get(2));
        Assert.assertEquals(Arrays.asList(10), result.get(3));
    }

    @Test
    public void split_groupSizeLargerThanList() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        List<List<Integer>> result = CollectionUtils.split(list, 10);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(list, result.get(0));
    }

    // ============================================================
    // 7. getList(Collection, Function) —— null guard
    // ============================================================

    @Test
    public void getListFromCollection_nullCollection() {
        List<String> result = CollectionUtils.getList(null, String::valueOf);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getListFromCollection_nullMapper() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        // mapper is not guarded by the method, null -> NPE expected
        try {
            CollectionUtils.getList(list, null);
            Assert.fail("Expected NullPointerException for null mapper");
        } catch (NullPointerException e) {
            // expected: Stream.map(null) throws NPE
        }
    }

    // ============================================================
    // 8. toMap(Collection, Function) —— null guard
    // ============================================================

    @Test
    public void toMap_nullCollection() {
        Map<Object, Object> result = CollectionUtils.toMap(null, Function.identity());
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void toMap_duplicateKey() {
        List<String> list = Arrays.asList("a", "b", "a");
        Map<String, String> result = CollectionUtils.toMap(list, Function.identity());
        Assert.assertEquals(2, result.size());
        // mergeFunction is (a,b) -> b, so last writer wins
        Assert.assertEquals("a", result.get("a"));
        Assert.assertEquals("b", result.get("b"));
    }

    @Test
    public void toMap_normal() {
        List<String> list = Arrays.asList("x", "y", "z");
        Map<String, String> result = CollectionUtils.toMap(list, s -> "key-" + s);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("x", result.get("key-x"));
        Assert.assertEquals("y", result.get("key-y"));
        Assert.assertEquals("z", result.get("key-z"));
    }

    // ============================================================
    // 9. shuffle(List, int) —— null guard
    // ============================================================

    @Test
    public void shuffle_nullList() {
        List<Object> result = CollectionUtils.shuffle(null, 5);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void shuffle_customCount() {
        List<Integer> list = IntStream.range(0, 100)
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toList());
        List<Integer> result = CollectionUtils.shuffle(list, 50);
        Assert.assertEquals(100, result.size());
        // Verify same elements (content equality regardless of order)
        List<Integer> sortedOriginal = list.stream().sorted().collect(Collectors.toList());
        List<Integer> sortedResult = result.stream().sorted().collect(Collectors.toList());
        Assert.assertEquals(sortedOriginal, sortedResult);
        // Verify order is changed
        String originalStr = CollectionUtils.join(list);
        String resultStr = CollectionUtils.join(result);
        Assert.assertNotEquals(originalStr, resultStr);
    }
}
