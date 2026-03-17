package com.tingfeng.util.java.base.common.utils.datetime;

import com.tingfeng.util.java.base.common.utils.TestUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tingfeng.util.java.base.common.utils.datetime.DateUtils.FORMAT_YYYYMMDDHHMMSSSSS_CHN;

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
}