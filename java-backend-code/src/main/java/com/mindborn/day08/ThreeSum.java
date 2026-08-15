package com.mindborn.day08;

import java.util.*;

public class ThreeSum {
    
    public List<List<Integer>> threeSum(int[] nums) {
        // 你自己写，参考上面的伪代码
        List<List<Integer>> result = new ArrayList<>();

        if(nums == null|| nums.length< 3)
            return result;
        Arrays.sort(nums);

        for(int i = 0; i < nums.length - 3 ; i++){
            if(nums[i] > 0)
                break;
            if (i > 0 && nums[i] == nums[ i - 1])
                continue;

            int left = i + 1, right = nums.length - 1;
            while (left < right){
                int sum = nums[i] + nums[left] +nums[right];
                if(sum == 0){
                    result.add(Arrays.asList(nums[i], nums[left], nums[right]));

                    while (left <right && nums[left] == nums[left + 1])
                        left++;
                    while (left < right && nums[right] == nums[right - 1])
                        right--;

                left++;  right--;
                }
                else if(sum < 0) {
                    left++;
                }else
                    right--;
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
        ThreeSum solution = new ThreeSum();

        // 测试用例 1
        int[] nums1 = {-1, 0, 1, 2, -1, -4};
        System.out.println("测试1: " + solution.threeSum(nums1));
        // 期望输出：[[-1, -1, 2], [-1, 0, 1]]

        // 测试用例 2：全是 0
        int[] nums2 = {0, 0, 0, 0};
        System.out.println("测试2: " + solution.threeSum(nums2));
        // 期望输出：[[0, 0, 0]]

        // 测试用例 3：无解
        int[] nums3 = {1, 2, -2, -1};
        System.out.println("测试3: " + solution.threeSum(nums3));
        // 期望输出：[]
    }
}