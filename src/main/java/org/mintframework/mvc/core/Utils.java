package org.mintframework.mvc.core;

import java.util.logging.Logger;

import org.mintframework.mvc.template.TemplateFactory;

/**
 * Utils for create ContainerFactory, TemplateFactory, etc.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:13:20
 *
 */
class Utils {
	TemplateFactory createTemplateFactory(String name) {
		TemplateFactory tf = tryInitTemplateFactory(TemplateFactory.class.getPackage().getName()
			+ "."
			+ name
			+ TemplateFactory.class.getSimpleName());
		
		if (tf == null) {
			Logger.getLogger(Utils.class.getName()).warning(
					"Cannot init template factory '" + name + "'.");
		}
		return tf;
	}

	TemplateFactory tryInitTemplateFactory(String clazz) {
		try {
			Object obj = Class.forName(clazz).getConstructor().newInstance();
			if (obj instanceof TemplateFactory)
				return (TemplateFactory) obj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
