package mint.mvc.core;

import java.util.Set;

import mint.mvc.annotation.InterceptorConfig;
import mint.mvc.annotation.ServiceConfig;

public interface ComponentReportor {
	/**
	 * 在mvc启动阶段将所有的模块和api配置参数传递过来，
	 * 供开发者进行处理
	 * @param modules
	 */
	public void report(Set<ModuleConfig> modules, Set<ServiceConfig> services, Set<InterceptorConfig> interceptors);
}
