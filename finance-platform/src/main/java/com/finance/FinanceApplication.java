package com.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.finance.mapper")
@EnableScheduling // 💡 核心：开启 Spring 自动定时任务调度功能
public class FinanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }
}