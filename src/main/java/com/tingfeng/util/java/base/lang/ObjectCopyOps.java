package com.tingfeng.util.java.base.lang;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 对象拷贝操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link ObjectUtils} 对外提供统一 API。
 */
class ObjectCopyOps {

    private ObjectCopyOps() {

    }

    /**
     * 深度拷贝数组对象（浅拷贝，只拷贝引用）
     *
     * @param src  源数组
     * @param dest 目标数组
     * @param <T>  数组元素类型
     */
    static <T> void cloneArray(T[] src, T[] dest) {
        if (src != null) {
            System.arraycopy(src, 0, dest, 0, src.length);
        }
    }

    /**
     * 深度拷贝对象（通过序列化方式）
     *
     * @param src 待拷贝的对象
     * @param <T> 对象类型
     * @return 拷贝后的新对象
     */
    static <T> T clone(T src) {
        return deepCopy(src);
    }

    /**
     * 深度拷贝普通的对象（通过Java序列化方式）
     *
     * @param src 待拷贝的对象
     * @param <T> 对象类型
     * @return 拷贝后的新对象
     */
    private static <T> T deepCopy(T src) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            return (T) in.readObject();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
