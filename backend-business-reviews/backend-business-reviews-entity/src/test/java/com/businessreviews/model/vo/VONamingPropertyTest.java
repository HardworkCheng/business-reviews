package com.businessreviews.model.vo;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VO类命名规范属性测试
 * 
 * **Feature: backend-alibaba-refactor, Property 4: VO类命名规范**
 * **Validates: Requirements 2.3**
 * 
 * 验证所有位于model.vo包下的类名都以"VO"结尾
 */
class VONamingPropertyTest {
    
    private static final String VO_PACKAGE = "com.businessreviews.model.vo";
    
    /**
     * Property 4: VO类命名规范
     * *For any* 位于model.vo包下的类，类名 SHALL 以"VO"结尾
     */
    @Test
    void allVOClassesShouldEndWithVO() {
        Set<Class<?>> voClasses = findClassesInPackage(VO_PACKAGE);
        
        assertThat(voClasses).isNotEmpty();
        
        for (Class<?> clazz : voClasses) {
            String className = clazz.getSimpleName();
            assertThat(className)
                .as("Class %s in VO package should end with 'VO'", className)
                .endsWith("VO");
        }
    }
    
    /**
     * 验证VO类都实现了Serializable接口
     */
    @Test
    void allVOClassesShouldBeSerializable() {
        Set<Class<?>> voClasses = findClassesInPackage(VO_PACKAGE);
        
        for (Class<?> clazz : voClasses) {
            assertThat(java.io.Serializable.class.isAssignableFrom(clazz))
                .as("Class %s should implement Serializable", clazz.getSimpleName())
                .isTrue();
        }
    }
    
    /**
     * 验证VO类不应该有@TableName注解（VO不是数据库映射对象）
     */
    @Test
    void voClassesShouldNotHaveTableNameAnnotation() {
        Set<Class<?>> voClasses = findClassesInPackage(VO_PACKAGE);
        
        for (Class<?> clazz : voClasses) {
            assertThat(clazz.isAnnotationPresent(com.baomidou.mybatisplus.annotation.TableName.class))
                .as("VO class %s should NOT have @TableName annotation", clazz.getSimpleName())
                .isFalse();
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
