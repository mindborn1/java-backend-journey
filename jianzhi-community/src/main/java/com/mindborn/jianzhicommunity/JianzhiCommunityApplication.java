package com.mindborn.jianzhicommunity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.mindborn.jianzhicommunity.mapper")
@EnableAsync
@EnableScheduling
public class JianzhiCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(JianzhiCommunityApplication.class, args);
    }
}