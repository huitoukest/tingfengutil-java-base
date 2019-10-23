package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

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
    }
}
