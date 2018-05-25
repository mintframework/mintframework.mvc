package org.mintframework.mvc.core;

import java.io.File;
import java.io.IOException;

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
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:12:08 
 *
 */
class StaticFileHandler {
	private final ServletContext servletContext;
	private final String cacheControl;
	private final Boolean lastModifiedCheck;
	private final String CONTEXTPATH;
	private String staticBase;
	
	//private final String webRoot;

	/**
	 * @param config
	 * @throws ServletException
	 */
	StaticFileHandler(ServletConfig config, PropertiesMap pmap) throws ServletException {
		this.servletContext = config.getServletContext();
		this.CONTEXTPATH = config.getServletContext().getContextPath();
		
		staticBase = pmap.get("mint.mvc.static-base");
		
		/*
		 * 自定义静态文件的存储路径
		 */
		String WEBROOT;
		String sep = File.separator;
		WEBROOT = (config.getServletContext().getRealPath("")).replace(sep, "/");
		String WEBINFPATH = (WEBROOT+sep+"WEB-INF").replace(sep, "/");
		
		if(staticBase==null || "".equals(staticBase.trim()) || (staticBase.trim().toUpperCase()).startsWith(WEBINFPATH.toUpperCase())){
			staticBase = WEBROOT;
		}
		staticBase = staticBase.replace(sep, "/");
		
		if(staticBase.endsWith("/")){
			staticBase = staticBase.substring(0, staticBase.length()-1);
		}
		
		/*
		 * 静态文件的缓存设置
		 */
		String 	cc = pmap.get("mint.mvc.static-file-cache-control"),
				lmfc = pmap.get("mint.mvc.static-file-last-modified-check");
		
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

		int n = url.indexOf('?');
		if (n!=(-1)){
			url = url.substring(0, n);
		}
		
		n = url.indexOf('#');
		if (n!=(-1)){
			url = url.substring(0, n);
		}
		
		File f = new File(staticBase+url);
		if (! f.isFile()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		FileRenderer fr = new FileRenderer(f);
		fr.setCacheControl(cacheControl);
		fr.setLastModifiedCheck(lastModifiedCheck);
		fr.setConnection("keep-alive");
		
		try {
			fr.render(servletContext, request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
