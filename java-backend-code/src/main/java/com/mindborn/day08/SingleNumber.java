package com.mindborn.day08;

/**
 * LeetCode 第 136 题：只出现一次的数字
 * 核心：异或运算，成对的数抵消为0，最后剩下目标数
 */
public class SingleNumber {

    public int singleNumber(int[] nums) {
        int result = 0;

        for (int num : nums) {
            result ^= num;  // 异或运算
        }

        return result;
    }

    public static void main(String[] args) {
        SingleNumber solution = new SingleNumber();

        // 测试用例 1
        int[] nums1 = {4, 1, 2, 1, 2};
        System.out.println("测试1: " + solution.singleNumber(nums1));  // 期望 4

        // 测试用例 2
        int[] nums2 = {2, 2, 1};
        System.out.println("测试2: " + solution.singleNumber(nums2));  // 期望 1

        // 测试用例 3：只有一个元素
        int[] nums3 = {1};
        System.out.println("测试3: " + solution.singleNumber(nums3));  // 期望 1
    }
}