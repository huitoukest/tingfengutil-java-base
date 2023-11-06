package com.tingfeng.util.java.base.common.utils.datetime;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class LocalDateUtilsTest {

    @Test
    public void getDateNumber() {
        Assert.assertEquals(20130101,LocalDateUtils.getDateNumber(LocalDateTime.of(2013,1,1,0,0,0,0)));
        Assert.assertEquals(20200229,LocalDateUtils.getDateNumber(LocalDateTime.of(2020,2,29,0,0,0,0)));
    }

    @Test
    public void getLocalDateTime() {
        Assert.assertEquals(LocalDateTime.of(2023,1,1,0,0,12,990000000),
                LocalDateUtils.getLocalDateTime("2023/01/01 00:00:12.99"));
        Assert.assertEquals(LocalDateTime.of(2020,2,29,0,0,0,111000000),
                LocalDateUtils.getLocalDateTime("2020年02月29日 00:00:00.111"));
    }
}