package com.mindborn.jianzhicommunity.common;

import com.mindborn.jianzhicommunity.entity.User;

/**
 * 用户上下文
 *
 * 作用：在请求处理过程中，随时随地获取当前登录用户。
 *
 * 为什么用 ThreadLocal？
 *   - ThreadLocal 为每个线程提供独立的变量副本。
 *   - 多个用户同时请求时，每个请求对应一个线程，互不干扰。
 *   - 比传参更优雅，Controller、Service 里直接 UserContext.getUser() 就能拿到。
 *
 * 重要警告：请求结束后必须调用 remove() 清理！
 *   因为 Tomcat 线程是复用的（线程池），如果不清理，
 *   下一个请求复用该线程时，会拿到上一个用户的脏数据！
 */
public class UserContext {

    /**
     * ThreadLocal 变量，类型为 User。
     *
     * ThreadLocal 的原理：
     *   每个线程内部有一个 ThreadLocalMap，key 是 ThreadLocal 对象本身，
     *   value 是我们要存的数据。所以不同线程的数据完全隔离。
     */
    private static final ThreadLocal<User> currentUser =
            new ThreadLocal<>();

    /**
     * 设置当前登录用户
     *
     * 通常在拦截器里调用：登录验证通过后，把用户信息放进去。
     */
    public static void setUser(User user) {
        currentUser.set(user);
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前线程关联的用户对象，未登录返回 null
     */
    public static User getUser() {
        return currentUser.get();
    }

    /**
     * 获取当前登录用户ID
     *
     * 这是一个便捷方法，很多地方只需要用户ID，不需要整个 User 对象。
     */
    public static Long getUserId() {
        User user = currentUser.get();
        return user != null ? user.getId() : null;
    }

    /**
     * 清理当前线程的用户信息
     *
     * 必须在拦截器的 afterCompletion 里调用！
     * 这是防止内存泄漏和脏数据的关键！
     */
    public static void remove() {
        currentUser.remove();
    }
}
