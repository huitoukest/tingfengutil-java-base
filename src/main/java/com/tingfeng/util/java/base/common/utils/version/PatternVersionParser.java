package com.tingfeng.util.java.base.common.utils.version;

import com.tingfeng.util.java.base.datetime.DateFormat;
import com.tingfeng.util.java.base.datetime.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 基于正则分割的版本解析器。
 * 解析流程：
 * 1. 校验入参非 null/非空
 * 2. 使用 splitRegex 分割版本字符串
 * 3. 对每段依次尝试：Long → Date（可选）→ String
 * 4. 包装为 VersionSegment 列表返回
 */
public class PatternVersionParser implements VersionParser {

    private final Pattern splitPattern;
    private final boolean dateDetectionEnabled;

    /**
     * 使用指定分割正则构造解析器，日期检测默认关闭。
     *
     * @param splitRegex 分割正则表达式
     */
    public PatternVersionParser(String splitRegex) {
        this(splitRegex, false);
    }

    /**
     * 使用指定分割正则和日期检测开关构造解析器。
     *
     * @param splitRegex           分割正则表达式
     * @param dateDetectionEnabled 是否开启日期类型推断（默认 false）
     */
    public PatternVersionParser(String splitRegex, boolean dateDetectionEnabled) {
        this.splitPattern = Pattern.compile(splitRegex);
        this.dateDetectionEnabled = dateDetectionEnabled;
    }

    @Override
    public VersionParsedResult parse(String version) {
        if (version == null) {
            throw new IllegalArgumentException("version must not be null");
        }
        if (version.isEmpty()) {
            throw new IllegalArgumentException("version must not be empty");
        }
        String[] parts = splitPattern.split(version, -1);
        List<VersionSegment> segments = new ArrayList<>(parts.length);
        for (String part : parts) {
            segments.add(parseSegment(part));
        }
        return new VersionParsedResult(segments);
    }

    /**
     * 单段类型推断：Long → Date（可选）→ String。
     *
     * @param raw 原始段字符串
     * @return 推断后的 VersionSegment
     */
    private VersionSegment parseSegment(String raw) {
        // 1. 尝试 Long
        try {
            long val = Long.parseLong(raw);
            return new VersionSegment(val, raw, true);
        } catch (NumberFormatException e) {
            // 正常流程信号，继续尝试下一类型
        }

        // 2. 尝试 Date（仅在 dateDetectionEnabled=true 时）
        if (dateDetectionEnabled) {
            Date date = tryParseDate(raw);
            if (date != null) {
                return new VersionSegment(date, raw, true);
            }
        }

        // 3. 作为 String 兜底
        return new VersionSegment(raw, raw, true);
    }

    /**
     * 尝试将原始字符串解析为日期。
     * 支持的格式：yyyyMMdd、yyyy-MM-dd、yyyy/MM/dd、yyyyMM、yyyy。
     * 增加年份范围约束 (1970-2099) 避免误判。
     *
     * @param raw 原始字符串
     * @return 解析成功返回 Date 对象，否则返回 null
     */
    private Date tryParseDate(String raw) {
        String[] formats = {
                DateFormat.FORMAT_YYYYMMDD,
                DateFormat.FORMAT_YYYYMMDD_THROUGH_LINE,
                DateFormat.FORMAT_YYYYMMDD_OBLINE,
                DateFormat.FORMAT_YYYYMM,
                DateFormat.FORMAT_YYYY
        };
        for (String format : formats) {
            try {
                Date date = DateUtils.parse(raw, format);
                if (date != null) {
                    int year = DateUtils.getYear(date);
                    if (year >= 1970 && year <= 2099) {
                        return date;
                    }
                }
            } catch (RuntimeException e) {
                // 解析失败，尝试下一格式
            }
        }
        return null;
    }
}
