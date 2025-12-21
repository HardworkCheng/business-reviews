package com.businessreviews.merchant.controller;

import com.businessreviews.common.Result;
import com.businessreviews.web.merchant.MerchantAuthController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Controller响应格式属性测试
 * 
 * **Feature: backend-alibaba-refactor, Property 1: API响应格式一致性**
 * **Validates: Requirements 1.2**
 * 
 * 验证所有Controller方法返回值都使用Result<T>包装
 */
class ControllerResponsePropertyTest {
    
    /**
     * Property 1: API响应格式一致性
     * *For any* Controller方法，返回类型 SHALL 为Result<T>
     */
    @Test
    void merchantAuthControllerShouldReturnResultType() {
        Class<?> controllerClass = MerchantAuthController.class;
        Method[] methods = controllerClass.getDeclaredMethods();
        
        for (Method method : methods) {
            // 跳过非public方法和Object类的方法
            if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            
            Class<?> returnType = method.getReturnType();
            
            assertThat(returnType)
                .as("Method %s should return Result type", method.getName())
                .isEqualTo(Result.class);
        }
    }
    
    /**
     * 验证Controller只依赖Service接口
     */
    @Test
    void controllerShouldOnlyDependOnServiceInterface() {
        Class<?> controllerClass = MerchantAuthController.class;
        
        Arrays.stream(controllerClass.getDeclaredFields())
            .filter(field -> field.getType().getSimpleName().endsWith("Service"))
            .forEach(field -> {
                Class<?> fieldType = field.getType();
                
                // Service字段应该是接口类型
                assertThat(fieldType.isInterface())
                    .as("Field %s should be an interface type", field.getName())
                    .isTrue();
                
                // Service字段名不应该以Impl结尾
                assertThat(fieldType.getSimpleName())
                    .as("Field %s should not be an implementation class", field.getName())
                    .doesNotEndWith("Impl");
            });
    }
    
    /**
     * 验证Controller方法使用@Valid注解进行参数校验
     */
    @Test
    void controllerMethodsShouldUseValidAnnotation() {
        Class<?> controllerClass = MerchantAuthController.class;
        Method[] methods = controllerClass.getDeclaredMethods();
        
        for (Method method : methods) {
            if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            
            // 检查带有@RequestBody注解的参数是否也有@Valid注解
            Arrays.stream(method.getParameters())
                .filter(param -> param.isAnnotationPresent(org.springframework.web.bind.annotation.RequestBody.class))
                .forEach(param -> {
                    boolean hasValid = param.isAnnotationPresent(jakarta.validation.Valid.class);
                    assertThat(hasValid)
                        .as("Parameter %s in method %s should have @Valid annotation", 
                            param.getName(), method.getName())
                        .isTrue();
                });
        }
    }
}
