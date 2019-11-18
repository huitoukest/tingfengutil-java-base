package com.tingfeng.util.java.base.common.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author huitoukest
 */
public class RunTimeUtilsTest {

    @Test
    public void getLocalIpV4AddressesTest(){
        List<String> ipAddr = RuntimeUtils.getLocalIpV4Addresses();
        Assert.assertTrue(!ipAddr.isEmpty());
    }
}
