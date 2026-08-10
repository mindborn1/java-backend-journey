package com.mindborn.jianzhicommunity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mindborn.jianzhicommunity.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评论服务层
 *
 * 职责：
 * 处理评论相关的业务逻辑，包括发表评论、查询评论列表、删除评论。
 *
 * 为什么需要 Service 层？
 * Controller 只负责接收请求和返回响应，不应该写业务逻辑。
 * 业务规则（如"文章不存在不能评论"、"只能删除自己的评论"）应该放在 Service 里，
 * 这样代码结构清晰，也便于单元测试和复用。
 */
@Service
public class CommentService {

    /**
     * 注入评论 Mapper，用于操作 comment 表
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * 注入文章 Mapper，用于校验文章是否存在
     *
     * 为什么需要 ArticleMapper？
     * 发表评论时要先检查文章是否存在，如果文章被删了就不能评论。
     * 这里直接注入 ArticleMapper 查一下，简单直接。
     * 也可以注入 ArticleService，但那样会多一层调用，没必要。
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 发表评论
     *
     * 业务流程：
     * 1. 检查文章是否存在（不能对不存在的文章评论）
     * 2. 检查评论内容是否为空（防止刷空评论）
     * 3. 组装 Comment 对象（parentId 默认为 0，表示一级评论）
     * 4. 插入数据库
     *
     * @param articleId 文章 ID（评论发在哪篇文章下）
     * @param userId    用户 ID（谁发的评论）
     * @param content   评论内容
     * @return 插入成功的评论对象（包含自动生成的 ID 和 createTime）
     */
    public Comment addComment(Long articleId, Long userId, String content) {
        // 步骤 1：检查文章是否存在
        // selectById 是 BaseMapper 提供的方法，根据主键查询
        // 如果返回 null，说明文章不存在或已被删除
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在，无法评论");
        }

        // 步骤 2：检查评论内容
        // trim() 去掉首尾空格，防止用户只输入空格或换行
        // 这里用 isBlank 判断（null、空字符串、纯空格都算空）
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        // 步骤 3：组装评论实体
        Comment comment = new Comment();
        comment.setArticleId(articleId);
        comment.setUserId(userId);
        // trim() 去掉首尾空格再存，保持数据干净
        comment.setContent(content.trim());
        // parentId = 0 表示这是一级评论（直接评论文章）
        // 以后做"回复评论"功能时，这里可以传被回复的评论 ID
        comment.setParentId(0L);

        // 步骤 4：插入数据库
        // insert 是 BaseMapper 提供的方法，会自动生成 ID 并回填到对象里
        // createTime 和 updateTime 由 MyMetaObjectHandler 自动填充
        commentMapper.insert(comment);

        // 返回包含完整信息的评论对象（前端可能需要显示评论 ID、时间等）
        return comment;
    }

    /**
     * 查询某篇文章的所有评论
     *
     * 查询逻辑：
     * - 只查属于这篇文章的评论（article_id = ?）
     * - 按时间倒序排列（最新的评论排在最前面）
     *
     * @param articleId 文章 ID
     * @return 评论列表（如果没有评论，返回空列表，不是 null）
     */
    public List<Comment> listByArticleId(Long articleId) {
        // QueryWrapper 是 MyBatis-Plus 的条件构造器
        // eq("article_id", articleId) 表示 WHERE article_id = ?
        // orderByDesc("create_time") 表示 ORDER BY create_time DESC
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleId);
        wrapper.orderByDesc("create_time");

        // selectList 返回 List<Comment>，如果没有数据返回空列表（size = 0）
        return commentMapper.selectList(wrapper);
    }

    /**
     * 删除评论
     *
     * 业务规则：
     * 1. 评论必须存在
     * 2. 只能删除自己发的评论（防止用户 A 删掉用户 B 的评论）
     *
     * @param commentId 评论 ID
     * @param userId    当前登录用户 ID（用于权限校验）
     */
    public void delete(Long commentId, Long userId) {
        // 步骤 1：查询评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 步骤 2：校验权限（只能删自己的）
        // !comment.getUserId().equals(userId) 表示"这条评论不是当前用户发的"
        // 注意：Long 是包装类型，不能用 == 比较，必须用 equals
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("不能删除别人的评论");
        }

        // 步骤 3：执行删除
        // deleteById 是物理删除（直接从数据库删掉这条记录）
        // 如果想做软删除（像 Article 那样改 status），可以改成 updateById
        // 评论一般直接物理删除即可，不需要保留
        commentMapper.deleteById(commentId);
    }
}