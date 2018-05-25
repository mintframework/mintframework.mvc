package org.mintframework.mvc.core;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Holds all Servlet objects in ThreadLocal.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author liangwei
 */
public final class RequestContext {

	static final ThreadLocal<RequestContext> actionContexts = new ThreadLocal<RequestContext>();

	private static String webRoot;
	
	private ServletContext context;
	private HttpServletRequest request;
	private HttpServletResponse response;

	/**
	 * Return the ServletContext of current web application.
	 */
	public ServletContext getServletContext() {
		return context;
	}

	/**
 	* Return current request object.
	 */
	public HttpServletRequest getHttpServletRequest() {
		return request;
	}

	/**
	 * Return current response object.
	 */
	public HttpServletResponse getHttpServletResponse() {
		return response;
	}

	/**
	 * Return current session object.
	 */
	public HttpSession getHttpSession() {
		return request.getSession();
	}

	/**
	 * Get current ActionContext object.
	 */
	public static RequestContext getActionContext() {
		return actionContexts.get();
	}

	static void setActionContext(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
		RequestContext ctx = new RequestContext();
		ctx.context = context;
		ctx.request = request;
		ctx.response = response;
		actionContexts.set(ctx);
	}

	static void removeActionContext() {
		actionContexts.remove();
	}

	/**
	 * 获得应用的部署path
	 * @return
	 */
	public static String getWebRoot() {
		return webRoot;
	}

	static void setWebRoot(String webRoot) {
		RequestContext.webRoot = webRoot;
	}
}
