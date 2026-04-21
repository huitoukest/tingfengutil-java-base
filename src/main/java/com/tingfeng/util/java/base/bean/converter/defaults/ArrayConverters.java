package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.array.ArrayUtils;
import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;
import com.tingfeng.util.java.base.bean.converter.ConverterUtils;

/**
 * 数组类型转换器注册
 * <p>
 * primitive array ↔ wrapper array 转换
 */
public final class ArrayConverters {

    private ArrayConverters() {}

    public static void register(ConverterRegistry registry) {
        // ========== primitive array -> wrapper array（简单转换器）==========

        // int[] -> Integer[]
        registry.register(ConverterUtils.of(
                int[].class, Integer[].class,
                ArrayConverters::intToInteger
        ));

        // long[] -> Long[]
        registry.register(ConverterUtils.of(
                long[].class, Long[].class,
                ArrayConverters::longToLong
        ));

        // double[] -> Double[]
        registry.register(ConverterUtils.of(
                double[].class, Double[].class,
                ArrayConverters::doubleToDouble
        ));

        // float[] -> Float[]
        registry.register(ConverterUtils.of(
                float[].class, Float[].class,
                ArrayConverters::floatToFloat
        ));

        // short[] -> Short[]
        registry.register(ConverterUtils.of(
                short[].class, Short[].class,
                ArrayConverters::shortToShort
        ));

        // byte[] -> Byte[]
        registry.register(ConverterUtils.of(
                byte[].class, Byte[].class,
                ArrayConverters::byteToByte
        ));

        // boolean[] -> Boolean[]
        registry.register(ConverterUtils.of(
                boolean[].class, Boolean[].class,
                ArrayConverters::booleanToBoolean
        ));

        // char[] -> Character[]
        registry.register(ConverterUtils.of(
                char[].class, Character[].class,
                ArrayConverters::charToCharacter
        ));

        // ========== wrapper array -> primitive array（条件转换器，仅非 null 时）==========

        // Integer[] -> int[]
        registry.register(ConverterUtils.of(
                Integer[].class, int[].class,
                ArrayUtils::allNonNull,
                ArrayConverters::integerToInt
        ));

        // Long[] -> long[]
        registry.register(ConverterUtils.of(
                Long[].class, long[].class,
                ArrayUtils::allNonNull,
                ArrayConverters::longToLongPrimitive
        ));

        // Double[] -> double[]
        registry.register(ConverterUtils.of(
                Double[].class, double[].class,
                ArrayUtils::allNonNull,
                ArrayConverters::doubleToDoublePrimitive
        ));

        // Float[] -> float[]
        registry.register(ConverterUtils.of(
                Float[].class, float[].class,
                ArrayUtils::allNonNull,
                ArrayConverters::floatToFloatPrimitive
        ));

        // Short[] -> short[]
        registry.register(ConverterUtils.of(
                Short[].class, short[].class,
                ArrayUtils::allNonNull,
                ArrayConverters::shortToShortPrimitive
        ));

        // Byte[] -> byte[]
        registry.register(ConverterUtils.of(
                Byte[].class, byte[].class,
                ArrayUtils::allNonNull,
                ArrayConverters::byteToBytePrimitive
        ));

        // Boolean[] -> boolean[]
        registry.register(ConverterUtils.of(
                Boolean[].class, boolean[].class,
                ArrayUtils::allNonNull,
                ArrayConverters::booleanToBooleanPrimitive
        ));

        // Character[] -> char[]
        registry.register(ConverterUtils.of(
                Character[].class, char[].class,
                ArrayUtils::allNonNull,
                ArrayConverters::characterToCharPrimitive
        ));
    }

    // ========== 辅助方法：primitive -> wrapper ==========

    private static Integer[] intToInteger(int[] arr) {
        if (arr == null) return null;
        Integer[] result = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static Long[] longToLong(long[] arr) {
        if (arr == null) return null;
        Long[] result = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static Double[] doubleToDouble(double[] arr) {
        if (arr == null) return null;
        Double[] result = new Double[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static Float[] floatToFloat(float[] arr) {
        if (arr == null) return null;
        Float[] result = new Float[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static Short[] shortToShort(short[] arr) {
        if (arr == null) return null;
        Short[] result = new Short[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static Byte[] byteToByte(byte[] arr) {
        if (arr == null) return null;
        Byte[] result = new Byte[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static Boolean[] booleanToBoolean(boolean[] arr) {
        if (arr == null) return null;
        Boolean[] result = new Boolean[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static Character[] charToCharacter(char[] arr) {
        if (arr == null) return null;
        Character[] result = new Character[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    // ========== 辅助方法：wrapper -> primitive ==========

    private static int[] integerToInt(Integer[] arr) {
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static long[] longToLongPrimitive(Long[] arr) {
        long[] result = new long[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static double[] doubleToDoublePrimitive(Double[] arr) {
        double[] result = new double[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static float[] floatToFloatPrimitive(Float[] arr) {
        float[] result = new float[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static short[] shortToShortPrimitive(Short[] arr) {
        short[] result = new short[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static byte[] byteToBytePrimitive(Byte[] arr) {
        byte[] result = new byte[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static boolean[] booleanToBooleanPrimitive(Boolean[] arr) {
        boolean[] result = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }

    private static char[] characterToCharPrimitive(Character[] arr) {
        char[] result = new char[arr.length];
        for (int i = 0; i < arr.length; i++) { result[i] = arr[i]; }
        return result;
    }
}