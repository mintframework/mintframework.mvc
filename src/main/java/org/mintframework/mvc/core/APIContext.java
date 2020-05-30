package org.mintframework.mvc.core;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mintframework.mvc.annotation.UploadConfig;
import org.mintframework.mvc.annotation.Required;
import org.mintframework.mvc.converter.ParameterConverterFactory;
import org.mintframework.mvc.core.upload.MintTempFile;

/**
 * Internal class which holds object instance, method and arguments' types.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LW
 */
class APIContext {
	static Logger logger = Logger.getLogger(APIContext.class.getName());
	
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
	
	final boolean isMultipartApi;
	
	final Map<String, UploadParamInfo> uploadConfigs = new HashMap<>();
	
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

	APIContext(
		Object instance,
		Method apiMethod,
		List<String> argumentNames,
		int[] urlArgumentOrder,
		String[] serviceNames,
		ModuleConfig module,
		APIConfig api
	) {
		
		this.instance 			= instance;
		this.apiMethod 			= apiMethod;
		this.argumentClasses 	= apiMethod.getParameterTypes();
		this.argumentNames		= argumentNames;
		this.urlArgumentOrder 	= urlArgumentOrder;
		this.serviceNames 		= serviceNames;
		this.module 			= module;
		this.api 				= api;
		
		Annotation[][] ans = apiMethod.getParameterAnnotations();
		this.requires = new boolean[argumentClasses.length];
		
		for(int i=0; i<ans.length; i++){
			for(Annotation a : ans[i]){
				if(a instanceof Required){
					requires[i] = true;
				} else {
					requires[i] = false;
				}
			}
		}
		/*取消虚拟机安全检查，大幅提高方法调用效率*/
		this.apiMethod.setAccessible(true);
		
		/**
		 * 为api方法初始化参数注射器（请求参数->java Object）
		 * 
		 * TODO 如果一个方法已经被解析过，就不要再解析了，这种情况在一个方法配置多个url时发生
		 */
		ParameterInjector injector = null;
		Set<String>	keys;
		Class<?> clazz;
		Parameter param;
		Parameter[] params = apiMethod.getParameters();
		boolean isInitMulti = false;
		for(int i=0 ;i<params.length ;i++){
			param = params[i];
			clazz = param.getType();
			/*
			 * 内置参数
			 * 包括Cookie数组、HttpServletRequest、HttpServletResponse、Session
			 */
			if(clazz.equals(Cookie.class) 
				|| clazz.equals(Cookie[].class) 
				|| clazz.equals(HttpSession.class) 
				|| clazz.equals(HttpServletRequest.class) 
				|| clazz.equals(HttpServletResponse.class)
				|| clazz.equals(RequestBody.class)
				|| clazz.equals(ServletConfig.class)
				|| clazz.equals(ServletContext.class)){
				
				if(builtInArguments == null) {
					builtInArguments = new ArrayList<BuildInArgumentInfo>(); 
				}
				builtInArguments.add(new BuildInArgumentInfo(i, argumentNames.get(i), clazz));
				
				continue;
			} else if(clazz.equals(Map.class)) {
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
						if(!(new ParameterConverterFactory()).canConvert(mapKeyClass)){
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
						if(!(new ParameterConverterFactory()).canConvert(mapValueClass)){
							logger.warning(apiMethod.toGenericString() + " include unsupported Map parameterizedType "
								+ mapValueClass.getName() + ", support only  primitive type or String");
							continue;
						}
					}
					injector = new ParameterInjector(i, clazz, argumentNames.get(i), true, mapKeyClass, mapValueClass);
				} else {
					injector = new ParameterInjector(i, clazz, argumentNames.get(i), true, String.class, String.class);
				}
			} else if(clazz.equals(MintTempFile.class) || clazz.equals(MintTempFile[].class) || clazz.equals(File.class) || clazz.equals(File[].class)) { //获取文件上传配置
				isInitMulti = true;
				UploadConfig uinfo = param.getAnnotation(UploadConfig.class);
				UploadParamInfo upinfo = null;
				if(clazz.equals(MintTempFile.class) || clazz.equals(File.class)) {
					upinfo = new UploadParamInfo(uinfo.limitSize(), i, 0);
				} else if(clazz.equals(MintTempFile[].class) || clazz.equals(File[].class)) {
					upinfo = new UploadParamInfo(uinfo.limitSize(), i, 1);
				}
				uploadConfigs.put(argumentNames.get(i), upinfo);
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
		
		this.isMultipartApi = isInitMulti;
	}
}