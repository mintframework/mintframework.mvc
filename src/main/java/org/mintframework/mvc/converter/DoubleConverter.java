package org.mintframework.mvc.converter;

/**
 * Convert String to Double.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class DoubleConverter implements Converter<Double> {

    public Double convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
        return Double.parseDouble(s);
    }

}
