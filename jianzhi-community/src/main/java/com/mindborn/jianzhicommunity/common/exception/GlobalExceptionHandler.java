package com.mindborn.jianzhicommunity.common.exception;

import com.mindborn.jianzhicommunity.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * 这个类的作用：
 * Controller 里抛出的任何异常，都会被这里"拦截"并统一处理。
 * 这样 Controller 里就不用写 try-catch 了，代码非常干净。
 *
 * @RestControllerAdvice 是什么？
 * - 它是 @ControllerAdvice + @ResponseBody 的组合
 * - @ControllerAdvice：专门用来全局处理 Controller 层的事情（异常、数据绑定等）
 * - @ResponseBody：把返回值自动转成 JSON
 *
 * @ExceptionHandler：标记这个方法处理哪种异常
 */
@Slf4j
@RestControllerAdvice

public class GlobalExceptionHandler {

    /**
     * 捕获【@RequestBody 参数校验失败】
     * 比如 @RequestBody @Valid UserRegisterDTO 校验不通过时抛出
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();
        log.warn("请求体参数校验失败: {}", message);
        return Result.error(400, message);
    }

    /**
     * 处理【业务异常】
     *
     * 什么时候触发？
     * Service 里 throw new BusinessException("xxx") 时
     *
     * 返回什么？
     * 友好的 JSON：{ "code": 400, "message": "用户名已存在", "data": null }
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        // log.warn 打印警告日志，带黄色标记，方便在控制台区分
        log.warn("业务异常：{}", e.getMessage());

        // 把异常里的 code 和 message 包成 Result 返回给前端
        return Result.error(e.getCode(),e.getMessage());
    }

    /**
     * 处理【参数校验失败】
     *
     * 什么时候触发？
     * 比如 DTO 字段加了 @NotBlank，但用户传了空字符串
     * 或者 @Size(min=6) 但密码只传了 3 位
     *
     * BindException 是参数绑定失败时 Spring 抛出的异常
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        // 从绑定结果里拿到第一个字段的错误信息
        // 比如 "用户名不能为空"、"密码长度不能小于6位"
        String message = e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        log.warn("参数校验失败：{}", message);
        return Result.error(400,message);
    }

    /**
     * 捕获【请求参数缺失】
     *
     * 什么时候触发？
     * 比如方法要求 @RequestParam Long userId，但请求里没传这个参数
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(org.springframework.web.bind.MissingServletRequestParameterException e) {
        log.warn("请求参数缺失: {}", e.getMessage());
        return Result.error(400, "缺少必要参数：" + e.getParameterName());
    }

    /**
     * 处理【其他所有异常】
     *
     * 这是兜底方法，程序出 bug（NullPointerException、SQL 异常等）时走这里
     *
     * 为什么不能让前端看到真实错误？
     * 1. 暴露堆栈信息有安全风险（黑客能看到你的代码结构）
     * 2. 用户看不懂 "NullPointerException at UserService.java:45"
     *
     * 所以返回一句友好的"系统繁忙"，同时后端 log.error 打印完整堆栈供排查
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // log.error 带异常对象，会打印完整堆栈跟踪
        log.error("系统异常：",e);

        return Result.error(500,"系统繁忙，请稍后再试");
    }
}
