package com.tingfeng.util.java.base.crypto;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * HashStrategy 类的单元测试。
 * 测试字符替换编码策略的加密解密功能，包括基本往返、
 * 接口兼容性、边界输入验证等场景。
 *
 * @author huitoukest
 */
public class HashStrategyTest {

    /**
     * 测试基本的加密和解密往返正确性。
     * 使用有效字符（0-9, A-Z, a-z, _, -）验证加密后再解密是否能还原。
     */
    @Test
    public void encryptDecryptRoundTrip() {
        HashStrategy strategy = new HashStrategy("testSalt");
        String input = "HelloWorld123_test-value";
        byte[] encrypted = strategy.encrypt(input.getBytes(StandardCharsets.UTF_8), null);
        byte[] decrypted = strategy.decrypt(encrypted, null);
        Assert.assertEquals("加密解密往返应与原字符串相同", input,
            new String(decrypted, StandardCharsets.UTF_8));
    }

    /**
     * 测试通过 EncryptionStrategy 接口调用加密解密。
     * 验证 HashStrategy 作为 EncryptionStrategy 实现的多态性。
     */
    @Test
    public void encryptDecryptViaInterface() {
        EncryptionStrategy strategy = new HashStrategy("interfaceSalt");
        String input = "testValue_123_ABC";
        byte[] encrypted = strategy.encrypt(input.getBytes(StandardCharsets.UTF_8), null);
        byte[] decrypted = strategy.decrypt(encrypted, null);
        Assert.assertEquals("通过接口调用加密解密往返应与原字符串相同", input,
            new String(decrypted, StandardCharsets.UTF_8));
    }

    /**
     * 测试 null 输入在加密时返回 null。
     */
    @Test
    public void encryptNullInput() {
        HashStrategy strategy = new HashStrategy("testSalt");
        Assert.assertNull("null 输入加密应返回 null", strategy.encrypt(null, null));
    }

    /**
     * 测试 null 输入在解密时返回 null。
     */
    @Test
    public void decryptNullInput() {
        HashStrategy strategy = new HashStrategy("testSalt");
        Assert.assertNull("null 输入解密应返回 null", strategy.decrypt(null, null));
    }

    /**
     * 测试空字节数组加密返回空字符串。
     */
    @Test
    public void encryptEmptyInput() {
        HashStrategy strategy = new HashStrategy("testSalt");
        byte[] result = strategy.encrypt(new byte[0], null);
        Assert.assertEquals("空输入加密应返回空字符串", "",
            new String(result, StandardCharsets.UTF_8));
    }

    /**
     * 测试空字节数组解密返回空字符串。
     */
    @Test
    public void decryptEmptyInput() {
        HashStrategy strategy = new HashStrategy("testSalt");
        byte[] result = strategy.decrypt(new byte[0], null);
        Assert.assertEquals("空输入解密应返回空字符串", "",
            new String(result, StandardCharsets.UTF_8));
    }

    /**
     * 测试严格模式下包含非法字符时加密抛出 IllegalArgumentException。
     * 空格字符不在支持的 64 个基础字符集中。
     */
    @Test(expected = IllegalArgumentException.class)
    public void encryptIllegalCharsStrictMode() {
        HashStrategy strategy = new HashStrategy("testSalt", HashStrategy.Mode.STRICT);
        strategy.encrypt("hello world".getBytes(StandardCharsets.UTF_8), null);
    }

    /**
     * 测试宽松模式下非法字符保持原样不变，且加密解密往返正确。
     */
    @Test
    public void encryptIllegalCharsLooseMode() {
        HashStrategy strategy = new HashStrategy("testSalt", HashStrategy.Mode.LOOSE);
        String input = "hello world!@# test-value_123";
        byte[] encrypted = strategy.encrypt(input.getBytes(StandardCharsets.UTF_8), null);
        byte[] decrypted = strategy.decrypt(encrypted, null);
        Assert.assertEquals("宽松模式非法字符应保持原样", input,
            new String(decrypted, StandardCharsets.UTF_8));
    }

    /**
     * 测试不同的盐值产生不同的加密结果（同一输入）。
     */
    @Test
    public void differentSaltDifferentResult() {
        HashStrategy s1 = new HashStrategy("salt1");
        HashStrategy s2 = new HashStrategy("salt2");
        String input = "testString123_ABC";
        byte[] r1 = s1.encrypt(input.getBytes(StandardCharsets.UTF_8), null);
        byte[] r2 = s2.encrypt(input.getBytes(StandardCharsets.UTF_8), null);
        String result1 = new String(r1, StandardCharsets.UTF_8);
        String result2 = new String(r2, StandardCharsets.UTF_8);
        Assert.assertNotEquals("不同盐值应产生不同的加密结果", result1, result2);
        Assert.assertEquals("salt1 加密后解密应还原", input,
            new String(s1.decrypt(r1, null), StandardCharsets.UTF_8));
        Assert.assertEquals("salt2 加密后解密应还原", input,
            new String(s2.decrypt(r2, null), StandardCharsets.UTF_8));
    }

    /**
     * 测试 supportsDecrypt 返回 true。
     */
    @Test
    public void supportsDecrypt() {
        HashStrategy strategy = new HashStrategy("testSalt");
        Assert.assertTrue("字符替换编码应支持解密", strategy.supportsDecrypt());
    }

    /**
     * 测试 getType 返回正确的枚举值。
     */
    @Test
    public void getType() {
        HashStrategy strategy = new HashStrategy("testSalt");
        Assert.assertEquals("策略类型应为 SUBSTITUTION",
            EncryptionAlgorithmType.SUBSTITUTION, strategy.getType());
    }

    /**
     * 测试所有 64 个基础字符的加密解密往返。
     * 确保每个基础字符都能正确编码和解码。
     */
    @Test
    public void encryptDecryptAllBaseChars() {
        HashStrategy strategy = new HashStrategy("testSaltForAllChars");
        String baseChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-";
        String input = baseChars + baseChars + baseChars;
        byte[] encrypted = strategy.encrypt(input.getBytes(StandardCharsets.UTF_8), null);
        byte[] decrypted = strategy.decrypt(encrypted, null);
        Assert.assertEquals("全部基础字符加密解密往返应与原字符串相同", input,
            new String(decrypted, StandardCharsets.UTF_8));
    }

    /**
     * 测试带位置偏移编码的加密解密往返。
     */
    @Test
    public void encryptDecryptWithPositionOffset() {
        HashStrategy strategy = new HashStrategy("positionSalt");
        strategy.hashPositionDictionary(50);
        Assert.assertTrue("应已启用位置偏移编码", strategy.isWithPositionEncode());
        String input = "test_value_123_ABC_00000000000000000000";
        byte[] encrypted = strategy.encrypt(input.getBytes(StandardCharsets.UTF_8), null);
        byte[] decrypted = strategy.decrypt(encrypted, null);
        Assert.assertEquals("带位置偏移编码的加密解密往返应与原字符串相同", input,
            new String(decrypted, StandardCharsets.UTF_8));
    }

}
