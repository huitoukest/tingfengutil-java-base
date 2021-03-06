package com.tingfeng.util.java.base.common.utils.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author huitoukest
 * java反射的一些工具方法;
 */
public class ReflectUtils {
	
	/**
	 * 给属性设置值,会先尝试调用其setter方法,如果没有setter方法会直接给属性赋值
	 * 支持a.b.c的链式调用取值;
	 * @param isReadNotPublicField 如果Field属性不是public,那么直接赋值可能会失败，设置是否读取非public的属性
	 * @param obj 此属性的对象实例
	 * @param filedName 属性的名称
	 * @param values 参数的值
	 * @param parameterTypes 参数类型
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws InstantiationException 
	 */
	public static void setFieldValue(boolean isReadNotPublicField,Object obj,String filedName,Object[] values,Class<?>...parameterTypes) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, InstantiationException{
		if(obj==null){
			return;
		}
		if(filedName.indexOf(".")>0&&!filedName.endsWith(".")){
			String[] fieldNameStrings=filedName.split("\\.");
			String nextString=filedName.substring(fieldNameStrings[0].length()+1);
			Object objTemp=getFieldValue(isReadNotPublicField, obj, fieldNameStrings[0],null);
			Class<?> clsTemp=null;
			if(objTemp==null){
				try {
					Field field=obj.getClass().getDeclaredField(fieldNameStrings[0]);
					clsTemp=field.getType();
					objTemp=clsTemp.newInstance();
					setFieldValue(isReadNotPublicField, objTemp, nextString, values, parameterTypes);
					setFieldValue(true, obj, fieldNameStrings[0],new Object[]{objTemp}, objTemp.getClass());
				}catch (Exception e) {
					   System.out.println(e.toString());
				}
			}else{
				setFieldValue(isReadNotPublicField, objTemp, nextString, values, parameterTypes);
			}			
			
		}else{
			Field field=null;
			try{
				field=obj.getClass().getDeclaredField(filedName);
			}catch(java.lang.NoSuchFieldException e){
				return;
			}
			try {
				Method method = obj.getClass().getMethod("set"+ initStr(field.getName()),parameterTypes);
				method.invoke(obj, values);
			} catch (NoSuchMethodException e) {
				Method method;
				try {
					method = obj.getClass().getSuperclass().getMethod("set"+ initStr(field.getName()),parameterTypes);
					method.invoke(obj, values);			   
				} catch (java.lang.NoSuchMethodException e1) {
					if(isReadNotPublicField)
						field.setAccessible(true);
					try {
						field.set(obj, values[0]);
					} catch (Exception e2) {
						System.out.println(e2.toString());
					}  
					
				}		
			}
		}
	}
	/**
	 * 在此类,和其超类中寻找此属性
	 * @param cls
	 * @param fieldName
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	public static Field getField(Class<?> cls,String fieldName) throws NoSuchFieldException, SecurityException{	
		try {
		return	cls.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
				return cls.getSuperclass().getDeclaredField(fieldName);
		} 
	}
	
	/**
	 * 取属性值,会先尝试调用其getter方法,如果没有getter方法会直接给属性赋值
	 * @param obj 此属性的对象实例
	 * @param values 参数的值
	 * @param parameterTypes 参数类型
	 * @return 如果没有找到属性会返回null;
	 */
	public static Object getFieldValue(boolean isReadNotPublicField,Object obj,String filedName,Object[] values,Class<?>...parameterTypes) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException{
		if(obj==null){
			return null;
		}
		if(filedName.indexOf(".")>0&&!filedName.endsWith(".")){
			String[] fieldNameStrings=filedName.split("\\.");
			Object objTemp=getFieldValue(isReadNotPublicField, obj, fieldNameStrings[0],null);
			return getFieldValue(isReadNotPublicField, objTemp, fieldNameStrings[1], values, parameterTypes);
		}else{
			Field field=null;
			try{
				field=obj.getClass().getDeclaredField(filedName);
			}catch(java.lang.NoSuchFieldException e){
				return null;
			}
			try {
				Method method = obj.getClass().getMethod("get"+ initStr(field.getName()),parameterTypes);// 此方法不需要参数，如：getName(),getAge()		
				return method.invoke(obj);
			} catch (NoSuchMethodException e) {
			    Method method;
				try {
					method = obj.getClass().getSuperclass().getMethod("get"+ initStr(field.getName()), parameterTypes);
					return method.invoke(obj, values);
				} catch (NoSuchMethodException e1) {
					if(isReadNotPublicField)
						field.setAccessible(true);
					return field.get(obj);				 
				}			
			}
		}
	}
	
	 /**
     * 通过反射调用指定getter方法,成功返回true,否则返回false;
     * @param obj
     * @param attr 属性名称,如name
     * @param value
     * @param type
     * @return
     */
	public static boolean setter(Object obj, String attr, Object value,Class<?> type) {
	try {
			// 第一个参数表示方法名称，setAge、setName,第二个参数表示类型，如int.class,String.class
			Method method = obj.getClass().getMethod("set"+ initStr(attr),type);
			if(method==null)
				return false;
			method.invoke(obj, value);// 调用方法
			return true;
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
	}

    /**
     * 通过反射调用指定getter方法,成功返回相应的值,否则返回null;
     * @param obj
     * @param attr 属性名称,如name
     * @return
     */
	public static Object getter(Object obj, String attr) {// 调用getter方法
		try {
			Method method = obj.getClass().getMethod("get"+ initStr(attr));// 此方法不需要参数，如：getName(),getAge()
			if(method!=null)
				return method.invoke(obj);		
		} catch (Exception e) {
			//e.printStackTrace();		
		}
		return null;
	}

	protected static String initStr(String old) {// 单词首字母大写
		String str = old.substring(0, 1).toUpperCase() + old.substring(1);
		return str;
	}
	
	/**
	 * 根据实体得到实体的所有属性
	 * @param objClass
	 * @return
	 * @throws ClassNotFoundException
	 */
		public static String[] getFieldNames(String objClass) throws ClassNotFoundException {
			String[] wageStrArray = null;
			if (objClass != null) {
				Class<?> class1 = Class.forName(objClass);
				Field[] field = class1.getDeclaredFields();// 这里便是获得实体Bean中所有属性的方法
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < field.length; i++) {// 这里不多说了

					sb.append(field[i].getName());

					// 这是分割符 是为了去掉最后那个逗号

					// 比如 如果不去最后那个逗号 最后打印出来的结果是 "id,name,"

					// 去了以后打印出来的是 "id,name"
					if (i < field.length - 1) {
						sb.append(",");

					}
				}
				// split(",");这是根据逗号来切割字符串使字符串变成一个数组
				wageStrArray = sb.toString().split(",");
				return wageStrArray;
			} else {
				return wageStrArray;
			}
		}
		/**
		 * 将属性的值转换为一个数组
		 * @param f
		 * @param o
		 * @return
		 * @throws Exception
		 */
		public static Object[] fieldToValue(Field[] f, Object o) throws Exception {
			Object[] value = new Object[f.length];
			for (int i = 0; i < f.length; i++) {
				value[i] = f[i].get(o);
			}
			return value;
		}
		
	    /**
		 * 得到实体类
		 * @param objClass 实体类包含包名
		 * @return
		 */
		 public static  Class<?> getObjectClass(String objClass){
			 Class<?> entityClass = null;
			try {
				entityClass = Class.forName(objClass);
			} catch (ClassNotFoundException e) {			
				e.printStackTrace();
			} 
			 return entityClass;
		 }  

			/**
			 * 得到除开指定名称的属性列 
			 */
			public static List<String> getFieldNames(Class<?> cls,String... exceptCoulumns){
				List<String> nameList=getBeanColumnNameList(cls);
				if(exceptCoulumns!=null){
					for(String s:exceptCoulumns){
						nameList.remove(s);
					}
				}
				return nameList;
			}
			
			/**
			 * 得到除开指定名称的属性列
			 */
			public static List<String> getFieldNames(Class<?> cls,List<String> exceptCoulumns){
				List<String> nameList=getBeanColumnNameList(cls);
				if(exceptCoulumns!=null){
					for(String s:exceptCoulumns){
						nameList.remove(s);
					}
				}
				return nameList;
			}
			 /**
			 * 返回此类的列的属性名称,不包含静态属性
			 * @param cls
			 * @return
			 */
			private static List<String> getBeanColumnNameList(Class<?> cls){
				   List<String> list=new ArrayList<String>();
				   Class<?> clazz=cls;
				   Field[] fs=clazz.getDeclaredFields();		
				   for(Field field:fs){
					   boolean isStatic = Modifier.isStatic(field.getModifiers());
					   if(isStatic)
						   continue;
					   field.setAccessible(true);
					   list.add(field.getName());				   
				   }
				   return list;
			}

	/**
	 * 将首字母小写
	 * @param srcString
	 * @return
	 */
	public static String firstLetterToLower(String srcString) {
				StringBuilder sb = new StringBuilder();
				sb.append(Character.toLowerCase(srcString.charAt(0)));
				sb.append(srcString.substring(1));
				return sb.toString();
			}
			/**
			 * 
			 * @param cls
			 * @param filedName 支持a.b.c的方式
			 * @return 返回此类中此名称的属性的类型,如果不存在则返回null
			 */
			public static Class<?> getTypeByFieldName(Class<?> cls,String filedName){
				try{
				if(filedName.indexOf(".")>0&&!filedName.endsWith(".")){
					String[] fieldNameStrings=filedName.split("\\.");
					String nextString=filedName.substring(filedName.indexOf(fieldNameStrings[1]));
					Field field=cls.getDeclaredField(fieldNameStrings[0]);					
					return getTypeByFieldName(field.getType(), nextString);
				}else{
					Field field=cls.getDeclaredField(filedName);
					return field.getType();
				}
			 }catch(Exception e){
				return null;
			 }
			}
}
