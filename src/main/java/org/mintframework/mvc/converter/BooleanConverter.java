package org.mintframework.mvc.converter;

/**
 * Convert String to Boolean.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class BooleanConverter implements ParameterConverter<Boolean> {

    public Boolean convert(String s) {
        return Boolean.parseBoolean(s);
    }

	@Override
	public boolean canConvert(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

}
