package com.mindborn.jianzhicommunity.service;

import com.mindborn.jianzhicommunity.dto.UserLoginDTO;
import com.mindborn.jianzhicommunity.dto.UserRegisterDTO;
import com.mindborn.jianzhicommunity.entity.User;

/**
 * 用户服务接口
 *
 * 注意：这里加了 getById(Long id) 方法。
 * 为什么需要它？
 *   LoginInterceptor（登录拦截器）需要从 Token 里解析出用户ID后，
 *   查询数据库获取完整用户信息，然后放进 ThreadLocal。
 *   所以 UserService 必须暴露这个方法。
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param dto 注册参数（用户名、密码、昵称）
     * @return 注册成功后的用户对象（密码已加密，ID 已生成）
     */
    User register(UserRegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 登录参数（用户名、密码）
     * @return 登录成功的用户对象（后面 Controller 会转成 DTO 或生成 Token）
     */
    User login(UserLoginDTO dto);

    /**
     * 根据 ID 查询用户
     *
     * 给 LoginInterceptor 用的！
     * JWT Token 里只存了用户ID，拦截器需要查库拿到完整 User 对象。
     *
     * @param id 用户ID
     * @return 用户对象，不存在返回 null（拦截器里自己判断）
     */
    User getById(Long id);
}