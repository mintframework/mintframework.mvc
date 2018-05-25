package org.mintframework.util;

import java.util.HashMap;

public class PropertiesMap extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	public Integer getInteger(String key){
		String value = (String) this.get(key);
		if(value==null){
			return null;
		} else {
			return Integer.valueOf(value);
		}
	}
	
	public Long getLong(String key){
		String value = (String) this.get(key);
		if(value==null){
			return null;
		} else {
			return Long.valueOf(value);
		}
	}
	
	public Float getFloat(String key){
		String value = (String) this.get(key);
		if(value==null){
			return null;
		} else {
			return Float.valueOf(value);
		}
	}
	
	public Double getDouble(String key){
		String value = (String) this.get(key);
		if(value==null){
			return null;
		} else {
			return Double.valueOf(value);
		}
	}
	
	public Short getChar(String key){
		String value = (String) this.get(key);
		if(value==null){
			return null;
		} else {
			return Short.valueOf(value);
		}
	}
	
	public Boolean getBoolean(String key){
		String value = (String) this.get(key);
		if(value==null){
			return null;
		} else {
			return  Boolean.valueOf(value);
		}
	}
}
