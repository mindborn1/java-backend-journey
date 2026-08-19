# Day11 - MySQL 索引原理

> 日期：2026-08-19  
> 阶段：筑基期 Day08-15  
> 主题：MySQL 索引原理 + EXPLAIN 实战

---

## 一、理论学习

### 1. 索引是什么？

索引是帮助 MySQL **高效获取数据**的**排好序的数据结构**。没有索引，MySQL 只能全表扫描（`type = ALL`），数据量越大越慢。

### 2. 为什么用 B+树，不用 B树 / Hash？

| 数据结构 | MySQL 为什么不用/用 |
|---------|------------------|
| **Hash** | 只能做等值查询（`=`），**不支持范围查询**（`>`、`<`、`BETWEEN`）、**不支持排序** |
| **B树** | 每个节点都存数据，导致节点体积大，一个页（16KB）存不了多少索引，树变高，IO 次数多 |
| **B+树** | **非叶子节点只存索引键值**，不存数据 → 一个页能存更多索引 → 树更矮胖 → IO 更少；**叶子节点用双向链表连接** → 天然支持范围查询和排序 |

> 💡 InnoDB 一页默认 16KB，B+树三层就能存几千万数据。

### 3. 聚簇索引 vs 非聚簇索引（二级索引）

- **聚簇索引**：叶子节点存的是**整行数据**。InnoDB 表必有且只有一个聚簇索引，就是**主键索引**。如果没有主键，会选第一个非空唯一索引；还没有就隐式生成一个 6 字节的 row_id。
- **非聚簇索引（二级索引）**：叶子节点存的是**主键值**。查到主键后，还要**回表**去聚簇索引查整行数据。

| 特性 | 聚簇索引 | 非聚簇索引（二级索引） |
|-----|---------|---------------------|
| 叶子节点存 | 整行数据 | 主键值 |
| 数量 | 只能有 1 个 | 可以有多个 |
| 查询方式 | 直接命中数据 | 先查主键，再回表 |
| 是否回表 | 不回表 | 需要回表（除非覆盖索引） |

### 4. 覆盖索引

查询的字段**全部在索引里**，不需要回表。EXPLAIN 的 Extra 列会出现 `Using index`（或 `Covering index lookup`）。

### 5. 最左前缀原则

联合索引 `(a, b, c)` 必须**从左到右按顺序使用**，中间不能断，不能跳过前面的列。

- ✅ `WHERE a=1 AND b=2`
- ✅ `WHERE a=1`
- ❌ `WHERE b=2`（跳过了 a）
- ⚠️ `WHERE b=2 AND a=1`（MySQL 优化器会调整顺序，可以用）

### 6. 索引下推（ICP, Index Condition Pushdown）

MySQL 5.6 引入。在**存储引擎层**就过滤不满足条件的记录，减少回表次数。

### 7. 索引失效的 10 种常见场景

1. `LIKE '%xx'`（左模糊，因为字符串排序从左到右匹配）
2. `OR` 条件中有一列没索引
3. 索引列做函数运算（`WHERE YEAR(create_time)=2024`）
4. 索引列隐式类型转换（字符串没加引号）
5. 索引列参与计算（`WHERE id + 1 = 2`）
6. `!=`、`<>`、`NOT IN`（不一定失效，但大概率不走索引）
7. `IS NULL` / `IS NOT NULL`（取决于数据分布）
8. 联合索引未遵循最左前缀
9. `ORDER BY` 字段不在索引中或顺序不对
10. 数据量太小，优化器认为全表扫描更快

---

## 二、EXPLAIN 实操记录

### 2.1 article 表索引添加

```sql
-- 给 user_id 加索引（查询某作者的所有文章）
ALTER TABLE article ADD INDEX idx_user_id (user_id);

-- 给 status 加索引（按状态筛选已发布/草稿等）
ALTER TABLE article ADD INDEX idx_status (status);

-- 给 create_time 加索引（按时间排序、时间范围查询）
ALTER TABLE article ADD INDEX idx_create_time (create_time);

-- 给 title 加索引（LIKE 测试用）
ALTER TABLE article ADD INDEX idx_title (title);
```

验证索引：
```sql
SHOW INDEX FROM article;
-- 结果：PRIMARY、idx_user_id、idx_status、idx_create_time、idx_title
```

### 2.2 comment 表联合索引添加

```sql
-- 添加联合索引 (article_id, parent_id)
ALTER TABLE comment ADD INDEX idx_article_parent (article_id, parent_id);
```

验证索引：
```sql
SHOW INDEX FROM comment;
-- 结果：PRIMARY、idx_article_parent
```

---

### 2.3 EXPLAIN 结果分析

#### 测试 1：单列索引等值查询

```sql
EXPLAIN SELECT * FROM article WHERE user_id = 1;
```

**结果：**
```
-> Index lookup on article using idx_user_id (user_id = 1) (cost=0.35 rows=1)
```

**分析：**
- `type = ref`（非唯一索引等值查询）
- `key = idx_user_id`（走了我们加的索引）
- 因为是 `SELECT *`，二级索引查完后需要**回表**去聚簇索引拿 content 等字段

---

#### 测试 2：单列索引范围查询

```sql
EXPLAIN SELECT * FROM article WHERE create_time > '2026-01-01';
```

**结果：**
```
-> Index range scan on article using idx_create_time 
   over ('2026-01-01 00:00:00' < create_time), 
   with index condition: ... (cost=... rows=...)
```

**分析：**
- `type = range`（索引范围扫描）
- `key = idx_create_time`
- 还触发了 **ICP（索引下推）**，在存储引擎层就过滤数据

---

#### 测试 3：联合索引 - 完全命中

```sql
EXPLAIN SELECT * FROM comment WHERE article_id = 1 AND parent_id = 0;
```

**结果：**
```
-> Index lookup on comment using idx_article_parent 
   (article_id = 1, parent_id = 0) (cost=0.7 rows=2)
```

**分析：**
- 联合索引两列全部命中
- `type = ref`，`key = idx_article_parent`

---

#### 测试 4：联合索引 - 只命中最左列（✅ 生效）

```sql
EXPLAIN SELECT * FROM comment WHERE article_id = 1;
```

**结果：**
```
-> Index lookup on comment using idx_article_parent (article_id = 1) (cost=0.7 rows=2)
```

**分析：**
- 只用到 `article_id`，但它是联合索引的最左列，所以索引**仍然生效**
- 这就是**最左前缀原则**的正面例子

---

#### 测试 5：联合索引 - 跳过最左列（❌ 失效）

```sql
EXPLAIN SELECT * FROM comment WHERE parent_id = 0;
```

**结果：**
```
-> Filter: (`comment`.parent_id = 0) (cost=0.45 rows=1)
   -> Table scan on comment (cost=0.45 rows=2)
```

**分析：**
- `type = ALL`（全表扫描）
- `key = NULL`（没有走任何索引）
- **原因**：跳过了联合索引的最左列 `article_id`，MySQL 无法从 B+树定位，只能全表扫描
- ⚠️ 这是面试**高频考点**

---

#### 测试 6：LIKE 左模糊（❌ 索引失效）

```sql
EXPLAIN SELECT * FROM article WHERE title LIKE '%Spring%';
```

**结果：**
```
-> Filter: (article.title like '%Spring%') (cost=0.55 rows=1)
   -> Table scan on article (cost=0.55 rows=3)
```

**分析：**
- `type = ALL`（全表扫描）
- `key = NULL`
- **原因**：B+树索引是按字符串**从左到右排序**的。`%Spring` 不知道第一个字符是什么，无法从根节点定位，只能遍历所有叶子节点 = 全表扫描

---

#### 测试 7：LIKE 右模糊（✅ 走索引）

```sql
EXPLAIN SELECT * FROM article WHERE title LIKE 'Spring%';
```

**结果：**
```
-> Index range scan on article using idx_title 
   over ('Spring' <= title <= 'Spring?????????????????????????...')
```

**分析：**
- `type = range`（索引范围扫描）
- `key = idx_title`
- **原因**：知道以 `'Spring'` 开头，可以从 B+树定位到 `'Spring'` 的位置，然后向后遍历

---

#### 测试 8：二级索引 + SELECT *（需要回表）

```sql
EXPLAIN SELECT * FROM article WHERE user_id = 1;
```

**结果：**
```
-> Index lookup on article using idx_user_id (user_id = 1) (cost=0.35 rows=1)
```

**分析：**
- 走了 `idx_user_id` 索引
- 但 `SELECT *` 需要 content、title 等字段，而 `idx_user_id` 的叶子节点只存 `user_id + 主键 id`
- 所以需要**回表**：先查二级索引拿到主键 id，再去聚簇索引查整行数据

---

#### 测试 9：覆盖索引（✅ 不回表）

```sql
EXPLAIN SELECT user_id, id FROM article WHERE user_id = 1;
```

**结果：**
```
-> Covering index lookup on article using idx_user_id (user_id = 1) (cost=0.35 rows=1)
```

**分析：**
- 明确显示 **`Covering index`**（覆盖索引）
- 查询的字段 `user_id` 和 `id` 都在 `idx_user_id` 索引里
- **不需要回表**，直接从索引叶子节点返回数据，效率更高

---

## 三、EXPLAIN 关键字段速查表

| 字段 | 含义 | 重点关注 |
|-----|------|---------|
| `type` | 访问类型 | `system > const > eq_ref > ref > range > index > ALL`，尽量让 `type >= ref` |
| `key` | 实际使用的索引 | 是否为 `NULL`（`NULL` = 没走索引） |
| `rows` | 预估扫描行数 | 越小越好 |
| `Extra` | 额外信息 | `Using index`（覆盖索引）、`Using where`、`Using index condition`（ICP）、`Using filesort`（需要额外排序，不好） |

---

## 四、今日收获

1. ✅ 会看 EXPLAIN 分析查询计划（重点关注 `type`、`key`、`rows`、`Extra`）
2. ✅ 理解了最左前缀原则，亲手验证了"跳过最左列 = 索引失效"
3. ✅ 理解了覆盖索引：`SELECT *` 会回表，只查索引字段则不回表
4. ✅ LIKE 左模糊是面试重灾区，实际验证了确实会全表扫描；右模糊可以走索引
5. ✅ 学会了给表添加索引：`ALTER TABLE ... ADD INDEX ...`

---

## 五、疑问 / 待复习

（Day14 筑基期总结时重点回顾）

- [ ] 索引下推（ICP）的底层实现细节
- [ ] 索引选择性（Cardinality）对索引选择的影响
- [ ] 什么时候应该建联合索引而不是多个单列索引？

---

## 附录：今日执行的完整 SQL

```sql
-- ==================== article 表索引 ====================
ALTER TABLE article ADD INDEX idx_user_id (user_id);
ALTER TABLE article ADD INDEX idx_status (status);
ALTER TABLE article ADD INDEX idx_create_time (create_time);
ALTER TABLE article ADD INDEX idx_title (title);

-- ==================== comment 表联合索引 ====================
ALTER TABLE comment ADD INDEX idx_article_parent (article_id, parent_id);

-- ==================== EXPLAIN 测试 ====================
EXPLAIN SELECT * FROM article WHERE user_id = 1;
EXPLAIN SELECT * FROM article WHERE status = 1;
EXPLAIN SELECT * FROM article WHERE create_time > '2026-01-01';

EXPLAIN SELECT * FROM comment WHERE article_id = 1 AND parent_id = 0;
EXPLAIN SELECT * FROM comment WHERE article_id = 1;
EXPLAIN SELECT * FROM comment WHERE parent_id = 0;

EXPLAIN SELECT * FROM article WHERE title LIKE '%Spring%';
EXPLAIN SELECT * FROM article WHERE title LIKE 'Spring%';

EXPLAIN SELECT * FROM article WHERE user_id = 1;
EXPLAIN SELECT user_id, id FROM article WHERE user_id = 1;
```
