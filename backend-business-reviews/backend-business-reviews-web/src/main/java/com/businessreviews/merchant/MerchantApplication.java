package com.businessreviews.merchant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 商家运营中心启动类
 */
@SpringBootApplication(scanBasePackages = "com.businessreviews")
@MapperScan("com.businessreviews.mapper")
@EnableAsync
public class MerchantApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MerchantApplication.class, args);
    }
}
