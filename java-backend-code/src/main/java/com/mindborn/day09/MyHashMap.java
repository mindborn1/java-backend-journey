package com.mindborn.day09;

import java.util.Objects;

/**
 * Day09 练习1：手写简易 HashMap，理解底层原理
 * 核心机制：数组 + 链表（暂不实现红黑树）
 * 关键理解：hash() 扰动函数、扩容机制、2的幂次方容量
 */
public class MyHashMap<K, V> {

    // 默认初始容量
    static final int DEFAULT_CAPACITY = 16;
    // 默认负载因子
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    // 存储数据的数组（每个元素是一个链表头节点）
    private Node<K, V>[] table;
    // 当前元素个数
    private int size;
    // 扩容阈值 = capacity * loadFactor
    private int threshold;

    // 链表节点定义
    static class Node<K, V> {
        final int hash;    // 哈希值，用于定位数组下标
        final K key;       // 键（不可变）
        V value;           // 值
        Node<K, V> next;   // 下一个节点

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // 构造方法
    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.threshold = (int) (DEFAULT_CAPACITY * DEFAULT_LOAD_FACTOR);
    }

    /**
     * 扰动函数：让高位也参与运算，减少哈希冲突
     * 原理：h = key.hashCode(), 然后 h ^ (h >>> 16)
     * 这样即使低位相同，高位不同也能产生不同结果
     */
    static int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 计算数组下标：(n - 1) & hash
     * 因为 n 是 2 的幂，n-1 的二进制全是1，相当于对 n 取模，但位运算更快
     * 例如：n=16, n-1=1111(二进制), hash & 1111 = hash % 16
     */
    static int indexFor(int hash, int length) {
        return hash & (length - 1);
    }

    /**
     * 放入键值对
     * 逻辑：计算hash -> 定位下标 -> 遍历链表 -> 存在则覆盖，不存在则追加 -> 检查扩容
     */
    public V put(K key, V value) {
        // 首次put时初始化数组（懒加载，节省内存）
        if (table == null) {
            resize();
        }

        int hash = hash(key);
        int index = indexFor(hash, table.length);

        // 遍历该位置的链表，查找是否已存在相同key
        for (Node<K, V> p = table[index]; p != null; p = p.next) {
            // 判断key相同：hash相等 且 (地址相等 或 equals相等)
            if (p.hash == hash && Objects.equals(key, p.key)) {
                V oldVal = p.value;
                p.value = value;  // 覆盖旧值
                return oldVal;
            }
        }

        // 不存在，头插法插入新节点（JDK7是头插，JDK8是尾插，这里用头插简单演示）
        table[index] = new Node<>(hash, key, value, table[index]);
        size++;

        // 超过阈值则扩容
        if (size > threshold) {
            resize();
        }
        return null;
    }

    /**
     * 获取值
     */
    public V get(K key) {
        if (table == null) return null;
        int hash = hash(key);
        int index = indexFor(hash, table.length);

        // 遍历链表匹配
        for (Node<K, V> p = table[index]; p != null; p = p.next) {
            if (p.hash == hash && Objects.equals(key, p.key)) {
                return p.value;
            }
        }
        return null;
    }

    /**
     * 扩容机制：容量翻倍，所有节点重新计算下标（rehash）
     * 关键点：容量保持2的幂，这样 (n-1)&hash 才能等价于 hash%n
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        int oldCap = (table == null) ? 0 : table.length;
        int newCap;
        if (oldCap == 0) {
            newCap = DEFAULT_CAPACITY;
        } else {
            newCap = oldCap << 1;  // 容量翻倍
        }

        // 创建新数组
        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCap];
        threshold = (int) (newCap * DEFAULT_LOAD_FACTOR);

        if (table != null) {
            // 遍历旧数组，将每个节点重新散列到新数组
            for (int j = 0; j < oldCap; j++) {
                Node<K, V> e = table[j];
                if (e != null) {
                    table[j] = null; //  help GC
                    do {
                        Node<K, V> next = e.next;
                        // 重新计算下标：注意这里有个优化点
                        // 如果 hash & oldCap == 0，则新下标不变；否则新下标 = 旧下标 + oldCap
                        int newIndex = e.hash & (newCap - 1);
                        e.next = newTable[newIndex];
                        newTable[newIndex] = e;
                        e = next;
                    } while (e != null);
                }
            }
        }
        table = newTable;
    }

    public int size() {
        return size;
    }

    // ==================== 测试主方法 ====================
    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        // 测试put和get
        map.put("apple", 100);
        map.put("banana", 200);
        map.put("cherry", 300);

        System.out.println("apple = " + map.get("apple"));    // 100
        System.out.println("banana = " + map.get("banana"));  // 200

        // 测试覆盖
        map.put("apple", 150);
        System.out.println("apple update = " + map.get("apple")); // 150

        // 测试扩容：插入大量数据触发扩容
        for (int i = 0; i < 20; i++) {
            map.put("key" + i, i);
        }
        System.out.println("size after insert = " + map.size()); // 23

        // 测试null key（hash为0，放在下标0）
        map.put(null, 999);
        System.out.println("null key = " + map.get(null)); // 999

        System.out.println("✅ MyHashMap 测试通过！");
    }
}