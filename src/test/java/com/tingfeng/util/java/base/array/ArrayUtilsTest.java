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
    public void testIsContain() {
        String[] array = {"a", "b", "c"};
        Assert.assertTrue(ArrayUtils.isContain(array, "a"));
        Assert.assertFalse(ArrayUtils.isContain(array, "d"));
        Assert.assertFalse(ArrayUtils.isContain(null, "a"));
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
}