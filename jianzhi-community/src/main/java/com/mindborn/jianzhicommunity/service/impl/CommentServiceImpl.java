package com.mindborn.jianzhicommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mindborn.jianzhicommunity.common.exception.BusinessException;
import com.mindborn.jianzhicommunity.entity.Article;
import com.mindborn.jianzhicommunity.entity.Comment;
import com.mindborn.jianzhicommunity.mapper.ArticleMapper;
import com.mindborn.jianzhicommunity.mapper.CommentMapper;
import com.mindborn.jianzhicommunity.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评论服务实现类
 *
 * 这个类里同时注入了 CommentMapper 和 ArticleMapper。
 * 为什么需要 ArticleMapper？
 *   发表评论时要先检查文章是否存在，这是跨表的业务校验。
 *   虽然 CommentService 主要操作 comment 表，但业务规则要求查 article 表。
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 发表评论
     *
     * 完整业务流程：
     *   1. 根据 articleId 查文章，不存在则报错
     *   2. 检查评论内容是否为空（trim 去掉首尾空格后判断）
     *   3. 组装 Comment 对象
     *   4. parentId 设为 0，表示一级评论（直接评论文章）
     *   5. 插入数据库，返回带 ID 的评论对象
     *
     * 关于 parentId：
     *   现在固定为 0，只做一级评论。
     *   以后做"回复评论"功能时，这里可以改成传 parentId 参数，
     *   表示这条评论是回复哪条评论的。
     */
    @Override
    public Comment addComment(Long articleId, Long userId, String content) {
        // ========== 步骤1：检查文章是否存在 ==========
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在，无法评论");
        }

        // ========== 步骤2：检查评论内容 ==========
        // trim() 去掉首尾空格，防止用户只输入空格或换行符
        // isEmpty() 判断是否是空字符串
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        // ========== 步骤3：组装评论实体 ==========
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        // 存的时候再 trim 一次，保证数据库里数据干净
        comment.setContent(content.trim());
        // parentId = 0 表示顶级评论
        comment.setParentId(0L);

        // ========== 步骤4：插入数据库 ==========
        // insert 后，MyBatis-Plus 自动回填主键 ID
        // createTime 和 updateTime 由 MyMetaObjectHandler 自动填充
        commentMapper.insert(comment);

        return comment;
    }

    /**
     * 查询某篇文章的所有评论
     *
     * 按时间倒序：最新的评论排在最前面，符合阅读习惯。
     * 如果没有评论，selectList 返回空列表（size = 0），不是 null。
     */
    @Override
    public List<Comment> listByArticleId(Long articleId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);      // WHERE article_id = ?
        wrapper.orderByDesc("create_time");       // ORDER BY create_time DESC
        return commentMapper.selectList(wrapper);
    }

    /**
     * 删除评论
     *
     * 权限校验逻辑：
     *   1. 先查出这条评论
     *   2. 比较 comment.getUserId() 和传入的 userId
     *   3. 不一致 → 抛异常"不能删除别人的评论"
     *   4. 一致 → 执行物理删除（评论一般直接删，不需要软删除）
     *
     * 注意：Long 是包装类，比较值必须用 equals()，不能用 ==！
     *   == 比较的是对象地址，equals 比较的是数值。
     *   两个 Long 对象值相同但地址不同，用 == 会返回 false。
     */
    @Override
    public void delete(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 权限校验
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("不能删除别人的评论");
        }

        // 物理删除：直接从数据库删掉这条记录
        // 评论不像文章那么重要，不需要软删除
        commentMapper.deleteById(commentId);
    }
}