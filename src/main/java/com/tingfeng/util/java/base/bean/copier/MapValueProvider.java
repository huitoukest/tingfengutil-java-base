package com.tingfeng.util.java.base.bean.copier;

import java.util.Map;

/**
 * 基于 Map 的 ValueProvider 实现。
 * <p>
 * 简单实现：直接委托 map.get(key)，暂不支持 a.b.c 点号嵌套路径。
 *
 * @author huitoukest
 */
public class MapValueProvider implements ValueProvider<Map<String, ?>> {

    private final Map<String, ?> map;

    public MapValueProvider(Map<String, ?> map) {
        this.map = map;
    }

    @Override
    public Object value(String key, Class<?> type) {
        return map.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }
}