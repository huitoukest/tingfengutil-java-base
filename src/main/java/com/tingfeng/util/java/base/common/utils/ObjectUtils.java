package com.tingfeng.util.java.base.common.utils;

import java.beans.XMLDecoder;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

import com.tingfeng.util.java.base.common.constant.ObjectType;
import com.tingfeng.util.java.base.common.constant.ObjectTypeString;
import com.tingfeng.util.java.base.common.inter.ConvertI;
import com.tingfeng.util.java.base.common.inter.ObjectDealReturnInter;
import com.tingfeng.util.java.base.common.utils.datetime.DateUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;

/**
 * 一些通用的对象工具类
 */
public class ObjectUtils {

	/**
	 * 通过class获取对象的类型
	 * @param cls
	 * @return
	 */
	public static ObjectType getObjectType(Class<?> cls){
		return getObjectType(cls.getName());
	}

	/**
	 * 通过className获取对象的类型
	 * @param className
	 * @return
	 */
	public  static ObjectType getObjectType(String className){
		String type=className;
		switch (type) {
			case ObjectTypeString.clsNameBaseBoolean:
				return ObjectType.Boolean;
			case ObjectTypeString.clsNameBoolean:
				return ObjectType.Boolean;
			case ObjectTypeString.clsNameDate:
				return ObjectType.Date;
			case ObjectTypeString.clsNameBaseFloat:
				return ObjectType.Float;
			case ObjectTypeString.clsNameFloat:
				return ObjectType.Float;
			case ObjectTypeString.clsNameBaseDouble:
				return ObjectType.Double;
			case ObjectTypeString.clsNameDouble:
				return ObjectType.Double;
			case ObjectTypeString.clsNameBaseLong:
				return ObjectType.Long;
			case ObjectTypeString.clsNameLong:
				return ObjectType.Long;
			case ObjectTypeString.clsNameBaseInt:
				return ObjectType.Integer;
			case ObjectTypeString.clsNameInteger:
				return ObjectType.Integer;
			case ObjectTypeString.clsNameString:
				return ObjectType.String;
			case ObjectTypeString.clsNameBaseShort:
				return ObjectType.Short;
			case ObjectTypeString.clsNameShort:
				return ObjectType.Short;
			case ObjectTypeString.clsNameBaseByte:
				return ObjectType.Byte;
			case ObjectTypeString.clsNameByte:
				return ObjectType.Byte;
			default:
				break;
		}

		return ObjectType.Other;
	}

	/**
	 * 通过属性来返回此属性的类型
	 * @param field
	 * @return
	 */
	public static ObjectType getObjectType(Field field){
		return getObjectType(field.getType().getCanonicalName());
	}

	/**
	 * 是否是基础数据类型,当为other类型，返回false；
	 * @param clsName
	 * @return
	 */
	public static boolean isBaseTypeObject(String  clsName){
		if(getObjectType(clsName).equals(ObjectType.Other))
			return false;
		return true;
	}
	/**
	 * 是否是基础数据类型,当为other类型，返回false；
	 * @param field
	 * @return
	 */
	public static boolean isBaseTypeObject(Field field){
		return isBaseTypeObject(field.getType().getCanonicalName());
	}
	/**
	 * 是否是基础数据类型,当为other类型，返回false；
	 * @param cls
	 * @return
	 */
	public static boolean isBaseTypeObject(Class<?> cls){
		return isBaseTypeObject(cls.getName());
	}

	/**
	 * 处理一个对象，并返回想要的结果
	 * @param source 来源对象
	 * @param recursive 是否递归处理
	 * @param isTrim 是否trim
	 * @param deal 处理的方式
	 * @param <S> 对象类型
	 * @param <T> 返回的结果类型
	 * @return
	 */
    public static <S,T> T dealObject(S source,boolean recursive,boolean isTrim,ObjectDealReturnInter<S,T> deal) {
        if(null == source){
            return deal.dealCommonObject(source);
        }
        if(source instanceof Map){
            return deal.dealMap(source, recursive);
        }
        if(source instanceof Collection){
            return deal.dealCollection(source, recursive);
        }
        if(source instanceof String){
            return deal.dealString(source,isTrim);
        }
        if(source.getClass().isArray()) {
            return deal.dealArray(source, recursive);
        }
        return deal.dealCommonObject(source);
    }
    
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
	 * 是否是布尔类
	 *
	 * @param cls
	 * @return
	 */
	public static Boolean isBoolean(Class<?> cls) {
		return Boolean.valueOf(cls != null && (Boolean.TYPE.isAssignableFrom(cls) || Boolean.class.isAssignableFrom(cls)));
	}
	/**
	 * 如果cls是基础数据类型和包装类型则返回转换之后的值,否则返回原值
	 * 这里如果obj是T类型，则直接返回，否则将之转为String类型后自动如果是基础数据类型
	 * 则转为T类型，否则返回null
	 * @param cls
	 * @param obj 传入的数据
	 * @return <T></>
	 */
	public static <T> T getObject(Class<T> cls,Object obj){
		if(cls == null) return (T)obj;
		if(obj == null) return null;
		if(cls.getName().equals(obj.getClass().getName())){
			return (T)obj;
		}
		String value  = obj.toString();
		switch (cls.getName()) {
		case ObjectTypeString.clsNameBoolean:
			 return (T) StringUtils.getBoolean(value);
		case ObjectTypeString.clsNameByte:
			 return (T)StringUtils.getByte(value);
		case ObjectTypeString.clsNameDate:
			 return (T)DateUtils.getDate(value);
		case ObjectTypeString.clsNameLong:
			 return (T)StringUtils.getLong(value);
		case ObjectTypeString.clsNameInteger:
			 return (T)StringUtils.getInteger(value);
		case ObjectTypeString.clsNameFloat:
			 return (T)StringUtils.getFloat(value);
		case ObjectTypeString.clsNameDouble:
			return (T)StringUtils.getDouble(value);
		case ObjectTypeString.clsNameShort:
			return (T)StringUtils.getShort(value);
		case ObjectTypeString.clsNameString:
			return (T)value;
		case ObjectTypeString.clsNameBaseBoolean:
			return (T)StringUtils.getBoolean(value, false);
		case ObjectTypeString.clsNameBaseByte:
			return (T)StringUtils.getByte(value,(byte) 0);
		case ObjectTypeString.clsNameBaseDouble:
			return (T)StringUtils.getDouble(value, 0d);
		case ObjectTypeString.clsNameBaseFloat:
			return (T)StringUtils.getFloat(value, 0f);
		case ObjectTypeString.clsNameBaseInt:
			return (T)StringUtils.getInteger(value, 0);
		case ObjectTypeString.clsNameBaseLong:
			return (T)StringUtils.getLong(value, 0L);
		case ObjectTypeString.clsNameBaseShort:
			return (T)StringUtils.getShort(value, (short)0);
		default:
			break;
		}
		return (T)obj;
	}

	/**
	 *
	 * @param objs
	 * @return 在objs中是否有一个obj是null
	 */
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
	/**
	 *
	 * @param objs
	 * @return 在objs中是否所有obj是null
	 */
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
	/**
	 *
	 * @param objs
	 * @return 在objs中是否所有obj不是是null
	 */
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
	/**
	 *
	 * @param objs
	 * @return 在objs中是否有一个obj不是null
	 */
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
        if(isEmpty(objs,isTrim)) {
            return true;
        }
	    for(Object obj : objs ) {
	        if(!isEmpty(obj,isTrim)) {
	            return false;
	        }
	    }
	    return true;
	}
	
	/**
	 * 其中的对象是否有空对象
	 * @param objs
	 * @return
	 */
	public static boolean isAnyEmpty(Object... objs) {
        if(isEmpty(objs,true)) {
            return true;
        }
        for(Object obj : objs ) {
            if(isEmpty(obj,true)) {
                return true;
            }
        }
        return false;
    }

	/**
	 * 判断一个对象是否为空
	 * @param obj
	 * @param isTrim 如果是字符串，是否去除首尾空白字符串
	 * @return
	 */
	public static boolean isEmpty(Object obj,boolean isTrim) {
		return isEmpty(obj,false,isTrim);
	}

	/**
	 * null or empt String ，不会trim
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
	    return isEmpty(obj,false,false);
	}
	
	/**
	 * 检查一个对象是否为空，如果是数组、Collection、Map结构则检查size或length是否大于0
	 * @param obj
	 * @param recursive 是否递归检查容器中的元素是否为空
	 * @param isTrim 是否trim
	 * @return
	 */
    public static boolean isEmpty(Object obj,boolean recursive,final boolean isTrim){
	    return dealObject(obj,recursive,isTrim,new ObjectDealReturnInter<Object,Boolean>(){

	        @Override
            public Boolean dealCollection(Object obj, boolean recursive) {
                @SuppressWarnings("unchecked")
                Collection<Object> data = (Collection<Object>) obj;
                if(data.isEmpty()){
                    return true;
                }
                if(recursive) {
                    for(Object key : data){
                        if(!isEmpty(key,recursive,isTrim)){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public Boolean dealMap(Object obj, boolean recursive) {
                @SuppressWarnings("unchecked")
                Map<Object,Object> map = ((Map<Object,Object>) obj);
                if(map.isEmpty()){
                    return true;
                }
                if(recursive) {
                    for(Object key : map.keySet()){
                        if(!isEmpty(key,recursive,isTrim)){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public Boolean dealArray(Object obj, boolean recursive) {
                Object[] data  = (Object[])obj;
                if(data.length <= 0){
                    return true;
                }
                if(recursive) {
                    for(Object key : data){
                        if(!isEmpty(key,recursive,isTrim)){
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }

            @Override
            public Boolean dealString(Object obj, boolean isTrim) {
                String str = obj.toString();
                if(isTrim) {
                    str = str.trim();
                }
                if("".equals(str)){
                    return true;
                }
                return false;
            }

            @Override
            public Boolean dealCommonObject(Object obj) {
                if(null == obj){
                    return true;
                }
                return false;
            }
	        
	    });
    }
	/**
	 * 两者都是null时表示相等，传入的两个String对象是否相等
	 * @param str1 String对象1
	 * @param str2 String对象2
	 * @return 两个对象相等：true，以外：false
	 */
	public static boolean equals(String str1, String str2) {
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
	 *
	 * @param defaultValue
	 * @param source
	 * @param convert
	 * @param <T> 返回值类型
	 * @param <S> 来源值类型
	 * @return
	 */
	public static <T,S> T getValue(T defaultValue, S source ,ConvertI<T,S> convert) {
		if(source== null){
			return defaultValue;
		}
		T v;
		try {
		    v = convert.convert(source);
		}catch (NullPointerException e) {
		    v = defaultValue;
        }
		return v;
	}
	
	/**
     * 遇到空值的时候就返回默认值
     * 如果没有空值，则返回最后一个values中的值
     * @param defaultValue
     * @param call 迭代获取值
     * @return
     */
	public static <T> T getValue(T defaultValue,Callable<T> call)  {
        T v;
        try {
            v = call.call();
        }catch (NullPointerException e) {
            v = defaultValue;
        }catch (Exception e) {
           throw new RuntimeException(e);
        }
        return v;
    }

	
	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj,true);
	}

	/**
	 * 深度拷贝数组对象
	 * @param src
	 * @param dest
	 * @param <T>
	 */
	public static <T> void deepCopyArray(T[] src,T[] dest) {
		if(null != src) {
			System.arraycopy(src, 0, dest, 0, src.length);
		}
	}

	/**
	 * 深度拷贝对象
	 * @param src
	 * @param <T>
	 * @return
	 */
	public static <T> T clone(T src){
		return deepCopy(src);
	}

	/**
	 * 深度拷贝对象
	 * @param src
	 * @param <T>
	 * @return
	 */
    public static <T> T deepCopy(T src){
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = null;
			out = new ObjectOutputStream(byteOut);
			out.writeObject(src);
			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in =new ObjectInputStream(byteIn);
			return (T)in.readObject();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
