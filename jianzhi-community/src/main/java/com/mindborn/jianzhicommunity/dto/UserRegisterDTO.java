package com.mindborn.jianzhicommunity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求 DTO
 *
 * 为什么单独建一个类？
 * 1. 把"前端传什么"和"数据库实体 User"分开，互不干扰
 * 2. 可以加 @NotBlank、@Size 等校验注解
 * 3. 注册只需要 username、password、nickname，不需要 id、createTime 等字段
 *
 * @Data = @Getter + @Setter + @ToString + @EqualsAndHashCode
 */
@Data
public class UserRegisterDTO {
    /**
     * @NotBlank：不能为 null，不能是空字符串，也不能全是空格
     * @Size：长度限制
     *
     * 这两个注解配合 @Valid 使用，参数不合法会自动抛异常
     * 被 GlobalExceptionHandler 捕获，返回 {code:400, message:"用户名不能为空"}
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3 , max = 20 , message = "用户名必须在3-20位之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能小于 6 位")
    private String password;

    /**
     * 昵称是可选的，所以不加 @NotBlank
     * 前端不传的话就是 null，Service 层可以设置默认值
     */
    private String nickname;
}
