package com.mindborn.day08;

/**
 * LeetCode 第 70 题：爬楼梯
 * 核心：动态规划，f(n) = f(n-1) + f(n-2)，斐波那契
 */
public class ClimbingStairs {

    public int climbStairs(int n) {
        // 边界情况
        if (n <= 2) {
            return n;
        }

        // 只需要两个变量滚动计算，不用数组
        int prev2 = 1;  // f(n-2)，初始 f(1) = 1
        int prev1 = 2;  // f(n-1)，初始 f(2) = 2

        for (int i = 3; i <= n; i++) {
            int curr = prev1 + prev2;  // f(n) = f(n-1) + f(n-2)
            prev2 = prev1;              // 更新 f(n-2)
            prev1 = curr;               // 更新 f(n-1)
        }

        return prev1;
    }

    public static void main(String[] args) {
        ClimbingStairs solution = new ClimbingStairs();

        System.out.println("n=1: " + solution.climbStairs(1));  // 1
        System.out.println("n=2: " + solution.climbStairs(2));  // 2
        System.out.println("n=3: " + solution.climbStairs(3));  // 3
        System.out.println("n=4: " + solution.climbStairs(4));  // 5
        System.out.println("n=5: " + solution.climbStairs(5));  // 8
        System.out.println("n=10: " + solution.climbStairs(10)); // 89
    }
}