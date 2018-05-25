package org.mintframework.mvc.core;

import org.mintframework.mvc.annotation.ApiService;

public abstract class Service {
	boolean initService(){
		ApiService si = this.getClass().getAnnotation(ApiService.class);
		
		if(si == null){
			return false;
		}
		
		String name = si.name();
		if("".equals(name.trim())){
			return false;
		}
		
		return true;
	}
	
	public abstract void service(RequestContext ctx, ModuleConfig module, APIConfig api, ServiceChain chain) throws Exception;
}
