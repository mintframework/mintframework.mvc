package org.mintframework.mvc.converter;

import java.math.BigDecimal;

/**
 * Convert String to Double.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class BigDecimalConverter implements Converter<BigDecimal> {

    public BigDecimal convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
        return new BigDecimal(s);
    }

}
