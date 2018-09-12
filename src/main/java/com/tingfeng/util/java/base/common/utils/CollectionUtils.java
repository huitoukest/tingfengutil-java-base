package com.tingfeng.util.java.base.common.utils;

import java.lang.reflect.Field;
import java.util.*;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

/**
 *
 */
public class CollectionUtils {

    public static <T> List<T> getList(T[] array) {
        if (array == null) return null;
        return Arrays.asList(array);
    }

    /**
     * 将一个字符串按照指定规则转换为指定的List对象
     * @param sourceString
     * @param regex
     * @param convert
     * @param <T>
     * @return
     */
    public static <T> List<T> getList(String sourceString, String regex, ConvertI<T, String> convert) {
        List<T> list = new ArrayList<T>();
        sourceString = sourceString.trim();
        if (sourceString.length() < 1) return list;
        try {
            String[] ss = sourceString.split(regex);
            for (String s : ss) {
                T t = null;
                try {
                    t = convert.convert(s);
                } catch (FormatFlagsConversionMismatchException e) {
                }
                list.add(t);
            }
        } catch (Exception e) {
            throw new BaseException(e);
        }
        return list;
    }

    /**
     * @param sourceString
     * @param regex       分隔标志
     * @return
     */
    public static List<String> getStringList(String sourceString, String regex) {
        return getList(sourceString, regex, new ConvertI<String, String>() {
            @Override
            public String convert(String e) {
                return e;
            }
        });
    }

    /**
     * 默认以逗号作为分割
     *
     * @param sourceString
     * @return
     */
    public static List<String> getStringList(String sourceString) {
        return getStringList(sourceString, ",");
    }

    /**
     * @param sourceString
     * @param regex       分隔标志
     * @return
     */
    public static List<Integer> getIntegerList(String sourceString, String regex) {
        return getList(sourceString, regex, new ConvertI<Integer, String>() {
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
     * @param sourceString 源字符串
     * @param regex       正则表达式 分隔标志
     * @return
     */
    public static List<Long> getLongList(String sourceString, String regex) {
        return getList(sourceString, regex, new ConvertI<Long, String>() {
            @Override
            public Long convert(String e) {
                return Long.parseLong(e);
            }
        });
    }

    /**
     * 默认以逗号作为分隔符
     *
     * @param sourceString
     * @return
     */
    public static List<Long> getLongList(String sourceString) {
        return getLongList(sourceString, ",");
    }

    /**
     * 泛型方法(通用)，把list转换成以“,”相隔的字符串 调用时注意类型初始化（申明类型） 如：List<Integer> intList = new ArrayList<Integer>(); 调用方法：StringUtils.listTtoString(intList); 效率：list中4条信息，1000000次调用时间为850ms左右
     *
     * @param <T>  泛型
     * @param collection
     * @return 以symbol分隔的字符串
     * @author fengliang
     * @serialData 2008-01-09
     * @params symbol
     * list列表
     */
    public static <T> String join(Collection<T> collection, String symbol) {
        if (collection == null || collection.size() < 1) {
            return "";
        }
        Iterator<T> i = collection.iterator();
        if (!i.hasNext()) {
            return "";
        }
        boolean isAppend = symbol.length() > 0 ? true : false;
        return StringUtils.doAppend(sb->{
            for (; ; ) {
                T e = i.next();
                sb.append(e);
                if (!i.hasNext()) {
                    return sb.toString();
                }
                if(isAppend) {
                    sb.append(symbol);
                }
            }
        });
    }

    /**
     * 默认返回以,分隔的字符串
     *
     * @param list
     * @return
     */
    public static <T> String join(Collection<T> list) {
        return join(list, ",");
    }

    /**
     * obj是否在objects中
     * @param objects
     * @param obj
     * @return
     */
    public static boolean isContain(Collection<?> objects, Object obj) {
        return objects.contains(obj);
    }
}
