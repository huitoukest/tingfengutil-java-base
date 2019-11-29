package com.tingfeng.util.java.base.common.utils;

import org.junit.Test;

public class TestUtilsTest {

    @Test
    public void test(){
        TestUtils.printTime(5,2,(i,j) -> System.out.println(i + "," + j));
    }

}