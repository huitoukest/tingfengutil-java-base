package com.tingfeng.util.java.base.bean.copier;

/**
 * 属性访问结果封装，包含属性值和访问模式信息。
 *
 * 用于替代原有的异常抛出模式，以更柔和的方式处理属性不存在的情况。
 *
 * @param <T> 属性值类型
 * @author huitoukest
 */
public final class PropertyResult<T> {

    /** 属性不存在的结果 */
    private static final PropertyResult<?> NOT_FOUND = new PropertyResult<>(false, null, null);

    /** 属性是否存在 */
    private final boolean exists;

    /** 属性值 */
    private final T value;

    /** 访问模式 */
    private final PropertyAccessMode mode;

    /**
     * 私有构造器，禁止外部直接实例化
     *
     * @param exists 属性是否存在
     * @param value 属性值
     * @param mode 访问模式
     */
    private PropertyResult(boolean exists, T value, PropertyAccessMode mode) {
        this.exists = exists;
        this.value = value;
        this.mode = mode;
    }

    /**
     * 返回属性不存在的结果
     *
     * @param <T> 属性值类型
     * @return 属性不存在的结果
     */
    public static <T> PropertyResult<T> notFound() {
        @SuppressWarnings("unchecked")
        PropertyResult<T> result = (PropertyResult<T>) NOT_FOUND;
        return result;
    }

    /**
     * 创建包含属性值和访问模式的结果
     *
     * @param value 属性值
     * @param mode 访问模式
     * @param <T> 属性值类型
     * @return 属性结果
     */
    public static <T> PropertyResult<T> of(T value, PropertyAccessMode mode) {
        return new PropertyResult<>(true, value, mode);
    }

    /**
     * 判断属性是否存在
     *
     * @return 属性是否存在
     */
    public boolean exists() {
        return exists;
    }

    /**
     * 获取属性值
     *
     * 注意：调用此方法前应先检查 exists()，避免在属性不存在时获取 null 值
     *
     * @return 属性值，若属性不存在则返回 null
     */
    public T getValue() {
        return value;
    }

    /**
     * 获取访问模式
     *
     * @return 访问模式，若属性不存在则返回 null
     */
    public PropertyAccessMode getMode() {
        return mode;
    }
}