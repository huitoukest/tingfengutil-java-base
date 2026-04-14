package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.bean.BeanUtils;
import com.tingfeng.util.java.base.common.inter.PropertyFunction;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * lambda 相关的一些工具
 */
public class LambdaUtils {
    /**
     * 通过Bean get方法的函数引用来获取其方法对应的property名称
     * @param func 如 User::getId
     * @param <T> 必须是标准的java bean类型
     * @return
     */
    public static <T> String getFieldName(PropertyFunction<T, ?> func) {
        try {
            if(!func.getClass().isSynthetic()){
                throw new RuntimeException("该方法仅能传入 lambda 表达式产生的合成类");
            }
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(Boolean.TRUE);
            // 利用jdk的SerializedLambda 解析方法引用
            java.lang.invoke.SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
            String getter = serializedLambda.getImplMethodName();
            return BeanUtils.getFieldNameByGetter(getter);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从Function获取声明该方法的类
     * @param function 函数式接口
     * @param <T> 类类型
     * @return 类对象
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getDeclaringClass(Function<T, ?> function) {
        try {
            // 通过反射获取SerializedLambda对象
            Method writeReplace = function.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(function);

            // 获取实现类名（斜杠格式），需要转换为点格式
            String implClassName = serializedLambda.getImplClass().replace('/', '.');

            // 加载类
            Class<?> clazz = Class.forName(implClassName);

            return (Class<T>) clazz;
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract class from function", e);
        }
    }
}