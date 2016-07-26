package mint.mvc.renderer;

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
	
	public ErrorRender(int status){
		this.status = status;
	}
	
	@Override
	public void render(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.sendError(status);
	}
	
}
