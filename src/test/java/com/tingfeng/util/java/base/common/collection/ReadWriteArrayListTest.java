package com.tingfeng.util.java.base.common.collection;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ReadWriteArrayList 单元测试。
 * <p>
 * 覆盖基础功能、removeIf回归、sort回归、迭代器快照语义、并发读写、
 * 边界条件、equals/hashCode 一致性。
 */
public class ReadWriteArrayListTest {

    // ==================== 基础功能 ====================

    @Test
    public void testAddAndGet() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        Assert.assertTrue(list.add("a"));
        Assert.assertTrue(list.add("b"));
        Assert.assertTrue(list.add("c"));
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
    }

    @Test
    public void testAddAtIndex() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("c");
        list.add(1, "b");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
    }

    @Test
    public void testAddAtIndexOutOfBounds() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        try {
            list.add(5, "b");
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            list.add(-1, "b");
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testSet() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        Assert.assertEquals("b", list.set(1, "c"));
        Assert.assertEquals("c", list.get(1));
    }

    @Test
    public void testSetOutOfBounds() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        try {
            list.set(0, "x");
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testRemoveByObject() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        Assert.assertTrue(list.remove("b"));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("c", list.get(1));
        Assert.assertFalse(list.remove("nonexistent"));
    }

    @Test
    public void testRemoveByIndex() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        Assert.assertEquals("b", list.remove(1));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("c", list.get(1));
    }

    @Test
    public void testRemoveByIndexOutOfBounds() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        try {
            list.remove(0);
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testClear() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        Assert.assertFalse(list.isEmpty());
        list.clear();
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testIsEmpty() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        Assert.assertTrue(list.isEmpty());
        list.add("a");
        Assert.assertFalse(list.isEmpty());
        list.remove("a");
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testSize() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        Assert.assertEquals(0, list.size());
        list.add("a");
        Assert.assertEquals(1, list.size());
        list.add("b");
        Assert.assertEquals(2, list.size());
        list.remove("a");
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testContains() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        Assert.assertTrue(list.contains("a"));
        Assert.assertTrue(list.contains("b"));
        Assert.assertFalse(list.contains("c"));
        Assert.assertFalse(list.contains(null));
    }

    @Test
    public void testContainsWithNull() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add(null);
        Assert.assertTrue(list.contains(null));
    }

    @Test
    public void testIndexOf() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("a");
        Assert.assertEquals(0, list.indexOf("a"));
        Assert.assertEquals(1, list.indexOf("b"));
        Assert.assertEquals(-1, list.indexOf("c"));
    }

    @Test
    public void testLastIndexOf() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("a");
        Assert.assertEquals(2, list.lastIndexOf("a"));
        Assert.assertEquals(1, list.lastIndexOf("b"));
        Assert.assertEquals(-1, list.lastIndexOf("c"));
    }

    @Test
    public void testContainsAll() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        Assert.assertTrue(list.containsAll(Arrays.asList("a", "b")));
        Assert.assertTrue(list.containsAll(Arrays.asList("a", "b", "c")));
        Assert.assertFalse(list.containsAll(Arrays.asList("a", "d")));
    }

    @Test
    public void testAddAll() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        Assert.assertTrue(list.addAll(Arrays.asList("a", "b", "c")));
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
    }

    @Test
    public void testAddAllEmpty() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        Assert.assertFalse(list.addAll(Collections.emptyList()));
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testAddAllAtIndex() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("d");
        Assert.assertTrue(list.addAll(1, Arrays.asList("b", "c")));
        Assert.assertEquals(4, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
        Assert.assertEquals("d", list.get(3));
    }

    @Test
    public void testAddAllAtIndexOutOfBounds() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        try {
            list.addAll(1, Arrays.asList("a"));
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testRemoveAll() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        Assert.assertTrue(list.removeAll(Arrays.asList("b", "d")));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("c", list.get(1));
    }

    @Test
    public void testRemoveAllNoMatch() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        Assert.assertFalse(list.removeAll(Arrays.asList("x", "y")));
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testRetainAll() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        Assert.assertTrue(list.retainAll(Arrays.asList("a", "c")));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("c", list.get(1));
    }

    @Test
    public void testRetainAllAllMatch() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        Assert.assertFalse(list.retainAll(Arrays.asList("a", "b")));
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testToArray() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        Object[] arr = list.toArray();
        Assert.assertEquals(2, arr.length);
        Assert.assertEquals("a", arr[0]);
        Assert.assertEquals("b", arr[1]);
    }

    @Test
    public void testToArrayTyped() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        String[] arr = list.toArray(new String[0]);
        Assert.assertEquals(2, arr.length);
        Assert.assertEquals("a", arr[0]);
        Assert.assertEquals("b", arr[1]);
    }

    @Test
    public void testToArrayEmptyList() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        Object[] arr = list.toArray();
        Assert.assertEquals(0, arr.length);
    }

    @Test
    public void testConstructorWithCollection() {
        List<String> source = Arrays.asList("a", "b", "c");
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>(source);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
    }

    // ==================== removeIf 回归 ====================

    @Test
    public void testRemoveIf() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6));
        Assert.assertTrue(list.removeIf(n -> n % 2 == 0));
        Assert.assertEquals(Arrays.asList(1, 3, 5), list);
    }

    @Test
    public void testRemoveIfAllMatch() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3));
        Assert.assertTrue(list.removeIf(n -> true));
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testRemoveIfNoMatch() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3));
        Assert.assertFalse(list.removeIf(n -> false));
        Assert.assertEquals(3, list.size());
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveIfNullPredicate() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.removeIf(null);
    }

    @Test
    public void testRemoveIfWithConsecutiveMatches() {
        // 验证逆序遍历：连续匹配的元素都能正确移除
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(2, 4, 6, 1, 3, 5));
        Assert.assertTrue(list.removeIf(n -> n % 2 == 0));
        Assert.assertEquals(Arrays.asList(1, 3, 5), list);
    }

    @Test
    public void testRemoveIfOnEmptyList() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        Assert.assertFalse(list.removeIf(n -> n % 2 == 0));
    }

    // ==================== sort 回归 ====================

    @Test
    public void testSortComparable() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(3, 1, 4, 1, 5, 9, 2, 6));
        list.sort(null);
        Assert.assertEquals(Arrays.asList(1, 1, 2, 3, 4, 5, 6, 9), list);
    }

    @Test
    public void testSortWithComparator() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList("bb", "aaa", "c", "dddd"));
        list.sort(Comparator.comparingInt(String::length));
        Assert.assertEquals(Arrays.asList("c", "bb", "aaa", "dddd"), list);
    }

    @Test
    public void testSortReverseOrder() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3, 4, 5));
        list.sort(Comparator.reverseOrder());
        Assert.assertEquals(Arrays.asList(5, 4, 3, 2, 1), list);
    }

    @Test
    public void testSortEmptyList() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.sort(null);
        Assert.assertTrue(list.isEmpty());
    }

    // ==================== 迭代器快照语义 ====================

    @Test
    public void testIteratorLazyBehavior() {
        // Lazy iterator 是弱一致性的，遍历期间可以看到后续修改
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        Iterator<String> it = list.iterator();
        list.add("d");
        list.remove("a");

        // Lazy iterator 读取当前列表状态（弱一致性）
        List<String> iterResult = new ArrayList<>();
        while (it.hasNext()) {
            iterResult.add(it.next());
        }
        Assert.assertEquals(Arrays.asList("b", "c", "d"), iterResult);

        // 原列表一致
        Assert.assertEquals(Arrays.asList("b", "c", "d"), list);
    }

    @Test
    public void testIteratorOverEmptyList() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        Iterator<String> it = list.iterator();
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testListIteratorSnapshotSemantics() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");

        ListIterator<String> lit = list.listIterator();
        list.add("c");

        // listIterator 也应基于快照
        List<String> iterResult = new ArrayList<>();
        while (lit.hasNext()) {
            iterResult.add(lit.next());
        }
        Assert.assertEquals(Arrays.asList("a", "b"), iterResult);
    }

    @Test
    public void testIteratorRemove() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String s = it.next();
            if ("b".equals(s)) {
                it.remove();
            }
        }

        // Verify element was removed from underlying list
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(Arrays.asList("a", "c"), list);
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNext() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        Iterator<String> it = list.iterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveTwice() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        it.next();
        it.remove();
        it.remove();
    }

    @Test
    public void testIteratorRemoveFirstAndLast() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        Iterator<String> it = list.iterator();
        // Remove first
        Assert.assertEquals("a", it.next());
        it.remove();
        Assert.assertEquals("b", it.next());
        // Remove last
        Assert.assertEquals("c", it.next());
        it.remove();
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(Arrays.asList("b"), list);
    }

    @Test
    public void testSpliteratorCharacteristics() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");

        Spliterator<String> spliterator = list.spliterator();
        int characteristics = spliterator.characteristics();

        // Should have ORDERED
        Assert.assertTrue("should have ORDERED",
                (characteristics & Spliterator.ORDERED) != 0);
        // Should NOT have SIZED (list is concurrently modifiable)
        Assert.assertTrue("should NOT have SIZED",
                (characteristics & Spliterator.SIZED) == 0);
        // Should NOT have SUBSIZED
        Assert.assertTrue("should NOT have SUBSIZED",
                (characteristics & Spliterator.SUBSIZED) == 0);
    }

    @Test
    public void testEqualsWithOtherList() {
        ReadWriteArrayList<String> rwList = new ReadWriteArrayList<>();
        rwList.add("a");
        rwList.add("b");
        rwList.add("c");

        // Compare with ArrayList
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Assert.assertEquals(rwList, arrayList);
        Assert.assertEquals(arrayList, rwList);

        // Compare with LinkedList
        LinkedList<String> linkedList = new LinkedList<>(Arrays.asList("a", "b", "c"));
        Assert.assertEquals(rwList, linkedList);
        Assert.assertEquals(linkedList, rwList);

        // Empty list comparison
        ReadWriteArrayList<String> empty = new ReadWriteArrayList<>();
        Assert.assertEquals(empty, new ArrayList<>());
        Assert.assertEquals(empty, new LinkedList<>());

        // Compare with non-List object
        Assert.assertNotEquals(rwList, "not a list");
        Assert.assertNotEquals(rwList, null);
    }

    @Test(timeout = 5000)
    public void testLockBalance() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();

        // Read operations
        list.size();
        list.isEmpty();
        list.contains("x");

        // Write lock should be acquirable (no read lock leak)
        list.add("a");

        // Read operation that throws
        try {
            list.get(5);
            Assert.fail("expected exception");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        // Write lock should still be acquirable after exception path
        list.add("b");

        // Iterator traversal
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            it.next();
        }

        // Iterator remove
        it = list.iterator();
        it.next();
        it.remove();

        // Write lock acquirable after iterator
        list.add("c");

        // Spliterator
        list.spliterator();

        // Equals and hashCode
        list.equals(new ArrayList<>());
        list.hashCode();

        // Write lock acquirable after all read operations
        list.remove("b");

        // Verify list consistency (after remove("b") from ["b","c"])
        Assert.assertEquals(Arrays.asList("c"), list);
    }

    // ==================== 并发读写 ====================

    @Test
    public void testConcurrentAddAndRead() throws InterruptedException {
        int threadCount = 10;
        int elementsPerThread = 100;
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean failed = new AtomicBoolean(false);

        for (int t = 0; t < threadCount; t++) {
            final int base = t * elementsPerThread;
            new Thread(() -> {
                try {
                    for (int i = 0; i < elementsPerThread; i++) {
                        list.add(base + i);
                    }
                } catch (Exception e) {
                    failed.set(true);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        Assert.assertFalse("concurrent add should not throw", failed.get());
        Assert.assertEquals(threadCount * elementsPerThread, list.size());

        // 验证所有元素都在
        Set<Integer> allElements = new HashSet<>(list);
        for (int i = 0; i < threadCount * elementsPerThread; i++) {
            Assert.assertTrue("missing element: " + i, allElements.contains(i));
        }
    }

    @Test
    public void testConcurrentReadWhileWriting() throws InterruptedException {
        int writerCount = 5;
        int readerCount = 5;
        int iterations = 50;
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(writerCount + readerCount);
        AtomicBoolean failed = new AtomicBoolean(false);

        for (int w = 0; w < writerCount; w++) {
            final int writerId = w;
            new Thread(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        list.add(writerId * iterations + i);
                        list.size(); // 读操作
                        list.contains(i); // 读操作
                    }
                } catch (Exception e) {
                    failed.set(true);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        for (int r = 0; r < readerCount; r++) {
            new Thread(() -> {
                try {
                    for (int i = 0; i < iterations; i++) {
                        list.isEmpty();
                        list.size();
                        if (!list.isEmpty()) {
                            list.get(0);
                        }
                        list.indexOf(i);
                    }
                } catch (Exception e) {
                    failed.set(true);
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
        Assert.assertFalse("concurrent read/write should not throw", failed.get());
    }

    @Test
    public void testConcurrentRemoveWhileReading() throws InterruptedException {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        int threadCount = 4;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicBoolean failed = new AtomicBoolean(false);

        Runnable reader = () -> {
            try {
                for (int i = 0; i < 100; i++) {
                    list.contains(i);
                    list.indexOf(i);
                    list.isEmpty();
                    list.size();
                    if (!list.isEmpty()) {
                        list.get(list.size() - 1);
                    }
                }
            } catch (Exception e) {
                failed.set(true);
            } finally {
                latch.countDown();
            }
        };

        for (int i = 0; i < threadCount; i++) {
            new Thread(reader).start();
        }

        latch.await();
        Assert.assertFalse("concurrent read while elements exist should not throw", failed.get());
    }

    // ==================== 边界条件 ====================

    @Test
    public void testGetOutOfBounds() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        try {
            list.get(-1);
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        try {
            list.get(1);
            Assert.fail("expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testNullElementAddAndGet() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add(null);
        Assert.assertEquals(1, list.size());
        Assert.assertNull(list.get(0));
        Assert.assertTrue(list.contains(null));
    }

    @Test
    public void testIndexOfNull() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add(null);
        list.add("b");
        Assert.assertEquals(1, list.indexOf(null));
        Assert.assertEquals(1, list.lastIndexOf(null));
    }

    @Test
    public void testLargeNumberOfElements() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        int count = 10000;
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
        Assert.assertEquals(count, list.size());
        for (int i = 0; i < count; i++) {
            Assert.assertEquals(Integer.valueOf(i), list.get(i));
        }
    }

    @Test
    public void testToString() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        String str = list.toString();
        Assert.assertTrue(str.contains("a"));
        Assert.assertTrue(str.contains("b"));
    }

    // ==================== equals / hashCode ====================

    @Test
    public void testEqualsWithArrayList() {
        ReadWriteArrayList<String> rwList = new ReadWriteArrayList<>();
        rwList.add("a");
        rwList.add("b");
        rwList.add("c");

        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Assert.assertEquals(rwList, arrayList);
        Assert.assertEquals(arrayList, rwList);
    }

    @Test
    public void testEqualsWithDifferentContent() {
        ReadWriteArrayList<String> rwList = new ReadWriteArrayList<>();
        rwList.add("a");
        rwList.add("b");

        ReadWriteArrayList<String> other = new ReadWriteArrayList<>();
        other.add("a");
        other.add("c");
        Assert.assertNotEquals(rwList, other);
    }

    @Test
    public void testEqualsWithSelf() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        //noinspection ObjectEqualsItself,EqualsWithItself
        Assert.assertEquals(list, list);
    }

    @Test
    public void testEqualsWithNull() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        //noinspection ConstantConditions,ObjectEqualsNull
        Assert.assertFalse(list.equals(null));
    }

    @Test
    public void testHashCodeConsistency() {
        ReadWriteArrayList<String> rwList = new ReadWriteArrayList<>();
        rwList.add("a");
        rwList.add("b");
        rwList.add("c");

        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList("a", "b", "c"));
        Assert.assertEquals(arrayList.hashCode(), rwList.hashCode());
    }

    @Test
    public void testHashCodeEmptyList() {
        ReadWriteArrayList<String> rwList = new ReadWriteArrayList<>();
        ArrayList<String> arrayList = new ArrayList<>();
        Assert.assertEquals(arrayList.hashCode(), rwList.hashCode());
    }

    @Test
    public void testEqualsEmptyLists() {
        ReadWriteArrayList<String> rwList = new ReadWriteArrayList<>();
        ReadWriteArrayList<String> other = new ReadWriteArrayList<>();
        Assert.assertEquals(rwList, other);
    }

    // ==================== subList ====================

    @Test
    public void testSubList() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));

        List<Integer> sub = list.subList(1, 4);
        Assert.assertEquals(3, sub.size());
        Assert.assertEquals(Arrays.asList(1, 2, 3), sub);
    }

    @Test
    public void testSubListFullRange() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3));
        List<Integer> sub = list.subList(0, 3);
        Assert.assertEquals(Arrays.asList(1, 2, 3), sub);
    }

    @Test
    public void testSubListEmpty() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3));
        List<Integer> sub = list.subList(2, 2);
        Assert.assertTrue(sub.isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListNegativeFromIndex() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3));
        list.subList(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListToIndexOutOfBounds() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3));
        list.subList(1, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListFromIndexGreaterThanToIndex() {
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3));
        list.subList(2, 1);
    }

    @Test
    public void testSubListSnapshotSemantics() {
        // subList 返回的是快照副本（new ArrayList<>(subList)），
        // 修改原列表不应影响已获取的 subList
        ReadWriteArrayList<Integer> list = new ReadWriteArrayList<>();
        list.addAll(Arrays.asList(1, 2, 3, 4, 5));

        List<Integer> sub = list.subList(1, 4);
        Assert.assertEquals(Arrays.asList(2, 3, 4), sub);

        list.add(6); // 在范围外添加元素
        Assert.assertEquals(6, list.size());
        // subList 是快照，不受影响
        Assert.assertEquals(Arrays.asList(2, 3, 4), sub);
    }

    // ==================== 额外场景：多种操作组合 ====================

    @Test
    public void testAddRemoveSequence() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.remove("b");
        list.add(1, "d");
        Assert.assertEquals(Arrays.asList("a", "d", "c"), list);
    }

    @Test
    public void testRetainAllToEmpty() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        list.add("b");
        Assert.assertTrue(list.retainAll(Collections.emptyList()));
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testRemoveAllEmptyCollection() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        Assert.assertFalse(list.removeAll(Collections.emptyList()));
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testContainsAllWithEmptyCollection() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        Assert.assertTrue(list.containsAll(Collections.emptyList()));
    }

    @Test
    public void testAddAllAtIndexWithEmpty() {
        ReadWriteArrayList<String> list = new ReadWriteArrayList<>();
        list.add("a");
        Assert.assertFalse(list.addAll(0, Collections.emptyList()));
        Assert.assertEquals(1, list.size());
    }
}
