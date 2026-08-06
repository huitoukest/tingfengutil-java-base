package com.tingfeng.util.java.base.io;

/**
 * 缓存读取接口：按块索引读取缓存数据
 *
 * 与 {@link CacheWriter} 分离定义，读取与写入缓存可分别定制实现；
 * 块索引由调用方（如 RangeCachingInputStream）按 position / blockSize 整除计算。
 *
 * 约定：
 * - 指定索引缓存块不存在返回 null
 * - IO 异常抛项目 IOException（由调用方捕获降级为 miss）
 *
 * @author huitoukest
 */
public interface CacheReader {

    /**
     * 读取指定索引的缓存块
     * @param blockIndex 块索引（从 0 起）
     * @return 缓存块字节数据；不存在返回 null
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 缓存读取失败
     */
    byte[] read(long blockIndex);
}
