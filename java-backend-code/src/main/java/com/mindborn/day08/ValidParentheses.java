package com.mindborn.day08;

import java.util.Stack;

/**
 * LeetCode 第 20 题：有效的括号
 * 核心：栈，左括号入栈，右括号匹配栈顶
 */
public class ValidParentheses {

    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();

        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                // 左括号：压入栈
                stack.push(c);
            } else {
                // 右括号：栈为空或栈顶不匹配，直接无效
                if (stack.isEmpty()) {
                    return false;
                }

                char top = stack.pop();
                if (c == ')' && top != '(') return false;
                if (c == ']' && top != '[') return false;
                if (c == '}' && top != '{') return false;
            }
        }

        // 栈为空说明全部匹配完，不为空说明有左括号没闭合
        return stack.isEmpty();
    }

    public static void main(String[] args) {
        ValidParentheses solution = new ValidParentheses();

        System.out.println("测试1: " + solution.isValid("()"));      // true
        System.out.println("测试2: " + solution.isValid("()[]{}")); // true
        System.out.println("测试3: " + solution.isValid("(]"));     // false
        System.out.println("测试4: " + solution.isValid("([)]"));   // false
        System.out.println("测试5: " + solution.isValid("{[]}"));   // true
        System.out.println("测试6: " + solution.isValid("("));      // false
    }
}