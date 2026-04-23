package com.tingfeng.util.java.base.array;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ArrayUtilsTest {

    @Test
    public void testContains() {
        String[] array = {"a", "b", "c"};
        Assert.assertTrue(ArrayUtils.contains(array, "a"));
        Assert.assertFalse(ArrayUtils.contains(array, "d"));
        Assert.assertFalse(ArrayUtils.contains(null, "a"));
    }

    @Test
    public void testGetArray() {
        Integer[] intArray = {1, 2, 3};
        String[] stringArray = ArrayUtils.getArray(intArray, String.class, String::valueOf);
        Assert.assertEquals(3, stringArray.length);
        Assert.assertEquals("1", stringArray[0]);
        Assert.assertEquals("2", stringArray[1]);
        Assert.assertEquals("3", stringArray[2]);
    }

    @Test
    public void testGetBytesByInputStream() throws IOException {
        byte[] expected = {1, 2, 3, 4, 5};
        InputStream in = new ByteArrayInputStream(expected);
        byte[] actual = ArrayUtils.getBytesByInputStream(in);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testConcatArray() {
        Object[] target = new Object[6];
        List<Object[]> srcArray = new ArrayList<>();
        srcArray.add(new Object[]{1, 2});
        srcArray.add(new Object[]{3, 4});
        srcArray.add(new Object[]{5, 6});
        ArrayUtils.concatArray(target, srcArray);
        Assert.assertEquals(1, target[0]);
        Assert.assertEquals(2, target[1]);
        Assert.assertEquals(3, target[2]);
        Assert.assertEquals(4, target[3]);
        Assert.assertEquals(5, target[4]);
        Assert.assertEquals(6, target[5]);
    }

    @Test
    public void testReverseObjectArray() {
        Object[] array = {1, 2, 3, 4, 5};
        ArrayUtils.reverse(array);
        Assert.assertEquals(5, array[0]);
        Assert.assertEquals(4, array[1]);
        Assert.assertEquals(3, array[2]);
        Assert.assertEquals(2, array[3]);
        Assert.assertEquals(1, array[4]);
    }

    @Test
    public void testReverseCharArray() {
        char[] array = {'a', 'b', 'c', 'd', 'e'};
        ArrayUtils.reverse(array);
        Assert.assertEquals('e', array[0]);
        Assert.assertEquals('d', array[1]);
        Assert.assertEquals('c', array[2]);
        Assert.assertEquals('b', array[3]);
        Assert.assertEquals('a', array[4]);
    }

    @Test
    public void testReverseIntArray() {
        int[] array = {1, 2, 3, 4, 5};
        ArrayUtils.reverse(array);
        Assert.assertEquals(5, array[0]);
        Assert.assertEquals(4, array[1]);
        Assert.assertEquals(3, array[2]);
        Assert.assertEquals(2, array[3]);
        Assert.assertEquals(1, array[4]);
    }

    @Test
    public void testReverseLongArray() {
        long[] array = {1L, 2L, 3L, 4L, 5L};
        ArrayUtils.reverse(array);
        Assert.assertEquals(5L, array[0]);
        Assert.assertEquals(4L, array[1]);
        Assert.assertEquals(3L, array[2]);
        Assert.assertEquals(2L, array[3]);
        Assert.assertEquals(1L, array[4]);
    }

    @Test
    public void testReverseFloatArray() {
        float[] array = {1.1f, 2.2f, 3.3f, 4.4f, 5.5f};
        ArrayUtils.reverse(array);
        Assert.assertEquals(5.5f, array[0], 0.001);
        Assert.assertEquals(4.4f, array[1], 0.001);
        Assert.assertEquals(3.3f, array[2], 0.001);
        Assert.assertEquals(2.2f, array[3], 0.001);
        Assert.assertEquals(1.1f, array[4], 0.001);
    }

    @Test
    public void testReverseDoubleArray() {
        double[] array = {1.1, 2.2, 3.3, 4.4, 5.5};
        ArrayUtils.reverse(array);
        Assert.assertEquals(5.5, array[0], 0.001);
        Assert.assertEquals(4.4, array[1], 0.001);
        Assert.assertEquals(3.3, array[2], 0.001);
        Assert.assertEquals(2.2, array[3], 0.001);
        Assert.assertEquals(1.1, array[4], 0.001);
    }

    @Test
    public void testGetFirst() {
        String[] array = {"a", "b", "c"};
        Assert.assertEquals("a", ArrayUtils.getFirst(array));
        Assert.assertNull(ArrayUtils.getFirst(null));
        Assert.assertNull(ArrayUtils.getFirst(new String[0]));
    }

    @Test
    public void testGetLast() {
        String[] array = {"a", "b", "c"};
        Assert.assertEquals("c", ArrayUtils.getLast(array));
        Assert.assertNull(ArrayUtils.getLast(null));
        Assert.assertNull(ArrayUtils.getLast(new String[0]));
    }

    @Test
    public void testShuffleIntArray() {
        int[] array = IntStream.range(0, 100).toArray();
        String oldValue = ArrayUtils.toString(array);
        ArrayUtils.shuffle(array);
        String newValue = ArrayUtils.toString(array);
        Assert.assertEquals(oldValue.length(), newValue.length());
        Assert.assertNotEquals(oldValue, newValue);
    }

    @Test
    public void testShuffleLongArray() {
        long[] array = {1L, 2L, 3L, 4L, 5L};
        String oldValue = ArrayUtils.toString(array);
        ArrayUtils.shuffle(array);
        String newValue = ArrayUtils.toString(array);
        Assert.assertEquals(oldValue.length(), newValue.length());
    }

    @Test
    public void testShuffleCharArray() {
        char[] array = {'a', 'b', 'c', 'd', 'e'};
        String oldValue = ArrayUtils.toString(array);
        ArrayUtils.shuffle(array);
        String newValue = ArrayUtils.toString(array);
        Assert.assertEquals(oldValue.length(), newValue.length());
    }

    @Test
    public void testShuffleBooleanArray() {
        boolean[] array = {true, false, true, false};
        String oldValue = ArrayUtils.toString(array);
        ArrayUtils.shuffle(array);
        String newValue = ArrayUtils.toString(array);
        Assert.assertEquals(oldValue.length(), newValue.length());
    }

    @Test
    public void testShuffleByteArray() {
        byte[] array = {1, 2, 3, 4, 5};
        String oldValue = ArrayUtils.toString(array);
        ArrayUtils.shuffle(array);
        String newValue = ArrayUtils.toString(array);
        Assert.assertEquals(oldValue.length(), newValue.length());
    }

    @Test
    public void testShuffleObjectArray() {
        String[] array = {"a", "b", "c", "d", "e"};
        String oldValue = ArrayUtils.toString(array);
        ArrayUtils.shuffle(array);
        String newValue = ArrayUtils.toString(array);
        Assert.assertEquals(oldValue.length(), newValue.length());
    }

    @Test
    public void testJoinByteArray() {
        byte[] array = {1, 2, 3, 4, 5};
        String result = ArrayUtils.join(array, ",");
        Assert.assertEquals("1,2,3,4,5", result);
        Assert.assertNull(ArrayUtils.join((byte[]) null, ","));
    }

    @Test
    public void testJoinIntArray() {
        int[] array = {1, 2, 3, 4, 5};
        String result = ArrayUtils.join(array, ",");
        Assert.assertEquals("1,2,3,4,5", result);
        Assert.assertNull(ArrayUtils.join((int[]) null, ","));
    }

    @Test
    public void testJoinLongArray() {
        long[] array = {1L, 2L, 3L, 4L, 5L};
        String result = ArrayUtils.join(array, ",");
        Assert.assertEquals("1,2,3,4,5", result);
        Assert.assertNull(ArrayUtils.join((long[]) null, ","));
    }

    @Test
    public void testJoinBooleanArray() {
        boolean[] array = {true, false, true};
        String result = ArrayUtils.join(array, ",");
        Assert.assertEquals("true,false,true", result);
        Assert.assertNull(ArrayUtils.join((Boolean[]) null, ","));
    }

    @Test
    public void testJoinCharArray() {
        char[] array = {'a', 'b', 'c'};
        String result = ArrayUtils.join(array, ",");
        Assert.assertEquals("a,b,c", result);
        Assert.assertNull(ArrayUtils.join((char[]) null, ","));
    }

    @Test
    public void testJoinObjectArray() {
        String[] array = {"a", "b", "c"};
        String result = ArrayUtils.join(array, ",");
        Assert.assertEquals("a,b,c", result);
        Assert.assertNull(ArrayUtils.join((String[]) null, ","));
    }

    @Test
    public void testJoinObjectArrayWithDefaultSeparator() {
        String[] array = {"a", "b", "c"};
        String result = ArrayUtils.join(array);
        Assert.assertEquals("a,b,c", result);
    }

    @Test
    public void testToStringByteArray() {
        byte[] array = {1, 2, 3};
        String result = ArrayUtils.toString(array);
        Assert.assertEquals("1,2,3", result);
        Assert.assertNull(ArrayUtils.toString((byte[]) null));
    }

    @Test
    public void testToStringIntArray() {
        int[] array = {1, 2, 3};
        String result = ArrayUtils.toString(array);
        Assert.assertEquals("1,2,3", result);
        Assert.assertNull(ArrayUtils.toString((int[]) null));
    }

    @Test
    public void testToStringLongArray() {
        long[] array = {1L, 2L, 3L};
        String result = ArrayUtils.toString(array);
        Assert.assertEquals("1,2,3", result);
        Assert.assertNull(ArrayUtils.toString((long[]) null));
    }

    @Test
    public void testToStringBooleanArray() {
        boolean[] array = {true, false, true};
        String result = ArrayUtils.toString(array);
        Assert.assertEquals("true,false,true", result);
        Assert.assertNull(ArrayUtils.toString((boolean[]) null));
    }

    @Test
    public void testToStringCharArray() {
        char[] array = {'a', 'b', 'c'};
        String result = ArrayUtils.toString(array);
        Assert.assertEquals("a,b,c", result);
        Assert.assertNull(ArrayUtils.toString((char[]) null));
    }

    @Test
    public void testToStringObjectArray() {
        String[] array = {"a", "b", "c"};
        String result = ArrayUtils.toString(array);
        Assert.assertEquals("a,b,c", result);
        Assert.assertNull(ArrayUtils.toString((String[]) null));
    }

    // ==================== 新增方法测试 ====================

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(ArrayUtils.isEmpty(null));
        Assert.assertTrue(ArrayUtils.isEmpty(new String[0]));
        Assert.assertFalse(ArrayUtils.isEmpty(new String[]{"a"}));
    }

    @Test
    public void testIsNotEmpty() {
        Assert.assertFalse(ArrayUtils.isNotEmpty(null));
        Assert.assertFalse(ArrayUtils.isNotEmpty(new String[0]));
        Assert.assertTrue(ArrayUtils.isNotEmpty(new String[]{"a"}));
    }

    @Test
    public void testIndexOf() {
        String[] array = {"a", "b", "c", "b"};
        Assert.assertEquals(0, ArrayUtils.indexOf(array, "a"));
        Assert.assertEquals(1, ArrayUtils.indexOf(array, "b"));
        Assert.assertEquals(-1, ArrayUtils.indexOf(array, "d"));
        Assert.assertEquals(-1, ArrayUtils.indexOf(null, "a"));
    }

    @Test
    public void testLastIndexOf() {
        String[] array = {"a", "b", "c", "b"};
        Assert.assertEquals(3, ArrayUtils.lastIndexOf(array, "b"));
        Assert.assertEquals(0, ArrayUtils.lastIndexOf(array, "a"));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(array, "d"));
        Assert.assertEquals(-1, ArrayUtils.lastIndexOf(null, "a"));
    }

    @Test
    public void testFilter() {
        Integer[] array = {1, 2, 3, 4, 5};
        Integer[] result = ArrayUtils.filter(array, i -> i % 2 == 0);
        Assert.assertEquals(2, result.length);
        Assert.assertEquals(Integer.valueOf(2), result[0]);
        Assert.assertEquals(Integer.valueOf(4), result[1]);
        Assert.assertNull(ArrayUtils.filter(null, i -> true));
        Assert.assertNull(ArrayUtils.filter(array, null));
    }

    @Test
    public void testMap() {
        Integer[] array = {1, 2, 3};
        String[] result = ArrayUtils.map(array, String::valueOf, String[]::new);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals("1", result[0]);
        Assert.assertEquals("2", result[1]);
        Assert.assertEquals("3", result[2]);
        Assert.assertNull(ArrayUtils.map(null, String::valueOf, String[]::new));
        Assert.assertNull(ArrayUtils.map(array, null, String[]::new));
    }

    @Test
    public void testSubArrayGeneric() {
        String[] array = {"a", "b", "c", "d", "e"};
        String[] result = ArrayUtils.subArray(array, 1, 4);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals("b", result[0]);
        Assert.assertEquals("c", result[1]);
        Assert.assertEquals("d", result[2]);
        // 边界测试：负数start自动修正为0，end超过长度自动截断
        Assert.assertEquals(0, ArrayUtils.subArray(array, 1, 1).length);
        Assert.assertEquals(2, ArrayUtils.subArray(array, -1, 2).length);  // start修正为0
        Assert.assertEquals(5, ArrayUtils.subArray(array, 0, 10).length); // end截断为5
        Assert.assertNull(ArrayUtils.subArray((String[]) null, 0, 2));
    }

    @Test
    public void testSubArrayInt() {
        int[] array = {1, 2, 3, 4, 5};
        int[] result = ArrayUtils.subArray(array, 1, 4);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals(2, result[0]);
        Assert.assertEquals(3, result[1]);
        Assert.assertEquals(4, result[2]);
    }

    @Test
    public void testSubArrayLong() {
        long[] array = {1L, 2L, 3L, 4L, 5L};
        long[] result = ArrayUtils.subArray(array, 1, 4);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals(2L, result[0]);
    }

    @Test
    public void testSubArrayByte() {
        byte[] array = {1, 2, 3, 4, 5};
        byte[] result = ArrayUtils.subArray(array, 1, 4);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals(2, result[0]);
    }

    @Test
    public void testSubArrayChar() {
        char[] array = {'a', 'b', 'c', 'd', 'e'};
        char[] result = ArrayUtils.subArray(array, 1, 4);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals('b', result[0]);
    }

    @Test
    public void testSubArrayDouble() {
        double[] array = {1.1, 2.2, 3.3, 4.4, 5.5};
        double[] result = ArrayUtils.subArray(array, 1, 4);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals(2.2, result[0], 0.001);
    }

    @Test
    public void testSubArrayFloat() {
        float[] array = {1.1f, 2.2f, 3.3f, 4.4f, 5.5f};
        float[] result = ArrayUtils.subArray(array, 1, 4);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals(2.2f, result[0], 0.001);
    }

    @Test
    public void testSubArrayBoolean() {
        boolean[] array = {true, false, true, false, true};
        boolean[] result = ArrayUtils.subArray(array, 1, 4);
        Assert.assertEquals(3, result.length);
        Assert.assertFalse(result[0]);
    }

    @Test
    public void testSubArrayShort() {
        short[] array = {1, 2, 3, 4, 5};
        short[] result = ArrayUtils.subArray(array, 1, 4);
        Assert.assertEquals(3, result.length);
        Assert.assertEquals(2, result[0]);
    }

    @Test
    public void testUnwrapperBoolean() {
        Boolean[] array = {true, null, false, true};
        boolean[] result = ArrayUtils.unwrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertTrue(result[0]);
        Assert.assertFalse(result[1]);
        Assert.assertFalse(result[2]);
        Assert.assertTrue(result[3]);
        Assert.assertNull(ArrayUtils.unwrapper((Boolean[]) null));
    }

    @Test
    public void testUnwrapperChar() {
        Character[] array = {'a', null, 'c', 'd'};
        char[] result = ArrayUtils.unwrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals('a', result[0]);
        Assert.assertEquals('\0', result[1]);
        Assert.assertEquals('c', result[2]);
        Assert.assertNull(ArrayUtils.unwrapper((Character[]) null));
    }

    @Test
    public void testUnwrapperByte() {
        Byte[] array = {1, null, 3, 4};
        byte[] result = ArrayUtils.unwrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(1, result[0]);
        Assert.assertEquals(0, result[1]);
        Assert.assertNull(ArrayUtils.unwrapper((Byte[]) null));
    }

    @Test
    public void testUnwrapperShort() {
        Short[] array = {1, null, 3, 4};
        short[] result = ArrayUtils.unwrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(1, result[0]);
        Assert.assertEquals(0, result[1]);
        Assert.assertNull(ArrayUtils.unwrapper((Short[]) null));
    }

    @Test
    public void testUnwrapperInt() {
        Integer[] array = {1, null, 3, 4};
        int[] result = ArrayUtils.unwrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(1, result[0]);
        Assert.assertEquals(0, result[1]);
        Assert.assertNull(ArrayUtils.unwrapper((Integer[]) null));
    }

    @Test
    public void testUnwrapperLong() {
        Long[] array = {1L, null, 3L, 4L};
        long[] result = ArrayUtils.unwrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(1L, result[0]);
        Assert.assertEquals(0L, result[1]);
        Assert.assertNull(ArrayUtils.unwrapper((Long[]) null));
    }

    @Test
    public void testUnwrapperFloat() {
        Float[] array = {1.1f, null, 3.3f, 4.4f};
        float[] result = ArrayUtils.unwrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(1.1f, result[0], 0.001);
        Assert.assertEquals(0f, result[1], 0.001);
        Assert.assertNull(ArrayUtils.unwrapper((Float[]) null));
    }

    @Test
    public void testUnwrapperDouble() {
        Double[] array = {1.1, null, 3.3, 4.4};
        double[] result = ArrayUtils.unwrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(1.1, result[0], 0.001);
        Assert.assertEquals(0d, result[1], 0.001);
        Assert.assertNull(ArrayUtils.unwrapper((Double[]) null));
    }

    @Test
    public void testWrapperByte() {
        byte[] array = {1, 2, 3, 4};
        Byte[] result = ArrayUtils.wrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(Byte.valueOf((byte)1), result[0]);
        Assert.assertEquals(Byte.valueOf((byte)2), result[1]);
        Assert.assertNull(ArrayUtils.wrapper((byte[]) null));
    }

    @Test
    public void testWrapperShort() {
        short[] array = {1, 2, 3, 4};
        Short[] result = ArrayUtils.wrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(Short.valueOf((short)1), result[0]);
        Assert.assertEquals(Short.valueOf((short)2), result[1]);
        Assert.assertNull(ArrayUtils.wrapper((short[]) null));
    }

    @Test
    public void testWrapperInt() {
        int[] array = {1, 2, 3, 4};
        Integer[] result = ArrayUtils.wrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(Integer.valueOf(1), result[0]);
        Assert.assertEquals(Integer.valueOf(2), result[1]);
        Assert.assertNull(ArrayUtils.wrapper((int[]) null));
    }

    @Test
    public void testWrapperLong() {
        long[] array = {1L, 2L, 3L, 4L};
        Long[] result = ArrayUtils.wrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(Long.valueOf(1L), result[0]);
        Assert.assertEquals(Long.valueOf(2L), result[1]);
        Assert.assertNull(ArrayUtils.wrapper((long[]) null));
    }

    @Test
    public void testWrapperFloat() {
        float[] array = {1.1f, 2.2f, 3.3f, 4.4f};
        Float[] result = ArrayUtils.wrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(Float.valueOf(1.1f), result[0]);
        Assert.assertEquals(Float.valueOf(2.2f), result[1]);
        Assert.assertNull(ArrayUtils.wrapper((float[]) null));
    }

    @Test
    public void testWrapperDouble() {
        double[] array = {1.1, 2.2, 3.3, 4.4};
        Double[] result = ArrayUtils.wrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(Double.valueOf(1.1), result[0]);
        Assert.assertEquals(Double.valueOf(2.2), result[1]);
        Assert.assertNull(ArrayUtils.wrapper((double[]) null));
    }

    @Test
    public void testWrapperBoolean() {
        boolean[] array = {true, false, true, false};
        Boolean[] result = ArrayUtils.wrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(Boolean.TRUE, result[0]);
        Assert.assertEquals(Boolean.FALSE, result[1]);
        Assert.assertNull(ArrayUtils.wrapper((boolean[]) null));
    }

    @Test
    public void testWrapperChar() {
        char[] array = {'a', 'b', 'c', 'd'};
        Character[] result = ArrayUtils.wrapper(array);
        Assert.assertEquals(4, result.length);
        Assert.assertEquals(Character.valueOf('a'), result[0]);
        Assert.assertEquals(Character.valueOf('b'), result[1]);
        Assert.assertNull(ArrayUtils.wrapper((char[]) null));
    }

    // ==================== 统计方法测试 ====================

    @Test
    public void testSumInt() {
        int[] array = {1, 2, 3, 4, 5};
        Assert.assertEquals(15L, ArrayUtils.sum(array));
        Assert.assertEquals(0L, ArrayUtils.sum((int[]) null));
        Assert.assertEquals(0L, ArrayUtils.sum(new int[0]));
    }

    @Test
    public void testSumLong() {
        long[] array = {1L, 2L, 3L, 4L, 5L};
        Assert.assertEquals(15L, ArrayUtils.sum(array));
    }

    @Test
    public void testSumDouble() {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        Assert.assertEquals(15.0, ArrayUtils.sum(array), 0.001);
    }

    @Test
    public void testSumFloat() {
        float[] array = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        Assert.assertEquals(15.0, ArrayUtils.sum(array), 0.001);
    }

    @Test
    public void testAverageInt() {
        int[] array = {1, 2, 3, 4, 5};
        Assert.assertEquals(3.0, ArrayUtils.average(array), 0.001);
        Assert.assertEquals(0d, ArrayUtils.average((int[]) null), 0.001);
        Assert.assertEquals(0d, ArrayUtils.average(new int[0]), 0.001);
    }

    @Test
    public void testAverageDouble() {
        double[] array = {1.0, 2.0, 3.0, 4.0, 5.0};
        Assert.assertEquals(3.0, ArrayUtils.average(array), 0.001);
    }

    @Test
    public void testAverageFloat() {
        float[] array = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        Assert.assertEquals(3.0, ArrayUtils.average(array), 0.001);
    }
}