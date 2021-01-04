package org.mintframework.mvc.core;

public class APIConfig {
	public final String id;
	public final String desc;
	public final String name;
	public final String[] urls;
	public final String[] tags;
	public final String[] method;
	public final String protocol;
	public final String[] parameterNames;
	public final Class<?>[] parameterTypes;

	/**
	 * API 的元信息
	 * @param urls
	 * @param id
	 * @param name
	 * @param method
	 * @param protocol
	 * @param desc
	 * @param tags
	 * @param paramTypes
	 * @param parameterNames
	 */
	APIConfig(String[] urls, String id, String name, String[] method, String protocol, String desc, String[] tags, Class<?>[] paramTypes, String[] parameterNames){
		this.id = id;
		this.urls = urls;
		this.name = name;
		this.desc = desc;
		this.tags = tags;
		this.method = method;
		this.protocol = protocol;
		this.parameterTypes = paramTypes;
		this.parameterNames = parameterNames;
	}
}
