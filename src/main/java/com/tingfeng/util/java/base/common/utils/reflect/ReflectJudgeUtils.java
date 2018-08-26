package com.tingfeng.util.java.base.common.utils.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.tingfeng.util.java.base.common.constant.ObjectTypeString;


/**
 * 
 * 	@author huitoukest
 *	用反射来判断的一个工具类
 */
public class ReflectJudgeUtils {
	/**
	 * 是否是静态方法
	 * @param cls 类名
	 * @param methodName 方法名称
	 * @param parameterTypes 每个参数的类型
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static boolean isStaticMethod(Class<?> cls,String methodName,Class<?> ...parameterTypes) throws NoSuchMethodException, SecurityException
	{
		Method method = cls.getMethod(methodName, parameterTypes);
		int  modifiers  = method.getModifiers();
		return Modifier.isStatic(modifiers); 
	}

	/**
	 * 是否是静态方法
	 * @param method
	 * @return
	 */
	public static boolean isStaticMethod(Method method){
		int  modifiers  = method.getModifiers();
		return Modifier.isStatic(modifiers); 
	}

	/**
	 * 是否是静态属性
	 * @param field
	 * @return
	 */
	public static boolean isStaticField(Field field){
	        boolean isStatic = Modifier.isStatic(field.getModifiers());
	        return isStatic;
	}
	
	
	/**
	 * 判断这个类是不是java.lang/math/utils包中自带的类;
	 * @param clazz
	 * @return
	 */
	public static boolean isBaseJavaClass(Class<?> clazz) {
		boolean isBaseClass = false;
		if(clazz.isArray()){
			isBaseClass = false;
		}else if (clazz.isPrimitive()||clazz.getPackage()==null
				|| clazz.getPackage().getName().equals("java.lang")
				|| clazz.getPackage().getName().equals("java.math")
				|| clazz.getPackage().getName().equals("java.util")) {
			isBaseClass =  true;
		}
		return isBaseClass;
	}
	/**
	 * 
	 * @param field
	 * @return 如果此类是基础数据或者包装类型或者Date类型,返回true;否则返回false; 如果cls为null,返回false;
	 */
	public static boolean isBaseFieldClass(Field field){
		return  isBaseFieldClass(field.getType());
	}
	/**
	 * 
	 * @param cls 
	 * @return 如果此类是基础数据或者包装类型或者Date类型,返回true;否则返回false; 如果cls为null,返回false;
	 */
	public static boolean isBaseFieldClass(Class<?> cls){
		if(cls==null) return false;
		switch (cls.getName()) {
		case ObjectTypeString.clsNameBoolean:
			 return true;
		case ObjectTypeString.clsNameByte:
			 return true;
		case ObjectTypeString.clsNameDate:
			 return true;
		case ObjectTypeString.clsNameLong:
			 return true;
		case ObjectTypeString.clsNameInteger:
			 return true;
		case ObjectTypeString.clsNameFloat:
			 return true;
		case ObjectTypeString.clsNameDouble:
			return true;
		case ObjectTypeString.clsNameShort:
			return true;
		case ObjectTypeString.clsNameString:
			return true;
		case ObjectTypeString.fieldBoolean:
			 return true;
		case ObjectTypeString.fieldByte:
			 return true;
		case ObjectTypeString.fieldDate:
			 return true;
		case ObjectTypeString.fieldLong:
			 return true;
		case ObjectTypeString.fieldInteger:
			 return true;		 
		case ObjectTypeString.fieldFloat:
			 return true;
		case ObjectTypeString.fieldDouble:
			return true;
		case ObjectTypeString.fieldShort:
			return true;
		case ObjectTypeString.fieldString:
			return true;
		case ObjectTypeString.clsNameBaseBoolean:
			return true;
		case ObjectTypeString.clsNameBaseByte:
			return true;
		case ObjectTypeString.clsNameBaseDouble:
			return true;
		case ObjectTypeString.clsNameBaseFloat:
			return true;
		case ObjectTypeString.clsNameBaseInt:
			return true;
		case ObjectTypeString.clsNameBaseLong:
			return true;
		case ObjectTypeString.clsNameBaseShort:
			return true;
		default:
			break;
		}
		return false;
	}
}
