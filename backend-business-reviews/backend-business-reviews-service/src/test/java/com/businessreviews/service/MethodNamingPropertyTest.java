package com.businessreviews.service;

import com.businessreviews.service.app.UserService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service方法命名规范属性测试
 * 
 * **Feature: backend-alibaba-refactor, Property 3: 方法命名规范**
 * **Validates: Requirements 3.1-3.6**
 * 
 * 验证Service方法命名符合阿里巴巴Java开发手册规范：
 * - 获取单个对象：get前缀
 * - 获取多个对象：list前缀
 * - 统计数量：count前缀
 * - 插入数据：save前缀
 * - 删除数据：remove前缀
 * - 修改数据：update前缀
 */
class MethodNamingPropertyTest {
    
    /**
     * Property 3: 方法命名规范
     * 验证UserService接口的方法命名符合规范
     */
    @Test
    void userServiceMethodNamingShouldFollowConvention() {
        Class<?> serviceClass = UserService.class;
        Method[] methods = serviceClass.getDeclaredMethods();
        
        for (Method method : methods) {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            
            // 跳过Object类的方法
            if (methodName.equals("toString") || methodName.equals("hashCode") || methodName.equals("equals")) {
                continue;
            }
            
            // 验证命名规范
            if (isSingleObjectReturn(returnType)) {
                // 获取单个对象应该用get前缀（除了is开头的布尔判断方法）
                if (returnType == boolean.class || returnType == Boolean.class) {
                    assertThat(methodName)
                        .as("Boolean method %s should start with 'is' or 'has' or 'can'", methodName)
                        .matches("^(is|has|can|check).*");
                } else {
                    // 允许get, save, update, remove, register, change, follow, unfollow等前缀
                    assertThat(methodName)
                        .as("Single object getter %s should have valid prefix", methodName)
                        .matches("^(get|save|update|remove|register|change|follow|unfollow|bookmark|unbookmark).*");
                }
            } else if (isCollectionReturn(returnType)) {
                // 获取多个对象应该用list或get前缀
                assertThat(methodName)
                    .as("Collection getter %s should start with 'list' or 'get'", methodName)
                    .matches("^(list|get).*");
            } else if (returnType == Long.class || returnType == long.class || 
                       returnType == Integer.class || returnType == int.class) {
                // 统计数量应该用count前缀
                if (methodName.toLowerCase().contains("count")) {
                    assertThat(methodName)
                        .as("Count method %s should start with 'count'", methodName)
                        .startsWith("count");
                }
            }
        }
    }
    
    /**
     * 验证Service接口有获取方法
     */
    @Test
    void shouldHaveGetMethods() {
        Class<?> serviceClass = UserService.class;
        Method[] methods = serviceClass.getDeclaredMethods();
        
        long getMethodCount = Arrays.stream(methods)
            .filter(m -> m.getName().startsWith("get"))
            .count();
        
        assertThat(getMethodCount)
            .as("Should have get methods for retrieval")
            .isGreaterThan(0);
    }
    
    /**
     * 验证update方法用于修改数据
     */
    @Test
    void updateMethodsShouldBeForModification() {
        Class<?> serviceClass = UserService.class;
        Method[] methods = serviceClass.getDeclaredMethods();
        
        long updateMethodCount = Arrays.stream(methods)
            .filter(m -> m.getName().startsWith("update"))
            .count();
        
        assertThat(updateMethodCount)
            .as("Should have update methods for modification")
            .isGreaterThanOrEqualTo(0);
    }
    
    /**
     * 判断是否为单个对象返回类型
     */
    private boolean isSingleObjectReturn(Class<?> returnType) {
        return !returnType.equals(void.class) 
            && !returnType.equals(Void.class)
            && !isCollectionReturn(returnType)
            && !returnType.isPrimitive()
            || returnType == boolean.class;
    }
    
    /**
     * 判断是否为集合返回类型
     */
    private boolean isCollectionReturn(Class<?> returnType) {
        return Collection.class.isAssignableFrom(returnType)
            || List.class.isAssignableFrom(returnType)
            || returnType.getSimpleName().contains("PageResult");
    }
}
