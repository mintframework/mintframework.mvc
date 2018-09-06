package org.mintframework.mvc.converter;

/**
 * Convert String to Byte.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class ByteConverter implements ParameterConverter<Byte> {

    public Byte convert(String s) {
        return Byte.parseByte(s);
    }

	@Override
	public boolean canConvert(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

}
