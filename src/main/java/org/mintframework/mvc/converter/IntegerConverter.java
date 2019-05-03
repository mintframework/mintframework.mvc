package org.mintframework.mvc.converter;

/**
 * Convert String to Integer.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class IntegerConverter implements ParameterConverter<Integer> {

    public Integer convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
    	
    	try {
    		return Integer.parseInt(s);
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
