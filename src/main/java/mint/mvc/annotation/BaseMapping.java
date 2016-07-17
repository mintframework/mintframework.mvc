package mint.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation for mapping URL.<br/>
 * For example:<br/>
 * <pre>
 * public class Blog {
 *     &#064;Mapping("/")
 *     public String index() {
 *         // handle index page...
 *     }
 * 
 *     &#064;Mapping("/blog/{id}")
 *     public String show(int id) {
 *         // show blog with id...
 *     }
 * 
 *     &#064;Mapping("/blog/edit/{id}")
 *     public void edit(int id) {
 *         // edit blog with id...
 *     }
 * }
 * </pre>
 * 
 * @author LiangWei(895925636@qq.com)
 * @date 2015年3月13日 下午9:22:32 
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseMapping {
    String value();
}