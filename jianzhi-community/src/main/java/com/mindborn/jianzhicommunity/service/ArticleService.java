package com.mindborn.jianzhicommunity.service;

import com.mindborn.jianzhicommunity.entity.Article;
import java.util.List;

/**
 * 文章服务接口
 *
 * 为什么要有接口？
 *   1. 定义规范：Controller 只认接口，不关心底层怎么实现
 *   2. 便于替换：以后想加缓存、换数据源，新建实现类就行，不用改 Controller
 *   3. 方便测试：单元测试时可以 Mock 接口，不用连数据库
 *   4. Spring 的 AOP、事务代理都是基于接口的
 */
public interface ArticleService {

    /**
     * 发布文章
     *
     * @param title   文章标题
     * @param content 文章内容
     * @param userId  作者用户ID
     * @return 插入成功后的文章对象（包含自动生成的 ID 和时间）
     */
    Article publish(String title, String content, Long userId);

    /**
     * 根据 ID 查询文章
     *
     * @param id 文章ID
     * @return 文章实体，不存在时抛 BusinessException
     */
    Article getById(Long id);

    /**
     * 查询某个用户的所有文章
     *
     * @param userId 用户ID
     * @return 该用户的文章列表，按创建时间倒序
     */
    List<Article> listByUserId(Long userId);

    /**
     * 查询所有已发布的文章
     *
     * @return 状态为 1（已发布）的文章列表，按创建时间倒序
     */
    List<Article> listPublished();

    /**
     * 删除文章（软删除）
     *
     * 业务规则：
     *   - 只能删除自己的文章
     *   - 把 status 改成 2（已删除），不是物理删除
     *
     * @param id     文章ID
     * @param userId 当前操作用户ID（用于权限校验）
     */
    void delete(Long id, Long userId);
}