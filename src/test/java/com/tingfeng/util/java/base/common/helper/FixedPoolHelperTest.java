package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class FixedPoolHelperTest {

    @Test
    public void fixedPoolHelperTest() throws InterruptedException {
        FixedPoolHelper<StringBuilder> fixedPoolHelper = new FixedPoolHelper(100,()->new StringBuilder());
        TestUtils.printTime(100,5000,(index) ->{
            String value = fixedPoolHelper.run(sb->{
                sb.setLength(0);
                sb.append("100");
                Thread.sleep(1);
                sb.append(123);
                return sb.toString();
            });
            if(index == 4000) {
                System.out.println(value);
            }
        });
    }


}
