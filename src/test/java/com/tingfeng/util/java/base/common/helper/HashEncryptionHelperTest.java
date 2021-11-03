package com.tingfeng.util.java.base.common.helper;


import com.tingfeng.util.java.base.common.utils.RandomUtils;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Test;

/**
 * hash编码解码工具
 */
public class HashEncryptionHelperTest {

    @Test
    public void encodeDecode() {
        String salt = RandomUtils.randomString(RandomUtils.randomInt(1000));
        HashEncryptionHelper hashEncryption = new HashEncryptionHelper(salt);
        String str = RandomUtils.randomString(RandomUtils.randomInt(10000));
        String enStr = hashEncryption.encode(str);
        //System.out.println(str);
       //System.out.println(enStr);
        String deStr = hashEncryption.decode(enStr);
        if(!str.equals(deStr)){
            System.out.println("salt = " + salt);
            System.out.println("str = " + str);
            System.out.println("enStr = " + enStr);
            System.out.println("deStr = " + deStr);
            throw new RuntimeException();
        };
        //System.out.println(deStr);
    }

    @Test
    public void bathTestEncodeDecode(){
        TestUtils.printTime(4, 10000, index -> {
            encodeDecode();
        });
    }

    @Test
    public void encodeDecodeOne() {
        String salt = "hDo5UkWwHfrLXeRHiCfvAoMNYaqXw";
        HashEncryptionHelper hashEncryption = new HashEncryptionHelper(salt);
        String str = "HeYLBBB8AdvcGjN9ryZELHRXoIvPyDBPnYn8WUcahXPBYcr2wI35sfXhhpka6RJLrSdd7FuOKi0XxWOIPISoak9LXxjckUglgqoJtJ3oXS1U5gFibezPLofaq";
        String enStr = hashEncryption.encode(str);
        String deStr = hashEncryption.decode(enStr);
        System.out.println("salt = " + salt);
        System.out.println("str = " + str);
        System.out.println("enStr = " + enStr);
        System.out.println("deStr = " + deStr);
    }

    @Test
    public void bathTestEncodeDecodeWithPositionOffset(){
        TestUtils.printTime(4, 10000, index -> {
            encodeDecode();
            encodeDecodeWithPositionOffset();
        });
    }

    @Test
    public void encodeDecodeWithPositionOffset(){
        String salt = RandomUtils.randomString(15);
        HashEncryptionHelper hashEncryption = new HashEncryptionHelper(salt);
        hashEncryption.hashPositionDictionary(RandomUtils.randomInt(1,1000));
        String str = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        String enStr = hashEncryption.encode(str,true);
        String deStr = hashEncryption.decode(enStr,true);
        System.out.println("salt = " + salt);
        System.out.println("str = " + str);
        System.out.println("enStr = " + enStr);
        System.out.println("deStr = " + deStr);
    }
}