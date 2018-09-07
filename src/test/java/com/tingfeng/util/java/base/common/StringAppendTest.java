package com.tingfeng.util.java.base.common;

import com.BaseTest;
import com.tingfeng.util.java.base.common.helper.FixedPoolHelper;
import org.junit.Test;

/**
 * 测试StringBuilder和String、以及多线程优化的StringBuilder线程池的效率
 */
public class StringAppendTest extends BaseTest {
    private static final int DEFAULT_MAX_SB_SIZE = 16;
    /**
     * StringBuilder默认的初始长度，如果不设置会导致StringBuilder在内存中一直保持最大长度
     */
    private static final int DEFAULT_MAX_SB_LENGTH = 128;
    private static final FixedPoolHelper<StringBuilder> tokenStringBuilderPool = new FixedPoolHelper<>(DEFAULT_MAX_SB_SIZE,()->new StringBuilder(),(sb)->{
        int len = sb.length();
        if(len > DEFAULT_MAX_SB_LENGTH){
            sb.delete(DEFAULT_MAX_SB_LENGTH,len);
        }
        sb.setLength(0);
    });



    @Test
    public void stringBuilderSiginTest(){
        StringBuilder stringBuilder = new StringBuilder();
        //一共循环2亿次
        printTime(200,100000,(j)->{
           synchronized (stringBuilder){
               stringBuilder.setLength(0);
               stringBuilder.append("123123");
               stringBuilder.append("99999");
               stringBuilder.append("888888888888888888888888888888888888");
               String s = stringBuilder.toString();
               if(j % 100000 == 0){
                    System.out.println(s);
               }
           }
        });
    }

    /**
     * 使用pool的方式大约是单个StringBuilder同步的效率的一倍
     */
    @Test
    public void stringBuilderPoolTest(){
        //一共循环2亿次
        printTime(200,100000,(j)->{
            tokenStringBuilderPool.run(stringBuilder->{
                stringBuilder.setLength(0);
                stringBuilder.append("123123");
                stringBuilder.append("99999");
                stringBuilder.append("888888888888888888888888888888888888");
                String s = stringBuilder.toString();
                if(j % 100000 == 0){
                    System.out.println(s);
                }
                return null;
            });
        });
    }

}
