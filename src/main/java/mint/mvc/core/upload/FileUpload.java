package mint.mvc.core.upload;

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

/** 
 * 文件上传的工具类
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:30:43 
 *  
 */
public class FileUpload {
	private static Logger log = Logger.getLogger(FileUpload.class.getName());
	private static Logger logger = Logger.getLogger(FileUpload.class.getName());
	
	/**
	 * @param tempFilePath 文件上传的临时保存路径
	 * @param attributeName 表单处理完毕后，将参数添加到request时用到的属性名，action内可以通过request.getAttribute()方法获取
	 * @param limitSize 表单项的最大长度，小于零表示无限制
	 * @param lock
	 * @return
	 */
	public static boolean upload(String tempFilePath, String attributeName, long limitSize, HttpServletRequest request){
		if(attributeName == null || "".equals(attributeName)) {
			log.warning("has no attributeName");
			return false;
		}
		
		if(tempFilePath == null || "".equals(tempFilePath)) {
			log.warning("has no tempFilePath");
			return false;
		}
		
		try{
			parseRequestBody(request, tempFilePath, attributeName, limitSize);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 解析请求体，获取参数和文件
	 * 本方法效率优先，写法不太优雅
	 * @param request
	 */
	static void parseRequestBody(HttpServletRequest request, String tempFilePath, String attributeName, long limitSize){
		ServletInputStream inputStream = null;
		try {
			inputStream = request.getInputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		String boundary = null;
		//获取请求体的分隔符
		for(String s : request.getHeader("Content-Type").split(";")){
			if(s.indexOf("boundary=") > 0){
				boundary = "--"+s.split("=")[1];
			}
		}
		
		List<MultipartParameter> multiParam = null;
		FileOutputStream fileOut = null;
		try {
			byte[] readBuf = new byte[1024*4];
			int readLen = 0;
			
			//用到的所有局部变量，为了效率，所有不在循环体内声明
			DefaultMultipartParameter currentPart = null;
			TempFile tempFile;
			boolean isFile = false;
			
			boolean end = false;
			String line, partInfo, mimeType = null;
			StringBuffer paramValue = new StringBuffer(512);
			
			/*缓存分隔符及其长度，提高性能*/
			byte[] boundaryByte = boundary.getBytes();
			int startBoundaryLen = boundaryByte.length+2;  	//回车 和 换行 符（\r\n）
			int endBoundaryLen = startBoundaryLen + 2; 		//--
			
			readLen = inputStream.readLine(readBuf, 0, readBuf.length);
			line = new String(readBuf, 0, readLen);
			
			//第一行必须是分隔符
			if(!line.startsWith(boundary)){
				logger.warning("Multimedia requests cannot be resolved");
				return;
			}
			
			if(line.startsWith(boundary+"--")){return;}
			
			multiParam = new ArrayList<MultipartParameter>();
			long partSize = 0;
			
			while(!end){
				if(!end && readLen < 0){
					logger.warning("Multimedia requests cannot be resolved");
					return;
				}
				
				//分析头部，分析分隔符
				//还未读取到请参数结尾
				
				line = new String(readBuf, 0, readLen);
				if(!line.startsWith(boundary+"--")){
					currentPart = new DefaultMultipartParameter();

					partInfo = new String(readBuf, 0, inputStream.readLine(readBuf, 0, readBuf.length));
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
							tempFile = createTempFile(tempFilePath, info.get("filename"));
							fileOut = new FileOutputStream(tempFile);
							currentPart.tempFile = tempFile;
						} else {
							logger.warning("Multimedia requests cannot be resolved");
							break;
						}
					} else {
						isFile = false;
					}
				} else {
					end = true;
				}
				
				//跳过描述头和内容之间的换行符
				inputStream.readLine(readBuf, 0, 3);
				
				/*
				 * 为了性能，以下循环尽量不生成垃圾变量
				 * 逻辑较强，可读性较差
				 */
				if(isFile){
					//解析文件内容
					int j, i;
					while((readLen = inputStream.readLine(readBuf, 0, readBuf.length)) > 0){
						//有可能出现分隔符
						if(readLen == startBoundaryLen || readLen == endBoundaryLen){
							i = 0; j=0;
							
							//不需要比较后面的回车换行符了
							for(; i<startBoundaryLen-2; i++){
								if(boundaryByte[i] != readBuf[i]) {
									j = 1;
									break;
								}
							}
							
							//当前part分析完成，分析下一个头部
							if(j == 0){
								//fileOut.write(writeBuf, 0, writeLen);
								//writeLen = 0;
								partSize = 0;
								multiParam.add(currentPart);
								
								fileOut.flush();
								fileOut.close();
								fileOut = null;
								break;
							}
						}
						
						partSize += readLen;
						
						if(partSize <= limitSize){
							fileOut.write(readBuf, 0, readLen);
							fileOut.flush();
						} else {
							logger.warning("files too large");
							end = true;
							break;
						}
						
						//先缓存再写入。在一些磁盘io资源不足的应用要开启文件缓冲功能
						/*for(i=0; i<readLen; i++){
							writeBuf[writeLen] = readBuf[i];	
							writeLen += 1;
							
							if(writeLen == writeBuf.length){
								fileOut.write(writeBuf, 0, writeLen);
								writeLen = 0;
							}
						}*/
					}
				} else {
					//解析普通内容
					int j, i;
					paramValue.delete(0, paramValue.length());
					while((readLen = inputStream.readLine(readBuf, 0, readBuf.length)) > 0){
						//有可能出现分隔符
						if(readLen == startBoundaryLen || readLen == endBoundaryLen){
							i = 0; j=0;
							
							//不需要比较后面的回车换行符了
							for(; i<startBoundaryLen-2; i++){
								if(boundaryByte[i] != readBuf[i]) {
									j = 1;
									break;
								}
							}
							
							//当前part分析完成，分析下一个头部
							if(j == 0){
								partSize = 0;
								currentPart.parameterValue = paramValue.toString().trim();
								multiParam.add(currentPart);
								break;
							}
						}
						
						partSize += readLen;
						
						if(partSize <= limitSize){
							paramValue.append(new String(readBuf, 0, readLen, "utf8"));
						} else {
							logger.warning("too long");
							end = true;
							break;
						}
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
			
			request.setAttribute(attributeName, ps);
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
	static TempFile createTempFile(String basepath, String filename) throws IOException{
		String suffix = "";
		if(filename.indexOf(".")>-1){
			suffix = filename.substring(filename.indexOf("."));
		}
		
		TempFile tempFile = new TempFile(basepath, UUID.randomUUID().toString().replace("-", "")+suffix);
		while(tempFile.exists()){
			tempFile = new TempFile(basepath, UUID.randomUUID().toString().replace("-", ""));
		}
		try {
			tempFile.createNewFile();
			return tempFile;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("can not careat temp file");
		}
	}
}
