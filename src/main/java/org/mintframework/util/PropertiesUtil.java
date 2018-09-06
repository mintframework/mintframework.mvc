package org.mintframework.util;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Properties;

public class PropertiesUtil {
	private static Logger log = Logger.getLogger(PropertiesUtil.class.getName());
	/**
	 * load Properties from class path
	 * @param propertieName
	 * @param encode
	 * @param mintInclude
	 * @return
	 */
	public static PropertiesMap load(String propertieName, String encode, Boolean mintInclude) {
		URL path = PropertiesUtil.class.getClassLoader().getResource(propertieName);
		
		PropertiesMap pMap = loadFromAbsolutePath(path.toString(), encode, false);
		
		if(mintInclude && pMap.get("properties.include")!=null) {
			String[] names = pMap.get("properties.include").toString().split(";");
			for(String n:names) {
				if(n.trim()=="") continue;
				
				path = PropertiesUtil.class.getClassLoader().getResource(n.trim());
				
				if(path==null) {
					log.warning("include properties file not found : " + n);
					continue;
				}
				
				try {
					PropertiesMap includePMap = loadFromAbsolutePath(path.toString(), encode, false);
					for(Entry<String, String> k: includePMap.entrySet()) {
						pMap.put(k.getKey(), k.getValue());
					}
				} catch(Exception e) {
					log.warning("fail to include properties file : " + path.toString());
					e.printStackTrace();
				}
			}
		}
		
		return pMap;
	}
	
	
	/**
	 * load from labsolute path
	 * @param path
	 * @param encode
	 * @param enableInclude
	 * @return
	 */
	public static PropertiesMap loadFromAbsolutePath(String path, String encode, Boolean enableInclude) {
		Properties props=new Properties();
		if(encode==null || encode.trim().equals("")) {
			encode = "UTF-8";
		}
		
		PropertiesMap pMap = new PropertiesMap();
		
		try {
			path = new URL(path).getPath();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		try {
			props.load(new InputStreamReader(new FileInputStream(path), encode));
			
			Iterator<Entry<Object, Object>> it=props.entrySet().iterator();
			
			while(it.hasNext()){
			    Entry<Object, Object> entry= it.next();
			    String key = (String) entry.getKey();
			    String value = (String) entry.getValue();
			    pMap.put(key, value);
			}
			
			if(enableInclude && pMap.get("properties.include")!=null) {
				String[] names = pMap.get("properties.include").toString().split(";");
				for(String n:names) {
					if(n.trim()=="") {
						continue;
					}
					
					try {
						props.load(new InputStreamReader(new FileInputStream(path), encode));
					} catch(Exception e) {
						log.warning("fail to include properties file : " + path.toString());
						e.printStackTrace();
					}
					Iterator<Entry<Object, Object>> kv=props.entrySet().iterator();
					while(kv.hasNext()){
					    Entry<Object, Object> entry= kv.next();
					    String key = (String) entry.getKey();
					    String value = (String) entry.getValue();
					    pMap.put(key, value);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return pMap;
	}
}
