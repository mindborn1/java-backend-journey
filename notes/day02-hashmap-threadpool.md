# Day02：Java并发基础 — 线程池与集合框架

## 一、今日完成内容
- [x] HashMapDemo — HashMap 底层原理与源码级理解
- [x] ListDemo — ArrayList vs LinkedList 对比
- [x] ThreadPoolDemo — 线程池7大参数、execute/submit、优雅关闭
- [x] 四种拒绝策略独立Demo
  - AbortPolicyDemo.java
  - CallerRunsPolicyDemo.java
  - DiscardPolicyDemo.java
  - DiscardOldestPolicyDemo.java
- [x] Lambda 表达式深入理解
- [x] Git 仓库整理、.gitignore 修正、重复文件夹清理

---

## 二、HashMap 详解

### 2.1 底层数据结构（JDK 8）
- **数组 + 链表 + 红黑树**
- 数组的每个位置叫"桶"（bucket），默认长度 16
- 当链表长度 ≥ 8 且数组长度 ≥ 64 时，链表转为红黑树
- 当红黑树节点数 ≤ 6 时，退化为链表

### 2.2 put() 执行流程
1. 计算 key 的 hash 值：`hash = (h = key.hashCode()) ^ (h >>> 16)`
2. 计算数组下标：`index = (n - 1) & hash`（等价于 hash % n，但位运算更快）
3. 如果该位置为空，直接放入
4. 如果该位置有值，遍历链表/红黑树，用 equals() 比较：
   - key 相同 → 覆盖旧 value
   - key 不同 → 尾插法插入链表尾部（JDK 7 是头插法，JDK 8 改为尾插法，避免并发死循环）

### 2.3 扩容机制
- **扩容阈值**：数组长度 × 负载因子（默认 0.75），即 16 × 0.75 = 12
- 当元素个数超过 12 时，触发扩容
- **扩容大小**：变为原来的 2 倍（16 → 32 → 64）
- 扩容后，元素重新计算下标（要么在原位置，要么在原位置 + 旧容量）

### 2.4 为什么容量必须是 2 的幂次？
- 为了让 `(n - 1) & hash` 等价于 `hash % n`
- 2的幂次 - 1 的二进制全是1（如15=1111），与运算能均匀散列
- 如果不是2的幂次，某些桶永远用不上，造成哈希冲突

### 2.5 线程安全问题
- **HashMap 不是线程安全的**
- 多线程环境下：
  - JDK 7：头插法 + 并发扩容 → 死循环（经典面试题）
  - JDK 8：尾插法，不会死循环，但会丢数据
- **解决方案**：
  - `Collections.synchronizedMap(new HashMap<>())`
  - `ConcurrentHashMap`（推荐，分段锁/CAS，性能高）

---

## 三、List 详解

### 3.1 ArrayList
- **底层**：Object 数组
- **特点**：查询快 O(1)，增删慢 O(n)
- **扩容**：默认初始容量 10，每次扩容为原来的 1.5 倍（`oldCapacity + (oldCapacity >> 1)`）
- **线程安全**：不安全。多线程并发 add 可能导致数组越界或数据覆盖

### 3.2 LinkedList
- **底层**：双向链表（Node 包含 prev、item、next）
- **特点**：查询慢 O(n)，增删快 O(1)（已知节点位置时）
- **内存占用**：比 ArrayList 大，每个节点要存两个指针
- **线程安全**：不安全

### 3.3 如何选择？
| 场景 | 推荐 |
|------|------|
| 随机访问多（get(i)） | ArrayList |
| 频繁增删（头尾插入） | LinkedList |
| 不知道选哪个 | ArrayList（大部分场景查询更多）|

---

## 四、线程池详解

### 4.1 为什么要用线程池？
1. **降低资源消耗**：复用线程，减少创建/销毁开销
2. **提高响应速度**：任务到达时，线程已存在，无需等待创建
3. **便于管理**：统一控制并发数，防止资源耗尽

### 4.2 线程池7大参数（面试必背）

| 参数 | 含义 | 面试考点 |
|------|------|---------|
| corePoolSize | 核心线程数 | 即使空闲也保留，除非设置 allowCoreThreadTimeOut |
| maximumPoolSize | 最大线程数 | = 核心线程 + 非核心线程 |
| keepAliveTime | 非核心线程存活时间 | 超过这个时间没任务，非核心线程被回收 |
| unit | 时间单位 | TimeUnit.SECONDS / MILLISECONDS 等 |
| workQueue | 任务等待队列 | 常用 LinkedBlockingQueue、ArrayBlockingQueue |
| threadFactory | 线程工厂 | 自定义线程名，方便排查问题 |
| handler | 拒绝策略 | 4种，见下方详解 |

### 4.3 线程池执行流程（面试高频）

```
提交任务 → 核心线程是否已满？
  - 否 → 创建核心线程执行任务
  - 是 → 队列是否已满？
    - 否 → 任务进入队列等待
    - 是 → 当前线程数 < maximumPoolSize？
      - 是 → 创建非核心线程执行任务
      - 否 → 执行拒绝策略
```

### 4.4 execute() vs submit()

| 对比项 | execute() | submit() |
|--------|-----------|----------|
| 参数 | Runnable | Runnable 或 Callable |
| 返回值 | void | Future<T> |
| 异常处理 | 异常直接抛出，无法在外部捕获 | 异常被封装在 Future 中，调用 get() 时抛出 |
| 适用场景 | 不需要结果的任务 | 需要结果或需要异常处理的任务 |

### 4.5 Future 和 Callable

**Callable**：有返回值、可以抛异常的任务接口

```java
Callable<String> task = () -> {
    Thread.sleep(1000);
    return "任务结果";
};
```

**Future**：异步计算的结果，提供了获取结果、取消任务等方法
- `future.get()`：阻塞等待结果
- `future.get(timeout, unit)`：限时等待
- `future.cancel(boolean mayInterruptIfRunning)`：取消任务
- `future.isDone()`：是否已完成
- `future.isCancelled()`：是否已取消

---

## 五、四种拒绝策略详解

### 5.1 AbortPolicy（中止策略）
- **行为**：直接抛出 RejectedExecutionException
- **特点**：最"诚实"的策略，让调用方立刻知道系统负载过高
- **风险**：如果不 try-catch，程序直接崩溃
- **适用场景**：核心业务，不能容忍静默失败，需要快速感知问题

### 5.2 CallerRunsPolicy（调用者运行策略）
- **行为**：让提交任务的线程（调用者）自己去执行这个任务
- **特点**：任务不会丢，但调用者线程会被阻塞
- **副作用**：相当于给提交方"降速"，自然起到流量削峰的作用
- **适用场景**：任务重要不能丢，但可以接受慢一点执行；希望用简单方式做背压

### 5.3 DiscardPolicy（静默丢弃策略）
- **行为**：直接丢弃新任务，什么都不做，不抛异常
- **特点**：最"危险"的策略，任务丢了完全无感知
- **风险**：数据丢失，调用方以为任务已提交成功
- **适用场景**：非关键任务，如日志、埋点、统计，丢了不影响业务

### 5.4 DiscardOldestPolicy（丢弃最老策略）
- **行为**：丢弃队列里等待最久的任务，然后把新任务放入队列
- **特点**：牺牲旧任务，保全新任务
- **风险**：旧任务被丢弃
- **适用场景**：新数据比旧数据更有价值，如实时行情、消息推送

### 5.5 对比总结表

| 策略 | 满了之后 | 任务会丢吗 | 调用者感知 | 适用场景 |
|------|---------|-----------|-----------|---------|
| AbortPolicy | 抛异常 | 会丢 | 收到异常 | 核心业务，需快速失败 |
| CallerRunsPolicy | 调用者自己执行 | 不会丢 | 线程被阻塞 | 不能丢任务，可降速 |
| DiscardPolicy | 静默丢弃新任务 | 新任务丢 | 完全不知道 | 非关键任务 |
| DiscardOldestPolicy | 丢弃最老，放入新任务 | 老任务丢 | 完全不知道 | 新任务更重要 |

---

## 六、Lambda 表达式

### 6.1 本质
- 函数式接口的匿名实现简写
- 函数式接口：只有一个抽象方法的接口（如 Runnable、Callable、Comparator）

### 6.2 语法

```java
(参数列表) -> { 方法体 }

// 无参数
() -> System.out.println("hello");

// 一个参数（括号可省略）
x -> x * 2;

// 多个参数
(a, b) -> a + b;
```

### 6.3 限制
- Lambda 里只能访问 **final** 或 **effectively final** 的外部变量
- 不能直接使用循环变量 `i`，必须 `final int taskId = i;`

---

## 七、遇到的问题与解决

### 问题1：LinkedBlockingQuene 拼写错误
- **现象**：编译报错 `找不到符号: 类 LinkedBlockingQuene`
- **原因**：Queue 拼成了 Quene
- **解决**：改为 `LinkedBlockingQueue<>()`

### 问题2：IDEA 旧文件全变红色
- **现象**：day01 和 day02 的旧 Demo 文件全红，但新建的文件正常，能编译运行
- **排查过程**：
  1. 检查项目结构 → SDK 正确（JDK 21）
  2. 检查模块 → 源代码根目录正确
  3. 检查语言级别 → 21 正确
  4. 检查编译器输出 → 已配置
  5. 检查 pom.xml → 无报错
  6. 清除缓存、重新加载 Maven、删除 .idea 均无效
- **最终原因**：未知（可能是 .idea 索引损坏）
- **解决**：执行 Git add/commit 操作后，IDEA 重新索引，红色自动消失

### 问题3：Git 仓库存在重复文件夹
- **现象**：仓库根目录同时存在 `day01-java-basics`（旧）和 `java-backend-code`（新）
- **解决**：`git rm -r day01-java-basics` 删除旧文件夹，保留新的 Maven 项目

### 问题4：GitHub push 超时
- **现象**：`Failed to connect to github.com:443`
- **解决**：重试后网络恢复，push 成功

---

## 八、面试自问自答

**Q1：线程池核心线程数和最大线程数怎么定？**

CPU 密集型任务（大量计算）：核心线程数 = CPU 核心数 + 1
IO 密集型任务（网络、磁盘）：核心线程数 = CPU 核心数 × 2
混合任务：根据压测结果调整，一般先设 CPU 核心数 × (1 + 等待时间/计算时间)

**Q2：为什么不推荐用 Executors 的快捷方法创建线程池？**

`Executors.newFixedThreadPool()` 和 `newSingleThreadExecutor()` 使用的是无界队列 `LinkedBlockingQueue`，队列可以无限增长，会导致 OOM。
`Executors.newCachedThreadPool()` 允许创建无限个线程，也会导致 OOM。
阿里巴巴开发规范推荐：手动创建 ThreadPoolExecutor，明确指定队列大小和最大线程数。

**Q3：HashMap 和 ConcurrentHashMap 的区别？**

HashMap 非线程安全；ConcurrentHashMap 线程安全。
JDK 7：ConcurrentHashMap 使用分段锁（Segment），每个段独立加锁。
JDK 8：使用 CAS + synchronized（锁单个桶），粒度更细，并发更高。

---

## 九、明日计划

### 线程池收尾
1. execute() vs submit() 深度对比 + 代码演示
2. 线程池参数设定公式（CPU密集型 vs IO密集型）
3. Future 和 Callable 实战（获取异步结果、异常处理、超时控制）

### CompletableFuture 异步编程
1. supplyAsync() / thenApply() / thenAccept() / thenCompose()
2. 串行、并行、异常处理
3. 实战：同时查用户、查订单、查库存，汇总结果

### Day03 —— 简知社区项目启动
1. Spring Initializr 创建 Spring Boot 3.x 项目
2. 配置 pom.xml（Spring Boot 3.2 + MySQL + Lombok）
3. 设计数据库表：用户表、文章表、评论表
4. 第一个接口：用户注册（Controller + Service + Mapper）
