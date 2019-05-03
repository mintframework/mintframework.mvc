package org.mintframework.mvc.core;

import java.util.Set;

import javax.servlet.ServletContext;

import org.mintframework.mvc.annotation.InterceptorConfig;
import org.mintframework.mvc.annotation.ApiService;

public interface StartupListener {
	/**
	 * 在mvc启动阶段将所有的模块和api配置参数传递过来，
	 * 供开发者进行处理
	 * @param context
	 * @param modules
	 * @param serviceConfigs
	 * @param interceptorConfig
	 */
	public void report(ServletContext context, Set<ModuleConfig> modules, Set<ApiService> serviceConfigs,
			Set<InterceptorConfig> interceptorConfig);
}
