package com.tingfeng.util.java.base.cache.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CacheVO<T> {
    private long expireTime;
    private T value;
}
