# Requirements Document

## Introduction

本需求文档描述了将后端项目中的两个Web模块（`backend-business-reviews-mobile` 和 `backend-business-reviews-web`）合并为一个统一模块的功能需求。目前系统存在两个独立的启动类：`MobileBusinessReviewsApplication`（移动端UniApp后端）和 `MerchantApplication`（Web端商家运营中心），需要将它们合并为一个统一的启动类，并将两个模块的所有内容整合到 `backend-business-reviews-web` 模块中。

## Glossary

- **MobileBusinessReviewsApplication**: 移动端UniApp后端的Spring Boot启动类，位于 `backend-business-reviews-mobile` 模块
- **MerchantApplication**: Web端商家运营中心的Spring Boot启动类，位于 `backend-business-reviews-web/merchant` 包下
- **BusinessReviewsApplication**: 合并后的统一启动类，位于 `backend-business-reviews-web` 模块
- **backend-business-reviews-mobile**: 当前的移动端Web控制层模块
- **backend-business-reviews-web**: 当前的商家运营中心Web控制层模块，合并后将成为统一的Web模块
- **Controller**: Spring MVC控制器，处理HTTP请求
- **Interceptor**: Spring拦截器，用于请求预处理和后处理
- **Config**: Spring配置类，用于配置Bean和应用设置

## Requirements

### Requirement 1

**User Story:** 作为开发者，我希望将两个启动类合并为一个统一的启动类，以便简化项目结构和部署流程。

#### Acceptance Criteria

1. WHEN 系统启动时 THEN BusinessReviewsApplication SHALL 同时加载移动端和商家端的所有Controller、Service和配置
2. WHEN 合并启动类后 THEN BusinessReviewsApplication SHALL 包含 @EnableAsync 和 @EnableScheduling 注解以支持异步和定时任务
3. WHEN 合并完成后 THEN 系统 SHALL 删除 MobileBusinessReviewsApplication 和 MerchantApplication 两个原始启动类
4. WHEN 合并完成后 THEN BusinessReviewsApplication SHALL 使用 @MapperScan 扫描 com.businessreviews.mapper 包

### Requirement 2

**User Story:** 作为开发者，我希望将移动端模块的Controller合并到Web模块中，以便统一管理所有API端点。

#### Acceptance Criteria

1. WHEN 合并Controller后 THEN 系统 SHALL 将移动端特有的Controller（CouponController、MessageController、UploadController）移动到 backend-business-reviews-web 模块的 controller 包下
2. WHEN 存在同名Controller时 THEN 系统 SHALL 合并两个Controller的功能或使用不同的包路径区分
3. WHEN 合并完成后 THEN 移动端API路径 /api/... SHALL 保持不变
4. WHEN 合并完成后 THEN 商家端API路径 /api/merchant/... SHALL 保持不变

### Requirement 3

**User Story:** 作为开发者，我希望将移动端模块的配置类合并到Web模块中，以便统一管理所有配置。

#### Acceptance Criteria

1. WHEN 合并配置类后 THEN 系统 SHALL 将移动端的 MobileWebSocketConfig 移动到 backend-business-reviews-web 模块
2. WHEN 存在同名配置类时 THEN 系统 SHALL 合并配置内容或重命名以避免冲突
3. WHEN 合并完成后 THEN 所有配置类 SHALL 能够正常加载并生效
4. WHEN 合并完成后 THEN WebSocket配置 SHALL 同时支持移动端和商家端的WebSocket连接

### Requirement 4

**User Story:** 作为开发者，我希望将移动端模块的拦截器合并到Web模块中，以便统一管理请求拦截逻辑。

#### Acceptance Criteria

1. WHEN 合并拦截器后 THEN 系统 SHALL 保留移动端的 AuthInterceptor 用于移动端API认证
2. WHEN 合并拦截器后 THEN 系统 SHALL 保留商家端的 MerchantAuthInterceptor 用于商家端API认证
3. WHEN 合并完成后 THEN WebMvcConfig SHALL 正确配置两种拦截器的拦截路径
4. WHEN 移动端API请求到达时 THEN 系统 SHALL 使用移动端 AuthInterceptor 进行认证
5. WHEN 商家端API请求到达时 THEN 系统 SHALL 使用 MerchantAuthInterceptor 进行认证

### Requirement 5

**User Story:** 作为开发者，我希望合并两个模块的pom.xml依赖配置，以便统一管理项目依赖。

#### Acceptance Criteria

1. WHEN 合并pom.xml后 THEN backend-business-reviews-web 模块 SHALL 包含移动端模块的所有依赖（包括WebSocket依赖）
2. WHEN 合并完成后 THEN 父pom.xml SHALL 移除 backend-business-reviews-mobile 模块的声明
3. WHEN 合并完成后 THEN backend-business-reviews-web 模块的pom.xml SHALL 能够成功编译

### Requirement 6

**User Story:** 作为开发者，我希望合并两个模块的application.yml配置文件，以便统一管理应用配置。

#### Acceptance Criteria

1. WHEN 合并配置文件后 THEN application.yml SHALL 包含移动端和商家端的所有配置项
2. WHEN 合并完成后 THEN 服务器端口 SHALL 统一为一个端口（8080）
3. WHEN 合并完成后 THEN JWT配置 SHALL 同时支持移动端和商家端的Token验证
4. WHEN 合并完成后 THEN SMS短信配置 SHALL 保留移动端的短信发送功能配置

### Requirement 7

**User Story:** 作为开发者，我希望合并完成后删除移动端模块目录，以便保持项目结构整洁。

#### Acceptance Criteria

1. WHEN 所有内容合并完成后 THEN 系统 SHALL 删除 backend-business-reviews-mobile 整个目录
2. WHEN 删除模块后 THEN 项目 SHALL 能够正常编译和运行
3. WHEN 删除模块后 THEN 所有原有功能 SHALL 保持正常工作

