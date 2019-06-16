package org.mintframework.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * 多媒体请求配置
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:25:13 
 *
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UploadConfig {
	/**
	 * 请求文件和普通参数长度
	 * @return
	 */
	long limitSize();
}
