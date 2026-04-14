package com.tingfeng.util.java.base.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 正则表达式工具类
 */
public class RegExpUtils {

    /**
     * 使用 ConcurrentHashMap 缓存编译后的 Pattern，避免重复 Pattern.compile()
     * computeIfAbsent 保证线程安全且只编译一次
     */
    private static final ConcurrentHashMap<String, Pattern> PATTERN_CACHE = new ConcurrentHashMap<>();

    /**
     * 常用正则表达式常量
     */
    public interface PatternStr {
        // ==================== 基础类型 ====================
        /** 整数（支持正负，不含前导零），如：123, -123, +123 */
        String INTEGER = "^[\\-\\+]{0,1}[1-9][0-9]*$";
        /** 浮点数（支持正负，支持 0.5、0.5、1.23 等格式），如：123.45, -123.45, +123.45 */
        String FLOAT_VALUE = "^[\\-\\+]{0,1}([1-9][0-9]*\\.[0-9]+|0\\.[0-9]+)$";
        /** 整数或浮点数，如：123, 123.45, -123, +123.45 */
        String NUMBER = "^[+\\-]?(\\d+\\.\\d+|\\d+)$";
        /** 自然数（0及正整数），如：0, 1, 123 */
        String NATURAL_NUMBER = "^\\d+$";
        /** 中文数字，如：零一二三四五六七八九十百千万 */
        String CHINESE_NUMBER = "^[零一二三四五六七八九十百千万]+$";

        // ==================== 标识符 ====================
        /** 英文字母，如：abc, ABC */
        String LETTER = "^[a-zA-Z]+$";
        /** 英文字母或数字，如：abc123 */
        String LETTER_OR_DIGIT = "^[a-zA-Z0-9]+$";
        /** 变量名（字母、数字、下划线，不能以数字开头），如：name, _private */
        String VARIABLE_NAME = "^[a-zA-Z_][a-zA-Z0-9_]*$";

        // ==================== 分隔符与空白 ====================
        /** 分隔符（逗号、顿号、分号、句号等） */
        String SPLIT = ",|，|;|；|、|\\.|。|-|_|\\(|\\)|\\[|\\]|\\{|\\}|\\\\|/| |　|\"";
        /** 空白字符 */
        String BLANK = "\\s*|\t|\r|\n";

        // ==================== 网络相关 ====================
        /** URL，如：http://example.com, https://example.com/path?query=value */
        String URL = "^((ht|f)tps?):\\/\\/([\\w\\-]+(\\.[\\w\\-]+)*\\/)*[\\w\\-]+(\\.[\\w\\-]+)*\\/?(\\?([\\w\\-\\.,@?^=%&:\\/~\\+#]*)+)?";
        /** HTTP/HTTPS 协议头，如：http://example.com, https://example.com */
        String HTTP = "^((http)s?)://.*";
        /** 邮箱，如：user@example.com */
        String EMAIL = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";
        /** 手机号（中国大陆），如：13812345678 */
        String PHONE_CN = "^(13\\d|14[57]|15[012356789]|18\\d|17[01678]|19[89]|166)\\d{8}$";
        /** 固定电话（中国），如：010-12345678 */
        String TELEPHONE_CN = "^\\d{3,4}-?\\d{7,8}$";

        // ==================== 身份与标识 ====================
        /** 身份证号（18位），如：11010119900101123X */
        String ID_CARD_CN_18 = "^[1-9]\\d{5}(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$";
        /** 年龄（1-99），如：18, 99 */
        String AGE = "^[1-9][0-9]{0,1}$";

        // ==================== 日期与时间 ====================
        /** 日期（yyyy-MM-dd），如：2024-01-01 */
        String BIRTHDAY = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$";
        /** 时间（HH:mm:ss），如：12:30:45 */
        String TIME = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$";
        /** 日期时间（yyyy-MM-dd HH:mm:ss），如：2024-01-01 12:30:45 */
        String DATETIME = "^\\d{4}-\\d{2}-\\d{2} [0-2]\\d:[0-5]\\d:[0-5]\\d$";

        // ==================== 网络协议 ====================
        /** IPv4 地址，如：192.168.1.1 */
        String IP_V4 = "^(?:(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";
        /** IPv6 地址（简化版），如：2001:0db8:85a3:0000:0000:8a2e:0370:7334 */
        String IP_V6 = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";
        /** MAC 地址，如：00:1A:2B:3C:4D:5E */
        String MAC_ADDRESS = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$";
        /** 端口号，如：80, 8080, 65535 */
        String PORT = "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$";

        // ==================== HTTP 相关 ====================
        /** HTTP 状态码，如：HTTP/1.1 200 OK */
        String HTTP_STATUS = "HTTPS?/[\\d\\.]+\\s(\\d+)\\s\\S+";

        // ==================== 其他 ====================
        /** 提取数字（匹配所有数字字符），用于 replaceAll */
        String NOT_INTEGER = "\\d";
        /** 中文字符 */
        String CHINESE = "[\\u4e00-\\u9fff]";
        /** HTML 标签，如：<div>, <p> */
        String HTML_TAG = "<[^>]+>";
        /** 脱敏手机号（保留前3后4），如：138****5678 */
        String MASK_PHONE_CN = "^1[3-9]\\d{4}\\d{4}$";
    }

    /**
     * 从缓存获取 Pattern，不存在则编译并写入缓存
     *
     * @param regex 正则表达式
     * @return Pattern
     */
    public static Pattern getPattern(String regex) {
        return getPattern(regex, 0);
    }

    /**
     * 从缓存获取 Pattern，不存在则编译并写入缓存
     *
     * @param regex 正则表达式
     * @param flags Pattern.compile 的 flags（默认 0）
     * @return Pattern
     */
    public static Pattern getPattern(String regex, int flags) {
        if (regex == null) {
            return null;
        }
        if (flags == 0) {
            return PATTERN_CACHE.computeIfAbsent(regex, Pattern::compile);
        }
        return Pattern.compile(regex, flags);
    }

    /**
     * 是否匹配某个正则表达式
     *
     * @param str      字符串内容
     * @param regex    正则表达式
     * @param matchAll true=匹配整个字符串，false=匹配部分
     * @return 是否匹配
     */
    public static boolean isMatch(String str, String regex, boolean matchAll) {
        if (str == null || regex == null) {
            return false;
        }
        Pattern pattern = getPattern(regex);
        Matcher matcher = pattern.matcher(str);
        if (matchAll) {
            return matcher.matches();
        } else {
            return matcher.find();
        }
    }

    /**
     * 是否匹配某个正则表达式（部分匹配）
     *
     * @param str   字符串内容
     * @param regex 正则表达式
     * @return 是否匹配
     */
    public static boolean isMatch(String str, String regex) {
        return isMatch(str, regex, false);
    }

    /**
     * 判断是否是整数
     *
     * @param src 源字符串
     * @return 是否整数
     */
    public static boolean isIntegerNumber(String src) {
        return isMatch(src, PatternStr.INTEGER, true);
    }

    /**
     * 判断是否浮点数
     *
     * @param src 源字符串
     * @return 是否浮点数
     */
    public static boolean isFloatNumber(String src) {
        return isMatch(src, PatternStr.FLOAT_VALUE, true);
    }

    /**
     * 判断是否纯字母组合
     *
     * @param src 源字符串
     * @return 是否纯字母组合的标志
     */
    public static boolean isLetter(String src) {
        return isMatch(src, PatternStr.LETTER, true);
    }
}
