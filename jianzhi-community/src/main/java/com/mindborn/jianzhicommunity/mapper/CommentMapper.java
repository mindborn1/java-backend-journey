package com.mindborn.jianzhicommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mindborn.jianzhicommunity.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论 Mapper 接口
 *
 * Mapper 是什么？
 * 它是 Java 代码和数据库之间的"翻译官"。
 * 你写 Java 方法调用，它帮你转成 SQL 去执行。
 *
 * 为什么继承 BaseMapper<Comment>？
 * MyBatis-Plus 已经帮你写好了所有单表 CRUD 的 SQL，
 * 你不需要手写 XML 或 @Select 注解，直接就能用。
 *
 * BaseMapper<Comment> 提供的方法包括：
 *   insert(Comment entity)          — 插入一条记录，返回影响行数
 *   selectById(Serializable id)     — 根据主键 ID 查询
 *   selectList(Wrapper wrapper)     — 按条件查询列表
 *   selectCount(Wrapper wrapper)    — 按条件统计数量
 *   updateById(Comment entity)      — 根据 ID 更新（null 字段不更新）
 *   deleteById(Serializable id)     — 根据 ID 删除
 *
 * @Mapper 的作用：
 * 告诉 Spring Boot："这是一个 MyBatis 的 Mapper，请帮我生成实现类并放进容器里。"
 * 这样其他类用 @Autowired 注入时就能拿到实例。
 *
 * 注意：JianzhiCommunityApplication 上已经有 @MapperScan，
 * 理论上不加 @Mapper 也能扫描到，但加上更保险，也便于阅读。
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    // 目前只做单表 CRUD，BaseMapper 已经够用了，不需要写额外方法。
    // 如果以后要做"查询某篇文章的所有评论及评论者用户名"这种联表查询，
    // 再在这里自定义方法，配合 XML 或 @Select 注解实现。
}