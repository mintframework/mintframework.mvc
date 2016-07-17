package mint.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 多媒体请求配置
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:25:13 
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipartConfig {
	/**
	 * 表单上传完毕后，保存在request attribute中的名字
	 * @return
	 */
	String attributeName();
	
	/**
	 * 针对该 multipart/form-data 请求请求体的最大长度，小于等于0，表示没有大小限制。默认是-1。
	 * @return
	 */
	int maxRequestSize() default -1;
	
	/**
	 * 请求文件和普通参数长度
	 * @return
	 */
	long limitSize();
}
