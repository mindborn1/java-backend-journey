package com.mindborn.jianzhicommunity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 *
 * 对应数据库中的 comment 表
 *
 * parent_id 的作用：
 * - 0 表示这是一级评论（直接评论文章）
 * - 非 0 表示这是回复某条评论（二级评论）
 *   目前先只做一级评论，parent_id 固定存 0
 *   以后扩展多级回复时这个字段直接就能用
 */
@Data
@TableName("comment")
public class Comment {


    /** 评论 ID（主键自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章 ID（这条评论属于哪篇文章） */
    private Long articleId;

    /** 用户 ID（谁发的这条评论） */
    private Long userId;

    /** 评论内容 */
    private String content;

    /**
     * 父评论 ID
     * 0 = 顶级评论（直接评论文章）
     * 其他 = 回复某条评论
     */
    private Long parentId;

    /** 创建时间（插入时自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（插入和更新时自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

