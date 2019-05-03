package org.mintframework.mvc.core;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mintframework.mvc.renderer.FileRenderer;
import org.mintframework.util.PropertiesMap;

/**
 * 
 * Handle static file request.
 * 
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:12:08 
 *
 */
class StaticFileHandler {
	private final ServletContext servletContext;
	private final String cacheControl;
	private final Boolean lastModifiedCheck;
	private final String CONTEXTPATH;
	private final String sep = File.separator;
	private Set<String> staticBases = new HashSet<>();
	
	private Logger log = Logger.getLogger(this.getClass().getName());

	/**
	 * @param config
	 * @throws ServletException
	 */
	StaticFileHandler(ServletConfig config, PropertiesMap pmap) throws ServletException {
		this.servletContext = config.getServletContext();
		this.CONTEXTPATH = config.getServletContext().getContextPath();
		
		String staticBaseStr = pmap.get("mint.mvc.staticBases");

		/*
		 * 自定义静态文件的存储路径
		 */
		String WEBROOT = (config.getServletContext().getRealPath("")).replace(sep, "/");
		if(staticBaseStr==null || staticBaseStr.isBlank()) {
			staticBases.add(getStaticBase(null, WEBROOT));
		} else {
			for(String s: staticBaseStr.split(";")) {
				staticBases.add(getStaticBase(s, WEBROOT));
			}
		}
		
		log.info("set staticBase to : "+ staticBases);
		
		/*
		 * 静态文件的缓存设置
		 */
		String 	cc = pmap.get("mint.mvc.staticFileCacheControl"),
				lmfc = pmap.get("mint.mvc.mint.mvc.staticFileLastModifiedCheck");
		
		if(cc != null){
			cacheControl = cc;
		} else {
			cacheControl = "max-age=600";
		}
		
		if(lmfc != null){
			lastModifiedCheck = Boolean.parseBoolean(lmfc);
		} else {
			lastModifiedCheck = true;
		}
	}
	
	private String getStaticBase(String staticBaseStr, String WEBROOT) {
		/*
		 * 自定义静态文件的存储路径
		 */
		String WEBINFPATH = (WEBROOT+sep+"WEB-INF").replace(sep, "/");
		
		if(staticBaseStr==null || "".equals(staticBaseStr.trim()) || (staticBaseStr.trim().toUpperCase()).startsWith(WEBINFPATH.toUpperCase())){
			staticBaseStr = WEBROOT;
		} else if(staticBaseStr.startsWith("webroot:/")){
			staticBaseStr = staticBaseStr.replace("webroot:/", WEBROOT);
		} else if(staticBaseStr.startsWith("webroot:")) {
			staticBaseStr = staticBaseStr.replace("webroot:", WEBROOT);
		}
		staticBaseStr = staticBaseStr.replace(sep, "/");
		
		if(staticBaseStr.endsWith("/")){
			staticBaseStr = staticBaseStr.substring(0, staticBaseStr.length()-1);
		} else if(staticBaseStr.endsWith("//")) {
			staticBaseStr = staticBaseStr.substring(0, staticBaseStr.length()-2);
		}
		
		return staticBaseStr;
	}

	/**
	 * 响应静态文件请求
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getRequestURI();
		url = url.substring(CONTEXTPATH.length());
		
		if(url.toUpperCase().startsWith("/WEB-INF")){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		for(String staticBase : staticBases) {
			File f = new File(staticBase+url);
			if(f.isFile()) {
				FileRenderer fr = new FileRenderer(f);
				fr.setCacheControl(cacheControl);
				fr.setLastModifiedCheck(lastModifiedCheck);
				fr.setConnection("keep-alive");
				
				try {
					fr.render(servletContext, request, response);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
		}
		
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
}
