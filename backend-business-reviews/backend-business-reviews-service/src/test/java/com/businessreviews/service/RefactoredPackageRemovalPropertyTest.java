package com.businessreviews.service;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 属性测试：验证refactored包已删除
 * 
 * **Feature: alibaba-package-restructure, Property 8: Refactored Package Removal**
 * **Validates: Requirements 4.3, 4.4**
 * 
 * 验证service.refactored包已被完全删除，不存在任何类
 */
class RefactoredPackageRemovalPropertyTest {
    
    /**
     * Property 8: Refactored Package Removal
     * 验证service.refactored包目录中不存在任何Java文件
     */
    @Test
    void refactoredPackageShouldNotContainAnyJavaFiles() {
        // 构建refactored包的可能路径
        String[] possibleBasePaths = {
            "src/main/java/com/businessreviews/service/refactored",
            "../backend-business-reviews-service/src/main/java/com/businessreviews/service/refactored",
            "backend-business-reviews-service/src/main/java/com/businessreviews/service/refactored"
        };
        
        for (String basePath : possibleBasePaths) {
            Path refactoredPath = Paths.get(basePath);
            
            if (Files.exists(refactoredPath)) {
                try (Stream<Path> walk = Files.walk(refactoredPath)) {
                    long javaFileCount = walk
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .count();
                    
                    assertThat(javaFileCount)
                        .as("refactored directory at %s should not contain any Java files", basePath)
                        .isZero();
                } catch (Exception e) {
                    // 如果无法遍历目录，继续检查其他路径
                }
            }
        }
        // 如果所有路径都不存在或为空，测试通过
    }
    
    /**
     * 验证UserServiceV2类不存在
     */
    @Test
    void userServiceV2ShouldNotExist() {
        try {
            Class.forName("com.businessreviews.service.refactored.UserServiceV2");
            assertThat(false)
                .as("UserServiceV2 class should not exist")
                .isTrue();
        } catch (ClassNotFoundException e) {
            // 预期行为：类不存在
            assertThat(true).isTrue();
        }
    }
    
    /**
     * 验证ShopServiceV2类不存在
     */
    @Test
    void shopServiceV2ShouldNotExist() {
        try {
            Class.forName("com.businessreviews.service.refactored.ShopServiceV2");
            assertThat(false)
                .as("ShopServiceV2 class should not exist")
                .isTrue();
        } catch (ClassNotFoundException e) {
            // 预期行为：类不存在
            assertThat(true).isTrue();
        }
    }
    
    /**
     * 验证NoteServiceV2类不存在
     */
    @Test
    void noteServiceV2ShouldNotExist() {
        try {
            Class.forName("com.businessreviews.service.refactored.NoteServiceV2");
            assertThat(false)
                .as("NoteServiceV2 class should not exist")
                .isTrue();
        } catch (ClassNotFoundException e) {
            // 预期行为：类不存在
            assertThat(true).isTrue();
        }
    }
    
    /**
     * 验证UserServiceV2Impl类不存在
     */
    @Test
    void userServiceV2ImplShouldNotExist() {
        try {
            Class.forName("com.businessreviews.service.refactored.impl.UserServiceV2Impl");
            assertThat(false)
                .as("UserServiceV2Impl class should not exist")
                .isTrue();
        } catch (ClassNotFoundException e) {
            // 预期行为：类不存在
            assertThat(true).isTrue();
        }
    }
}
