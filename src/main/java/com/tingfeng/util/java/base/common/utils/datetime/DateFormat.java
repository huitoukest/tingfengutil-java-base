package com.tingfeng.util.java.base.common.utils.datetime;

/**
 * 日期的格式化定义
 */
public interface DateFormat {
    /** DateFormat:yyyyMMddHHmmssSSS */
    String FORMAT_YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";

    /** DateFormat:yyyyMMddHHmmss */
    String FORMAT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    /** DateFormat:yyyyMMdd */
    String FORMAT_YYYYMMDD = "yyyyMMdd";
    /** DateFormat:HH:mm:ss */
    String FORMAT_HHMMSS = "HHmmss";
    String FORMAT_YYYYMM = "yyyyMM";
    String FORMAT_YYYY = "yyyy";

    /** DateFormat:yyyy-MM-dd */
    String FORMAT_YYYYMMDD_THROUGH_LINE = "yyyy-MM-dd";
    /** DateFormat:yyyy-MM */
    String FORMAT_YYYYMM_THROUGH_LINE = "yyyy-MM";
    /** DateFormat:yyyy-MM-dd HH:mm:ss */
    String FORMAT_YYYYMMDDHHMMSS_THROUGH_LINE = "yyyy-MM-dd HH:mm:ss";
    /** DateFormat:yyyy-MM-dd HH:mm:ss.SSS */
    String FORMAT_YYYYMMDDHHMMSSSSS_THROUGH_LINE = "yyyy-MM-dd HH:mm:ss.SSS";

    /** 年月日格式 */
    String FORMAT_YYYYMMDD_CHN = "yyyy年MM月dd日";
    /** 年月格式 */
    String FORMAT_YYYYMM_CHN = "yyyy年MM月";

    /** 年月日格式时分秒 */
    String FORMAT_YYYYMMDDHHMMSS_CHN = "yyyy年MM月dd日 HH:mm:ss";
    /** 年月日格式 时分秒毫秒 */
    String FORMAT_YYYYMMDDHHMMSSSSS_CHN = "yyyy年MM月dd日 HH:mm:ss.SSS";

    /** yyyy/MM/dd */
    String FORMAT_YYYYMMDD_OBLINE = "yyyy/MM/dd";
    /** yyyy/MM/dd HH:mm:ss */
    String FORMAT_YYYYMMDDHHMMSS_OBLINE = "yyyy/MM/dd HH:mm:ss";
    /** yyyy/MM/dd HH:mm:ss.SSS*/
    String FORMAT_YYYYMMDDHHMMSSSSS_OBLINE = "yyyy/MM/dd HH:mm:ss.SSS";
    /**
     * yyyy-MM-dd'T'HH:mm:ss'Z' 国际标准UTC时间
     */
    String FORMAT_YYYYMMDDTHHMMSSZ_THROUGH_LINE = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * yyyy-MM-dd'T'HH:mm:ss.SSS'Z' 国际标准UTC时间
     */
    String FORMAT_YYYYMMDDTHHMMSSSSSZ_THROUGH_LINE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    /**
     * 格式化时分秒
     */
    String FORMAT_HHMMSS_THROUGH_LINE = "HH:mm:ss";

    /**
     * 格式化时分秒-毫秒
     */
    String FORMAT_HHMMSSSSS_THROUGH_LINE = "HH:mm:ss.SSS";
}
