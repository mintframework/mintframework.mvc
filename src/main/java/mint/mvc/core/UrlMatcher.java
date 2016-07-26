package mint.mvc.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mint.mvc.converter.ConverterFactory;
import mint.mvc.util.GetArgumentName;

/**
 * 
 * Match URL by regular expression<br/>
 * The maximum number of parameters is 10(from 0 to 10).
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:12:42
 *
 */
final class UrlMatcher {
	static final String[] EMPTY_STRINGS = new String[0];
	static final String SAFE_CHARS = "/$-_.+!*'(),";
	private Logger log = Logger.getLogger(this.getClass().getName());
	final String url;
	/**
	 * 匹配url的正则
	 */
	final Pattern pattern;
	final int[] urlArgumentOrder;

	/**
	 * Build UrlMatcher by given url like "/blog/{name}/{id}".
	 * 
	 * @param url
	 *            Url may contains {name}, {id}, ... {..}.
	 */
	UrlMatcher(String url, Method actMethod) {
		List<String> argNames = GetArgumentName.getArgumentNames(actMethod);
		this.url = url;

		List<String> urlPList = new ArrayList<String>();
		String urlParamName;

		Matcher matcher = Pattern.compile("\\{[^\\{^\\}]+\\}").matcher(url);

		/**
		 * 匹配如: id:12345, name:xxxx 这样的字符串
		 */
		Pattern urlParameterReg = Pattern.compile("^(\\w+)[:](.+)");

		while (matcher.find()) {
			urlParamName = matcher.group(0).replace("{", "").replace("}", "");

			// ":"后面的字符串应该是一个正则表达式。用来限定url该部分所能匹配的字符串格式
			if (urlParamName.contains(":")) {
				Matcher m = urlParameterReg.matcher(urlParamName);
				if (m.matches()) {
					urlParamName = m.group(1);
				} else {
					throw new ConfigException("inexact regex parameter name -> " + urlParamName);
				}
			}

			/* 检查url有没有包含相同参数名 */
			if (urlPList.contains(urlParamName)) {
				throw new ConfigException("uri包含同名参数");
			}

			urlPList.add(urlParamName);
		}

		int len = urlPList.size();
		this.urlArgumentOrder = new int[len];

		for (int i = 0, j; i < len; i++) {

			if (urlPList != null) {
				urlParamName = urlPList.get(i);
				j = argNames.indexOf(urlParamName);

				/* 如果url中的参数名在action方法中找不到，则抛出异常 */
				if (j > -1) {
					urlArgumentOrder[i] = j;
				} else {
					// throw new ConfigException("action 方法:" +
					// actMethod.toGenericString() + " 不含有" + uPName + "参数");
				}
			}
		}

		/**
		 * TODO url匹配部分需要谨慎对待，以防出现安全问题。暂时默认web容器转发过来的请求是符合rfc标准的
		 */
		if (checkIsActionMethod(actMethod)) {
			matcher.reset();
			StringBuffer sb = new StringBuffer();

			sb.append("^");
			while (matcher.find()) {
				Matcher m = urlParameterReg.matcher(matcher.group(0).replace("{", "").replace("}", ""));

				if (m.matches()) {
					matcher.appendReplacement(sb, "(" + m.group(2) + ")");
				} else {
					/*
					 * 这里只保证group中不包含‘/’，其他的不安全字符，假定web服务器在接受请求时已经按rfc的定义校验过
					 */
					matcher.appendReplacement(sb, "([^/]+)");
				}

			}
			matcher.appendTail(sb);

			/* "/user/name" 和 "/user/name/" 都可以匹配 */
			this.pattern = Pattern.compile(sb.toString() + "[/]?$");
		} else {
			this.pattern = null;
		}
	}

	/**
	 * 检查是否合法的action方法
	 * 
	 * @param method
	 * @return
	 */
	private boolean checkIsActionMethod(Method method) {
		/* check if the url argument type can be convert */
		Class<?>[] argTypes = method.getParameterTypes();
		ConverterFactory cvFact = new ConverterFactory();

		Class<?> argType;
		for (int argIndex : urlArgumentOrder) {
			argType = argTypes[argIndex];
			if (!cvFact.canConvert(argTypes[argIndex])) {
				log.warning(method.toGenericString() + " include unsupported uri argument type"
						+ argType.getName() + ",uri argument support only  primitive type or String");
				return false;
			}
			argTypes[argIndex] = null;
		}

		/*
		 * url参数必须为基础类型，比如int, long etc. 非url参数不许为基础类型。这主要是为了防止 参数空指针的问题
		 */
		for (Class<?> type : argTypes) {
			if (type != null && type.isPrimitive()) {
				throw new ConfigException(method.toGenericString()+ "除了uri参数之外，所有action方法参数都不能是基础类型");
			}
		}
		return true;
	}

	/**
	 * Test if the url is match the regex. If matched, the parameters are
	 * returned as String[] array, otherwise, null is returned.
	 * 
	 * @param url
	 *            The target url.
	 * @return String[] array or null if url is not match.
	 */
	String[] getUrlParameters(String url) {
		Matcher m = pattern.matcher(url);
		if (!m.matches()) {
			return null;
		}
		if (urlArgumentOrder.length == 0) {
			return EMPTY_STRINGS;
		}
		String[] params = new String[urlArgumentOrder.length];
		for (int i = 0; i < urlArgumentOrder.length; i++) {
			params[i] = m.group(i + 1);
		}
		return params;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof UrlMatcher) {
			return ((UrlMatcher) obj).url.equals(this.url);
		}
		return false;
	}

	/* 简单类型的hashCode效率较高 */
	@Override
	public int hashCode() {
		return url.hashCode();
	}
}