package org.mintframework.mvc.converter;

/**
 * Convert String to Integer.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class IntegerConverter implements Converter<Integer> {

    public Integer convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
    	
        return Integer.parseInt(s);
    }

}
