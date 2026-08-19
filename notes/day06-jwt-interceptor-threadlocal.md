# Day06 - JWT 登录鉴权

> 日期：2026-08-13
> 主题：JWT 登录鉴权 + 项目结构重构

---

## 完成清单

| 模块 | 状态 |
|------|------|
| JWT Token 生成与解析 | ✅ |
| 登录接口返回 Token | ✅ |
| 登录拦截器校验 Token | ✅ |
| ThreadLocal 用户上下文 | ✅ |
| 获取当前登录用户 `/me` | ✅ |
| 项目结构重构（标准分层） | ✅ |
| Controller 注入 Service 接口 | ✅ |
| 文章/评论接口去掉 `userId` 参数 | ✅ |
| 白名单精确配置 | ✅ |

---

## 核心知识点

### 1. JWT 工作流程

```
前端登录 ──→ 后端校验密码 ──→ 生成 JWT Token ──→ 前端保存 Token
    ↑                                                    ↓
前端收到响应 ←── 后端验签放行 ←── 请求头带 Token ───────┘
```

### 2. ThreadLocal 原理

```java
// 每个线程独立存储，线程之间互不干扰
ThreadLocal<User> currentUser = new ThreadLocal<>();

// 请求开始时（拦截器）存入
UserContext.setUser(user);

// 请求中任何地方取出
UserContext.getUser();

// 请求结束时（拦截器）必须清理！
UserContext.remove();
```

**为什么必须 `remove()`？**

Tomcat 线程池复用线程，不清理会导致下一个请求拿到上一个用户的脏数据。

### 3. 拦截器三阶段

| 方法 | 执行时机 | 用途 |
|------|----------|------|
| `preHandle` | Controller 之前 | 校验 Token、权限判断 |
| `postHandle` | Controller 之后 | 修改 ModelAndView（很少用） |
| `afterCompletion` | 请求完全结束后 | 清理资源（ThreadLocal） |

### 4. 白名单设计原则

- **精确匹配**：`/api/articles/detail/**` ✅，`/api/articles/**` ❌（会误放行 `/publish`）
- **登录/注册必须放行**：不然拿不到 Token
- **只读接口可放行**：文章列表、详情、评论列表

---

## 新增/修改文件清单

```
com.mindborn.jianzhicommunity
├── common/
│   ├── JwtUtil.java                    ← 新增：JWT 生成/解析/验证
│   └── UserContext.java                ← 新增：ThreadLocal 用户上下文
├── config/
│   └── WebConfig.java                  ← 新增：注册拦截器 + 白名单
├── interceptor/
│   └── LoginInterceptor.java            ← 新增：Token 校验
├── service/                            ← 新增：接口层
│   ├── ArticleService.java
│   ├── CommentService.java
│   ├── UserService.java                ← 新增 getById() 供拦截器用
│   └── impl/                           ← 原 Service 类改名移入
│       ├── ArticleServiceImpl.java
│       ├── CommentServiceImpl.java
│       └── UserServiceImpl.java
├── controller/
│   ├── ArticleController.java           ← 修改：去掉 userId 参数，从 UserContext 取
│   ├── CommentController.java           ← 修改：同上
│   └── UserController.java              ← 修改：login 返回 Token，新增 /me
├── entity/                              ← 原根包下实体类移入
├── mapper/                              ← 原根包下 Mapper 移入
└── dto/
    └── UserDTO.java                     ← 从根包移入
```

---

## 踩坑记录

| 坑 | 原因 | 解决 |
|----|------|------|
| `@MapperScan` 范围太大 | 扫描了 `service` 包，把 `UserService` 当成 Mapper | 改成 `@MapperScan("...mapper")` |
| 旧密码无法登录 | 之前用 `org.mindrot.bcrypt`，现在用 Spring Security 的 `BCryptPasswordEncoder`，格式不兼容 | 重新注册用户 |
| `/api/articles/**` 误放行发布接口 | `**` 匹配了 `/publish`，导致没走拦截器 | 白名单改成精确路径 `/api/articles/detail/**` |
| `UserContext` import 错误 | `User` 类移到 `entity` 包后，`UserContext` 的 import 没改 | 改成 `import ...entity.User` |
| `/me` 返回 500 | ThreadLocal 里存的是 `entity.User`，取的时候类型不匹配导致 null | 修正 import 后清理 target 重启 |

---

## Day07 预告

| 主题 | 内容 |
|------|------|
| **AOP 日志** | 用切面统一记录接口入参、出参、耗时 |
| **接口文档** | 集成 Swagger / Knife4j，自动生成 API 文档 |
| **分页查询** | MyBatis-Plus 分页插件，文章列表支持分页 |
| **文件上传** | 用户头像上传，存本地或 OSS |

---

## 一句话总结

> **JWT 让服务端无状态，ThreadLocal 让代码无侵入，拦截器让权限无感知。**
