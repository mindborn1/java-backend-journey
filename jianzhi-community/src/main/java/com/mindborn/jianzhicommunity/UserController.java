package com.mindborn.jianzhicommunity;

import com.mindborn.jianzhicommunity.common.Result;
import com.mindborn.jianzhicommunity.dto.UserLoginDTO;
import com.mindborn.jianzhicommunity.dto.UserRegisterDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 *
 * 改造点：
 * 1. @RequestParam → @RequestBody + @Valid（前端传 JSON，自动校验）
 * 2. 返回 UserDTO → 返回 Result<UserDTO>（统一响应格式）
 * 3. 不需要 try-catch，异常直接抛给 GlobalExceptionHandler
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     *
     * @Valid 告诉 Spring：先校验参数，不合法直接抛 BindException
     * @RequestBody 告诉 Spring：从请求体里读 JSON，转成 UserRegisterDTO
     */
    @PostMapping("/register")
    public Result<UserDTO> register(@RequestBody @Valid UserRegisterDTO dto) {
        // Service 里可能抛 BusinessException（如用户名已存在）
        // 也可能抛 BindException（参数校验失败）
        // 都不需要管，GlobalExceptionHandler 会统一处理

        User user = userService.register(dto);

        // 转成 DTO 去掉 password 后再返回
        return Result.success("注册成功", convertToDTO(user));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserDTO> login(@RequestBody @Valid UserLoginDTO dto) {
        User user = userService.login(dto);
        return Result.success("登录成功", convertToDTO(user));
    }

    /**
     * 把 User 转成 UserDTO（去掉 password，不暴露敏感信息）
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