package mint.mvc.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Factory for all converters(add string support).
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LW
 */
public class ConverterFactory {
	private Logger log = Logger.getLogger(ConverterFactory.class.getName());

	private Map<Class<?>, Converter<?>> map = new HashMap<Class<?>, Converter<?>>();

	public ConverterFactory() {
		loadInternal();
	}

	void loadInternal() {
		Converter<?> c = null;

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
			loadExternalConverter(Class.forName(typeClass), (Converter<?>) Class.forName(converterClass).newInstance());
		} catch (Exception e) {
			log.warning("Cannot load converter '" + converterClass + "' for type '" + typeClass + "'.");
			e.printStackTrace();
		}
	}

	public void loadExternalConverter(Class<?> clazz, Converter<?> converter) {
		if (clazz == null)
			throw new NullPointerException("Class is null.");
		if (converter == null)
			throw new NullPointerException("Converter is null.");
		if (map.containsKey(clazz)) {
			log.warning("Cannot replace the exist converter for type '"
					+ clazz.getName() + "'.");
			return;
		}
		map.put(clazz, converter);
	}

	public boolean canConvert(Class<?> clazz) {
		return clazz.equals(String.class) || map.containsKey(clazz);
	}

	public Object convert(Class<?> clazz, String s) {
		if (clazz.equals(String.class)) {
			return s;
		}
		Converter<?> c = map.get(clazz);
		try{
			return c.convert(s);
		} catch (Exception e){
			return null;
		}
	}
}
