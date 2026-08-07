package com.tingfeng.util.java.base.math;

import com.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomUtilsNewTest extends BaseTest {

    /**
     * 易混淆字符集合，与实现独立硬编码，避免测试与实现耦合
     */
    private static final String AMBIGUOUS_CHARS = "0O1lI";

    @Test
    public void randomElement() {
        List<String> list = Arrays.asList("a", "b", "c", "d", "e");
        IntStream.range(0, 100).forEach(i -> {
            String element = RandomUtils.randomElement(list);
            Assert.assertTrue("元素必须来自原集合", list.contains(element));
        });
        // Set 无 get 方法，同样支持
        Set<String> set = new HashSet<>(list);
        IntStream.range(0, 100).forEach(i -> {
            String element = RandomUtils.randomElement(set);
            Assert.assertTrue("元素必须来自原集合", set.contains(element));
        });
        // 单元素集合恒返回该元素
        Assert.assertEquals("only", RandomUtils.randomElement(Collections.singletonList("only")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomElementWithNullSrc() {
        RandomUtils.randomElement(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomElementWithEmptySrc() {
        RandomUtils.randomElement(Collections.emptyList());
    }

    @Test
    public void randomElements() {
        List<String> list = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
        // 正常取样：数量正确且元素均来自原集合
        List<String> elements = RandomUtils.randomElements(list, 3);
        Assert.assertEquals(3, elements.size());
        Assert.assertTrue("元素必须来自原集合", list.containsAll(elements));
        Assert.assertEquals("元素不应重复", 3, new HashSet<>(elements).size());
        // 数量等于集合大小时返回全部元素(不重复)
        List<String> all = RandomUtils.randomElements(list, list.size());
        Assert.assertEquals(list.size(), all.size());
        Assert.assertEquals(new HashSet<>(list), new HashSet<>(all));
        // 数量为0时返回空列表
        Assert.assertTrue(RandomUtils.randomElements(list, 0).isEmpty());
        // 空集合取0个元素合法
        Assert.assertTrue(RandomUtils.randomElements(Collections.emptyList(), 0).isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomElementsWithNegativeCount() {
        RandomUtils.randomElements(Arrays.asList("a", "b"), -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomElementsWithCountOverSize() {
        RandomUtils.randomElements(Arrays.asList("a", "b"), 3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomElementsWithNullSrc() {
        RandomUtils.randomElements(null, 1);
    }

    @Test
    public void randomBoolean() {
        int trueCount = 0;
        int falseCount = 0;
        for (int i = 0; i < 100; i++) {
            if (RandomUtils.randomBoolean()) {
                trueCount++;
            } else {
                falseCount++;
            }
        }
        Assert.assertEquals(100, trueCount + falseCount);
        Assert.assertTrue("100次调用应出现两种取值", trueCount > 0 && falseCount > 0);
    }

    @Test
    public void randomStringExcludeAmbiguous() {
        // 单次定长验证
        String random = RandomUtils.randomStringExcludeAmbiguous(50);
        Assert.assertEquals(50, random.length());
        for (char c : random.toCharArray()) {
            Assert.assertTrue("不应包含易混淆字符: " + c, AMBIGUOUS_CHARS.indexOf(c) < 0);
        }
        // 多次采样验证
        IntStream.range(0, 10).forEach(i -> {
            String value = RandomUtils.randomStringExcludeAmbiguous(20);
            Assert.assertEquals(20, value.length());
            for (char c : value.toCharArray()) {
                Assert.assertTrue("不应包含易混淆字符: " + c, AMBIGUOUS_CHARS.indexOf(c) < 0);
            }
        });
        // 长度0合法
        Assert.assertEquals("", RandomUtils.randomStringExcludeAmbiguous(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomStringExcludeAmbiguousWithNegativeLength() {
        RandomUtils.randomStringExcludeAmbiguous(-1);
    }

    @Test
    public void randomBigDecimal() {
        IntStream.range(0, 100).forEach(i -> {
            BigDecimal value = RandomUtils.randomBigDecimal(1.0, 2.0, 2);
            Assert.assertTrue("值应大于等于origin", value.compareTo(BigDecimal.valueOf(1.0)) >= 0);
            // 四舍五入后可能等于bound(如1.9999999999999998舍入为2.00)，故用小于等于
            Assert.assertTrue("值应小于等于bound", value.compareTo(BigDecimal.valueOf(2.0)) <= 0);
            Assert.assertEquals("小数位数应为scale", 2, value.scale());
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomBigDecimalWithOriginEqualsBound() {
        RandomUtils.randomBigDecimal(1.0, 1.0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomBigDecimalWithOriginGreaterThanBound() {
        RandomUtils.randomBigDecimal(2.0, 1.0, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomBigDecimalWithNegativeScale() {
        RandomUtils.randomBigDecimal(1.0, 2.0, -1);
    }

    @Test
    public void weightedRandom() {
        // 单元素映射恒返回该元素
        Map<String, Double> single = Collections.singletonMap("only", 5.0);
        IntStream.range(0, 100).forEach(i -> Assert.assertEquals("only", RandomUtils.weightedRandom(single)));
        // 多元素映射：结果必须来自键集
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("a", 1.0);
        map.put("b", 2.0);
        map.put("c", 3.0);
        IntStream.range(0, 1000).forEach(i -> Assert.assertTrue("结果必须来自键集", map.containsKey(RandomUtils.weightedRandom(map))));
        // 权重为0的元素永不出现
        Map<String, Double> zeroWeight = new LinkedHashMap<>();
        zeroWeight.put("a", 1.0);
        zeroWeight.put("b", 0.0);
        IntStream.range(0, 500).forEach(i -> Assert.assertEquals("a", RandomUtils.weightedRandom(zeroWeight)));
    }

    @Test
    public void weightedRandomDistribution() {
        // 均匀性基本断言：90/10 权重下 2000 次采样，高权重元素次数显著多于低权重元素
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("a", 90.0);
        map.put("b", 10.0);
        int aCount = 0;
        int bCount = 0;
        for (int i = 0; i < 2000; i++) {
            if ("b".equals(RandomUtils.weightedRandom(map))) {
                bCount++;
            } else {
                aCount++;
            }
        }
        Assert.assertTrue("低权重元素应出现非零次数: " + bCount, bCount > 0);
        Assert.assertTrue("高权重元素次数应显著多于低权重元素: a=" + aCount + ", b=" + bCount, aCount > bCount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedRandomWithNullMap() {
        RandomUtils.weightedRandom(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedRandomWithEmptyMap() {
        RandomUtils.weightedRandom(Collections.emptyMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedRandomWithNegativeWeight() {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("a", 1.0);
        map.put("b", -1.0);
        RandomUtils.weightedRandom(map);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedRandomWithNanWeight() {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("a", Double.NaN);
        RandomUtils.weightedRandom(map);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedRandomWithNullWeight() {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("a", null);
        RandomUtils.weightedRandom(map);
    }

    @Test(expected = IllegalArgumentException.class)
    public void weightedRandomWithAllZeroWeights() {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("a", 0.0);
        map.put("b", 0.0);
        RandomUtils.weightedRandom(map);
    }

    @Test
    public void randomBigInteger() {
        // 区间断言：1000 次采样全部落在 [origin, bound)
        BigInteger origin = BigInteger.valueOf(10);
        BigInteger bound = BigInteger.valueOf(20);
        IntStream.range(0, 1000).forEach(i -> {
            BigInteger value = RandomUtils.randomBigInteger(origin, bound);
            Assert.assertTrue("值应大于等于origin: " + value, value.compareTo(origin) >= 0);
            Assert.assertTrue("值应小于bound: " + value, value.compareTo(bound) < 0);
        });
        // 零起点区间
        IntStream.range(0, 500).forEach(i -> {
            BigInteger value = RandomUtils.randomBigInteger(BigInteger.ZERO, BigInteger.valueOf(100));
            Assert.assertTrue("值应大于等于0: " + value, value.signum() >= 0);
            Assert.assertTrue("值应小于100: " + value, value.compareTo(BigInteger.valueOf(100)) < 0);
        });
    }

    @Test
    public void randomBigIntegerLargeRange() {
        // 大数区间（bitLength 大，覆盖拒绝采样重试路径）
        BigInteger origin = new BigInteger("1000000000000000000000000000000000000000");
        BigInteger bound = origin.add(new BigInteger("1000"));
        IntStream.range(0, 1000).forEach(i -> {
            BigInteger value = RandomUtils.randomBigInteger(origin, bound);
            Assert.assertTrue("值应大于等于origin: " + value, value.compareTo(origin) >= 0);
            Assert.assertTrue("值应小于bound: " + value, value.compareTo(bound) < 0);
        });
    }

    @Test
    public void randomBigIntegerRangeOne() {
        // range = 1（origin = bound - 1）恒返回 origin
        BigInteger origin = BigInteger.valueOf(42);
        IntStream.range(0, 100).forEach(i ->
                Assert.assertEquals(origin, RandomUtils.randomBigInteger(origin, origin.add(BigInteger.ONE))));
    }

    @Test
    public void randomBigIntegerBothValuesAppear() {
        // 均匀性基本断言：[0, 2) 采样 1000 次，0 和 1 均应出现
        int zeroCount = 0;
        int oneCount = 0;
        for (int i = 0; i < 1000; i++) {
            if (BigInteger.ZERO.equals(RandomUtils.randomBigInteger(BigInteger.ZERO, BigInteger.valueOf(2)))) {
                zeroCount++;
            } else {
                oneCount++;
            }
        }
        Assert.assertTrue("0应出现: " + zeroCount, zeroCount > 0);
        Assert.assertTrue("1应出现: " + oneCount, oneCount > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomBigIntegerWithOriginEqualsBound() {
        RandomUtils.randomBigInteger(BigInteger.TEN, BigInteger.TEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomBigIntegerWithOriginGreaterThanBound() {
        RandomUtils.randomBigInteger(BigInteger.TEN, BigInteger.ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomBigIntegerWithNullOrigin() {
        RandomUtils.randomBigInteger(null, BigInteger.TEN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomBigIntegerWithNullBound() {
        RandomUtils.randomBigInteger(BigInteger.TEN, null);
    }
}
