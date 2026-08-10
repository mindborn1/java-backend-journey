package com.mindborn.jianzhicommunity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发表评论请求 DTO
 *
 * 为什么单独建 DTO？
 * 1. 明确前端需要传哪些字段（articleId + content）
 * 2. 加 @NotBlank、@NotNull 做参数校验
 * 3. 和数据库实体 Comment 解耦，互不干扰
 *
 * userId 为什么不在这里？
 * 正常应该登录后从 Token/Session 里取当前用户 ID，
 * 现在还做登录鉴权，所以 userId 暂时从请求参数里传。
 */
@Data
public class CommentAddDTO {

    /**
     * @NotNull 表示这个字段不能为 null
     * 注意：@NotBlank 只能用于 String，Long 用 @NotNull
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    /**
     * @NotBlank 表示不能为 null、空字符串、纯空格
     * 防止用户刷空评论
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
}