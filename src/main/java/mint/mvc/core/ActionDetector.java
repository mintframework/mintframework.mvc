package mint.mvc.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import mint.mvc.annotation.BaseMapping;
import mint.mvc.annotation.Mapping;
import mint.mvc.annotation.ServiceNames;

/**
 * action 探测器。用来从指定实体中找到多有的action实体，并找到所有的action 方法（带Mapping）的方法
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午7:43:43 
 */
class ActionDetector {
	private Logger log = Logger.getLogger(this.getClass().getName());
	protected Map<UrlMatcher, ActionConfig> getUrlMap 	= new HashMap<UrlMatcher, ActionConfig>();
	protected Map<UrlMatcher, ActionConfig> putUrlMap 	= new HashMap<UrlMatcher, ActionConfig>();
	protected Map<UrlMatcher, ActionConfig> postUrlMap 	= new HashMap<UrlMatcher, ActionConfig>();
	protected Map<UrlMatcher, ActionConfig> headUrlMap 	= new HashMap<UrlMatcher, ActionConfig>();
	protected Map<UrlMatcher, ActionConfig> deleteUrlMap 	= new HashMap<UrlMatcher, ActionConfig>();
	protected Map<UrlMatcher, ActionConfig> optionsUrlMap 	= new HashMap<UrlMatcher, ActionConfig>();
	
	/**
	 * find out action methods from given beans.
	 * @param beans
	 * @return
	 */
	void awareActionMethodFromBeans(Set<Object> beans) {
		for(Object bean : beans){
			awareActionFromBean(bean);
		}
	}

	/**
	 * find out action methods from single bean.
	 * @param actionBean
	 * @return
	 */
	private void awareActionFromBean(Object actionBean){
		Class<?> clazz = actionBean.getClass();
		String 	baseUrl = clazz.getAnnotation(BaseMapping.class).value();

		/*一个url匹配器和一个action组成键值对*/
		/*"UrlMatcher=>Action" key-value*/
		Method[]	methods 	= clazz.getMethods();
		Mapping		mapping		= null;
		String[]	urls 		= null;
		
		
		for (Method method : methods) {
			if (isActionMethod(method)) {
				mapping = method.getAnnotation(Mapping.class);
				urls = mapping.urls();
				ServiceNames service;
				for(String url : urls){
					url = baseUrl + url;
					
					UrlMatcher 	matcher = new UrlMatcher(url, method);
					/*如果pattern为空，则说明该action方法无法被访问到*/
					if(matcher.pattern != null){
						log.info("Mapping url '" + url + "' to method '" + method.toGenericString() + "'.");
						service = method.getAnnotation(ServiceNames.class);
						
						if(service!=null){
							addAction(matcher, new ActionConfig(actionBean, method, matcher.urlArgumentOrder, service.value()), mapping.method());
						} else {
							addAction(matcher, new ActionConfig(actionBean, method, matcher.urlArgumentOrder, null), mapping.method());
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
	private void addAction(UrlMatcher matcher, ActionConfig action, String[] methods){
		
		
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
		Mapping mapping = method.getAnnotation(Mapping.class);
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