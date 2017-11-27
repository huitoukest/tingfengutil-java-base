package com.tingfeng.util.java.base.common.utils;

import java.lang.reflect.Field;

import com.tingfeng.util.java.base.common.constant.ObjectType;
import com.tingfeng.util.java.base.common.constant.ObjectTypeString;


/**
 * 
 * @author huitoukest
 * @version 20160516
 */
public class ObjectTypeUtils{	
	public static ObjectType getObjectType(Class<?> cls){
		return getObjectType(cls.getName());
	}
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
	public static ObjectType getObjectType(Field field){
		return getObjectType(field.getType().getCanonicalName());
	}
	/**
	 *是否是基础数据类型,当为other类型，返回false；
	 * @param clsName
	 * @see util.tingfeng.java.common.constant.ObjectType
	 * @return
	 */
	public static boolean isBaseTypeObject(String  clsName){
		if(getObjectType(clsName).equals(ObjectType.Other))
			return false;
		return true;
	}
	/**
	 *是否是基础数据类型,当为other类型，返回false；
	 * @param clsName
	 * @see util.tingfeng.java.common.constant.ObjectType
	 * @return
	 */
	public static boolean isBaseTypeObject(Field field){
		return isBaseTypeObject(field.getType().getCanonicalName());
	}
	/**
	 *是否是基础数据类型,当为other类型，返回false；
	 * @param clsName
	 * @see util.tingfeng.java.common.constant.ObjectType
	 * @return
	 */
	public static boolean isBaseTypeObject(Class<?> cls){
		return isBaseTypeObject(cls.getName());
	}
}
