package mint.mvc.renderer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Render http response as binary stream. This is usually used to render PDF, 
 * image, or any binary type.
 * @author Michael Liao (askxuefeng@gmail.com)
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:14:06 
 *
 */
public class FileRenderer extends Renderer {

    private File file;
    private String cacheControl = null;
    private String connection = null;
    private boolean lastModifiedCheck = true;
    
    public FileRenderer() {
    	
    }

    /**
     * 
     * @param file
     */
    public FileRenderer(File file) {
        this.file = file;
    }

    /**
     * 
     * @param file 文件路径
     */
    public FileRenderer(String file) {
        this.file = new File(file);
    }

    @Override
    public void render(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (file == null || !file.exists() || !file.isFile() || file.length()>Integer.MAX_VALUE) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        /*静态文件缓存*/
        if(cacheControl != null){
        	response.setHeader("Cache-Control", cacheControl);
        }
        
        if(connection != null){
        	response.setHeader("Connection", connection);
        }
        
        if(lastModifiedCheck){
        	long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        	long lastModified = file.lastModified();
        	
        	if (ifModifiedSince!=(-1) && ifModifiedSince>=lastModified) { 
        		response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        		return; 
        	} 
        	/*lastModified 精确到毫秒，但是ifModifiedSince只精确到秒*/
        	lastModified = lastModified+1000;
        	response.setDateHeader("Last-Modified", lastModified);
        }
        
        String mime = contentType;
        if (mime==null) {
            mime = context.getMimeType(file.getName());
            if (mime==null) {
                mime = "application/octet-stream";
            }
        }
        
        response.setContentType(mime);
        response.setContentLength((int)file.length());
        InputStream input = null;
        try {
            input = new BufferedInputStream(new FileInputStream(file));
            OutputStream output = response.getOutputStream();
            byte[] buffer = new byte[4096];
            for (;;) {
                int n = input.read(buffer);
                if (n==(-1)){
                    break;
                }
                output.write(buffer, 0, n);
            }
            output.flush();
        } finally {
            if (input!=null) {
                input.close();
            }
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

	/**
	 * @return
	 */
	public String getCacheControl() {
		return cacheControl;
	}

	/**
	 * Cache option, accepted value:
	 * "max-age=[secs]", "no-cache", "private", "must-revalidate" ...
	 * 
	 * @param cacheControl
	 */
	public void setCacheControl(String cacheControl) {
		this.cacheControl = cacheControl;
	}

	public String getConnection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	/**
	 * request server judge whether the document is modified
	 * default to true
	 * 
	 * @return
	 */
	public boolean isLastModifiedCheck() {
		return lastModifiedCheck;
	}

	/**
	 * request server judge whether the document is modified
	 * default to true
	 * 
	 * @param lastModifiedCheck
	 */
	public void setLastModifiedCheck(boolean lastModifiedCheck) {
		this.lastModifiedCheck = lastModifiedCheck;
	}
}
