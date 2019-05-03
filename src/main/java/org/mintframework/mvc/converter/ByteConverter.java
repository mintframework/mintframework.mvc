package org.mintframework.mvc.converter;

/**
 * Convert String to Byte.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class ByteConverter implements ParameterConverter<Byte> {

    public Byte convert(String s) {
        try {
    		return Byte.parseByte(s);
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
