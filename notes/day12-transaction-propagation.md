# Day12 - Spring 事务传播行为（Transaction Propagation）

> **日期**：2026-08-22  
> **阶段**：筑基期 Day08~15  
> **代码位置**：`java-backend-code/src/main/java/com/mindborn/day12/`  
> **数据库**：`day12_practice`

---

## 一、今日目标

理解并动手验证 Spring 事务的三种核心传播行为：

1. `REQUIRED`（默认）—— 加入当前事务
2. `REQUIRES_NEW` —— 新建独立事务
3. `NESTED` —— 在当前事务内创建 savepoint

通过 4 个真实场景（下单+扣库存+记日志），直观感受它们的区别。

---

## 二、核心概念

### 2.1 什么是事务传播行为？

当一个**已存在事务**的方法 A，去调用另一个**也加了 @Transactional** 的方法 B 时，B 应该怎么处理事务？

这就是"传播行为"要回答的问题。

### 2.2 三种传播行为对比

| 传播行为 | 含义 | 底层实现 | 使用场景 |
|---------|------|---------|---------|
| `REQUIRED` | 有事务就加入，没有就新建一个 | 同一个 Connection，同一个事务 | 绝大多数业务方法 |
| `REQUIRES_NEW` | 挂起当前事务，新建一个**完全独立**的事务 | 两个 Connection，两个独立事务 | 审计日志、监控打点——不管业务成败都必须记录 |
| `NESTED` | 在当前事务内打一个**存档点 savepoint** | 同一个 Connection，savepoint 机制 | 部分回滚，比如扣库存失败但订单要保留 |

### 2.3 关键区别

```
REQUIRED     → 同生共死（一个事务，一起提交/回滚）
REQUIRES_NEW → 各自独立（两个事务，互不干扰）
NESTED       → 存档读档（一个事务，内部失败只回滚到 savepoint）
```

---

## 三、实验设计与结果

### 3.1 数据准备

```sql
-- 商品1：库存充足（100个）
INSERT INTO t_stock (product_id, count) VALUES (1, 100);

-- 商品2：库存不足（0个）
INSERT INTO t_stock (product_id, count) VALUES (2, 0);
```

### 3.2 四场景实验结果

| 场景 | 方法 | 传播链 | 预期 | 实际结果 |
|------|------|--------|------|---------|
| **场景1** | `placeOrderSuccess` | `REQUIRED` → `REQUIRED` | 订单+1，库存 100→99 | ✅ 正确 |
| **场景2** | `placeOrderFail` | `REQUIRED` → `REQUIRED`（抛异常） | 订单回滚，库存不变 | ✅ 正确 |
| **场景3** | `placeOrderWithLog` | `REQUIRED` → `REQUIRES_NEW` → `REQUIRED`（抛异常） | 订单回滚，**日志保留**，库存不变 | ✅ 正确 |
| **场景4** | `placeOrderWithNested` | `REQUIRED` → `NESTED`（抛异常，被 catch） | **订单保留**，库存回滚到 savepoint | ✅ 正确 |

### 3.3 场景3 的 REQUIRES_NEW 为什么能保留日志？

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void record(String operation) {
    // 这段代码执行时：
    // 1. 挂起外部事务（placeOrderWithLog 的事务）
    // 2. 新建一个独立事务
    // 3. 立即提交（insert 落盘）
    // 4. 恢复外部事务
    // 
    // 所以即使后面 stockService.deduct 抛异常导致外部事务回滚，
    // 这条日志已经写进数据库了，不受影响！
}
```

### 3.4 场景4 的 NESTED 为什么能部分回滚？

```java
@Transactional(propagation = Propagation.NESTED)
public void deductNested(Long productId, int count) {
    // 这段代码执行时：
    // 1. 在当前事务内创建一个 savepoint
    // 2. 执行扣库存逻辑
    // 3. 如果失败 → 回滚到 savepoint（库存不变）
    // 4. 外部事务继续执行（订单保留）
}
```

就像玩游戏：**存档 → 打BOSS失败 → 读档 → 继续游戏**。

---

## 四、关键代码

### 4.1 OrderServiceImpl —— 4 个场景入口

```java
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private StockService stockService;
    @Autowired private OperationLogService operationLogService;

    // ========== 场景1：REQUIRED 正常下单 ==========
    @Override
    @Transactional
    public void placeOrderSuccess(Long userId, Long productId, int count) {
        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setAmount(new BigDecimal("99.99"));
        order.setStatus("CREATED");
        orderMapper.insert(order);

        // 扣库存（REQUIRED，加入当前事务）
        stockService.deduct(productId, count);
    }

    // ========== 场景2：REQUIRED 库存不足，全部回滚 ==========
    @Override
    @Transactional
    public void placeOrderFail(Long userId, Long productId, int count) {
        // 创建订单（未提交）
        Order order = new Order();
        ...
        orderMapper.insert(order);

        // 扣库存会抛"库存不足"异常 → 当前事务回滚
        stockService.deduct(productId, count);
    }

    // ========== 场景3：REQUIRES_NEW 日志保留 ==========
    @Override
    @Transactional
    public void placeOrderWithLog(Long userId, Long productId, int count) {
        // 创建订单
        Order order = new Order();
        ...
        orderMapper.insert(order);

        // 先记录日志（REQUIRES_NEW，独立事务，立即提交！）
        operationLogService.record("【场景3】用户" + userId + "尝试下单，商品=" + productId);

        // 扣库存（会抛异常，当前事务回滚，但日志已保留）
        stockService.deduct(productId, count);
    }

    // ========== 场景4：NESTED 部分回滚 ==========
    @Override
    @Transactional
    public void placeOrderWithNested(Long userId, Long productId, int count) {
        // 创建订单
        Order order = new Order();
        ...
        orderMapper.insert(order);

        try {
            // NESTED：在当前事务内创建 savepoint
            stockService.deductNested(productId, count);
        } catch (Exception e) {
            // 捕获异常，只回滚 savepoint，订单仍然保留！
            System.out.println("库存扣减失败，但订单保留！异常：" + e.getMessage());
        }
    }
}
```

### 4.2 StockServiceImpl —— 库存扣减

```java
@Service
public class StockServiceImpl implements StockService {

    @Autowired private StockMapper stockMapper;

    // REQUIRED（默认）：加入调用方的事务
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deduct(Long productId, int count) {
        Stock stock = stockMapper.selectOne(
            new QueryWrapper<Stock>().eq("product_id", productId)
        );

        if (stock.getCount() < count) {
            throw new RuntimeException("库存不足，当前库存=" + stock.getCount());
        }

        stock.setCount(stock.getCount() - count);
        stockMapper.updateById(stock);
    }

    // NESTED：在调用方事务内创建 savepoint
    @Override
    @Transactional(propagation = Propagation.NESTED)
    public void deductNested(Long productId, int count) {
        // 逻辑同上，但传播行为不同
        ...
    }
}
```

### 4.3 OperationLogServiceImpl —— 日志记录

```java
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired private OperationLogMapper logMapper;

    /**
     * REQUIRES_NEW：挂起当前事务，新建一个完全独立的事务
     *
     * 使用场景：审计日志、监控打点——不管业务成功失败，必须留下记录
     * 代价：多一次事务提交，性能略低，但在"必须记录"的场景下值得
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String operation) {
        OperationLog log = new OperationLog();
        log.setOperation(operation);
        logMapper.insert(log);
        System.out.println("日志记录成功，内容=" + operation);
    }
}
```

---

## 五、踩坑记录

### 5.1 ❌ 坑1：MyBatis-Plus 依赖版本不匹配

**错误**：
```
java.lang.NoClassDefFoundError: com/baomidou/mybatisplus/core/mapper/BaseMapper
```

**原因**：`mybatis-plus-boot-starter:3.5.6` 是为 Spring Boot 2.x 设计的，Spring Boot 3.x 改了自动配置机制，导致类加载失败。

**解决**：换成 Spring Boot 3 专用 starter：

```xml
<!-- ❌ 错误 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.6</version>
</dependency>

<!-- ✅ 正确 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.9</version>
</dependency>
```

### 5.2 ❌ 坑2：artifactId 拼写错误

`mybatis-plus-spring-boot3-starter` 中间有 `spring`，不要漏掉，也不要拼成 `spirng`。

### 5.3 ⚠️ 坑3：Order 实体类字段命名

```java
// ❌ 不规范（大驼峰开头）
private Long UserId;
private Long ProductId;

// ✅ 规范（小驼峰）
private Long userId;
private Long productId;
```

MyBatis-Plus 的自动驼峰转换（`userId` → `user_id`）对大驼峰开头的字段可能映射异常。

---

## 六、事务传播行为速查表

| 传播行为 | 当前有事务？ | 行为 |
|---------|------------|------|
| REQUIRED | 有 | 加入 |
| REQUIRED | 无 | 新建 |
| REQUIRES_NEW | 有 | 挂起当前，新建独立事务 |
| REQUIRES_NEW | 无 | 新建独立事务 |
| NESTED | 有 | 在当前事务内创建 savepoint |
| NESTED | 无 | 等价于 REQUIRED（新建事务） |

---

## 七、学习心得

1. **REQUIRED 是默认且最常用的**，理解"同生共死"就够了。
2. **REQUIRES_NEW 是审计场景的救命稻草**，日志、监控、消息发送——这些"必须成功"的操作，必须用独立事务。
3. **NESTED 是"部分回滚"的优雅方案**，比手动控制 savepoint 简洁得多，但要注意它只在当前有事务时才生效。
4. **事务不是越多越好**，REQUIRES_NEW 每次都会多一次事务提交，性能有损耗，只在必要场景使用。

---

## 八、明日预告

**Day13 - Spring AOP 面向切面编程**

- 什么是 AOP？切面、连接点、通知、切点
- 用 AOP 实现统一日志记录
- 用 AOP 实现接口耗时统计
- 自定义注解 + AOP 的组合拳

这是进入框架期前的最后一道基础关卡，和今天的事务一样，属于 Spring 核心机制。

---

> **Git 提交记录**：`Day12: Spring事务传播行为 - REQUIRED/REQUIRES_NEW/NESTED`
