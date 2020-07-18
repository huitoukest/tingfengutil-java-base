package com.tingfeng.util.java.base.common.utils;

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


}
