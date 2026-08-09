package com.mindborn.jianzhicommunity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 *
 * 提供文章的 HTTP 接口
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
    public Article publish(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam Long userId) {
        return articleService.publish(title, content, userId);
    }

    /**
     * 根据 ID 查询文章
     * GET /api/articles/{id}
     */
    @GetMapping("/{id}")
    public Article getById(@PathVariable Long id) {
        return articleService.getById(id);
    }

    /**
     * 查询某个用户的所有文章
     * GET /api/articles/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public List<Article> listByUserId(@PathVariable Long userId) {
        return articleService.listByUserId(userId);
    }

    /**
     * 查询所有已发布的文章
     * GET /api/articles
     */
    @GetMapping
    public List<Article> listPublished() {
        return articleService.listPublished();
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