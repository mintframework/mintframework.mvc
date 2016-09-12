package mint.mvc.core;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * DispatcherServlet must be mapped to root URL "/". It handles ALL requests 
 * from clients, and dispatches to appropriate handler to handle each request.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:10:43 
 *
 */
@WebServlet(asyncSupported = true) 
public class ServiceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Logger log = Logger.getLogger(this.getClass().getName());

	private Dispatcher dispatcher;
	private StaticFileHandler staticFileHandler;
	private ApiExecutor actionExecutor;
	
	private boolean handleStatic = true;
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(final ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		log.info("Init ServiceServlet...");
		this.dispatcher = new Dispatcher();
		this.actionExecutor = new ApiExecutor();
		
		Config config = new Config() {
			public String getInitParameter(String name) {
				return servletConfig.getInitParameter(name);
			}

			public ServletContext getServletContext() {
				return servletConfig.getServletContext();
			}
		};
		
		this.handleStatic = Boolean.valueOf(servletConfig.getInitParameter("handleStatic"));
		this.dispatcher.init(config);
		this.actionExecutor.init(config);
		
		if(handleStatic){
			this.staticFileHandler = new StaticFileHandler(servletConfig);
		}
		RequestContext.setWebRoot(servletConfig.getServletContext().getContextPath());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
	 */
	@Override
	public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
		final HttpServletRequest 	httpReq		= (HttpServletRequest) req;
		final HttpServletResponse 	httpResp 	= (HttpServletResponse) resp;
		String						method		= httpReq.getMethod().toLowerCase();
		
		/*进入处理请求*/
		final Action action = dispatcher.dispatch(httpReq, method);
		if (action != null) {
			final AsyncContext asyncContext = httpReq.startAsync();
			
			/*内置baseUrl*/
			StringBuilder baseUrl = new StringBuilder(255);
			baseUrl.append(httpReq.getScheme()).append("://").append(httpReq.getServerName()).append(":").append(httpReq.getServerPort());
			String contextPath = httpReq.getContextPath();
			
			if(contextPath != null){
				baseUrl.append(contextPath).append("/");
			}
			
			httpReq.setAttribute("baseUrl", baseUrl);
			
			/*// 添加监听器监听异步的执行结果
			asyncContext.addListener(new AsyncListener() {
				@Override
				public void onComplete(AsyncEvent event) throws IOException {
					//在这里处理正常结束的逻辑
				}

				@Override
				public void onTimeout(AsyncEvent event) throws IOException {
					//在这里处理超时的逻辑
				}

				@Override
				public void onError(AsyncEvent event) throws IOException {
					// 在这里处理出错的逻辑
				}

				@Override
				public void onStartAsync(AsyncEvent event) throws IOException {
					//开始异步线程执行动态请求
				}
			});*/
			
			//设置超时的时间，到了时间以后，会回调onTimeout的方法
			asyncContext.setTimeout(0);
			
			// 在这里启动，传入一个Runnable对象，服务器会把此Runnable对象放在线程池里面执行
			asyncContext.start(new Runnable() {
				@Override
				public void run() {
					try {
						actionExecutor.executeAction(httpReq, httpResp, action);
					} catch (ServletException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					// 在这里做耗时的操作，如果做完，则调用complete方法通知回调，异步处理结束了
					asyncContext.complete();
				}
			});
		} else if(handleStatic && "get".equals(method)){
			staticFileHandler.handle(httpReq, httpResp);
		} else {
			httpResp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	public void destroy() {
		log.info("Destroy DispatcherServlet...");
		this.dispatcher.destroy();
	}
}