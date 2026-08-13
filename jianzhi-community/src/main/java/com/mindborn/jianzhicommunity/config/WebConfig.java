package com.mindborn.jianzhicommunity.config;

import com.mindborn.jianzhicommunity.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类
 *
 * 作用：注册 Spring MVC 的拦截器，并配置拦截规则（白名单）。
 *
 * 为什么实现 WebMvcConfigurer？
 *   Spring Boot 提供这个接口，让我们可以自定义 Spring MVC 的行为，
 *   比如注册拦截器、配置跨域(CORS)、静态资源映射等。
 *   这里我们只用到 addInterceptors() 方法。
 *
 * 为什么加 @Configuration？
 *   告诉 Spring 这是一个配置类，启动时会加载并执行里面的配置。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 注入登录拦截器
     *
     * LoginInterceptor 上有 @Component，所以 Spring 会自动创建它的实例。
     * 这里用 @Autowired 把实例拿过来，注册到拦截器链中。
     */
    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * 添加拦截器
     *
     * 执行逻辑：
     *   1. addInterceptor(loginInterceptor) → 注册拦截器
     *   2. addPathPatterns("/**") → 拦截所有请求路径
     *   3. excludePathPatterns(...) → 白名单，这些路径不拦截
     *
     * 白名单设计原则：
     *   - 登录、注册：本来就是用来获取 Token 的，不能拦截
     *   - 文章列表、文章详情：游客也能看，不需要登录
     *   - 评论列表：游客也能看
     *   - /error：Spring Boot 的错误页面，放行
     *
     * 注意：路径匹配规则
     *   "/**" 表示所有路径
     *   "/api/articles/**" 表示 /api/articles 下的所有子路径
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 白名单：以下路径不需要登录就能访问
                .excludePathPatterns(
                        "/register",                    // 用户注册接口
                        "/login",                       // 用户登录接口
                        "/api/articles",                // 文章列表（GET）
                        "/api/articles/detail/**",       // 文章详情、发布等（注意：发布文章需要登录，这里先简单放行）
                        "/api/comments/article/**",     // 某篇文章的评论列表（游客可看）
                        "/error"                        // Spring Boot 默认错误处理路径
                );
    }
}