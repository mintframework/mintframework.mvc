package mint.mvc.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import mint.mvc.annotation.Module;
import mint.mvc.annotation.InterceptorMapping;
import mint.mvc.annotation.ServiceName;
import mint.mvc.util.ClassScaner;

/**
 * 
* 组件的扫描器。根据web.xml配置的"actionPackages"启动参数，自动扫描出action、interceptor和service
* @author LiangWei 
* @date 2015年3月13日 下午7:37:45 
*
 */
class ComponentScaner {
	private Logger logger = Logger.getLogger(ComponentScaner.class.getName());
	
	Set<Class<?>> interceptorClasses;
	Set<Class<?>> moduleClass;
	Set<Class<?>> serviceClasses;
	
	/**
	 * 根据web.xml配置的"actionPackages"启动参数，自动扫描出action、interceptor和service
	 * @param config
	 */
	ComponentScaner(Config config){
		ClassScaner sc = new ClassScaner(config.getClass().getClassLoader());
		
		String param = config.getInitParameter("componentPackages");
		
		if(param != null && !param.equals("")) {
			Set<String> componentNames = new HashSet<String>();
			
			for(String pkg : param.split(";")){
				componentNames.addAll(sc.getClassnameFromPackage(pkg.trim(), true));
			}
			
			Class<?> clazz;
			
			interceptorClasses =  new HashSet<Class<?>>();
			moduleClass = new HashSet<Class<?>>();
			serviceClasses = new HashSet<Class<?>>();
			
			for(String clsName : componentNames){
				try {
					clazz = Class.forName(clsName, false, this.getClass().getClassLoader()); //避免static语句执行所发生的错误
					
					if(clazz.getAnnotation(Module.class) != null){
						//识别action
						moduleClass.add(clazz);
						logger.info("discover a action->"+clsName);
					} else if(clazz.getAnnotation(InterceptorMapping.class) != null){
						//识别拦截器
						for(Class<?> parent = clazz.getSuperclass(); parent != null; parent = parent.getSuperclass()){
							if(parent.equals(Interceptor.class)){
								interceptorClasses.add(clazz);
								logger.info("discover a interceptor->"+clsName);
								break;
							}
						}
					} else if(clazz.getAnnotation(ServiceName.class) != null){
						//识别服务
						for(Class<?> parent = clazz.getSuperclass(); parent != null; parent = parent.getSuperclass()){
							if(parent.equals(Service.class)){
								serviceClasses.add(clazz);
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
	Set<Interceptor> getInteceptorBeans(){
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
	Map<String, Service> getServiceBeans(){
		if(serviceClasses!=null){
			Map<String, Service> services = new HashMap<String, Service>();
			Service sis = null;
			String name;
			for(Class<?> cls : serviceClasses){
				name = cls.getAnnotation(ServiceName.class).value().trim();
				
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
	Set<Object> getActionBeans(){
		if(interceptorClasses!=null){
			Set<Object> actions = new HashSet<Object>();
			for(Class<?> cls : moduleClass){
				try {
					actions.add(cls.newInstance());
				} catch (InstantiationException | IllegalAccessException e) {
					logger.warning("can't instantiates a action->"+cls.getName());
				}
			}
			return actions;
		} else {
			return null;
		}
	}
}
