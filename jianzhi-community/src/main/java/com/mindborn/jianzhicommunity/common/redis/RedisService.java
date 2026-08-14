package com.mindborn.jianzhicommunity.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 封装了 String、Hash、List、Set、ZSet 的常用操作
 * 以及过期时间设置，方便业务层直接调用
 */
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ============================ String 操作 ============================

    /**
     * 设置 String 类型的值
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置 String 类型的值，并指定过期时间
     * @param key     键
     * @param value   值
     * @param timeout 过期时间（秒）
     */
    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取 String 类型的值
     * @param key 键
     * @return 值，不存在返回 null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定的 key
     * @param key 键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断 key 是否存在
     * @param key 键
     * @return true 存在，false 不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置 key 的过期时间
     * @param key     键
     * @param timeout 过期时间（秒）
     * @return true 设置成功
     */
    public Boolean expire(String key, long timeout) {
        return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 获取 key 的剩余过期时间
     * @param key 键
     * @return 剩余秒数，-1 表示永不过期，-2 表示已过期/不存在
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    // ============================ 计数器操作 ============================

    /**
     * 对 key 的值进行自增 1
     * 常用于：文章浏览量、点赞数等计数场景
     * @param key 键
     * @return 自增后的值
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 对 key 的值进行自增指定数值
     * @param key   键
     * @param delta 增量
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    // ============================ Hash 操作 ============================

    /**
     * 向 Hash 中放入一个字段
     * @param key   Hash 的键
     * @param field 字段名
     * @param value 字段值
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 获取 Hash 中指定字段的值
     * @param key   Hash 的键
     * @param field 字段名
     * @return 字段值
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取 Hash 中所有字段和值
     * @param key Hash 的键
     * @return Map<字段, 值>
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除 Hash 中的指定字段
     * @param key    Hash 的键
     * @param fields 要删除的字段名（可变参数）
     */
    public void hDelete(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    // ============================ List 操作 ============================

    /**
     * 从 List 左侧（头部）插入元素
     * @param key   List 的键
     * @param value 元素值
     * @return 插入后 List 的长度
     */
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 从 List 右侧（尾部）弹出元素
     * @param key List 的键
     * @return 弹出的元素，List 为空返回 null
     */
    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 获取 List 指定范围的元素
     * @param key   List 的键
     * @param start 起始索引（0 开始）
     * @param end   结束索引（-1 表示到最后）
     * @return 元素列表
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    // ============================ Set 操作 ============================

    /**
     * 向 Set 中添加元素
     * @param key   Set 的键
     * @param value 元素值
     * @return 添加成功返回 1，已存在返回 0
     */
    public Long sAdd(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 判断元素是否在 Set 中
     * @param key   Set 的键
     * @param value 元素值
     * @return true 存在
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    // ============================ ZSet 操作 ============================

    /**
     * 向 ZSet 中添加元素（带分数）
     * 常用于：排行榜、热门文章排序
     * @param key   ZSet 的键
     * @param value 元素值
     * @param score 分数
     * @return true 添加成功
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取 ZSet 中指定范围的元素（按分数从低到高）
     * @param key   ZSet 的键
     * @param start 起始索引
     * @param end   结束索引
     * @return 元素集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取 ZSet 中分数最高的前 N 个元素
     * @param key   ZSet 的键
     * @param count 数量
     * @return 元素集合
     */
    public Set<Object> zReverseRange(String key, long count) {
        return redisTemplate.opsForZSet().reverseRange(key, 0, count - 1);
    }

    /**
     * 获取元素在 ZSet 中的分数
     * @param key   ZSet 的键
     * @param value 元素值
     * @return 分数
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }
}