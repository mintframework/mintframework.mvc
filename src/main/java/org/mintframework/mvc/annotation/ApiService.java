package org.mintframework.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2016年1月4日 下午14:24:42 
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiService {
	String name();
	
	String 		desc()		default "";
	String 		id()		default "";
	String[] 	tags()		default "";
}