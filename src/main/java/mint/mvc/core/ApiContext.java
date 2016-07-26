package mint.mvc.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import mint.mvc.annotation.MultipartConfig;
import mint.mvc.annotation.Required;

/**
 * Internal class which holds object instance, method and arguments' types.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LW
 */
class ApiContext {
	static Logger logger = Logger.getLogger(ApiContext.class.getName());
	
	/**
	 * 声明的内置变量
	 */
	static final Class<?>[] builtInObjects = {HttpServletRequest.class, HttpServletResponse.class, HttpSession.class, Cookie.class};
	
	/**
	 * Object instance.
	 */
	final Object instance;

	/**
	 * Method instance.
	 */
	final Method actionMethod;
	
	final boolean isMultipartAction;
	
	final MultipartConfig multipartConfig;
	
	/**
	 * Method's arguments' types.
	 */
	final Class<?>[] argumentTypes;
	
	final boolean[] requires;
	/**
	 * 
	 */
	final int[] urlArgumentOrder;
	
	/**
	 * Method's arguments' names.
	 */
	final List<String> 	argumentNames;
	
	final String[] serviceNames;
	
	final ModuleConfig module;
	
	final APIConfig api;
	
	/**
	 * 参数注射器
	 */
	final Map<String ,ParameterInjector> injectors = new HashMap<String ,ParameterInjector>();
	
	List<BuildInArgumentInfo> builtInArguments = null;

	ApiContext(Object instance, Method apiMethod, List<String> argumentNames, int[] urlArgumentOrder, String[] serviceNames, ModuleConfig module, APIConfig api) {
		this.instance 		= instance;
		this.actionMethod 	= apiMethod;
		this.argumentTypes 	= apiMethod.getParameterTypes();
		this.argumentNames	= argumentNames;
		this.urlArgumentOrder = urlArgumentOrder;
		this.serviceNames = serviceNames;
		this.module = module;
		this.api = api;
		
		Annotation[][] ans = apiMethod.getParameterAnnotations();
		this.requires = new boolean[argumentTypes.length];
		
		for(int i=0; i<ans.length; i++){
			for(Annotation a : ans[i]){
				if(a instanceof Required){
					requires[i] = true;
					continue;
				} else {
					requires[i] = false;
				}
			}
		}
		
		if(apiMethod.getAnnotation(MultipartConfig.class) != null){
			multipartConfig = apiMethod.getAnnotation(MultipartConfig.class);
			boolean is = true;
			if("".equals(multipartConfig.attributeName())){
			is = false;
			logger.warning(apiMethod.getName() + ":多媒体请求没有配置 attributeName");
		}
		
		if(multipartConfig.limitSize() <= 0){
			is = false;
				logger.warning(apiMethod.getName() + ":多媒体请求没有配置 正确的limitSize");
			}
			
			this.isMultipartAction = is;
		} else {
			this.isMultipartAction = false;
			multipartConfig = null;
		}
		
		/*取消虚拟机安全检查，大幅提高方法调用效率*/
		this.actionMethod.setAccessible(true);
		initInjector();
	}

	/**
	 * 为action方法初始化参数注射器（请求参数->java Object）
	 * 
	 * TODO 如果一个方法已经被解析过，就不要在解析了，这种情况在一个方法配置多个url时发生
	 */
	private void initInjector(){
		ParameterInjector injector;
		Set<String>	keys;
		Class<?> type;
		for(int i=0 ;i<argumentTypes.length ;i++){
			type = argumentTypes[i];
			/*
			 * 内置参数
			 * 包括Cookie数组、HttpServletRequest、HttpServletResponse、Session
			 */
			if(type.equals(Cookie.class) || type.equals(Cookie[].class) || type.equals(HttpSession.class) || type.equals(HttpServletRequest.class) || type.equals(HttpServletResponse.class)){
				if(builtInArguments == null) builtInArguments = new ArrayList<BuildInArgumentInfo>(); 
				builtInArguments.add(new BuildInArgumentInfo(i, argumentNames.get(i), type));
				
				continue;
			}

			injector = new ParameterInjector(i, type, argumentNames.get(i));
			keys = injector.getKeys();
			for(String key : keys){
				injectors.put(key, injector);
			}
		}
	}
}