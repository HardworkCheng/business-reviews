package com.businessreviews.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * 跨域配置
 * 支持移动端和商家端的跨域请求
 */
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域名
        config.addAllowedOriginPattern("*");
        // 允许所有请求头，包括Private Network Access相关头
        config.addAllowedHeader("*");
        // 允许所有请求方法
        config.addAllowedMethod("*");
        // 允许携带Cookie
        config.setAllowCredentials(true);
        // 预检请求缓存时间
        config.setMaxAge(3600L);
        // 暴露响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Access-Control-Allow-Private-Network");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
