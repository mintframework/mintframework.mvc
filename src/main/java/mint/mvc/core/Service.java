package mint.mvc.core;

import mint.mvc.annotation.ServiceName;

public abstract class Service {
	boolean initService(){
		ServiceName si = this.getClass().getAnnotation(ServiceName.class);
		
		if(si == null){
			return false;
		}
		
		String name = si.value();
		if("".equals(name.trim())){
			return false;
		}
		
		return true;
	}
	
	public abstract void service(ActionContext ctx, ModuleConfig module, APIConfig api, ServiceChain chain) throws Exception;
}
