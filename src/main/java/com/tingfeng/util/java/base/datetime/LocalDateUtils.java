package com.tingfeng.util.java.base.datetime;

import com.tingfeng.util.java.base.text.RegExpUtils;
import com.tingfeng.util.java.base.lang.StringUtils;

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
     * @param containsSelf 是否包含起始和结束日期
     * @return 包含所有日期的列表
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
                re = Collections.emptyList();
            }
        }
        return re;
    }

    /**
     * 获取一天结束的时间,默认23:59:59.999
     * @param toLocalTime 输入的时间
     * @return 设置为当天结束时间的LocalTime对象
     */
    public static LocalTime getDayEnd(LocalTime toLocalTime) {
        return toLocalTime.withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999000000);
    }

    /**
     * 获取一天的开始时间,默认00:00:00.000
     * @param time 输入的时间
     * @return 设置为当天开始时间的LocalDateTime对象
     */
    public static LocalDateTime getDayStart(LocalDateTime time) {
        return time.withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    /**
     * 获取一天的结束时间 默认23:59:59.999
     * @param time 输入的时间
     * @return 设置为当天结束时间的LocalDateTime对象
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
     * @return 包含所有月份值的列表
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
     * @param localDateTime 输入的日期时间
     * @return 该月第一天的开始时间（00:00:00.000）
     */
    public static LocalDateTime getMonthBeginDateTime(LocalDateTime localDateTime) {
        return getDayStart(localDateTime.withDayOfMonth(1));
    }

    /**
     * 获取月份的结束时间
     * @param localDateTime 输入的日期时间
     * @return 该月最后一天的结束时间（23:59:59.999）
     */
    public static LocalDateTime getMonthEndDateTime(LocalDateTime localDateTime){
        return getDayEnd(localDateTime.plusMonths(1).withDayOfMonth(1).plusDays(-1));
    }

    /**
     * 获取指定日期时间的毫秒数
     * @param localDateTime 输入的日期时间
     * @return 从纪元开始的毫秒数
     */
    public static long getTime(LocalDateTime localDateTime){
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取指定日期时间的毫秒数
     * @param localDateTime 输入的日期时间
     * @return 从纪元开始的毫秒数
     */
    public static long getMills(LocalDateTime localDateTime){
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取指定日期的毫秒数（当天开始时间）
     * @param localDate 输入的日期
     * @return 从纪元开始的毫秒数
     */
    public static long getTime(LocalDate localDate){
        return localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }


    /**
     * 获取指定日期的毫秒数（当天开始时间）
     * @param localDate 输入的日期
     * @return 从纪元开始的毫秒数
     */
    public static long getMills(LocalDate localDate){
        return localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 默认格式化 yyyy-MM-dd
     *
     * @param date 输入的日期
     * @return 格式化后的日期字符串
     */
    public static String getDateString(LocalDate date) {
        return getDateString(date, FORMAT_YYYYMMDD_THROUGH_LINE);
    }

    /**
     * 按指定格式格式化日期
     *
     * @param date 输入的日期
     * @param format 日期格式
     * @return 格式化后的日期字符串，如果输入为null则返回null
     */
    public static String getDateString(LocalDate date,String format) {
        if(date == null){
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 默认格式化 yyyy-MM-dd HH:mm:ss
     *
     * @param date 输入的日期时间
     * @return 格式化后的日期时间字符串
     */
    public static String getDateString(LocalDateTime date) {
        return getDateString(date, FORMAT_YYYYMMDDHHMMSS_THROUGH_LINE);
    }

    /**
     * 按指定格式格式化日期时间
     *
     * @param date 输入的日期时间
     * @param format 日期时间格式
     * @return 格式化后的日期时间字符串，如果输入为null则返回null
     */
    public static String getDateString(LocalDateTime date,String format) {
        if(date == null){
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 默认格式化 HH:mm:ss
     *
     * @param dateTime 输入的日期时间
     * @return 格式化后的时间字符串
     */
    public static String getTimeString(LocalDateTime dateTime) {
        return getTimeString(dateTime, FORMAT_HHMMSS_THROUGH_LINE);
    }

    /**
     * 按指定格式格式化时间
     * @param dateTime 输入的日期时间
     * @param format 时间格式
     * @return 格式化后的时间字符串，如果输入为null则返回null
     */
    public static String getTimeString(LocalDateTime dateTime,String format) {
        if(dateTime == null){
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 将LocalDateTime转换为Date
     * @param localDateTime 输入的日期时间
     * @return 转换后的Date对象，如果输入为null则返回null
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
     * 将LocalDate转换为Date
     * @param localDate 输入的日期
     * @return 转换后的Date对象，如果输入为null则返回null
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
     * 将LocalDateTime转换为Calendar
     * @param localDateTime 输入的日期时间
     * @return 转换后的Calendar对象，如果输入为null则返回null
     */
    public static Calendar toCalendar(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return DateUtils.toCalendar(toDate(localDateTime));
    }

    /**
     * 将LocalDate转换为Calendar
     * @param localDate 输入的日期
     * @return 转换后的Calendar对象，如果输入为null则返回null
     */
    public static Calendar toCalendar(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return DateUtils.toCalendar(toDate(localDate));
    }

    /**
     * 将LocalDateTime转换为秒数（从纪元开始）
     * @param localDateTime 输入的日期时间
     * @return 秒数，如果输入为null则返回null
     */
    public static Integer toSeconds(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return (int) localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * 将LocalDate转换为秒数（从纪元开始，当天0点）
     * @param localDate 输入的日期
     * @return 秒数，如果输入为null则返回null
     */
    public static Integer toSeconds(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return (int) localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * 将LocalDateTime转换为毫秒数（从纪元开始）
     * @param localDateTime 输入的日期时间
     * @return 毫秒数，如果输入为null则返回null
     */
    public static Long toMills(LocalDateTime localDateTime) {
        return getMills(localDateTime);
    }

    /**
     * 将LocalDate转换为毫秒数（从纪元开始，当天0点）
     * @param localDate 输入的日期
     * @return 毫秒数，如果输入为null则返回null
     */
    public static Long toMills(LocalDate localDate) {
        return getMills(localDate);
    }

    /**
     * 解析数值类型的日期 , 如 20200101 , 8位数字
     * @param dateNumber 8位数字的日期，格式为yyyyMMdd
     * @return 解析后的LocalDate对象
     */
    public static LocalDate getDate(int dateNumber){
        int year = dateNumber / 10000;
        int month = dateNumber % 10000 / 100;
        int day = dateNumber % 100;
        return LocalDate.of(year, month, day);
    }

    /**
     * 获取日期对应的数值 如 20200101 , 8位数字
     * @param localDate 输入的日期
     * @return 8位数字的日期，格式为yyyyMMdd
     */
    public static int getDateNumber(LocalDate localDate){
        return localDate.getYear() * 10000 + localDate.getMonthValue() * 100 + localDate.getDayOfMonth();
    }

    /**
     * 获取日期时间对应的数值 如 20200101 , 8位数字
     * @param localDatetime 输入的日期时间
     * @return 8位数字的日期，格式为yyyyMMdd
     */
    public static int getDateNumber(LocalDateTime localDatetime){
        return localDatetime.getYear() * 10000 + localDatetime.getMonthValue() * 100 + localDatetime.getDayOfMonth();
    }


    /**
     * 解析字符串为日期-时间，默认启用自动格式转换
     * @param str 输入的时间信息
     * @return 解析后的LocalDateTime对象，如果解析失败则返回null
     */
    public static LocalDateTime getLocalDateTime(String str){
        return getLocalDateTime(str, true);
    }

    /**
     * 解析字符串为日期，默认启用自动格式转换
     * @param str 输入的时间信息
     * @return 解析后的LocalDate对象，如果解析失败则返回null
     */
    public static LocalDate getLocalDate(String str){
        return getLocalDate(str, true);
    }

    /**
     * 解析字符串为日期-时间
     * @param str 输入的时间字符串
     * @param isAutoConvert 是否根据输入的值,自动猜测时间格式并转换 ,支持:DateFormat中所列出的常量格式类型的自动猜测转换
     * @return 解析后的LocalDateTime对象，如果解析失败则返回null
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
     * 解析字符串为日期
     * @param str 输入的时间字符串
     * @param isAutoConvert 是否根据输入的值,自动猜测时间格式并转换 ,支持:DateFormat中所列出的常量格式类型的自动猜测转换
     * @return 解析后的LocalDate对象，如果解析失败则返回null
     */
    public static LocalDate getLocalDate(String str,boolean isAutoConvert){
        return Optional.ofNullable(getLocalDateTime(str, isAutoConvert)).map(LocalDateTime::toLocalDate).orElse(null);
    }
}