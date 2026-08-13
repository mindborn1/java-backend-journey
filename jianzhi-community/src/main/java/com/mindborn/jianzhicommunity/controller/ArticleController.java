package com.mindborn.jianzhicommunity.controller;

import com.mindborn.jianzhicommunity.common.Result;
import com.mindborn.jianzhicommunity.common.UserContext;       // ✅ 新增
import com.mindborn.jianzhicommunity.entity.Article;
import com.mindborn.jianzhicommunity.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 *
 * 重要改动：
 *   1. publish 和 delete 不再接收 userId 参数，改为从 UserContext 取当前登录用户ID
 *   2. getById 路径从 /{id} 改为 /detail/{id}
 *      原因：如果保留 /{id}，Spring 拦截器的 /api/articles/* 会同时匹配 /api/articles/1（详情）和 /api/articles/publish（发布），
 *      导致发布接口被误放行。改成 /detail/{id} 后，白名单可以精确放行详情，不会误伤发布。
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 发布文章
     *
     * 请求方式：POST
     * 请求路径：/api/articles/publish
     *
     * ✅ 改动：去掉 @RequestParam Long userId
     * 现在从 UserContext.getUserId() 获取当前登录用户ID。
     * 因为 LoginInterceptor 已经校验过 Token，走到这里一定是登录状态。
     */
    @PostMapping("/publish")
    public Result<Article> publish(
            @RequestParam String title,
            @RequestParam String content) {

        // 从 ThreadLocal 中获取当前登录用户ID
        // 拦截器已确保用户已登录，这里一定能拿到，不需要判空
        Long userId = UserContext.getUserId();

        Article article = articleService.publish(title, content, userId);
        return Result.success("发布成功", article);
    }

    /**
     * 根据 ID 查询文章详情
     *
     * 请求方式：GET
     * 请求路径：/api/articles/detail/{id}
     *
     * ✅ 改动：路径从 /{id} 改为 /detail/{id}
     * 这样白名单可以精确配置 /api/articles/detail/** 放行，
     * 不会和 /api/articles/publish 冲突。
     */
    @GetMapping("/detail/{id}")
    public Result<Article> getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        return Result.success(article);
    }

    /**
     * 查询某个用户的所有文章
     *
     * 请求方式：GET
     * 请求路径：/api/articles/user/{userId}
     *
     * 注意：这个 userId 是路径参数，表示"查谁的文章"，不是当前登录用户，
     * 所以不需要从 UserContext 取，保持原样。
     */
    @GetMapping("/user/{userId}")
    public Result<List<Article>> listByUserId(@PathVariable Long userId) {
        List<Article> list = articleService.listByUserId(userId);
        return Result.success(list);
    }

    /**
     * 查询所有已发布的文章
     *
     * 请求方式：GET
     * 请求路径：/api/articles
     */
    @GetMapping
    public Result<List<Article>> listPublished() {
        List<Article> list = articleService.listPublished();
        return Result.success(list);
    }

    /**
     * 删除文章（软删除）
     *
     * 请求方式：DELETE
     * 请求路径：/api/articles/{id}
     *
     * ✅ 改动：去掉 @RequestParam Long userId，从 UserContext 取
     * 只能删除自己的文章，userId 用于 Service 层权限校验。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        articleService.delete(id, userId);
        return Result.success("文章已删除");
    }
}