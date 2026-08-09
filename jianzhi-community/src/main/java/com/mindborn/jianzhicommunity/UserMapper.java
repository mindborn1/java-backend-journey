package com.mindborn.jianzhicommunity;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper 接口
 * 
 * 继承 BaseMapper<User> 后，自动拥有以下方法（不用写 SQL！）：
 *   insert(user)         — 插入一条记录
 *   selectById(id)       — 根据 ID 查询
 *   selectList(null)     — 查询全部
 *   updateById(user)     — 根据 ID 更新
 *   deleteById(id)       — 根据 ID 删除
 *   selectCount(null)    — 统计总数
 * 
 * 这就是 MyBatis-Plus 的威力：单表 CRUD 零 SQL
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 不用写任何方法，BaseMapper 已经提供了全部 CRUD
}
