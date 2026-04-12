package com.tingfeng.util.java.base.common.utils.datetime;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class LocalDateUtilsTest {
    @Test
    public void testGetBetweenDate() {
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2023, 1, 5);
        
        // 测试包含自己的情况
        List<LocalDate> resultWithSelf = LocalDateUtils.getBetweenDate(start, end, true);
        assertEquals(5, resultWithSelf.size());
        assertEquals(LocalDate.of(2023, 1, 1), resultWithSelf.get(0));
        assertEquals(LocalDate.of(2023, 1, 5), resultWithSelf.get(4));
        
        // 测试不包含自己的情况
        List<LocalDate> resultWithoutSelf = LocalDateUtils.getBetweenDate(start, end, false);
        assertEquals(3, resultWithoutSelf.size());
        assertEquals(LocalDate.of(2023, 1, 2), resultWithoutSelf.get(0));
        assertEquals(LocalDate.of(2023, 1, 4), resultWithoutSelf.get(2));
    }

    @Test
    public void testGetDayEnd_LocalTime() {
        LocalTime time = LocalTime.of(12, 30, 45);
        LocalTime result = LocalDateUtils.getDayEnd(time);
        assertEquals(LocalTime.of(23, 59, 59, 999000000), result);
    }

    @Test
    public void testGetDayStart() {
        LocalDateTime time = LocalDateTime.of(2023, 1, 1, 12, 30, 45, 123456789);
        LocalDateTime result = LocalDateUtils.getDayStart(time);
        assertEquals(LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0), result);
    }

    @Test
    public void testGetDayEnd_LocalDateTime() {
        LocalDateTime time = LocalDateTime.of(2023, 1, 1, 12, 30, 45, 123456789);
        LocalDateTime result = LocalDateUtils.getDayEnd(time);
        assertEquals(LocalDateTime.of(2023, 1, 1, 23, 59, 59, 999000000), result);
    }

    @Test
    public void testGetMonthValues() {
        List<Integer> result = LocalDateUtils.getMonthValues(202301, 202303);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(202301), result.get(0));
        assertEquals(Integer.valueOf(202302), result.get(1));
        assertEquals(Integer.valueOf(202303), result.get(2));
    }

    @Test
    public void testGetMonthBeginDateTime() {
        LocalDateTime time = LocalDateTime.of(2023, 3, 15, 12, 30, 45);
        LocalDateTime result = LocalDateUtils.getMonthBeginDateTime(time);
        assertEquals(LocalDateTime.of(2023, 3, 1, 0, 0, 0, 0), result);
    }

    @Test
    public void testGetMonthEndDateTime() {
        LocalDateTime time = LocalDateTime.of(2023, 2, 15, 12, 30, 45);
        LocalDateTime result = LocalDateUtils.getMonthEndDateTime(time);
        assertEquals(LocalDateTime.of(2023, 2, 28, 23, 59, 59, 999000000), result);
    }

    @Test
    public void testGetTime_LocalDateTime() {
        LocalDateTime time = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        long result = LocalDateUtils.getTime(time);
        assertEquals(time.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), result);
    }

    @Test
    public void testGetMills_LocalDateTime() {
        LocalDateTime time = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        long result = LocalDateUtils.getMills(time);
        assertEquals(time.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), result);
    }

    @Test
    public void testGetTime_LocalDate() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        long result = LocalDateUtils.getTime(date);
        assertEquals(date.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), result);
    }

    @Test
    public void testGetMills_LocalDate() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        long result = LocalDateUtils.getMills(date);
        assertEquals(date.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(), result);
    }

    @Test
    public void getDateNumber() {
        Assert.assertEquals(20130101,LocalDateUtils.getDateNumber(LocalDateTime.of(2013,1,1,0,0,0,0)));
        Assert.assertEquals(20200229,LocalDateUtils.getDateNumber(LocalDateTime.of(2020,2,29,0,0,0,0)));
    }

    @Test
    public void testGetDateNumber_LocalDate() {
        assertEquals(20230101, LocalDateUtils.getDateNumber(LocalDate.of(2023, 1, 1)));
    }

    @Test
    public void testGetDateString_LocalDate_DefaultFormat() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        String result = LocalDateUtils.getDateString(date);
        assertEquals("2023-01-01", result);
    }

    @Test
    public void testGetDateString_LocalDate_CustomFormat() {
        LocalDate date = LocalDate.of(2023, 1, 1);
        String result = LocalDateUtils.getDateString(date, "yyyy/MM/dd");
        assertEquals("2023/01/01", result);
    }

    @Test
    public void testGetDateString_LocalDateTime_DefaultFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 12, 30, 45);
        String result = LocalDateUtils.getDateString(dateTime);
        assertEquals("2023-01-01 12:30:45", result);
    }

    @Test
    public void testGetDateString_LocalDateTime_CustomFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 12, 30, 45);
        String result = LocalDateUtils.getDateString(dateTime, "yyyy/MM/dd HH:mm:ss");
        assertEquals("2023/01/01 12:30:45", result);
    }

    @Test
    public void testGetTimeString_LocalDateTime_DefaultFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 12, 30, 45);
        String result = LocalDateUtils.getTimeString(dateTime);
        assertEquals("12:30:45", result);
    }

    @Test
    public void testGetTimeString_LocalDateTime_CustomFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 12, 30, 45);
        String result = LocalDateUtils.getTimeString(dateTime, "HH:mm");
        assertEquals("12:30", result);
    }

    @Test
    public void testToDate_LocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 1, 1, 12, 30, 45);
        Date date = LocalDateUtils.toDate(localDateTime);
        assertNotNull(date);
        
        // 验证转换回来的LocalDateTime是否一致
        LocalDateTime convertedBack = date.toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDateTime();
        
        assertEquals(localDateTime.getYear(), convertedBack.getYear());
        assertEquals(localDateTime.getMonth(), convertedBack.getMonth());
        assertEquals(localDateTime.getDayOfMonth(), convertedBack.getDayOfMonth());
        assertEquals(localDateTime.getHour(), convertedBack.getHour());
        assertEquals(localDateTime.getMinute(), convertedBack.getMinute());
        assertEquals(localDateTime.getSecond(), convertedBack.getSecond());
    }

    @Test
    public void testToDate_LocalDate() {
        LocalDate localDate = LocalDate.of(2023, 1, 1);
        Date date = LocalDateUtils.toDate(localDate);
        assertNotNull(date);
        
        // 验证转换回来的LocalDate是否一致
        LocalDate convertedBack = date.toInstant()
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate();
        
        assertEquals(localDate, convertedBack);
    }

    @Test
    public void testGetDate_FromInt() {
        LocalDate result = LocalDateUtils.getDate(20230101);
        assertEquals(LocalDate.of(2023, 1, 1), result);
    }

    @Test
    public void testGetLocalDateTime() {
        Assert.assertEquals(LocalDateTime.of(2023,1,1,0,0,12,990000000),
                LocalDateUtils.getLocalDateTime("2023/01/01 00:00:12.99"));
        Assert.assertEquals(LocalDateTime.of(2020,2,29,0,0,0,111000000),
                LocalDateUtils.getLocalDateTime("2020年02月29日 00:00:00.111"));
    }

    @Test
    public void testGetLocalDate() {
        LocalDate result = LocalDateUtils.getLocalDate("2023-01-01");
        assertEquals(LocalDate.of(2023, 1, 1), result);
    }

    @Test
    public void testGetLocalDateTime_AutoConvert() {
        // 测试自动格式转换
        LocalDateTime result = LocalDateUtils.getLocalDateTime("20230101000000000", true);
        assertEquals(LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0), result);
    }

    @Test
    public void testGetLocalDate_AutoConvert() {
        // 测试自动格式转换
        LocalDate result = LocalDateUtils.getLocalDate("20230101", true);
        assertEquals(LocalDate.of(2023, 1, 1), result);
    }
}