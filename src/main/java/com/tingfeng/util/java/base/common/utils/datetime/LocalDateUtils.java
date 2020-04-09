package com.tingfeng.util.java.base.common.utils.datetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 处理时间的工具类
 * @author huitoukest
 */
public class LocalDateUtils {
    /**
     * 计算机开始时间
     */
    public static final String COMPUTER_START_TIME_STR = "1970-01-01 00:00:00.000";
    /**
     * 常用的最小时间
     */
    public static final String COMMON_MIN_TIME_STR = "1900-01-01 00:00:00.000";
    /**
     * 没有时间
     */
    public static final String COMMON_NULL_TIME_STR = "0000-00-00 00:00:00.000";

    public static final LocalDateTime COMPUTER_START_TIME = LocalDateTime.parse(COMPUTER_START_TIME_STR);
    /**
     * 常用的最小时间
     */
    public static final LocalDateTime COMMON_MIN_TIME = LocalDateTime.parse(COMMON_MIN_TIME_STR);

    /**
     * 获取两个日期间的所有日期
     *
     * @param startDate 格式 '2018-01-25'
     * @param endDate 格式 '2018-01-25'
     * @param containsSelf 是否包含自己
     * @return
     */
    public static List<LocalDate> getBetweenDate(LocalDate startDate, LocalDate endDate,boolean containsSelf) {
        assert !endDate.isBefore(startDate);
        long distance = ChronoUnit.DAYS.between(startDate, endDate);
        List<LocalDate> re =  Stream.iterate(startDate, d ->  d.plusDays(1))
              .limit(distance + 1).collect(Collectors.toList());
        if(!containsSelf){
            if(re.size() > 1) {
                re = re.subList(1, re.size() - 1);
            }else {
                re = Collections.EMPTY_LIST;
            }
        }
        return re;
    }

    /**
     * 获取结束的时间,默认23.59.59.999毫秒
     * @param toLocalTime
     * @return
     */
    public static LocalTime getDayEnd(LocalTime toLocalTime) {
        return toLocalTime.withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999000000);
    }

    /**
     * 获取一天的开始时间,默认0.0.0.000毫秒
     * @param time
     * @return
     */
    public static LocalDateTime getDayStart(LocalDateTime time) {
        return time.withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    /**
     * 获取一天的结束时间 默认23.59.59.999毫秒
     * @param time
     * @return
     */
    public static LocalDateTime getDayEnd(LocalDateTime time) {
        return time.withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999000000);
    }

    /**
     * 传入开始结束年月，计算中间的月份，
     * @param startMonthValue 开始月份值, 如202001
     * @param endMonthValue 结束月份值, 如202315
     * @return
     */
    public static List<Integer> getMonthValues(int startMonthValue,int endMonthValue){
        int startYear = startMonthValue / 100;
        int endYear = endMonthValue / 100;
        int startMonth = startMonthValue % 100;
        int endMonth = endMonthValue % 100;
        int year = startYear;
        List<Integer> monthValues = new ArrayList<>(16);
        for(int month = startMonth; ;month ++){
            if(month == 13){
                year ++;
                month = 1;
            }
            monthValues.add(year * 100 + month);
            if(year == endYear && month >= endMonth){
                break;
            }
        }
        return monthValues;
    }

    /**
     * 获取月份的开始时间
     * @param localDateTime
     * @return
     */
    public static LocalDateTime getMonthBeginDateTime(LocalDateTime localDateTime) {
        return getDayStart(localDateTime.withDayOfMonth(1));
    }

    /**
     * 获取月份的结束时间
     * @param localDateTime
     * @return
     */
    public static LocalDateTime getMonthEndDateTime(LocalDateTime localDateTime){
        return getDayEnd(localDateTime.plusMonths(1).withDayOfMonth(1).plusDays(-1));
    }


}
