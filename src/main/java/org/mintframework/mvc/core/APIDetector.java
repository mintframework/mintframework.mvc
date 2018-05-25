package org.mintframework.mvc.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mintframework.mvc.annotation.API;
import org.mintframework.mvc.annotation.Module;
import org.mintframework.mvc.annotation.ApiServices;
import org.mintframework.mvc.util.GetArgumentName;

/**
 * action 探测器。用来从指定实体中找到多有的action实体，并找到所有的action 方法（带Mapping）的方法
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午7:43:43 
 */
class APIDetector {
	private Logger log = Logger.getLogger(this.getClass().getName());
	protected Map<UrlMatcher, APIContext> getUrlMap 	= new HashMap<UrlMatcher, APIContext>();
	protected Map<UrlMatcher, APIContext> putUrlMap 	= new HashMap<UrlMatcher, APIContext>();
	protected Map<UrlMatcher, APIContext> postUrlMap 	= new HashMap<UrlMatcher, APIContext>();
	protected Map<UrlMatcher, APIContext> headUrlMap 	= new HashMap<UrlMatcher, APIContext>();
	protected Map<UrlMatcher, APIContext> deleteUrlMap 	= new HashMap<UrlMatcher, APIContext>();
	protected Map<UrlMatcher, APIContext> optionsUrlMap 	= new HashMap<UrlMatcher, APIContext>();
	
	protected Set<ModuleConfig> modules = new HashSet<ModuleConfig>();
	
	/**
	 * find out action methods from given beans.
	 * @param beans
	 * @return
	 */
	void awareActionMethodFromBeans(Set<Object> beans) {
		for(Object bean : beans){
			awareAPIFromBean(bean);
		}
	}

	/**
	 * find out action methods from single bean.
	 * @param moduleBean
	 * @return
	 */
	private void awareAPIFromBean(Object moduleBean){
		Class<?> clazz = moduleBean.getClass();
		Module mconfig = clazz.getAnnotation(Module.class);
		String 	baseUrl = mconfig.url();

		/*一个url匹配器和一个action组成键值对*/
		/*"UrlMatcher=>API" key-value*/
		Method[]	apiMethods 	= clazz.getMethods();
		API	apiConfig	= null;
		String[]	urls 		= null;
		
		Set<APIConfig> apis = new HashSet<APIConfig>();
		ModuleConfig module = new ModuleConfig(mconfig.url(), mconfig.id(), mconfig.name(), mconfig.desc(), mconfig.tags(), apis);
		modules.add(module);
		
		for (Method apiMethod : apiMethods) {
			if (isActionMethod(apiMethod)) {
				apiConfig = apiMethod.getAnnotation(API.class);
				urls = apiConfig.urls();
				ApiServices service;
				
				for(String url : urls){
					url = baseUrl + "/" + url;

					UrlMatcher 	matcher = new UrlMatcher(url, apiMethod);
					/*如果pattern为空，则说明该api方法无法被访问到*/
					if(matcher.pattern != null){
						log.info("Mapping url '" + matcher.url + "' to method '" + apiMethod.toGenericString() + "'.");
						service = apiMethod.getAnnotation(ApiServices.class);
						List<String> argNames = GetArgumentName.getArgumentNames(apiMethod);
						
						APIConfig api = new APIConfig(
							apiConfig.urls(), 
							apiConfig.id(), 
							apiConfig.name(), 
							apiConfig.method(), 
							apiConfig.protocol(), 
							apiConfig.desc(), 
							apiConfig.tags(),
							apiMethod.getParameterTypes(),
							argNames.toArray(new String[argNames.size()]));
						
						apis.add(api);
						
						if(service!=null){
							addApi(matcher, new APIContext(moduleBean, apiMethod, argNames, matcher.urlArgumentOrder, service.value(), module, api), apiConfig.method());
						} else {
							addApi(matcher, new APIContext(moduleBean, apiMethod, argNames, matcher.urlArgumentOrder, null, module, api), apiConfig.method());
						}
					}
				}
			}
		}
	}
	
	/**
	 * @param matcher
	 * @param action
	 * @param method
	 */
	private void addApi(UrlMatcher matcher, APIContext action, String[] methods){
 		for(String method : methods){
			method = method.toLowerCase();
		
			if("".equals(method)){
				getUrlMap.put(matcher, action);
				postUrlMap.put(matcher, action);
				putUrlMap.put(matcher, action);
				deleteUrlMap.put(matcher, action);
				headUrlMap.put(matcher, action);
				optionsUrlMap.put(matcher, action);
			} else if("get".equals(method)){
				getUrlMap.put(matcher, action);
			} else if("post".equals(method)){
				postUrlMap.put(matcher, action);
			} else if("put".equals(method)){
				putUrlMap.put(matcher, action);
			} else if("delete".equals(method)){
				deleteUrlMap.put(matcher, action);
			} else if("head".equals(method)){
				headUrlMap.put(matcher, action);
			} else if("options".equals(method)){
				optionsUrlMap.put(matcher, action);
			} else {
				log.warning("unsupport request method : " + method + ".");
			}
		}
	}

	/**
	 * 初步检查方法是否为action方法
	 * check if the specified method is a vaild action method:
	 * @param method
	 * @return
	 */
	private boolean isActionMethod(Method method) {
		/*静态方法不是action方法*/
		if (Modifier.isStatic(method.getModifiers())) {
			warnInvalidActionMethod(method, "method is static.");
			return false;
		}
		
		/*没有Mapping 注解的不是action方法*/
		API mapping = method.getAnnotation(API.class);
		if (mapping == null) {
			return false;
		}
		
		return true;
	}

	//log warning message of invalid action method:
	private void warnInvalidActionMethod(Method m, String string) {
		log.warning("Invalid Action method '" + m.toGenericString() + "': " + string);
	}
}