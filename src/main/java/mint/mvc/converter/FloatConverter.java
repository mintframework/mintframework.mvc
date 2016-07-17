package mint.mvc.converter;

/**
 * Convert String to Float.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class FloatConverter implements Converter<Float> {

    public Float convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
        return Float.parseFloat(s);
    }

}
