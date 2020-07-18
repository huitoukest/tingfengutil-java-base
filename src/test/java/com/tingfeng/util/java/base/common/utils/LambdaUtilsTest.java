package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.bean.User;
import org.junit.Assert;
import org.junit.Test;

public class LambdaUtilsTest {

    @Test
    public void test() {
        Assert.assertEquals("age", LambdaUtils.getFieldName(User::getAge));
        Assert.assertEquals("isOk", LambdaUtils.getFieldName(User::getIsOk));
    }

}
