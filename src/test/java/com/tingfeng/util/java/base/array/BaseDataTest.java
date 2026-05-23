package com.tingfeng.util.java.base.array;

import java.lang.reflect.Array;

import org.junit.Assert;
import org.junit.Test;

public class BaseDataTest {
    @Test
    public void testStrings()
    {
        String [] strs = {};
        int [] ints = {1,2,5,4,3,0};
        Object [] objects = {};
         
        Assert.assertTrue(ints.getClass().isAssignableFrom(int [].class));
        Assert.assertTrue(strs.getClass().isAssignableFrom(String [].class));
        Assert.assertTrue(objects.getClass().isAssignableFrom(Object [].class));
        Assert.assertFalse(ints.getClass().isAssignableFrom(Object [].class));
        Assert.assertTrue(ints.getClass().isArray());
        Assert.assertEquals(6, Array.getLength(ints));
    }
}
