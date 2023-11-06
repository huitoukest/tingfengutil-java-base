package com.tingfeng.util.java.base.common.utils.datetime;

import com.tingfeng.util.java.base.common.utils.RegExpUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 处理时间的工具类
 * @author huitoukest
 */
public class LocalDateUtils implements DateFormat{
    /**
     * 计算机开始时间
     */
    public static final String COMPUTER_START_TIME_STR = "1970-01-01 00:00:00.000";
    /**
     * 常用的最小时间
     */
    public static final String COMMON_MIN_TIME_STR = "1900-01-01 00:00:00.000";
    /**
     * 没有时间，无法转换为 LocalDateTime
     */
    public static final String COMMON_ZERO_TIME_STR = "0000-00-00 00:00:00.000";

    public static final LocalDateTime COMPUTER_START_TIME = LocalDateTime.parse(COMPUTER_START_TIME_STR, DateTimeFormatter.ofPattern(DateFormat.FORMAT_YYYYMMDDHHMMSSSSS_THROUGH_LINE));
    /**
     * 常用的最小时间
     */
    public static final LocalDateTime COMMON_MIN_TIME = LocalDateTime.parse(COMMON_MIN_TIME_STR, DateTimeFormatter.ofPattern(DateFormat.FORMAT_YYYYMMDDHHMMSSSSS_THROUGH_LINE));

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

    /**
     * 获取毫秒数
     * @param localDateTime
     * @return
     */
    public static long getTime(LocalDateTime localDateTime){
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取毫秒数
     * @param localDateTime
     * @return
     */
    public static long getMills(LocalDateTime localDateTime){
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取毫秒数
     * @param localDate
     * @return
     */
    public static long getTime(LocalDate localDate){
        return localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }


    /**
     * 获取毫秒数
     * @param localDate
     * @return
     */
    public static long getMills(LocalDate localDate){
        return localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 默认格式化 yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String getDateString(LocalDate date) {
        return getDateString(date, FORMAT_YYYYMMDDHHMMSS_THROUGH_LINE);
    }

    /**
     * 默认格式化 yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public static String getDateString(LocalDate date,String format) {
        if(date == null){
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 默认格式化 HH:mm:ss
     *
     * @param dateTime
     * @return
     */
    public static String getTimeString(LocalDateTime dateTime) {
        return getTimeString(dateTime, FORMAT_HHMMSS_THROUGH_LINE);
    }

    /**
     * 默认格式化 HH:mm:ss
     * @param dateTime
     * @return
     */
    public static String getTimeString(LocalDateTime dateTime,String format) {
        if(dateTime == null){
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 日期时间转换
     * @param localDateTime
     * @return
     */
    public static Date toDate(LocalDateTime localDateTime){
        if(localDateTime == null){
            return null;
        }
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * 日期转换
     * @param localDate
     * @return
     */
    public static Date toDate(LocalDate localDate){
        if(localDate == null){
            return null;
        }
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * 解析数值类型的日期 , 如 20200101 , 8位数字
     * @param dateNumber
     * @return
     */
    public static LocalDate getDate(int dateNumber){
        int year = dateNumber / 10000;
        int month = dateNumber % 10000 / 100;
        int day = dateNumber % 100;
        return LocalDate.of(year, month, day);
    }

    /**
     * 获取日期对应的数值 如 20200101 , 8位数字
     * @param localDate
     * @return
     */
    public static int getDateNumber(LocalDate localDate){
        return localDate.getYear() * 10000 + localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
    }

    public static int getDateNumber(LocalDateTime localDatetime){
        return localDatetime.getYear() * 10000 + localDatetime.getMonthValue() * 100 + localDatetime.getDayOfMonth();
    }


    /**
     * 解析未日期-时间
     * @param str 输入的时间信息
     * @return
     */
    public static LocalDateTime getLocalDateTime(String str){
        return getLocalDateTime(str, true);
    }

    /**
     * 解析未日期-时间
     * @param str 输入的时间信息
     * @return
     */
    public static LocalDate getLocalDate(String str){
        return getLocalDate(str, true);
    }

    /**
     * isAutoConvert是false时返回,"yyyy-MM-dd HH:mm:ss"支持的格式
     * @param str
     * @param isAutoConvert 是否根据输入的值,自动猜测时间格式并转换 ,支持:DateFormat中所列出的常量格式类型的自动猜测转换
     * @return 日期-时间
     */
    public static LocalDateTime getLocalDateTime(String str,boolean isAutoConvert){
        if(isAutoConvert) {
            if(StringUtils.isEmpty(str)) {
                return null;
            }
            str = str.replaceAll("[^\\d]","");
            int length = str.length();
            if(length == 10 && RegExpUtils.isIntegerNumber(str)){
                return LocalDateTime.ofEpochSecond(Long.parseLong(str), 0 , ZoneOffset.of(ZoneOffset.systemDefault().getId()));
            }
            //默认识别为毫秒数量
            if(length == 13 && RegExpUtils.isIntegerNumber(str)){
                long mills = Long.parseLong(str);
                LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(mills / 1000, 0, ZoneOffset.of(ZoneOffset.systemDefault().getId()));
                localDateTime = localDateTime.plus(mills % 1000 , ChronoUnit.MILLIS);
                return localDateTime;
            }
            StringBuilder sb = new StringBuilder(30);
            sb.append(str);
            for(int  i = length + 1 ; i < 18; i++) {//初始化数据
                if(i == 5 || i == 7 || i >= 9){
                    sb.append(0);
                }
                if(i == 6 || i == 8){
                    sb.append(1);
                }
            }
            //jdk8中毫秒必须单独处理,部分格式无法处理包含毫秒的内容
            LocalDateTime localDateTime = LocalDateTime.parse(sb.substring(0, 14), DateTimeFormatter.ofPattern(FORMAT_YYYYMMDDHHMMSS));
            localDateTime = localDateTime.plus(Integer.valueOf(sb.substring(14)) , ChronoUnit.MILLIS);
            return localDateTime;
        }else {
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(FORMAT_YYYYMMDDHHMMSS_THROUGH_LINE));
        }
    }

    /**
     * isAutoConvert是false时返回,"yyyy-MM-dd HH:mm:ss"支持的格式
     * @param str
     * @param isAutoConvert 是否根据输入的值,自动猜测时间格式并转换 ,支持:DateFormat中所列出的常量格式类型的自动猜测转换
     * @return 日期
     */
    public static LocalDate getLocalDate(String str,boolean isAutoConvert){
        return Optional.ofNullable(getLocalDateTime(str, isAutoConvert)).map(LocalDateTime::toLocalDate).orElse(null);
    }
}
