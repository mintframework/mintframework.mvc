package mint.mvc.converter;

/**
 * Convert String to Long.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
public class LongConverter implements Converter<Long> {

    public Long convert(String s) {
    	if("".equals(s.trim())){
    		return null;
    	}
    	
        return Long.parseLong(s);
    }

}
