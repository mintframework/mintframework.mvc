package org.mintframework.mvc.core.upload;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.mintframework.mvc.core.UploadParamInfo;

/** 
 * 文件上传的工具类
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:30:43 
 *  
 */
public class FileUploader {
	private static Logger log = Logger.getLogger(FileUploader.class.getName());
	private static Logger logger = Logger.getLogger(FileUploader.class.getName());
	private static String tempFilePath = null;
	
	/**
	 * 返回文件上传临时目录
	 * @return
	 */
	public static String getTempFilePath() {
		return tempFilePath;
	}

	/**
	 * @param tempFilePath 文件上传的临时保存路径
	 */
	public static void setTempFilePath(String tempFilePath){
		if(tempFilePath==null || tempFilePath.trim().equals("")){
			return;
		}
		FileUploader.tempFilePath = tempFilePath.trim();
	}
	
	/**
	 * 
	 * @param uploadConfigs 表单项的最大长度，小于零表示无限制
	 * @param request
	 * @return
	 */
	public static DefaultMultipartParameter[] upload(Map<String, UploadParamInfo> uploadConfigs, HttpServletRequest request, Boolean acceptFile){
		if(tempFilePath == null || "".equals(tempFilePath)) {
			log.warning("has no mint.mvc.uploadTemp config item");
			return null;
		}
		
		try{
			return parseRequestBody(request, tempFilePath, uploadConfigs, acceptFile);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 解析请求体，获取参数和文件
	 * 本方法效率优先，写法不太优雅
	 * @param request
	 * @return 
	 */
	static DefaultMultipartParameter[] parseRequestBody(HttpServletRequest request, String tempFilePath, Map<String, UploadParamInfo> uploadConfigs, Boolean acceptFile){
		ServletInputStream inputStream = null;
		try {
			inputStream = request.getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		
		String boundary = null;
		//获取请求体的分隔符
		for(String s : request.getHeader("Content-Type").split(";")){
			if(s.indexOf("boundary=") > 0){
				boundary = "--"+s.split("=")[1];
				break;
			}
		}
		
		List<MultipartParameter> multiParam = null;
		FileOutputStream fileOut = null;
		UploadParamInfo currentUploadConfig = null;
		try {
			//用到的所有局部变量，为了效率，所有不在循环体内声明
			DefaultMultipartParameter currentPart = null;
			MintUploadFile tempFile;
			boolean isFile = false;
			
			boolean finish = false;
			String line = null, partInfo, mimeType = null;
			StringBuffer paramValue = new StringBuffer(512);
			
			byte[] readBuf = new byte[1024*10];
			int readLen = inputStream.readLine(readBuf, 0, readBuf.length);
			line = new String(readBuf, 0, readLen);
			
			if(boundary==null){
				boundary = line.trim();
			}
			
			//第一行必须是分隔符
			if(!line.startsWith(boundary)){
				logger.warning("Multimedia requests cannot be resolved");
				return null;
			}
			
			//请求体的结尾分隔符
			String endBoundary = boundary+"--";
			
			//请求体的结尾
			if(line.startsWith(endBoundary)){return null;}
			
			/*缓存分隔符及其长度，提高性能*/
			byte[] boundaryByte = boundary.getBytes();
			int startBoundaryLen = boundaryByte.length+2;  	//回车 和 换行 符（\r\n）
			int endBoundaryLen = startBoundaryLen + 2; 		//--
			
			multiParam = new ArrayList<MultipartParameter>();
			long partSize = 0;
			
			while(!finish){
				//理论上未结束，但是没有读到数据
				if(!finish && readLen < 0){
					logger.warning("Multimedia requests cannot be resolved");
					return null;
				}
				
				//分析头部，分析分隔符
				//还未读取到请参数结尾
				line = new String(readBuf, 0, readLen);
				if(!line.startsWith(endBoundary)){
					currentPart = new DefaultMultipartParameter();
					readLen = inputStream.readLine(readBuf, 0, readBuf.length);
					partInfo = new String(readBuf, 0, readLen);
					Map<String, String> info = parsePartInfo(partInfo, null);
					if(info.get("name") == null || "".equals(info.get("name"))){
						logger.warning("Multimedia requests cannot be resolved");
						break;
					}
					currentPart.name = info.get("name");
					/*文件头部*/
					if(info.get("filename") != null){
						currentPart.isFile = true;
						String fileName = info.get("filename");
						/*为了解决IE上传文件时文件名为绝对路径的情况*/
						if(fileName.lastIndexOf("\\") > 0){
							currentPart.filename = fileName.substring(fileName.lastIndexOf("\\")+1);
						} else {
							currentPart.filename = fileName;
						}
						partInfo =  new String(readBuf, 0, inputStream.readLine(readBuf, 0, readBuf.length));
						/*文件mimetype*/
						if(partInfo.startsWith("Content-Type: ")){
							mimeType = partInfo.split(":")[1].trim();
							if("".equals(mimeType)){
								logger.warning("Multimedia requests cannot be resolved");
								break;
							}
							currentPart.contentType = mimeType;
							isFile = true;
							
							//是否有对应的参数接受该文件
							if(acceptFile && uploadConfigs.containsKey(currentPart.name)) {
								tempFile = createTempFile(tempFilePath, info.get("filename"));
								fileOut = new FileOutputStream(tempFile);
								currentPart.tempFile = tempFile;
								currentUploadConfig = uploadConfigs.get(currentPart.name);
							}
						} else {
							logger.warning("Multimedia requests cannot be resolved");
							break;
						}
					} else {
						isFile = false;
					}
				} else {
					finish = true;
				}
				//跳过描述头和内容之间的换行符
				inputStream.readLine(readBuf, 0, 3);
				 // 为了性能，以下循环尽量不生成垃圾变量，逻辑较强，可读性较差
				if(isFile){
					//解析文件内容
					while((readLen = inputStream.readLine(readBuf, 0, readBuf.length)) > -1){
						//有可能出现分隔符
						if(readLen == startBoundaryLen || readLen == endBoundaryLen){
							line = new String(readBuf, 0, readLen);
							//当前part分析完成，保存文件，准备分析下一个头部
							if(line.startsWith(boundary)){
								partSize = 0;
								if(fileOut != null) {
									multiParam.add(currentPart);
									fileOut.flush();
									fileOut.close();
									fileOut = null;
								}
								break;
							}
						}
						partSize += readLen;
						if(fileOut != null) {
							if(currentUploadConfig == null || partSize <= currentUploadConfig.limitSize) {
								fileOut.write(readBuf, 0, readLen);
								fileOut.flush();
							} else {
								logger.warning("files too large. filesize="+partSize+", maxsize="+currentUploadConfig.limitSize);
								finish = true;
								break;
							}
						}
					}
				} else {
					//解析普通内容
					paramValue.delete(0, paramValue.length());
					while((readLen = inputStream.readLine(readBuf, 0, readBuf.length)) > -1){
						//有可能出现分隔符
						if(readLen == startBoundaryLen || readLen == endBoundaryLen){
							line = new String(readBuf, 0, readLen);
							
							//当前part分析完成，保存参数，分析下一个头部
							if(line.startsWith(boundary)){
								partSize = 0;
								currentPart.parameterValue = paramValue.toString().trim();
								multiParam.add(currentPart);
								break;
							}
						}
						
						partSize += readLen;
						paramValue.append(new String(readBuf, 0, readLen, "utf8"));
					}
				}
			}
			inputStream.close();
		} catch (IOException e) {
			logger.warning("fail to receive file");
		} finally{
			try {
				if(fileOut != null) fileOut.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(multiParam != null && multiParam.size() > 0){
			DefaultMultipartParameter[] ps = new DefaultMultipartParameter[multiParam.size()];
			
			for(int i=0; i<ps.length; i++){
				ps[i] = (DefaultMultipartParameter) multiParam.get(i);
			}
			return ps;
		} else {
			return null;
		}
	}
	
	/**
	 * 形如:
	 * Content-Disposition: form-data; name="file"; filename="test上传.txt"
	 * 被解析成map结构返回
	 * @param info 描述信息
	 * @param split key[split]value 的分隔符，默认是";"
	 * @return
	 */
	static Map<String, String>parsePartInfo(String info, String split){
		if(info == null || "".equals(info.trim())) return null;
		
		Map<String, String> partInfos = new HashMap<String, String>();
		
		if(split == null) split = "=";
		for(String s : info.split(";")){
			String kv[] = s.split(split);
			if(kv.length == 2){
				partInfos.put(kv[0].trim(), kv[1].replace("\"", "").trim());
			}
		}
		
		return partInfos;
	}
	
	/**
	 * @param basepath
	 * @return
	 * @throws IOException 
	 */
	static MintUploadFile createTempFile(String basepath, String filename) throws IOException{
		String suffix = "";
		if(filename.indexOf(".")>-1){
			suffix = filename.substring(filename.indexOf("."));
		}
		
		MintUploadFile tempFile = new MintUploadFile(basepath, UUID.randomUUID().toString().replace("-", "")+suffix);
		tempFile.originalFileName = filename;
		while(tempFile.exists()){
			tempFile = new MintUploadFile(basepath, UUID.randomUUID().toString().replace("-", ""));
		}
		try {
			tempFile.getParentFile().mkdirs();
			tempFile.createNewFile();
			return tempFile;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("can not careat temp file");
		}
	}
}
