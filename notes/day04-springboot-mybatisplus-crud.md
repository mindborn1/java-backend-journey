# Day04：简知社区项目启动 — Spring Boot 3.2 + 用户/文章模块

> 日期：2026-08-09  
> 主题：Spring Boot 项目搭建、MyBatis-Plus 集成、用户注册/登录、文章 CRUD  
> 状态：✅ 已完成

---

## 一、今日完成内容

- [x] Spring Initializr 创建 Spring Boot 3.2 项目
- [x] 配置 pom.xml（Spring Boot 3.2 + MyBatis-Plus + MySQL + Lombok）
- [x] 配置 application.yml（数据库连接、MyBatis-Plus 配置）
- [x] 建数据库 jianzhi_community + 用户表 + 文章表
- [x] 用户模块：注册接口 + 登录接口
- [x] 文章模块：发布 + 查询 + 软删除
- [x] 自动填充时间字段（MetaObjectHandler）
- [x] UserDTO 隐藏敏感数据（password）
- [x] 测试所有接口（HTTP Client）

---

## 二、项目结构

```
jianzhi-community/
├── pom.xml
├── src/main/
│   ├── java/com/mindborn/jianzhicommunity/
│   │   ├── JianzhiCommunityApplication.java  # 主启动类
│   │   ├── User.java                         # 用户实体
│   │   ├── UserMapper.java                   # 用户 Mapper
│   │   ├── UserService.java                  # 用户 Service
│   │   ├── UserController.java               # 用户 Controller
│   │   ├── UserDTO.java                      # 用户 DTO（隐藏密码）
│   │   ├── Article.java                      # 文章实体
│   │   ├── ArticleMapper.java                # 文章 Mapper
│   │   ├── ArticleService.java               # 文章 Service
│   │   ├── ArticleController.java            # 文章 Controller
│   │   └── MyMetaObjectHandler.java          # 自动填充处理器
│   └── resources/
│       ├── application.yml                   # 配置文件
│       └── test.http                         # 接口测试文件
```

---

## 三、核心技术点

### 3.1 Spring Boot 3.2 + MyBatis-Plus

**pom.xml 关键依赖：**

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<!-- MyBatis-Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.5</version>
</dependency>
```

**application.yml 配置：**

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/jianzhi_community?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 913625

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 驼峰命名转换
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 打印 SQL
```

### 3.2 MyBatis-Plus 单表 CRUD 零 SQL

**Mapper 接口只需继承 BaseMapper：**

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 不用写任何方法，BaseMapper 已经提供了全部 CRUD
}
```

**自动获得的方法：**
- `insert(entity)` — 插入
- `selectById(id)` — 根据 ID 查询
- `selectList(wrapper)` — 条件查询
- `updateById(entity)` — 根据 ID 更新
- `deleteById(id)` — 根据 ID 删除
- `selectCount(wrapper)` — 统计数量

### 3.3 自动填充时间字段

**实体类字段加注解：**

```java
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;
```

**创建 MetaObjectHandler：**

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

### 3.4 DTO 隐藏敏感数据

**UserDTO 不包含 password 字段：**

```java
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    // ... 其他字段
    // 注意：没有 password！
}
```

**Controller 里转换：**

```java
@PostMapping("/register")
public UserDTO register(...) {
    User user = userService.register(...);
    return convertToDTO(user);  // 转成 DTO，去掉 password
}
```

### 3.5 软删除

**不真删数据，只改 status：**

```java
public void delete(Long id, Long userId) {
    Article article = articleMapper.selectById(id);
    if (!article.getUserId().equals(userId)) {
        throw new RuntimeException("不能删除别人的文章");
    }
    article.setStatus(2);  // 2 = 已删除
    articleMapper.updateById(article);
}
```

---

## 四、接口清单

### 用户接口

| 方法 | 路径 | 参数 | 作用 |
|------|------|------|------|
| POST | `/register` | username, password, nickname | 用户注册 |
| POST | `/login` | username, password | 用户登录 |

### 文章接口

| 方法 | 路径 | 参数 | 作用 |
|------|------|------|------|
| POST | `/api/articles/publish` | title, content, userId | 发布文章 |
| GET | `/api/articles/{id}` | - | 查询单篇文章 |
| GET | `/api/articles/user/{userId}` | - | 查询用户文章 |
| GET | `/api/articles` | - | 查询所有已发布文章 |
| DELETE | `/api/articles/{id}?userId=xxx` | userId | 软删除文章 |

---

## 五、数据库表结构

### 用户表

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 文章表

```sql
CREATE TABLE article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    user_id BIGINT NOT NULL,
    status TINYINT DEFAULT 1,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 六、遇到的问题与解决

### 问题 1：Spring Boot 版本不兼容

**现象**：`sqlSessionFactory` 创建失败

**原因**：Spring Initializr 默认选了 4.1.0，但 MyBatis-Plus 3.5.5 只支持 Spring Boot 3.x

**解决**：把 pom.xml 里的 Spring Boot 版本改成 3.2.0

### 问题 2：找不到主类

**现象**：`ClassNotFoundException: JianzhiCommunityApplication`

**原因**：`src/main/java` 没有被标记为源码目录

**解决**：右键 `src/main/java` → 标记目录为 → 源根

### 问题 3：MySQL 端口写错

**现象**：连接数据库失败

**原因**：application.yml 里端口写成了 3386，应该是 3306

**解决**：改成正确的端口 3306

---

## 七、面试知识点

**Q1：什么是 DTO？为什么要用 DTO？**

DTO（Data Transfer Object）是数据传输对象，用于控制返回给前端的数据格式。

原因：
1. 隐藏敏感字段（如密码）
2. 解耦数据库实体和接口返回
3. 可以自定义返回格式（如格式化时间）

**Q2：什么是软删除？为什么要软删除？**

软删除是不真删数据，只改一个状态字段（如 status = 2）。

原因：
1. 数据可恢复
2. 保留历史记录
3. 避免外键关联问题

**Q3：MyBatis-Plus 的 BaseMapper 提供了哪些方法？**

- insert(entity)
- selectById(id)
- selectList(wrapper)
- updateById(entity)
- deleteById(id)
- selectCount(wrapper)
- 等等...

---

## 八、明日计划

### Day 05：评论模块 + 密码加密

1. 建评论表 + Comment 实体类
2. 评论的增删查接口
3. BCrypt 密码加密（spring-security-core）
4. 统一异常处理（@ControllerAdvice）
