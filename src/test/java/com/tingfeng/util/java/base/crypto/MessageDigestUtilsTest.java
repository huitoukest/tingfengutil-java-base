package com.tingfeng.util.java.base.crypto;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 消息摘要工具类测试
 */
public class MessageDigestUtilsTest {

    /**
     * 测试MD5加密
     */
    @Test
    public void testMd5() {
        String content = "i m a sample";
        byte[] result = MessageDigestUtils.md5(content.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull("MD5结果不能为空", result);
        Assert.assertEquals("MD5结果长度应该为16字节", 16, result.length);
    }

    /**
     * 测试MD5加密 - 空字符串
     */
    @Test
    public void testMd5Empty() {
        String content = "";
        byte[] result = MessageDigestUtils.md5(content.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull("MD5结果不能为空", result);
        Assert.assertEquals("MD5结果长度应该为16字节", 16, result.length);
    }

    /**
     * 测试MD5加密 - 中文内容
     */
    @Test
    public void testMd5Chinese() {
        String content = "中文测试内容";
        byte[] result = MessageDigestUtils.md5(content.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull("MD5结果不能为空", result);
        Assert.assertEquals("MD5结果长度应该为16字节", 16, result.length);
    }

    /**
     * 测试SHA-256加密
     */
    @Test
    public void testSha256() {
        String content = "test content";
        byte[] result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA256, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull("SHA-256结果不能为空", result);
        Assert.assertEquals("SHA-256结果长度应该为32字节", 32, result.length);
    }

    /**
     * 测试SHA-512加密
     */
    @Test
    public void testSha512() {
        String content = "test content";
        byte[] result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA512, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull("SHA-512结果不能为空", result);
        Assert.assertEquals("SHA-512结果长度应该为64字节", 64, result.length);
    }

    /**
     * 测试SHA-1加密
     */
    @Test
    public void testSha1() {
        String content = "test content";
        byte[] result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA1, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull("SHA-1结果不能为空", result);
        Assert.assertEquals("SHA-1结果长度应该为20字节", 20, result.length);
    }

    /**
     * 测试带salt的hash
     */
    @Test
    public void testHashWithSalt() {
        String content = "test content";
        String salt = "salt";
        byte[] result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA256, content.getBytes(StandardCharsets.UTF_8), salt);
        Assert.assertNotNull("带salt的hash结果不能为空", result);
        Assert.assertEquals("SHA-256结果长度应该为32字节", 32, result.length);
    }

    /**
     * 测试多次迭代hash
     */
    @Test
    public void testHashWithIterations() {
        String content = "test content";
        byte[] salt = "salt".getBytes(StandardCharsets.UTF_8);
        int iterations = 1000;

        byte[] result = MessageDigestUtils.hash("SHA-256", content.getBytes(StandardCharsets.UTF_8), salt, iterations);
        Assert.assertNotNull("多次迭代hash结果不能为空", result);
        Assert.assertEquals("SHA-256结果长度应该为32字节", 32, result.length);
    }

    /**
     * 测试MAC SHA-256
     */
    @Test
    public void testMacSha256() {
        String content = "test content";
        byte[] result = MessageDigestUtils.macSha(MessageDigestUtils.DigestType.SHAMAC256, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull("MAC SHA-256结果不能为空", result);
        Assert.assertEquals("MAC SHA-256结果长度应该为32字节", 32, result.length);
    }

    /**
     * 测试MAC SHA-512
     */
    @Test
    public void testMacSha512() {
        String content = "test content";
        byte[] result = MessageDigestUtils.macSha(MessageDigestUtils.DigestType.SHAMAC512, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull("MAC SHA-512结果不能为空", result);
        Assert.assertEquals("MAC SHA-512结果长度应该为64字节", 64, result.length);
    }

    /**
     * 测试带密钥的MAC
     */
    @Test
    public void testMacShaWithKey() {
        String content = "test content";
        byte[] secretKey = "secret".getBytes(StandardCharsets.UTF_8);
        byte[] result = MessageDigestUtils.macSha(MessageDigestUtils.DigestType.SHAMAC256, content.getBytes(StandardCharsets.UTF_8), secretKey);
        Assert.assertNotNull("带密钥的MAC结果不能为空", result);
        Assert.assertEquals("MAC SHA-256结果长度应该为32字节", 32, result.length);
    }

    /**
     * 测试转换为十六进制字符串
     */
    @Test
    public void testToHexString() {
        byte[] bytes = {0x01, 0x02, 0x03, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
        String result = MessageDigestUtils.toHexString(bytes);
        Assert.assertNotNull("十六进制字符串不能为空", result);
        Assert.assertEquals("十六进制字符串长度应该为18", 18, result.length());
        Assert.assertEquals("十六进制字符串应该正确", "0102030a0b0c0d0e0f", result);
    }

    /**
     * 测试转换为hash十六进制字符串
     */
    @Test
    public void testToHashHexString() {
        String algorithm = "MD5";
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);
        byte[] salt = "salt".getBytes(StandardCharsets.UTF_8);
        int iterations = 1;

        String result = MessageDigestUtils.toHashHexString(algorithm, content, salt, iterations);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("MD5 hash十六进制字符串长度应该为32", 32, result.length());
        Assert.assertTrue("结果应该只包含十六进制字符", result.matches("[0-9a-f]+"));
    }

    /**
     * 测试转换为hash十六进制字符串 - 字符串版本
     */
    @Test
    public void testToHashHexStringStringVersion() {
        String algorithm = "MD5";
        String content = "test content";
        String salt = "salt";

        String result = MessageDigestUtils.toHashHexString(algorithm, content, salt);
        Assert.assertNotNull("结果不能为空", result);
        Assert.assertEquals("MD5 hash十六进制字符串长度应该为32", 32, result.length());
        Assert.assertTrue("结果应该只包含十六进制字符", result.matches("[0-9a-f]+"));
    }

    /**
     * 测试digest方法 - 输入流
     */
    @Test
    public void testDigestInputStream() {
        String content = "test content for input stream";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        byte[] result = MessageDigestUtils.digest("MD5", inputStream);
        Assert.assertNotNull("digest结果不能为空", result);
        Assert.assertEquals("MD5 digest结果长度应该为16字节", 16, result.length);
    }

    /**
     * 测试md5方法 - 输入流
     */
    @Test
    public void testMd5InputStream() {
        String content = "test content for input stream";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        byte[] result = MessageDigestUtils.md5(inputStream);
        Assert.assertNotNull("MD5结果不能为空", result);
        Assert.assertEquals("MD5结果长度应该为16字节", 16, result.length);
    }

    /**
     * 测试digest方法 - 自定义填充函数
     */
    @Test
    public void testDigestWithFiller() {
        String algorithm = "MD5";
        byte[] content = "test content".getBytes(StandardCharsets.UTF_8);

        byte[] result = MessageDigestUtils.digest(algorithm, digest -> {
            digest.update(content);
        });

        Assert.assertNotNull("digest结果不能为空", result);
        Assert.assertEquals("MD5 digest结果长度应该为16字节", 16, result.length);
    }

    /**
     * 测试相同内容产生相同hash
     */
    @Test
    public void testSameContentSameHash() {
        String content = "test content";
        byte[] result1 = MessageDigestUtils.md5(content.getBytes(StandardCharsets.UTF_8));
        byte[] result2 = MessageDigestUtils.md5(content.getBytes(StandardCharsets.UTF_8));

        Assert.assertArrayEquals("相同内容应该产生相同的hash", result1, result2);
    }

    /**
     * 测试不同内容产生不同hash
     */
    @Test
    public void testDifferentContentDifferentHash() {
        String content1 = "test content 1";
        String content2 = "test content 2";
        byte[] result1 = MessageDigestUtils.md5(content1.getBytes(StandardCharsets.UTF_8));
        byte[] result2 = MessageDigestUtils.md5(content2.getBytes(StandardCharsets.UTF_8));

        Assert.assertFalse("不同内容应该产生不同的hash", java.util.Arrays.equals(result1, result2));
    }

    /**
     * 测试空内容的hash
     */
    @Test
    public void testEmptyContentHash() {
        byte[] content = new byte[0];
        byte[] result = MessageDigestUtils.md5(content);
        Assert.assertNotNull("空内容的hash不能为空", result);
        Assert.assertEquals("MD5结果长度应该为16字节", 16, result.length);
    }

    /**
     * 测试大文件hash
     */
    @Test
    public void testLargeContentHash() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("test content line ").append(i).append("\n");
        }
        byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = MessageDigestUtils.md5(content);
        Assert.assertNotNull("大文件hash不能为空", result);
        Assert.assertEquals("MD5结果长度应该为16字节", 16, result.length);
    }

    /**
     * 测试DigestType枚举
     */
    @Test
    public void testDigestTypeEnum() {
        Assert.assertEquals("MD5", MessageDigestUtils.DigestType.MD5.getValue());
        Assert.assertEquals("SHA-1", MessageDigestUtils.DigestType.SHA1.getValue());
        Assert.assertEquals("SHA-256", MessageDigestUtils.DigestType.SHA256.getValue());
        Assert.assertEquals("SHA-512", MessageDigestUtils.DigestType.SHA512.getValue());
        Assert.assertEquals("HmacSHA256", MessageDigestUtils.DigestType.SHAMAC256.getValue());
        Assert.assertEquals("HmacSHA512", MessageDigestUtils.DigestType.SHAMAC512.getValue());
    }

    /**
     * 测试性能 - MD5
     */
    @Test
    public void testMd5Performance() {
        String content = "test content for performance";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            MessageDigestUtils.md5(bytes);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        Assert.assertTrue("10000次MD5加密应该在合理时间内完成", duration < 1000);
    }

    /**
     * 测试性能 - SHA-256
     */
    @Test
    public void testSha256Performance() {
        String content = "test content for performance";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA256, bytes);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        Assert.assertTrue("10000次SHA-256加密应该在合理时间内完成", duration < 2000);
    }

    /**
     * 测试十六进制转换 - 边界值
     */
    @Test
    public void testToHexBoundary() {
        byte[] bytes1 = {(byte) 0x00};
        String result1 = MessageDigestUtils.toHexString(bytes1);
        Assert.assertEquals("0x00应该转换为00", "00", result1);

        byte[] bytes2 = {(byte) 0xFF};
        String result2 = MessageDigestUtils.toHexString(bytes2);
        Assert.assertEquals("0xFF应该转换为ff", "ff", result2);
    }

    /**
     * 测试hash一致性 - 多次调用
     */
    @Test
    public void testHashConsistency() {
        String content = "test content";
        String salt = "salt";

        String result1 = MessageDigestUtils.toHashHexString("SHA-256", content, salt);
        String result2 = MessageDigestUtils.toHashHexString("SHA-256", content, salt);
        String result3 = MessageDigestUtils.toHashHexString("SHA-256", content, salt);

        Assert.assertEquals("多次调用应该产生相同的结果", result1, result2);
        Assert.assertEquals("多次调用应该产生相同的结果", result2, result3);
    }

    /**
     * 测试不同算法的hash长度
     */
    @Test
    public void testDifferentAlgorithmLengths() {
        String content = "test content";

        byte[] md5Result = MessageDigestUtils.md5(content.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("MD5长度应该是16字节", 16, md5Result.length);

        byte[] sha1Result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA1, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("SHA-1长度应该是20字节", 20, sha1Result.length);

        byte[] sha256Result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA256, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("SHA-256长度应该是32字节", 32, sha256Result.length);

        byte[] sha512Result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA512, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("SHA-512长度应该是64字节", 64, sha512Result.length);
    }

    /**
     * 测试macSha方法 - 自动生成密钥
     */
    @Test
    public void testMacShaAutoKey() {
        String content = "test content";
        byte[] result1 = MessageDigestUtils.macSha(MessageDigestUtils.DigestType.SHAMAC256, content.getBytes(StandardCharsets.UTF_8));
        byte[] result2 = MessageDigestUtils.macSha(MessageDigestUtils.DigestType.SHAMAC256, content.getBytes(StandardCharsets.UTF_8));
        Assert.assertNotNull("MAC结果不能为空", result1);
        Assert.assertEquals("MAC SHA-256结果长度应该为32字节", 32, result1.length);
        // 自动生成密钥每次结果不同
        Assert.assertFalse("自动生成密钥的MAC结果应该不同", java.util.Arrays.equals(result1, result2));
    }

    /**
     * 测试macSha方法 - 指定密钥
     */
    @Test
    public void testMacShaFixedKey() {
        String content = "test content";
        byte[] secretKey = "fixed-secret-key".getBytes(StandardCharsets.UTF_8);
        byte[] result1 = MessageDigestUtils.macSha(MessageDigestUtils.DigestType.SHAMAC256, content.getBytes(StandardCharsets.UTF_8), secretKey);
        byte[] result2 = MessageDigestUtils.macSha(MessageDigestUtils.DigestType.SHAMAC256, content.getBytes(StandardCharsets.UTF_8), secretKey);
        Assert.assertNotNull("MAC结果不能为空", result1);
        Assert.assertEquals("MAC SHA-256结果长度应该为32字节", 32, result1.length);
        // 指定相同密钥结果应该相同
        Assert.assertArrayEquals("相同密钥的MAC结果应该相同", result1, result2);
    }

    /**
     * 测试sha方法 - 字符串盐值
     */
    @Test
    public void testShaStringSalt() {
        String content = "test content";
        String salt = "salt-value";
        byte[] result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA256, content, salt);
        Assert.assertNotNull("带盐值的SHA结果不能为空", result);
        Assert.assertEquals("SHA-256结果长度应该为32字节", 32, result.length);
    }

    /**
     * 测试并发环境下池初始化的线程安全
     * 多个线程同时调用同一算法，验证池被正确初始化且结果正确
     */
    @Test
    public void testConcurrentPoolInitialization() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> exception = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    // 所有线程同时调用同一算法，触发并发池初始化
                    byte[] result = MessageDigestUtils.md5("concurrent test".getBytes(StandardCharsets.UTF_8));
                    Assert.assertNotNull("并发访问时MD5结果不能为空", result);
                    Assert.assertEquals("并发访问时MD5结果长度应为16字节", 16, result.length);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        Assert.assertNull("并发池初始化不应发生异常: " +
                (exception.get() != null ? exception.get().getMessage() : ""), exception.get());
    }

    /**
     * 测试并发环境下多种算法的线程安全
     * 多个线程同时调用不同算法，验证各池互不影响
     */
    @Test
    public void testConcurrentDifferentAlgorithms() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * 4);
        AtomicReference<Throwable> exception = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            final String content = "test content " + i;
            executor.submit(() -> {
                try {
                    byte[] md5Result = MessageDigestUtils.md5(content.getBytes(StandardCharsets.UTF_8));
                    Assert.assertEquals("MD5长度应为16字节", 16, md5Result.length);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
            executor.submit(() -> {
                try {
                    byte[] sha256Result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA256,
                            content.getBytes(StandardCharsets.UTF_8));
                    Assert.assertEquals("SHA-256长度应为32字节", 32, sha256Result.length);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
            executor.submit(() -> {
                try {
                    byte[] sha512Result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA512,
                            content.getBytes(StandardCharsets.UTF_8));
                    Assert.assertEquals("SHA-512长度应为64字节", 64, sha512Result.length);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
            executor.submit(() -> {
                try {
                    byte[] macResult = MessageDigestUtils.macSha(MessageDigestUtils.DigestType.SHAMAC256,
                            content.getBytes(StandardCharsets.UTF_8));
                    Assert.assertEquals("HmacSHA256长度应为32字节", 32, macResult.length);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        Assert.assertNull("并发多算法访问不应发生异常: " +
                (exception.get() != null ? exception.get().getMessage() : ""), exception.get());
    }

    /**
     * 测试并发环境下带盐值哈希的线程安全
     */
    @Test
    public void testConcurrentHashWithSalt() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> exception = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            final String content = "test content " + i;
            executor.submit(() -> {
                try {
                    byte[] result = MessageDigestUtils.sha(MessageDigestUtils.DigestType.SHA256,
                            content.getBytes(StandardCharsets.UTF_8), "common-salt");
                    Assert.assertEquals("带盐值SHA-256长度应为32字节", 32, result.length);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        Assert.assertNull("并发带盐值哈希不应发生异常: " +
                (exception.get() != null ? exception.get().getMessage() : ""), exception.get());
    }
}
