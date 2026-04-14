package com.tingfeng.util.java.base.datetime;

import com.tingfeng.util.java.base.common.utils.TestUtils;
import junit.framework.TestCase;
import org.junit.Test;

 import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tingfeng.util.java.base.datetime.DateUtils.FORMAT_YYYYMMDDHHMMSSSSS_CHN;

public class DateUtilsTest extends TestCase {

    @Test
    public void testTestFormatPerformance() {
        //准备 10w 个待处理的数据,后续单线程/5多线程处理测试性能情况
        long currentTimeMillis = System.currentTimeMillis();
        List<Date> dates = IntStream.range(0,100000)
                .mapToObj(index -> new Date(currentTimeMillis - index * 1000))
                .collect(Collectors.toList());
        //测试单线程性能
        TestUtils.printTime(1,100000,index -> {
            String dateStr = DateUtils.format(dates.get(index), FORMAT_YYYYMMDDHHMMSSSSS_CHN);
            if(index % 20000 == 0){
                System.out.println(dateStr);
            }
        });
        //测试多线程性能
        TestUtils.printTime(5,20000,(i,j) -> {
            String dateStr = DateUtils.format(dates.get(i * 20000 + j), FORMAT_YYYYMMDDHHMMSSSSS_CHN);
            if((i * 20000 + j) % 20000 == 0){
                System.out.println(dateStr);
            }
        });
    }

    @Test
    public void testParsePerformance() {
        //准备 10w 个待处理的数据,后续单线程/5多线程处理测试性能情况
        long currentTimeMillis = System.currentTimeMillis();
        List<String> dates = IntStream.range(0,100000)
                .mapToObj(index -> DateUtils.getDateString(new Date(currentTimeMillis - index * 1000)))
                .collect(Collectors.toList());
        //测试单线程性能
        TestUtils.printTime(1,100000,index -> {
            Date date = null;
                date = DateUtils.parse(dates.get(index),DateUtils.FORMAT_YYYYMMDDHHMMSS_THROUGH_LINE);
            if(index % 20000 == 0){
                System.out.println(date);
            }
        });
        //测试多线程性能
        TestUtils.printTime(5,20000,(i,j) -> {
            Date date = DateUtils.parse(dates.get(i * 20000 + j),DateUtils.FORMAT_YYYYMMDDHHMMSS_THROUGH_LINE);
            if((i * 20000 + j) % 20000 == 0){
                System.out.println(date);
            }
        });
    }

    @Test
    public void testGetNextDayBegin() {
        Date date = new Date();
        Date nextDayBegin = DateUtils.getNextDayBegin(date, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date expected = calendar.getTime();
        assertEquals(expected, nextDayBegin);
    }

    @Test
    public void testGetYearCountBetweenTwoDate() {
        // 测试同一年
        Date date1 = DateUtils.parse("2023-01-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        Date date2 = DateUtils.parse("2023-12-31", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(0, DateUtils.getYearCountBetweenTwoDate(date1, date2));

        // 测试跨年
        date1 = DateUtils.parse("2023-01-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        date2 = DateUtils.parse("2024-01-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(1, DateUtils.getYearCountBetweenTwoDate(date1, date2));

        // 测试跨年但未到生日
        date1 = DateUtils.parse("2023-12-31", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        date2 = DateUtils.parse("2024-01-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(0, DateUtils.getYearCountBetweenTwoDate(date1, date2));

        // 测试跨年但日期不同（未到生日）
        date1 = DateUtils.parse("2023-01-02", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        date2 = DateUtils.parse("2024-01-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(0, DateUtils.getYearCountBetweenTwoDate(date1, date2));
        
        // 测试跨年且已过生日
        date1 = DateUtils.parse("2023-01-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        date2 = DateUtils.parse("2024-01-02", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(1, DateUtils.getYearCountBetweenTwoDate(date1, date2));
    }

    @Test
    public void testGetDatesBetweenTwoDate() {
        // 测试开始日期小于结束日期
        Date beginDate = DateUtils.parse("2023-01-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        Date endDate = DateUtils.parse("2023-01-05", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        List<Date> dates = DateUtils.getDatesBetweenTwoDate(beginDate, endDate);
        assertEquals(5, dates.size());

        // 测试开始日期大于结束日期
        dates = DateUtils.getDatesBetweenTwoDate(endDate, beginDate);
        assertEquals(5, dates.size());

        // 测试开始日期等于结束日期
        dates = DateUtils.getDatesBetweenTwoDate(beginDate, beginDate);
        assertEquals(1, dates.size());
    }

    @Test
    public void testToDate() {
        // 测试 LocalDate 转 Date
        LocalDate localDate = LocalDate.now();
        Date date = DateUtils.toDate(localDate);
        assertNotNull(date);

        // 测试 LocalDateTime 转 Date
        LocalDateTime localDateTime = LocalDateTime.now();
        date = DateUtils.toDate(localDateTime);
        assertNotNull(date);

        // 测试 null 值
        assertNull(DateUtils.toDate((LocalDate) null));
        assertNull(DateUtils.toDate((LocalDateTime) null));
    }

    @Test
    public void testGetDaysBetween() {
        Date date1 = DateUtils.parse("2023-01-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        Date date2 = DateUtils.parse("2023-01-05", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(4, DateUtils.getDaysBetween(date2, date1));
        assertEquals(-4, DateUtils.getDaysBetween(date1, date2));
        assertEquals(0, DateUtils.getDaysBetween(date1, date1));
    }

    @Test
    public void testGetDaysInMonth() {
        assertEquals(31, DateUtils.getDaysInMonth(2023, 1));
        assertEquals(28, DateUtils.getDaysInMonth(2023, 2));
        assertEquals(29, DateUtils.getDaysInMonth(2024, 2)); // 闰年
        assertEquals(30, DateUtils.getDaysInMonth(2023, 4));
    }

    @Test
    public void testIsLeapYear() {
        assertFalse(DateUtils.isLeapYear(2023));
        assertTrue(DateUtils.isLeapYear(2024));
        assertFalse(DateUtils.isLeapYear(1900));
        assertTrue(DateUtils.isLeapYear(2000));
    }

    @Test
    public void testGetQuarter() {
        Date date = DateUtils.parse("2023-01-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(1, DateUtils.getQuarter(date));

        date = DateUtils.parse("2023-04-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(2, DateUtils.getQuarter(date));

        date = DateUtils.parse("2023-07-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(3, DateUtils.getQuarter(date));

        date = DateUtils.parse("2023-10-01", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        assertEquals(4, DateUtils.getQuarter(date));
    }

    @Test
    public void testGetFirstDayOfMonth() {
        Date date = DateUtils.parse("2023-05-15", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        Date firstDay = DateUtils.getFirstDayOfMonth(date);
        assertEquals("2023-05-01", DateUtils.formatDateToString(firstDay));
    }

    @Test
    public void testGetLastDayOfMonth() {
        Date date = DateUtils.parse("2023-05-15", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        Date lastDay = DateUtils.getLastDayOfMonth(date);
        assertEquals("2023-05-31", DateUtils.formatDateToString(lastDay));

        // 测试闰年2月
        date = DateUtils.parse("2024-02-15", DateUtils.FORMAT_YYYYMMDD_THROUGH_LINE);
        lastDay = DateUtils.getLastDayOfMonth(date);
        assertEquals("2024-02-29", DateUtils.formatDateToString(lastDay));
    }

    @Test
    public void testToLocalDate() {
        Date date = new Date();
        LocalDate localDate = DateUtils.toLocalDate(date);
        assertNotNull(localDate);

        // 测试 null 值
        assertNull(DateUtils.toLocalDate(null));
    }

    @Test
    public void testToLocalDateTime() {
        Date date = new Date();
        LocalDateTime localDateTime = DateUtils.toLocalDateTime(date);
        assertNotNull(localDateTime);

        // 测试 null 值
        assertNull(DateUtils.toLocalDateTime(null));
    }

}