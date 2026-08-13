package com.mindborn.jianzhicommunity.controller;

import com.mindborn.jianzhicommunity.common.Result;
import com.mindborn.jianzhicommunity.common.UserContext;       // ✅ 新增
import com.mindborn.jianzhicommunity.dto.CommentAddDTO;
import com.mindborn.jianzhicommunity.entity.Comment;
import com.mindborn.jianzhicommunity.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 *
 * 重要改动：
 *   addComment 和 delete 不再接收 userId 参数，改为从 UserContext 取当前登录用户ID。
 *   前端不需要再传 userId，只需要在请求头里带 Token 即可。
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 发表评论
     *
     * 请求方式：POST
     * 请求路径：/api/comments
     * 请求头：Authorization: Bearer <token>
     * 请求体（JSON）：{ "articleId": 1, "content": "写得不错" }
     *
     * ✅ 改动：去掉 @RequestParam Long userId
     * userId 现在从 UserContext.getUserId() 获取，由 Token 解析而来。
     */
    @PostMapping
    public Result<Comment> addComment(@RequestBody @Valid CommentAddDTO dto) {
        // 从 ThreadLocal 获取当前登录用户ID
        Long userId = UserContext.getUserId();

        Comment comment = commentService.addComment(dto.getArticleId(), userId, dto.getContent());
        return Result.success("评论成功", comment);
    }

    /**
     * 查询某篇文章的所有评论
     *
     * 不需要登录，游客也能看。
     */
    @GetMapping("/article/{articleId}")
    public Result<List<Comment>> listByArticleId(@PathVariable Long articleId) {
        List<Comment> list = commentService.listByArticleId(articleId);
        return Result.success(list);
    }

    /**
     * 删除评论
     *
     * 请求方式：DELETE
     * 请求路径：/api/comments/{commentId}
     * 请求头：Authorization: Bearer <token>
     *
     * ✅ 改动：去掉 @RequestParam Long userId
     */
    @DeleteMapping("/{commentId}")
    public Result<Void> delete(@PathVariable Long commentId) {
        Long userId = UserContext.getUserId();
        commentService.delete(commentId, userId);
        return Result.success("评论已删除");
    }
}