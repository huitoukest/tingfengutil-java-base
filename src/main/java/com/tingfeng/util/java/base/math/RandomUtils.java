package com.tingfeng.util.java.base.math;

import com.tingfeng.util.java.base.array.ArrayUtils;
import com.tingfeng.util.java.base.common.constant.RandomType;
import com.tingfeng.util.java.base.lang.ObjectUtils;
import com.tingfeng.util.java.base.lang.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
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

    /**
     * 易混淆字符集合(数字0与字母O、数字1与字母l/I等)
     */
    private static final String AMBIGUOUS_CHARS = "0O1lI";

    /**
     * 剔除易混淆字符后的随机字符串字符集(数字+小写字母+大写字母)，本地计算，不污染RANDOM_TYPE_MAP缓存
     */
    private static final char[] RANDOM_STRING_EXCLUDE_AMBIGUOUS_CHARS = Stream.of(
            RandomType.NUMBER.getStrValue(),
            RandomType.LOWER_CHAR.getStrValue(),
            RandomType.UPPER_CHAR.getStrValue())
            .collect(Collectors.joining())
            .chars()
            .filter(c -> AMBIGUOUS_CHARS.indexOf(c) < 0)
            .collect(StringBuilder::new, (sb, c) -> sb.append((char) c), StringBuilder::append)
            .toString()
            .toCharArray();

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
        return randomString(length, RandomType.NUMBER, RandomType.LOWER_CHAR, RandomType.UPPER_CHAR);
    }

    /**
     * 返回一个定长的随机字符串(包含RandomType中指定的字符)
     *
     * @param length      随机字符串长度
     * @param randomTypes 随机字符类型组合
     * @return 随机字符串
     */
    public static String randomString(int length, RandomType... randomTypes) {
        if (length < 0) {
            throw new IllegalArgumentException("随机字符串长度不能小于0: " + length);
        }
        if (randomTypes == null || randomTypes.length == 0) {
            throw new IllegalArgumentException("随机字符类型不能为空");
        }
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
        return randomString(length, RandomType.LOWER_CHAR, RandomType.UPPER_CHAR);
    }

    /**
     * 返回一个定长的随机纯大写字母字符串(只包含大写字母)
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomUpperString(int length) {
        return randomString(length, RandomType.UPPER_CHAR);
    }

    /**
     * 返回一个定长的随机纯小写字母字符串(只包含小写字母)
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomLowerString(int length) {
        return randomString(length, RandomType.LOWER_CHAR);
    }

    /**
     * 返回一个定长的随机数字字符串
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomNumberString(int length) {
        return randomString(length, RandomType.NUMBER);
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
     * @param exchangeOffsetList  打乱时使用的位置的移动偏移量列表，不能为null或空列表，值必须都大于等于0，值为0时该位置不变；列表为null或空时返回原列表副本
     * @return 打乱后的列表(不会影响原列表)
     */
    public static <T> List<T> shuffleByExchange(List<T> srcList, List<Integer> exchangeOffsetList) {
        if (ObjectUtils.isEmpty(srcList)) {
            return Collections.emptyList();
        }
        if (ObjectUtils.isEmpty(exchangeOffsetList)) {
            return new ArrayList<>(srcList);
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
     * @param exchangeOffsetList  打乱时使用的位置的移动偏移量列表，不能为null或空列表，值必须都大于等于0，值为0时该位置不变；列表为null或空时不做任何操作
     */
    public static <T> void shuffleByExchange(T[] srcArray, List<Integer> exchangeOffsetList) {
        if (srcArray == null || srcArray.length == 0) {
            return;
        }
        if (ObjectUtils.isEmpty(exchangeOffsetList)) {
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
                    return (int) (Math.abs(result) % Integer.MAX_VALUE);
                })
                .collect(Collectors.toList());
    }

    /**
     * 从集合中随机返回一个元素
     *
     * @param src 来源集合，不能为null或空
     * @return 随机元素
     */
    public static <T> T randomElement(Collection<T> src) {
        if (src == null || src.isEmpty()) {
            throw new IllegalArgumentException("随机取样的集合不能为null或空");
        }
        int index = ThreadLocalRandom.current().nextInt(src.size());
        Iterator<T> iterator = src.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator.next();
    }

    /**
     * 从集合中随机返回指定数量的不重复元素
     *
     * @param src   来源集合，不能为null
     * @param count 需要返回的元素数量，必须大于等于0且不超过集合大小
     * @return 随机元素列表(元素不重复，顺序随机)
     */
    public static <T> List<T> randomElements(Collection<T> src, int count) {
        if (src == null) {
            throw new IllegalArgumentException("随机取样的集合不能为null");
        }
        if (count < 0 || count > src.size()) {
            throw new IllegalArgumentException("随机取样数量必须大于等于0且不超过集合大小: " + count);
        }
        List<T> copy = new ArrayList<>(src);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            int swapIndex = random.nextInt(i, copy.size());
            Collections.swap(copy, i, swapIndex);
        }
        return new ArrayList<>(copy.subList(0, count));
    }

    /**
     * 返回一个随机boolean值
     *
     * @return 随机boolean值
     */
    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * 返回一个定长的随机字符串，已剔除易混淆字符(数字0与字母O、数字1与字母l/I等)
     *
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomStringExcludeAmbiguous(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("随机字符串长度不能小于0: " + length);
        }
        char[] chars = RANDOM_STRING_EXCLUDE_AMBIGUOUS_CHARS;
        return StringUtils.doAppend(sb -> {
            for (int i = 0; i < length; i++) {
                sb.append(chars[ThreadLocalRandom.current().nextInt(chars.length)]);
            }
            return sb.toString();
        });
    }

    /**
     * 返回指定范围内的随机BigDecimal值(保留指定小数位数)
     *
     * @param origin 最小值(包含)
     * @param bound  最大值(不包含)
     * @param scale  小数位数，必须大于等于0
     * @return 大于等于origin、小于bound的随机值按指定小数位数四舍五入后的BigDecimal
     */
    public static BigDecimal randomBigDecimal(double origin, double bound, int scale) {
        if (origin >= bound) {
            throw new IllegalArgumentException("随机范围起始值必须小于结束值: " + origin + " >= " + bound);
        }
        if (scale < 0) {
            throw new IllegalArgumentException("小数位数不能小于0: " + scale);
        }
        double value = ThreadLocalRandom.current().nextDouble(origin, bound);
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * 按权重随机返回一个元素（轮盘赌算法）
     *
     * 遍历所有权重累加得到总权重，再生成 [0, total) 的随机数，命中累计分布的对应区间即返回该元素；
     * 结果只依赖累计分布，与 Map 的遍历顺序无关。
     *
     * @param weightMap 元素到权重的映射，不能为 null 或空；每个权重必须为非 null、非 NaN、非无穷的有限非负值，且总权重必须大于 0
     * @param <T>       元素类型
     * @return 按权重比例随机选中的元素
     */
    public static <T> T weightedRandom(Map<T, Double> weightMap) {
        if (weightMap == null || weightMap.isEmpty()) {
            throw new IllegalArgumentException("权重映射不能为null或空");
        }
        double total = 0.0;
        for (Map.Entry<T, Double> entry : weightMap.entrySet()) {
            Double weight = entry.getValue();
            if (weight == null || Double.isNaN(weight) || Double.isInfinite(weight) || weight < 0.0) {
                throw new IllegalArgumentException("权重必须为非null、非NaN、非无穷的有限非负值: " + weight);
            }
            total += weight;
        }
        if (total <= 0.0) {
            throw new IllegalArgumentException("总权重必须大于0");
        }
        double randomValue = ThreadLocalRandom.current().nextDouble(0.0, total);
        double cumulative = 0.0;
        T lastKey = null;
        for (Map.Entry<T, Double> entry : weightMap.entrySet()) {
            lastKey = entry.getKey();
            cumulative += entry.getValue();
            if (randomValue < cumulative) {
                return entry.getKey();
            }
        }
        // 浮点累计尾差防御：随机值落在累计分布末端时返回最后一个元素（正常路径不可达）
        return lastKey;
    }

    /**
     * 返回指定范围内的随机 BigInteger 值（拒绝采样）
     *
     * 以 range = bound - origin 的 bitLength 位生成随机数，值域 [0, 2^bitLength)，
     * 拒绝不小于 range 的值后重新采样，保证结果在 [origin, bound) 上均匀分布；
     * 因 2^(m-1) ≤ range < 2^m，单次接受概率至少为 1/2，期望重试不超过 2 次。
     *
     * @param origin 最小值（包含）
     * @param bound  最大值（不包含）
     * @return 大于等于 origin、小于 bound 的随机 BigInteger
     */
    public static BigInteger randomBigInteger(BigInteger origin, BigInteger bound) {
        if (origin == null || bound == null) {
            throw new IllegalArgumentException("随机范围边界不能为null");
        }
        if (origin.compareTo(bound) >= 0) {
            throw new IllegalArgumentException("随机范围起始值必须小于结束值: " + origin + " >= " + bound);
        }
        BigInteger range = bound.subtract(origin);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        BigInteger value;
        do {
            value = new BigInteger(range.bitLength(), random);
        } while (value.compareTo(range) >= 0);
        return origin.add(value);
    }
}
