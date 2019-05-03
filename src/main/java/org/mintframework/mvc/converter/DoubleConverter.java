package org.mintframework.mvc.converter;

/**
 * Convert String to Double.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class DoubleConverter implements ParameterConverter<Double> {

    public Double convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
        try {
    		return Double.parseDouble(s);
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
