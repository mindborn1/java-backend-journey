package com.mindborn.jianzhicommunity.service;

import com.mindborn.jianzhicommunity.entity.Comment;
import java.util.List;

/**
 * 评论服务接口
 *
 * 接口里只声明"做什么"，不声明"怎么做"。
 * 具体怎么做，交给 impl 包下的实现类。
 */
public interface CommentService {

    /**
     * 发表评论
     *
     * 业务规则：
     *   - 文章必须存在（不能对不存在的文章评论）
     *   - 评论内容不能为空
     *   - parentId 固定为 0（一级评论，以后扩展回复功能时可变）
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @param content   评论内容
     * @return 插入后的评论对象（包含自动生成的 ID）
     */
    Comment addComment(Long articleId, Long userId, String content);

    /**
     * 查询某篇文章的所有评论
     *
     * @param articleId 文章ID
     * @return 评论列表，按时间倒序（最新的在前面），无评论返回空列表
     */
    List<Comment> listByArticleId(Long articleId);

    /**
     * 删除评论
     *
     * 业务规则：
     *   - 评论必须存在
     *   - 只能删除自己发的评论
     *
     * @param commentId 评论ID
     * @param userId    当前操作用户ID（用于权限校验）
     */
    void delete(Long commentId, Long userId);
}