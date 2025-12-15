package com.businessreviews.config;

import com.businessreviews.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        // WebSocket端点不需要拦截（WebSocket有自己的认证机制）
                        "/api/ws",
                        "/api/ws/**",
                        
                        // 认证相关接口不需要拦截
                        "/auth/**",
                        
                        // 公开的笔记接口（不需要登录）
                        "/notes/recommended",
                        "/notes/explore",
                        "/notes/nearby",
                        "/notes/user/**",
                        
                        // 公开的商家接口
                        "/shops/**",
                        
                        // 公开的分类接口
                        "/categories/**",
                        
                        // 搜索接口
                        "/search/**",
                        
                        // 话题接口
                        "/topics/**",
                        
                        // 优惠券公开接口
                        "/coupons"
                );
    }
}
