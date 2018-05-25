package org.mintframework.mvc.core;

import java.util.List;

/**
 * Used for holds an interceptor chain.
 * @author Michael Liao (askxuefeng@gmail.com)
 */
class InterceptorChainImpl implements InterceptorChain {
    private final 	List<Interceptor> interceptors;
    private int 	index 	= 0;
    private boolean isPass 	= false;
    private	int size;
    private ModuleConfig module;
    private APIConfig api;
    
    boolean isPass() {
        return isPass;
    }
    
    InterceptorChainImpl(List<Interceptor> interceptors, ModuleConfig module, APIConfig api) {
    	this.interceptors = interceptors;
    	this.api = api;
    	this.module = module;
    	size = this.interceptors.size();
    }

    public void doInterceptor(RequestContext ctx) throws Exception {
        if(index == size){
        	this.isPass = true;
        } else {
            //must update index first, otherwise will cause stack overflow:
            index++;
            interceptors.get(index-1).intercept(ctx, module, api, this);
        }
    }
}
