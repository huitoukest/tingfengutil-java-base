package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.text.RegExpUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串搜索、区间查找、分割与正则提取操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link StringUtils} 对外提供统一 API。
 */
class StringSearchOps {

    private StringSearchOps() {

    }

    /**
     * souceString默认使用的是逗号作为分隔符号
     * 去掉首尾的空白字符和symbol字符串
     *
     * @param souceString
     * @param symbol
     * @return
     */
    static String trimSymbol(String souceString, String symbol) {
        souceString = souceString.trim();
        if (souceString.length() < 1) {
            return souceString;
        }
        if (souceString.indexOf(symbol) == 0) {
            souceString = souceString.substring(symbol.length());
        }
        if (souceString.lastIndexOf(symbol) == souceString.length() - symbol.length()) {
            souceString = souceString.substring(0, souceString.length() - symbol.length());
        }
        return souceString;
    }

    /**
     * 根据传入的分割符号,把传入的字符串分割为List字符串
     *
     * @param src              字符串
     * @param splitRegexSymbol 分隔的正则表达式字符串,
     * @return 列表
     */
    static String[] split(String src, String splitRegexSymbol) {
        if (src == null) {
            return null;
        }
        return src.split(splitRegexSymbol);
    }

    /**
     * word是否包含大写字符串
     *
     * @param word
     * @return
     */
    static boolean isContainUpperCase(String word) {
        for (int i = 0; i < word.length(); ++i) {
            char c = word.charAt(i);
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个字符串是否是存在于数组中某一个子串的subStr
     *
     * @param stringArray 原数组
     * @param source      查找的字符串
     * @return 是否找到
     */
    static boolean isAnyItemContainsSource(String[] stringArray, String source) {
        // 转换为list
        List<String> tempList = Arrays.asList(stringArray);
        for (String s : tempList) {
            if (s != null && s.indexOf(source) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * source 中是否包含items中的某个item的字符串
     *
     * @param source 如果source is null，返回false
     * @param items
     * @return
     */
    static boolean isContainsAnyItem(String source, List<String> items) {
        if (null == source) {
            return false;
        }
        for (String s : items) {
            if (s != null && source.indexOf(s) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * source中是否包含items中所有的字符串
     *
     * @param source 如果source is null，返回false
     * @param items
     * @return
     */
    static boolean isContainsAllItem(String source, List<String> items) {
        if (null == source) {
            return false;
        }
        for (String s : items) {
            if (s != null && source.indexOf(s) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 是否包含小写字符串
     *
     * @param s
     * @return
     */
    static boolean isContainLowerCase(String s) {
        char[] arr = StringUtils.getCharArray(s);
        int len = arr.length;
        for (int i = 0; i < len; ++i) {
            char c = arr[i];
            if (Character.isLowerCase(c)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 页面中去除字符串中的空格、回车、换行符、制表符
     *
     * @param str
     * @return
     */
    static String replaceBlank(String str) {
        if (str != null) {
            Pattern pattern = RegExpUtils.getPattern(RegExpUtils.PatternStr.BLANK);
            Matcher m = pattern.matcher(str);
            str = m.replaceAll("");
        }
        return str;
    }

    /**
     * 字符串替换
     *
     * @param str        源字符串
     * @param regEx      正则表达式样式
     * @param insteadStr 替换文本
     * @return 结果串
     */
    static String replaceByReg(String str, String regEx, String insteadStr) {
        Pattern p = RegExpUtils.getPattern(regEx);
        Matcher m = p.matcher(str);
        str = m.replaceAll(insteadStr);
        return str;
    }

    /**
     * 得到字符串的regExp匹配的位置序列
     *
     * @param str    字符串
     * @param regExp 正则表达式
     * @return 字符串的子串位置序列
     */
    static List<Integer> getSubStringPositions(String str, String regExp) {
        if (str == null || regExp == null) {
            return Collections.emptyList();
        }
        String[] sp = StringUtils.split(str, regExp);
        if (sp == null || sp.length < 1) {
            return Collections.emptyList();
        }
        int spIndex = 0;
        int lastIndex = 0;
        if (str.indexOf(sp[0]) == 0) {
            lastIndex = sp[0].length();
            ++spIndex;
        }
        List<Integer> positions = new ArrayList<>();
        Pattern p = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(str);
        while (matcher.find() && spIndex < sp.length) {
            positions.add(lastIndex + 1);
            lastIndex = lastIndex + matcher.group(0).length();
            if (spIndex + 1 < sp.length) {
                lastIndex = lastIndex + sp[++spIndex].length();
            } else {
                spIndex++;
            }
        }
        return positions;
    }

    /**
     * 根据正则表达式提取字符串,相同的字符串只返回一个
     *
     * @param str    源字符串
     * @param regExp 正则表达式
     * @return 目标字符串数据组
     * ************************************************************************
     */
    static Set<String> getStringsByPattern(String str, String regExp) {
        if (str == null || regExp == null) {
            return Collections.emptySet();
        }
        Pattern p = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(str);
        // 范型
        Set<String> result = new HashSet<String>();// 目的是：相同的字符串只返回一个。。。 不重复元素
        // boolean find() 尝试在目标字符串里查找下一个匹配子串。
        while (matcher.find()) {
            // 添加整个匹配的字符串
            result.add(matcher.group(0));
        }
        return result;

    }

    /**
     * 如果str或者subStr为null，返回-1 （-1 表示不存在匹配的子串），否则返回其子串在主串中的索引值
     * 一般情况下，朴素indexOf的速度更快，前缀的重复率越高，KMP的速度越快
     *
     * @param sArray          主串
     * @param pArray       子串、模式串
     * @param kmpNextArray KMP的next数组，传入null时会自动获取
     * @param skipLength 跳过主串中前面skipLength个长度的字符
     * @return
     */
    static int indexOfByKMP(char[] sArray, char[] pArray, int[] kmpNextArray, int skipLength) {
        if (kmpNextArray == null) {
            kmpNextArray = getKmpNextArray(pArray);
        }

        int i = skipLength;
        int maxLength = sArray.length - skipLength;
        int pLength = pArray.length;
        int j = 0;
        while (j < pLength && i < maxLength) {
            //①如果j = -1，或者当前字符匹配成功（即S[i] == P[j]），都令i++，j++
            if (j == -1 || sArray[i] == pArray[j]) {
                //①如果当前字符匹配成功（即S[i] == P[j]），则i++，j++
                i++;
                j++;
            } else {
                //②如果j != -1，且当前字符匹配失败（即S[i] != P[j]），则令 i 不变，j = next[j]
                //next[j]即为j所对应的next值
                j = kmpNextArray[j];
            }
        }
        //匹配成功，返回模式串p在文本串s中的位置，否则返回-1
        if (j == pArray.length) {
            return i - j;
        }
        return -1;

    }

    /**
     * 如果str或者subStr为null，返回-1 （-1 表示不存在匹配的子串），否则返回其子串在主串中的索引值
     * 一般情况下，朴素indexOf的速度更快，前缀的重复率越高，KMP的速度越快
     *
     * @param sArray          主串
     * @param pArray       子串、模式串
     * @param kmpNextArray KMP的next数组，传入null时会自动获取
     * @return
     */
    static int indexOfByKMP(char[] sArray, char[] pArray, int[] kmpNextArray) {
        return indexOfByKMP(sArray, pArray, kmpNextArray, 0);
    }

    /**
     * 如果str或者subStr为null，返回-1 （-1 表示不存在匹配的子串），否则返回其子串在主串中的索引值
     * 一般情况下，朴素indexOf的速度更快，前缀的重复率越高，KMP的速度越快
     *
     * @param str          主串
     * @param subStr       子串、模式串
     * @param kmpNextArray KMP的next数组，传入null时会自动获取
     * @return
     */
    static int indexOfByKMP(String str, String subStr, int[] kmpNextArray) {
        return indexOfByKMP(StringUtils.getCharArray(str), StringUtils.getCharArray(subStr), kmpNextArray);
    }

    /**
     * 如果str或者subStr为null，返回-1 （-1 表示不存在匹配的子串），否则返回其子串在主串中的索引值
     * 使用KMP算法搜索字符串
     *
     * @param str    主串
     * @param subStr 子串
     * @return
     */
    static int indexOfByKMP(String str, String subStr) {
        return indexOfByKMP(str, subStr, null);
    }

    /**
     * 求出KMP算法中的next数组，优化过后的next 数组求法
     *
     * @param pArray 模式串的char数组内容
     * @return
     */
    static int[] getKmpNextArray(char[] pArray) {
        int[] next = new int[pArray.length];
        next[0] = -1;
        int k = -1;
        int j = 0;
        int maxIndex = pArray.length - 1;
        while (j < maxIndex) {
            //p[k]表示前缀，p[j]表示后缀
            if (k == -1 || pArray[j] == pArray[k]) {
                ++j;
                ++k;
                //较之前next数组求法，改动在下面4行
                if (pArray[j] != pArray[k]) {
                    //之前只有这一行
                    next[j] = k;
                } else {
                    //因为不能出现p[j] = p[ next[j ]]，所以当出现时需要继续递归，k = next[k] = next[next[k]]
                    //优化之前这里是next[j] = k; 但是因为当前 pArray[j] = pArray[k]的时候，由于前后缀相等，
                    //所以在第一次失配并移动后，移动的位置的当前字符其实仍然是失配的，所以只要值相等，这里就递归前移
                    next[j] = next[k];
                }
            } else {
                k = next[k];
            }
        }
        return next;
    }

    /**
     * 从jdk拷贝出来的工具
     * Code shared by String and StringBuffer to do searches. The
     * source is the character array being searched, and the target
     * is the string being searched for.
     *
     * @param   source       the characters being searched.
     * @param   sourceOffset offset of the source string.
     * @param   sourceCount  count of the source string.
     * @param   target       the characters being searched for.
     * @param   targetOffset offset of the target string.
     * @param   targetCount  count of the target string.
     * @param   fromIndex    the index to begin searching from.
     */
    static int indexOf(char[] source, int sourceOffset, int sourceCount,
                       char[] target, int targetOffset, int targetCount,
                       int fromIndex) {
        if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }

        char first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);

        for (int i = sourceOffset + fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (source[i] != first) {
                while (++i <= max && source[i] != first) {
                    ;
                }
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for (int k = targetOffset + 1; j < end && source[j]
                        == target[k]; j++, k++) {
                    ;
                }

                if (j == end) {
                    /* Found whole string. */
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    /**
     * 在source的指定区间查找source
     *
     * @param source 源字符串
     * @param target 目标字符串
     * @param fromIndex 开始搜索的索引
     * @param endIndex 结束搜索的索引
     * @return 目标字符串在源字符串中的位置，未找到返回-1
     */
    static int indexOf(String source, String target, int fromIndex, int endIndex) {
        if (source == null || target == null) {
            return -1;
        }
        char[] sourceValue = StringUtils.getCharArray(source);
        char[] targetValue = StringUtils.getCharArray(target);
        if (sourceValue == null || targetValue == null) {
            return -1;
        }
        // 确保 sourceCount 不超过 sourceValue.length
        int sourceCount = Math.min(endIndex + 1, sourceValue.length);
        return indexOf(sourceValue, 0, sourceCount,
                targetValue, 0, targetValue.length, fromIndex);
    }

    /**
     * 从jdk中拷贝出来的工具；
     * Code shared by String and StringBuffer to do searches. The
     * source is the character array being searched, and the target
     * is the string being searched for.
     *
     * @param   source       the characters being searched.
     * @param   sourceOffset offset of the source string.
     * @param   sourceCount  count of the source string.
     * @param   target       the characters being searched for.
     * @param   targetOffset offset of the target string.
     * @param   targetCount  count of the target string.
     * @param   fromIndex    the index to begin searching from. 从fromIndex往前查找
     */
    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
                           char[] target, int targetOffset, int targetCount,
                           int fromIndex) {
        /*
         * Check arguments; return immediately where possible. For
         * consistency, don't check for null str.
         */
        int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        /* Empty string always matches. */
        if (targetCount == 0) {
            return fromIndex;
        }

        int strLastIndex = targetOffset + targetCount - 1;
        char strLastChar = target[strLastIndex];
        int min = sourceOffset + targetCount - 1;
        int i = min + fromIndex;

        startSearchForLastChar:
        while (true) {
            while (i >= min && source[i] != strLastChar) {
                i--;
            }
            if (i < min) {
                return -1;
            }
            int j = i - 1;
            int start = j - (targetCount - 1);
            int k = strLastIndex - 1;

            while (j > start) {
                if (source[j--] != target[k--]) {
                    i--;
                    continue startSearchForLastChar;
                }
            }
            return start - sourceOffset + 1;
        }
    }

    /**
     * 从jdk中拷贝出来的工具，做了点调整;
     * Code shared by String and AbstractStringBuilder to do searches. The
     * source is the character array being searched, and the target
     * is the string being searched for.
     *
     * @param   source       the characters being searched.
     * @param   sourceOffset offset of the source string.
     * @param   sourceCount  count of the source string.
     * @param   target       the characters being searched for.
     * @param   fromIndex    the index to begin searching from. 从fromIndex往前查找
     */
    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
                           String target, int fromIndex) {
        char[] targetValue = StringUtils.getCharArray(target);
        return lastIndexOf(source, sourceOffset, sourceCount,
                targetValue, 0, targetValue.length, fromIndex);
    }

    /**
     * 从指定区间查询最后一个匹配的索引；
     * 从jdk中拷贝出来的工具，做了点调整；包含搜索的开始和结束索引
     * Code shared by String and AbstractStringBuilder to do searches. The
     * source is the character array being searched, and the target
     * is the string being searched for.
     *
     * @param   source     the characters being searched.
     * @param   target       the characters being searched for.
     * @param   startIndex    搜索区间的开始索引
     * @param   endIndex   搜索区间的结束索引
     */
    static int lastIndexOf(String source, String target, int startIndex, int endIndex) {
        if (source == null || target == null) {
            return -1;
        }
        char[] sourceValue = StringUtils.getCharArray(source);
        if (sourceValue == null) {
            return -1;
        }
        int offsetIndex = lastIndexOf(sourceValue, startIndex, endIndex - startIndex + 1,
                target, endIndex - startIndex);
        //默认返回的索引是相对于偏移量的，所以这里需要通过偏移量来恢复索引值
        return offsetIndex >= 0 ? offsetIndex + startIndex : offsetIndex;
    }

    /**
     * 自定义分割函数，返回全部
     *
     * @param str   待分割的字符串
     * @param delimitString 分隔符,字符串
     * @param limit 限制返回的List的数量
     * @return 分割后的返回结果
     */
    static List<String> splitByStr(String str, String delimitString, int limit) {
        if (null == str || limit <= 0) {
            return Collections.emptyList();
        }
        if (StringUtils.isEmpty(delimitString)) {
            return Arrays.asList(str);
        }

        List<String> stringList = new ArrayList<>();
        int lastIndex = 0;
        while (true && limit > 0) {
            int index = str.indexOf(delimitString, lastIndex);
            if (index < 0) {
                stringList.add(str.substring(lastIndex));
                break;
            } else {
                stringList.add(str.substring(lastIndex, index));
            }
            --limit;
            lastIndex = index + delimitString.length();
        }
        return stringList;
    }

    /**
     * 自定义分割函数，返回全部
     *
     * @param str   待分割的字符串
     * @param delimitString 分隔符,字符串
     * @return 分割后的返回结果
     */
    static List<String> splitByStr(String str, String delimitString) {
        return splitByStr(str, delimitString, Integer.MAX_VALUE);
    }

    /**
     * 判断两个字符串指定开始结束位置的 子串是否相等
     *
     * @param sourceStr 来源字符串
     * @param sourceStart 比较使用的来源的字符串的开始的 索引值
     * @param sourceEnd 比较使用的来源的字符串的 结束的 索引值
     * @param targetStr 目标字符串
     * @param targetStart 比较使用的目标的字符串的开始的 索引值
     * @param targetEnd 比较使用的目标的字符串的 结束的 索引值
     * @return 是否相等
     */
    static boolean equals(String sourceStr, int sourceStart, int sourceEnd, String targetStr, int targetStart, int targetEnd) {
        int sourceLength = sourceEnd - sourceStart;
        int endLength = targetEnd - targetStart;
        if (sourceLength != endLength) {
            return false;
        }
        for (int i = sourceStart, j = targetStart; i < sourceEnd && j < targetEnd; i++, j++) {
            if (sourceStr.charAt(i) != targetStr.charAt(j)) {
                return false;
            }
        }
        return true;
    }
}
