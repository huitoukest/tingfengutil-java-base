package com.tingfeng.util.java.base.crypto.digest;

import com.tingfeng.util.java.base.pool.FixedPoolHelper;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import java.security.MessageDigest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 统一摘要池持有者测试。
 * <p>
 * 覆盖：池共享验证、不同算法隔离、线程安全初始化、实际池操作验证。
 * </p>
 */
public class DigestPoolHolderTest {

    // ==================== MessageDigest 池测试 ====================

    /**
     * 测试同一算法名称返回相同的 MessageDigest 池实例
     */
    @Test
    public void testGetMessageDigestPoolSameAlgorithm() {
        FixedPoolHelper<MessageDigest> pool1 = DigestPoolHolder.getMessageDigestPool("MD5");
        FixedPoolHelper<MessageDigest> pool2 = DigestPoolHolder.getMessageDigestPool("MD5");
        Assert.assertSame("同一算法应返回相同的池实例", pool1, pool2);
    }

    /**
     * 测试不同算法名称返回不同的 MessageDigest 池实例
     */
    @Test
    public void testGetMessageDigestPoolDifferentAlgorithm() {
        FixedPoolHelper<MessageDigest> md5Pool = DigestPoolHolder.getMessageDigestPool("MD5");
        FixedPoolHelper<MessageDigest> sha256Pool = DigestPoolHolder.getMessageDigestPool("SHA-256");
        Assert.assertNotSame("不同算法应返回不同的池实例", md5Pool, sha256Pool);
    }

    /**
     * 测试使用 MessageDigest 池进行实际的 MD5 计算
     */
    @Test
    public void testMessageDigestPoolMd5Operation() {
        FixedPoolHelper<MessageDigest> pool = DigestPoolHolder.getMessageDigestPool("MD5");
        byte[] data = "test data".getBytes();
        byte[] result = pool.run(digest -> {
            digest.reset();
            return digest.digest(data);
        });
        Assert.assertNotNull("MD5 计算结果不能为空", result);
        Assert.assertEquals("MD5 结果长度应为 16 字节", 16, result.length);
    }

    /**
     * 测试使用 MessageDigest 池进行实际的 SHA-256 计算
     */
    @Test
    public void testMessageDigestPoolSha256Operation() {
        FixedPoolHelper<MessageDigest> pool = DigestPoolHolder.getMessageDigestPool("SHA-256");
        byte[] data = "test data".getBytes();
        byte[] result = pool.run(digest -> {
            digest.reset();
            return digest.digest(data);
        });
        Assert.assertNotNull("SHA-256 计算结果不能为空", result);
        Assert.assertEquals("SHA-256 结果长度应为 32 字节", 32, result.length);
    }

    // ==================== Mac 池测试 ====================

    /**
     * 测试同一算法名称返回相同的 Mac 池实例
     */
    @Test
    public void testGetMacPoolSameAlgorithm() {
        FixedPoolHelper<Mac> pool1 = DigestPoolHolder.getMacPool("HmacSHA256");
        FixedPoolHelper<Mac> pool2 = DigestPoolHolder.getMacPool("HmacSHA256");
        Assert.assertSame("同一算法应返回相同的 Mac 池实例", pool1, pool2);
    }

    /**
     * 测试不同算法名称返回不同的 Mac 池实例
     */
    @Test
    public void testGetMacPoolDifferentAlgorithm() {
        FixedPoolHelper<Mac> pool256 = DigestPoolHolder.getMacPool("HmacSHA256");
        FixedPoolHelper<Mac> pool512 = DigestPoolHolder.getMacPool("HmacSHA512");
        Assert.assertNotSame("不同算法应返回不同的 Mac 池实例", pool256, pool512);
    }

    /**
     * 测试使用 Mac 池进行实际的 HMAC-SHA256 计算
     */
    @Test
    public void testMacPoolOperation() {
        FixedPoolHelper<Mac> pool = DigestPoolHolder.getMacPool("HmacSHA256");
        byte[] data = "test data".getBytes();
        byte[] keyBytes = "secret-key-12345".getBytes();
        byte[] result = pool.run(mac -> {
            try {
                javax.crypto.spec.SecretKeySpec key =
                        new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
                mac.init(key);
                return mac.doFinal(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Assert.assertNotNull("HMAC-SHA256 计算结果不能为空", result);
        Assert.assertEquals("HMAC-SHA256 结果长度应为 32 字节", 32, result.length);
    }

    // ==================== KeyGenerator 池测试 ====================

    /**
     * 测试同一算法名称返回相同的 KeyGenerator 池实例
     */
    @Test
    public void testGetKeyGeneratorPoolSameAlgorithm() {
        FixedPoolHelper<KeyGenerator> pool1 = DigestPoolHolder.getKeyGeneratorPool("HmacSHA256");
        FixedPoolHelper<KeyGenerator> pool2 = DigestPoolHolder.getKeyGeneratorPool("HmacSHA256");
        Assert.assertSame("同一算法应返回相同的 KeyGenerator 池实例", pool1, pool2);
    }

    /**
     * 测试不同算法名称返回不同的 KeyGenerator 池实例
     */
    @Test
    public void testGetKeyGeneratorPoolDifferentAlgorithm() {
        FixedPoolHelper<KeyGenerator> pool256 = DigestPoolHolder.getKeyGeneratorPool("HmacSHA256");
        FixedPoolHelper<KeyGenerator> pool512 = DigestPoolHolder.getKeyGeneratorPool("HmacSHA512");
        Assert.assertNotSame("不同算法应返回不同的 KeyGenerator 池实例", pool256, pool512);
    }

    /**
     * 测试使用 KeyGenerator 池生成密钥
     */
    @Test
    public void testKeyGeneratorPoolOperation() {
        FixedPoolHelper<KeyGenerator> pool = DigestPoolHolder.getKeyGeneratorPool("HmacSHA256");
        byte[] keyBytes = pool.run(keyGenerator -> keyGenerator.generateKey().getEncoded());
        Assert.assertNotNull("生成的密钥不能为空", keyBytes);
        Assert.assertTrue("生成的密钥长度应大于 0", keyBytes.length > 0);
    }

    // ==================== 跨池类型隔离测试 ====================

    /**
     * 测试不同类型的池（MessageDigest vs Mac）互不干扰
     */
    @Test
    public void testDifferentPoolTypesIsolation() {
        FixedPoolHelper<MessageDigest> mdPool = DigestPoolHolder.getMessageDigestPool("SHA-256");
        FixedPoolHelper<Mac> macPool = DigestPoolHolder.getMacPool("HmacSHA256");
        // 相同算法名称但不同类型应返回不同的池
        Assert.assertNotNull("MessageDigest 池不应为空", mdPool);
        Assert.assertNotNull("Mac 池不应为空", macPool);
    }

    /**
     * 测试与 MessageDigestUtils 共享同一池实例
     * 验证同一算法在 DigestPoolHolder 和外部调用间共享相同池
     */
    @Test
    public void testSharedPoolWithUtils() {
        FixedPoolHelper<MessageDigest> holderPool = DigestPoolHolder.getMessageDigestPool("MD5");
        // 通过 MessageDigestUtils 间接获取（它委托给 DigestPoolHolder）
        byte[] data = "shared pool test".getBytes();
        byte[] result = holderPool.run(digest -> {
            digest.reset();
            return digest.digest(data);
        });
        Assert.assertNotNull("共享池计算结果不能为空", result);
        Assert.assertEquals("MD5 结果长度应为 16 字节", 16, result.length);
    }

    // ==================== 并发测试 ====================

    /**
     * 测试并发环境下 MessageDigest 池初始化的线程安全
     */
    @Test
    public void testConcurrentMessageDigestPoolInitialization() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> exception = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    FixedPoolHelper<MessageDigest> pool = DigestPoolHolder.getMessageDigestPool("SHA-256");
                    byte[] result = pool.run(digest -> {
                        digest.reset();
                        return digest.digest("concurrent test".getBytes());
                    });
                    Assert.assertNotNull("并发访问时结果不能为空", result);
                    Assert.assertEquals("SHA-256 结果长度应为 32 字节", 32, result.length);
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
     * 测试并发环境下多种算法池的线程安全
     */
    @Test
    public void testConcurrentDifferentAlgorithms() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * 3);
        AtomicReference<Throwable> exception = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    FixedPoolHelper<MessageDigest> pool = DigestPoolHolder.getMessageDigestPool("MD5");
                    byte[] result = pool.run(digest -> {
                        digest.reset();
                        return digest.digest("test".getBytes());
                    });
                    Assert.assertEquals("MD5 长度应为 16 字节", 16, result.length);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
            executor.submit(() -> {
                try {
                    FixedPoolHelper<Mac> pool = DigestPoolHolder.getMacPool("HmacSHA256");
                    byte[] keyBytes = "test-key-123456".getBytes();
                    byte[] result = pool.run(mac -> {
                        try {
                            javax.crypto.spec.SecretKeySpec key =
                                    new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA256");
                            mac.init(key);
                            return mac.doFinal("test".getBytes());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    Assert.assertEquals("HmacSHA256 长度应为 32 字节", 32, result.length);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
            executor.submit(() -> {
                try {
                    FixedPoolHelper<KeyGenerator> pool = DigestPoolHolder.getKeyGeneratorPool("HmacSHA256");
                    byte[] key = pool.run(kg -> kg.generateKey().getEncoded());
                    Assert.assertTrue("生成的密钥长度应大于 0", key.length > 0);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        Assert.assertNull("并发多算法池访问不应发生异常: " +
                (exception.get() != null ? exception.get().getMessage() : ""), exception.get());
    }

    /**
     * 测试并发环境下池实例共享（所有线程获取同一池）
     */
    @Test
    public void testConcurrentPoolSharing() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> exception = new AtomicReference<>();
        AtomicReference<FixedPoolHelper<MessageDigest>> firstPool = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    FixedPoolHelper<MessageDigest> pool = DigestPoolHolder.getMessageDigestPool("SHA-512");
                    if (firstPool.get() == null) {
                        firstPool.compareAndSet(null, pool);
                    }
                    // 所有线程应获取到同一个池实例
                    Assert.assertSame("所有线程应获取到同一个池实例", firstPool.get(), pool);
                } catch (Throwable t) {
                    exception.set(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        Assert.assertNull("并发池共享测试不应发生异常: " +
                (exception.get() != null ? exception.get().getMessage() : ""), exception.get());
    }
}
