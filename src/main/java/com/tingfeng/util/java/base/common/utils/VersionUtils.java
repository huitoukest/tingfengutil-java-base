package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.utils.version.PatternVersionParser;
import com.tingfeng.util.java.base.common.utils.version.VersionParser;
import com.tingfeng.util.java.base.common.utils.version.VersionParsedResult;
import com.tingfeng.util.java.base.common.utils.version.VersionSegment;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * 版本号工具的门面类。
 * 提供 parse/compare/isValid 三大核心操作。
 * 默认使用分割正则 [\.-]（支持 "." 和 "-" 分割）。
 *
 * 用法示例：
 * <pre>{@code
 * // 基本比较
 * VersionUtils.compare("1.0.0-RC1", "1.0.0-RELEASE");
 *
 * // 自定义分割
 * VersionParser parser = VersionUtils.ofPattern("[._-]");
 * VersionUtils.compare("1_0_0", "1-0-0", parser);
 *
 * // 自定义比较器
 * VersionUtils.compare("1.0.0-RC1", "1.0.0-RELEASE",
 *     Collections.singletonMap(3, (a, b) -> rc1VsReleaseComparator(a, b)));
 *
 * // 解析后手动比较
 * VersionParsedResult r = VersionUtils.parse("1.0.0");
 * r.putComparator(2, myComparator);
 * r.compareTo(otherResult);
 * }</pre>
 */
public final class VersionUtils {

    /** 默认分割正则：[\.-] */
    private static final String DEFAULT_SPLIT_REGEX = "[\\\\.-]";

    private static final VersionParser DEFAULT_PARSER =
            new PatternVersionParser(DEFAULT_SPLIT_REGEX);

    private VersionUtils() {
    }

    // ==================== 解析 ====================

    /**
     * 使用默认解析器解析版本字符串。
     *
     * @param version 版本字符串
     * @return 解析结果
     * @throws IllegalArgumentException 若 version 为 null/空
     */
    public static VersionParsedResult parse(String version) {
        return DEFAULT_PARSER.parse(version);
    }

    /**
     * 使用指定解析器解析版本字符串。
     *
     * @param version 版本字符串
     * @param parser  版本解析器
     * @return 解析结果
     * @throws IllegalArgumentException 若 version 为 null/空
     */
    public static VersionParsedResult parse(String version, VersionParser parser) {
        return parser.parse(version);
    }

    // ==================== 比较 ====================

    /**
     * 使用默认解析器比较两个版本号。
     *
     * @param v1 第一个版本号
     * @param v2 第二个版本号
     * @return 负数表示 v1 < v2，零表示相等，正数表示 v1 > v2
     */
    public static int compare(String v1, String v2) {
        return parse(v1).compareTo(parse(v2));
    }

    /**
     * 使用指定解析器比较两个版本号。
     *
     * @param v1     第一个版本号
     * @param v2     第二个版本号
     * @param parser 版本解析器
     * @return 负数表示 v1 < v2，零表示相等，正数表示 v1 > v2
     */
    public static int compare(String v1, String v2, VersionParser parser) {
        return parse(v1, parser).compareTo(parse(v2, parser));
    }

    /**
     * 使用默认解析器比较两个版本号，支持自定义比较器。
     * 以左侧结果的自定义比较器为准。
     *
     * @param v1          第一个版本号
     * @param v2          第二个版本号
     * @param comparators 索引到自定义比较器的映射
     * @return 负数表示 v1 < v2，零表示相等，正数表示 v1 > v2
     */
    public static int compare(String v1, String v2,
                              Map<Integer, Comparator<VersionSegment>> comparators) {
        VersionParsedResult r1 = parse(v1);
        VersionParsedResult r2 = parse(v2);
        if (comparators != null) {
            r1.putAllComparators(comparators);
        }
        return r1.compareTo(r2);
    }

    // ==================== 解析器工厂 ====================

    /**
     * 获取默认解析器（分割正则：[\.-]，日期检测关闭）。
     *
     * @return 默认版本解析器
     */
    public static VersionParser defaultParser() {
        return DEFAULT_PARSER;
    }

    /**
     * 创建指定分割正则的解析器，日期检测关闭。
     *
     * @param splitRegex 分割正则表达式
     * @return 新创建的版本解析器
     */
    public static VersionParser ofPattern(String splitRegex) {
        return new PatternVersionParser(splitRegex);
    }

    /**
     * 创建指定分割正则和日期检测开关的解析器。
     *
     * @param splitRegex           分割正则表达式
     * @param dateDetectionEnabled 是否开启日期类型推断
     * @return 新创建的版本解析器
     */
    public static VersionParser ofPattern(String splitRegex, boolean dateDetectionEnabled) {
        return new PatternVersionParser(splitRegex, dateDetectionEnabled);
    }

    // ==================== 校验 ====================

    /**
     * 校验版本字符串能否被默认解析器成功解析。
     *
     * @param version 版本字符串
     * @return true 表示可被成功解析
     */
    public static boolean isValid(String version) {
        try {
            parse(version);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 使用指定解析器校验版本字符串。
     *
     * @param version 版本字符串
     * @param parser  版本解析器
     * @return true 表示可被成功解析
     */
    public static boolean isValid(String version, VersionParser parser) {
        try {
            parser.parse(version);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
