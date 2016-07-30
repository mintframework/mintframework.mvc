package mint.mvc.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mint.mvc.annotation.MultipartConfig;
import mint.mvc.converter.ConverterFactory;
import mint.mvc.core.upload.FileUpload;
import mint.mvc.core.upload.MultipartHttpServletRequest;
import mint.mvc.core.upload.MultipartParameter;
import mint.mvc.renderer.ErrorRender;
import mint.mvc.renderer.Renderer;
import mint.mvc.renderer.TextRenderer;
import mint.mvc.template.JspTemplateFactory;
import mint.mvc.template.TemplateFactory;

/**
 * action的执行者。将请求传递过来的参数经过友好的封装，
 * 整理成action方法的参数，然后调用action方法，并且对
 * 方法的返回值做处理后返回
 *  
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午7:44:15 
 *
 */
class ActionExecutor {
	private Logger log = Logger.getLogger(this.getClass().getName());
	private ServletContext servletContext;
	private ExceptionListener exceptionListener;

	private ConverterFactory converterFactory = new ConverterFactory();
	
	private String uploadTemp;
	
	private boolean trimString = false;
	/**
	 * @param config
	 * @throws ServletException
	 */
	void init(Config config) throws ServletException {
		log.info("Init Dispatcher...");
		this.servletContext = config.getServletContext();
		uploadTemp = config.getInitParameter("uploadTemp");
		trimString = Boolean.valueOf(config.getInitParameter("trimString"));
		
		String exHandler = config.getInitParameter("exceptionListener");
		if(exHandler!=null && !exHandler.equals("")){
			try {
				exceptionListener = (ExceptionListener) Class.forName(exHandler).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				log.warning("can not init custom exceptionListener");
				e.printStackTrace();
			}
		} else {
			exceptionListener = new DefaultExceptionListener();
		}
		
		try {
			initAll(config);
		} catch (Exception e) {
			throw new ServletException("Dispatcher init failed.", e);
		}
	}
	
	/**
	 * 调用action方法
	 * @param apiContext
	 * @param arguments
	 * @return
	 * @throws Exception
	 */
	Object executeActionMethod(ApiContext apiContext, Object[] arguments) throws Exception {
		//@Required 注解标注的参数不能为空
		for(int i=apiContext.requires.length-1; i>-1; i--){
			if(apiContext.requires[i] && arguments[i]==null){
				return new ErrorRender(403);
			}
		}
		
		try {
			return apiContext.actionMethod.invoke(apiContext.instance, arguments);
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t != null && t instanceof Exception){
				throw (Exception) t;
			}
			throw e;
		}
	}
	
	void executeAction(HttpServletRequest request, HttpServletResponse response, Action action) throws ServletException, IOException{
		ActionContext.setActionContext(servletContext, request, response);
		
		/*处理上传请求*/
		if(action.actionConfig != null){
			String contentType = request.getContentType();
			if(contentType != null && contentType.indexOf("multipart/form-data") >= 0){
				/*避免“上传任意文件”*/
				if(action.actionConfig.isMultipartAction){
					MultipartConfig multipartConfig = action.actionConfig.multipartConfig;
					
					if(multipartConfig.maxRequestSize() <= 0 || (request.getContentLength() < multipartConfig.maxRequestSize())){
						/*正在上传文件*/
						FileUpload.upload(uploadTemp, multipartConfig.attributeName(), multipartConfig.limitSize(), request);
						//上传的文件通过attribute带出来
						if(request.getAttribute(multipartConfig.attributeName()) != null){
							String attributeName = multipartConfig.attributeName();
							MultipartHttpServletRequest r = new MultipartHttpServletRequest(request, (MultipartParameter[]) request.getAttribute(attributeName), attributeName);
							request = r;
						}
					} else {
						log.warning("request body is too large");
					}
				}
			}
		}
		
		/* apply interceptor chain */
		InterceptorChainImpl interceptorChain = null;
		if(action.interceptors!=null){
			if(action.actionConfig!=null){
				interceptorChain = new InterceptorChainImpl(action.interceptors, action.actionConfig.module, action.actionConfig.api);
			} else {
				interceptorChain = new InterceptorChainImpl(action.interceptors, null, null);
			}
			
			try {
				interceptorChain.doInterceptor(ActionContext.getActionContext());
			} catch (Exception e) {
				ActionContext.removeActionContext();
				handleException(request, response, e);
			}
		}
		
		//apply service chain
		ServiceChainImpl serviceChain = null;
		if(action.services!=null && (interceptorChain==null || interceptorChain.isPass())){
			if(action.actionConfig!=null){
				serviceChain = new ServiceChainImpl(action.services, action.actionConfig.module, action.actionConfig.api);
			} else {
				serviceChain = new ServiceChainImpl(action.services, null, null);
			}
			
			try {
				serviceChain.doService(ActionContext.getActionContext());
			} catch (Exception e) {
				ActionContext.removeActionContext();
				handleException(request, response, e);
			}
		}
		
		
		//有效的请求
		if ((interceptorChain==null || interceptorChain.isPass()) && (serviceChain==null || serviceChain.isPass())) {
			if(action.actionConfig!=null){
				/*调用action方法方法的参数*/
				Object[] arguments = initArguments(request, response, action);
				
				try {
					//调用action方法并处理action返回的结果
					handleResult(request, response, executeActionMethod(action.actionConfig, arguments), action.actionConfig);
				} catch (Exception e) {
					ActionContext.removeActionContext();
					handleException(request, response, e);
				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}
	}
	
	/**
	 * @param config
	 */
	void initAll(Config config){
		initTemplateFactory(config);
	}
	
	/**
	 * @param config
	 */
	private void initTemplateFactory(Config config) {
		String name = config.getInitParameter("template");
		if (name == null) {
			name = JspTemplateFactory.class.getName();
			log.info("No template factory specified. Default to '" + name + "'.");
		}
		Utils util = new Utils();
		TemplateFactory tf = util.createTemplateFactory(name);
		tf.init(config);
		log.info("Template factory '" + tf.getClass().getName() + "' init ok.");
		TemplateFactory.setTemplateFactory(tf);
	}
	
	/**
	 * prepare arguments for action method by parameter in request.
	 * 
	 * @param req
	 * @param action
	 * @param matcher
	 */
	private Object[] initArguments(HttpServletRequest req, HttpServletResponse resp, Action action) {
		ApiContext actionConfig = action.actionConfig;
		
		Object[] arguments = new Object[actionConfig.argumentTypes.length];

		/* 从url获取参数（parameter）初始化action 方法参数（argument） */
		String[] urlArgs = action.urlParams;
		
		int argIndex;
		int[] urlArgOrder = actionConfig.urlArgumentOrder; // 对应位置的参数接受从url中分离出来的参数
		
		String str;
		for (int i = 0; i < urlArgs.length; i++) {
			argIndex = urlArgOrder[i];
			
			str = urlArgs[i];
			
			/*
			 * 如果参数中有“%”，说明该参数被编码过，需要解码。目前只支持utf8编码的解码
			 */
			if(str.contains("%")){
				try {
					str = URLDecoder.decode(urlArgs[i],  "utf8");
				} catch (UnsupportedEncodingException e) { }
			}
			
			if(trimString){
				str = str.trim();
			}
			
			arguments[argIndex] = converterFactory.convert(actionConfig.argumentTypes[argIndex], str);
		}

		/* 从请求参数中初始化action方法参数(argument) */
		Map<String, String[]> paramMap = req.getParameterMap();
		Object arguInstance;
		Map<String, ParameterInjector> injectors = actionConfig.injectors;
		ParameterInjector injector;
		
		try {
			for (String paramName : paramMap.keySet()) {
				injector = injectors.get(paramName);
				
				if (injector != null) {
					if (injector.needInject) {
						arguInstance = arguments[injector.argIndex];
						if (arguInstance == null) {
							/* instantiate a instance the first time you use */
							arguInstance = injector.argType.newInstance();
							arguments[injector.argIndex] = arguInstance;
						}
						str = paramMap.get(paramName)[0];
						
						if(trimString){
							str = str.trim();
						}
						
						arguments[injector.argIndex] = injector.inject(arguInstance, str, paramName);
					} else {
						if (injector.isArray) {
							/*
							 * 支持数组
							 */
							String array[] = paramMap.get(paramName);
							int len = array.length;
							Class<?> t = injector.argType.getComponentType();
							Object arr = Array.newInstance(t, len);
							for (int i = 0; i < len; i++) {
								str = array[i];
								
								if(trimString){
									str = str.trim();
								}
								
								Array.set(arr, i, converterFactory.convert(t, str));
							}
	
							arguments[injector.argIndex] = arr;
						} else {
							/* 简单类型直接转换 */
							
							arguments[injector.argIndex] = converterFactory.convert(injector.argType, paramMap.get(paramName)[0]);
						}
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
		
		/*
		 * 从request.getAttributeNames()初始化参数
		 */
		Enumeration<String> attributes = req.getAttributeNames();
		Object attribute;
		String attributeName;
		injector = null;
		injectors = actionConfig.injectors;
		while(attributes.hasMoreElements()){
			attributeName = attributes.nextElement();
			attribute = req.getAttribute(attributeName);
			
			if(attribute != null){
				injector = injectors.get(attributeName);
				/*attributeName and attributeType 匹配时，进行参数替换*/
				if(injector != null && injector.argType.isInstance(attribute)){
					arguments[injectors.get(attributeName).argIndex] = attribute;
				}
			}
		}
		
		/* 初始化内置参数（request, response, secction, cookies） */
		if (actionConfig.builtInArguments != null) {
			for (BuildInArgumentInfo info : actionConfig.builtInArguments) {
				switch (info.typeCode) {
					case 0:{
						arguments[info.argIndex] = req;
						break;
					}
					case 1:{
						arguments[info.argIndex] = resp;
						break;
					}	
					case 2:{
						arguments[info.argIndex] = req.getSession();
						break;
					}
					case 3:{
						arguments[info.argIndex] = req.getCookies();
						break;
					}
					case 4:{
						Cookie[] cookies = req.getCookies();
						
						if(cookies!=null){
							for(Cookie cookie : cookies){
								if(cookie.getName().equals(info.argName)){
									arguments[info.argIndex] = cookie;
									break;
								}
							}
						}
						break;
					}
					
					default:
						break;
				}
			}
		}

		return arguments;
	}

	/**
	 *  处理action返回的结果。当方法出现异常时，处理异常
	 */
	private void handleResult(HttpServletRequest request, HttpServletResponse response, Object result, ApiContext actionConfig) throws Exception {
		if (result == null) {
			return;
		}
		
		/*处理模板结果*/
		if (result instanceof Renderer) {
			((Renderer) result).render(servletContext, request, response);
			return;
		}
		
		new TextRenderer(result+"").render(servletContext, request, response);
		return;
	}
	
	/**
	 * 处理action执行时发生的异常
	 * @param request
	 * @param response
	 * @param ex
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleException(HttpServletRequest request, HttpServletResponse response, Exception ex) throws ServletException, IOException {
		try {
			exceptionListener.handle(request, response, ex);
		} catch (ServletException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
