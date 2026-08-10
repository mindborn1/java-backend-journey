package com.mindborn.jianzhicommunity.common;

/**
 * 统一响应包装类
 *
 * 为什么需要这个？
 * 以前接口可能返回 User、返回 List、返回 String，格式乱七八糟。
 * 前端不知道你这次返回的是对象还是字符串，解析起来很痛苦。
 *
 * 现在规定：所有接口，不管成功失败，都包成这个结构返回。
 * 前端只需要认三个字段：code、message、data
 */
public class Result<T> {

    /**
     * 状态码
     * 200 = 成功
     * 400 = 业务错误（如用户名已存在）
     * 500 = 系统错误（如数据库挂了）
     */
    private Integer code;

    /**
     * 提示消息
     * 成功时可以是"操作成功"
     * 失败时告诉用户具体原因，如"用户名不能为空"
     */
    private String message;

    /**
     * 真正的业务数据
     * 用泛型 T，这样可以是 User、List<Article>、Map 等任何类型
     * 失败时这个字段为 null
     */
    private T data;

    // ========== 构造方法（私有，不允许外部直接 new） ==========

    /**
     * 私有构造，强制使用下面的静态工厂方法创建对象
     * 这样代码更规范，不会出现 new Result(?) 这种乱传参数的情况
     */
    private Result() {}

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 快捷静态工厂方法 ==========
    // 这些方法名一看就知道是干嘛的，比 new Result(...) 清晰多了

    /**
     * 成功，不带数据
     * 适用场景：删除操作、退出登录等，只需要告诉前端"搞定了"
     * 用法：return Result.success();
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功，带数据
     * 适用场景：查询列表、查询详情
     * 用法：return Result.success(userList);
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功，自定义消息 + 数据
     * 适用场景：需要特定提示语，如"注册成功"
     * 用法：return Result.success("注册成功", user);
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败，指定错误码和消息
     * 适用场景：业务异常，如"用户不存在"（400）、"未登录"（401）
     * 用法：return Result.error(400, "用户名已存在");
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败，默认 500 系统错误
     * 适用场景：兜底，一般不在业务代码里直接用
     * 用法：return Result.error("系统繁忙");
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(200, message, null);
    }

    // ========== Getter / Setter ==========
    // 必须提供，否则 Spring 转 JSON 时读不到字段

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 成功，只传消息，不带数据
     * 适用场景：删除、退出等不需要返回数据的接口
     */

}