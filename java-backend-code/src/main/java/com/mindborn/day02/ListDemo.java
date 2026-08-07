package com.mindborn.day02;

import java.util.ArrayList;
import java.util.LinkedList;

public class ListDemo {
    public static void main(String[] args) {
        // ArrayList：底层是数组，查询快 O(1)，增删慢 O(n)
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("A");      // 尾部添加，快
        arrayList.get(0);        // 随机访问，快
        arrayList.remove(0);     // 头部删除，慢（要移动后面所有元素）

        // LinkedList：底层是双向链表，增删快 O(1)，查询慢 O(n)
        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.addFirst("A");   // 头部添加，快
        linkedList.addLast("B");    // 尾部添加，快
        linkedList.get(0);          // 随机访问，慢（要遍历）

        // 面试考点：
        // 1. ArrayList 扩容：默认容量10，扩容1.5倍，用 Arrays.copyOf
        // 2. LinkedList 没有扩容概念，每次 new 节点
        // 3. 频繁随机访问 → 用 ArrayList；频繁头尾增删 → 用 LinkedList
    }
}
