package com.tingfeng.util.java.base.common;

import java.lang.reflect.Array;

import org.junit.Test;

public class BaseTest {
    @Test
    public void testStrings()
    {
        String [] strs = {};
        int [] ints = {1,2,5,4,3,0};
        Object [] objects = {};
         
        System.out.println(ints.getClass().isAssignableFrom(int [].class));
        System.out.println(strs.getClass().isAssignableFrom(String [].class));
        System.out.println(objects.getClass().isAssignableFrom(Object [].class));
        System.out.println(ints.getClass().isAssignableFrom(Object [].class));
        System.out.println(ints.getClass().isArray());
        System.out.println(Array.getLength(ints));
    }
}
