# Day03：线程池收尾 + CompletableFuture 异步编程

> 日期：2026-08-08
> 主题：execute vs submit、Future 超时/异常、CompletableFuture 链式调用
> 状态：✅ 已完成

---

## 一、今日完成内容

- [x] ExecuteVsSubmitDemo — execute() vs submit() 本质区别
- [x] FutureTimeoutDemo — Future 超时控制 get(timeout, unit)
- [x] FutureExceptionDemo — execute 和 submit 异常处理差异
- [x] CompletableFutureDemo — 链式调用 + exceptionally 异常兜底

---

## 二、execute() vs submit() 详解

### 2.1 核心区别

| 对比项 | execute() | submit() |
|--------|-----------|----------|
| 参数 | 只能传 Runnable | 可以传 Runnable 或 Callable |
| 返回值 | void（无返回值） | Future\<T\>（欠条） |
| 异常处理 | 异常直接抛出，无法在外部捕获 | 异常被 Future 兜住，get() 时抛出 |

### 2.2 Future 的五大方法

| 方法 | 作用 |
|------|------|
| get() | 阻塞等待结果（可能永久阻塞） |
| get(timeout, unit) | 限时等待，超时抛 TimeoutException |
| cancel(true) | 取消任务（true=允许中断） |
| isDone() | 查询是否完成 |
| isCancelled() | 查询是否已取消 |

### 2.3 超时控制（生产环境必用）

```java
// ❌ 不推荐：无限等待，可能永久阻塞
String result = future.get();

// ✅ 推荐：最多等 2 秒，超时就放弃
try {
    String result = future.get(2, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true); // 超时后主动取消任务
}
```

### 2.4 异常处理差异

- `execute()` 里的异常 → 直接炸在子线程，外部 try-catch **抓不到**
- `submit()` 里的异常 → 被 Future 兜住，`get()` 时以 `ExecutionException` 抛出，**可以优雅处理**

```java
// submit 的异常处理
Future<String> future = pool.submit(() -> {
    throw new RuntimeException("数据库连接超时");
});
try {
    future.get();
} catch (ExecutionException e) {
    // getCause() 拿到原始异常
    System.out.println(e.getCause().getMessage());
}
```

**面试金句**：
> "execute 的异常在线程池线程里直接抛出，调用方无法捕获；submit 的异常被封装在 Future 中，调用 get() 时以 ExecutionException 形式暴露，可以优雅处理。所以生产环境推荐用 submit。"

---

## 三、CompletableFuture 详解

### 3.1 为什么需要 CompletableFuture？

| Future 的痛点 | CompletableFuture 的解法 |
|---|---|
| get() 阻塞，没法链式处理 | thenApply() 自动触发下一步 |
| 多个 Future 无法编排 | thenCompose() 串联多个异步任务 |
| 异常处理麻烦 | exceptionally() 一行搞定 |
| 无法手动完成 | complete() 主动设置结果 |

**一句话**：Future 是"欠条"，CompletableFuture 是"欠条 + 自动兑现 + 链式操作"。

### 3.2 核心方法

| 方法 | 作用 | 类比 |
|------|------|------|
| supplyAsync() | 异步执行有返回值的任务 | 创建任务 |
| thenApply() | 拿到结果后继续处理（有返回值） | Stream.map() |
| thenAccept() | 拿到结果后消费（无返回值） | Stream.forEach() |
| thenCompose() | 串联两个异步任务 | Stream.flatMap() |
| exceptionally() | 异常兜底，返回降级值 | try-catch |
| join() | 阻塞等待结果 | Future.get() |

### 3.3 链式调用示例

```java
CompletableFuture.supplyAsync(() -> {
    return "张三";          // 第1步：查用户
}).thenApply(name -> {
    return "你好，" + name;  // 第2步：拼问候语
}).thenAccept(message -> {
    System.out.println(message); // 第3步：打印结果
});
```

**流程**：三步自动串联，不需要手动 get() 再传参。

### 3.4 异常兜底 exceptionally

```java
CompletableFuture.supplyAsync(() -> {
    throw new RuntimeException("数据库连接超时");
}).exceptionally(ex -> {
    return "默认用户（兜底）"; // 异常时返回降级值
}).thenAccept(result -> {
    System.out.println(result); // 照样执行
});
```

**关键点**：即使出异常，链式调用**不会断**，后面的 thenAccept 照样执行。这就是**服务降级**的思想。

---

## 四、今日代码文件清单

| 文件 | 路径 |
|------|------|
| ExecuteVsSubmitDemo | day03/ExecuteVsSubmitDemo.java |
| FutureTimeoutDemo | day03/FutureTimeoutDemo.java |
| FutureExceptionDemo | day03/FutureExceptionDemo.java |
| CompletableFutureDemo | day03/CompletableFutureDemo.java |

---

## 五、面试自问自答

**Q1：Future 和 CompletableFuture 的区别？**

Future 是 Java 5 引入的异步计算结果，只能 get() 阻塞等待或轮询 isDone()。
CompletableFuture 是 Java 8 引入的，支持链式调用、任务编排、异常兜底，功能强大得多。

**Q2：为什么生产环境不用 get() 而用 get(timeout, unit)？**

get() 会无限阻塞，如果任务卡死（比如数据库连接池耗尽），主线程就永远等下去。
get(timeout, unit) 最多等 N 秒，超时后可以取消任务、返回降级值，保证系统可用性。

**Q3：CompletableFuture 默认用什么线程池？**

默认用 ForkJoinPool.commonPool()（公共线程池，线程数 = CPU核心数 - 1）。
生产环境建议传入自定义线程池，避免和其他任务竞争。

---

## 六、明日计划

### Day 04：「简知社区」项目启动

1. Spring Initializr 创建 Spring Boot 3.x 项目
2. 配置 pom.xml（Spring Boot 3.2 + MySQL + MyBatis-Plus + Lombok）
3. 设计数据库表：用户表、文章表、评论表
4. 第一个接口：用户注册（Controller + Service + Mapper）
