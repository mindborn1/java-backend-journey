package com.mindborn.day08;

/**
 * switch 模式匹配练习
 */
public class PatternMatchingSwitch {

    /**
     * 根据对象类型返回不同描述
     */
    public static String describe(Object obj) {
        return switch (obj){
            case null -> "传入的是null";
            case Integer i when i > 0 -> "正整数：" + i;
            case Integer i when i < 0 -> "负整数：" + i;
            case Integer i -> "零";
            case String s when s.isEmpty() -> "空字符串";
            case String s ->"字符串（长度" + s.length() + ")：" + s;
            case Double d when d > 100 -> "大数：" + d;
            case Double d -> "小数：" + d;
            case Point p -> "坐标点：(" + p.x() + "," + p.y() + ")";
            default -> "未知类型：" + obj.getClass().getSimpleName();
        };
    }

    public static void main(String[] args) {
        System.out.println(describe(42));
        System.out.println(describe(-5));
        System.out.println(describe(0));
        System.out.println(describe("Hello"));
        System.out.println(describe(""));
        System.out.println(describe(3.14));
        System.out.println(describe(999.9));
        System.out.println(describe(new Point(1, 2)));
        System.out.println(describe(null));
        System.out.println(describe(new Object()));
    }
}
