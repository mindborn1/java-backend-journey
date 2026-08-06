package com.mindborn.day01;

public class TextBlockDemo {
    public static void main(String[] args) {
        //java 15+ 文本块：三个双引号
        String json = """
                {
                "name": "张三",
                "age": 25,
                "city"; "北京"
                }
                """;

        System.out.println("=== 文本块 ===");
        System.out.println(json);

        // 传统方式对比（需要大量转义）
        String oldJson = "{\n" +
                "    \"name\": \"张三\",\n" +
                "    \"age\": 25,\n" +
                "    \"city\": \"北京\"\n" +
                "}";

        System.out.println("=== 传统字符串 ===");
        System.out.println(oldJson);
    }
}
