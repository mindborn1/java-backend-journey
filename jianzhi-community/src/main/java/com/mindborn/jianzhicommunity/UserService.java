package com.mindborn.jianzhicommunity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mindborn.jianzhicommunity.common.exception.BusinessException;
import com.mindborn.jianzhicommunity.dto.UserLoginDTO;
import com.mindborn.jianzhicommunity.dto.UserRegisterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务层
 *
 * 改造点：
 * 1. 方法参数改成 DTO 对象
 * 2. 密码用 BCrypt 加密存储
 * 3. RuntimeException → BusinessException
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * BCrypt 密码加密器
     *
     * 为什么用它？
     * - 内部自动生成随机盐值，每次加密结果都不一样
     * - 盐值直接拼在加密结果里，验证时自动提取
     * - 不需要自己存盐值，不需要自己写哈希算法
     * - Spring 官方推荐
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     *
     * @param dto 注册请求参数
     * @return 注册成功的用户（id 已自动生成）
     */
    public User register(UserRegisterDTO dto) {
        // 1. 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", dto.getUsername());
        Long count = userMapper.selectCount(queryWrapper);

        if (count > 0) {
            // 抛业务异常，会被 GlobalExceptionHandler 捕获
            // 前端收到：{ "code": 400, "message": "用户名已存在：xxx", "data": null }
            throw new BusinessException("用户名已存在：" + dto.getUsername());
        }

        // 2. 创建新用户对象
        User user = new User();
        user.setUsername(dto.getUsername());

        // BCrypt 加密密码
        // encode(明文) → 返回加密后的哈希字符串（长度固定 60 位）
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 昵称如果没传，默认用用户名
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setStatus(1); // 1 = 正常

        // 3. 插入数据库（createTime/updateTime 由 MyMetaObjectHandler 自动填充）
        userMapper.insert(user);

        // 4. 返回新用户（包含 MyBatis-Plus 自动生成的 id）
        return user;
    }

    /**
     * 用户登录
     *
     * @param dto 登录请求参数
     * @return 登录成功的用户对象
     */
    public User login(UserLoginDTO dto) {
        // 1. 根据用户名查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", dto.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        // 2. 用户不存在
        if (user == null) {
            throw new BusinessException("用户不存在：" + dto.getUsername());
        }

        // 3. 密码校验
        // matches(明文密码, 数据库里的加密密码) → true/false
        // BCrypt 会自动从加密字符串里提取盐值来比对
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 4. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 5. 登录成功
        return user;
    }
}