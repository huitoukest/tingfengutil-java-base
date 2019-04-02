package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

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

}
