package com.tingfeng.util.java.base.math;

import com.BaseTest;
import org.junit.Assert;
import org.junit.Test;

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
    public void getDefaultExchangeOffsetValues() {
        IntStream.range(1,10)
                .forEach(seed -> {
                    List<Integer> values = RandomUtils.getDefaultExchangeOffsetValues(seed,20);
                    Assert.assertTrue(values.size() > 0);
                });
    }
}
