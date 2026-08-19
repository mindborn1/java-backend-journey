# Day10 —— Java 21 新特性（下）+ Optional

> 学习内容：Virtual Threads、Text Blocks、Sealed Classes、Optional 深度使用 + 项目重构

---

## 一、Virtual Threads（虚拟线程）

### 1.1 为什么需要虚拟线程？

传统**平台线程**（Platform Thread）是 OS 线程的 1:1 映射：

```
Java 平台线程 ──→ 操作系统线程 ──→ CPU 核心
```

问题：
- **创建成本高**：OS 线程是稀缺资源，一台机器通常只能开几千个
- **切换成本高**：线程上下文切换需要内核态参与
- **阻塞浪费**：一个线程阻塞在 I/O 上，OS 线程就被占着，啥也干不了

### 1.2 虚拟线程是什么？

虚拟线程是 **JVM 层面** 管理的轻量级线程，由 JVM 调度，**不直接绑定 OS 线程**：

```
虚拟线程（10万个）──→ JVM 调度器 ──→ 少量平台线程（如 8 个）──→ OS 线程
```

| 特性 | 平台线程 | 虚拟线程 |
|------|----------|----------|
| 创建成本 | 高（~1MB 栈空间） | 极低（~几百字节） |
| 数量 | 几千个就吃力 | 轻松百万级 |
| 阻塞时 | 占用 OS 线程 | **不占用** OS 线程，JVM 把平台线程让给别人 |
| 调度 | OS 内核调度 | JVM 用户态调度 |

### 1.3 核心原理：Mount / Unmount

```
虚拟线程 A 正在运行 ──Mount──→ 平台线程 1
        ↓ 遇到 I/O 阻塞（sleep/socket.read等）
虚拟线程 A 被 Unmount，挂起等待
        ↓
平台线程 1 去执行 虚拟线程 B
        ↓
I/O 完成，虚拟线程 A 重新 Mount 到某个平台线程继续执行
```

整个过程对开发者**完全透明**。

### 1.4 创建虚拟线程的三种方式

```java
// 方式1：直接启动（最简洁）
Thread.startVirtualThread(() -> {
    System.out.println("虚拟线程运行中: " + Thread.currentThread());
});

// 方式2：用 Thread.Builder
Thread vt = Thread.ofVirtual()
    .name("my-vthread-", 0)
    .unstarted(() -> { /* 任务 */ });
vt.start();

// 方式3：用 ExecutorService（推荐，和现有代码兼容）
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> { /* 任务 */ });
}
```

### 1.5 使用场景 & 注意事项

**适合**：高并发 I/O 密集型（Web 服务、数据库查询、HTTP 调用）

**不适合**：
- CPU 密集型（虚拟线程不会比平台线程快）
- `synchronized` 块内阻塞、调用 native 方法时会"钉住"平台线程
  - 解决：用 `ReentrantLock` 替代 `synchronized`

**注意**：
- 虚拟线程的 `ThreadLocal` 开销比平台线程大，尽量少用
- 虚拟线程是 **daemon 线程**，主线程结束时它们不会阻止 JVM 退出

### 1.6 Demo 代码

```java
package com.mindborn.day10;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;

public class VirtualThreadDemo {
    public static void main(String[] args) throws Exception {
        Runnable task = () -> {
            try { Thread.sleep(100); } 
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        };

        int count = 1000;

        // 平台线程（线程池 100）
        Instant start1 = Instant.now();
        try (var executor = Executors.newFixedThreadPool(100)) {
            for (int i = 0; i < count; i++) executor.submit(task);
        }
        System.out.println("平台线程耗时: " + Duration.between(start1, Instant.now()).toMillis() + "ms");

        // 虚拟线程
        Instant start2 = Instant.now();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < count; i++) executor.submit(task);
        }
        System.out.println("虚拟线程耗时: " + Duration.between(start2, Instant.now()).toMillis() + "ms");
    }
}
```

---

## 二、Text Blocks（文本块）

### 2.1 为什么需要文本块？

传统多行字符串：

```java
String sql = "SELECT id, username, email, status, create_time, update_time " +
             "FROM user " +
             "WHERE status = 1 " +
             "  AND create_time > '2024-01-01' " +
             "ORDER BY create_time DESC " +
             "LIMIT 10 OFFSET 0";
```

文本块写法：

```java
String sql = """
    SELECT id, username, email, status, create_time, update_time
    FROM user
    WHERE status = 1
      AND create_time > '2024-01-01'
    ORDER BY create_time DESC
    LIMIT 10 OFFSET 0
    """;
```

### 2.2 基本规则

```java
// 文本块以 """ 开头，后面必须跟换行
String html = """
    <html>
        <body>
            <h1>Hello, 简知社区!</h1>
        </body>
    </html>
    """;
```

**缩进控制**：结束符 `"""` 所在列决定去掉多少前导空格。

```java
public void test() {
    String s = """
        Hello
        World
        """;  // """ 在最左列，每行前面的4个空格会被去掉
    // 结果："Hello\nWorld\n"
}
```

### 2.3 转义控制

| 符号 | 作用 |
|------|------|
| `\`（行尾） | 不要自动换行，合并多行 |
| `\s` | 保留末尾空格 |

```java
// 行尾 \ 合并多行
String inlineSql = """
    SELECT id, username, email \
    FROM user \
    WHERE status = 1
    """;
// 结果："SELECT id, username, email FROM user WHERE status = 1\n"

// formatted 格式化
String dynamicSql = """
    SELECT * FROM %s WHERE id = %d
    """.formatted("user", 10086);
```

### 2.4 使用原则

> **凡是有多行字符串的地方（SQL、JSON、HTML），一律用文本块。**

---

## 三、Sealed Classes（密封类）

### 3.1 为什么需要密封类？

传统继承是"开放的"——任何人都能继承你的类，设计意图不明确。

### 3.2 语法

```java
// sealed 关键字 + permits 子句，明确列出允许的子类
public sealed class Animal permits Dog, Cat, Bird {
    public void makeSound() { }
}

// 子类必须是以下三种之一：
public final class Dog extends Animal { }        // 1. final，不能再被继承
public sealed class Bird extends Animal permits Sparrow, Eagle { }  // 2. 继续密封
public non-sealed class Cat extends Animal { }   // 3. 打破密封，恢复开放继承
```

### 3.3 配合 Pattern Matching Switch（穷尽匹配）

```java
public String describe(Animal animal) {
    return switch (animal) {
        case Dog d -> "狗";
        case Cat c -> "猫";
        case Bird b -> "鸟";
        // 不需要 default！编译器知道只有 Dog/Cat/Bird 三种
    };
}
```

### 3.4 关键字速查

| 关键字 | 含义 |
|--------|------|
| `sealed` | 声明密封类/接口 |
| `permits` | 列出允许的子类 |
| `final` | 子类不能再被继承 |
| `non-sealed` | 子类打破密封，恢复开放 |

> 密封类 = **设计意图显性化** + **模式匹配更安全**。

---

## 四、Optional 类深度使用

### 4.1 为什么用 Optional？

**传统写法**：

```java
User user = userMapper.selectById(id);
if (user != null) {
    String email = user.getEmail();
    if (email != null) {
        sendEmail(email);
    }
}
```

**Optional 写法**：

```java
Optional.ofNullable(userMapper.selectById(id))
    .map(User::getEmail)
    .ifPresent(this::sendEmail);
```

### 4.2 创建 Optional 的三种方式

```java
Optional.ofNullable(userMapper.selectById(1L));  // 值可能为 null（最常用）
Optional.of("mindborn");                          // 值一定不为 null
Optional.empty();                                 // 明确表示"没有值"
```

### 4.3 常用 API 速查

| API | 作用 |
|-----|------|
| `ifPresent(Consumer)` | 有值就消费 |
| `orElse(T)` | 没值返回默认值（**每次都会创建对象**） |
| `orElseGet(Supplier)` | 没值才创建默认值（**懒加载，推荐**） |
| `orElseThrow()` | 没值抛异常 |
| `map(Function)` | 有值就转换 |
| `flatMap(Function)` | map 的返回值也是 Optional 时用 |
| `filter(Predicate)` | 有值且满足条件才保留 |

### 4.4 常见错误

**错误 1：isPresent + get（和 if-null 没区别）**

```java
// 不要这样写
if (opt.isPresent()) {
    User u = opt.get();
    System.out.println(u.getUsername());
}

// 正确
opt.ifPresent(u -> System.out.println(u.getUsername()));
```

**错误 2：无脑 orElse（性能浪费）**

```java
User user = opt.orElse(new User());      // 每次都会 new User()
User user = opt.orElseGet(User::new);    // 正确：只有为空时才创建
```

**错误 3：字段/参数用 Optional**

```java
// 不要这样
private Optional<String> email;

// 正确：字段保持普通类型，在方法返回值上用 Optional
public Optional<String> getEmailOpt() {
    return Optional.ofNullable(this.email);
}
```

> **Oracle 官方推荐**：Optional 主要用于**方法返回值**。

### 4.5 链式操作

```java
// 获取用户的部门名称（用户→部门→名称，每一层都可能为 null）
String deptName = Optional.ofNullable(user)
    .map(User::getDepartment)
    .map(Department::getName)
    .orElse("未知部门");
```

### 4.6 项目实战：重构 UserServiceImpl

**重构前**：

```java
@Override
public User login(UserLoginDTO dto) {
    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("username", dto.getUsername());
    User user = userMapper.selectOne(queryWrapper);

    if (user == null) {
        throw new BusinessException("用户不存在：" + dto.getUsername());
    }
    // ...
}
```

**重构后**：

```java
@Override
public User login(UserLoginDTO dto) {
    User user = Optional.ofNullable(
            userMapper.selectOne(
                new QueryWrapper<User>().eq("username", dto.getUsername())
            )
        )
        .orElseThrow(() -> new BusinessException("用户不存在：" + dto.getUsername()));

    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
        throw new BusinessException("密码错误");
    }
    if (user.getStatus() == 0) {
        throw new BusinessException("账号已被禁用");
    }
    return user;
}
```

**新增方法**：

```java
// 根据 ID 查询，不存在抛异常（Service 层内部调用）
public User getUserById(Long id) {
    return Optional.ofNullable(userMapper.selectById(id))
        .orElseThrow(() -> new BusinessException("用户不存在，id=" + id));
}

// 根据 ID 查询用户名，不存在返回"匿名用户"
public String getUserName(Long id) {
    return Optional.ofNullable(userMapper.selectById(id))
        .map(User::getUsername)
        .orElse("匿名用户");
}
```

### 4.7 Optional 使用原则

| 场景 | 推荐写法 |
|------|----------|
| 查询可能为 null，不存在抛异常 | `.orElseThrow(() -> new XxxException("..."))` |
| 查询可能为 null，不存在给默认值 | `.orElse(default)` / `.orElseGet(Supplier)` |
| 取对象的某个属性 | `.map(属性getter)` |
| 连续取多层属性 | `.map(...).map(...).orElse(...)` |
| 条件过滤后再取值 | `.filter(条件).map(...).orElse(...)` |

> Optional 不是银弹，它让"值可能不存在"这个意图变得**显式**和**链式**。

---

## 五、今日总结

| 特性 | 核心要点 | 项目应用 |
|------|----------|----------|
| Virtual Threads | I/O 密集型高并发，写同步代码获得异步性能 | Demo 练习，后续 Web 服务可用 |
| Text Blocks | 多行字符串用 `"""`，所见即所得 | 养成习惯，后续 @Select / JSON 用 |
| Sealed Classes | 限制继承层次，配合 switch 穷尽匹配 | 用户权限体系设计 |
| Optional | 消灭 if-null，链式处理可能为 null 的值 | **已重构 UserServiceImpl** |

---

## 六、Git 提交

```bash
git add .
git commit -m "Day10: Java21新特性+Optional重构"
```

---

*笔记完成时间：Day10*
