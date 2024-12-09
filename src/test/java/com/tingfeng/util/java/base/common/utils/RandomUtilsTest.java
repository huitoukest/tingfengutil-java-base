package com.tingfeng.util.java.base.common.utils;

import com.BaseTest;
import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomUtilsTest extends BaseTest {
    @Test
    public void testRandom(){
        Set<String> value = new HashSet<>();
        printTime(50,1000000,(index->{
                String random = RandomUtils.randomString(15);
                if(index % 100000 == 0) {
                    System.out.println(random);
                }
        }));
    }

    /**
     * 测试两种随机数产生方式的效率,Random的效率只有ThreadLocal的十分之一左右，
     * 而采用FixedPool之后测试效率来看threadLocalRandom的效率仍然要高50%以上
     */
    @Test
    public void testRandom1Time(){//5千万次
        /*printTime(50,1000000,(index->{
            Integer value = RandomUtils.doRandom(random -> {
                return random.nextInt();
            });
            if(index % 1000000 == 0) {
                System.out.println(value);
            }
        }));*/
    }

    @Test
    public void testThreadLocalRandomTime(){//5千万次
        printTime(50,1000000,(index->{
            Integer value = ThreadLocalRandom.current().nextInt();
            if(index % 1000000 == 0) {
                System.out.println(value);
            }
        }));
    }

    @Test
    public void testRandomInt(){
        printTime(50,1000000,(index->{
            Integer value = RandomUtils.randomInt(9,10);
            if(index % 1000000 == 0) {
                System.out.println(value);
            }
        }));
    }

    @Test
    public void shuffleByExchange() {
        List<Integer> list = IntStream.range(0,20).mapToObj(Integer::valueOf).collect(Collectors.toList());
        for (int i = 0; i < 10; i++) {
            System.out.println(JSON.toJSONString(RandomUtils.shuffleByExchange(list, RandomUtils.getDefaultExchangeOffsetValues(i,5))));
        }
    }

    @Test
    public void getDefaultExchangeOffsetValues() {
        IntStream.range(1,50)
                .forEach(seed -> System.out.println(JSON.toJSONString(RandomUtils.getDefaultExchangeOffsetValues(seed,20))));
    }
}
