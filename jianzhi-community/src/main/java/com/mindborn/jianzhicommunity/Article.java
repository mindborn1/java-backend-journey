package com.mindborn.jianzhicommunity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体类
 *
 * 对应数据库中的 article 表
 */
@Data
@TableName("article")
public class Article {

    /** 文章 ID（主键自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章标题 */
    private String title;

    /** 文章内容 */
    private String content;

    /** 作者 ID */
    private Long userId;

    /** 状态：0-草稿 1-已发布 2-已删除 */
    private Integer status;

    /** 浏览量 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（插入和更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}