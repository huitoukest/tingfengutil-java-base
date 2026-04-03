package com.tingfeng.util.java.base.common.utils;

import com.tingfeng.util.java.base.common.constant.RandomType;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * 生成随机数/随机字符串的一个工具类
 *
 * @author huitoukest
 * @version 20180912
 */
public class RandomUtils {

    /**
     * 用来保存类型和char数组的对应关系
     */
    private static final ConcurrentHashMap<String, char[]> RANDOM_TYPE_MAP = new ConcurrentHashMap<>();

    private static char[] getRandomValue(RandomType... randomTypes) {
        String key = ArrayUtils.join(randomTypes);
        return RANDOM_TYPE_MAP.computeIfAbsent(key, k -> {
            String strValue = Stream.of(randomTypes)
                    .map(RandomType::getStrValue)
                    .collect(Collectors.joining());
            return strValue.toCharArray();
        });
    }

    /**
     * 返回一个定长的随机字符串(包含字母和数字)
     *
     * @param length 指定长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        return randomString(length, RandomType.number, RandomType.lowerChar, RandomType.upperChar);
    }

    /**
     * 返回一个定长的随机字符串(包含RandomType中指定的字符)
     *
     * @param length      随机字符串长度
     * @param randomTypes 随机字符类型组合
     * @return 随机字符串
     */
    public static String randomString(int length, RandomType... randomTypes) {
        char[] chars = getRandomValue(randomTypes);
        int arrayLength = chars.length;
        return StringUtils.doAppend(sb -> {
            for (int i = 0; i < length; i++) {
                sb.append(chars[ThreadLocalRandom.current().nextInt(arrayLength)]);
            }
            return sb.toString();
        });
    }

    /**
     * 返回一个定长的随机纯字母字符串(只包含大小写字母)
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomLetterString(int length) {
        return randomString(length, RandomType.lowerChar, RandomType.upperChar);
    }

    /**
     * 返回一个定长的随机纯大写字母字符串(只包含大写字母)
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomUpperString(int length) {
        return randomString(length, RandomType.upperChar);
    }

    /**
     * 返回一个定长的随机纯小写字母字符串(只包含小写字母)
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomLowerString(int length) {
        return randomString(length, RandomType.lowerChar);
    }

    /**
     * 返回一个定长的随机数字字符串
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomNumberString(int length) {
        return randomString(length, RandomType.number);
    }

    /**
     * 返回指定范围内的随机double值
     *
     * @param origin the least value returned
     * @param bound  the upper bound (exclusive)
     * @return 返回大于等于origin，小于bound的值
     */
    public static double randomDouble(double origin, double bound) {
        return ThreadLocalRandom.current().nextDouble(origin, bound);
    }

    /**
     * 返回指定范围内的随机int值
     * 默认开始值为0
     * @param bound  the upper bound (exclusive)
     * @return 返回大于等于0，小于bound的值
     */
    public static int randomInt(int bound) {
        return ThreadLocalRandom.current().nextInt(0, bound);
    }

    /**
     * 返回指定范围内的随机int值
     *
     * @param origin the least value returned
     * @param bound  the upper bound (exclusive)
     * @return 返回大于等于origin，小于bound的值
     */
    public static int randomInt(int origin, int bound) {
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    /**
     * 返回指定范围内的随机long值
     *
     * @param origin the least value returned
     * @param bound  the upper bound (exclusive)
     * @return 返回大于等于origin，小于bound的值
     */
    public static long randomLong(long origin, long bound) {
        return ThreadLocalRandom.current().nextLong(origin, bound);
    }

    /**
     * 返回小于bound的随机long值
     *
     * @param bound the upper bound (exclusive)
     * @return 返回大于等于0，小于bound的值
     */
    public static long randomLongBounded(long bound) {
        return ThreadLocalRandom.current().nextLong(bound);
    }

    /**
     * 返回小于bound的随机int值
     *
     * @param bound the upper bound (exclusive)
     * @return 返回大于等于0，小于bound的值
     */
    public static int randomIntBounded(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    /**
     * 产生一个Long范围内的随机正数
     *
     * @return 随机long值
     */
    public static long randomLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    /**
     * 产生一个int范围内的随机正数
     *
     * @return 随机int值
     */
    public static int randomInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    /**
     * 产生一个double范围内的随机正数
     *
     * @return 随机double值
     */
    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * 产生一个float范围内的随机正数
     *
     * @return 随机float值
     */
    public static float randomFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    /**
     * 返回长度是128位的UUID随机字符串
     *
     * @return UUID字符串
     */
    public static String randomUUid() {
        return randomUUid(false);
    }

    /**
     * 返回UUID随机字符串
     *
     * @param removeSplit 是否去掉分隔符
     * @return UUID字符串
     */
    public static String randomUUid(boolean removeSplit) {
        if (removeSplit) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return UUID.randomUUID().toString();
    }

    /**
     * 打乱来源列表顺序
     *
     * @param srcList             来源数据
     * @param exchangeOffsetList  打乱时使用的位置的移动偏移量列表，值必须都大于等于0，值为0时该位置不变
     * @return 打乱后的列表(不会影响原列表)
     */
    public static <T> List<T> shuffleByExchange(List<T> srcList, List<Integer> exchangeOffsetList) {
        if (ObjectUtils.isEmpty(srcList)) {
            return Collections.emptyList();
        }
        List<T> targetIds = new ArrayList<>(srcList);
        for (int i = 0; i < targetIds.size(); i++) {
            int exchangeOffsetIndex = i % exchangeOffsetList.size();
            int exchangeOffsetValue = exchangeOffsetList.get(exchangeOffsetIndex);
            T beforeShuffleValue = targetIds.get(i);
            int shuffleIndex = Math.floorMod(i + exchangeOffsetValue, targetIds.size());
            targetIds.set(i, targetIds.get(shuffleIndex));
            targetIds.set(shuffleIndex, beforeShuffleValue);
        }
        return targetIds;
    }

    /**
     * 打乱来源数组顺序
     *
     * @param srcArray            来源数据
     * @param exchangeOffsetList  打乱时使用的位置的移动偏移量列表，值必须都大于等于0，值为0时该位置不变
     */
    public static <T> void shuffleByExchange(T[] srcArray, List<Integer> exchangeOffsetList) {
        if (srcArray == null || srcArray.length == 0) {
            return;
        }
        for (int i = 0; i < srcArray.length; i++) {
            int exchangeOffsetIndex = i % exchangeOffsetList.size();
            int exchangeOffsetValue = exchangeOffsetList.get(exchangeOffsetIndex);
            T beforeShuffleValue = srcArray[i];
            int shuffleIndex = Math.floorMod(i + exchangeOffsetValue, srcArray.length);
            srcArray[i] = srcArray[shuffleIndex];
            srcArray[shuffleIndex] = beforeShuffleValue;
        }
    }

    /**
     * 获取默认的打乱偏移量列表
     *
     * @param seed  种子，建议值在1 ~ 10000
     * @param count 偏移量的数量，建议设置长度在4~20
     * @return 偏移量列表
     */
    public static List<Integer> getDefaultExchangeOffsetValues(int seed, int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> {
                    long result = (long) index + seed / 101L + index * 31L - (long) seed * seed;
                    return (int) (result & 0xFFFFFFFFL);
                })
                .collect(Collectors.toList());
    }
}
