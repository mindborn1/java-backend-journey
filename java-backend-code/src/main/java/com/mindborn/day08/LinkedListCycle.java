package com.mindborn.day08;

import java.util.List;

public class LinkedListCycle {
    static class ListNode{

            int val;
            ListNode next;

            ListNode(int x) {
                val = x;
                next = null;
            }
    }
    public boolean hasCycle(ListNode head) {
        if(head == null || head.next == null)
            return false;
        ListNode slow = head;
        ListNode fast = head;
        while(fast.next != null && fast != null){
            slow = slow.next;
            fast = fast.next.next;
            if(slow == fast)
                return true;
        }
        return false;
    }
    public static void main(String[] args) {
        LinkedListCycle solution = new LinkedListCycle();

        // 测试用例 1：有环
        // 1 -> 2 -> 3 -> 4 -> 2（4的next指向2，形成环）
        ListNode node1 = new ListNode(1);
        ListNode node2 = new ListNode(2);
        ListNode node3 = new ListNode(3);
        ListNode node4 = new ListNode(4);
        node1.next = node2;
        node2.next = node3;
        node3.next = node4;
        node4.next = node2;  // 成环
        System.out.println("测试1（有环）: " + solution.hasCycle(node1));  // true

        // 测试用例 2：无环
        // 1 -> 2 -> 3 -> null
        ListNode a1 = new ListNode(1);
        ListNode a2 = new ListNode(2);
        ListNode a3 = new ListNode(3);
        a1.next = a2;
        a2.next = a3;
        System.out.println("测试2（无环）: " + solution.hasCycle(a1));  // false
    }
}
