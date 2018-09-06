package org.mintframework.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation for mapping URL.<br/>
 * For example:<br/>
 * <pre>
 *	@InterceptorOrder(0)
 *	@InterceptorMapping(urls="/user/*")
 *	public class DefaultInterceptor implements Interceptor{
 *		@Override
 *		public void intercept(ActionContext ctx, InterceptorChain chain) throws Exception {
 *		}
 *	}
 * </pre> 
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:23:15 
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptorConfig {
    String[] 	urls();
    int			order()		default 0;
    
    String 		desc()		default "";
	String 		id()		default "";
	String[] 	tags()		default "";
}