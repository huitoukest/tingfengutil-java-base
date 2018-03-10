package com.tingfeng.util.java.base.common.utils;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import com.tingfeng.util.java.base.common.constant.ObjectTypeString;
import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.utils.datetime.DateUtils;
import com.tingfeng.util.java.base.common.utils.string.StringConvertUtils;

public class ObjectUtils {
	
	/**
	 * 把xml 转为object
	 * 
	 * @param xml
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static Object getObjectByXml(String xml) throws UnsupportedEncodingException {
		XMLDecoder decoder = null;
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes("UTF8"));
			decoder = new XMLDecoder(new BufferedInputStream(in));
			return decoder.readObject();
		} finally {
			if(null !=decoder) {
				decoder.close();
			}			
		}
	}
	/**
	 * 如果cls是基础数据类型和包装类型则返回转换之后的值,否则返回原值
	 * @param cls
	 * @param obj
	 * @return
	 */
	public static Object getObject(Class<?> cls,Object obj){
		String value=null;
		if(obj!=null)
			value=obj.toString();
		if(cls==null) return obj;
		if(obj==null) return null;
		switch (cls.getName()) {
		case ObjectTypeString.clsNameBoolean:
			 return StringConvertUtils.getBoolean(value);
		case ObjectTypeString.clsNameByte:
			 return StringConvertUtils.getByte(value);
		case ObjectTypeString.clsNameDate:
			 return DateUtils.getDate(value);
		case ObjectTypeString.clsNameLong:
			 return StringConvertUtils.getLong(value);
		case ObjectTypeString.clsNameInteger:
			 return StringConvertUtils.getInteger(value);
		case ObjectTypeString.clsNameFloat:
			 return StringConvertUtils.getFloat(value);
		case ObjectTypeString.clsNameDouble:
			return StringConvertUtils.getDouble(value);
		case ObjectTypeString.clsNameShort:
			return StringConvertUtils.getShort(value);
		case ObjectTypeString.clsNameString:
			return value;
		case ObjectTypeString.fieldBoolean:
			 return StringConvertUtils.getBoolean(value);
		case ObjectTypeString.fieldByte:
			 return StringConvertUtils.getByte(value);
		case ObjectTypeString.fieldDate:
			 return DateUtils.getDate(value);
		case ObjectTypeString.fieldLong:
			 return StringConvertUtils.getLong(value);
		case ObjectTypeString.fieldInteger:
			 return StringConvertUtils.getInteger(value);		 
		case ObjectTypeString.fieldFloat:
			 return StringConvertUtils.getFloat(value);
		case ObjectTypeString.fieldDouble:
			return StringConvertUtils.getDouble(value);
		case ObjectTypeString.fieldShort:
			return StringConvertUtils.getShort(value);
		case ObjectTypeString.fieldString:
			return value;
		case ObjectTypeString.clsNameBaseBoolean:
			return StringConvertUtils.getBoolean(value, false);
		case ObjectTypeString.clsNameBaseByte:
			return StringConvertUtils.getByte(value,(byte) 0);
		case ObjectTypeString.clsNameBaseDouble:
			return StringConvertUtils.getDouble(value, 0d);
		case ObjectTypeString.clsNameBaseFloat:
			return StringConvertUtils.getFloat(value, 0f);
		case ObjectTypeString.clsNameBaseInt:
			return StringConvertUtils.getInteger(value, 0);
		case ObjectTypeString.clsNameBaseLong:
			return StringConvertUtils.getLong(value, 0L);
		case ObjectTypeString.clsNameBaseShort:
			return StringConvertUtils.getShort(value, (short)0);
		default:
			break;
		}
		return obj;
	}
	
	
	public static boolean isAnyNull(Object... objs) {
	    if(isNull(objs)) {
	        return true;
	    }
        for(Object obj : objs) {
                if(isNull(obj)) {
                    return true;
                }
        }
       return false;
    }
	
	public static boolean isAllNull(Object... objs) {
	    if(!isNull(objs)) {
            for(Object obj : objs) {
                if(!isNull(obj)) {
                    return false;
                }
            }
        }
       return true;
    }
	
	public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }
	
	public static boolean isAllNotNull(Object... objs) {
	    if(!isNull(objs)) {
            for(Object obj : objs) {
                if(isNull(obj)) {
                    return false;
                }
            }
        }
       return true;
    }
	
	public static boolean isAnyNotNull(Object... objs) {
        if(!isNull(objs)) {
            for(Object obj : objs) {
                if(!isNull(obj)) {
                    return true;
                }
            }
        }
       return false;
    }
	
	/**
	 * 传入的对象是否为null
	 * @param obj java对象
	 * @return 对象为null或者空：true，以外：false
	 */
	public static boolean isNull(Object obj) {
		return null == obj;
	}

	/**
     * 传入的所有对象是否为空（String是否等于空串，数组和集合,Map是否有内容）
     * @param isTrim 字符串是否自动trim
     * @param objs java对象的组数
     * @return 对象为null或者空：true，以外：false
     */
	public static boolean isAllEmpty(boolean isTrim,Object... objs) {
        if(isEmpty(isTrim,objs)) {
            return true;
        }
	    for(Object obj : objs ) {
	        if(!isEmpty(isTrim,obj)) {
	            return false;
	        }
	    }
	    return true;
	}
	
	/**
	 * 其中的对象是否有空对象
	 * @param isTrim
	 * @param objs
	 * @return
	 */
	public static boolean isAnyEmpty(boolean isTrim,Object... objs) {
        if(isEmpty(isTrim,objs)) {
            return true;
        }
        for(Object obj : objs ) {
            if(isEmpty(isTrim,obj)) {
                return true;
            }
        }
        return false;
    }

	public static boolean isEmpty(Object obj) {
		return isEmpty(true,obj);
	}
	/**
	 * 传入的对象是否为空（String是否等于空串，数组和集合,Map是否有内容）
	 * @param isTrim 字符串是否自动trim
	 * @param obj java对象
	 * @return 对象为null或者空：true，以外：false
	 */
	public static boolean isEmpty(boolean isTrim,Object obj) {
        if(isNull(obj)) {
            return true;
        }
        if (obj instanceof String) {
            String tmp = (String)obj;          
            if(isTrim) {
                tmp = tmp.trim();
            }
            return "".equals(tmp);
		}
        if(obj.getClass().isArray()) {
            return Array.getLength(obj) <= 0;
        }
        if(obj instanceof Collection<?>) {
            return ((Collection<?>) obj).size() <= 0;
        }
        
        if(obj instanceof Map<?,?>) {
            return ((Map<?,?>) obj).size() <= 0;
        }
        
        return false;
	}

	/**
	 * 传入的两个String对象是否相等
	 * @param str1 String对象1
	 * @param str2 String对象2
	 * @return 两个对象相等：true，以外：false
	 */
	public static boolean isEquals(String str1, String str2) {
		if ((isNull(str1) && isNull(str2))
				|| (str1 != null && str1.equals(str2))
				|| (str2 != null && str2.equals(str1))) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 遇到空值的时候就返回默认值
	 * 如果没有空值，则返回最后一个values中的值
	 * @param defaultValue
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static <T> T getValue(T defaultValue,Object ... values) {
	       if(null == values) {
	           return defaultValue;
	       }
	       Object v = null;
	       for(Object obj : values) {
	           if(isNull(obj)) {
	               return defaultValue;
	           }
	           v = obj;
	       }
	       return (T)v;
	}

	/**
	 * 遇到空值的时候就返回默认值
	 * 如果没有空值，则返回最后一个values中的值
	 * @param defaultValue
	 * @param convertIs 迭代获取值
	 * @return
	 */
	public static <T,E> T getValue(T defaultValue, E source, ConvertI<Object,Object> ... convertIs) {
		if(source== null){
			return defaultValue;
		}
		Object v = null;
		for(ConvertI<Object,Object> convert : convertIs) {
			if(isNull(convert)) {
				return defaultValue;
			}
			v = convert.convert(v);
		}
		return (T)v;
	}

	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}
}
