package com.tingfeng.util.java.base.concurrent;

import com.tingfeng.util.java.base.common.constant.EncryptionAlgorithmType;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * 加密工具类测试
 */
public class EncryptionUtilsTest {

    private static final String TEST_DATA = "Hello, World!";
    private static final byte[] TEST_KEY = "test-key-12345".getBytes(StandardCharsets.UTF_8);

    // ==================== MD5测试 ====================

    @Test
    public void testMd5Encrypt() {
        String result = EncryptionUtils.md5(TEST_DATA);
        Assert.assertNotNull("MD5结果不能为空", result);
        Assert.assertEquals("MD5结果长度应为32字符", 32, result.length());
        Assert.assertTrue("MD5结果应为十六进制", result.matches("[0-9a-f]+"));
    }

    @Test
    public void testMd5Consistency() {
        String result1 = EncryptionUtils.md5(TEST_DATA);
        String result2 = EncryptionUtils.md5(TEST_DATA);
        Assert.assertEquals("相同输入应产生相同MD5", result1, result2);
    }

    @Test
    public void testMd5DifferentInput() {
        String result1 = EncryptionUtils.md5("input1");
        String result2 = EncryptionUtils.md5("input2");
        Assert.assertNotEquals("不同输入应产生不同MD5", result1, result2);
    }

    // ==================== SHA256测试 ====================

    @Test
    public void testSha256Encrypt() {
        String result = EncryptionUtils.sha256(TEST_DATA);
        Assert.assertNotNull("SHA-256结果不能为空", result);
        Assert.assertEquals("SHA-256结果长度应为64字符", 64, result.length());
        Assert.assertTrue("SHA-256结果应为十六进制", result.matches("[0-9a-f]+"));
    }

    @Test
    public void testSha256Consistency() {
        String result1 = EncryptionUtils.sha256(TEST_DATA);
        String result2 = EncryptionUtils.sha256(TEST_DATA);
        Assert.assertEquals("相同输入应产生相同SHA-256", result1, result2);
    }

    // ==================== SHA512测试 ====================

    @Test
    public void testSha512Encrypt() {
        String result = EncryptionUtils.sha512(TEST_DATA);
        Assert.assertNotNull("SHA-512结果不能为空", result);
        Assert.assertEquals("SHA-512结果长度应为128字符", 128, result.length());
        Assert.assertTrue("SHA-512结果应为十六进制", result.matches("[0-9a-f]+"));
    }

    // ==================== 通用encrypt/decrypt测试 ====================

    @Test
    public void testEncryptToHex() {
        String result = EncryptionUtils.encryptToHex(TEST_DATA, EncryptionAlgorithmType.MD5, TEST_KEY);
        Assert.assertNotNull("加密结果不能为空", result);
        Assert.assertEquals("MD5十六进制结果长度应为32", 32, result.length());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testHashDecryptThrowsException() {
        // 哈希算法不支持解密，应抛出异常
        byte[] encrypted = EncryptionUtils.encrypt(TEST_DATA.getBytes(StandardCharsets.UTF_8),
                EncryptionAlgorithmType.MD5, TEST_KEY);
        EncryptionUtils.decrypt(encrypted, EncryptionAlgorithmType.MD5, TEST_KEY);
    }

    @Test
    public void testSha1Encrypt() {
        byte[] result = EncryptionUtils.encrypt(TEST_DATA.getBytes(StandardCharsets.UTF_8),
                EncryptionAlgorithmType.SHA1, TEST_KEY);
        Assert.assertNotNull("SHA1结果不能为空", result);
        Assert.assertEquals("SHA1结果长度应为20字节", 20, result.length);
    }

    @Test
    public void testSha256EncryptBytes() {
        byte[] result = EncryptionUtils.encrypt(TEST_DATA.getBytes(StandardCharsets.UTF_8),
                EncryptionAlgorithmType.SHA256, TEST_KEY);
        Assert.assertNotNull("SHA256结果不能为空", result);
        Assert.assertEquals("SHA256结果长度应为32字节", 32, result.length);
    }

    @Test
    public void testSha512EncryptBytes() {
        byte[] result = EncryptionUtils.encrypt(TEST_DATA.getBytes(StandardCharsets.UTF_8),
                EncryptionAlgorithmType.SHA512, TEST_KEY);
        Assert.assertNotNull("SHA512结果不能为空", result);
        Assert.assertEquals("SHA512结果长度应为64字节", 64, result.length);
    }

    // ==================== AES加解密测试 ====================

    @Test
    public void testAesEncryptDecrypt() {
        byte[] original = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = EncryptionUtils.encrypt(original, EncryptionAlgorithmType.AES, TEST_KEY);
        Assert.assertNotNull("AES加密结果不能为空", encrypted);
        Assert.assertFalse("加密后内容应不同", java.util.Arrays.equals(original, encrypted));

        byte[] decrypted = EncryptionUtils.decrypt(encrypted, EncryptionAlgorithmType.AES, TEST_KEY);
        Assert.assertArrayEquals("AES解密后应还原原内容", original, decrypted);
    }

    @Test
    public void testAesEncryptDecryptWithString() {
        byte[] encrypted = EncryptionUtils.encrypt(TEST_DATA, EncryptionAlgorithmType.AES, TEST_KEY);
        String decrypted = EncryptionUtils.decryptToString(encrypted, EncryptionAlgorithmType.AES, TEST_KEY);
        Assert.assertEquals("AES解密字符串应还原原内容", TEST_DATA, decrypted);
    }

    @Test
    public void testAesEncryptDecryptHex() {
        String encryptedHex = EncryptionUtils.encryptToHex(TEST_DATA, EncryptionAlgorithmType.AES, TEST_KEY);
        Assert.assertNotNull("AES加密十六进制结果不能为空", encryptedHex);

        String decrypted = EncryptionUtils.decryptFromHex(encryptedHex, EncryptionAlgorithmType.AES, TEST_KEY);
        Assert.assertEquals("AES解密后应还原原内容", TEST_DATA, decrypted);
    }

    @Test
    public void testAesConsistency() {
        byte[] encrypted1 = EncryptionUtils.encrypt(TEST_DATA.getBytes(StandardCharsets.UTF_8),
                EncryptionAlgorithmType.AES, TEST_KEY);
        byte[] encrypted2 = EncryptionUtils.encrypt(TEST_DATA.getBytes(StandardCharsets.UTF_8),
                EncryptionAlgorithmType.AES, TEST_KEY);
        Assert.assertArrayEquals("相同输入相同密钥应产生相同加密结果", encrypted1, encrypted2);
    }

    @Test
    public void testAesDifferentKey() {
        byte[] encrypted1 = EncryptionUtils.encrypt(TEST_DATA.getBytes(StandardCharsets.UTF_8),
                EncryptionAlgorithmType.AES, TEST_KEY);
        byte[] differentKey = "different-key!!".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted2 = EncryptionUtils.encrypt(TEST_DATA.getBytes(StandardCharsets.UTF_8),
                EncryptionAlgorithmType.AES, differentKey);
        Assert.assertFalse("不同密钥应产生不同加密结果",
                java.util.Arrays.equals(encrypted1, encrypted2));
    }

    @Test
    public void testAesNullKey() {
        // 测试null密钥（应使用默认密钥）
        byte[] original = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = EncryptionUtils.encrypt(original, EncryptionAlgorithmType.AES, null);
        byte[] decrypted = EncryptionUtils.decrypt(encrypted, EncryptionAlgorithmType.AES, null);
        Assert.assertArrayEquals("null密钥应能正常加解密", original, decrypted);
    }

    @Test
    public void testAesShortKey() {
        // 测试短密钥（应补足到16字节）
        byte[] shortKey = "1234".getBytes(StandardCharsets.UTF_8);
        byte[] original = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = EncryptionUtils.encrypt(original, EncryptionAlgorithmType.AES, shortKey);
        byte[] decrypted = EncryptionUtils.decrypt(encrypted, EncryptionAlgorithmType.AES, shortKey);
        Assert.assertArrayEquals("短密钥应能正常加解密", original, decrypted);
    }

    // ==================== 边界测试 ====================

    @Test
    public void testNullInputEncrypt() {
        byte[] result = EncryptionUtils.encrypt((byte[]) null, EncryptionAlgorithmType.MD5, TEST_KEY);
        Assert.assertNull("null输入应返回null", result);
    }

    @Test
    public void testNullStringEncrypt() {
        String result = EncryptionUtils.encryptToHex(null, EncryptionAlgorithmType.MD5, TEST_KEY);
        Assert.assertNull("null字符串输入应返回null", result);
    }

    @Test
    public void testEmptyStringEncrypt() {
        String result = EncryptionUtils.encryptToHex("", EncryptionAlgorithmType.MD5, TEST_KEY);
        Assert.assertNotNull("空字符串加密结果不应为空", result);
        Assert.assertEquals("空字符串MD5长度应为32", 32, result.length());
    }

    // ==================== AlgorithmType测试 ====================

    @Test
    public void testAlgorithmTypeFromValue() {
        Assert.assertEquals(EncryptionAlgorithmType.MD5,
                EncryptionAlgorithmType.fromValue("MD5"));
        Assert.assertEquals(EncryptionAlgorithmType.SHA256,
                EncryptionAlgorithmType.fromValue("SHA-256"));
        Assert.assertEquals(EncryptionAlgorithmType.AES,
                EncryptionAlgorithmType.fromValue("AES"));
    }

    @Test
    public void testAlgorithmTypeSupportsDecrypt() {
        Assert.assertFalse("MD5不支持解密", EncryptionAlgorithmType.MD5.supportsDecrypt());
        Assert.assertFalse("SHA1不支持解密", EncryptionAlgorithmType.SHA1.supportsDecrypt());
        Assert.assertFalse("SHA256不支持解密", EncryptionAlgorithmType.SHA256.supportsDecrypt());
        Assert.assertFalse("SHA512不支持解密", EncryptionAlgorithmType.SHA512.supportsDecrypt());
        Assert.assertTrue("AES支持解密", EncryptionAlgorithmType.AES.supportsDecrypt());
    }
}
