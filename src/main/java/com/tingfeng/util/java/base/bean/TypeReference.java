package com.tingfeng.util.java.base.bean;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 泛型类型引用，用于在运行时保留泛型信息。
 * <p>
 * 使用方式：
 * <pre>
 * TypeReference&lt;List&lt;UserDTO&gt;&gt; ref = new TypeReference&lt;List&lt;UserDTO&gt;&gt;() {};
 * Class&lt;?&gt; genericType = ref.getType(); // 获取 List&lt;UserDTO&gt; 的泛型参数
 * </pre>
 *
 * @param <T> 泛型类型
 */
public abstract class TypeReference<T> {

    private final Type type;

    /**
     * 构造器，通过匿名子类捕获泛型信息。
     *
     * @throws RuntimeException 如果未使用匿名类方式（即缺少类型参数）
     */
    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new RuntimeException("Missing type parameter");
        }
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    /**
     * 获取泛型类型。
     *
     * @return Type
     */
    public Type getType() {
        return type;
    }
}