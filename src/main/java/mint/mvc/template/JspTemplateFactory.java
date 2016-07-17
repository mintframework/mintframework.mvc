package mint.mvc.template;

import java.util.logging.Logger;

import mint.mvc.core.Config;

/**
 * TemplateFactory which uses JSP.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:17:18 
 *
 */
public class JspTemplateFactory extends TemplateFactory {
	private Logger log = Logger.getLogger(this.getClass().getName());

    public Template loadTemplate(String path) throws Exception {
        return new JspTemplate(path);
    }

    public void init(Config config) {
        log.info("JspTemplateFactory init ok.");
    }

}
