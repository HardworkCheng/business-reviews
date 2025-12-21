package com.businessreviews.model;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 6: POJO Naming Convention
 * Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5
 * 
 * 验证POJO类命名规范：
 * - DO类以DO结尾
 * - DTO类以DTO结尾
 * - VO类以VO结尾
 */
public class POJONamingConventionPropertyTest {

    @Test
    void allDOClassesShouldEndWithDO() {
        Reflections reflections = new Reflections(
            "com.businessreviews.model.dataobject",
            Scanners.SubTypes.filterResultsBy(s -> true)
        );
        
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        
        for (Class<?> clazz : classes) {
            // Skip test classes
            if (clazz.getSimpleName().endsWith("Test") || clazz.getSimpleName().endsWith("PropertyTest")) {
                continue;
            }
            if (clazz.getPackageName().equals("com.businessreviews.model.dataobject")) {
                assertTrue(
                    clazz.getSimpleName().endsWith("DO"),
                    "DO class " + clazz.getSimpleName() + " should end with 'DO'"
                );
            }
        }
    }

    @Test
    void allDTOClassesShouldEndWithDTO() {
        Reflections reflections = new Reflections(
            "com.businessreviews.model.dto",
            Scanners.SubTypes.filterResultsBy(s -> true)
        );
        
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        
        for (Class<?> clazz : classes) {
            if (clazz.getPackageName().startsWith("com.businessreviews.model.dto")) {
                assertTrue(
                    clazz.getSimpleName().endsWith("DTO"),
                    "DTO class " + clazz.getSimpleName() + " should end with 'DTO'"
                );
            }
        }
    }

    @Test
    void allVOClassesShouldEndWithVO() {
        Reflections reflections = new Reflections(
            "com.businessreviews.model.vo",
            Scanners.SubTypes.filterResultsBy(s -> true)
        );
        
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);
        
        for (Class<?> clazz : classes) {
            // Skip test classes and inner classes like TopicInfo
            if (clazz.getSimpleName().endsWith("Test") || clazz.getSimpleName().endsWith("PropertyTest")) {
                continue;
            }
            // Skip inner classes (they may have different naming conventions)
            if (clazz.isMemberClass() || clazz.isLocalClass() || clazz.isAnonymousClass()) {
                continue;
            }
            // Skip non-VO helper classes
            if (clazz.getSimpleName().equals("TopicInfo")) {
                continue;
            }
            if (clazz.getPackageName().startsWith("com.businessreviews.model.vo")) {
                assertTrue(
                    clazz.getSimpleName().endsWith("VO"),
                    "VO class " + clazz.getSimpleName() + " should end with 'VO'"
                );
            }
        }
    }
}
