package com.tingfeng.util.java.base.common.utils;

import java.util.*;

import com.tingfeng.util.java.base.common.inter.ConvertI;

/**
 *
 */
public class CollectionUtils {

    public static <T> List<T> getListByArray(T[] array) {
        if (array == null) return null;
        return Arrays.asList(array);
    }

    public static <T> List<T> getObjectList(String souceString, String regex, ConvertI<T, String> convert) {
        List<T> list = new ArrayList<T>();
        souceString = souceString.trim();
        if (souceString.length() < 1) return list;
        try {
            String[] ss = souceString.split(regex);
            for (String s : ss) {
                T t = null;
                try {
                    t = convert.convert(s);
                } catch (FormatFlagsConversionMismatchException e) {
                }
                list.add(t);
            }
        } catch (Exception e) {
        }
        return list;
    }

    /**
     * @param souceString
     * @param regex       分隔标志
     * @return
     */
    public static List<String> getStringList(String souceString, String regex) {
        return getObjectList(souceString, regex, new ConvertI<String, String>() {
            @Override
            public String convert(String e) {
                return e;
            }
        });
    }

    /**
     * 默认以逗号作为分割
     *
     * @param souceString
     * @return
     */
    public static List<String> getStringList(String souceString) {
        return getStringList(souceString, ",");
    }

    /**
     * @param souceString
     * @param regex       分隔标志
     * @return
     */
    public static List<Integer> getIntegerList(String souceString, String regex) {
        return getObjectList(souceString, regex, new ConvertI<Integer, String>() {
            @Override
            public Integer convert(String e) {
                return Integer.parseInt(e);
            }
        });
    }

    public static List<Integer> getIntegerList(String souceString) {
        return getIntegerList(souceString, ",");
    }

    /**
     * @param souceString 源字符串
     * @param regex       正则表达式 分隔标志
     * @return
     */
    public static List<Long> getLongList(String souceString, String regex) {
        return getObjectList(souceString, regex, new ConvertI<Long, String>() {
            @Override
            public Long convert(String e) {
                return Long.parseLong(e);
            }
        });
    }

    /**
     * 默认以逗号作为分隔符
     *
     * @param souceString
     * @return
     */
    public static List<Long> getLongList(String souceString) {
        return getLongList(souceString, ",");
    }

    /**
     * 泛型方法(通用)，把list转换成以“,”相隔的字符串 调用时注意类型初始化（申明类型） 如：List<Integer> intList = new ArrayList<Integer>(); 调用方法：StringUtils.listTtoString(intList); 效率：list中4条信息，1000000次调用时间为850ms左右
     *
     * @param <T>  泛型
     * @param list
     * @return 以symbol分隔的字符串
     * @author fengliang
     * @serialData 2008-01-09
     * @params symbol
     * list列表
     */
    public static <T> String toListString(List<T> list, String symbol) {
        if (list == null || list.size() < 1)
            return "";
        Iterator<T> i = list.iterator();
        if (!i.hasNext())
            return "";
        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            T e = i.next();
            sb.append(e);
            if (!i.hasNext())
                return sb.toString();
            sb.append(symbol);
        }
    }

    /**
     * 默认返回以,分隔的字符串
     *
     * @param list
     * @return
     */
    public static <T> String toListString(List<T> list) {
        return toListString(list, ",");
    }


}
