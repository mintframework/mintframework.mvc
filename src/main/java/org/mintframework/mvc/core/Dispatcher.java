package org.mintframework.mvc.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.mintframework.mvc.annotation.InterceptorConfig;
import org.mintframework.util.PropertiesMap;

/**
 * Dispatcher handles ALL requests from clients, and dispatches to appropriate
 * handler to handle each request.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:07:16 
 *
 */
class Dispatcher {
	private Logger log = Logger.getLogger(this.getClass().getName());
	private Map<String, Map<UrlMatcher, APIContext>> urlMapMap = new HashMap<String, Map<UrlMatcher, APIContext>>();
	private Map<String, UrlMatcher[]> matchersMap = new HashMap<String, UrlMatcher[]>();
	private Boolean interceptStatic = false;
	private ServletContext context = null;
	private Boolean hasNoApi = true;

	/**
	 * 拦截器
	 */
	private List<Interceptor>		uriInterceptors = new ArrayList<Interceptor>();
	private Map<String, Service> 	servicesMap = new HashMap<String, Service>();
	
	void init(ServletContext context, PropertiesMap config) throws ServletException {
		log.info("Init Dispatcher...");
		
		this.context = context;
		try {
			initAll(config);
		} catch (ServletException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException("Dispatcher init failed.", e);
		}
	}

	/**
	 * http://example.com:8080/over/there?name=ferret#nose
	 * \__/   \______________/\_________/ \_________/ \__/
	 *   |         |                |         |         |
	 * scheme   authority          path     query    fragment
	 *
	 * OR
	 * 
	 * [scheme:][//authority][path][?query][#fragment]
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	API dispatch(HttpServletRequest request, String method) throws ServletException, IOException {
		
		if(this.hasNoApi) {
			return null;
		}
		
		String path = request.getRequestURI();
		String ctxP = request.getContextPath();
		
		if (ctxP.length() > 0) {
			path = path.substring(ctxP.length());
		}
		
		// set default character encoding to "utf-8" if encoding is not set:
		if (request.getCharacterEncoding() == null) {
			request.setCharacterEncoding("UTF-8");
		}

		APIContext 	actionConfig	= null;
		String[] 		urlArgs		= null;
		
		
		/* 寻找处理请求的action（方法） */
		for (UrlMatcher m : this.matchersMap.get(method)) {
			
			//获取url参数
			urlArgs = m.getUrlParameters(path);
			if (urlArgs != null) {
				actionConfig = urlMapMap.get(method).get(m);
				break;
			}
		}

		if(actionConfig == null && !this.interceptStatic){
			return null;
		}
		
		//查找处理请求的拦截器
		//uri拦截器
		List<Interceptor> interceptors = new ArrayList<Interceptor>();
		for(Interceptor it : uriInterceptors){
			if(it.matchers(path)){
				interceptors.add(it);
			}
		}
		
		//服务
		List<Service> services = null;
		if(actionConfig != null){
			if(servicesMap!=null && actionConfig.serviceNames!=null && actionConfig.serviceNames.length>0){
				services = new ArrayList<Service>();
				
				Service s;
				for(String name : actionConfig.serviceNames){
					s = servicesMap.get(name);
					if(s!=null){
						services.add(s);
					}
				}
			}
		}
		
		if(interceptors.size()==0 && actionConfig==null){
			return null;
		} else {
			return new API(actionConfig, urlArgs, path, interceptors, services);
		}
	}

	/**
	 * 
	 * @param config
	 * @throws Exception
	 */
	private void initAll(PropertiesMap config) throws Exception {
		initComponents(config);
	}

	/* 初始化action */
	private void initComponents(PropertiesMap config) throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		interceptStatic = config.getBoolean("mint.mvc.interceptStatic");

		if(interceptStatic==null){
			interceptStatic = false;
		}

		ComponentScaner componentScaner = new ComponentScaner(config);
		
		/* 初始化action */
		log.info("start matching url ...");
		APIDetector ad = new APIDetector();
		ad.awareActionMethodFromBeans(componentScaner.getActionObjects());
		
		this.urlMapMap.put("get", ad.getUrlMap);
		this.urlMapMap.put("put", ad.putUrlMap);
		this.urlMapMap.put("post", ad.postUrlMap);
		this.urlMapMap.put("head", ad.headUrlMap);
		this.urlMapMap.put("delete", ad.deleteUrlMap);
		this.urlMapMap.put("options", ad.optionsUrlMap);
		
		UrlMatcher[] ms = ad.getUrlMap.keySet().toArray(new UrlMatcher[ad.getUrlMap.size()]);
		Arrays.sort(ms);
		this.matchersMap.put("get", ms);
		
		ms = ad.putUrlMap.keySet().toArray(new UrlMatcher[ad.putUrlMap.size()]);
		Arrays.sort(ms);
		this.matchersMap.put("put", ms);
		
		ms = ad.postUrlMap.keySet().toArray(new UrlMatcher[ad.postUrlMap.size()]);
		Arrays.sort(ms);
		this.matchersMap.put("post", ms);
		
		ms = ad.headUrlMap.keySet().toArray(new UrlMatcher[ad.headUrlMap.size()]);
		Arrays.sort(ms);
		this.matchersMap.put("head", ms);
		
		ms = ad.deleteUrlMap.keySet().toArray(new UrlMatcher[ad.deleteUrlMap.size()]);
		Arrays.sort(ms);
		this.matchersMap.put("delete", ms);
		
		ms = ad.optionsUrlMap.keySet().toArray(new UrlMatcher[ad.optionsUrlMap.size()]);
		Arrays.sort(ms);
		this.matchersMap.put("options", ms);
		
		log.info("end matching url ");
		
		for(Entry<String, UrlMatcher[]> m : this.matchersMap.entrySet()) {
			if(m.getValue().length > 0) {
				this.hasNoApi = false;
				break;
			}
		}
		
		//初始化url拦截器
		Set<Interceptor> itset = componentScaner.getInteceptorObjects();
		uriInterceptors = new ArrayList<Interceptor>();
		if(itset != null){
			uriInterceptors.addAll(itset);
		}
		// url拦截器 拦截器排序
		Collections.sort(uriInterceptors, new Comparator<Interceptor>() {	
			public int compare(Interceptor i1, Interceptor i2) {
				InterceptorConfig o1 = i1.getClass().getAnnotation(InterceptorConfig.class);
				InterceptorConfig o2 = i2.getClass().getAnnotation(InterceptorConfig.class);
				int n1 = o1 == null ? Integer.MAX_VALUE : o1.order();
				int n2 = o2 == null ? Integer.MAX_VALUE : o2.order();
				
				if (n1 == n2) {
					return i1.getClass().getName().compareTo(i2.getClass().getName());
				}
				return n1 < n2 ? (-1) : 1;
			}
		});
		
		//初始化service拦截器
		servicesMap = componentScaner.getServiceObjects();
		
		//初始化组件报告器
		String cr = config.get("mint.mvc.startupListener");
		if(cr!=null && !"".equals(cr.trim())){
			try {
				Class<?> clazz = Class.forName(cr, false, this.getClass().getClassLoader());
				
				if(StartupListener.class.isAssignableFrom(clazz)){
					StartupListener componentReportor = (StartupListener) clazz.getDeclaredConstructor().newInstance();
					componentReportor.report(this.context, ad.modules, componentScaner.getServiceConfigs(), componentScaner.getInterceptorConfig());
				}
				
			} catch (ClassNotFoundException e) {
				log.warning("componentReportor class("+ cr +") do not be found");
			} catch (InstantiationException | IllegalAccessException e) {
				log.warning("componentReportor class("+ cr +") can not be instance");
			}
		}
	}
	
	void destroy() {
		log.info("Destroy Dispatcher...");
	}
}