package org.mintframework.mvc.converter;

/**
 * Convert String to Character.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class CharacterConverter implements ParameterConverter<Character> {

    public Character convert(String s) {
        if (s.length()==0)
            throw new IllegalArgumentException("Cannot convert empty string to char.");
        return s.charAt(0);
    }

	@Override
	public boolean canConvert(Class<?> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

}
