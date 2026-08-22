package com.mindborn.day12;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Day12 事务传播行为练习启动类
 *
 * @SpringBootApplication 自动扫描同级包和子包下的所有 Bean
 * @MapperScan 扫描 day12.mapper 包下的 Mapper 接口
 */
@SpringBootApplication
@MapperScan("com.mindborn.day12.mapper")
public class Day12TransactionDemo {
    public static void main(String[] args) {
        SpringApplication.run(Day12TransactionDemo.class, args);
        System.out.println("Day12 事务传播行为练习启动成功！");
    }
}

