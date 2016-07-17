package mint.mvc.converter;

import java.math.BigInteger;

/**
 * Convert String to Float.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class BigIntegerConverter implements Converter<BigInteger> {

	public BigInteger convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
        return new BigInteger(s);
    }
}
