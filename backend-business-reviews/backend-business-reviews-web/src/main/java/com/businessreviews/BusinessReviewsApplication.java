package com.businessreviews;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 美食点评系统启动类
 */
@SpringBootApplication
@MapperScan("com.businessreviews.mapper")
@EnableAsync
public class BusinessReviewsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BusinessReviewsApplication.class, args);
    }
}
