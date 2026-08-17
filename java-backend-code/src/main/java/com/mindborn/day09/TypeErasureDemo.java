package com.mindborn.day09;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Day09 练习3：泛型类型擦除（Type Erasure）
 *
 * 核心结论：
 * 1. 编译后，List<String> 和 List<Integer> 在 JVM 看来是同一个类型：List
 * 2. 所有类型参数 T 被替换为 Object（或第一个边界）
 * 3. 编译器自动插入类型转换代码
 * 4. 因此：不能 new T()、不能 instanceof T、不能 new T[]
 * 5. 但反射可以获取泛型信息（编译器保留了签名在字节码中）
 */
public class TypeErasureDemo {
    // ========== 演示1：编译后泛型信息被擦除 ==========

    public static void demo1() {
        List<String> strList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();

        // 编译后，这两个 List 的泛型信息都被擦除了
        // JVM 运行时看到的都是 ArrayList，无法区分是 String 还是 Integer
        System.out.println(strList.getClass() == intList.getClass()); // true ✅

        // 甚至可以通过反射"绕过"泛型检查（不要这样做！）
        // 因为运行时没有泛型约束，所以能 add Integer 到 List<String>
        // 但取出来的时候 ClassCastException 就炸了
    }

    // ========== 演示2：编译器自动插入强制转换 ==========

    /**
     * 编译前：
     *   String s = list.get(0);
     *
     * 编译后（反编译看到的）：
     *   String s = (String) list.get(0);  // 编译器自动插入了强转！
     *
     * 这就是类型擦除的代价：运行时没有泛型保护，靠编译期插入的强转保证类型安全
     */

    public static void demo2() {
        List<String> list = new ArrayList<>();
        list.add("hello");
        String s = list.get(0);
        System.out.println(s);

    }

    // ========== 演示3：为什么不能用 T 做某些事 ==========
    static class GenericBox<T> {
        // ❌ 编译错误：不能 new T()
        // T instance = new T();
        // 原因：编译后 T 变成 Object，new Object() 不是你想要的

        // ❌ 编译错误：不能 new T[]
        // T[] array = new T[10];
        // 原因：同上，编译后变成 new Object[10]

        // ❌ 编译错误：不能 instanceof T
        // if (obj instanceof T) { }
        // 原因：运行时 T 已经擦除了，JVM 不知道 T 是什么

        // ✅ 正确做法：用 Class<T> 来创建实例
        T createInstance(Class<T> clazz) throws Exception {
            return clazz.getDeclaredConstructor().newInstance();
        }

        // ✅ 正确做法：用 Object[] 强转（有警告，但可行）
        @SuppressWarnings("unchecked")
        T[] creatArray(int size) {
            return(T[]) new Object[size];  // 先建 Object[]，再强转
        }
    }

    // ========== 演示4：反射可以"看到"泛型信息（签名保留） ==========

    // 定义一个带泛型参数的父类
    static abstract class Dao<T> {
        // 这个类的签名里保留了 T 的信息！
    }

    // UserDao 继承了 Dao<User>，T 被具体化为 User
    static class User {}
    static class UserDao extends Dao<User> {}

    public static void demo4() {
        // 通过子类获取父类的泛型参数
        Type genericSuperclass = UserDao.class.getGenericSuperclass();

        // 是参数化类型 ParameterizedType
        if (genericSuperclass instanceof ParameterizedType pt) {
            Type[] actualArgs = pt.getActualTypeArguments();
            for (Type t : actualArgs) {
                // 输出：class com.mindborn.day09.TypeErasureDemo$User
                System.out.println("泛型参数类型 =" + t);

            }
        }
        // 这就是 MyBatis-Plus 的 BaseMapper<T>、Spring 的泛型依赖注入的底层原理！
        // 框架通过反射读取你声明的 <T>，自动推断实体类型
    }

    // ========== 演示5：桥方法（Bridge Method） ==========

    /**
     * 编译器为了兼容泛型擦除前后的方法签名，会自动生成"桥方法"
     */
    static class Node<T> {
        public void setData(T data) {}
        }

        static class MyNode extends Node<Integer> {
            // 我重写的方法签名是：setData(Integer data)
            @Override
            public void setData(Integer data) {
                System.out.println("MyNode.setData :" + data);
            }
        }

        public static void demo5() throws Exception {
            // 但编译后，Node 的 setData 被擦除为 setData(Object data)
            // 为了兼容，编译器给 MyNode 自动生成一个桥方法：
            // public void setData(Object data) { setData((Integer) data);}
            MyNode node = new MyNode();
            // 通过反射可以看到这个桥方法
            java.lang.reflect.Method[] methods = MyNode.class.getDeclaredMethods();
            for (java.lang.reflect.Method m : methods) {
                if (m.getName().equals("setData")) {
                    System.out.println("方法: " + m + ", 是否是桥方法: " + m.isBridge());

                }
            }
        }

        // ========== 主方法：运行所有演示 ==========
        public static void main(String[] args) throws Exception {
            System.out.println("=== 演示1：类型擦除后 Class 相同 ===");
            demo1();

            System.out.println("\n=== 演示2：编译器自动插入强转 ===");
            demo2();

            System.out.println("\n=== 演示4：反射获取泛型参数");
            demo4();

            System.out.println("\n=== 演示5：桥方法 ===");
            demo5();

            System.out.println("\n 类型擦除理解完成！");
            System.out.println("面试金句：Java泛型是编译期的语法糖，运行时全部擦除为object，");
            System.out.println("类型安全由编译器通过插入强制转换来保证。");
        }

}
