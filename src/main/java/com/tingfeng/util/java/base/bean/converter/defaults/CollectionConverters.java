package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 集合与数组类型转换器注册
 */
public final class CollectionConverters {

    private CollectionConverters() {}

    public static void register(ConverterRegistry registry) {
        registry.register(ConverterUtils.of(
                Object[].class, List.class,
                arr -> Arrays.asList(arr)
        ));

        registry.register(ConverterUtils.of(
                int[].class, Integer[].class,
                CollectionConverters::intArrayToIntegerArray
        ));
        registry.register(ConverterUtils.of(
                long[].class, Long[].class,
                CollectionConverters::longArrayToLongArray
        ));
        registry.register(ConverterUtils.of(
                double[].class, Double[].class,
                CollectionConverters::doubleArrayToDoubleArray
        ));
        registry.register(ConverterUtils.of(
                float[].class, Float[].class,
                CollectionConverters::floatArrayToFloatArray
        ));
        registry.register(ConverterUtils.of(
                short[].class, Short[].class,
                CollectionConverters::shortArrayToShortArray
        ));
        registry.register(ConverterUtils.of(
                byte[].class, Byte[].class,
                CollectionConverters::byteArrayToByteArray
        ));
        registry.register(ConverterUtils.of(
                boolean[].class, Boolean[].class,
                CollectionConverters::booleanArrayToBooleanArray
        ));

        registry.register(ConverterUtils.of(
                List.class, Object[].class,
                list -> list.toArray()
        ));
    }

    private static Integer[] intArrayToIntegerArray(int[] arr) {
        if (arr == null) {
            return null;
        }
        Integer[] result = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    private static Long[] longArrayToLongArray(long[] arr) {
        if (arr == null) {
            return null;
        }
        Long[] result = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    private static Double[] doubleArrayToDoubleArray(double[] arr) {
        if (arr == null) {
            return null;
        }
        Double[] result = new Double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    private static Float[] floatArrayToFloatArray(float[] arr) {
        if (arr == null) {
            return null;
        }
        Float[] result = new Float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    private static Short[] shortArrayToShortArray(short[] arr) {
        if (arr == null) {
            return null;
        }
        Short[] result = new Short[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    private static Byte[] byteArrayToByteArray(byte[] arr) {
        if (arr == null) {
            return null;
        }
        Byte[] result = new Byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    private static Boolean[] booleanArrayToBooleanArray(boolean[] arr) {
        if (arr == null) {
            return null;
        }
        Boolean[] result = new Boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }
}
