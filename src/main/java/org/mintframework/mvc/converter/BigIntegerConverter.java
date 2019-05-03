package org.mintframework.mvc.converter;

import java.math.BigInteger;

/**
 * Convert String to Float.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class BigIntegerConverter implements ParameterConverter<BigInteger> {

	public BigInteger convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
    	try {
    		return new BigInteger(s);
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
