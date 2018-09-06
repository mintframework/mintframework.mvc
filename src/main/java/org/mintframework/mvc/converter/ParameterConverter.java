package org.mintframework.mvc.converter;

/**
 * Convert String to any given type.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * 
 * @param <T> Generic type of converted result.
 */
public interface ParameterConverter<T> {

    /**
     * Convert a not-null String to specified object.
     */
    T convert(String s);

    boolean canConvert(Class<?> clazz);
}
