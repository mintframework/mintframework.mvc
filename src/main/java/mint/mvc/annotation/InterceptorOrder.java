package mint.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Used to sort interceptors.
 * @author LiangWei(895925636@qq.com)
 * @author Michael Liao (askxuefeng@gmail.com)
 * @date 2015年3月13日 下午9:24:03 
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptorOrder {
	/**
	 * Lower value has more priority.
	 */
	int value();
}
