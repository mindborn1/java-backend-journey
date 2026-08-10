# Day 05：统一异常处理 + BCrypt 加密 + 评论模块

## 一、今日目标

1. 统一响应格式（Result）
2. 统一异常处理（BusinessException + GlobalExceptionHandler）
3. BCrypt 密码加密
4. 参数校验（@Valid）
5. 评论模块（发表 / 查询 / 删除）

---

## 二、核心知识点

### 2.1 统一响应 Result<T>

**为什么要做？**

以前接口返回格式不统一：有的返回 `User`、有的返回 `List`、有的返回 `String`。前端解析困难，不知道这次返回的是什么结构。

**解决方案：**

所有接口都包成 `{ code, message, data }`。

**关键代码：**

- 泛型 `<T>` 让 `data` 可以是任何类型
- 私有构造 + 静态工厂方法（`success` / `error`），防止乱 `new`
- 静态方法必须自己声明 `<T>`，不能直接用类的 `T`

**踩坑：**

```java
public static Result<T> success() {}      // ❌ 编译报错
public static <T> Result<T> success() {}  // ✅ 正确
```

---

### 2.2 业务异常 BusinessException

**为什么要自定义？**

- 和程序 bug（`NullPointerException`）区分开
- 可以携带错误码（400 / 401 / 403），前端做不同处理
- 继承 `RuntimeException`，不用写 `throws`，Spring 事务默认回滚

**用法：**

```java
throw new BusinessException("用户名已存在");
throw new BusinessException(401, "请先登录");
```

---

### 2.3 全局异常处理器 GlobalExceptionHandler

**核心注解：**

- `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`
- `@ExceptionHandler(异常类.class)` 标记处理哪种异常

**处理优先级（越具体越靠前）：**

1. `BusinessException`（业务错误）
2. `BindException`（`@ModelAttribute` 参数校验失败）
3. `MissingServletRequestParameterException`（`@RequestParam` 参数缺失）
4. `MethodArgumentNotValidException`（`@RequestBody` 校验失败）
5. `Exception`（兜底，放最后！）

---

### 2.4 BCrypt 密码加密

**为什么不用 MD5？**

MD5 是哈希，但相同密码结果相同，容易被彩虹表破解。BCrypt 自带随机盐值，每次加密结果都不一样，安全性高。

**用法：**

```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("123456");           // 加密存储
boolean match = encoder.matches("123456", hash);  // 校验
```

**踩坑：**

老用户（明文密码）登录会失败，因为 `matches` 期望的是 BCrypt 格式的字符串。解决办法：重新注册新用户测试。

---

### 2.5 参数校验 @Valid

**依赖：**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**常用注解：**

| 注解 | 作用 | 适用类型 |
|------|------|---------|
| `@NotBlank` | 不能为 null、空字符串、纯空格 | String |
| `@NotNull` | 不能为 null | Long、Integer 等 |
| `@Size(min=, max=)` | 长度范围 | String、Collection |

**用法：**

```java
public Result<User> register(@RequestBody @Valid UserRegisterDTO dto)
```

校验失败自动抛异常，被全局处理器捕获，无需手写 `if` 判断。

---

### 2.6 评论模块设计

**数据库表：**

```sql
CREATE TABLE comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL COMMENT '文章ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID，0表示一级评论',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
```

**`parent_id` 设计：**

- 现在：固定存 0，只做一级评论
- 以后：存被回复的评论 ID，实现多级回复（楼中楼）

---

## 三、新增文件清单

```
├── common/
│   ├── Result.java                              -- 统一响应包装类
│   └── exception/
│       ├── BusinessException.java               -- 业务异常
│       └── GlobalExceptionHandler.java          -- 全局异常处理器
├── dto/
│   ├── CommentAddDTO.java                       -- 发表评论请求DTO
│   ├── UserLoginDTO.java                        -- 登录请求DTO
│   └── UserRegisterDTO.java                     -- 注册请求DTO
├── Comment.java                                 -- 评论实体
├── CommentMapper.java                           -- 评论Mapper
├── CommentService.java                          -- 评论Service
└── CommentController.java                       -- 评论Controller
```

---

## 四、改造文件清单

| 文件 | 改造内容 |
|------|---------|
| `UserService.java` | 接入 BCrypt 密码加密；`RuntimeException` → `BusinessException`；方法参数改为 DTO |
| `UserController.java` | `@RequestParam` → `@RequestBody` + `@Valid`；返回 `Result<UserDTO>` |
| `ArticleService.java` | `RuntimeException` → `BusinessException` |
| `ArticleController.java` | 所有方法返回 `Result<...>` |
| `pom.xml` | 新增 `spring-boot-starter-validation`、`spring-security-crypto` |

---

## 五、接口清单（Day 05 后）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/register` | 用户注册（JSON） |
| POST | `/login` | 用户登录（JSON） |
| POST | `/api/articles/publish` | 发布文章 |
| GET | `/api/articles/{id}` | 查询文章 |
| GET | `/api/articles/user/{userId}` | 查询用户文章列表 |
| GET | `/api/articles` | 查询所有已发布文章 |
| DELETE | `/api/articles/{id}?userId=xxx` | 删除文章 |
| POST | `/api/comments?userId=xxx` | 发表评论 |
| GET | `/api/comments/article/{id}` | 查询文章评论 |
| DELETE | `/api/comments/{id}?userId=xxx` | 删除评论 |

---

## 六、今日踩坑记录

| 序号 | 问题 | 原因 | 解决 |
|------|------|------|------|
| 1 | `Result` 编译报错"无法解析符号 'T'" | 静态方法没写 `<T>` | `public static <T> Result<T> success()` |
| 2 | `BusinessException` 构造方法报错 | 没写 `extends RuntimeException` | 加上继承 |
| 3 | `@NotBlank` 找不到 | 缺少 validation 依赖 | `pom.xml` 加 `spring-boot-starter-validation` |
| 4 | 字段解析失败 | `privte` 拼写错误 | `private` |
| 5 | 发表评论报 500 | 数据库没建 `comment` 表 | 执行 `CREATE TABLE comment` |
| 6 | `@RequestParam userId` 没传返回"系统繁忙" | 缺少缺失参数处理器 | `GlobalExceptionHandler` 加 `MissingServletRequestParameterException` |
| 7 | `Result.success("消息")` 类型推断冲突 | 编译器把字符串当 `data` 而非 `message` | 在 `Result` 里加 `success(String message)` 方法 |
| 8 | 老用户登录报"密码错误" | 数据库里是明文密码，BCrypt 无法匹配 | 重新注册新用户测试 |

---

## 七、测试用例（test.http）

```http
### 注册
POST http://localhost:8080/register
Content-Type: application/json

{
    "username": "zhangsan",
    "password": "123456",
    "nickname": "张三"
}

### 登录
POST http://localhost:8080/login
Content-Type: application/json

{
    "username": "zhangsan",
    "password": "123456"
}

### 注册 - 测试参数校验（用户名空）
POST http://localhost:8080/register
Content-Type: application/json

{
    "username": "",
    "password": "123"
}

### 发表评论
POST http://localhost:8080/api/comments?userId=1
Content-Type: application/json

{
    "articleId": 1,
    "content": "这篇文章写得真好！"
}

### 查询文章评论列表
GET http://localhost:8080/api/comments/article/1

### 删除评论
DELETE http://localhost:8080/api/comments/1?userId=1
```

---

## 八、Git 提交记录

```bash
# 方式一：分两次提交（推荐，更清晰）
git add .
git commit -m "feat: 统一响应与全局异常处理
- 新增 Result<T> 统一响应包装
- 新增 BusinessException 业务异常
- 新增 GlobalExceptionHandler 全局异常处理器
- 新增 spring-boot-starter-validation 依赖"

git commit -m "feat: BCrypt加密 + 评论模块 + 接口改造
- UserService 接入 BCrypt 密码加密
- UserController 改为 @RequestBody + @Valid，返回 Result
- ArticleService/ArticleController 接入统一异常和响应
- 新增评论模块：Comment/Mapper/Service/Controller/DTO
- 新增 comment 数据库表"

git push origin main

# 方式二：一次提交
git add .
git commit -m "feat: Day05 统一异常 + BCrypt加密 + 评论模块"
git push origin main
```

---

## 九、明日预告

**Day 06：JWT 登录鉴权**

- 用 Token 代替传 `userId`
- 接口更安全，不用每个方法都带 `userId` 参数
- 登录状态持久化，支持多端登录
