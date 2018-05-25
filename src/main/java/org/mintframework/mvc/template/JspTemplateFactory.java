package org.mintframework.mvc.template;

import java.util.logging.Logger;

import org.mintframework.util.PropertiesMap;

/**
 * TemplateFactory which uses JSP.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:17:18 
 *
 */
public class JspTemplateFactory extends TemplateFactory {
	private Logger log = Logger.getLogger(this.getClass().getName());

    public Template loadTemplate(String path) throws Exception {
        return new JspTemplate(path);
    }

    public void init(PropertiesMap config) {
        log.info("JspTemplateFactory init ok.");
    }

}
