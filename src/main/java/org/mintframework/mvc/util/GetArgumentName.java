package org.mintframework.mvc.util;
 
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
/**
 * 通过读取Class文件,获得方法形参名称列表
 * @author wendal(wendal1985@gmail.com)
 * @author LiangWei(cnliangwei@foxmail.com)
 * @date 2015年3月13日 下午9:19:48 
 *
 */
public class GetArgumentName {
 
    /**
     * 获取Method的形参名称列表
     * 
     * @param method
     *            被解析的方方法
     * @return 形参名称列表,如果没有调试信息,将返回null
     */
    public static List<String> getArgumentNames(Method method) {
        try {
            int size = method.getParameterTypes().length;
            if (size == 0) {
            	return new ArrayList<String>(0);
            }
            
            List<String> list = getParamNames(method.getDeclaringClass()).get(getKey(method));
            if (list != null && list.size() != size) {
				/*
				 * System.out.println(Arrays.asList(method.getParameterTypes()));
				 * System.out.println(list);
				 */
            	//throw new Exception("获取方法参数错误："+method.getDeclaringClass().getName()+"  "+method.getName());
            	return list.subList(0, size);
            }
            return list;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
 
    /**
     * 获取Constructor的形参名称列表
     * 
     * @param constructor
     *            被解析的构造方法
     * @return 形参名称列表,如果没有调试信息,将返回null
     */
    public static List<String> getParamNames(Constructor<?> constructor) {
        try {
            int size = constructor.getParameterTypes().length;
            if (size == 0)
                return new ArrayList<String>(0);
            List<String> list = getParamNames(constructor.getDeclaringClass())
                    .get(getKey(constructor));
            if (list != null && list.size() != size)
                return list.subList(0, size);
            return list;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
 
    // ---------------------------------------------------------------------------------------------------
 
    /**
     * 获取�?��类的�?��方法/构�?方法的形参名称Map
     * 
     * @param klass
     *            �?��解析的类
     * @return �?��方法/构�?方法的形参名称Map
     * @throws IOException
     *             如果有任何IO异常,不应该有,如果是本地文�?�?00%遇到bug�?
     */
    public static Map<String, List<String>> getParamNames(Class<?> klass) throws IOException {
        InputStream in = klass.getResourceAsStream("/" + klass.getName().replace('.', '/') + ".class");
        return getParamNames(in);
    }
 
    public static Map<String, List<String>> getParamNames(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(new BufferedInputStream(in));
        Map<String, List<LocalVariable>> names = new HashMap<String, List<LocalVariable>>();
        Map<Integer, String> strs = new HashMap<Integer, String>();
        dis.skipBytes(4);	// Magic
        dis.skipBytes(2);	// 副版本号
        dis.skipBytes(2);	// 主版本号
 
        // 读取常量�?
        int constant_pool_count = dis.readUnsignedShort();
        for (int i = 0; i < (constant_pool_count - 1); i++) {
            byte flag = dis.readByte();
            switch (flag) {
            case 7:	// CONSTANT_Class:
                dis.skipBytes(2);
                break;
            case 9:	// CONSTANT_Fieldref:
            case 10:// CONSTANT_Methodref:
            case 11:// CONSTANT_InterfaceMethodref:
                dis.skipBytes(2);
                dis.skipBytes(2);
                break;
            case 8:// CONSTANT_String:
                dis.skipBytes(2);
                break;
            case 3:// CONSTANT_Integer:
            case 4:// CONSTANT_Float:
                dis.skipBytes(4);
                break;
            case 5:// CONSTANT_Long:
            case 6:// CONSTANT_Double:
                dis.skipBytes(8);
                i++;// 必须跳过�?��,这是class文件设计的一个缺�?历史遗留问题
                break;
            case 12:// CONSTANT_NameAndType:
                dis.skipBytes(2);
                dis.skipBytes(2);
                break;
            case 1:// CONSTANT_Utf8:
                int len = dis.readUnsignedShort();
                byte[] data = new byte[len];
                dis.read(data);
                strs.put(i + 1, new String(data, "UTF-8"));// 必然是UTF8�?
                break;
            case 15:// CONSTANT_MethodHandle:
                dis.skipBytes(1);
                dis.skipBytes(2);
                break;
            case 16:// CONSTANT_MethodType:
                dis.skipBytes(2);
                break;
            case 18:// CONSTANT_InvokeDynamic:
                dis.skipBytes(2);
                dis.skipBytes(2);
                break;
            default:
                throw new RuntimeException("Impossible!! flag=" + flag);
            }
        }
 
        dis.skipBytes(2);// 版本控制�?
        dis.skipBytes(2);// 类名
        dis.skipBytes(2);// 超类
 
        // 跳过接口定义
        int interfaces_count = dis.readUnsignedShort();
        dis.skipBytes(2 * interfaces_count);// 每个接口数据,�?个字�?
 
        // 跳过字段定义
        int fields_count = dis.readUnsignedShort();
        for (int i = 0; i < fields_count; i++) {
            dis.skipBytes(2);
            dis.skipBytes(2);
            dis.skipBytes(2);
            int attributes_count = dis.readUnsignedShort();
            for (int j = 0; j < attributes_count; j++) {
                dis.skipBytes(2);// 跳过访问控制�?
                int attribute_length = dis.readInt();
                dis.skipBytes(attribute_length);
            }
        }
 
        // �?��读取方法
        int methods_count = dis.readUnsignedShort();
        for (int i = 0; i < methods_count; i++) {
            dis.skipBytes(2); // 跳过访问控制�?
            String methodName = strs.get(dis.readUnsignedShort());
            String descriptor = strs.get(dis.readUnsignedShort());
            short attributes_count = dis.readShort();
            for (int j = 0; j < attributes_count; j++) {
                String attrName = strs.get(dis.readUnsignedShort());
                int attribute_length = dis.readInt();
                if ("Code".equals(attrName)) { // 形参只在Code属�?�?
                    dis.skipBytes(2);
                    dis.skipBytes(2);
                    int code_len = dis.readInt();
                    dis.skipBytes(code_len); // 跳过具体代码
                    int exception_table_length = dis.readUnsignedShort();
                    dis.skipBytes(8 * exception_table_length); // 跳过异常�?
 
                    int code_attributes_count = dis.readUnsignedShort();
                    for (int k = 0; k < code_attributes_count; k++) {
                        int str_index = dis.readUnsignedShort();
                        String codeAttrName = strs.get(str_index);
                        int code_attribute_length = dis.readInt();
                        if ("LocalVariableTable".equals(codeAttrName)) {// 形参在LocalVariableTable属�?�?
                            int local_variable_table_length = dis
                                    .readUnsignedShort();
                            List<LocalVariable> varNames = new ArrayList<LocalVariable>(
                                    local_variable_table_length);
                            for (int l = 0; l < local_variable_table_length; l++) {
                                dis.skipBytes(2);
                                dis.skipBytes(2);
                                String varName = strs.get(dis
                                        .readUnsignedShort());
                                dis.skipBytes(2);
                                // index
                                int index = dis.readUnsignedShort();
                                if (!"this".equals(varName)) // 非静态方�?第一个参数是this
                                    varNames.add(new LocalVariable(index,
                                            varName));
                            }
                            names.put(methodName + "," + descriptor, varNames);
                        } else
                            dis.skipBytes(code_attribute_length);
                    }
                } else
                    dis.skipBytes(attribute_length);
            }
        }
        dis.close();
 
        Set<String> set = names.keySet();
        Map<String, List<String>> reMap = new HashMap<String, List<String>>();
        for (String key : set) {
            LocalVariable[] tmpArr = names.get(key).toArray(
                    new LocalVariable[0]);
            Arrays.sort(tmpArr);
            List<String> list = new ArrayList<String>();
            for (LocalVariable var : tmpArr) {
                list.add(var.name);
            }
            reMap.put(key, list);
        }
        return reMap;
    }
 
    static class LocalVariable implements Comparable<LocalVariable> {
        public int    index;
        public String name;
 
        public LocalVariable(int index, String name) {
            this.index = index;
            this.name = name;
        }
 
        public int compareTo(LocalVariable o) {
            return this.index - o.index;
        }
    }
 
    /**
     * 传入Method或Constructor,获取getParamNames方法返回的Map�?��应的key
     */
    public static String getKey(Object obj) {
        StringBuilder sb = new StringBuilder();
        if (obj instanceof Method) {
            sb.append(((Method) obj).getName()).append(',');
            getDescriptor(sb, (Method) obj);
        } else if (obj instanceof Constructor) {
            sb.append("<init>,"); // 只有非静态构造方法才能用有方法参数的,而且通过反射API拿不到静态构造方�?
            getDescriptor(sb, (Constructor<?>) obj);
        } else
            throw new RuntimeException("Not Method or Constructor!");
        return sb.toString();
    }
 
    public static void getDescriptor(StringBuilder sb, Method method) {
        sb.append('(');
        for (Class<?> klass : method.getParameterTypes())
            getDescriptor(sb, klass);
        sb.append(')');
        getDescriptor(sb, method.getReturnType());
    }
 
    public static void getDescriptor(StringBuilder sb,
            Constructor<?> constructor) {
        sb.append('(');
        for (Class<?> klass : constructor.getParameterTypes())
            getDescriptor(sb, klass);
        sb.append(')');
        sb.append('V');
    }
 
    /*本方法来源于ow2的asm库*/
    public static void getDescriptor(final StringBuilder buf, final Class<?> c) {
        Class<?> d = c;
        while (true) {
            if (d.isPrimitive()) {
                char car;
                if (d == Integer.TYPE) {
                    car = 'I';
                } else if (d == Void.TYPE) {
                    car = 'V';
                } else if (d == Boolean.TYPE) {
                    car = 'Z';
                } else if (d == Byte.TYPE) {
                    car = 'B';
                } else if (d == Character.TYPE) {
                    car = 'C';
                } else if (d == Short.TYPE) {
                    car = 'S';
                } else if (d == Double.TYPE) {
                    car = 'D';
                } else if (d == Float.TYPE) {
                    car = 'F';
                } else /* if (d == Long.TYPE) */{
                    car = 'J';
                }
                buf.append(car);
                return;
            } else if (d.isArray()) {
                buf.append('[');
                d = d.getComponentType();
            } else {
                buf.append('L');
                String name = d.getName();
                int len = name.length();
                for (int i = 0; i < len; ++i) {
                    char car = name.charAt(i);
                    buf.append(car == '.' ? '/' : car);
                }
                buf.append(';');
                return;
            }
        }
    }
}