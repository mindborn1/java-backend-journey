package com.mindborn.jianzhicommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mindborn.jianzhicommunity.common.exception.BusinessException;
import com.mindborn.jianzhicommunity.dto.UserLoginDTO;
import com.mindborn.jianzhicommunity.dto.UserRegisterDTO;
import com.mindborn.jianzhicommunity.entity.User;
import com.mindborn.jianzhicommunity.mapper.UserMapper;
import com.mindborn.jianzhicommunity.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * 密码加密用的是 Spring Security 提供的 BCryptPasswordEncoder。
 * 它的特点：
 *   - 每次 encode 生成的哈希值都不一样（因为内置随机盐）
 *   - 盐值直接拼在哈希字符串里，不需要单独存储
 *   - matches(明文, 哈希) 会自动提取盐值进行比对
 *   - 比 MD5、SHA 安全得多，是目前业界标准做法
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * BCrypt 密码加密器
     *
     * 为什么用 final？
     *   这个对象创建后不需要改变，用 final 防止被意外重新赋值。
     * 为什么不在方法里 new？
     *   每次调用都 new 一个太浪费，作为类成员只创建一次，所有方法共用。
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     *
     * 完整流程：
     *   1. 检查用户名是否已存在（查数据库 count）
     *   2. 创建 User 对象
     *   3. 用 BCrypt 加密密码
     *   4. 昵称如果没传，默认用用户名
     *   5. 状态设为 1（正常）
     *   6. 插入数据库（createTime/updateTime 自动填充）
     *   7. 返回新用户（ID 已回填）
     */
    @Override
    public User register(UserRegisterDTO dto) {
        // ========== 步骤1：检查用户名是否重复 ==========
        // QueryWrapper 构造 WHERE username = ?
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", dto.getUsername());
        // selectCount 返回符合条件的记录数
        Long count = userMapper.selectCount(queryWrapper);

        if (count > 0) {
            // 用户名已存在，抛业务异常
            // 前端收到：{ "code": 400, "message": "用户名已存在：xxx" }
            throw new BusinessException("用户名已存在：" + dto.getUsername());
        }

        // ========== 步骤2：组装用户对象 ==========
        User user = new User();
        user.setUsername(dto.getUsername());

        // ========== 步骤3：密码加密 ==========
        // encode(明文密码) → 返回 60 位的哈希字符串
        // 例如：$2a$10$N9qo8uLOickgx2ZMRZoMy.Mqrq...
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // ========== 步骤4：处理昵称 ==========
        // 三元运算符：如果 dto.getNickname() 不为 null 就用它，否则用用户名
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());

        // ========== 步骤5：设置状态 ==========
        user.setStatus(1); // 1 = 正常，0 = 禁用

        // ========== 步骤6：插入数据库 ==========
        // insert 后，MyBatis-Plus 自动把自增主键回填到 user.getId()
        userMapper.insert(user);

        // ========== 步骤7：返回 ==========
        return user;
    }

    /**
     * 用户登录
     *
     * 完整流程：
     *   1. 根据用户名查用户
     *   2. 用户不存在 → 报错
     *   3. 密码不匹配 → 报错
     *   4. 账号被禁用 → 报错
     *   5. 全部通过 → 返回用户对象
     *
     * 为什么报错信息都是"用户不存在"或"密码错误"，不区分到底是哪个？
     *   这是安全考虑。如果告诉攻击者"用户名不存在"，他就能枚举出哪些用户名存在。
     *   不过这里为了调试方便还是分开了，生产环境可以统一成"用户名或密码错误"。
     */
    @Override
    public User login(UserLoginDTO dto) {
        // ========== 步骤1：根据用户名查询 ==========
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", dto.getUsername());
        // selectOne 返回单条记录，没有则返回 null
        User user = userMapper.selectOne(queryWrapper);

        // ========== 步骤2：用户不存在 ==========
        if (user == null) {
            throw new BusinessException("用户不存在：" + dto.getUsername());
        }

        // ========== 步骤3：密码校验 ==========
        // matches(前端传来的明文密码, 数据库里的哈希密码)
        // BCrypt 会自动从哈希字符串里提取盐值，然后对明文进行相同运算，比较结果
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // ========== 步骤4：检查账号状态 ==========
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // ========== 步骤5：登录成功 ==========
        return user;
    }

    /**
     * 根据 ID 查询用户
     *
     * 这个方法专门给 LoginInterceptor 用的！
     * 拦截器从 JWT Token 里解析出用户ID后，需要查库获取完整用户信息，
     * 然后放进 ThreadLocal，供后续 Controller 和 Service 使用。
     *
     * 这里不抛异常，返回 null 让调用方自己判断。
     * 因为拦截器里要根据 null 的情况返回 401。
     */
    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}