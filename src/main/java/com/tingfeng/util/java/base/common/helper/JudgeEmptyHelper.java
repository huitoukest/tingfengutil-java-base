package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.bean.tuple.Tuple2;
import com.tingfeng.util.java.base.common.bean.tuple.Tuple3;
import com.tingfeng.util.java.base.common.inter.ObjectDealReturnInter;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.*;

import static com.tingfeng.util.java.base.common.utils.ObjectUtils.isEmpty;

/**
 * 对象判空的Helper
 * @author wanggang
 */
public class JudgeEmptyHelper  implements ObjectDealReturnInter<Boolean> {

    private boolean recursive;
    private boolean isTrim;

    private static final JudgeEmptyHelper INSTANCE = new JudgeEmptyHelper();
    private static volatile  Map<String, JudgeEmptyHelper> helperMap = new HashMap<>();
    /**
     * 保存判断条件和返回数组长度,以及,需要处理的数组内的元素
     */
    private static final List<Tuple3<Predicate<Object>,Function<Object,Integer>,Function<Object,BaseStream>>> ARRAY_EMPTY_HANDLER = new ArrayList();
    static {
        ARRAY_EMPTY_HANDLER.add(new Tuple3<>(obj -> obj instanceof  Object[],obj -> ((Object[])obj).length ,obj -> Stream.of((Object[])obj)));
        ARRAY_EMPTY_HANDLER.add(new Tuple3<>(obj -> obj instanceof  int[],obj -> ((int[])obj).length , obj -> IntStream.of((int[])obj)));
        ARRAY_EMPTY_HANDLER.add(new Tuple3<>(obj -> obj instanceof  long[],obj -> ((long[])obj).length , obj -> LongStream.of((long[])obj)));
        ARRAY_EMPTY_HANDLER.add(new Tuple3<>(obj -> obj instanceof  double[],obj -> ((double[])obj).length , obj -> DoubleStream.of((double[])obj)));
        ARRAY_EMPTY_HANDLER.add(new Tuple3<>(obj -> obj instanceof  byte[],obj -> ((byte[])obj).length , obj -> {
            byte[] arr = ((byte[]) obj);
            return IntStream.range(0, arr.length)
                     .mapToObj(index -> arr[index]);
        }));
        ARRAY_EMPTY_HANDLER.add(new Tuple3<>(obj -> obj instanceof  char[],obj -> ((char[])obj).length , obj -> {
            char[] arr = ((char[]) obj);
            return IntStream.range(0, arr.length)
                    .mapToObj(index -> arr[index]);
        }));
        ARRAY_EMPTY_HANDLER.add(new Tuple3<>(obj -> obj instanceof  float[],obj -> ((float[])obj).length , obj -> {
            float[] arr = ((float[]) obj);
            return IntStream.range(0, arr.length)
                    .mapToObj(index -> arr[index]);
        }));
        ARRAY_EMPTY_HANDLER.add(new Tuple3<>(obj -> obj instanceof  short[],obj -> ((short[])obj).length , obj -> {
            short[] arr = ((short[]) obj);
            return IntStream.range(0, arr.length)
                    .mapToObj(index -> arr[index]);
        }));
        ARRAY_EMPTY_HANDLER.add(new Tuple3<>(obj -> obj instanceof  boolean[],obj -> ((boolean[])obj).length , obj -> {
            boolean[] arr = ((boolean[]) obj);
            return IntStream.range(0, arr.length)
                    .mapToObj(index -> arr[index]);
        }));
    }


    private JudgeEmptyHelper(){
        this.recursive = false;
        this.isTrim = true;
    }

    private JudgeEmptyHelper(boolean recursive, boolean isTrim) {
        this.recursive = recursive;
        this.isTrim = isTrim;
    }

    /**
     * double check ,提高性能
     * @param recursive
     * @param isTrim
     * @return
     */
    public static JudgeEmptyHelper newInstance(boolean recursive, boolean isTrim) {
        String key = getKey(recursive,isTrim);
        if(helperMap.get(key) == null){
            synchronized (helperMap){
                if(helperMap.get(key) == null) {
                    helperMap.put(key, new JudgeEmptyHelper(recursive, isTrim));
                }
            }
        }
        return helperMap.get(key);
    }

    /**
     * 默认不递归，但是去除字符序列首尾的空格;
     * @return
     */
    public static JudgeEmptyHelper newInstance() {
        return newInstance(false,true);
    }

    private static String getKey(boolean recursive, boolean isTrim){
        return String.valueOf(recursive) + String.valueOf(isTrim);
    }

    @Override
    public Boolean dealCollection(Collection<?> data) {
        if (data.isEmpty()) {
            return true;
        }
        if (recursive) {
            for (Object key : data) {
                if (!isEmpty(key, recursive, isTrim)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean dealMap(Map<?, ?> map) {
        if (map.isEmpty()) {
            return true;
        }
        if (recursive) {
            for (Object key : map.keySet()) {
                if (!isEmpty(key, recursive, isTrim)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean dealArray(Object source) {
        if(source == null){
            return true;
        }
        Tuple3<Predicate<Object>, Function<Object, Integer>, Function<Object, BaseStream>> functionTuple3 = ARRAY_EMPTY_HANDLER.stream()
                .filter(it -> it.get_1().test(source))
                .findFirst().get();
        if(functionTuple3.get_2().apply(source) == 0){
            return true;
        }
        if(!recursive){
            return false;
        }
        Iterator iterator = functionTuple3.get_3()
                .apply(source)
                .iterator();
        while (iterator.hasNext()){
            Object next = iterator.next();
            if (!isEmpty(next, recursive, isTrim)) {
                return false;
            }
        }
        return true;
    }



    @Override
    public Boolean dealCharSequence(CharSequence obj) {
        String str = obj.toString();
        if (isTrim) {
            str = str.trim();
        }
        if ("".equals(str)) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean dealDate(Date obj) {
        if (null == obj) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean dealCommonObject(Object obj) {
        if (null == obj) {
            return true;
        }
        return false;
    }
}
