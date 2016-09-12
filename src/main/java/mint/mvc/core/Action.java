package mint.mvc.core;

import java.util.List;

/**
 * 
 * action的配置信息
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午7:43:05 
 *
 */
class Action {
	final APIContext 			apiContext;
	final String[] 				urlParams;
	final String 				uri;
	final List<Interceptor> 	interceptors;
	final List<Service>			services;
	
	Action(APIContext actionConfig, String[] urlParams, String uri, List<Interceptor> interceptors, List<Service> services){
		this.apiContext 		= actionConfig;
		this.urlParams 		= urlParams;
		this.uri			= uri;
		this.interceptors 	= interceptors;
		this.services		= services;
	}
}
