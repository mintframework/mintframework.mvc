package mint.mvc.core;

import mint.mvc.annotation.ServiceConfig;

public abstract class Service {
	boolean initService(){
		ServiceConfig si = this.getClass().getAnnotation(ServiceConfig.class);
		
		if(si == null){
			return false;
		}
		
		String name = si.name();
		if("".equals(name.trim())){
			return false;
		}
		
		return true;
	}
	
	public abstract void service(ActionContext ctx, ModuleConfig module, APIConfig api, ServiceChain chain) throws Exception;
}
