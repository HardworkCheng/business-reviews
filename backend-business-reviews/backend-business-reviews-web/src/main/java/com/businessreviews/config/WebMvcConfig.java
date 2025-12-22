package com.businessreviews.config;

import com.businessreviews.interceptor.AuthInterceptor;
import com.businessreviews.merchant.interceptor.MerchantAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 统一Web配置
 * 整合移动端和商家端的拦截器配置
 * 
 * Controller包结构（符合阿里巴巴Java开发手册规范）：
 * - web.app: 用户端/移动端控制器 (UniApp)
 * - web.merchant: 商户端控制器 (Web运营中心)
 * 
 * 拦截器配置基于URL路径：
 * - /merchant/** 路径由 MerchantAuthInterceptor 处理
 * - 其他路径由 AuthInterceptor 处理
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final MerchantAuthInterceptor merchantAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 商家端拦截器 - 只拦截 /merchant/** 路径
        registry.addInterceptor(merchantAuthInterceptor)
                .addPathPatterns("/merchant/**")
                .excludePathPatterns(
                        "/merchant/auth/login",
                        "/merchant/auth/register",
                        "/merchant/auth/send-code",
                        "/merchant/upload/public"
                );
        
        // 移动端拦截器 - 拦截除商家端外的所有路径
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 商家端路径由商家拦截器处理
                        "/merchant/**",
                        
                        // WebSocket端点（注意：context-path是/api，所以这里用相对路径）
                        "/ws",
                        "/ws/**",
                        
                        // 认证相关接口
                        "/auth/**",
                        
                        // 公开的笔记接口
                        "/notes/recommended",
                        "/notes/explore",
                        "/notes/nearby",
                        "/notes/user/**",
                        
                        // 公开的商家接口（只读操作）
                        "/shops",
                        "/shops/nearby",
                        "/shops/search",
                        "/shops/registered",
                        
                        // 公开的分类接口
                        "/categories/**",
                        
                        // 搜索接口
                        "/search/**",
                        
                        // 话题接口
                        "/topics/**"
                );
    }
}
