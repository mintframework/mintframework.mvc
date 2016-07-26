package mint.mvc.core.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;

/** 
 * 文件类，添加了一个saveTo方法
 * @author LiangWei(895925636@qq.com)
 * @date 2015年8月9日 下午15:30:43 
 */
public class TempFile extends File{
	private static final long serialVersionUID = 1L;

	public TempFile(URI uri) {
		super(uri);
	}

	public TempFile(String parent, String child) {
		super(parent, child);
	}

	public TempFile(String pathname) {
		super(pathname);
	}

	public TempFile(File parent, String child) {
		super(parent, child);
	}

	/**
	 * 将被文件复制到指定的目标位置
	 * @param path 路径和文件名字符串
	 * @throws IOException
	 */
	public void saveAs(String path) throws IOException{
		FileChannel in = null;
		FileChannel out = null;
		FileInputStream fin = null;
		FileOutputStream fout = null;
		try {
			fin = new FileInputStream(this);
			fout = new FileOutputStream(new File(path));
			
			in = fin.getChannel();		//得到对应的文件通道
			out = fout.getChannel();	//得到对应的文件通道
			in.transferTo(0, in.size(), out);	//连接两个通道，并且从in通道读取，然后写入out通道
		} catch (IOException e) {
			throw e;
		} finally {
			if(fin != null) fin.close();
			if(fout != null) fout.close();
			if(in != null) in.close();
			if(out != null) out.close();
		}
	}
	
	/**
	 * 将被文件复制到指定的目标位置
	 * @param path 路径, end with "/"
	 * @param name 文件名字符串
	 * @throws IOException
	 */
	public void saveAs(String path, String name) throws IOException{
		saveAs(path+name);
	}
}
