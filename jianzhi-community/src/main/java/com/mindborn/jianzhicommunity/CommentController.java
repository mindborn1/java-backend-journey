package com.mindborn.jianzhicommunity;

import com.mindborn.jianzhicommunity.common.Result;
import com.mindborn.jianzhicommunity.dto.CommentAddDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 *
 * 提供评论相关的 HTTP 接口：
 *   POST   /api/comments              — 发表评论
 *   GET    /api/comments/article/{id} — 查询某篇文章的评论列表
 *   DELETE /api/comments/{id}         — 删除评论
 *
 * @RequestMapping("/api/comments") 的作用：
 * 给这个类的所有方法加上统一的前缀路径。
 * 比如方法上写 @PostMapping，实际访问路径就是 POST /api/comments
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
     * 请求体（JSON）：
     *   {
     *       "articleId": 1,
     *       "content": "这篇文章写得真好！"
     *   }
     *
     * @Valid 的作用：
     * 自动校验 CommentAddDTO 里的注解。
     * 如果 articleId 为 null 或 content 为空字符串，
     * Spring 会抛 BindException，被 GlobalExceptionHandler 捕获，
     * 返回 { "code": 400, "message": "评论内容不能为空", "data": null }
     *
     * userId 参数：
     * 正常应该从登录 Token 里解析当前用户 ID。
     * 现在还没做登录鉴权，所以暂时用 @RequestParam 从请求参数里传。
     * 等后面做了 JWT 登录后，这里会改成从请求头里取 Token 解析用户 ID。
     */
    @PostMapping
    public Result<Comment> addComment(
            @RequestBody @Valid CommentAddDTO dto,
            @RequestParam Long userId) {

        // 调用 Service 层处理业务逻辑
        // Service 里会校验文章是否存在、内容是否为空
        Comment comment = commentService.addComment(dto.getArticleId(), userId, dto.getContent());

        // 包装成统一响应格式返回
        return Result.success("评论成功", comment);
    }

    /**
     * 查询某篇文章的所有评论
     *
     * 请求方式：GET
     * 请求路径：/api/comments/article/{articleId}
     * 示例：GET /api/comments/article/1
     *
     * 返回数据：
     * 按时间倒序排列的评论列表，最新的排在最前面。
     * 如果没有评论，返回空数组 []，不是 null。
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
     * 请求路径：/api/comments/{commentId}?userId=xxx
     * 示例：DELETE /api/comments/5?userId=1
     *
     * 权限校验：
     * 只能删除自己发的评论，删别人的会抛 BusinessException。
     *
     * 返回 Result<Void>：
     * 删除操作不需要返回数据，只返回成功提示。
     */
    @DeleteMapping("/{commentId}")
    public Result<Void> delete(
            @PathVariable Long commentId,
            @RequestParam Long userId) {

        commentService.delete(commentId, userId);
        return Result.success("评论已删除");
    }
}