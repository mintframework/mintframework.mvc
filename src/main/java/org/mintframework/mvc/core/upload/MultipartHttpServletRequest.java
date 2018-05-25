package org.mintframework.mvc.core.upload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;

/** 
 * 封装多媒体请求
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:30:58 
 *  
 */
public class MultipartHttpServletRequest extends HttpServletRequestWrapper{
	private MultipartParameter[] multiParams = null;  
	private Map<String, String[]> parameters = null;

	/**
	 * 将文件上传参数封装进request里
	 * @param request
	 * @param multiParams
	 */
	public MultipartHttpServletRequest(HttpServletRequest request, MultipartParameter[] multiParams, String attributeName) {
		super(request);
		this.multiParams = multiParams;
		
		/*你麻痹的，看你怎么给我锁定*/
		this.parameters = new HashMap<String, String[]>(request.getParameterMap());
		
		if(multiParams!=null && multiParams.length>0){
			Map<String, List<String>> newParams = new HashMap<String, List<String>>();
			List<TempFile> files = new LinkedList<TempFile>();
			
			/*分离出二进制上传的非文件参数和文件参数*/
			for(MultipartParameter mp : multiParams){
				if(mp.isFile()){
					files.add(mp.getTempFile());
				} else {
					if(newParams.get(mp.getName()) != null){
						newParams.get(mp.getName()).add(mp.getParameterValue());
					} else {
						newParams.put(mp.getName(), new ArrayList<String>());
						newParams.get(mp.getName()).add(mp.getParameterValue());
					}
				}
			}
			
			//将文件参数放到attribute内
			if(files.size()>0){
				request.setAttribute(attributeName, files.toArray(new TempFile[files.size()]));
			} else {
				request.setAttribute(attributeName, null);
			}
			
			
			/*将二进制上传的非文件参数添加到parameter中*/
			for(String key : newParams.keySet()){
				String[] oldParam = parameters.get(key);
				List<String> multipartParam = newParams.get(key);

				
				if(oldParam == null){
					parameters.put(key, (String[]) multipartParam.toArray(new String[multipartParam.size()]));
				} else {
					//为了效率不用简单方法
					String[] newParam = new String[oldParam.length + multipartParam.size()];
					for(int i=0; i<oldParam.length; i++){
						newParam[i] = oldParam[i];
					}
					
					for(int i=0; i<multipartParam.size(); i++){
						newParam[i+oldParam.length] = multipartParam.get(i);
					}
					
					parameters.put(key, newParam);
				}
			}
		}
	}
	
	public Part getPart(String name){
		if(name != null){
			for(MultipartParameter mp : multiParams){
				if(name.equals(mp.getName()) && mp.isFile()){
					return mp;
				}
			}
		}
		return null;
	}
	
	public Collection<Part> getParts(){
		List<Part> parts = new ArrayList<Part>();
		
		for(MultipartParameter mp : multiParams){
			if(mp.isFile()){
				parts.add(mp);
			}
		}
		
		return parts;
	}
	
	
	public String getParameter(String name){
		if(parameters.get(name) != null){
			return parameters.get(name)[0];
		}
		
		return null;
	}
	
	public Map<String,String[]>	getParameterMap(){
		return parameters;
	}
	
	/**
	 * 返回所有参数名
	 * @return
	 */
	public Set<String>	getParameterNamesSet(){
		return parameters.keySet();
	}
	
	/**
	 * 获取二进制表单的参数,包括多媒体（文件）和普通参数
	 * @param name
	 * @return
	 */
	public MultipartParameter getMultipartParameter(String name){
		if(name != null){
			for(MultipartParameter mp : multiParams){
				if(name.equals(mp.getName())){
					return mp;
				}
			}
		}
		
		return null;
	}
}
