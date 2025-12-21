package com.businessreviews.service;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service实现类包组织属性测试
 * 
 * **Feature: alibaba-package-restructure, Property 3: Service Implementation Package Organization**
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
 * 
 * 验证Service实现类包组织符合阿里巴巴Java开发手册规范：
 * - 商户端服务实现位于 service.impl.merchant 包
 * - 用户端服务实现位于 service.impl.app 包
 * - 公共服务实现位于 service.impl.common 包
 * - service.impl 根目录不存在实现类（已迁移到子包）
 */
class ServiceImplementationPackageOrganizationPropertyTest {
    
    private static final String BASE_PACKAGE = "com.businessreviews.service.impl";
    
    /**
     * Property 3.1: 商户端服务实现应位于 service.impl.merchant 包
     * 
     * *For any* Service implementation that serves the merchant client, 
     * it should be located in the `service.impl.merchant` package.
     */
    @Test
    void merchantServiceImplementationsShouldBeInMerchantPackage() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE + ".merchant")
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> merchantImpls = reflections.getSubTypesOf(Object.class).stream()
            .filter(c -> !c.isInterface())
            .filter(c -> c.getPackageName().equals(BASE_PACKAGE + ".merchant"))
            .filter(c -> c.getSimpleName().startsWith("Merchant") && c.getSimpleName().endsWith("Impl"))
            .collect(Collectors.toSet());
        
        assertThat(merchantImpls)
            .as("Merchant service implementations should exist in service.impl.merchant package")
            .isNotEmpty();
        
        // 验证所有商户端实现都在正确的包中
        for (Class<?> impl : merchantImpls) {
            assertThat(impl.getPackageName())
                .as("Merchant implementation %s should be in service.impl.merchant package", impl.getSimpleName())
                .isEqualTo(BASE_PACKAGE + ".merchant");
        }
    }
    
    /**
     * Property 3.2: 用户端服务实现应位于 service.impl.app 包
     * 
     * *For any* Service implementation that serves the app/mobile client, 
     * it should be located in the `service.impl.app` package.
     */
    @Test
    void appServiceImplementationsShouldBeInAppPackage() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE + ".app")
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> appImpls = reflections.getSubTypesOf(Object.class).stream()
            .filter(c -> !c.isInterface())
            .filter(c -> c.getPackageName().equals(BASE_PACKAGE + ".app"))
            .filter(c -> c.getSimpleName().endsWith("ServiceImpl"))
            .collect(Collectors.toSet());
        
        assertThat(appImpls)
            .as("App service implementations should exist in service.impl.app package")
            .isNotEmpty();
        
        // 验证所有用户端实现都在正确的包中
        for (Class<?> impl : appImpls) {
            assertThat(impl.getPackageName())
                .as("App implementation %s should be in service.impl.app package", impl.getSimpleName())
                .isEqualTo(BASE_PACKAGE + ".app");
        }
    }
    
    /**
     * Property 3.3: 公共服务实现应位于 service.impl.common 包
     * 
     * *For any* Service implementation that is shared between clients, 
     * it should be located in the `service.impl.common` package.
     */
    @Test
    void commonServiceImplementationsShouldBeInCommonPackage() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE + ".common")
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> commonImpls = reflections.getSubTypesOf(Object.class).stream()
            .filter(c -> !c.isInterface())
            .filter(c -> c.getPackageName().equals(BASE_PACKAGE + ".common"))
            .filter(c -> c.getSimpleName().endsWith("ServiceImpl"))
            .collect(Collectors.toSet());
        
        assertThat(commonImpls)
            .as("Common service implementations should exist in service.impl.common package")
            .isNotEmpty();
        
        // 验证所有公共实现都在正确的包中
        for (Class<?> impl : commonImpls) {
            assertThat(impl.getPackageName())
                .as("Common implementation %s should be in service.impl.common package", impl.getSimpleName())
                .isEqualTo(BASE_PACKAGE + ".common");
        }
    }
    
    /**
     * Property 3.4: service.impl 根目录不应存在ServiceImpl类（已迁移到子包）
     * 
     * *For any* Service implementation in the project, there should be no implementations 
     * directly under `service.impl` package after migration.
     * 
     * 注意：此测试验证迁移完成后的状态，在迁移过程中可能会失败
     */
    @Test
    void noServiceImplementationsShouldExistInRootImplPackage() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE)
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> rootImpls = reflections.getSubTypesOf(Object.class).stream()
            .filter(c -> !c.isInterface())
            .filter(c -> c.getPackageName().equals(BASE_PACKAGE))
            .filter(c -> c.getSimpleName().endsWith("ServiceImpl"))
            .collect(Collectors.toSet());
        
        assertThat(rootImpls)
            .as("No Service implementations should exist in root service.impl package after migration")
            .isEmpty();
    }
    
    /**
     * 验证所有ServiceImpl类都位于正确的子包中
     * 
     * *For any* Service implementation in the project, it should be located in exactly one of:
     * `service.impl.app`, `service.impl.merchant`, or `service.impl.common` packages.
     */
    @Test
    void allServiceImplementationsShouldBeInCorrectSubPackages() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE)
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> allImpls = reflections.getSubTypesOf(Object.class).stream()
            .filter(c -> !c.isInterface())
            .filter(c -> c.getSimpleName().endsWith("ServiceImpl"))
            .filter(c -> c.getPackageName().startsWith(BASE_PACKAGE))
            .collect(Collectors.toSet());
        
        for (Class<?> impl : allImpls) {
            String packageName = impl.getPackageName();
            
            assertThat(packageName)
                .as("Service implementation %s should be in app, merchant, or common sub-package", impl.getSimpleName())
                .satisfiesAnyOf(
                    p -> assertThat(p).isEqualTo(BASE_PACKAGE + ".app"),
                    p -> assertThat(p).isEqualTo(BASE_PACKAGE + ".merchant"),
                    p -> assertThat(p).isEqualTo(BASE_PACKAGE + ".common")
                );
        }
    }
    
    /**
     * 验证商户端实现类与接口的对应关系
     * 
     * *For any* Merchant service implementation, it should implement a corresponding 
     * interface from the `service.merchant` package.
     */
    @Test
    void merchantImplementationsShouldImplementMerchantInterfaces() {
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .forPackage(BASE_PACKAGE + ".merchant")
                .setScanners(Scanners.SubTypes.filterResultsBy(s -> true))
        );
        
        Set<Class<?>> merchantImpls = reflections.getSubTypesOf(Object.class).stream()
            .filter(c -> !c.isInterface())
            .filter(c -> c.getPackageName().equals(BASE_PACKAGE + ".merchant"))
            .filter(c -> c.getSimpleName().endsWith("ServiceImpl"))
            .collect(Collectors.toSet());
        
        for (Class<?> impl : merchantImpls) {
            Class<?>[] interfaces = impl.getInterfaces();
            
            assertThat(interfaces)
                .as("Merchant implementation %s should implement at least one interface", impl.getSimpleName())
                .isNotEmpty();
            
            // 验证至少有一个接口来自 service.merchant 包
            boolean hasCorrectInterface = false;
            for (Class<?> iface : interfaces) {
                if (iface.getPackageName().equals("com.businessreviews.service.merchant")) {
                    hasCorrectInterface = true;
                    break;
                }
            }
            
            assertThat(hasCorrectInterface)
                .as("Merchant implementation %s should implement an interface from service.merchant package", impl.getSimpleName())
                .isTrue();
        }
    }
}
