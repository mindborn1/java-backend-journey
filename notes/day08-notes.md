# Day08：Java 21 新特性（上）+ 补刷基础算法题

> 记录时间：2026-08-15  
> 所属阶段：筑基期

---

## 一、今日学习主题

- Java 21 Record 类
- Pattern Matching for switch
- 补刷 Day01-07 缺失的 LeetCode 基础题

---

## 二、Java 21 新特性

### 2.1 Record 类

**是什么：** 不可变数据载体类，专门存数据，省去样板代码。

**语法：**
```java
public record UserRecord(Long id, String name, Integer age) {}
```

**自动生成：**
- `private final` 字段
- 全参构造器
- `equals()`、`hashCode()`、`toString()`
- getter（注意是 `name()` 不是 `getName()`）

**紧凑构造器（校验）：**
```java
public UserRecord {
    if (age < 0 || age > 150) {
        throw new IllegalArgumentException("年龄不合法");
    }
}
```

**什么时候用：**
- ✅ 纯 DTO/VO、方法返回多个值、配置类
- ❌ 需要 setter 的 Entity、需要继承的类、需要无参构造器的类

**Record vs Lombok @Data：**

| 特性 | Record | @Data |
|------|--------|-------|
| 可变性 | 不可变（final） | 可变 |
| 继承 | 不能继承 | 可以 |
| 序列化 | Jackson 2.12+ 支持 | 通用 |

---

### 2.2 Pattern Matching for switch

**解决什么问题：** 替代 `if-else instanceof` 链。

**传统写法：**
```java
if (obj instanceof Integer i) { ... }
else if (obj instanceof String s) { ... }
```

**Pattern Matching 写法：**
```java
return switch (obj) {
    case Integer i -> "整数: " + i;
    case String s  -> "字符串: " + s;
    case null      -> "空值";
    default        -> "未知";
};
```

**关键点：**
- `case` 后直接跟 `类型 变量名`
- 变量自动强转，可直接使用
- 支持 `null` 分支
- 支持 `when` 守卫条件：`case int s when s >= 90 -> "优秀"`

---

## 三、编码实战记录

### 练习 1：Record（Point + UserRecord）
- 路径：`java-backend-code/src/main/java/com/mindborn/day08/`
- 文件：`Point.java`、`UserRecord.java`、`RecordTest.java`
- 验证了：自定义方法、静态方法、紧凑构造器校验、equals

### 练习 2：Pattern Matching for switch
- 文件：`PatternMatchingSwitch.java`
- 验证了：类型匹配、null 处理、when 守卫条件、自定义 Record 匹配

---

## 四、LeetCode 刷题记录

今天共刷 **9 道题**（Day08 本身 2 道 + 补 Day01-07 基础题 7 道）。

### 4.1 第 1 题：Two Sum（Day08）
- **思路：** HashMap 一次遍历，key 存数值，value 存下标
- **核心：** `complement = target - nums[i]`，检查 map 里有没有
- **结论：** 不适合用 Stream，传统 HashMap 最优

### 4.2 第 15 题：三数之和（Day08）
- **思路：** 排序 + 固定一个数 + 双指针
- **核心：** 先排序，固定 `i`，`left` 和 `right` 向中间夹逼
- **难点：** 去重（固定数去重、left 去重、right 去重）
- **剪枝：** `nums[i] > 0` 直接 break

### 4.3 第 88 题：合并两个有序数组（补 Day01）
- **思路：** 从后往前填，三指针（p1, p2, p）
- **为什么从后往前：** 避免覆盖 nums1 前面的有效数据
- **为什么不能新建数组：** 原地修改要求直接改 nums1 的元素，不能换引用

### 4.4 第 136 题：只出现一次的数字（补 Day02）
- **思路：** 异或运算 `^`
- **性质：** `a ^ a = 0`，`a ^ 0 = a`，交换律结合律
- **代码：** 全部异或一遍，成对抵消，剩下目标数

### 4.5 第 141 题：环形链表（补 Day03）
- **思路：** 快慢指针（龟兔赛跑）
- **核心：** 快指针每次走 2 步，慢指针走 1 步，有环必相遇
- **终止条件：** `fast == null || fast.next == null` 说明无环

### 4.6 第 206 题：反转链表（补 Day04）
- **思路：** 三指针迭代（prev, curr, next）
- **核心：** 保存 next → curr.next 指向 prev → prev 和 curr 后移
- **返回：** 最后 prev 指向新头节点

### 4.7 第 283 题：移动零（补 Day05）
- **思路：** 双指针（slow, fast）
- **核心：** fast 找非零，放到 slow 位置，最后 slow 后面全填 0

### 4.8 第 20 题：有效括号（补 Day06）
- **思路：** 栈
- **核心：** 左括号压栈，右括号匹配栈顶（先 pop 再检查）
- **关键：** 先无脑弹出栈顶，再检查类型是否匹配；最后栈必须为空

### 4.9 第 70 题：爬楼梯（补 Day07）
- **思路：** 动态规划，斐波那契数列
- **状态转移：** `f(n) = f(n-1) + f(n-2)`
- **空间优化：** 只用两个变量滚动计算，O(1) 空间

---

## 五、今日感悟

- Record 和 Pattern Matching 让 Java 代码更简洁，但 Record 不能替代所有 DTO，Entity 还是用传统类
- 从后往前填数组是经典技巧（合并有序数组），要记住
- 异或运算在"找唯一"的场景非常巧妙
- 快慢指针是解决链表环问题的标准套路
- 栈处理括号匹配时，**先弹出再检查**的顺序很重要

---

## 六、明日预告

**Day09：Stream API 深入**
- Stream 完整流程（创建 → 中间操作 → 终端操作）
- Collectors 工具类
- 5 个 Stream 练习 + 2 道 LeetCode

---

*计划版本：v2.0*  
*记录时间：2026-08-15*
