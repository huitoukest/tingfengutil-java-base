package com.tingfeng.util.java.base.cache.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存值对象，包含过期时间和实际值
 *
 * @param <T> 值类型
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CacheVO<T> {
    private long expireTime;
    private T value;
}
