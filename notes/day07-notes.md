# Day07：JWT 登录鉴权 + Redis 缓存 + 定时任务同步浏览量

## 今日目标
- 实现基于 JWT 的 Token 登录鉴权
- 用 Redis 缓存文章浏览量，减轻数据库压力
- 用 Spring 定时任务将 Redis 浏览量同步回 MySQL
- 掌握 ThreadLocal 在请求链路中传递用户信息

---

## 一、JWT 登录鉴权

### 1. 为什么用 JWT？
- 无状态：服务端不需要存 Session，适合分布式
- 自包含：Token 里自带用户ID、用户名、过期时间
- 前端只需在请求头带 `Authorization: Bearer <token>`

### 2. 核心组件

| 文件 | 作用 |
|------|------|
| `JwtUtil` | 生成、解析、验证 JWT Token |
| `LoginInterceptor` | 拦截请求，校验 Token，解析用户ID |
| `UserContext` | ThreadLocal 存当前登录用户，供 Controller/Service 随时取用 |
| `WebConfig` | 注册拦截器，配置白名单（登录/注册/文章列表等放行） |

### 3. 登录流程
```
前端 POST /login → 校验用户名密码 → JwtUtil.generateToken() → 返回 Token
前端后续请求 → Header 带 Authorization: Bearer <token> → LoginInterceptor 校验 → 放行
```

### 4. 白名单设计
- `/register`、`/login`：本来就是获取 Token 的，不能拦截
- `/api/articles`、`/api/articles/detail/**`：游客也能看
- `/api/comments/article/**`：评论列表游客也能看

### 5. Controller 改造
- `publish`、`delete` 不再接收 `userId` 参数，改为 `UserContext.getUserId()`
- 前端不需要传 userId，只需要带 Token 即可
- 路径调整：`/api/articles/{id}` → `/api/articles/detail/{id}`，避免白名单误放行

---

## 二、Redis 缓存浏览量

### 1. 为什么用 Redis 计数？
- 用户每次看文章都写数据库，高并发时数据库压力太大
- Redis 是内存操作，速度快，支持高并发
- 定时任务每隔一段时间把 Redis 增量同步回 MySQL

### 2. 核心组件

| 文件 | 作用 |
|------|------|
| `RedisConfig` | 配置 RedisTemplate 的序列化方式（Key 用 String，Value 用 JSON） |
| `RedisService` | 封装 Redis 常用操作（String、Hash、List、Set、ZSet、计数器） |
| `ArticleServiceImpl` | `getById()` 时 Redis 浏览量 +1，实时返回总浏览量 |

### 3. 浏览量计算
```
实时浏览量 = 数据库基础值 + Redis 增量
```

---

## 三、Spring 定时任务

### 1. 核心文件
- `ViewCountSyncTask`：每分钟执行一次，把 `article:view:*` 的增量同步到数据库

### 2. 关键注解
- `@EnableScheduling`：必须在**启动类**上加，否则定时任务不生效！
- `@Scheduled(cron = "0 * * * * ?")`：每分钟的第 0 秒执行

### 3. 执行流程
```
扫描所有 article:view:* 的 key → 提取文章ID → 查数据库 → 累加浏览量 → 删除 Redis key
```

---

## 四、遇到的问题 & 解决

### ❌ 问题1：定时任务没有执行
**原因**：启动类上只有 `@EnableAsync`，没有 `@EnableScheduling`  
**解决**：启动类加上 `@EnableScheduling`

### ❌ 问题2：拦截器误放行发布接口
**原因**：白名单 `/api/articles/**` 会同时匹配 `/api/articles/1`（详情）和 `/api/articles/publish`（发布）  
**解决**：文章详情路径改为 `/api/articles/detail/{id}`，白名单精确配置 `/api/articles/detail/**`

### ❌ 问题3：ThreadLocal 内存泄漏
**原因**：Tomcat 线程池复用线程，如果不清理 ThreadLocal，下一个请求会拿到脏数据  
**解决**：`LoginInterceptor.afterCompletion()` 中调用 `UserContext.remove()`

---

## 五、今日新增依赖（pom.xml）

```xml
<!-- JWT 支持 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

---

## 六、明日计划（Day08）

- 文章点赞功能（Redis + 定时任务同步）
- 或者：用户关注功能
- 或者：文章搜索（Elasticsearch 初步）

---

## 七、Git 提交记录

```
Day07: JWT登录鉴权 + Redis浏览量缓存 + 定时任务同步
- 新增 JwtUtil、LoginInterceptor、UserContext 实现 Token 鉴权
- 新增 RedisConfig、RedisService 封装 Redis 操作
- 新增 ViewCountSyncTask 定时同步浏览量到 MySQL
- 改造 ArticleController、CommentController 从 UserContext 取用户ID
- 启动类添加 @EnableScheduling 开启定时任务
```
