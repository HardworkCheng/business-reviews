package com.businessreviews.service;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service接口包组织属性测试
 * 
 * **Feature: alibaba-package-restructure, Property 2: Service Interface Package Organization**
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
 * 
 * 验证Service接口包组织符合阿里巴巴Java开发手册规范：
 * - 商户端服务接口位于 service.merchant 包
 * - 用户端服务接口位于 service.app 包
 * - 公共服务接口位于 service.common 包
 * - service 根目录不存在重复的接口
 */
class ServiceInterfacePackageOrganizationPropertyTest {
    
    private static final String BASE_PACKAGE = "com.businessreviews.service";
    
    /**
     * Property 2.1: 商户端服务接口应位于 service.merchant 包
     * 
     * *For any* Service interface that serves the merchant client, 
     * it should be located in the `service.merchant` package.
     */
    @Test
    void merchantServiceInterfacesShouldBeInMerchantPackage() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE + ".merchant")
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> merchantInterfaces = reflections.getSubTypesOf(Object.class).stream()
            .filter(Class::isInterface)
            .filter(c -> c.getPackageName().equals(BASE_PACKAGE + ".merchant"))
            .filter(c -> c.getSimpleName().startsWith("Merchant"))
            .collect(Collectors.toSet());
        
        assertThat(merchantInterfaces)
            .as("Merchant service interfaces should exist in service.merchant package")
            .isNotEmpty();
        
        // 验证所有商户端接口都在正确的包中
        for (Class<?> iface : merchantInterfaces) {
            assertThat(iface.getPackageName())
                .as("Merchant interface %s should be in service.merchant package", iface.getSimpleName())
                .isEqualTo(BASE_PACKAGE + ".merchant");
        }
    }
    
    /**
     * Property 2.2: 用户端服务接口应位于 service.app 包
     * 
     * *For any* Service interface that serves the app/mobile client, 
     * it should be located in the `service.app` package.
     */
    @Test
    void appServiceInterfacesShouldBeInAppPackage() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE + ".app")
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> appInterfaces = reflections.getSubTypesOf(Object.class).stream()
            .filter(Class::isInterface)
            .filter(c -> c.getPackageName().equals(BASE_PACKAGE + ".app"))
            .filter(c -> c.getSimpleName().endsWith("Service"))
            .collect(Collectors.toSet());
        
        assertThat(appInterfaces)
            .as("App service interfaces should exist in service.app package")
            .isNotEmpty();
        
        // 验证所有用户端接口都在正确的包中
        for (Class<?> iface : appInterfaces) {
            assertThat(iface.getPackageName())
                .as("App interface %s should be in service.app package", iface.getSimpleName())
                .isEqualTo(BASE_PACKAGE + ".app");
        }
    }
    
    /**
     * Property 2.3: 公共服务接口应位于 service.common 包
     * 
     * *For any* Service interface that is shared between clients, 
     * it should be located in the `service.common` package.
     */
    @Test
    void commonServiceInterfacesShouldBeInCommonPackage() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE + ".common")
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> commonInterfaces = reflections.getSubTypesOf(Object.class).stream()
            .filter(Class::isInterface)
            .filter(c -> c.getPackageName().equals(BASE_PACKAGE + ".common"))
            .filter(c -> c.getSimpleName().endsWith("Service"))
            .collect(Collectors.toSet());
        
        assertThat(commonInterfaces)
            .as("Common service interfaces should exist in service.common package")
            .isNotEmpty();
        
        // 验证所有公共接口都在正确的包中
        for (Class<?> iface : commonInterfaces) {
            assertThat(iface.getPackageName())
                .as("Common interface %s should be in service.common package", iface.getSimpleName())
                .isEqualTo(BASE_PACKAGE + ".common");
        }
    }
    
    /**
     * Property 2.4: service 根目录不应存在重复的Service接口
     * 
     * *For any* Service interface in the project, there should be no duplicates 
     * at the root `service` package level.
     */
    @Test
    void noServiceInterfacesShouldExistInRootPackage() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE)
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> rootInterfaces = reflections.getSubTypesOf(Object.class).stream()
            .filter(Class::isInterface)
            .filter(c -> c.getPackageName().equals(BASE_PACKAGE))
            .filter(c -> c.getSimpleName().endsWith("Service"))
            .collect(Collectors.toSet());
        
        assertThat(rootInterfaces)
            .as("No Service interfaces should exist in root service package")
            .isEmpty();
    }
    
    /**
     * 验证所有Service接口都位于正确的子包中
     * 
     * *For any* Service interface in the project, it should be located in exactly one of:
     * `service.app`, `service.merchant`, or `service.common` packages.
     */
    @Test
    void allServiceInterfacesShouldBeInCorrectSubPackages() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE)
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> allInterfaces = reflections.getSubTypesOf(Object.class).stream()
            .filter(Class::isInterface)
            .filter(c -> c.getSimpleName().endsWith("Service"))
            .filter(c -> c.getPackageName().startsWith(BASE_PACKAGE))
            .filter(c -> !c.getPackageName().contains(".refactored")) // 排除refactored包
            .collect(Collectors.toSet());
        
        for (Class<?> iface : allInterfaces) {
            String packageName = iface.getPackageName();
            
            assertThat(packageName)
                .as("Service interface %s should be in app, merchant, or common sub-package", iface.getSimpleName())
                .satisfiesAnyOf(
                    p -> assertThat(p).isEqualTo(BASE_PACKAGE + ".app"),
                    p -> assertThat(p).isEqualTo(BASE_PACKAGE + ".merchant"),
                    p -> assertThat(p).isEqualTo(BASE_PACKAGE + ".common")
                );
        }
    }
}
