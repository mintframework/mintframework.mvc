package org.mintframework.mvc.core;

import java.util.Set;

import org.mintframework.mvc.annotation.InterceptorConfig;
import org.mintframework.mvc.annotation.ApiService;

public interface StartupListener {
	/**
	 * 在mvc启动阶段将所有的模块和api配置参数传递过来，
	 * 供开发者进行处理
	 * @param modules
	 */
	public void report(Set<ModuleConfig> modules, Set<ApiService> services, Set<InterceptorConfig> interceptors);
}
