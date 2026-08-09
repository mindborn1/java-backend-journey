package com.mindborn.jianzhicommunity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 用户服务层
 * 
 * @Service: 标记这是 Spring 的 Service 组件
 */
@Service
public class UserService {

    /**
     * 注入 UserMapper（MyBatis-Plus 会自动生成实现类）
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称（可选）
     * @return 注册成功的用户对象
     */
    public User register(String username, String password, String nickname) {
        // 1. 检查用户名是否已存在
        // QueryWrapper 是 MyBatis-Plus 的条件构造器
        // eq 表示 "等于"，就是 WHERE username = ?
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        Long count = userMapper.selectCount(queryWrapper);

        // 如果已存在，抛异常
        if (count > 0) {
            throw new RuntimeException("用户名已存在：" + username);
        }

        // 2. 创建新用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 实际项目要加密
        user.setNickname(nickname);
        user.setStatus(1); // 默认正常状态

        // 3. 插入数据库
        userMapper.insert(user);

        // 4. 返回新用户（包含自动生成的 ID）
        return user;
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象
     */
    public User login(String username, String password) {
        // 1. 根据用户名查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);

        // 2. 用户不存在
        if (user == null) {
            throw new RuntimeException("用户不存在：" + username);
        }

        // 3. 密码不匹配
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }

        // 4. 检查账号状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 5. 登录成功，返回用户信息
        return user;
    }
}
