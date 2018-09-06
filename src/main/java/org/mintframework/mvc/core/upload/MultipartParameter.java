package org.mintframework.mvc.core.upload;

import javax.servlet.http.Part;

/** 
 * 
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:31:59 
 *  
 */
public interface  MultipartParameter extends Part{
	/**
	 * 是否文件
	 * @return
	 */
	boolean isFile();
	
	/**
	 * 获取非文件参数值
	 * @return
	 */
	String getParameterValue();
	
	/**
	 * 获取临时文件
	 * @return
	 */
	public TempFile getTempFile();
	
	/**
	 * 获取客户端上传文件的文件名
	 * @return
	 */
	public String getFilename();
}
