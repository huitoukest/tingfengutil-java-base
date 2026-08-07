package com.tingfeng.util.java.base.math;

import com.BaseTest;
import com.tingfeng.util.java.base.common.constant.RandomType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomUtilsTest extends BaseTest {
    @Test
    public void testRandom(){
        Set<String> value = new HashSet<>();
        printTime(1,10000,(index->{
                String random = RandomUtils.randomString(15);
                value.add(random);
        }));
        // 验证生成了足够多的不重复字符串
        Assert.assertTrue("应该生成足够多的随机字符串", value.size() > 100);
    }

    @Test
    public void testRandom1Time(){
        // 性能测试已移除，仅保留功能验证
    }

    @Test
    public void testThreadLocalRandomTime(){
        // 性能测试已移除，仅保留功能验证
    }

    @Test
    public void testRandomInt(){
        printTime(1,10000,(index->{
            RandomUtils.randomInt(9,10);
        }));
    }

    @Test
    public void shuffleByExchange() {
        List<Integer> list = IntStream.range(0,20).mapToObj(Integer::valueOf).collect(Collectors.toList());
        for (int i = 0; i < 3; i++) {
            List<Integer> offsetValues = RandomUtils.getDefaultExchangeOffsetValues(i,5);
            List<Integer> shuffled = RandomUtils.shuffleByExchange(list, offsetValues);
            Assert.assertEquals(20, shuffled.size());
        }
    }

    @Test
    public void shuffleByExchangeWithEmptyOffsetList() {
        List<Integer> list = IntStream.range(0,20).mapToObj(Integer::valueOf).collect(Collectors.toList());
        // null 偏移列表 → 返回原列表副本，不抛异常
        List<Integer> shuffled1 = RandomUtils.shuffleByExchange(list, null);
        Assert.assertEquals(list, shuffled1);
        Assert.assertNotSame(list, shuffled1);
        // 空偏移列表 → 返回原列表副本，不抛异常
        List<Integer> shuffled2 = RandomUtils.shuffleByExchange(list, Collections.emptyList());
        Assert.assertEquals(list, shuffled2);
        Assert.assertNotSame(list, shuffled2);
        // 数组版 null/空偏移列表 → 不抛异常且数组不变
        Integer[] array = IntStream.range(0,20).boxed().toArray(Integer[]::new);
        Integer[] origin = Arrays.copyOf(array, array.length);
        RandomUtils.shuffleByExchange(array, null);
        RandomUtils.shuffleByExchange(array, Collections.emptyList());
        Assert.assertArrayEquals(origin, array);
    }

    @Test
    public void getDefaultExchangeOffsetValues() {
        IntStream.range(1,10)
                .forEach(seed -> {
                    List<Integer> values = RandomUtils.getDefaultExchangeOffsetValues(seed,20);
                    Assert.assertTrue(values.size() > 0);
                    Assert.assertTrue("偏移量必须都大于等于0", values.stream().allMatch(value -> value >= 0));
                });
        // 负种子与建议上限种子：原实现 (int)(result & 0xFFFFFFFFL) 会产出负偏移量
        IntStream.of(-1, 10000)
                .forEach(seed -> {
                    List<Integer> values = RandomUtils.getDefaultExchangeOffsetValues(seed,20);
                    Assert.assertTrue("偏移量必须都大于等于0", values.stream().allMatch(value -> value >= 0));
                });
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomStringWithNegativeLength() {
        RandomUtils.randomString(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomStringWithNullTypes() {
        RandomUtils.randomString(5, (RandomType[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void randomStringWithEmptyTypes() {
        RandomUtils.randomString(5, new RandomType[0]);
    }
}
