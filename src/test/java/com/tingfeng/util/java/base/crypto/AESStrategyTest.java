package com.tingfeng.util.java.base.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * AES-GCM 加密策略单元测试。
 * <p>
 * 覆盖：正常加解密、各密钥长度、非法密钥、空数据、null 输入、随机 IV 验证、篡改密文检测。
 * </p>
 */
public class AESStrategyTest {

    private static final byte[] KEY_128 = "1234567890abcdef".getBytes(StandardCharsets.UTF_8);   // 16 字节
    private static final byte[] KEY_192 = "1234567890abcdef12345678".getBytes(StandardCharsets.UTF_8); // 24 字节
    private static final byte[] KEY_256 = "1234567890abcdef1234567890abcdef".getBytes(StandardCharsets.UTF_8); // 32 字节

    private static final String PLAINTEXT = "Hello, AES-GCM!";
    private static final String PLAINTEXT_CHINESE = "中文加密测试内容";

    private final AESStrategy strategy = new AESStrategy();

    // ==================== 正常加解密路径 ====================

    /**
     * 测试使用 128 位密钥进行加解密往返
     */
    @Test
    public void testEncryptDecryptWith128BitKey() {
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, KEY_128);
        Assert.assertNotNull("密文不能为空", ciphertext);
        Assert.assertTrue("密文长度必须大于 IV 长度（12 字节）", ciphertext.length > AESStrategyTestHelper.IV_LENGTH);

        byte[] decrypted = strategy.decrypt(ciphertext, KEY_128);
        Assert.assertArrayEquals("解密结果应与原始明文一致", plaintext, decrypted);
    }

    /**
     * 测试使用 192 位密钥进行加解密往返
     */
    @Test
    public void testEncryptDecryptWith192BitKey() {
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, KEY_192);
        Assert.assertNotNull("密文不能为空", ciphertext);

        byte[] decrypted = strategy.decrypt(ciphertext, KEY_192);
        Assert.assertArrayEquals("解密结果应与原始明文一致", plaintext, decrypted);
    }

    /**
     * 测试使用 256 位密钥进行加解密往返
     */
    @Test
    public void testEncryptDecryptWith256BitKey() {
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, KEY_256);
        Assert.assertNotNull("密文不能为空", ciphertext);

        byte[] decrypted = strategy.decrypt(ciphertext, KEY_256);
        Assert.assertArrayEquals("解密结果应与原始明文一致", plaintext, decrypted);
    }

    /**
     * 测试中文字符串加解密往返
     */
    @Test
    public void testEncryptDecryptChinese() {
        byte[] plaintext = PLAINTEXT_CHINESE.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, KEY_128);
        Assert.assertNotNull("密文不能为空", ciphertext);

        byte[] decrypted = strategy.decrypt(ciphertext, KEY_128);
        Assert.assertArrayEquals("中文解密结果应与原始明文一致", plaintext, decrypted);
    }

    // ==================== null / 空数据边界 ====================

    /**
     * 测试 null 数据加密应返回 null
     */
    @Test
    public void testNullDataReturnsNull() {
        byte[] result = strategy.encrypt(null, KEY_128);
        Assert.assertNull("null 数据加密应返回 null", result);

        result = strategy.decrypt(null, KEY_128);
        Assert.assertNull("null 数据解密应返回 null", result);
    }

    /**
     * 测试空字节数组加解密
     */
    @Test
    public void testEmptyDataEncryptDecrypt() {
        byte[] plaintext = new byte[0];
        byte[] ciphertext = strategy.encrypt(plaintext, KEY_128);
        Assert.assertNotNull("空数据加密结果不能为 null", ciphertext);
        // GCM 空加密输出为 IV(12B) + GCM tag(16B)
        Assert.assertEquals("空数据密文长度应为 28 字节（12 IV + 16 GCM tag）",
            AESStrategyTestHelper.IV_LENGTH + AESStrategyTestHelper.GCM_TAG_LENGTH,
            ciphertext.length);

        byte[] decrypted = strategy.decrypt(ciphertext, KEY_128);
        Assert.assertNotNull("空数据解密结果不能为 null", decrypted);
        Assert.assertEquals("空数据解密结果长度应为 0", 0, decrypted.length);
    }

    // ==================== 密钥兼容测试 ====================

    /**
     * 测试 null 密钥：应通过 {@link AESStrategy#DEFAULT_KEY} 正常工作
     */
    @Test
    public void testNullKeyUsesDefaultKey() {
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, null);
        Assert.assertNotNull("null 密钥加密结果不能为 null", ciphertext);

        byte[] decrypted = strategy.decrypt(ciphertext, null);
        Assert.assertArrayEquals("null 密钥解密结果应与原始明文一致", plaintext, decrypted);
    }

    /**
     * 测试过短密钥（1 字节）通过 SHA-256 派生到 16 字节后正常加解密
     */
    @Test
    public void testShortKeyDerivesTo128Bit() {
        byte[] invalidKey = {0x01};
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, invalidKey);
        Assert.assertNotNull("派生密钥加密结果不能为 null", ciphertext);

        byte[] decrypted = strategy.decrypt(ciphertext, invalidKey);
        Assert.assertArrayEquals("派生密钥解密结果应与原始明文一致", plaintext, decrypted);
    }

    /**
     * 测试 15 字节密钥派生到 16 字节后正常加解密
     */
    @Test
    public void test15ByteKeyDerivesTo128Bit() {
        byte[] invalidKey = "1234567890abcde".getBytes(StandardCharsets.UTF_8);
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, invalidKey);
        Assert.assertNotNull("派生密钥加密结果不能为 null", ciphertext);

        byte[] decrypted = strategy.decrypt(ciphertext, invalidKey);
        Assert.assertArrayEquals("派生密钥解密结果应与原始明文一致", plaintext, decrypted);
    }

    /**
     * 测试 17 字节密钥派生到 24 字节后正常加解密
     */
    @Test
    public void test17ByteKeyDerivesTo192Bit() {
        byte[] invalidKey = "1234567890abcdefg".getBytes(StandardCharsets.UTF_8);
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, invalidKey);
        Assert.assertNotNull("派生密钥加密结果不能为 null", ciphertext);

        byte[] decrypted = strategy.decrypt(ciphertext, invalidKey);
        Assert.assertArrayEquals("派生密钥解密结果应与原始明文一致", plaintext, decrypted);
    }

    /**
     * 测试 31 字节密钥派生到 32 字节后正常加解密
     */
    @Test
    public void test31ByteKeyDerivesTo256Bit() {
        byte[] invalidKey = "1234567890abcdef1234567890abcde".getBytes(StandardCharsets.UTF_8);
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, invalidKey);
        Assert.assertNotNull("派生密钥加密结果不能为 null", ciphertext);

        byte[] decrypted = strategy.decrypt(ciphertext, invalidKey);
        Assert.assertArrayEquals("派生密钥解密结果应与原始明文一致", plaintext, decrypted);
    }

    /**
     * 测试 33 字节密钥派生到 32 字节后正常加解密
     */
    @Test
    public void test33ByteKeyDerivesTo256Bit() {
        byte[] invalidKey = "1234567890abcdef1234567890abcdef!".getBytes(StandardCharsets.UTF_8);
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, invalidKey);
        Assert.assertNotNull("派生密钥加密结果不能为 null", ciphertext);

        byte[] decrypted = strategy.decrypt(ciphertext, invalidKey);
        Assert.assertArrayEquals("派生密钥解密结果应与原始明文一致", plaintext, decrypted);
    }

    // ==================== 随机 IV 验证 ====================

    /**
     * 测试相同明文和密钥每次加密产生不同密文（随机 IV）
     */
    @Test
    public void testRandomIVProducesDifferentCiphertext() {
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);

        byte[] ciphertext1 = strategy.encrypt(plaintext, KEY_128);
        byte[] ciphertext2 = strategy.encrypt(plaintext, KEY_128);

        Assert.assertNotNull("第一次密文不能为 null", ciphertext1);
        Assert.assertNotNull("第二次密文不能为 null", ciphertext2);
        // IV 不同导致密文不同
        Assert.assertFalse("相同明文密钥应产生不同密文（随机 IV）",
            java.util.Arrays.equals(ciphertext1, ciphertext2));

        // 但解密结果都应正确
        byte[] decrypted1 = strategy.decrypt(ciphertext1, KEY_128);
        byte[] decrypted2 = strategy.decrypt(ciphertext2, KEY_128);
        Assert.assertArrayEquals("两次解密结果应一致", plaintext, decrypted1);
        Assert.assertArrayEquals("两次解密结果应一致", plaintext, decrypted2);
    }

    // ==================== 篡改密文验证 ====================

    /**
     * 测试篡改密文后解密应抛出 RuntimeException（GCM 认证失败）
     */
    @Test(expected = RuntimeException.class)
    public void testTamperedCiphertextThrowsException() {
        byte[] plaintext = PLAINTEXT.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = strategy.encrypt(plaintext, KEY_128);

        // 篡改密文部分（跳过 IV）
        ciphertext[AESStrategyTestHelper.IV_LENGTH] ^= 0x01;

        strategy.decrypt(ciphertext, KEY_128);
    }

    /**
     * 测试截断密文（过短）应抛出 RuntimeException
     */
    @Test(expected = RuntimeException.class)
    public void testTruncatedCiphertextThrowsException() {
        byte[] truncated = new byte[AESStrategyTestHelper.IV_LENGTH - 1];
        strategy.decrypt(truncated, KEY_128);
    }

    // ==================== 接口方法 ====================

    /**
     * 测试 getType 返回正确算法类型
     */
    @Test
    public void testGetType() {
        Assert.assertEquals("算法类型应为 AES",
            com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType.AES,
            strategy.getType());
    }

    /**
     * 测试 supportsDecrypt 返回 true
     */
    @Test
    public void testSupportsDecrypt() {
        Assert.assertTrue("AES 应支持解密", strategy.supportsDecrypt());
    }
}

/**
 * AESStrategy 测试辅助常量（包级可见）。
 */
class AESStrategyTestHelper {
    static final int IV_LENGTH = 12;
    static final int GCM_TAG_LENGTH = 16;
}
