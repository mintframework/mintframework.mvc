package org.mintframework.mvc.renderer;

import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Render http response as text. This is usually used to render HTML, JavaScript, 
 * CSS or any text type.
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:15:39 
 *
 */
public class TextRenderer extends Renderer {

    private String characterEncoding;
    private String text;

    public TextRenderer(String text) {
        this.text = text;
    }

    /**
     * 
     * @param text
     * @param contentType 默认为"text/html"
     * @param characterEncoding 默认为"UTF-8"
     */
    public TextRenderer(String text, String contentType, String characterEncoding) {
        this.text = text;
        this.contentType = contentType;
        this.characterEncoding = characterEncoding;
    }

    @Override
    public void render(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuilder sb = new StringBuilder(64);
        sb.append(contentType==null ? "text/html" : contentType)
          .append(";charset=")
          .append(characterEncoding==null ? "UTF-8" : characterEncoding);
        response.setContentType(sb.toString());
        PrintWriter pw = response.getWriter();
        pw.write(text);
        pw.flush();
    }
}
