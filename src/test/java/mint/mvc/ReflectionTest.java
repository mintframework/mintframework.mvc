package mint.mvc;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.mintframework.mvc.util.GetArgumentName;

public class ReflectionTest {
	public static void main(String args[]) {
		try {
			Class<AppTest> c = AppTest.class;
			Method[] ms = c.getMethods();

			for (Method method : ms) {
				int size = method.getParameterTypes().length;
				List<String> list = GetArgumentName.getParamNames(method.getDeclaringClass()).get(GetArgumentName.getKey(method));
				System.out.println(GetArgumentName.getKey(method));
				if (list != null && list.size() != size) {
					System.out.println(method.getName());
					System.out.println(Arrays.asList(method.getParameterTypes()));
					System.out.println(list);
					System.out.println("-------------");
					list.subList(0, size);
				}
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}