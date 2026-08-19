package com.mindborn.day10;

/**
 * Text Blocks 文本块 Demo
 */
public class TextBlockDemo {

    public static void main(String[] args) {
        //1.基本多行字符串
        String poem = """
                1
                2
                3
                4
                5
                """;
        System.out.println("===古诗===");
        System.out.println(poem);

        //2.SQL查询
        String sql = """
            SELECT
                u.id,
                u.username,
                u.email,
                COUNT(a.id) AS article_count
            FROM user u
            LEFT JOIN article a ON u.id = a.author_id
            WHERE u.status = 1
                AND u.create_time > '2024-01-01'
            GROUP BY u.id
            HAVING article_count >= 5
            ORDER BY article_count DESC
            LIMIT 10
            """;

        System.out.println("=== SQL ===");
        System.out.println(sql);

        //3.JSON字符串
        String json = """
                {
                    "code":200,
                    "message": "success",
                    "data":{
                        "id":1,
                        "username": "mindborn"
                    }
                }
                """;
        System.out.println("=== JSON ===");
        System.out.println(json);

        // 4. 行尾 \ 合并多行
        String inlineSql = """
            SELECT id, username, email \
            FROM user \
            WHERE status = 1
            """;
        System.out.println("=== 合并后的SQL（一行）===");
        System.out.println(inlineSql);

        // 5. formatted 格式化
        String dynamicSql = """
            SELECT * FROM %s
            WHERE id = %d
            AND status = %d
            """.formatted("user", 10086, 1);
        System.out.println("=== 格式化SQL ===");
        System.out.println(dynamicSql);
    }
}
