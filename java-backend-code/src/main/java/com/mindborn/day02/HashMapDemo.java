package com.mindborn.day02;

import java.util.HashMap;

public class HashMapDemo {
    public static void main(String[] args) {
        // 1. 基本用法
        HashMap<String, Integer> map = new HashMap<>();
        map.put("张三", 25);
        map.put("李四", 30);
        map.put("王五", 22);

        System.out.println("张三的年龄: " + map.get("张三"));
        System.out.println("是否存在张三: " + map.containsKey("张三"));

        // 2. 遍历（面试常问）
        map.forEach((key, value) -> {
            System.out.println(key + " = " + value);
        });

        // 3. 面试重点：HashMap 底层是什么？
        // 答：数组 + 链表 + 红黑树（JDK 8+）
        // put 流程：先算 hash → 找数组下标 → 没冲突直接放 → 冲突了挂链表 → 链表>8转红黑树
        // 扩容：容量*2，重新散列

        // 4. 线程不安全演示（ConcurrentModificationException）
        // HashMap 不是线程安全的，多线程要用 ConcurrentHashMap
    }
}