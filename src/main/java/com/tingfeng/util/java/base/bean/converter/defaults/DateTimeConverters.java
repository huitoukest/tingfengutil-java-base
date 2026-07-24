package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;
import com.tingfeng.util.java.base.datetime.DateFormat;
import com.tingfeng.util.java.base.datetime.DateUtils;
import com.tingfeng.util.java.base.datetime.LocalDateUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间类型转换器注册
 * <p>
 * 所有 src=Date/LocalDateTime/LocalDate 的转换器（不含 String -> X）
 */
public final class DateTimeConverters {

    private DateTimeConverters() {}

    public static void register(ConverterRegistry registry) {
        // Date -> String
        registry.register(ConverterUtils.of(
                Date.class, String.class,
                d -> DateUtils.getDateString(d, DateFormat.FORMAT_YYYYMMDDHHMMSS_THROUGH_LINE)
        ));

        // LocalDateTime -> String
        registry.register(ConverterUtils.of(
                LocalDateTime.class, String.class,
                d -> LocalDateUtils.getDateString(d)
        ));

        // LocalDate -> String
        registry.register(ConverterUtils.of(
                LocalDate.class, String.class,
                d -> LocalDateUtils.getDateString(d)
        ));

        // Date <-> LocalDateTime
        registry.register(ConverterUtils.of(
                Date.class, LocalDateTime.class,
                d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        ));
        registry.register(ConverterUtils.of(
                LocalDateTime.class, Date.class,
                d -> Date.from(d.atZone(ZoneId.systemDefault()).toInstant())
        ));

        // Date <-> LocalDate
        registry.register(ConverterUtils.of(
                Date.class, LocalDate.class,
                d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        ));
        registry.register(ConverterUtils.of(
                LocalDate.class, Date.class,
                d -> Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant())
        ));

        // ==================== Date -> Calendar ====================
        registry.register(ConverterUtils.of(
                Date.class, Calendar.class,
                DateUtils::toCalendar
        ));
        registry.register(ConverterUtils.of(
                LocalDateTime.class, Calendar.class,
                LocalDateUtils::toCalendar
        ));
        registry.register(ConverterUtils.of(
                LocalDate.class, Calendar.class,
                LocalDateUtils::toCalendar
        ));

        // ==================== Date -> Integer (秒) ====================
        registry.register(ConverterUtils.of(
                Date.class, Integer.class,
                DateUtils::toSeconds
        ));
        registry.register(ConverterUtils.of(
                LocalDateTime.class, Integer.class,
                LocalDateUtils::toSeconds
        ));
        registry.register(ConverterUtils.of(
                LocalDate.class, Integer.class,
                LocalDateUtils::toSeconds
        ));

        // ==================== Date -> Long (毫秒) ====================
        registry.register(ConverterUtils.of(
                Date.class, Long.class,
                DateUtils::getMills
        ));
        registry.register(ConverterUtils.of(
                LocalDateTime.class, Long.class,
                LocalDateUtils::getMills
        ));
        registry.register(ConverterUtils.of(
                LocalDate.class, Long.class,
                LocalDateUtils::getMills
        ));

        // ==================== Duration -> X ====================
        registry.register(ConverterUtils.of(
                Duration.class, Long.class,
                Duration::toMillis
        ));
        registry.register(ConverterUtils.of(
                Duration.class, Integer.class,
                d -> (int) d.getSeconds()
        ));
        registry.register(ConverterUtils.of(
                Duration.class, String.class,
                Duration::toString
        ));

        // ==================== Period -> X ====================
        registry.register(ConverterUtils.of(
                Period.class, String.class,
                Period::toString
        ));
        registry.register(ConverterUtils.of(
                Period.class, int[].class,
                p -> new int[]{p.getYears(), p.getMonths(), p.getDays()}
        ));

        // ==================== ZonedDateTime -> String ====================
        registry.register(ConverterUtils.of(
                ZonedDateTime.class, String.class,
                zdt -> zdt.format(DateTimeFormatter.ofPattern(DateFormat.FORMAT_YYYYMMDDHHMMSS_THROUGH_LINE))
        ));
    }
}
