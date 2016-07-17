package mint.mvc.core;

import java.util.List;

/**
 * Used for holds an interceptor chain.
 * @author Michael Liao (askxuefeng@gmail.com)
 */
class ServiceChainImpl implements ServiceChain {
    private final 	List<Service> services;
    private int 	index 	= 0;
    private boolean isPass 	= false;
    private	int size;
    
    boolean isPass() {
        return isPass;
    }
    
    ServiceChainImpl(List<Service> services) {
    	this.services = services;
    	size = this.services.size();
    }

    public void doService(ActionContext ctx) throws Exception {
        if(index == size){
        	this.isPass = true;
        } else {
            //must update index first, otherwise will cause stack overflow:
            index++;
            services.get(index-1).service(ctx, this);
        }
    }
}
