package com.mindborn.jianzhicommunity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public UserDTO register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String nickname) {

        // 调用 Service 层
        User user = userService.register(username, password, nickname);

        // 转成 DTO（不包含 password）
        return convertToDTO(user);
    }

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public UserDTO login(
            @RequestParam String username,
            @RequestParam String password) {

        // 调用 Service 层
        User user = userService.login(username, password);

        // 转成 DTO（不包含 password）
        return convertToDTO(user);
    }

    /**
     * 把 User 转成 UserDTO（去掉 password）
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