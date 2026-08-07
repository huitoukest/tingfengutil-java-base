package com.tingfeng.util.java.base.lang.support;

import com.tingfeng.util.java.base.lang.StringUtils;

import java.util.regex.Pattern;

/**
 * getter/setter 方法命名实现。
 *
 * 包级私有，不对外暴露。通过 {@link ReflectUtils} 对外提供统一 API。
 */
class ReflectNameOps {

    /**
     * 判断一个属性名称的首字母是否需要被转换
     */
    private static Pattern needConvertFiled = Pattern.compile("^[_a-z][^A-Z]|[_a-z]$");

    private ReflectNameOps() {

    }

    /**
     * 根据属性名称生成 getter 方法名。
     *
     * 转换规则：
     * 1. 如果属性名的第二个字母大写，那么该属性名直接用作 getter/setter 方法中 get/set 的后部分，大小写不变。
     * 例如属性名为 uName，方法是 getuName/setuName。
     * 2. 如果属性名的前两个字母是大写（一般的专有名词和缩略词都会大写），也是属性名直接用作 getter/setter 方法中
     * get/set 的后部分。例如属性名为 URL，方法是 getURL/setURL。
     * 3. 如果属性名的首字母大写，也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为 Name，
     * 方法是 getName/setName，这种是最糟糕的情况，会找不到属性出错，因为默认的属性名是 name。
     * 4. 如果属性名以 "is" 开头，则 getter 方法会省掉 get，set 方法会去掉 is。例如属性名为 isOK，方法是 isOK/setOK。
     *
     * @param fieldName 属性名称
     * @return getter 方法名
     */
    static String getGetterName(String fieldName) {
        boolean hasIs = fieldName.startsWith("is");
        String str = formatGetterOrSetterFieldName(fieldName);
        if (hasIs) {
            str = "is" + str;
        } else {
            str = "get" + str;
        }

        return str;
    }

    /**
     * 根据属性名称生成 setter 方法名。
     *
     * 转换规则：
     * 1. 如果属性名的第二个字母大写，那么该属性名直接用作 getter/setter 方法中 get/set 的后部分，大小写不变。
     * 例如属性名为 uName，方法是 getuName/setuName。
     * 2. 如果属性名的前两个字母是大写（一般的专有名词和缩略词都会大写），也是属性名直接用作 getter/setter 方法中
     * get/set 的后部分。例如属性名为 URL，方法是 getURL/setURL。
     * 3. 如果属性名的首字母大写，也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为 Name，
     * 方法是 getName/setName，这种是最糟糕的情况，会找不到属性出错，因为默认的属性名是 name。
     * 4. 如果属性名以 "is" 开头，则 getter 方法会省掉 get，set 方法会去掉 is。例如属性名为 isOK，方法是 isOK/setOK。
     *
     * @param fieldName 属性名称
     * @return setter 方法名
     */
    static String getSetterName(String fieldName) {
        String str = formatGetterOrSetterFieldName(fieldName);
        str = "set" + str;
        return str;
    }

    /**
     * 格式化 getter/setter 方法名中的属性部分。
     *
     * 转换规则：
     * 1. 如果属性名的第二个字母大写，那么该属性名直接用作 getter/setter 方法中 get/set 的后部分，大小写不变。
     * 例如属性名为 uName，方法是 getuName/setuName。
     * 2. 如果属性名的前两个字母是大写（一般的专有名词和缩略词都会大写），也是属性名直接用作 getter/setter 方法中
     * get/set 的后部分。例如属性名为 URL，方法是 getURL/setURL。
     * 3. 如果属性名的首字母大写，也是属性名直接用作 getter/setter 方法中 get/set 的后部分。例如属性名为 Name，
     * 方法是 getName/setName，这种是最糟糕的情况，会找不到属性出错，因为默认的属性名是 name。
     * 4. 如果属性名以 "is" 开头，则 getter 方法会省掉 get，set 方法会去掉 is。例如属性名为 isOK，方法是 isOK/setOK。
     *
     * 即：只有前两位字母都是小写，或者只有一位小写字母时，首字母大写。
     *
     * @param fieldName 属性名称
     * @return 格式化后的属性名称
     */
    private static String formatGetterOrSetterFieldName(String fieldName) {
        if (needConvertFiled.matcher(fieldName).find()) {
            fieldName = StringUtils.toUpperFirstChar(fieldName);
        }
        return fieldName;
    }
}
