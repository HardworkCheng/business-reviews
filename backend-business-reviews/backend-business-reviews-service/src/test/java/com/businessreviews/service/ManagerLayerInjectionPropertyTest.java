package com.businessreviews.service;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 7: Manager Layer Injection
 * Validates: Requirements 9.4
 * 
 * 验证Manager类只被Service层注入，不被Controller或Mapper层注入
 */
public class ManagerLayerInjectionPropertyTest {

    @Test
    void managerClassesShouldOnlyBeInjectedIntoServiceLayer() {
        // Check that no Controller classes inject Manager classes
        Reflections controllerReflections = new Reflections(
            "com.businessreviews.web",
            Scanners.SubTypes.filterResultsBy(s -> true)
        );
        
        Set<Class<?>> controllerClasses = controllerReflections.getSubTypesOf(Object.class);
        
        for (Class<?> clazz : controllerClasses) {
            if (clazz.getSimpleName().endsWith("Controller")) {
                for (Field field : clazz.getDeclaredFields()) {
                    String fieldTypeName = field.getType().getSimpleName();
                    assertFalse(
                        fieldTypeName.endsWith("Manager"),
                        "Controller " + clazz.getSimpleName() + " should not inject Manager class: " + fieldTypeName
                    );
                }
            }
        }
    }

    @Test
    void managerClassesShouldNotBeInjectedIntoMapperLayer() {
        // Check that no Mapper classes inject Manager classes
        Reflections mapperReflections = new Reflections(
            "com.businessreviews.mapper",
            Scanners.SubTypes.filterResultsBy(s -> true)
        );
        
        Set<Class<?>> mapperClasses = mapperReflections.getSubTypesOf(Object.class);
        
        for (Class<?> clazz : mapperClasses) {
            if (clazz.getSimpleName().endsWith("Mapper")) {
                for (Field field : clazz.getDeclaredFields()) {
                    String fieldTypeName = field.getType().getSimpleName();
                    assertFalse(
                        fieldTypeName.endsWith("Manager"),
                        "Mapper " + clazz.getSimpleName() + " should not inject Manager class: " + fieldTypeName
                    );
                }
            }
        }
    }
}
