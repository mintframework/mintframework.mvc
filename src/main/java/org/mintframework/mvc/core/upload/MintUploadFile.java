package org.mintframework.mvc.core.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;

/** 
 * 文件类，添加了一个saveTo方法
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年8月9日 下午15:30:43 
 */
public class MintUploadFile extends File{
	private static final long serialVersionUID = 1L;

	public MintUploadFile(URI uri) {
		super(uri);
	}

	public MintUploadFile(String parent, String child) {
		super(parent, child);
	}

	public MintUploadFile(String pathname) {
		super(pathname);
	}

	public MintUploadFile(File parent, String child) {
		super(parent, child);
	}
	
	String originalFileName = null;
	
	/**
	 * get the original name of the upload file
	 * @return
	 */
	public String getOriginalFileNmae(){
		return this.originalFileName;
	}
	
	/**
	 * get the file extension of current file
	 * @return
	 */
	public String getPrefix(){
		String suffix = "",
			filename = this.getName();
		if(filename.indexOf(".")>-1){
			suffix = filename.substring(filename.lastIndexOf("."));
		}
		
		return suffix;
	}
	
	/**
	 * 将被文件复制到指定的目标位置， 如果父目录不存在，会自动创建
	 * @param fullPath file full path
	 * @throws IOException
	 */
	public File saveAs(String fullPath, Boolean deleteTempFile) throws IOException{
		FileChannel in = null;
		FileChannel out = null;
		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			File file = new File(fullPath);
			file.getParentFile().mkdirs();
			fin = new FileInputStream(this);
			fout = new FileOutputStream(file);
			
			in = fin.getChannel();		//得到对应的文件通道
			out = fout.getChannel();	//得到对应的文件通道
			in.transferTo(0, in.size(), out);	//连接两个通道，并且从in通道读取，然后写入out通道
			
			return file;
		} catch (IOException e) {
			throw e;
		} finally {
			if(fin != null) fin.close();
			if(fout != null) fout.close();
			if(in != null) in.close();
			if(out != null) out.close();
			
			if(deleteTempFile) {
				this.delete();
			}
		}
	}
	
	/**
	 * 将被文件复制到指定的目标位置
	 * @param path 路径
	 * @param name 文件名字符串
	 * @throws IOException
	 */
	public void saveAs(String path, String name, Boolean deleteTempFile) throws IOException{
		File file = new File(path);
		saveAs(file.getAbsolutePath().replaceAll("\\\\", "/")+"/"+name, deleteTempFile);
	}
}
