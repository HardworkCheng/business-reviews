package com.businessreviews.model.dataobject;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DO类命名规范属性测试
 * 
 * **Feature: backend-alibaba-refactor, Property 2: DO类命名规范**
 * **Validates: Requirements 2.1**
 * 
 * 验证所有位于model.dataobject包下的类名都以"DO"结尾
 */
class DONamingPropertyTest {
    
    private static final String DO_PACKAGE = "com.businessreviews.model.dataobject";
    
    /**
     * Property 2: DO类命名规范
     * *For any* 位于model.dataobject包下的类，类名 SHALL 以"DO"结尾
     */
    @Test
    void allDOClassesShouldEndWithDO() {
        Set<Class<?>> doClasses = findClassesInPackage(DO_PACKAGE);
        
        assertThat(doClasses).isNotEmpty();
        
        for (Class<?> clazz : doClasses) {
            String className = clazz.getSimpleName();
            assertThat(className)
                .as("Class %s in DO package should end with 'DO'", className)
                .endsWith("DO");
        }
    }
    
    /**
     * 验证DO类都实现了Serializable接口
     */
    @Test
    void allDOClassesShouldBeSerializable() {
        Set<Class<?>> doClasses = findClassesInPackage(DO_PACKAGE);
        
        for (Class<?> clazz : doClasses) {
            assertThat(java.io.Serializable.class.isAssignableFrom(clazz))
                .as("Class %s should implement Serializable", clazz.getSimpleName())
                .isTrue();
        }
    }
    
    /**
     * 验证DO类都有@TableName注解
     */
    @Test
    void allDOClassesShouldHaveTableNameAnnotation() {
        Set<Class<?>> doClasses = findClassesInPackage(DO_PACKAGE);
        
        for (Class<?> clazz : doClasses) {
            assertThat(clazz.isAnnotationPresent(com.baomidou.mybatisplus.annotation.TableName.class))
                .as("Class %s should have @TableName annotation", clazz.getSimpleName())
                .isTrue();
        }
    }
    
    /**
     * 使用Reflections库查找指定包下的所有类（排除测试类）
     */
    private Set<Class<?>> findClassesInPackage(String packageName) {
        Reflections reflections = new Reflections(packageName, Scanners.SubTypes.filterResultsBy(s -> true));
        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);
        
        // 过滤掉测试类
        return allClasses.stream()
            .filter(clazz -> !clazz.getSimpleName().endsWith("Test"))
            .filter(clazz -> !clazz.getSimpleName().endsWith("Tests"))
            .filter(clazz -> !clazz.getName().contains("$")) // 排除内部类
            .collect(Collectors.toSet());
    }
}
