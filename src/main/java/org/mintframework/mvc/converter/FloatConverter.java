package org.mintframework.mvc.converter;

/**
 * Convert String to Float.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class FloatConverter implements ParameterConverter<Float> {

    public Float convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
    	try {
    		return Float.parseFloat(s);
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
