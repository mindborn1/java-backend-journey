# Day09 学习总结

> 日期：2026-08-17  
> 主题：手写 HashMap + 泛型通配符 PECS + 类型擦除  
> 阶段：筑基期（Day08-15）

---

## 一、手写简易 HashMap

### 1.1 核心数据结构

```
数组 + 链表（拉链法解决哈希冲突）

        table[0] → Node → Node → null
        table[1] → null
        table[2] → Node → null
        table[3] → Node → Node → Node → null
        ...
```

### 1.2 关键设计点

| 设计点 | 说明 |
|--------|------|
| **懒加载** | 首次 `put` 时才初始化数组，节省内存 |
| **扰动函数** | `hash = h ^ (h >>> 16)`，让高位参与运算，减少冲突 |
| **位运算取模** | `index = hash & (length - 1)`，等价于 `hash % length`，但更快 |
| **容量为 2ⁿ** | 保证 `length - 1` 二进制全为 1，位运算取模才成立 |
| **头插法** | JDK7 风格，简单演示；JDK8 改为尾插法（防止并发死循环） |
| **扩容阈值** | `threshold = capacity * loadFactor`（默认 0.75） |

### 1.3 扩容机制（resize）

```
旧容量 = 16，新容量 = 32
旧下标 = hash & 15（二进制 1111）
新下标 = hash & 31（二进制 11111）

关键发现：
- 如果 hash 的第 5 位是 0 → 新下标 = 旧下标
- 如果 hash 的第 5 位是 1 → 新下标 = 旧下标 + 16

这就是 JDK8 扩容时不用重新计算 hash 的优化原理！
```

### 1.4 代码结构

```java
public class MyHashMap<K, V> {
    // 常量
    static final int DEFAULT_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // 成员
    private Node<K, V>[] table;   // 数组
    private int size;             // 元素个数
    private int threshold;        // 扩容阈值

    // 节点
    static class Node<K, V> {
        final int hash;           // 缓存 hash，避免重复计算
        final K key;              // 键不可变
        V value;
        Node<K, V> next;
    }

    // 核心方法：put、get、resize
}
```

### 1.5 面试要点

- **为什么用 2 的幂次方？** → 位运算取模快，且扩容时只需判断 hash 新增的那一位
- **JDK7 头插法 vs JDK8 尾插法？** → 头插法在并发下会形成环导致死循环，JDK8 改为尾插法
- **JDK8 什么时候转红黑树？** → 链表长度 ≥ 8 且数组长度 ≥ 64
- **扰动函数的作用？** → 让高位也参与低位运算，减少低位相同导致的冲突

---

## 二、泛型通配符与 PECS 原则

### 2.1 类层次

```
Object
  └── Animal
        └── Dog
              └── Husky
```

### 2.2 PECS = Producer-Extends, Consumer-Super

| 通配符 | 角色 | 能读 | 能写 | 适用场景 |
|--------|------|------|------|----------|
| `? extends T` | **Producer**（生产者） | ✅ 读出来是 `T` | ❌ 不能写（除了 null） | 遍历、获取数据 |
| `? super T` | **Consumer**（消费者） | ⚠️ 读出来是 `Object` | ✅ 能写 `T` 及子类 | 添加、收集数据 |

### 2.3 记忆口诀

> **只读不写 → extends（上界）**  
> **只写不读 → super（下界）**  
> **又读又写 → 不用通配符，直接用 `List<T>`**

### 2.4 代码示例

```java
// extends：安全读取，不能写入
void printAnimals(List<? extends Animal> animals) {
    for (Animal a : animals) {
        a.eat();  // ✅ 安全
    }
    // animals.add(new Dog());  // ❌ 编译错误
}

// super：安全写入，读取只能当 Object
void addDogs(List<? super Dog> dogs) {
    dogs.add(new Dog());      // ✅
    dogs.add(new Husky());    // ✅
    // dogs.add(new Animal()); // ❌ 编译错误

    Object obj = dogs.get(0); // ✅ 只能当 Object
}
```

### 2.5 经典应用：Collections.copy

```java
public static <T> void copy(List<? super T> dest, List<? extends T> src) {
    // dest 是 Consumer（接收数据）→ 用 super
    // src  是 Producer（提供数据）→ 用 extends
}
```

---

## 三、类型擦除（Type Erasure）

### 3.1 核心结论

> Java 泛型是**编译期的语法糖**，运行时全部擦除为 `Object`（或第一个边界），类型安全由编译器通过**插入强制转换**来保证。

### 3.2 擦除后的变化

| 编译前 | 编译后 |
|--------|--------|
| `List<String>` | `List`（raw type） |
| `T get()` | `Object get()` |
| `void set(T data)` | `void set(Object data)` |
| `String s = list.get(0)` | `String s = (String) list.get(0)` ← 编译器自动插强转 |

### 3.3 因为擦除，所以不能做的事

```java
static class Box<T> {
    // ❌ T t = new T();          // 运行时不知道 T 是什么
    // ❌ T[] arr = new T[10];    // 同上
    // ❌ if (obj instanceof T)    // 运行时无 T
    // ❌ if (obj instanceof List<T>) // 同上

    // ✅ 正确做法：传 Class<T>
    T create(Class<T> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    // ✅ 正确做法：Object[] 强转
    @SuppressWarnings("unchecked")
    T[] createArray(int size) {
        return (T[]) new Object[size];
    }
}
```

### 3.4 反射可以"看到"泛型信息

```java
// 父类保留泛型签名
abstract class Dao<T> {}
class UserDao extends Dao<User> {}

// 通过反射获取 T 的具体类型
Type type = UserDao.class.getGenericSuperclass();  // ParameterizedType
Type[] args = ((ParameterizedType) type).getActualTypeArguments();
// args[0] = User.class
```

**这就是 MyBatis-Plus `BaseMapper<T>`、Spring 泛型依赖注入的底层原理！**

### 3.5 桥方法（Bridge Method）

```java
class Node<T> {
    public void setData(T data) {}
}

class MyNode extends Node<Integer> {
    @Override
    public void setData(Integer data) {}  // 编译后...
}

// 编译器自动生成桥方法：
// public void setData(Object data) { setData((Integer) data); }
```

---

## 四、今日收获

1. **手写 HashMap** 让我真正理解了 `hash()` 扰动、`(n-1)&hash` 位运算、懒加载、扩容 rehash 的底层逻辑
2. **PECS 原则** 彻底搞懂了 `extends` 和 `super` 的边界，不再死记硬背
3. **类型擦除** 明白了 Java 泛型的本质——编译期语法糖，运行时擦除为 Object，框架通过反射 `ParameterizedType` 获取泛型参数

---

## 五、明日计划（Day10）

**MyBatis-Plus 进阶**
- `QueryWrapper` / `LambdaQueryWrapper` 条件构造器
- 分页查询（`Page` + 分页插件）
- 逻辑删除（`@TableLogic`）
- 自动填充（`@TableField` + `MetaObjectHandler`）

---

> 代码文件：`java-backend-code/src/main/java/com/mindborn/day09/`
