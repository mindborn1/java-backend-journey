package com.mindborn.jianzhicommunity.common.schedule;

import com.mindborn.jianzhicommunity.entity.Article;
import com.mindborn.jianzhicommunity.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 文章浏览量同步定时任务
 * 每隔一段时间将 Redis 中的浏览量增量同步到 MySQL
 */
@Component
public class ViewCountSyncTask {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 浏览量 Key 前缀
     */
    private static final String ARTICLE_VIEW_KEY_PREFIX = "article:view:";

    /**
     * 每 1 分钟执行一次（测试用）
     * cron 格式：秒 分 时 日 月 周
     * "0 * * * * ?" 表示每分钟的第 0 秒执行
     *
     * 测试通过后，改成 "0 0/5 * * * ?" 就是每 5 分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void syncViewCountToDatabase() {
        System.out.println("【定时任务】开始同步文章浏览量到数据库...");

        // 获取所有以 article:view: 开头的 key
        Set<String> keys = redisTemplate.keys(ARTICLE_VIEW_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            System.out.println("【定时任务】暂无需要同步的浏览量数据");
            return;
        }

        for (String key : keys) {
            try {
                // 从 key 中提取文章ID
                // key 格式：article:view:1
                Long articleId = Long.valueOf(key.replace(ARTICLE_VIEW_KEY_PREFIX, ""));

                // 获取 Redis 中的增量
                Object countObj = redisTemplate.opsForValue().get(key);
                if (countObj == null) continue;

                long increment = Long.parseLong(countObj.toString());

                // 更新数据库：在原有 view_count 基础上增加
                Article article = articleMapper.selectById(articleId);
                if (article != null) {
                    article.setViewCount(article.getViewCount() + (int) increment);
                    articleMapper.updateById(article);

                    // 同步完成后，删除 Redis 中的 key
                    redisTemplate.delete(key);
                    System.out.println("【定时任务】文章ID=" + articleId + " 同步完成，增量=" + increment);
                }
            } catch (Exception e) {
                System.err.println("【定时任务】同步失败，key=" + key + "，错误：" + e.getMessage());
            }
        }

        System.out.println("【定时任务】浏览量同步结束，共同步 " + keys.size() + " 条");
    }
}