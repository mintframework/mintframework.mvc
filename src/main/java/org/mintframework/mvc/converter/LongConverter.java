package org.mintframework.mvc.converter;

/**
 * Convert String to Long.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class LongConverter implements ParameterConverter<Long> {

	public Long convert(String s) {
		if("".equals(s.trim())){
			return null;
		}
		try {
			return Long.parseLong(s);
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	public boolean canConvert(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

}
