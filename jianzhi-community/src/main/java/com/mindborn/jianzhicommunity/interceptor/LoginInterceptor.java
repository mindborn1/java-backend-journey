package com.mindborn.jianzhicommunity.interceptor;

/**
 * 注入 UserService 接口，不是 UserServiceImpl！
 * 拦截器也是 Spring 管理的 Bean，同样要遵循面向接口编程。
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindborn.jianzhicommunity.entity.User;
import com.mindborn.jianzhicommunity.common.JwtUtil;
import com.mindborn.jianzhicommunity.common.Result;
import com.mindborn.jianzhicommunity.common.UserContext;
import com.mindborn.jianzhicommunity.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 *
 * 实现 HandlerInterceptor 接口，重写三个方法：
 *   - preHandle：请求处理前执行（最常用，用于权限校验）
 *   - postHandle：请求处理后、视图渲染前执行（很少用）
 *   - afterCompletion：请求完成后执行（用于资源清理）
 *
 * @Component：
 *   标记为 Spring 组件，启动时自动扫描并创建实例。
 *   为什么不用 @Service？因为拦截器不属于业务层，用 @Component 更语义化。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * JWT 工具类，用于生成和验证 Token
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户服务接口
     *
     * 拦截器需要从 Token 中解析出用户ID后，查库获取完整用户信息。
     * 这里调用 userService.getById(userId)。
     *
     * 注意：注入的是接口！如果注入 UserServiceImpl，AOP 代理会失效。
     */
    @Autowired
    private UserService userService;

    /**
     * Jackson 的 ObjectMapper，用于把 Java 对象转成 JSON 字符串。
     *
     * 为什么需要它？
     *   拦截器在 Controller 之前执行，如果在这里拦截请求，
     *   @RestControllerAdvice 全局异常处理器是捕获不到的。
     *   所以必须手动构造 JSON 响应，用 ObjectMapper 做序列化。
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 请求处理前执行：校验 JWT Token
     *
     * 完整流程：
     *   1. 从请求头 Authorization 中读取 Token
     *   2. 检查是否存在且格式为 "Bearer xxx"
     *   3. 验证 Token 是否有效（签名、过期时间）
     *   4. 从 Token 中解析用户ID
     *   5. 查数据库获取完整用户信息
     *   6. 将用户放入 ThreadLocal（供后续代码使用）
     *   7. 放行请求
     *
     * @param request  HTTP 请求对象，可以获取请求头、参数等
     * @param response HTTP 响应对象，可以设置状态码、写入响应体
     * @param handler  将要执行的处理器（Controller 方法），一般不用
     * @return true 放行，false 拦截（请求到此结束，不会进入 Controller）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // ========== 步骤1：从请求头获取 Authorization ==========
        // HTTP 标准规定：Token 放在 Authorization 请求头里
        // 例如：Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxx.xxx
        String authHeader = request.getHeader("Authorization");

        // ========== 步骤2：检查 Token 是否存在且格式正确 ==========
        // 如果请求头为空，或者不是 "Bearer " 开头，说明没登录或格式错误
        // startsWith("Bearer ") 注意后面有个空格，标准格式要求空格分隔
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 返回 401 未授权，提示请先登录
            writeErrorResponse(response, 401, "请先登录");
            return false;  // 拦截，不放行，请求到此结束
        }

        // ========== 步骤3：提取真正的 Token 字符串 ==========
        // substring(7) 去掉前面的 "Bearer "（7个字符：B-e-a-r-e-r-空格）
        // 剩下的就是 JWT 本身
        String token = authHeader.substring(7);

        // ========== 步骤4：验证 Token 有效性 ==========
        // validateToken 内部会解析 Token，如果以下情况会返回 false：
        //   - Token 被篡改（签名验证失败）
        //   - Token 已过期（超过 expiration 时间）
        //   - Token 格式错误（不是合法的 JWT）
        if (!jwtUtil.validateToken(token)) {
            writeErrorResponse(response, 401, "登录已过期，请重新登录");
            return false;
        }

        // ========== 步骤5：从 Token 中解析用户ID ==========
        // Token 的 subject 字段里存的是用户ID（String 类型），转成 Long
        Long userId = jwtUtil.getUserIdFromToken(token);

        // ========== 步骤6：查询数据库获取完整用户信息 ==========
        // 为什么查数据库？Token 里只存了用户ID和用户名，
        // 但后续 Controller 可能需要用户头像、角色等更多信息。
        // 也可以把这些全塞 Token 里，但 Token 会变大，每次请求都传浪费带宽。
        User user = userService.getById(userId);

        // 防御性编程：虽然 Token 有效，但用户可能已被删除
        if (user == null) {
            writeErrorResponse(response, 401, "用户不存在");
            return false;
        }

        // ========== 步骤7：将用户信息存入 ThreadLocal ==========
        // ThreadLocal 的作用：在同一次请求中，任何地方都能拿到当前用户。
        // 后续 Controller 里调用 UserContext.getUser() 就能直接取到。
        UserContext.setUser(user);

        // ========== 步骤8：放行，请求继续进入 Controller ==========
        return true;
    }

    /**
     * 请求处理完成后执行
     *
     * 无论请求成功、失败还是抛异常，这个方法最终都会被执行。
     * （前提是 preHandle 返回了 true，如果 false 被拦截，不会执行）
     *
     * 核心作用：清理 ThreadLocal！
     *   Tomcat 使用线程池处理请求，线程会被复用。
     *   如果不清理 ThreadLocal，下一个请求复用该线程时，
     *   UserContext.getUser() 会拿到上一个用户的脏数据！
     *   这是严重的安全漏洞！
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清理 ThreadLocal 中的用户信息，防止内存泄漏和脏数据
        UserContext.remove();
    }

    /**
     * 向客户端写入 JSON 格式的错误响应
     *
     * 为什么需要这个方法？
     *   拦截器在 Controller 之前执行，如果在这里拦截了请求，
     *   @RestControllerAdvice 全局异常处理器是捕获不到的（因为还没到 Controller）。
     *   所以必须手动构造响应，设置状态码，写入 JSON。
     *
     * @param response HTTP 响应对象
     * @param code     HTTP 状态码（401 未授权）
     * @param message  错误提示信息
     */
    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
        // 设置响应内容类型为 JSON，编码 UTF-8（防止中文乱码）
        response.setContentType("application/json;charset=UTF-8");

        // 设置 HTTP 状态码
        response.setStatus(code);

        // 构造统一响应对象：{ "code": 401, "message": "请先登录", "data": null }
        Result<Void> result = Result.error(code, message);

        // 用 Jackson 把 Result 对象序列化成 JSON 字符串
        // writeValueAsString(result) → "{\"code\":401,\"message\":\"请先登录\"}"
        String json = objectMapper.writeValueAsString(result);

        // 把 JSON 字符串写入响应体，返回给前端
        response.getWriter().write(json);
    }
}