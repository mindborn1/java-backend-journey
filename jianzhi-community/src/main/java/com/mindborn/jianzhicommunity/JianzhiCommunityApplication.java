package com.mindborn.jianzhicommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mindborn.jianzhicommunity")
public class JianzhiCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(JianzhiCommunityApplication.class, args);
    }
}