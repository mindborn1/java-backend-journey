package com.mindborn.jianzhicommunity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据传输对象（DTO）
 *
 * 只包含需要返回给前端的数据
 * 不包含 password 等敏感信息
 */
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}