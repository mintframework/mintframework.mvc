package org.mintframework.mvc.core.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/** 
 * 封装多媒体请求参数的类
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:29:10 
 *  
 */
public class DefaultMultipartParameter implements MultipartParameter{
	static Logger logger = Logger.getLogger(MultipartParameter.class.getName());
	
	boolean isFile 			= false;
	MintTempFile 	tempFile 		= null;
	
	String	name			= null;
	String	filename		= null;
	String 	parameterValue 	= null;
	String	contentType		= null;
	
	Map<String, String> headers = new HashMap<String, String>();
	
	
	@Override
	public void delete() throws IOException {
		if(tempFile != null){
			if(tempFile.delete()){
				logger.warning("fail to delete temp file");
			};
		}
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getHeader(String headerName) {
		return headers.get(headerName);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return headers.keySet();
	}

	@Override
	public Collection<String> getHeaders(String headerName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if(tempFile != null){
			return new FileInputStream(tempFile);
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getSize() {
		if(tempFile != null){
			return tempFile.length();
		}
		return 0;
	}

	@Override
	public void write(String fileName) throws IOException {
		if(isFile){
			File file = new File(fileName);
			if(!file.createNewFile()) return;
		}
	}

	@Override
	public boolean isFile() {
		return isFile;
	}

	@Override
	public String getParameterValue() {
		return parameterValue;
	}

	@Override
	public MintTempFile getTempFile() {
		return tempFile;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getSubmittedFileName() {
		return null;
	}
}
