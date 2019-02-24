package org.mintframework.mvc.core;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mintframework.mvc.converter.ParameterConverterFactory;

/**
 * 参数注射器，负责把前台参数注射入对应的对象内部
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:09:34 
 *
 */
class ParameterInjector {
	private final static ParameterConverterFactory converterFactory = new ParameterConverterFactory();
	
	/**
	 * parameter's index in action method's parameters.
	 */
	final int 						argIndex;
	final Class<?> 					argType;
	final String					argName;
	final Map<String ,SetterInfo> 	settersMap  = new HashMap<String ,SetterInfo>();
	
	/*
	 * 基础类型和String 类型和数组不需要注射
	 */
	final boolean 					needInject;
	final boolean					isArray;
	
	final Class<?> 					mapKeyClass;
	final Class<?>					mapValueClass;
	
	boolean isEnum = false;
	List<Integer> enumOrdinals;
	List<String> enumNames;
	
	
	/**
	 * @param argIndex 参数的索引
	 * @param argType 参数的类型
	 * @param argName 参数的名字
	 * @param isMapType 是否Map类型参数
	 * @param mapKeyClass 泛型第一个参数
	 * @param mapValueClass 泛型第二个参数
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	ParameterInjector(int argIndex, Class<?> argType, String argName, boolean isMapType, Class<?> mapKeyClass, Class<?> mapValueClass){
		this.argIndex = argIndex;
		this.argType = argType;
		this.argName = argName;
		
		this.mapKeyClass = mapKeyClass;
		this.mapValueClass = mapValueClass;
		
		isArray = argType.isArray();
		
		if(argType.isPrimitive() || argType.equals(String.class) || isArray){
			needInject = false;
		} else if(isMapType){
			needInject = false;
		} else if(argType.isEnum()){
			needInject = false;
			enumOrdinals = new ArrayList<>();
			enumNames = new ArrayList<>();
			isEnum = true;
			
			Enum<?> es[] = ((Class<? extends Enum>)argType).getEnumConstants();
			if(es!=null){
				for(Enum<?> e : es){
					enumOrdinals.add(e.ordinal());
					enumNames.add(e.name());
				}
			}
		} else {
			boolean result;
			try {
				result = !((Class<?>)argType.getField("TYPE").get(null)).isPrimitive();
			} catch (Exception e) {
				result = true;
			}
			
			needInject = result;
		}
		
		initSetters();
	}
	
	/**
	 * 将请求参数注射入action参数Bean对象中
	 * @param instance
	 * @param value
	 * @param key the key for access setter method
	 * @return 
	 */
	void injectBean(Object instance, String value, String key){
		SetterInfo setterInfo = settersMap.get(key);
		
		if(setterInfo.isSetter){
			try {
				setterInfo.setter.invoke(instance, converterFactory.convert(setterInfo.fieldType, value));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			try {
				setterInfo.field.set(instance, converterFactory.convert(setterInfo.fieldType, value));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 将请求参数注射入action参数Bean对象中
	 * @param <T>
	 * @param <T>
	 * @param instance
	 * @param value
	 * @param key the key for access setter method
	 * @return 
	 */
	void injectMap(Map<Object, Object> instance, String key, String value){		
		instance.put(converterFactory.convert(this.mapKeyClass, key), converterFactory.convert(this.mapValueClass, value));
	}
	
	/**
	 * @return keys to access current injector
	 */
	Set<String> getKeys(){
		return settersMap.keySet();
	}
	
	/**
	 * 从action参数中分离出请求参数名和对象字段的对应关系
	 * 使用内省获取getter和setter方法
	 */
	private void initSetters(){
		if(needInject){
			ParameterConverterFactory converter = new ParameterConverterFactory();
			
			try {
				/*内省方式获取属性和setter*/
				PropertyDescriptor[] props = Introspector.getBeanInfo(argType, Object.class).getPropertyDescriptors();
				Method setter;
				Class<?> type;
				SetterInfo sInfo;
				for(PropertyDescriptor pd : props){
					type = pd.getPropertyType();
					
					if(converter.canConvert(type)){
						setter = pd.getWriteMethod();
						/*取消虚拟机安全检查，提高方法调用效率*/
						if(setter!=null){
							setter.setAccessible(true);
							
							sInfo = new SetterInfo(setter, type, null, true);
							settersMap.put(argName+"."+pd.getName(), sInfo);
							settersMap.put(argName+"["+pd.getName()+"]", sInfo);
							settersMap.put(argName+"['"+pd.getName()+"']", sInfo);
							settersMap.put(argName+"[\""+pd.getName()+"\"]", sInfo);
						}
					}
				}
			} catch (IntrospectionException e) {
				e.printStackTrace();
			}
			
			//反射获取属性，非final,static,private属性也可以注入
			for(Field f : argType.getFields()){
				if(Modifier.isFinal(f.getModifiers()) || Modifier.isStatic(f.getModifiers()) || Modifier.isPrivate(f.getModifiers())) continue;
				
				if(settersMap.get(argName+"."+f.getName())!=null) continue;
				
				if(converter.canConvert(f.getType())){
					f.setAccessible(true);
					settersMap.put(argName+"."+f.getName(),  new SetterInfo(null, f.getType(), f, false));
				}
			}
			
			
			/*把参数本身当成一个可解析项*/
			settersMap.put(argName, null);
		} else {
			settersMap.put(argName, null);
			if(isArray){
				settersMap.put(argName+"[]", null);
			}
		}
	}
}

/**
 * @author LW
 * 暂存setter的信息
 */
class SetterInfo {
	public final Method 	setter;
	public final Class<?>	fieldType;
	public final Boolean 	isSetter;
	public final Field		field;
	
	SetterInfo(Method setter, Class<?> fieldType, Field field, Boolean isSetter){
		this.setter 	= setter;
		this.fieldType 	= fieldType;
		this.isSetter = isSetter;
		this.field = field;
	}
}
