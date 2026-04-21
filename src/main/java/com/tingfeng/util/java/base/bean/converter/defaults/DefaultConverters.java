package com.tingfeng.util.java.base.bean.converter.defaults;

import com.tingfeng.util.java.base.bean.converter.ConverterRegistry;

/**
 * 默认转换器统一注册入口
 * <p>
 * 聚合所有分类转换器的注册方法，依次调用完成完整注册。
 */
public final class DefaultConverters {

    private DefaultConverters() {}

    /**
     * 注册所有默认转换器到指定注册中心
     *
     * @param registry 转换器注册中心
     */
    public static void registerDefaults(ConverterRegistry registry) {
        if (registry == null) {
            return;
        }
        StringConverters.register(registry);
        NumberConverters.register(registry);
        DateTimeConverters.register(registry);
        ByteArrayConverters.register(registry);
        CollectionConverters.register(registry);
        UrlConverters.register(registry);
        BigNumberConverters.register(registry);
        // EnumConverters.registerEnum(MyEnum.class, registry); // 需指定枚举类
    }
}
