package com.tingfeng.util.java.base.common.utils;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import com.tingfeng.util.java.base.common.constant.ObjectTypeString;
import com.tingfeng.util.java.base.common.utils.datetime.DateUtils;
import com.tingfeng.util.java.base.common.utils.string.StringConversionUtils;

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
			 return StringConversionUtils.getBoolean(value);
		case ObjectTypeString.clsNameByte:
			 return StringConversionUtils.getByte(value);
		case ObjectTypeString.clsNameDate:
			 return DateUtils.getDate(value);
		case ObjectTypeString.clsNameLong:
			 return StringConversionUtils.getLong(value);
		case ObjectTypeString.clsNameInteger:
			 return StringConversionUtils.getInteger(value);
		case ObjectTypeString.clsNameFloat:
			 return StringConversionUtils.getFloat(value);
		case ObjectTypeString.clsNameDouble:
			return StringConversionUtils.getDouble(value);
		case ObjectTypeString.clsNameShort:
			return StringConversionUtils.getShort(value);
		case ObjectTypeString.clsNameString:
			return value;
		case ObjectTypeString.fieldBoolean:
			 return StringConversionUtils.getBoolean(value);
		case ObjectTypeString.fieldByte:
			 return StringConversionUtils.getByte(value);
		case ObjectTypeString.fieldDate:
			 return DateUtils.getDate(value);
		case ObjectTypeString.fieldLong:
			 return StringConversionUtils.getLong(value);
		case ObjectTypeString.fieldInteger:
			 return StringConversionUtils.getInteger(value);		 
		case ObjectTypeString.fieldFloat:
			 return StringConversionUtils.getFloat(value);
		case ObjectTypeString.fieldDouble:
			return StringConversionUtils.getDouble(value);
		case ObjectTypeString.fieldShort:
			return StringConversionUtils.getShort(value);
		case ObjectTypeString.fieldString:
			return value;
		case ObjectTypeString.clsNameBaseBoolean:
			return StringConversionUtils.getBoolean(value, false);
		case ObjectTypeString.clsNameBaseByte:
			return StringConversionUtils.getByte(value,(byte) 0);
		case ObjectTypeString.clsNameBaseDouble:
			return StringConversionUtils.getDouble(value, 0d);
		case ObjectTypeString.clsNameBaseFloat:
			return StringConversionUtils.getFloat(value, 0f);
		case ObjectTypeString.clsNameBaseInt:
			return StringConversionUtils.getInteger(value, 0);
		case ObjectTypeString.clsNameBaseLong:
			return StringConversionUtils.getLong(value, 0L);
		case ObjectTypeString.clsNameBaseShort:
			return StringConversionUtils.getShort(value, (short)0);
		default:
			break;
		}
		return obj;
	}
	
	/**
	 * 传入的对象是否为null（String是否等于空串）
	 * @param obj java对象
	 * @return 对象为null或者空：true，以外：false
	 */
	public static boolean isNull(Object obj) {

		if (obj instanceof String) {
			return null == obj || "".equals(obj);
		}

		return null == obj;
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
}
