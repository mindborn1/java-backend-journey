package com.mindborn.jianzhicommunity.common.exception;

/**
 * 业务异常
 *
 * 什么是业务异常？
 * 不是程序 bug，而是"业务规则不允许"的情况。
 * 比如：用户名已存在、文章已删除、密码错误、没有权限...
 *
 * 为什么要自定义？
 * 1. 和系统异常（NullPointerException、SQLException）区分开
 * 2. 可以携带自定义错误码，方便前端做特定处理
 * 3. 全局异常处理器里可以单独捕获它，返回友好的错误提示
 *
 * 继承 RuntimeException 的好处：
 * - 不用在方法签名上写 throws，代码更干净
 * - Spring 的事务默认只回滚 RuntimeException，正好适用
 */

public class BusinessException extends RuntimeException{

    /**
     * 业务错误码
     * 200 成功
     * 400 业务参数错误（如用户名已存在）
     * 401 未登录/Token 过期
     * 403 没有权限
     * 404 资源不存在
     *
     * 注意：这个和 HTTP 状态码是两回事，只是我们内部约定的数字
     */
    private Integer code;

    /**
     * 构造方法 1：只传消息，默认错误码 400
     * 用法：throw new BusinessException("用户名已存在");
     */
    public BusinessException(String message) {
        // 调用父类 RuntimeException 的构造方法
        super(message);
        this.code = 400;
    }

    /**
     * 构造方法 2：传错误码 + 消息
     * 用法：throw new BusinessException(401, "请先登录");
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     */
    public Integer getCode() {
        return code;
    }
}
