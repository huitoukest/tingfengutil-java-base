package com.tingfeng.util.java.base.io;

/**
 * 缓存写入接口：按块索引写入缓存数据
 *
 * 与 {@link CacheReader} 分离定义，读取与写入缓存可分别定制实现；
 * 块索引由调用方（如 RangeCachingInputStream）按 position / blockSize 整除计算。
 *
 * 约定：
 * - 写入失败抛项目 IOException（由调用方捕获降级，读路径不受影响）
 * - 实现应保证并发写同一块不产生半写数据（如原子替换）
 *
 * @author huitoukest
 */
public interface CacheWriter {

    /**
     * 写入指定索引的缓存块
     * @param blockIndex 块索引（从 0 起）
     * @param data 块数据，不允许为 null
     * @throws com.tingfeng.util.java.base.lang.exception.IOException 缓存写入失败
     */
    void write(long blockIndex, byte[] data);
}
