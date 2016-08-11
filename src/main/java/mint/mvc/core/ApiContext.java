package mint.mvc.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
import mint.mvc.converter.ConverterFactory;

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
	final Method apiMethod;
	
	final boolean isMultipartAction;
	
	final MultipartConfig multipartConfig;
	
	/**
	 * Method's arguments' types.
	 */
	final Class<?>[] argumentClasses;
	
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
	
	boolean hasMapParam = false;
	
	Class<?> mapKeyClass = null;
	
	Class<?> mapValueClass = null;
	
	/**
	 * 参数注射器
	 */
	final Map<String ,ParameterInjector> injectorsMap = new HashMap<String ,ParameterInjector>();
	
	List<BuildInArgumentInfo> builtInArguments = null;

	ApiContext(Object instance, Method apiMethod, List<String> argumentNames, int[] urlArgumentOrder, String[] serviceNames, ModuleConfig module, APIConfig api) {
		this.instance 		= instance;
		this.apiMethod 	= apiMethod;
		this.argumentClasses 	= apiMethod.getParameterTypes();
		this.argumentNames	= argumentNames;
		this.urlArgumentOrder = urlArgumentOrder;
		this.serviceNames = serviceNames;
		this.module = module;
		this.api = api;
		
		Annotation[][] ans = apiMethod.getParameterAnnotations();
		this.requires = new boolean[argumentClasses.length];
		
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
		this.apiMethod.setAccessible(true);
		initInjector();
	}

	/**
	 * 为action方法初始化参数注射器（请求参数->java Object）
	 * 
	 * TODO 如果一个方法已经被解析过，就不要在解析了，这种情况在一个方法配置多个url时发生
	 */
	private void initInjector(){
		ParameterInjector injector = null;
		Set<String>	keys;
		Class<?> clazz;
		
		for(int i=0 ;i<argumentClasses.length ;i++){
			clazz = argumentClasses[i];
			/*
			 * 内置参数
			 * 包括Cookie数组、HttpServletRequest、HttpServletResponse、Session
			 */
			if(clazz.equals(Cookie.class) 
				|| clazz.equals(Cookie[].class) 
				|| clazz.equals(HttpSession.class) 
				|| clazz.equals(HttpServletRequest.class) 
				|| clazz.equals(HttpServletResponse.class)){
				
				if(builtInArguments == null) {
					builtInArguments = new ArrayList<BuildInArgumentInfo>(); 
				}
				builtInArguments.add(new BuildInArgumentInfo(i, argumentNames.get(i), clazz));
				
				continue;
			} else if(clazz.equals(Map.class)){
				this.hasMapParam = true;
				Type type = apiMethod.getGenericParameterTypes()[i];
				
				//获取Map中泛型的类型
				if (type instanceof ParameterizedType) {
					ParameterizedType paramType = (ParameterizedType) type;
					Type[] argTypes = paramType.getActualTypeArguments();
					
					//是否有泛型
					if(argTypes!=null){
						//是否有第一个泛型
						if(argTypes.length>0){
							try {
								mapKeyClass = Class.forName(argTypes[0].getTypeName());
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
								continue;
							}
						}
						
						if(mapKeyClass == null) {
							mapKeyClass = String.class;
						}
						
						//检测泛型是否可以转换
						if(!(new ConverterFactory()).canConvert(mapKeyClass)){
							logger.warning(apiMethod.toGenericString() + " include unsupported Map parameterizedType "
								+ mapKeyClass.getName() + ", support only  primitive type or String");
							continue;
						}
						
						//是否有第二个泛型
						if(argTypes.length==2){
							try {
								mapValueClass = Class.forName(argTypes[1].getTypeName());
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
								continue;
							}
						}
						
						if(mapValueClass == null) {
							mapValueClass = String.class;
						}
						
						//检测泛型是否可以转换
						if(!(new ConverterFactory()).canConvert(mapValueClass)){
							logger.warning(apiMethod.toGenericString() + " include unsupported Map parameterizedType "
								+ mapValueClass.getName() + ", support only  primitive type or String");
							continue;
						}
					}
					
					injector = new ParameterInjector(i, clazz, argumentNames.get(i), true, mapKeyClass, mapValueClass);
				} else {
					injector = new ParameterInjector(i, clazz, argumentNames.get(i), true, String.class, String.class);
				}
			} else {
				injector = new ParameterInjector(i, clazz, argumentNames.get(i), false, null, null);
			}
			
			if(injector!=null){
				keys = injector.getKeys();
				for(String key : keys){
					injectorsMap.put(key, injector);
				}
			}
		}
	}
}