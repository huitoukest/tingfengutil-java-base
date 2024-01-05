package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;

public class ArrayUtilsTest {

    @Test
    public void shuffle() {
        int[] array = IntStream.range(0, 100).toArray();
        String oldValue = ArrayUtils.toString(array);
        ArrayUtils.shuffle(array);
        String newValue = ArrayUtils.toString(array);
        Assert.assertEquals(oldValue.length(), newValue.length());
        Assert.assertNotEquals(oldValue, newValue);
    }
}