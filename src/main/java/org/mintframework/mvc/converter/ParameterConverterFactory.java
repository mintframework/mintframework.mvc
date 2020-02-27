package org.mintframework.mvc.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Factory for all converters(add string support).
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LW
 */
public class ParameterConverterFactory {
	private Logger log = Logger.getLogger(ParameterConverterFactory.class.getName());

	private static Map<Class<?>, ParameterConverter<?>> map = new HashMap<Class<?>, ParameterConverter<?>>();
	private static ParameterConverter<?> defaultConverter = null;

	public ParameterConverterFactory() {
		loadInternal();
	}

	private static void loadInternal() {
		ParameterConverter<?> c = null;

		c = new BooleanConverter();
		map.put(boolean.class, c);
		map.put(Boolean.class, c);

		c = new CharacterConverter();
		map.put(char.class, c);
		map.put(Character.class, c);

		c = new ByteConverter();
		map.put(byte.class, c);
		map.put(Byte.class, c);

		c = new ShortConverter();
		map.put(short.class, c);
		map.put(Short.class, c);

		c = new IntegerConverter();
		map.put(int.class, c);
		map.put(Integer.class, c);

		c = new LongConverter();
		map.put(long.class, c);
		map.put(Long.class, c);

		c = new FloatConverter();
		map.put(float.class, c);
		map.put(Float.class, c);

		c = new DoubleConverter();
		map.put(double.class, c);
		map.put(Double.class, c);
		
		c = new BigDecimalConverter();
		map.put(BigDecimal.class, c);
		
		c = new BigIntegerConverter();
		map.put(BigInteger.class, c);
	}

	public void loadExternalConverter(String typeClass, String converterClass) {
		try {
			loadExternalConverter(Class.forName(typeClass), (ParameterConverter<?>) Class.forName(converterClass).getDeclaredConstructor().newInstance());
		} catch (Exception e) {
			log.warning("Cannot load converter '" + converterClass + "' for type '" + typeClass + "'.");
			e.printStackTrace();
		}
	}

	public void loadExternalConverter(Class<?> targetClazz, ParameterConverter<?> converter) {
		if (targetClazz == null)
			throw new NullPointerException("converter Class is null.");
		if (converter == null)
			throw new NullPointerException("Converter is null.");
		if (map.containsKey(targetClazz)) {
			log.warning("Cannot replace the exist converter for type '"
					+ targetClazz.getName() + "'.");
			return;
		}
		map.put(targetClazz, converter);
	}

	public boolean canConvert(Class<?> clazz) {
		return clazz.equals(String.class) || clazz.isEnum() || map.containsKey(clazz) || (defaultConverter!=null && defaultConverter.canConvert(clazz));
	}

	public Object convert(Class<?> clazz, String s) {
		//字符串直接返回
		if (clazz.equals(String.class)) {
			return s;
		}
		
		//枚举类型
		if(clazz.isEnum()) {
			return initEnum(clazz, s);
		} else { //其他类型
			ParameterConverter<?> c = map.get(clazz);
			if(c != null) {
				try{
					return c.convert(s);
				} catch (Exception e){
					e.printStackTrace();
					return null;
				}
			} else if(defaultConverter!=null){
				try{
					return defaultConverter.convert(s);
				} catch (Exception e){
					e.printStackTrace();
					return null;
				}
			}
		}
		
		
		return null;
	}
	
	private static Map<Class<?>, List<Integer>> enumOrdinalsMap = new HashMap<Class<?>, List<Integer>>();
	private static Map<Class<?>, List<String>> enumNamesMap = new HashMap<Class<?>, List<String>>();
	private final Pattern enumValuePattern = Pattern.compile("^\\d+$");
	/**
	 * 转换枚举参数
	 * @param argType
	 * @param value
	 * @param enumNames
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Enum<?> initEnum(Class<?> argType, String value){
		List<Integer> enumOrdinals = enumOrdinalsMap.get(argType);
		List<String> enumNames = null;
		
		if(enumOrdinals == null) {
			enumOrdinals = new ArrayList<>();
			enumNames = new ArrayList<>();
			Enum<?> es[] = ((Class<? extends Enum>)argType).getEnumConstants();
			if(es!=null){
				for(Enum<?> e : es){
					enumOrdinals.add(e.ordinal());
					enumNames.add(e.name());
				}
			}
			enumOrdinalsMap.put(argType, enumOrdinals);
			enumNamesMap.put(argType, enumNames);
		} else {
			enumNames = enumNamesMap.get(argType);
		}
		
		//索引方式初始化枚举
		if(enumValuePattern.matcher(value).matches()){
			int val = Integer.valueOf(value);
			if(enumOrdinals.indexOf(val)>-1){
				value = enumNames.get(val);
				return Enum.valueOf((Class<? extends Enum>)argType, value);
			} else if(enumNames.indexOf(value) > -1) {
				return Enum.valueOf((Class<? extends Enum>)argType, value);
			}
		} else if(enumNames.indexOf(value) > -1){ //字符串方式初始化枚举
			return Enum.valueOf((Class<? extends Enum>)argType, value);
		}
		
		return null;
	}
	
	public void setDefaultConverter(ParameterConverter<?> converter) {
		defaultConverter = converter;
	}
	
	public ParameterConverter<?> getDefaultConverter() {
		return defaultConverter;
	}
}
