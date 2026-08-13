package com.mindborn.jianzhicommunity.controller;

import com.mindborn.jianzhicommunity.common.JwtUtil;           // ✅ 新增：生成 Token
import com.mindborn.jianzhicommunity.common.Result;
import com.mindborn.jianzhicommunity.common.UserContext;       // ✅ 新增：获取当前用户
import com.mindborn.jianzhicommunity.dto.UserDTO;
import com.mindborn.jianzhicommunity.dto.UserLoginDTO;
import com.mindborn.jianzhicommunity.dto.UserRegisterDTO;
import com.mindborn.jianzhicommunity.entity.User;
import com.mindborn.jianzhicommunity.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * 接口清单：
 *   POST /register    — 注册
 *   POST /login       — 登录（返回 JWT Token）
 *   GET  /me          — 获取当前登录用户信息（需登录）
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * JwtUtil 用于生成和解析 JWT Token
     * 登录成功后，调用 generateToken(userId, username) 生成 Token 返回给前端
     */
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody @Valid UserRegisterDTO dto) {
        User user = userService.register(dto);
        return Result.success("注册成功", convertToDTO(user));
    }

    /**
     * 用户登录
     *
     * 请求方式：POST
     * 请求路径：/login
     *
     * 流程：
     *   1. Service 层校验用户名密码
     *   2. 校验通过，用 JwtUtil 生成 Token
     *   3. 返回 Token 给前端
     *
     * 前端拿到 Token 后，必须保存在 LocalStorage 中，
     * 后续每次请求都在请求头里带上：Authorization: Bearer <token>
     *
     * 返回类型 Result<String>：data 字段就是 Token 字符串
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid UserLoginDTO dto) {
        // 步骤1：Service 层校验用户名、密码、账号状态
        User user = userService.login(dto);

        // 步骤2：生成 JWT Token
        // Token 里存用户ID（subject）和用户名（claim），有效期 7 天
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 步骤3：返回 Token
        // 前端收到：{ "code": 200, "message": "登录成功", "data": "eyJhbG..." }
        return Result.success("登录成功", token);
    }

    /**
     * 获取当前登录用户信息
     *
     * 请求方式：GET
     * 请求路径：/me
     * 请求头：Authorization: Bearer <token>
     *
     * 关键点：不需要传任何参数！
     * LoginInterceptor 已经在请求开始时把用户信息放进了 ThreadLocal，
     * 这里直接 UserContext.getUser() 就能拿到。
     *
     * 如果用户未登录：LoginInterceptor 会拦住，根本走不到这里。
     * 但为了代码健壮性，还是做了空判断（防御性编程）。
     */
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser() {
        // 从 ThreadLocal 中取出当前登录用户
        User user = UserContext.getUser();

        // 防御性编程：理论上不会为空，但万一拦截器漏了，做个兜底
        if (user == null) {
            return Result.error(401, "未登录");
        }

        // 转成 DTO（去掉密码）返回
        return Result.success(convertToDTO(user));
    }

    /**
     * User 实体类 → UserDTO 转换
     * 去掉 password 等敏感字段，只返回安全数据
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }
}