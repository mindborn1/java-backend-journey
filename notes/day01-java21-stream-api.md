# Day 1 学习笔记

> 日期：2026-08-06  
> 主题：Java 21 新特性 + Stream API 实战  
> 状态：✅ 已完成

---

## 一、今日目标回顾

- [x] Java 21 新特性（Record、Switch、Text Block）
- [x] Stream API 核心操作（过滤、映射、排序、分组、聚合）
- [x] 第一个 Maven 项目搭建并运行
- [x] 代码提交到 GitHub

---

## 二、Java 21 新特性

### 1. Record（数据类）

**是什么**：Java 14+ 引入的不可变数据载体，一行代码替代传统的 POJO。

**核心特点**：
- 自动生成 getter、equals()、hashCode()、toString()
- 不可变（创建后不能修改字段）
- 不能继承其他类（隐式继承 java.lang.Record）

**代码示例**：
```java
record User(int id, String name, int age, String city, double salary) {}

// 使用
User user = new User(1, "张三", 25, "北京", 15000);
System.out.println(user.name());  // 张三
```

**注意**：
- 一个 .java 文件只能有 1 个 public 类
- record 前面不能加 public（如果文件里已有其他 public 类）

---

### 2. Switch 表达式（Java 14+）

**是什么**：支持箭头语法 -> 直接返回值的 switch，告别 break 穿透 bug。

**传统写法 vs 现代写法**：

```java
// ❌ 传统写法：又臭又长，容易漏 break
String grade;
switch (score) {
    case 90: case 91: ... case 100:
        grade = "A";
        break;
    case 80:
        grade = "B";
        break;
    default:
        grade = "F";
}

// ✅ 现代写法：简洁、无 break、直接返回值
String grade = switch (score / 10) {
    case 10, 9 -> "A";
    case 8 -> "B";
    case 7 -> "C";
    default -> "F";
};
```

**关键语法**：
- case A, B -> value：多 case 合并
- default -> { yield 0; }：代码块用 yield 返回值

**实际应用场景**：
- 订单状态流转（CREATED → PAID → SHIPPED）
- 权限角色判断（ADMIN / USER / VIP）
- 错误码映射（1001 → "参数错误"）

---

### 3. Text Block 文本块（Java 15+）

**是什么**：用三个双引号包裹多行字符串，保留原始格式。

**解决的问题**：
传统字符串写 JSON/SQL 需要大量 \n 和 \" 转义，可读性极差。

**代码示例**：
```java
// ✅ 文本块：所见即所得
String json = """
    {
        "name": "张三",
        "age": 25,
        "city": "北京"
    }
    """;

// ❌ 传统写法：又丑又难维护
String oldJson = "{
" +
    "    "name": "张三",
" +
    "    "age": 25
" +
    "}";
```

**实际应用场景**：
- 原生 SQL 查询语句
- 调用第三方 API 的 JSON 请求体
- HTML/邮件模板

---

## 三、Stream API 核心操作（面试重点！）

Stream API 是 Java 8 引入的函数式编程特性，用于对集合进行链式操作。

### 核心方法清单

| 操作 | 方法 | 作用 | 示例 |
|------|------|------|------|
| **过滤** | filter(Predicate) | 按条件筛选 | .filter(u -> u.age() > 25) |
| **映射** | map(Function) | 把 A 转成 B | .map(User::name) |
| **排序** | sorted(Comparator) | 按规则排序 | .sorted(Comparator.comparingDouble(User::salary).reversed()) |
| **截取** | limit(n) | 取前 N 个 | .limit(3) |
| **跳过** | skip(n) | 跳过前 N 个 | .skip(2) |
| **分组** | collect(groupingBy(...)) | 按条件分组 | .collect(groupingBy(User::city)) |
| **分区** | collect(partitioningBy(...)) | 按 true/false 二分 | .collect(partitioningBy(u -> u.age() > 30)) |
| **聚合** | collect(averagingDouble(...)) | 求平均值 | .collect(averagingDouble(User::salary)) |
| **收集** | collect(toList()) | 转成 List | .collect(Collectors.toList()) |
| **拼接** | collect(joining(", ")) | 拼成字符串 | .collect(joining(", ")) |

### 链式调用流程

```
数据源(List) → stream() → 中间操作(filter/map/sorted) → 终端操作(collect)
```

**面试金句**：
> "Stream API 支持链式调用，可以对集合进行过滤、映射、排序、分组等操作，代码简洁且支持并行流 parallelStream() 提升性能。"

---

## 四、今天踩的坑 & 解决方案

| 坑 | 原因 | 解决 |
|----|------|------|
| String 写成 string | Java 大小写敏感 | 类名必须大写开头：String ✅ |
| new user() 报错 | 类名大小写不一致 | new User() ✅ |
| public record User 报错 | 一个文件只能有 1 个 public 类 | record User 去掉 public ✅ |
| Git push 失败 | 不在仓库目录下 | cd /d/projects/java-backend-journey |
| GitHub 看不到代码 | commit 了但没 push | git push origin main |

---

## 五、明日预告

**Day 2：集合源码 + 多线程基础**

- HashMap 源码（数组+链表+红黑树、扩容机制）
- ArrayList vs LinkedList
- 线程池 ThreadPoolExecutor
- CompletableFuture 异步编程

---

