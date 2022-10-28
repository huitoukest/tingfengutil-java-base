package com.tingfeng.util.java.base.common.utils.datetime;

import com.tingfeng.util.java.base.common.utils.TestUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tingfeng.util.java.base.common.utils.datetime.DateUtils.FORMATE_YYYYMMDDHHMMSSSSS_CHN;

public class DateUtilsTest extends TestCase {

    @Test
    public void testTestFormatPerformance() {
        //准备 100w 个待处理的数据,后续单线程/10多线程处理测试性能情况
        long currentTimeMillis = System.currentTimeMillis();
        List<Date> dates = IntStream.range(0,1000000)
                .mapToObj(index -> new Date(currentTimeMillis - index * 1000))
                .collect(Collectors.toList());
        //before 1225 , after optimize no change
        TestUtils.printTime(1,1000000,index -> {
            String dateStr = DateUtils.format(dates.get(index), FORMATE_YYYYMMDDHHMMSSSSS_CHN);
            if(index % 100000 == 0){
                System.out.println(dateStr);
            }
        });
        //before 1311 , after optimize 293
        TestUtils.printTime(10,100000,(i,j) -> {
            String dateStr = DateUtils.format(dates.get(i * 100000 + j), FORMATE_YYYYMMDDHHMMSSSSS_CHN);
            if((i * 100000 + j) % 100000 == 0){
                System.out.println(dateStr);
            }
        });
    }

    @Test
    public void testParsePerformance() {
        //准备 100w 个待处理的数据,后续单线程/10多线程处理测试性能情况
        long currentTimeMillis = System.currentTimeMillis();
        List<String> dates = IntStream.range(0,1000000)
                .mapToObj(index -> DateUtils.getDateString(new Date(currentTimeMillis - index * 1000)))
                .collect(Collectors.toList());
        //before 1014 , after optimize noChange
        TestUtils.printTime(1,1000000,index -> {
            Date date = null;
                date = DateUtils.parse(dates.get(index),DateUtils.FORMATE_YYYYMMDDHHMMSS_THROUGH_LINE);
            if(index % 100000 == 0){
                System.out.println(date);
            }
        });
        //before 1298 , after optimize 289
        TestUtils.printTime(10,100000,(i,j) -> {
            Date date = DateUtils.parse(dates.get(i * 100000 + j),DateUtils.FORMATE_YYYYMMDDHHMMSS_THROUGH_LINE);
            if((i * 100000 + j) % 100000 == 0){
                System.out.println(date);
            }
        });
    }
}