package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.crypto.HashEncryptionHelper;
import com.tingfeng.util.java.base.math.RandomUtils;
import com.tingfeng.util.java.base.common.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * HashEncryptionHelper 类的单元测试
 * 测试哈希加密工具的编码和解码功能，包括基本编码解码和带位置偏移的编码解码
 *
 * @author huitoukest
 */
public class HashEncryptionHelperTest {

    /**
     * 测试基本的编码和解码功能
     * 生成随机盐值和随机字符串，验证编码后再解码是否与原字符串相同
     */
    @Test
    public void encodeDecode() {
        String salt = RandomUtils.randomString(RandomUtils.randomInt(1000));
        HashEncryptionHelper hashEncryption = new HashEncryptionHelper(salt);
        String str = RandomUtils.randomString(RandomUtils.randomInt(10000));
        String enStr = hashEncryption.encode(str);
        String deStr = hashEncryption.decode(enStr);
        Assert.assertEquals("编码解码后应该与原字符串相同", str, deStr);
    }

    /**
     * 批量测试基本的编码和解码功能
     * 多线程执行多次测试，验证编码解码的性能和稳定性
     */
    @Test
    public void bathTestEncodeDecode(){
        TestUtils.printTime(4, 10000, index -> {
            encodeDecode();
        });
    }

    /**
     * 测试固定盐值和固定字符串的编码和解码功能
     * 使用预设的盐值和字符串，验证编码后再解码是否与原字符串相同
     */
    @Test
    public void encodeDecodeOne() {
        String salt = "hDo5UkWwHfrLXeRHiCfvAoMNYaqXw";
        HashEncryptionHelper hashEncryption = new HashEncryptionHelper(salt);
        String str = "HeYLBBB8AdvcGjN9ryZELHRXoIvPyDBPnYn8WUcahXPBYcr2wI35sfXhhpka6RJLrSdd7FuOKi0XxWOIPISoak9LXxjckUglgqoJtJ3oXS1U5gFibezPLofaq";
        String enStr = hashEncryption.encode(str);
        String deStr = hashEncryption.decode(enStr);
        Assert.assertEquals("编码解码后应该与原字符串相同", str, deStr);
        System.out.println("salt = " + salt);
        System.out.println("str = " + str);
        System.out.println("enStr = " + enStr);
        System.out.println("deStr = " + deStr);
    }

    /**
     * 批量测试基本编码解码和带位置偏移的编码解码功能
     * 多线程执行多次测试，验证两种编码解码方式的性能和稳定性
     */
    @Test
    public void bathTestEncodeDecodeWithPositionOffset(){
        TestUtils.printTime(4, 10000, index -> {
            encodeDecode();
            encodeDecodeWithPositionOffset();
        });
    }

    /**
     * 测试带位置偏移的编码和解码功能
     * 生成随机盐值，设置随机位置偏移，验证编码后再解码是否与原字符串相同
     */
    @Test
    public void encodeDecodeWithPositionOffset(){
        String salt = RandomUtils.randomString(15);
        HashEncryptionHelper hashEncryption = new HashEncryptionHelper(salt);
        hashEncryption.hashPositionDictionary(RandomUtils.randomInt(1,1000));
        String str = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        String enStr = hashEncryption.encode(str,true);
        String deStr = hashEncryption.decode(enStr,true);
        Assert.assertEquals("带位置偏移的编码解码后应该与原字符串相同", str, deStr);
        System.out.println("salt = " + salt);
        System.out.println("str = " + str);
        System.out.println("enStr = " + enStr);
        System.out.println("deStr = " + deStr);
    }
}