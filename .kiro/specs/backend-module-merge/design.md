# Design Document: Backend Module Merge

## Overview

本设计文档描述了将后端项目中的两个Web模块（`backend-business-reviews-mobile` 和 `backend-business-reviews-web`）合并为一个统一模块的技术方案。合并后，系统将使用单一的启动类 `BusinessReviewsApplication`，统一管理移动端和商家端的所有API。

### 当前架构

```
backend-business-reviews/
├── backend-business-reviews-mobile/     # 移动端模块（待删除）
│   ├── MobileBusinessReviewsApplication.java  # 移动端启动类
│   ├── controller/                      # 9个Controller
│   ├── config/                          # 6个配置类
│   ├── interceptor/                     # 1个拦截器
│   └── exception/                       # 1个异常处理器
│
├── backend-business-reviews-web/        # 商家端模块（合并目标）
│   ├── BusinessReviewsApplication.java  # 统一启动类（已存在）
│   ├── MerchantApplication.java         # 商家端启动类（待删除）
│   ├── controller/                      # 6个Controller
│   ├── merchant/controller/             # 7个商家Controller
│   ├── config/                          # 6个配置类
│   ├── merchant/config/                 # 1个商家配置类
│   ├── interceptor/                     # 1个拦截器
│   └── exception/                       # 1个异常处理器
```

### 目标架构

```
backend-business-reviews/
├── backend-business-reviews-web/        # 统一Web模块
│   ├── BusinessReviewsApplication.java  # 统一启动类
│   ├── controller/                      # 移动端Controller（合并后）
│   │   ├── AuthController.java
│   │   ├── CommentController.java
│   │   ├── CommonController.java
│   │   ├── CouponController.java        # 从mobile移入
│   │   ├── MessageController.java       # 从mobile移入
│   │   ├── NoteController.java
│   │   ├── ShopController.java
│   │   ├── UploadController.java        # 从mobile移入
│   │   └── UserController.java
│   ├── merchant/controller/             # 商家端Controller（保持不变）
│   ├── config/                          # 统一配置类
│   ├── merchant/config/                 # 商家端配置（保持不变）
│   ├── interceptor/                     # 移动端拦截器
│   ├── merchant/interceptor/            # 商家端拦截器
│   └── exception/                       # 统一异常处理器
```

## Architecture

### 模块合并策略

1. **启动类合并**：使用现有的 `BusinessReviewsApplication` 作为统一启动类，删除 `MobileBusinessReviewsApplication` 和 `MerchantApplication`

2. **Controller合并**：
   - 保留web模块现有的Controller
   - 将mobile模块特有的Controller（CouponController、MessageController、UploadController）移动到web模块

3. **配置类合并**：
   - 保留web模块现有的配置类（已包含移动端和商家端的配置）
   - 删除mobile模块的重复配置类

4. **拦截器合并**：
   - 保留web模块的 `AuthInterceptor` 和 `MerchantAuthInterceptor`
   - 删除mobile模块的重复拦截器

5. **配置文件合并**：
   - 合并两个 `application.yml` 文件
   - 统一使用端口8080

### API路径设计

| 端点类型 | 路径前缀 | 拦截器 |
|---------|---------|--------|
| 移动端API | `/api/*` | AuthInterceptor |
| 商家端API | `/api/merchant/*` | MerchantAuthInterceptor |

## Components and Interfaces

### 1. 统一启动类

```java
@SpringBootApplication
@MapperScan("com.businessreviews.mapper")
@EnableAsync
@EnableScheduling
public class BusinessReviewsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BusinessReviewsApplication.class, args);
    }
}
```

### 2. 需要移动的Controller

| Controller | 源位置 | 目标位置 | 说明 |
|-----------|--------|---------|------|
| CouponController | mobile/controller | web/controller | 优惠券功能 |
| MessageController | mobile/controller | web/controller | 私信功能 |
| UploadController | mobile/controller | web/controller | 文件上传 |

### 3. 配置类对比

| 配置类 | mobile模块 | web模块 | 合并策略 |
|-------|-----------|---------|---------|
| WebMvcConfig | 有 | 有（更完整） | 保留web版本 |
| CorsConfig | 有 | 有 | 保留web版本 |
| RedisConfig | 有 | 有 | 保留web版本 |
| AsyncConfig | 有 | 有 | 保留web版本 |
| MybatisPlusConfig | 有 | 有 | 保留web版本 |
| WebSocketConfig | MobileWebSocketConfig | WebSocketConfig | 保留web版本 |

### 4. 拦截器配置

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 商家端拦截器 - 只拦截 /merchant/** 路径
        registry.addInterceptor(merchantAuthInterceptor)
                .addPathPatterns("/merchant/**")
                .excludePathPatterns(...);
        
        // 移动端拦截器 - 拦截除商家端外的所有路径
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/merchant/**", ...);
    }
}
```

## Data Models

本次合并不涉及数据模型的变更，所有实体类保持在 `backend-business-reviews-entity` 模块中。

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: 组件加载完整性
*For any* Spring应用上下文，启动后应包含所有预期的Controller、Service和配置Bean
**Validates: Requirements 1.1, 3.3**

### Property 2: API路径保持不变
*For any* 移动端API请求路径，合并后应返回与合并前相同的响应结构
**Validates: Requirements 2.3**

### Property 3: 商家端API路径保持不变
*For any* 商家端API请求路径（/merchant/**），合并后应返回与合并前相同的响应结构
**Validates: Requirements 2.4**

### Property 4: 移动端认证拦截
*For any* 需要认证的移动端API请求，AuthInterceptor应正确验证Token并设置用户上下文
**Validates: Requirements 4.1, 4.4**

### Property 5: 商家端认证拦截
*For any* 需要认证的商家端API请求（/merchant/**），MerchantAuthInterceptor应正确验证Token
**Validates: Requirements 4.2, 4.5**

### Property 6: JWT双端支持
*For any* 有效的JWT Token，系统应能正确解析并验证，无论是移动端还是商家端生成的Token
**Validates: Requirements 6.3**

## Error Handling

### 异常处理策略

使用统一的 `GlobalExceptionHandler` 处理所有异常：

1. **BusinessException**: 业务异常，返回自定义错误码和消息
2. **MethodArgumentNotValidException**: 参数校验异常，返回400
3. **HttpRequestMethodNotSupportedException**: 方法不支持，返回405
4. **MaxUploadSizeExceededException**: 文件大小超限，返回400
5. **Exception**: 其他异常，返回500

### 认证失败处理

- 移动端：返回 `{"code":401,"message":"未登录","data":null}`
- 商家端：返回 `{"code":401,"message":"未登录或登录已过期","data":null}`

## Testing Strategy

### 单元测试

1. **启动类测试**：验证启动类包含正确的注解
2. **配置类测试**：验证配置类正确加载
3. **拦截器测试**：验证拦截器正确配置路径

### 属性测试

使用 JUnit 5 + AssertJ 进行属性测试：

1. **组件加载测试**：验证所有Bean正确加载
2. **API路径测试**：验证API路径保持不变
3. **认证测试**：验证Token验证逻辑

### 集成测试

1. **编译测试**：验证项目能够成功编译
2. **启动测试**：验证应用能够成功启动
3. **API测试**：验证所有API端点正常工作

### 测试框架

- JUnit 5
- Spring Boot Test
- MockMvc
- AssertJ

