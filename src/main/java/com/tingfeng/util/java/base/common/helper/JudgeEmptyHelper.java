package com.tingfeng.util.java.base.common.helper;

import com.tingfeng.util.java.base.common.inter.ObjectDealReturnInter;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.tingfeng.util.java.base.common.utils.ObjectUtils.isEmpty;

/**
 * 对象判空的Helper
 * @date 2019-10-23
 * @author wanggang
 */
public class JudgeEmptyHelper  implements ObjectDealReturnInter<Boolean> {

    private boolean recursive;
    private boolean isTrim;

    private static final JudgeEmptyHelper INSTANCE = new JudgeEmptyHelper();
    private static volatile  Map<String, JudgeEmptyHelper> helperMap = new HashMap<>();

    private JudgeEmptyHelper(){
        this.recursive = false;
        this.isTrim = true;
    }

    private JudgeEmptyHelper(boolean recursive, boolean isTrim) {
        this.recursive = recursive;
        this.isTrim = isTrim;
    }

    /**
     * double check ,提高性能
     * @param recursive
     * @param isTrim
     * @return
     */
    public static JudgeEmptyHelper newInstance(boolean recursive, boolean isTrim) {
        String key = getKey(recursive,isTrim);
        if(helperMap.get(key) == null){
            synchronized (helperMap){
                if(helperMap.get(key) == null) {
                    helperMap.put(key, new JudgeEmptyHelper(recursive, isTrim));
                }
            }
        }
        return helperMap.get(key);
    }

    /**
     * 默认不递归，但是去除字符序列首尾的空格;
     * @return
     */
    public static JudgeEmptyHelper newInstance() {
        return newInstance(false,true);
    }

    private static String getKey(boolean recursive, boolean isTrim){
        return String.valueOf(recursive) + String.valueOf(isTrim);
    }

    @Override
    public Boolean dealCollection(Collection<?> data) {
        if (data.isEmpty()) {
            return true;
        }
        if (recursive) {
            for (Object key : data) {
                if (!isEmpty(key, recursive, isTrim)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean dealMap(Map<?, ?> map) {
        if (map.isEmpty()) {
            return true;
        }
        if (recursive) {
            for (Object key : map.keySet()) {
                if (!isEmpty(key, recursive, isTrim)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean dealArray(Object[] data) {
        if (data.length <= 0) {
            return true;
        }
        if (recursive) {
            for (Object key : data) {
                if (!isEmpty(key, recursive, isTrim)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean dealCharSequence(CharSequence obj) {
        String str = obj.toString();
        if (isTrim) {
            str = str.trim();
        }
        if ("".equals(str)) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean dealDate(Date obj) {
        if (null == obj) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean dealCommonObject(Object obj) {
        if (null == obj) {
            return true;
        }
        return false;
    }
}
