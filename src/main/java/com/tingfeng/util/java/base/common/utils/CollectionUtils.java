package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.inter.returnfunction.FunctionRTwo;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author huitoukest
 */
public class CollectionUtils {

    public static <T> List<T> getList(T[] array) {
        if (array == null) {
            return null;
        }
        return Arrays.asList(array);
    }

    /**
     * 将一个字符串按照指定规则转换为指定的List对象
     *
     * @param sourceString
     * @param regex
     * @param convert
     * @param <T>
     * @return
     */
    public static <T> List<T> getList(String sourceString, String regex, ConvertI<String, T> convert) {
        List<T> list = new ArrayList<T>();
        sourceString = sourceString.trim();
        if (sourceString.length() < 1) {
            return list;
        }
        try {
            String[] ss = sourceString.split(regex);
            for (String s : ss) {
                T t = null;
                try {
                    t = convert.apply(s);
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
     * @param regex        分隔标志
     * @return
     */
    public static List<String> getStringList(String sourceString, String regex) {
        return getList(sourceString, regex, it -> it);
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
     * @param regex        分隔标志
     * @return
     */
    public static List<Integer> getIntegerList(String sourceString, String regex) {
        return getList(sourceString, regex, it -> Integer.parseInt(it));
    }

    public static List<Integer> getIntegerList(String souceString) {
        return getIntegerList(souceString, ",");
    }

    /**
     * @param sourceString 源字符串
     * @param regex        正则表达式 分隔标志
     * @return
     */
    public static List<Long> getLongList(String sourceString, String regex) {
        return getList(sourceString, regex, it -> Long.parseLong(it));
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
     * 泛型方法(通用)，把list转换成以“,”相隔的字符串 调用时注意类型初始化（申明类型）
     * 如：List&lt;Integer&gt; intList = new ArrayList&lt;Integer&gt;();
     * 调用方法：StringUtils.listTtoString(intList); 效率：list中4条信息，1000000次调用时间为850ms左右
     *
     * @param <T>        泛型
     * @param collection
     * @param symbol 分隔标识符号
     * @return 以symbol分隔的字符串
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
        return StringUtils.doAppend(sb -> {
            for (; ; ) {
                T e = i.next();
                sb.append(e);
                if (!i.hasNext()) {
                    return sb.toString();
                }
                if (isAppend) {
                    sb.append(symbol);
                }
            }
        });
    }

    /**
     * 默认返回以,分隔的字符串
     *
     * @param collection
     * @return
     */
    public static <T> String join(Collection<T> collection) {
        return join(collection, ",");
    }

    /**
     * obj是否在objects中
     *
     * @param objects
     * @param obj
     * @return
     */
    public static boolean isContain(Collection<?> objects, Object obj) {
        return objects.contains(obj);
    }

    /**
     * objectsA 中 是否 存在 objectsB 中指定条件的一个数据(可以理解为有某一个自动的交集);
     * 最终比较targetValue 是否有相同的
     *
     * @param objectsA
     * @param objectsB
     * @param aToTarget function to from A to targetValue
     * @param bToTarget function to from B to targetValue
     * @return
     */
    public static <A, B, T> boolean isContainAny(Collection<A> objectsA, Collection<B> objectsB, Function<A, T> aToTarget, Function<B, T> bToTarget) {
        List<T> listA = objectsA.stream().map(aToTarget).collect(Collectors.toList());
        Set<T> setB = objectsB.stream().map(bToTarget).collect(Collectors.toSet());
        return null != listA.stream().filter(it -> setB.contains(it)).findFirst().orElse(null);
    }

    /**
     * 获取容器的一个值，如果没有则返回null
     *
     * @param set
     * @param <T>
     * @return
     */
    public static <T> T getOne(Set<T> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.stream().findAny().orElse(null);
    }

    /**
     * 获取容器的第一个值（如果有序），如果没有则返回null
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T getFirst(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().findFirst().orElse(null);
    }

    /**
     * 获取容器的最后一个值（如果有序），如果没有则返回null
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T getLast(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().skip(list.size() - 1).findFirst().orElse(null);
    }

    /**
     * 判断两个List的内容是否相同；大小和内容;
     *
     * @param listA
     * @param listB
     * @param <T>
     * @return
     */
    public static <T> boolean eq(List<T> listA, List<T> listB) {
        return eq(listA, listA, (a, b) -> a.equals(b));
    }

    /**
     * 判断两个List的内容是否相同；大小和内容;
     *
     * @param listA
     * @param listB
     * @param eqFunc 判断相等的方法
     * @param <T>
     * @return
     */
    public static <T, E> boolean eq(List<T> listA, List<E> listB, BiFunction<T, E, Boolean> eqFunc) {
        int size = listA.size();
        if (size != listB.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!eqFunc.apply(listA.get(i), listB.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断两个Set的内容是否相同；大小和内容;
     *
     * @param a
     * @param b
     * @param <T>
     * @return
     */
    public static <T> boolean eq(Set<T> a, Set<T> b) {
        int size = a.size();
        if (size != b.size()) {
            return false;
        }
        return !a.stream().filter(it -> !b.contains(it))
                .findAny().isPresent();
    }

    /**
     * 通过数组创建Set
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> Set<T> createSet(T... data) {
        return Arrays.asList(data).stream().collect(Collectors.toSet());
    }

    /**
     * 将list 按照 groupSize 拆分并返回
     *
     * @param list      来源list
     * @param groupSize 分组拆分后的子List中的最大长度值
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> split(List<T> list, int groupSize) {
        List<List<T>> reList = new ArrayList<>();
        int listSize = list.size();
        int i = 0;
        while (i < listSize) {
            int start = i;
            i = i + groupSize;
            int end = Math.min(listSize, i);
            reList.add(list.subList(start, end));
        }
        return reList;
    }

    /**
     * 创建一个List,并加入所有集合中的元素
     *
     * @param <T>
     * @return
     */
    public static <T> List<T> mergeToList(Collection<T>... collections) {
        List<T> list = new ArrayList<>();
        if (collections != null) {
            for (Collection<T> collection : collections) {
                list.addAll(collection);
            }
        }
        return list;
    }

    /**
     * 返回集合中的特定元素
     *
     * @param collection   if null or empty , return defaultValue
     * @param predicate    if null , not use
     * @param defaultValue
     * @param function     通过stream 查找特定值
     * @param <T>
     * @return 集合中的第一个元素 or defaultValue
     */
    public static <T> T findValue(Collection<T> collection, Predicate<? super T> predicate, T defaultValue, Function<Stream<T>, Optional<T>> function) {
        if (collection == null) {
            return defaultValue;
        }
        Stream<T> stream = collection.stream();
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        return function.apply(stream).orElse(defaultValue);
    }

    /**
     * 返回集合中的符合条件的第一个元素
     *
     * @param collection   if null or empty , return defaultValue
     * @param predicate    if null , not use
     * @param defaultValue
     * @param <T>
     * @return 集合中的第一个元素 or defaultValue
     */
    public static <T> T findFirst(Collection<T> collection, Predicate<? super T> predicate, T defaultValue) {
        return findValue(collection, predicate, defaultValue, Stream::findFirst);
    }

    /**
     * 返回集合中的符合条件的第一个元素
     *
     * @param collection if null or empty , return defaultValue
     * @param <T>
     * @return 集合中的第一个元素 or null
     */
    public static <T> T findFirst(Collection<T> collection) {
        return findFirst(collection, null, null);
    }

    /**
     * 返回集合中的符合条件的任意一个元素
     *
     * @param collection   if null or empty , return defaultValue
     * @param predicate    if null , not use
     * @param defaultValue
     * @param <T>
     * @return 集合中的第一个元素 or defaultValue
     */
    public static <T> T findAny(Collection<T> collection, Predicate<? super T> predicate, T defaultValue) {
        return findValue(collection, predicate, defaultValue, Stream::findAny);
    }

    /**
     * 返回集合中的符合条件的任意一个元素
     *
     * @param collection if null or empty , return defaultValue
     * @param <T>
     * @return 集合中的第一个元素 or null
     */
    public static <T> T findAny(Collection<T> collection) {
        return findAny(collection, null, null);
    }

    /**
     * 将一个集合中的数据变为String.
     * @param collection 集合数据 ; if null or empty , return empty String
     * @param open 字符串开始符号 ; if null or empty , not use
     * @param close 字符串结尾符号 ; if null or empty , not use
     * @param separator  拼接集合中元素的分隔符 if null or empty , not use
     * @param itemToStrF 将集合中元素转为String.  FunctionRTwo[返回类型: String, 当前遍历的元素: T, 索引Index : Integer]
     * @param <T>
     * @return
     */
    public static <T> String toString(Collection<T> collection, String open, String close, String separator, FunctionRTwo<String, T, Integer> itemToStrF) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }
        Iterator<T> i = collection.iterator();
        if (!i.hasNext()) {
            return "";
        }
        boolean isAppend = StringUtils.isNotEmpty(separator);
        return StringUtils.doAppend(sb -> {
            if(StringUtils.isNotEmpty(open)){
                sb.append(open);
            }
            for (int index = 0; ; index++) {
                T e = i.next();
                sb.append(itemToStrF.run(e, index));
                if (!i.hasNext()) {
                    break;
                }
                if (isAppend) {
                    sb.append(separator);
                }
            }
            if(StringUtils.isNotEmpty(close)){
                sb.append(close);
            }
            return sb.toString();
        });
    }

    /**
     * 将一个集合中的数据变为String.
     * @param collection 集合数据 ; if null or empty , return empty String
     * @param open 字符串开始符号 ; if null or empty , not use
     * @param close 字符串结尾符号 ; if null or empty , not use
     * @param separator  拼接集合中元素的分隔符 if null or empty , not use
     * @param itemToStrF 将集合中元素转为String.  Function[当前遍历的元素: T, 返回类型: String]
     * @param <T>
     * @return
     */
    public static <T> String toString(Collection<T> collection, String open, String close, String separator, Function<T, String> itemToStrF) {
        return toString(collection, open, close, separator, (item , index) -> itemToStrF.apply(item));
    }
}
