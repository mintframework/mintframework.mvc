package org.mintframework.mvc.core;

public class APIConfig {
	public final String[] urls;
	public final String id;
	public final String name;
	public final String[] method;
	public final String protocol;
	public final String desc;
	public final String[] tags;
	public final Class<?>[] parameterTypes;
	public final String[] parameterNames;

	/**
	 * @param id
	 * @param name
	 * @param method
	 * @param protocol
	 * @param desc
	 * @param tags
	 */
	APIConfig(String[] urls, String id, String name, String[] method, String protocol, String desc, String[] tags, Class<?>[] paramTypes, String[] parameterNames){
		this.urls = urls;
		this.id = id;
		this.name = name;
		this.method = method;
		this.protocol = protocol;
		this.desc = desc;
		this.tags = tags;
		this.parameterTypes = paramTypes;
		this.parameterNames = parameterNames;
	}
}
