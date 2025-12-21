# Requirements Document

## Introduction

本规范文档定义了基于阿里巴巴Java开发手册对 business-reviews 后端项目进行包结构重构的需求。项目是一个 Spring Boot 单体应用，同时服务于两个前端：商户运营中心（Web）和用户客户端（UniApp/Mobile）。重构目标是清晰分离商户端和用户端的业务逻辑，同时保持公共逻辑的共享，并清理历史遗留的冗余代码。

## Glossary

- **System**: business-reviews 后端系统
- **Controller Layer (web)**: 负责处理 HTTP 请求的控制器层
- **Service Layer (service)**: 负责业务逻辑处理的服务层
- **DAO Layer (mapper)**: 负责数据访问的持久层
- **Manager Layer (manager)**: 负责封装第三方服务调用的管理层
- **DO (Data Object)**: 数据库实体对象，与数据库表一一对应
- **DTO (Data Transfer Object)**: 数据传输对象，用于层间数据传递
- **VO (View Object)**: 视图对象，用于返回给前端的数据
- **Query**: 查询参数对象，封装查询条件
- **Merchant**: 商户端，指商户运营中心 Web 前端
- **App/Mobile**: 用户端，指 UniApp 移动端前端
- **refactored package**: 历史重构过程中产生的临时包，包含 V2 版本的服务接口

## Requirements

### Requirement 1

**User Story:** As a developer, I want the Controller layer to be clearly separated into merchant and app packages, so that I can easily locate and maintain endpoint code for different clients.

#### Acceptance Criteria

1. WHEN the system organizes Controller classes THEN the System SHALL place all user/mobile-facing controllers under `web.app` package
2. WHEN the system organizes Controller classes THEN the System SHALL place all merchant-facing controllers under `web.merchant` package
3. WHEN existing root-level controllers are found in `controller` package THEN the System SHALL move them to `web.app` package
4. WHEN controllers are reorganized THEN the System SHALL update all import statements and Spring component scan configurations

### Requirement 2

**User Story:** As a developer, I want the Service layer interfaces to be organized by client type, so that I can clearly understand which services belong to which client.

#### Acceptance Criteria

1. WHEN the system organizes Service interfaces THEN the System SHALL place merchant-specific interfaces under `service.merchant` package
2. WHEN the system organizes Service interfaces THEN the System SHALL place app/mobile-specific interfaces under `service.app` package
3. WHEN the system organizes Service interfaces THEN the System SHALL place shared/common interfaces under `service.common` package
4. WHEN duplicate interfaces exist at root level and sub-packages THEN the System SHALL remove root-level duplicates and keep only sub-package versions

### Requirement 3

**User Story:** As a developer, I want all Service implementations to follow a consistent package structure, so that I can easily find implementation classes.

#### Acceptance Criteria

1. WHEN the system organizes Service implementations THEN the System SHALL place merchant implementations under `service.impl.merchant` package
2. WHEN the system organizes Service implementations THEN the System SHALL place app/mobile implementations under `service.impl.app` package
3. WHEN the system organizes Service implementations THEN the System SHALL place common implementations under `service.impl.common` package
4. WHEN implementations exist directly under `service.impl` THEN the System SHALL move them to appropriate sub-packages based on their interface location

### Requirement 4

**User Story:** As a developer, I want the refactored package to be removed and its contents merged into the main service structure, so that there is no confusion about which version to use.

#### Acceptance Criteria

1. WHEN the `service.refactored` package exists THEN the System SHALL evaluate each V2 service against its original version
2. WHEN a V2 service is superior to the original THEN the System SHALL replace the original with the V2 version
3. WHEN a V2 service is merged THEN the System SHALL remove the V2 suffix from class names
4. WHEN all V2 services are processed THEN the System SHALL delete the `service.refactored` package entirely
5. WHEN V2 services are merged THEN the System SHALL update all references to use the new class names

### Requirement 5

**User Story:** As a developer, I want all Mapper interfaces to be in the mapper package root without sub-packages, so that the DAO layer structure is clean and consistent.

#### Acceptance Criteria

1. WHEN Mapper interfaces exist in sub-packages like `mapper.dataobject` THEN the System SHALL move them to `mapper` package root
2. WHEN duplicate Mapper interfaces exist THEN the System SHALL keep only one version with proper DO suffix handling
3. WHEN Mapper interfaces are moved THEN the System SHALL update all import statements and MyBatis configurations

### Requirement 6

**User Story:** As a developer, I want all DO classes to be in the entity module only, so that the mapper module contains only interfaces.

#### Acceptance Criteria

1. WHEN DO classes exist in `backend-business-reviews-mapper` module THEN the System SHALL move them to `backend-business-reviews-entity` module
2. WHEN DO classes are moved THEN the System SHALL place them under `model.dataobject` package
3. WHEN DO classes are moved THEN the System SHALL update all import statements across all modules

### Requirement 7

**User Story:** As a developer, I want DTO and VO classes to be organized by client type, so that I can easily find the correct transfer objects for each client.

#### Acceptance Criteria

1. WHEN the system organizes DTO classes THEN the System SHALL place merchant-specific DTOs under `model.dto.merchant` package
2. WHEN the system organizes DTO classes THEN the System SHALL place app/mobile-specific DTOs under `model.dto.app` package
3. WHEN the system organizes VO classes THEN the System SHALL place merchant-specific VOs under `model.vo.merchant` package
4. WHEN the system organizes VO classes THEN the System SHALL place app/mobile-specific VOs under `model.vo.app` package
5. WHEN shared DTOs or VOs exist THEN the System SHALL keep them in the root `model.dto` or `model.vo` package

### Requirement 8

**User Story:** As a developer, I want all POJO classes to follow Alibaba naming conventions, so that the codebase is consistent and professional.

#### Acceptance Criteria

1. WHEN a class represents a database entity THEN the System SHALL ensure its name ends with `DO`
2. WHEN a class represents a data transfer object THEN the System SHALL ensure its name ends with `DTO`
3. WHEN a class represents a view object THEN the System SHALL ensure its name ends with `VO`
4. WHEN a Service interface is defined THEN the System SHALL ensure its name follows `XxxService` pattern
5. WHEN a Service implementation is defined THEN the System SHALL ensure its name follows `XxxServiceImpl` pattern

### Requirement 9

**User Story:** As a developer, I want the Manager layer to properly encapsulate third-party service calls, so that external dependencies are isolated.

#### Acceptance Criteria

1. WHEN third-party service calls exist THEN the System SHALL encapsulate them in Manager classes under `manager` package
2. WHEN OssManager exists THEN the System SHALL ensure it handles all OSS-related operations
3. WHEN SmsManager exists THEN the System SHALL ensure it handles all SMS-related operations
4. WHEN Manager classes are used THEN the System SHALL inject them into Service layer only

### Requirement 10

**User Story:** As a developer, I want the project to compile and run successfully after refactoring, so that the refactoring does not break existing functionality.

#### Acceptance Criteria

1. WHEN all refactoring is complete THEN the System SHALL pass Maven compilation without errors
2. WHEN all refactoring is complete THEN the System SHALL pass all existing unit tests
3. WHEN all refactoring is complete THEN the System SHALL maintain all existing API endpoints with same request/response contracts
4. WHEN Spring context loads THEN the System SHALL successfully wire all beans without circular dependency errors
