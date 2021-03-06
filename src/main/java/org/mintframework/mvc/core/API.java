package org.mintframework.mvc.core;

import java.util.List;

/**
 * 
 * action的配置信息
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午7:43:05 
 *
 */
class API {
	final APIContext 			apiContext;
	final String[] 				urlParams;
	final String 				uri;
	final List<Interceptor> 	interceptors;
	final List<Service>			services;
	
	API(APIContext actionConfig, String[] urlParams, String uri, List<Interceptor> interceptors, List<Service> services){
		this.uri			= uri;
		this.services		= services;
		this.urlParams 		= urlParams;
		this.apiContext 	= actionConfig;
		this.interceptors 	= interceptors;
	}
}
