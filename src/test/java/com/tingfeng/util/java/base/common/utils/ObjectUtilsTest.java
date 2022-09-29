package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * @Author wangGang
 * @Description //TODO
 * @Date 2019-04-02 14:44
 **/
public class ObjectUtilsTest {

    @Test
    public void equalsValueTest(){
        Assert.assertTrue(ObjectUtils.valueEquals(1,new Long(1)));
        Assert.assertTrue(ObjectUtils.valueEquals(new Integer(2),new Long(2)));
        Assert.assertTrue(ObjectUtils.valueEquals(new Float(1.0),new Float(1.0)));
        Assert.assertTrue(ObjectUtils.valueEquals(new Float(2.0),new Double(2.00)));
        Assert.assertTrue(ObjectUtils.valueEquals(new Float(2.0),"2.0"));
        Assert.assertTrue(ObjectUtils.valueEquals(2.0,"2.0"));
        Assert.assertFalse(ObjectUtils.valueEquals(2.0,"2.00"));
    }


    @Test
    public void isAllEmptyTest(){
        Assert.assertFalse(ObjectUtils.isAllEmpty(false,null,new String[]{}," "));
        Assert.assertTrue(ObjectUtils.isAllEmpty(true,null,new String[]{}," "));
    }

    @Test
    public void isEmptyTest(){
        Assert.assertFalse(ObjectUtils.isEmpty(Arrays.asList("",new Object[]{1}),true,true));
        Assert.assertTrue(ObjectUtils.isEmpty(Arrays.asList("",new Object[]{Collections.EMPTY_LIST}),true,true));
        Assert.assertTrue(ObjectUtils.isEmpty(Arrays.asList("",new Object[]{Collections.EMPTY_LIST,"  "}),true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(Arrays.asList("",new Object[]{Collections.EMPTY_LIST,"  "}),true,false));
        Assert.assertFalse(ObjectUtils.isEmpty(Arrays.asList("",new Object[]{Collections.EMPTY_LIST,"  "}),false,true));
        Assert.assertFalse(ObjectUtils.isEmpty(Arrays.asList(""),false,true));
        Assert.assertTrue(ObjectUtils.isEmpty("  ",true,true));
        Assert.assertTrue(ObjectUtils.isEmpty(Optional.of("  "),true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(Optional.of("  "),false,true));
        Assert.assertFalse(ObjectUtils.isEmpty(Optional.of("  "),true,false));
        Assert.assertTrue(ObjectUtils.isEmpty(Arrays.asList(Optional.of("  ")),true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(Arrays.asList(Optional.of("  ")),true,false));
    }
    @Test
    public void isEmptyBaseDataTest(){
        Assert.assertFalse(ObjectUtils.isEmpty(new int[][]{new int[]{1}},true,true));
        Assert.assertTrue(ObjectUtils.isEmpty(new int[][]{},true,true));
        Assert.assertTrue(ObjectUtils.isEmpty(new int[][][]{new int[][]{}},true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(new int[][][]{new int[][]{new int[]{1}}},true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(new byte[][]{new byte[]{1}},true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(new short[][]{new short[]{1}},true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(new long[][]{new long[]{1}},true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(new float[][]{new float[]{1}},true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(new double[][]{new double[]{1}},true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(new char[][]{new char[]{1}},true,true));
        Assert.assertFalse(ObjectUtils.isEmpty(new boolean[][]{new boolean[]{true}},true,true));
        //test Optional
        Assert.assertTrue(ObjectUtils.isEmpty(Optional.empty(),false,false));
        Assert.assertTrue(ObjectUtils.isEmpty(Optional.empty(),true,false));
        Assert.assertTrue(ObjectUtils.isEmpty(Optional.empty(),false,true));
        Assert.assertTrue(ObjectUtils.isEmpty(Optional.empty(),true,true));
        Assert.assertTrue(ObjectUtils.isEmpty(Optional.of(new char[][]{}),true,false));
        Assert.assertFalse(ObjectUtils.isEmpty(Optional.of(new byte[][]{new byte[]{1}}),true,true));
    }

    @Test
    public void getValueTest(){
        Assert.assertTrue(null == ObjectUtils.getValue(null,() -> new User().getMap().get(RandomUtils.randomString(12).concat("123"))));
    }
}
