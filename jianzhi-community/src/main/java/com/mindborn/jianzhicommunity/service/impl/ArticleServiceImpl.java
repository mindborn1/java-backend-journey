package com.mindborn.jianzhicommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mindborn.jianzhicommunity.common.exception.BusinessException;
import com.mindborn.jianzhicommunity.entity.Article;
import com.mindborn.jianzhicommunity.mapper.ArticleMapper;
import com.mindborn.jianzhicommunity.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章服务实现类
 *
 * @Service 的作用：
 *   1. 标记这是一个 Spring 的 Service 层 Bean
 *   2. Spring 启动时会扫描到它，创建实例放进容器
 *   3. Controller 里 @Autowired ArticleService 时，Spring 会自动注入这个实现类
 *
 * implements ArticleService：
 *   表示这个类实现了 ArticleService 接口，必须实现接口里定义的所有方法。
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    /**
     * 注入文章 Mapper
     *
     * @Autowired 是 Spring 的依赖注入注解。
     * Spring 会自动去容器里找 ArticleMapper 的实例（MyBatis-Plus 生成的代理对象），
     * 然后赋值给这个字段，不需要我们手动 new。
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 发布文章
     *
     * 流程：
     *   1. 创建 Article 对象
     *   2. 设置标题、内容、作者ID
     *   3. 设置状态为 1（已发布）
     *   4. 浏览量和点赞数初始化为 0
     *   5. 插入数据库（createTime/updateTime 由 MyMetaObjectHandler 自动填充）
     *   6. 返回包含完整信息的文章对象（ID 已回填）
     */
    @Override
    public Article publish(String title, String content, Long userId) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setUserId(userId);
        article.setStatus(1);        // 1 = 已发布，0 = 草稿，2 = 已删除
        article.setViewCount(0);     // 初始浏览量 0
        article.setLikeCount(0);   // 初始点赞数 0

        // insert 是 BaseMapper 提供的方法，执行 INSERT SQL
        articleMapper.insert(article);

        // insert 后，MyBatis-Plus 会自动把数据库生成的主键回填到 article.getId()
        return article;
    }

    /**
     * 根据 ID 查询文章
     *
     * selectById 是 BaseMapper 提供的方法，执行 SELECT * FROM article WHERE id = ?
     *
     * 如果查不到（返回 null），抛 BusinessException，被全局异常处理器捕获，
     * 前端收到 { "code": 400, "message": "文章不存在：xxx" }
     */
    @Override
    public Article getById(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            // 用 BusinessException 而不是 RuntimeException，走统一异常处理
            throw new BusinessException("文章不存在：" + id);
        }
        return article;
    }

    /**
     * 查询某个用户的所有文章
     *
     * QueryWrapper 是 MyBatis-Plus 的条件构造器，链式 API 拼 SQL。
     * eq("user_id", userId)  →  WHERE user_id = ?
     * orderByDesc("create_time")  →  ORDER BY create_time DESC
     */
    @Override
    public List<Article> listByUserId(Long userId) {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);           // 只查这个用户的
        queryWrapper.orderByDesc("create_time");      // 最新的排前面
        return articleMapper.selectList(queryWrapper);
    }

    /**
     * 查询所有已发布的文章
     *
     * eq("status", 1)  →  WHERE status = 1
     * 只查状态为"已发布"的文章，草稿和已删除的不显示。
     */
    @Override
    public List<Article> listPublished() {
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);                 // 只查已发布的
        queryWrapper.orderByDesc("create_time");        // 最新的排前面
        return articleMapper.selectList(queryWrapper);
    }

    /**
     * 删除文章（软删除）
     *
     * 为什么用软删除？
     *   直接 DELETE FROM 会导致数据永久丢失，以后无法恢复。
     *   改成 update status = 2，文章在列表里不显示，但数据还在数据库里。
     *
     * 权限校验：
     *   只能删除自己发的文章，防止用户 A 删掉用户 B 的文章。
     *   比较时用 equals()，因为 userId 是 Long 包装类型，不能用 ==。
     */
    @Override
    public void delete(Long id, Long userId) {
        // 先查出这篇文章
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在：" + id);
        }

        // 权限校验：文章作者ID 必须等于 当前操作用户ID
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("不能删除别人的文章");
        }

        // 软删除：把状态改成 2（已删除），然后更新数据库
        article.setStatus(2);
        articleMapper.updateById(article);
    }
}