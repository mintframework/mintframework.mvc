package mint.mvc.template;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Template interface.
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:17:58 
 *
 */
public interface Template {

    /**
     * Render by template engine.
     * 
     * @param request HttpServletRequest object.
     * @param response HttpServletResponse object.
     * @param model Model as java.util.Map.
     * @throws Exception If render failed.
     */
    void render(HttpServletRequest request, HttpServletResponse response, Map<String, Object> model) throws Exception;
}
