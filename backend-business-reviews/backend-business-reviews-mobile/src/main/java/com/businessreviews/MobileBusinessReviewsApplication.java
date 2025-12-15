package com.businessreviews;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 美食点评系统移动端启动类
 */
@SpringBootApplication
@MapperScan("com.businessreviews.mapper")
@EnableAsync
@EnableScheduling
public class MobileBusinessReviewsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MobileBusinessReviewsApplication.class, args);
    }
}
