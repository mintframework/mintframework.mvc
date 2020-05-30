package org.mintframework.mvc.core.upload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;

/** 
 * 封装多媒体请求
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:30:58 
 *  
 */
public class MintMultipartHttpServletRequest extends HttpServletRequestWrapper {
	/**
	 * 文件参数
	 */
	private Map<String, List<MultipartParameter>> fileParams = null;
	
	/**
	 * 文件参数 和 普通参数
	 */
	private Collection<Part> allFiles = null;
	
	/**
	 * 仅仅是普通参数
	 */
	private Map<String, String[]> parameters = null;

	/**
	 * 将文件上传参数封装进request里
	 * @param request
	 * @param multiParams
	 */
	public MintMultipartHttpServletRequest(HttpServletRequest request, MultipartParameter[] multiParams) {
		super(request);
		/*你麻痹的，看你怎么给我锁定*/
		this.parameters = new HashMap<>(request.getParameterMap());
		if(multiParams!=null && multiParams.length>0){
			Map<String, List<String>> newParams = new HashMap<>();
			this.fileParams = new HashMap<>();
			/*分离出二进制上传的非文件参数和文件参数*/
			List<String> tmp;
			List<MultipartParameter> tmpf;
			List<Part> allFiles = new ArrayList<>();
			for(MultipartParameter mp : multiParams){
				if(mp.isFile()){
					allFiles.add(mp);
					if(this.fileParams.get(mp.getName()) != null) {
						this.fileParams.get(mp.getName()).add(mp);
					} else {
						tmpf = new ArrayList<MultipartParameter>();
						tmpf.add(mp);
						this.fileParams.put(mp.getName(), tmpf);
					}
				} else {
					if(newParams.get(mp.getName()) != null){
						newParams.get(mp.getName()).add(mp.getParameterValue());
					} else {
						tmp = new ArrayList<String>();
						tmp.add(mp.getParameterValue());
						newParams.put(mp.getName(), tmp);
					}
				}
			}
			/*将二进制上传的非文件参数添加到parameter中*/
			for(String key : newParams.keySet()){
				String[] oldParam = parameters.get(key);
				List<String> params = newParams.get(key);
				if(oldParam == null){
					parameters.put(key, (String[]) params.toArray(new String[params.size()]));
				} else {
					//为了效率不用简单方法
					String[] newParam = new String[oldParam.length + params.size()];
					for(int i=0; i<oldParam.length; i++){
						newParam[i] = oldParam[i];
					}
					for(int i=0; i<params.size(); i++){
						newParam[i+oldParam.length] = params.get(i);
					}
					parameters.put(key, newParam);
				}
			}
		}
	}
	
	@Override
	public Part getPart(String name){
		if(name != null){
			List<MultipartParameter> parts = fileParams.get(name);
			if(parts!=null) {
				return parts.get(0);
			} else {
				return null;
			}
		}
		return null;
	}
	
	@Override
	public Collection<Part> getParts(){
		return allFiles;
	}
	
	@Override
	public String getParameter(String name){
		if(parameters.get(name) != null){
			return parameters.get(name)[0];
		}
		return null;
	}
	
	@Override
	public Map<String,String[]>	getParameterMap(){
		return parameters;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public MintTempFile getPartFile(String name){
		if(name != null){
			List<MultipartParameter> parts = fileParams.get(name);
			if(parts!=null) {
				return parts.get(0).getTempFile();
			} else {
				return null;
			}
		}
		return null;
	}
	
	/**
	 * 通过参数名获取上传的文件
	 * @param name
	 * @return
	 */
	public MintTempFile[] getPartFiles(String name){
		if(name != null){
			List<MultipartParameter> files = fileParams.get(name);
			if(files == null) return null;
			MintTempFile[] tfs = new MintTempFile[files.size()];
			for(int i=0; i<files.size(); i++) {
				tfs[i] = files.get(i).getTempFile();
			}
			return tfs;
		}
		return null;
	}
	
	/**
	 * 通过参数名获取上传的文件
	 * @param name
	 * @return
	 */
	public List<MultipartParameter> getPartFileList(String name){
		if(name != null){
			return fileParams.get(name);
		}
		return null;
	}
}
