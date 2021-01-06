package org.mintframework.mvc.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Default exception handler which just print the exception trace on web page.
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年5月2日 下午2:14:02 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
class DefaultExceptionListener implements ExceptionListener {
	static Logger logger = Logger.getLogger(DefaultExceptionListener.class.getName());
	
    /**
     * Handle exception that print stack trace on HTML page.
     * @throws IOException 
     */
    public void handle(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException  {
    	if(response.isCommitted()) {
    		logger.warning("当前请求已经被相应。");
			e.printStackTrace();
		} else {
			PrintWriter pw = response.getWriter();
			pw.write("<html><head><title>Exception</title></head><body><pre>");
			e.printStackTrace(pw);
			pw.write("</pre></body></html>");
			pw.flush();
		}
    }
}
