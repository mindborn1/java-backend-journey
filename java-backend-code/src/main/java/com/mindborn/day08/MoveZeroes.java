package com.mindborn.day08;

/**
 * LeetCode 第 283 题：移动零
 * 核心：双指针，slow 指向放非零的位置，fast 遍历找非零
 */
public class MoveZeroes {

    public void moveZeroes(int[] nums) {
        int slow = 0;  // 指向下一个该放非零元素的位置

        // fast 遍历数组
        for (int fast = 0; fast < nums.length; fast++) {
            if (nums[fast] != 0) {
                // 找到非零元素，放到 slow 位置
                nums[slow] = nums[fast];
                slow++;
            }
        }

        // slow 后面的位置全部填 0
        while (slow < nums.length) {
            nums[slow] = 0;
            slow++;
        }
    }

    public static void main(String[] args) {
        MoveZeroes solution = new MoveZeroes();

        // 测试用例 1
        int[] nums1 = {0, 1, 0, 3, 12};
        solution.moveZeroes(nums1);
        System.out.print("测试1: ");
        for (int num : nums1) {
            System.out.print(num + " ");
        }
        System.out.println();  // 期望：1 3 12 0 0

        // 测试用例 2
        int[] nums2 = {0, 0, 1};
        solution.moveZeroes(nums2);
        System.out.print("测试2: ");
        for (int num : nums2) {
            System.out.print(num + " ");
        }
        System.out.println();  // 期望：1 0 0

        // 测试用例 3：没有 0
        int[] nums3 = {1, 2, 3};
        solution.moveZeroes(nums3);
        System.out.print("测试3: ");
        for (int num : nums3) {
            System.out.print(num + " ");
        }
        System.out.println();  // 期望：1 2 3
    }
}