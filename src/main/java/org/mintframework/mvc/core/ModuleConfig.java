package org.mintframework.mvc.core;

import java.util.Set;

public class ModuleConfig {
	public final String url;
	public final String id;
	public final String desc;
	public final String[] tags;
	public final String name;
	public final Set<APIConfig> apiSet;
	
	ModuleConfig(String url, String id, String name, String desc, String[] tags, Set<APIConfig> apis){
		this.url = url;
		this.id = id;
		this.name = name;
		this.desc = desc;
		this.tags = tags;
		this.apiSet = apis;
	}
}
