package com.mindborn.jianzhicommunity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户登录请求 DTO
 *
 * 和 UserRegisterDTO 分开的原因：
 * 登录只需要用户名和密码，不需要昵称。
 * 如果以后登录要加验证码、记住我等功能，也在这里扩展，不影响注册。
 */
@Data
public class UserLoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3 , max = 20 , message = "用户名长度必须在 3 - 20 位之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6 , message = "密码长度不能小于六位")
    private String password;
}
