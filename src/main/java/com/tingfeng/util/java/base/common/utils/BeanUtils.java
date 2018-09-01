package com.tingfeng.util.java.base.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.tingfeng.util.java.base.common.utils.reflect.ReflectUtils;
import com.tingfeng.util.java.base.common.utils.string.StringUtils;
/**
 * @author Bear
 *  做一个方法，可以将一个JavaBean风格对象的属性值拷贝到另一个对象的同名属性中 (如果不存在同名属性的就不拷贝）
 * @version 20160517
**/
@SuppressWarnings("unchecked")
public class BeanUtils{
	/**
	  *  深度复制一个对象,即同时赋值对象内部的基础数据类型和引用类型,通过对对象的二进制流读写达到此效果
	  * @return
	  * @throws IOException
	  * @throws ClassNotFoundException
	  */
	public static <T> T deepClone(T t) throws IOException, ClassNotFoundException {  
  
        /* 写入当前对象的二进制流 */  
        ByteArrayOutputStream bos = new ByteArrayOutputStream();  
        ObjectOutputStream oos = new ObjectOutputStream(bos);  
        oos.writeObject(t);    
        /* 读出二进制流产生的新对象 */
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());  
        ObjectInputStream ois = new ObjectInputStream(bis);  
        return (T) ois.readObject();  
    }	
	/**
	 * 返回一个拷贝之后的新的数组,通过方法和属性赋值新建对象来达到赋值的目的,是浅复制;
	 * 深度拷贝基础属性，浅拷贝数组和集合
	 * @param sourceList
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	public static<T> List<T> middleCopyListByField(List<? extends Object> sourceList,Class<T> tagetClass,String...exceptionFileds) throws SecurityException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException{
		List<Object> tagetList=new ArrayList<Object>();
		for(Object o:sourceList){
		   try {
			Object t=tagetClass.newInstance();
			middleCopyBean(t, o, exceptionFileds);
			tagetList.add(t);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	   }
		return (List<T>) tagetList;
	}	
	
	/**
	 * 浅复制普通Bean对象,,通过方法和属性赋值新建对象来达到赋值的目的;如果对象中存在集合/数组那么会执行浅复制,其它非基础数据的对象,会执行深度复制,将会自动对它们也进行深度拷贝;
	 * 如果存在setter方法,将优先使用setter和getter方法,如果不存在,那么直接使用属性操作;
	 * @param target
	 * @param source
	 * @param exceptionFileds 对于来源对象中的某些属性不进行拷贝
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 * @throws InstantiationException 
	 * @throws NoSuchFieldException 
	 */
	public static void middleCopyBean(Object target,Object source,String...exceptionFileds) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException{
		Class<? extends Object> sourceClz = source.getClass();
		Class<? extends Object> targetClz = target.getClass();
		// 得到Class对象所表征的类的所有属性(包括私有属性)
		Field[] fields_s = sourceClz.getDeclaredFields();
        Field[] fields_t=targetClz.getDeclaredFields();        
		if (fields_s.length == 0) {
			fields_s = sourceClz.getSuperclass().getDeclaredFields();
		}
		if (fields_t.length == 0) {
			fields_t = targetClz.getSuperclass().getDeclaredFields();
		}
					
		for (int i = 0; i < fields_s.length; i++) {
			try{
				String fieldName = fields_s[i].getName();
				if(ArrayUtils.isExitedInArray(fieldName, exceptionFileds))
					continue;
				
				Field targetField =null;
				// 得到targetClz对象所表征的类的名为fieldName的属性，不存在就进入下次循环
				try {
					targetField = ReflectUtils.getField(targetClz, fieldName);		
				} catch (Exception e) {
					continue;				
				}
				fields_s[i].setAccessible(true);    
				Object fieldValueObject=ReflectUtils.getFieldValue(true, source,fields_s[i].getName(),null);
				if(fieldValueObject==null)
					continue;
				if(!isBaseDataType(fieldValueObject)){
					Class<?> cls=targetField.getType();
					Object tagetFieldObject=cls.newInstance();
						middleCopyBean(tagetFieldObject,fieldValueObject);		
					fieldValueObject=tagetFieldObject;
				}				
				ReflectUtils.setFieldValue(true, target, targetField.getName(),new Object[]{fieldValueObject},fieldValueObject.getClass());
			}catch(Exception e){
				//e.printStackTrace();
				continue;
			}
			
						 
		}
	}
	
	/**
	 * 判断是否是基础数据类型,包括Boolean,Byte,Long,Integer,String,Date,Float,Double,Short,数组和集合
	 * @return
	 */
	public static boolean isBaseDataType(Object obj){
		if(obj instanceof Object[])
		{
			return true;
		}else if(obj instanceof Collection){
			return true;
		}else if(obj instanceof Boolean){
			return true;
		}else if(obj instanceof Byte){
			return true;
		}else if(obj instanceof Short){
			return true;
		}else if(obj instanceof Long){
			return true;
		}else if(obj instanceof Integer){
			return true;
		}else if(obj instanceof Float){
			return true;
		}else if(obj instanceof Double){
			return true;
		}else if(obj instanceof Date){
			return true;
		}else if(obj instanceof String){
			return true;
		}
		return false;
	}
	
	/**
     * 根据属性的名称和类型来拷贝,即只拷贝名称和类型都相同的属性的值
     * @param target
     * @param source
	 * @throws InstantiationException 
     * @throws Exception
     */
	public static void middleCopyBeanByField(Object target, Object source) throws InstantiationException{
			Class<? extends Object> sourceClz = source.getClass();
			Class<? extends Object> targetClz = target.getClass();

			// 得到Class对象所表征的类的所有属性(包括私有属性)
			Field[] fields_s = sourceClz.getDeclaredFields();
            Field[] fields_t=targetClz.getDeclaredFields();                      
            
			if (fields_s.length == 0) {
				fields_s = sourceClz.getSuperclass().getDeclaredFields();
			}
			if (fields_t.length == 0) {
				fields_t = targetClz.getSuperclass().getDeclaredFields();
			}
					
			for (int i = 0; i < fields_s.length; i++) {

				String fieldName = fields_s[i].getName();
				Field targetField =null;
				// 得到targetClz对象所表征的类的名为fieldName的属性，不存在就进入下次循环
				try {
					targetField = targetClz.getDeclaredField(fieldName);
				} catch (NoSuchFieldException e) {
					try {
						targetField = targetClz.getSuperclass().getDeclaredField(
								fieldName);
					} catch (NoSuchFieldException e1) {						
						e1.printStackTrace();
						continue;
					}
				}
				targetField.setAccessible(true);
				fields_s[i].setAccessible(true);
				if (fields_s[i].getType() == targetField.getType()) {
                    try {
						Object value=fields_s[i].get(source);
						if(!isBaseDataType(value)){
							Object taget=value.getClass().newInstance();
							middleCopyBeanByField(taget,value);
							value=taget;
						}
                    	targetField.set(target,value);
						
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}       
				   }				 
			}
		}
	
    /**
     * 根据属性所使用的getter和setter方法的名称和类型来拷贝
     * @param target
     * @param source
     * @throws Exception
     */
	public static void middleCopyBeanByMethod(Object target, Object source) throws Exception {
			/*
			 * 分别获得源对象和目标对象的Class类型对象,Class对象是整个反射机制的源头和灵魂！
			 * 
			 * Class对象是在类加载的时候产生,保存着类的相关属性，构造器，方法等信息
			 */
			Class<? extends Object> sourceClz = source.getClass();
			Class<? extends Object> targetClz = target.getClass();
			// 得到Class对象所表征的类的所有属性(包括私有属性)
			Field[] fields = sourceClz.getDeclaredFields();
			if (fields.length == 0) {
				fields = sourceClz.getSuperclass().getDeclaredFields();
			}
			for (int i = 0; i < fields.length; i++) {
				String fieldName = fields[i].getName();
				Field targetField = null;
				// 得到targetClz对象所表征的类的名为fieldName的属性，不存在就进入下次循环
				try {
					targetField = targetClz.getDeclaredField(fieldName);
				} catch (NoSuchFieldException e) {
					targetField = targetClz.getSuperclass().getDeclaredField(
							fieldName);
				}
				// 判断sourceClz字段类型和targetClz同名字段类型是否相同
				if (fields[i].getType() == targetField.getType()) {
					// 由属性名字得到对应get和set方法的名字
					String getMethodName = "get"
							+ fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH)
							+ fieldName.substring(1);
					String setMethodName = "set"
							+ fieldName.substring(0, 1).toUpperCase(Locale.ENGLISH)
							+ fieldName.substring(1);
					// 由方法的名字得到get和set方法的Method对象
					Method getMethod;
					Method setMethod;
					try {
						try {
							getMethod = sourceClz.getDeclaredMethod(getMethodName,
									new Class[] {});
						} catch (NoSuchMethodException e) {
							getMethod = sourceClz.getSuperclass()
									.getDeclaredMethod(getMethodName,
											new Class[] {});
						}
						try {
							setMethod = targetClz.getDeclaredMethod(setMethodName,
									fields[i].getType());
						} catch (NoSuchMethodException e) {
							setMethod = targetClz.getSuperclass()
									.getDeclaredMethod(setMethodName,
											fields[i].getType());
						}
						// 调用source对象的getMethod方法
						Object result = getMethod.invoke(source, new Object[] {});						
						if(!isBaseDataType(result)){
							Object taget=result.getClass().newInstance();
							middleCopyBeanByMethod(taget,result);
							result=taget;
						}								
						// 调用target对象的setMethod方法
						setMethod.invoke(target, result);
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				} else {
					throw new Exception("同名属性类型不匹配！");
				}
			}
		}

		public static String firstLetterToLower(String srcString) {
			StringBuilder sb = new StringBuilder();
			sb.append(Character.toLowerCase(srcString.charAt(0)));
			sb.append(srcString.substring(1));
			return sb.toString();
		}
		public static <T> T initBeanFromMap(Class<T> cls,Map<String,?> map) throws InstantiationException, IllegalAccessException{
			T t=cls.newInstance();
			return initBeanFromMap(t, map);
		}
		
		public static <T> T initBeanFromMap(T t,Map<String,?> map) throws InstantiationException, IllegalAccessException{
			//return initBeanFromMapByTraverseBean(t, map);
			return initBeanFromMapByTraverseMap(t, "", map);
		}
		/**
		 * 通过遍历Bean属性的方式来初始化map,适用于bean属性很少的情况;
		 * map中的key支持a.b.c的方式
		 * 注意:这种方式不支持自我调用的bean(即a中存在a的引用的情况)的自动赋值;
		 */
		public static <T> T initBeanFromMapByTraverseBean(T t,Map<String,?> map){
			Set<String> keySet=map.keySet(); 
			for(String s:keySet){
				Object object=map.get(s);
				try{
					Class<?> typeClass=ReflectUtils.getTypeByFieldName(t.getClass(),s);
					Object valueObject= ObjectUtils.getObject(typeClass, object);
					if(valueObject==null) continue;
					ReflectUtils.setFieldValue(true,t, s,new Object[]{valueObject},valueObject.getClass());
				}catch(Exception e){
					continue;
				}
			}
			return t;		
		}
		/**
		 * 通过遍历Map键的方式来初始化Bean的属性,适用于大部分的情况;
		 * map中的key支持a.b.c的方式;如果存在异常,会默认跳过;
		 * 仅仅支持基础数据和包装类型的赋值;
		 * 不支持集合/数组方式赋值;map中的值如果是字符串,则会更具属性的类型来进行自动的转换;
		 * @throws IllegalAccessException 
		 * @throws InstantiationException 
		 */
		public static <T> T initBeanFromMapByTraverseMap(Class<T> cls,Map<String,?> map) throws InstantiationException, IllegalAccessException{
			return initBeanFromMapByTraverseMap(cls.newInstance(), "", map);
		}
		/**
		 * 通过遍历Map键的方式来初始化Bean的属性,适用于大部分的情况;
		 * map中的key支持a.b.c的方式;如果存在异常,会默认跳过;
		 * 仅仅支持基础数据和包装类型的赋值;
		 * 不支持集合/数组方式赋值;map中的值如果是字符串,则会更具属性的类型来进行自动的转换;
		 */
		public static <T> T initBeanFromMapByTraverseMap(T t,Map<String,?> map){
			return initBeanFromMapByTraverseMap(t, "", map);
		}
		/**
		 * 通过遍历Map键的方式来初始化Bean的属性,适用于大部分的情况;
		 * map中的key支持a.b.c的方式;如果存在异常,会默认跳过;
		 * @param prefix map中键的前缀,默认为"",一般前缀是"a."来表示此属性map中值为类a的属性值;
		 * 仅仅支持基础数据和包装类型的赋值;
		 * 不支持集合/数组方式赋值;map中的值如果是字符串,则会更具属性的类型来进行自动的转换;
		 */
		public static <T> T initBeanFromMapByTraverseMap(T t,String prefix,Map<String,?> map){
			Set<String> keySet=map.keySet();
			Field[] fields=t.getClass().getDeclaredFields();
			//利用map来提速
			Map<String, Field> fieldMap=new HashMap<String, Field>();
			for(Field f:fields){
				f.setAccessible(true);
				fieldMap.put(f.getName(), f);
			}
			for(String s:keySet){
				try {							
					String[] names=StringUtils.getFieldNames(prefix, s);
					String nameOne=names[0];
					String nameTwo=names[1];
					if(nameOne==null) 
						continue;
					Field f=fieldMap.get(nameOne);
						if(f!=null){//如果存在此属性		
							boolean isBaseType= ReflectUtils.isJavaBaseDataField(f);
							if(!isBaseType&&nameTwo!=null){
								//如果此属性是一个引用类;
								Object object=f.get(t);
								if(object==null){
									object=f.getType().newInstance();
								}
								Map<String,Object> nextMap=new HashMap<String,Object>(1);
									nextMap.put(s,map.get(s));
								initBeanFromMapByTraverseMap(object,prefix+nameOne+".",nextMap);
								ReflectUtils.setFieldValue(true, t,nameOne,new Object[]{object},object.getClass());
							}else if(isBaseType&&nameTwo==null){
								  //如果是一个基础属性;
								 Object value=map.get(prefix+nameOne);
								 Object object=ObjectUtils.getObject(f.getType(),value);
								 //f.set(t, object);
								 ReflectUtils.setFieldValue(true, t,nameOne,new Object[]{object},f.getType());
							}					
						 }
					
				} catch (Exception e) {
					continue;
				}
			}
			return t;			
		}
}
