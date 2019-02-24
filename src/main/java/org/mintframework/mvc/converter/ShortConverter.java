package org.mintframework.mvc.converter;

/**
 * Convert String to Short.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class ShortConverter implements ParameterConverter<Short> {

    public Short convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
        return Short.parseShort(s);
    }

	@Override
	public boolean canConvert(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

}
