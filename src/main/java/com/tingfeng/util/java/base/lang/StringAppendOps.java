package com.tingfeng.util.java.base.lang;

import com.tingfeng.util.java.base.lang.inter.returnfunction.FunctionROne;
import com.tingfeng.util.java.base.pool.FixedPoolHelper;

import java.util.stream.Stream;

/**
 * 字符串拼接与 StringBuilder 池操作实现。
 *
 * 包级私有，不对外暴露。通过 {@link StringUtils} 对外提供统一 API。
 */
class StringAppendOps {

    /**
     * null 对象的字符串表示，即 "null"
     */
    static final String STR_NULL_OBJ = String.valueOf(((Object) null));

    /**
     * 默认的StringBuilder的数量
     */
    private static final int DEFAULT_MAX_SB_SIZE = 16;
    private static final int DEFAULT_INIT_SB_LENGTH = 128;
    private static final int DEFAULT_MAX_SB_LENGTH = 512;

    /**
     * 公共的StringBuilder的资源，用于多线程时复用对象提高效率
     */
    private static final FixedPoolHelper<StringBuilder> stringBuilderPool = new FixedPoolHelper<>(DEFAULT_MAX_SB_SIZE, () -> new StringBuilder(DEFAULT_INIT_SB_LENGTH), (sb) -> {
        int len = sb.length();
        if (len > DEFAULT_MAX_SB_LENGTH) {
            sb.delete(DEFAULT_MAX_SB_LENGTH, len);
        }
        sb.setLength(0);
    });

    private StringAppendOps() {

    }

    /**
     * 数字转字符串
     *
     * @param num
     * @param minValue 如果小于minValue，则输出""
     * @return
     */
    static String toString(Number num, double minValue) {
        if (num == null) {
            return null;
        } else if (num instanceof Integer && (Integer) num > minValue) {
            return Integer.toString((Integer) num);
        } else if (num instanceof Long && (Long) num > minValue) {
            return Long.toString((Long) num);
        } else if (num instanceof Float && (Float) num > minValue) {
            return Float.toString((Float) num);
        } else if (num instanceof Double && (Double) num > minValue) {
            return Double.toString((Double) num);
        } else {
            return "";
        }
    }

    /***************************************************************************
     * repeat - 通过源字符串重复生成N次组成新的字符串。
     *
     * @param src
     *            - 源字符串 例如: 空格(" "), 星号("*"), "浙江" 等等...
     * @param num
     *            - 重复生成次数
     * @return 返回已生成的重复字符串
     **************************************************************************/
    static String repeat(String src, int num) {
        return stringBuilderPool.run(s -> {
            for (int i = 0; i < num; i++) {
                s.append(src);
            }
            return s.toString();
        });
    }

    /**
     * 一个高效的支持多线程的字符串append工具，传入StringBuilder用于自定义append
     *
     * @param functionROne
     * @return
     */
    static String doAppend(FunctionROne<String, StringBuilder> functionROne) {
        return stringBuilderPool.run(functionROne);
    }

    /**
     * 将objects中的对象按照顺序依次append到StringBuilder中并且返回
     *
     * @param freeMemoryThen 使用完毕后，当内容长度大于 512 字符（DEFAULT_MAX_SB_LENGTH）时，
     *                       删除超过 512 字符的部分，即最低保留 512 字符
     * @param objects        Object[]
     * @param isAppendNull   是否将null值也append到字符串中，默认为false
     * @return
     */
    static String appendValue(boolean freeMemoryThen, boolean isAppendNull, Object[] objects) {
        if (objects == null) {
            if(isAppendNull){
                return STR_NULL_OBJ;
            }
            return null;
        }
        return stringBuilderPool.run(sb -> {
            Stream.of(objects).forEach(it -> {
                if (it == null && isAppendNull) {
                    sb.append(STR_NULL_OBJ);
                } else if (it != null) {
                    sb.append(it);
                }
            });
            if (freeMemoryThen && sb.length() > DEFAULT_MAX_SB_LENGTH) {
                sb.delete(0, Math.max(0, sb.length() - DEFAULT_MAX_SB_LENGTH));
            }
            return sb.toString();
        });
    }

    /**
     * 将objects中的对象按照顺序依次append到StringBuilder中并且返回
     *
     * @param objects      Object[]
     * @param isAppendNull 是否将null值也append到字符串中，默认为false
     * @return
     */
    static String appendValue(boolean isAppendNull, Object... objects) {
        return appendValue(false, isAppendNull, objects);
    }

    /**
     * 将objects中的对象按照顺序依次append到StringBuilder中并且返回
     * 默认将null对象忽略，不会append到字符串中
     *
     * @param objects
     * @return
     */
    static String append(Object... objects) {
        if (objects == null) {
            return null;
        }
        return appendValue(false, objects);
    }
}
