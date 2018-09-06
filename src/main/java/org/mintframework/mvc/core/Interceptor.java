package org.mintframework.mvc.core;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mintframework.mvc.annotation.InterceptorConfig;


/**
 * 
 * Intercept action's execution like servlet Filter, but interceptors are 
 * configured and managed by IoC container. Another difference from Filter 
 * is that Interceptor is executed around Action's execution, but before 
 * rendering view.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:08:52 
 *
 */
public abstract class Interceptor {
	private final static String checkReg = "^((/\\w+)*)[/]?[\\*]?";
	
	private Matcher matcher = null;
	private Logger log = Logger.getLogger(this.getClass().getName());
	
	/**
	 * 根据注解初始化url匹配器，并返回成功与否
	 * @return
	 */
	boolean initMatcher(){
		InterceptorConfig intemap = this.getClass().getAnnotation(InterceptorConfig.class);
		
		if(intemap != null){
			String[] urlPattern = intemap.urls();
			matcher = Pattern.compile(checkReg).matcher("");
			
			StringBuilder icptPattern = new StringBuilder();
			String holder = "。。！！！！······~~~~~~————￥￥￥。。‘’‘“”“、、、？？？";
			
			for(String url : urlPattern){
				
				/*检查url结构是否正确*/
				matcher.reset(url);
				url = url.replace("/**", holder);
				url = url.replace("/*", "(/[^/]*)");
				url = url.replace(holder, "(/[^/]+)*");
				url = url+"[/]?$";
				icptPattern.append("|").append(url);
			}
			
			if(icptPattern.length()>1){
				icptPattern.deleteCharAt(0);
				matcher.usePattern(Pattern.compile(icptPattern.toString()));
			} else {
				log.warning("发现无效拦截器:"+this.getClass().getName());
				return false;
			}
			return true;
		} else {
			log.warning("拦截器未正确配置InterceptorMapping");
			return false;
		}
	}
	
	/**
	 * 当前拦截器是否匹配当前请求的url
	 * @param url
	 * @return
	 */
	boolean matchers(String url){
		return matcher.reset(url).matches();
	}
	
	/**
	 * Do intercept and invoke chain.doInterceptor() to process next interceptor. 
	 * NOTE that process will not continue if chain.doInterceptor() method is not 
	 * invoked.
	 * 
	 * @param chain Interceptor chain.
	 * @throws Exception If any exception is thrown, process will not continued.
	 */
	public abstract void intercept(RequestContext ctx, ModuleConfig module, APIConfig api, InterceptorChain chain) throws Exception;
}
