package org.mintframework.mvc.renderer;

import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author liangwei
 *
 */
public class ErrorRender extends Renderer{
	private int status;
	private String errorText;
	
	public ErrorRender(int status, String errorText){
		this.status = status;
		this.errorText = errorText;
	}
	
	@Override
	public void render(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.sendError(status);
		PrintWriter pw = response.getWriter();
        pw.write(errorText);
        pw.flush();
	}
	
}
