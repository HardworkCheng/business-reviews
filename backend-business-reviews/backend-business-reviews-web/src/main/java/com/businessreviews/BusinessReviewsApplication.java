package com.businessreviews;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 美食点评系统统一启动类
 * 整合移动端API和商家运营中心API
 * 
 * 移动端API路径: /api/...
 * 商家端API路径: /api/merchant/...
 * 
 * @author businessreviews
 */
@SpringBootApplication
@MapperScan("com.businessreviews.mapper")
@EnableAsync
@EnableScheduling
@Import(AiServicesAutoConfig.class)
public class BusinessReviewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessReviewsApplication.class, args);
    }
}
