package com.businessreviews.config;

import com.businessreviews.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 认证模块 - 无需登录
                        "/api/auth/send-code",
                        "/api/auth/login-by-code",
                        "/api/auth/login",
                        "/api/auth/oauth-login",
                        // 公共模块 - 无需登录
                        "/api/categories",
                        "/api/categories/**",
                        "/api/topics",
                        "/api/topics/hot",
                        "/api/search/**",
                        // 笔记模块 - 部分无需登录
                        "/api/notes/recommended",
                        "/api/notes/explore",
                        "/api/notes/nearby",
                        "/api/notes/following",
                        // 商家模块 - 部分无需登录
                        "/api/shops",
                        "/api/shops/nearby",
                        "/api/shops/search",
                        // 静态资源
                        "/static/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
