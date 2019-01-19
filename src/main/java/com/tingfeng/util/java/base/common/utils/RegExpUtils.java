package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.helper.SimpleCacheHelper;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 */
public class RegExpUtils {
    /**
     * 用来缓存用到的正则表达式提高效率；这里k是String，value是Pattern
     * 注意Matcher 是一个非线程安全，所以调用matcher.find()是线程不安全的
     */
    private static final  SimpleCacheHelper<String,Pattern> patternCache = new SimpleCacheHelper<>(50);

    /**
     * 用来存储各种Pattern
     */
    public interface PatternStr{
         String integer = "^[\\-\\+]{0,1}[1-9][0-9]*$";
         String floatValue = "(^[\\-\\+]{0,1}([1-9][0-9]*\\.[0-9]+|0\\.[0-9]+)$";
         String intOrFloatNumber = "^[\\-\\+]([0-9]*$|^0+\\.[0-9]+$|^[1-9]+[0-9]*$|^[1-9]+[0-9]*.[0-9]+)$";
         String split = ",|，|;|；|、|\\.|。|-|_|\\(|\\)|\\[|\\]|\\{|\\}|\\\\|/| |　|\"";
         String blank = "\\s*|\t|\r|\n";
         String letter = "^[a-zA-Z]+$";
         String url = "^((ht|f)tps?):\\/\\/([\\w\\-]+(\\.[\\w\\-]+)*\\/)*[\\w\\-]+(\\.[\\w\\-]+)*\\/?(\\?([\\w\\-\\.,@?^=%&:\\/~\\+#]*)+)?";
         String http = "^((http)s?):\\/\\/";
         String email = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";
         String age  = "^[1-9][0-9]{0,1}$";
         String birthday  = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$";
         String phoneCN = "^(13\\d|14[57]|15[012356789]|18\\d|17[01678]|19[89]|166)\\d{8}$";
         String notInteger = "[^\\d]";
         String ipV4 = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\." + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        /**
         * group 1是具体的status
         */
         String httpStatus = "HTTPS?/[\\d\\.]+\\s(\\d+)\\s\\S+";
    }


    private static String getPatternKey(String regex,int flags){
        return StringUtils.appendValue(false,new Object[]{flags,"_",regex});
    }

    /* 先从缓存中取Pattern，没有则新建并写入缓存中
     * 默认Pattern.CASE_INSENSITIVE
     * @param regex 正则表达式
     * @return
     */
    public static Pattern getPattern(String regex){
        return getPattern(regex,Pattern.CASE_INSENSITIVE);
    }

    /**
     * 先从缓存中取Pattern，没有则新建并写入缓存中
     * @param regex 正则表达式
     * @param flags ，同Pattern.compile(regex,flags)
     * @return
     */
    public static Pattern getPattern(String regex,int flags){
        if(null == regex){
           return null;
        }
        String patternKey =  getPatternKey(regex,flags);
        Pattern pattern = patternCache.get(patternKey);
        if(pattern == null){
            pattern = Pattern.compile(regex,flags);
            patternCache.set(patternKey,pattern);
        }
        return pattern;
    }

    /**
     * 是否匹配某个正则表达式
     *
     * @param str   字符串内容
     * @param regex 正则表达式
     * @param isMathAll 是否匹配整个字符串
     * @return
     */
    public static boolean isMatch(String str,String regex,boolean isMathAll) {
        if(str == null || regex == null){
            return false;
        }
        Pattern pattern = getPattern(regex);
        Matcher matcher = pattern.matcher(str);
        if(isMathAll){
            return matcher.matches();
        }else {
            return matcher.find();
        }
    }

    /**
     * 是否匹配某个正则表达式,默认字符串str的一部分匹配即可(不要求一定匹配整个字符串)
     * @param str   字符串内容
     * @param regex 正则表达式
     * @return
     */
    public static boolean isMatch( String str,String regex) {
        return isMatch(str,regex,false);
    }


    /**
     * 判断是否是整数
     *
     * @param src 源字符串
     * @return 是否整数
     */
    public static boolean isIntegerNumber(String src) {
        return isMatch(src,PatternStr.integer,true);
    }

    /**
     * 判断是否浮点数
     *
     * @param src 源字符串
     * @return 是否浮点数
     */
    public static boolean isFloatNumber(String src) {
        return isMatch(src,PatternStr.floatValue,true);
    }

    /**
     * 判断是否纯字母组合
     *
     * @param src 源字符串
     * @return 是否纯字母组合的标志
     */
    public static boolean isLetter(String src) {
        return isMatch(src,PatternStr.letter,true);
    }

}
