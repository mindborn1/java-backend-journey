package com.mindborn.jianzhicommunity;

import com.mindborn.jianzhicommunity.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 *
 * 提供文章的 HTTP 接口
 *
 * 改造点：
 * 1. 所有方法返回值包成 Result<T>
 * 2. 删除接口返回 Result.success() 而不是 String
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 发布文章
     * POST /api/articles/publish
     */
    @PostMapping("/publish")
    public Result<Article> publish(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long userId) {
        Article article = articleService.publish(title, content, userId);
        return Result.success("发布成功", article);
    }

    /**
     * 根据 ID 查询文章
     * GET /api/articles/{id}
     */
    @GetMapping("/{id}")
    public Result<Article> getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        return Result.success(article);
    }

    /**
     * 查询某个用户的所有文章
     * GET /api/articles/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Result<List<Article>> listByUserId(@PathVariable Long userId) {
        List<Article> list = articleService.listByUserId(userId);
        return Result.success(list);
    }

    /**
     * 查询所有已发布的文章
     * GET /api/articles
     */
    @GetMapping
    public Result<List<Article>> listPublished() {
        List<Article> list = articleService.listPublished();
        return Result.success(list);
    }

    /**
     * 删除文章（软删除）
     * DELETE /api/articles/{id}?userId=xxx
     */
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, @RequestParam Long userId) {
        articleService.delete(id, userId);
        return "文章已删除";
    }
}