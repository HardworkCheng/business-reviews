package com.businessreviews.merchant.config;

import com.businessreviews.merchant.interceptor.MerchantAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 商家端Web配置
 */
@Configuration
@RequiredArgsConstructor
public class MerchantWebConfig implements WebMvcConfigurer {

    private final MerchantAuthInterceptor merchantAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(merchantAuthInterceptor)
                .addPathPatterns("/merchant/**")
                .excludePathPatterns(
                        "/merchant/auth/login",
                        "/merchant/auth/register",
                        "/merchant/auth/send-code"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
