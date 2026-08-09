package com.mindborn.jianzhicommunity;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章 Mapper 接口
 *
 * 继承 BaseMapper<Article> 后，自动拥有：
 *   insert(article)       — 插入文章
 *   selectById(id)        — 根据 ID 查询
 *   selectList(wrapper)   — 条件查询
 *   updateById(article)   — 根据 ID 更新
 *   deleteById(id)        — 根据 ID 删除
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
    // 不用写任何方法，BaseMapper 已经提供了全部 CRUD
}