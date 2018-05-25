package org.mintframework.mvc.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mintframework.mvc.annotation.ApiService;
import org.mintframework.mvc.annotation.InterceptorConfig;
import org.mintframework.mvc.annotation.Module;
import org.mintframework.mvc.util.ClassScaner;
import org.mintframework.util.PropertiesMap;

/**
 * 
* 组件的扫描器。根据web.xml配置的"actionPackages"启动参数，自动扫描出action、interceptor和service
* @author LiangWei 
* @date 2015年3月13日 下午7:37:45 
*
 */
class ComponentScaner {
	private Logger logger = Logger.getLogger(ComponentScaner.class.getName());
	
	Set<Class<Interceptor>> interceptorClasses;
	Set<Class<?>> moduleClass;
	Set<Class<Service>> serviceClasses;
	Set<ApiService> services;
	Set<InterceptorConfig> interceptors;
	
	/**
	 * 根据web.xml配置的"actionPackages"启动参数，自动扫描出action、interceptor和service
	 * @param config
	 */
	@SuppressWarnings("unchecked")
	ComponentScaner(PropertiesMap config){
		ClassScaner sc = new ClassScaner(config.getClass().getClassLoader());
		
		String param = config.get("mint.mvc.component-packages");

		
		if(param != null && !param.equals("")) {
			Set<String> componentNames = new HashSet<String>();
			
			for(String pkg : param.split(";")){
				componentNames.addAll(sc.getClassnameFromPackage(pkg.trim(), true));
			}

			Class<?> clazz;
			
			interceptorClasses =  new HashSet<Class<Interceptor>>();
			moduleClass = new HashSet<Class<?>>();
			serviceClasses = new HashSet<Class<Service>>();
			
			for(String clsName : componentNames){
				try {
					clazz = Class.forName(clsName, false, this.getClass().getClassLoader()); //避免static语句执行所发生的错误
					
					if(clazz.getAnnotation(Module.class) != null){
						//识别action
						moduleClass.add(clazz);
						logger.info("discover a action->"+clsName);
					} else if(clazz.getAnnotation(InterceptorConfig.class) != null){
						//识别拦截器
						for(Class<?> parent = clazz.getSuperclass(); parent != null; parent = parent.getSuperclass()){
							if(parent.equals(Interceptor.class)){
								interceptorClasses.add((Class<Interceptor>)clazz);
								logger.info("discover a interceptor->"+clsName);
								break;
							}
						}
					} else if(clazz.getAnnotation(ApiService.class) != null){
						//识别服务
						for(Class<?> parent = clazz.getSuperclass(); parent != null; parent = parent.getSuperclass()){
							if(parent.equals(Service.class)){
								serviceClasses.add((Class<Service>)clazz);
								logger.info("discover a service->"+clsName);
								break;
							}
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 返回拦截器对象
	 * @return
	 */
	Set<Interceptor> getInteceptorObjects(){
		if(interceptorClasses!=null){
			Set<Interceptor> interceptors = new HashSet<Interceptor>();
			Interceptor itcep = null;
			for(Class<?> cls : interceptorClasses){
				try {
					itcep = (Interceptor) cls.newInstance();
					if(itcep.initMatcher()){
						interceptors.add(itcep);
					}
				} catch (InstantiationException | IllegalAccessException e) {
					logger.warning("can't instantiates a interceptor->"+cls.getName());
				}
			}
			
			return interceptors;
		} else {
			return null;
		}
	}
	
	/**
	 * 返回服务对象
	 * @return
	 */
	Map<String, Service> getServiceObjects(){
		if(serviceClasses!=null){
			Map<String, Service> services = new HashMap<String, Service>();
			Service sis = null;
			String name;
			for(Class<?> cls : serviceClasses){
				name = cls.getAnnotation(ApiService.class).name().trim();
				
				if(!"".equals(name)){
					try {
						sis = (Service) cls.newInstance();
						if(sis.initService()){
							services.put(name, sis);
						}
					} catch (InstantiationException | IllegalAccessException e) {
						logger.warning("can't instantiates a service->"+cls.getName());
					}
				}
			}
			
			return services;
		} else {
			return null;
		}
	}
	
	/**
	 * Find all beans in container.
	 */
	Set<Object> getActionObjects(){
		if(moduleClass!=null){
			Set<Object> modules = new HashSet<Object>();
			for(Class<?> cls : moduleClass){
				try {
					modules.add(cls.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					logger.warning("can't instantiates a action->"+cls.getName());
				}
			}
			return modules;
		} else {
			return null;
		}
	}
	
	/**
	 * 获取service的注解配置
	 * @return
	 */
	Set<ApiService> getServiceConfigs(){
		if(serviceClasses.size()>0){
			services = new HashSet<ApiService>();
			for(Class<Service> s: serviceClasses){
				services.add(s.getAnnotation(ApiService.class));
			}
			return services;
		} else {
			return null;
		}
	}
	
	/**
	 * 获取inteceptor的注解配置
	 * @return
	 */
	Set<InterceptorConfig> getInterceptorConfig(){
		if(serviceClasses.size()>0){
			interceptors = new HashSet<InterceptorConfig>();
			for(Class<Interceptor> s : interceptorClasses){
				interceptors.add(s.getAnnotation(InterceptorConfig.class));
			}
			return interceptors;
		} else {
			return null;
		}
	}
}
