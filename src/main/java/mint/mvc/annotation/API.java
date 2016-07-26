package mint.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * Method annotation for mapping URL.<br/>
 * For example:<br/>
 * <pre>
 * public class Blog {
 *     &#064;APIConfig("/")
 *     public String index() {
 *         // handle index page...
 *     }
 * 
 *     &#064;Mapping("/blog/$1")
 *     public String show(int id) {
 *         // show blog with id...
 *     }
 * 
 *     &#064;Mapping("/blog/edit/$1")
 *     public void edit(int id) {
 *         // edit blog with id...
 *     }
 * }
 * </pre>
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:24:42 
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface API {
	String[] 	urls()		default "";
	String[] 	method()	default "";
	String 		protocol()	default "http";

	String 		desc()		default "";
	String 		id()		default "";
	String[] 	tags()		default "";
	String 		name()		default "";
}