package com.mindborn.jianzhicommunity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mindborn.jianzhicommunity.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章服务层
 *
 * 提供文章的增删改查功能
 *
 *  * 改造点：
 *  * 1. RuntimeException → BusinessException
 *  * 2. 方法参数更清晰
 *
 */
@Service
public class ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 发布文章
     */
    public Article publish(String title, String content, Long userId) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setUserId(userId);
        article.setStatus(1); // 1 = 已发布
        article.setViewCount(0);
        article.setLikeCount(0);

        articleMapper.insert(article);
        return article;
    }

    /**
     * 根据 ID 查询文章
     */
    public Article getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在：" + id);
        }
        return article;
    }

    /**
     * 查询某个用户的所有文章
     */
    public List<Article> listByUserId(Long userId) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        // 按创建时间倒序（最新的排前面）
        queryWrapper.orderByDesc("create_time");
        return articleMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有已发布的文章
     */
    public List<Article> listPublished() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1); // 只查已发布的
        queryWrapper.orderByDesc("create_time");
        return articleMapper.selectList(queryWrapper);
    }

    /**
     * 删除文章（软删除：把 status 改成 2）
     */
    public void delete(Long id, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在：" + id);
        }
        // 只能删除自己的文章
        if (!article.getUserId().equals(userId)) {
            throw new RuntimeException("不能删除别人的文章");
        }
        article.setStatus(2); // 2 = 已删除
        articleMapper.updateById(article);
    }
}