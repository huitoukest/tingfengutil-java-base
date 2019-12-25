package com.tingfeng.util.java.base.common.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.tingfeng.util.java.base.common.bean.tuple.Tuple2;
import com.tingfeng.util.java.base.common.exception.BaseException;
import com.tingfeng.util.java.base.common.utils.reflect.ReflectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author huitoukest
 *  做一个方法，可以将一个JavaBean风格对象的属性值拷贝到另一个对象的同名属性中 (如果不存在同名属性的就不拷贝）
 * @version 20180917
**/
public class BeanUtils{
	private static final Log logger = LogFactory.getLog(BeanUtils.class);

	/** 注意：不copy类型是final或者static的属性
	 *  返回一个拷贝之后的新的数组,通过方法和属性赋值新建对象来达到赋值的目的,是浅复制;
	 * 	深度拷贝基础属性，浅拷贝数组和集合，优先通过getter和setter读取属性，如果没有getter和setter则直接读取属性
	 * @param sourceList
	 * @param targetClass
	 * @param exceptFields 需要排除的字段
	 * @param <T>
	 * @return
	 */
	public static<T> List<T> copyListProperties(List<? extends Object> sourceList,Class<T> targetClass,String...exceptFields){
		List<T> targetList = new ArrayList<T>();
		try {
			for(Object o : sourceList){
				T t = targetClass.newInstance();
				copyProperties(t, o, exceptFields);
				targetList.add(t);
			}
		} catch (Exception e) {
			throw new BaseException(e);
		}
		return targetList;
	}	
	
	/** 注意：不copy类型是final或者static的属性
	 * 浅复制普通Bean对象,,通过方法和属性赋值新建对象来达到赋值的目的;如果对象中存在集合/数组那么会执行浅复制,其它非基础数据的对象,会执行深度复制,将会自动对它们也进行深度拷贝;
	 * 如果存在setter方法,将优先使用setter和getter方法,如果不存在,那么直接使用属性操作;
	 * @param target
	 * @param source
	 * @param exceptFields 对于来源对象中的某些属性不进行拷贝
	 */
	public static void copyProperties(Object target,Object source,String...exceptFields){
		 copyProperties(target,source,null,null,exceptFields);
	}

	/** 注意：
	 *  1. 不copy类型是final或者static的属性
	 *  2. 判断和过滤的优先级如下 exceptFields > predicate > mapper ;
	 * 浅复制普通Bean对象,,通过方法和属性赋值新建对象来达到赋值的目的;如果对象中存在集合/数组那么会执行浅复制,其它非基础数据的对象,会执行深度复制,将会自动对它们也进行深度拷贝;
	 * 如果存在setter方法,将优先使用setter和getter方法,如果不存在,那么直接使用属性操作;
	 * @param target 目标对象
	 * @param source 源对象
	 * @param predicate 传入源对象的 Tuple2<字段,字段值> ; 返回是否进行拷贝true or false; null 时不生效
	 * @param mapper 传入源对象的 Tuple2<字段,字段值> ; 返回转换后的值，将使用此值拷贝到目标对象对应的字段中; null 时不生效
	 * @param exceptFields 对于来源对象中的某些属性不进行拷贝； 优先级高于predicate
	 *
	 */
	public static <T> void copyProperties(Object target, Object source, Predicate<Tuple2<Field,Object>> predicate, Function<Tuple2<Field,Object>, Object> mapper, String... exceptFields){
		Class<?> sourceClz = source.getClass();
		Class<?> targetClz = target.getClass();
		// 得到Class对象所表征的类的所有属性(包括私有属性)
		List<Field> fieldsS = ReflectUtils.getFields(sourceClz,false,false,true,true);
		List<Field> fieldsT = ReflectUtils.getFields(targetClz,false,false,true,true);
		List<Field> fieldsSourceUse = fieldsS.stream()
				.filter(field-> !ArrayUtils.isContain(exceptFields,field.getName()))
				.collect(Collectors.toList());
		Map<String,Field> targetFieldMap = fieldsT.stream()
				.map(it->{ it.setAccessible(true); return it;})
				.collect(Collectors.toMap(it -> it.getName(),it -> it, (a,b) -> b));
		try{
			fieldsSourceUse.forEach(field -> {
				field.setAccessible(true);
				String fieldName = field.getName();
				Field targetField = targetFieldMap.get(fieldName);
				if(null != targetField){
					//即使value是null也拷贝
					Object value = ReflectUtils.getFieldValue(true, source,fieldName,null,null);
					Tuple2 tuple2 = null;
					if(predicate == null || predicate.test(tuple2 = new Tuple2(field,value))){
						if(mapper != null) {
							value = mapper.apply(tuple2);
						}
						Class<?> valueClass = targetField.getType();
						if(value != null){
							valueClass = value.getClass();
						}
						ReflectUtils.setFieldValue(true, target, targetField.getName(),new Object[]{value},valueClass);
					}
				}
			});
		}catch (Exception e){
			throw new BaseException(e);
		}
	}

	/** 注意：不copy类型是final或者static的属性
	 * 浅复制普通Bean对象,,通过方法和属性赋值新建对象来达到赋值的目的;如果对象中存在集合/数组那么会执行浅复制,其它非基础数据的对象,会执行深度复制,将会自动对它们也进行深度拷贝;
	 * 如果存在setter方法,将优先使用setter和getter方法,如果不存在,那么直接使用属性操作;
	 * @param target
	 * @param source
	 */
	public static void copyProperties(Object target,Object source){
		String[] args = null;
		copyProperties(target,source,args);
	}

	/**
	 * 通过遍历Bean属性的方式来初始化map,适用于bean属性很少的情况;
	 * map中的key支持a.b.c的方式
	 * 注意:这种方式不支持自我调用的bean(即a中存在a的引用的情况)的自动赋值;
	 * @param cls 目标类型
	 * @param map map中保存的是当前对象的属性和值得键值对
	 */
	public static <T> T getBeanByMap(Class<T> cls,Map<String,?> map){
		try {
			T t = cls.newInstance();
			return getBeanByMap(t, map);
		}catch (Exception e){
			throw new BaseException(e);
		}
	}
	/**
	 * 通过遍历Bean属性的方式来初始化map,适用于bean属性很少的情况;
	 * map中的key支持a.b.c的方式
	 * 注意:这种方式不支持自我调用的bean(即a中存在a的引用的情况)的自动赋值;
	 * @param t 目标对象
	 * @param map map中保存的是当前对象的属性和值得键值对
	 */
	public static <T> T getBeanByMap(T t,Map<String,?> map){
		Set<String> keySet = map.keySet();
		for(String fieldName : keySet){
			Object object = map.get(fieldName);
			try{
				Class<?> typeClass = ReflectUtils.getTypeByFieldName(t.getClass(),fieldName);
				Object valueObject = ObjectUtils.getObject(typeClass, object);
				if(valueObject == null) {
					continue;
				}
				ReflectUtils.setFieldValue(true,t, fieldName,new Object[]{valueObject},valueObject.getClass());
			}catch(Exception e){
				if(logger.isDebugEnabled()){
					logger.debug("getBeanByMap error:",e);
				}
				continue;
			}
		}
		return t;
	}

	/**
	 * 封装系统的Introspector#getBeanInfo，默认带有缓存
	 * @param cls
	 * @param <T>
	 * @return
	 */
	public static <T> BeanInfo getBeanInfo(Class<T> cls) {
		return ObjectUtils.getValue(null,() -> Introspector.getBeanInfo(cls));
	}
}
