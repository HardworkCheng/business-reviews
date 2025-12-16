# Requirements Document

## Introduction

本需求文档描述了对 backend-business-reviews 后端项目进行代码分层重构的需求。目标是严格按照 MVC 架构模式进行分层，同时清晰区分移动端（UniApp）后端和 Web 端（商家运营中心）后端代码，使代码结构更加清晰易懂。

## 当前项目结构分析

### 现有模块
- **backend-business-reviews-common**: 公共模块（配置、工具类、常量、枚举、异常处理）
- **backend-business-reviews-entity**: 实体模块（Entity、DTO）
- **backend-business-reviews-mapper**: 数据访问层（MyBatis Mapper）
- **backend-business-reviews-service**: 业务逻辑层（Service接口和实现）
- **backend-business-reviews-mobile**: 移动端控制器层（UniApp后端API）
- **backend-business-reviews-web**: Web端控制器层（商家运营中心后端API）

### 当前问题
1. Service层混合了Mobile和Web的业务逻辑，不够清晰
2. DTO没有按照端进行区分
3. 缺少清晰的模块职责说明文档

## Glossary

- **MVC**: Model-View-Controller，模型-视图-控制器架构模式
- **Mobile端**: 面向UniApp移动应用的后端API
- **Web端**: 面向商家运营中心网页的后端API
- **Entity**: 数据库实体类
- **DTO**: Data Transfer Object，数据传输对象
- **Mapper**: MyBatis数据访问接口
- **Service**: 业务逻辑服务层
- **Controller**: 控制器层，处理HTTP请求

## Requirements

### Requirement 1

**User Story:** 作为开发者，我希望Service层代码按照端进行清晰分组，以便我能快速定位Mobile端和Web端的业务逻辑代码。

#### Acceptance Criteria

1. WHEN 开发者查看Service模块 THEN 系统 SHALL 展示清晰的包结构区分Mobile端服务和Web端服务
2. WHEN 开发者需要修改Mobile端业务逻辑 THEN 系统 SHALL 提供独立的mobile包下的Service类
3. WHEN 开发者需要修改Web端业务逻辑 THEN 系统 SHALL 提供独立的merchant包下的Service类
4. WHEN 存在共享业务逻辑 THEN 系统 SHALL 将其放置在common包下供两端复用

### Requirement 2

**User Story:** 作为开发者，我希望DTO按照端和用途进行分组，以便我能清晰理解每个DTO的使用场景。

#### Acceptance Criteria

1. WHEN 开发者查看DTO包结构 THEN 系统 SHALL 展示按端分组的request和response子包
2. WHEN 开发者需要Mobile端的请求DTO THEN 系统 SHALL 在dto/mobile/request包下提供
3. WHEN 开发者需要Web端的请求DTO THEN 系统 SHALL 在dto/merchant/request包下提供
4. WHEN 存在两端共用的DTO THEN 系统 SHALL 在dto/common包下提供

### Requirement 3

**User Story:** 作为开发者，我希望每个模块都有清晰的README文档，以便我能快速了解模块职责和代码组织方式。

#### Acceptance Criteria

1. WHEN 开发者进入任意模块目录 THEN 系统 SHALL 提供README.md说明模块职责
2. WHEN 开发者阅读README THEN 系统 SHALL 清晰说明该模块的包结构和各包用途
3. WHEN 开发者需要了解Mobile端和Web端的区别 THEN 系统 SHALL 在文档中明确标注

### Requirement 4

**User Story:** 作为开发者，我希望Controller层保持现有的端分离结构，同时增加清晰的注释说明。

#### Acceptance Criteria

1. WHEN 开发者查看mobile模块 THEN 系统 SHALL 展示所有UniApp移动端的Controller
2. WHEN 开发者查看web模块 THEN 系统 SHALL 展示所有商家运营中心的Controller
3. WHEN 开发者阅读Controller代码 THEN 系统 SHALL 看到清晰的类级别注释说明API用途

### Requirement 5

**User Story:** 作为开发者，我希望Mapper层保持统一，因为数据访问逻辑是共享的。

#### Acceptance Criteria

1. WHEN 开发者查看Mapper模块 THEN 系统 SHALL 展示统一的数据访问接口
2. WHEN Mobile端和Web端需要相同的数据操作 THEN 系统 SHALL 复用同一个Mapper接口
3. WHEN 需要特定端的复杂查询 THEN 系统 SHALL 在Mapper中添加带端标识的方法名

### Requirement 6

**User Story:** 作为开发者，我希望项目根目录有完整的架构说明文档，以便新成员快速理解项目结构。

#### Acceptance Criteria

1. WHEN 开发者查看项目根目录 THEN 系统 SHALL 提供ARCHITECTURE.md架构说明文档
2. WHEN 开发者阅读架构文档 THEN 系统 SHALL 看到完整的模块依赖关系图
3. WHEN 开发者需要了解代码分层 THEN 系统 SHALL 在文档中展示MVC分层结构图
4. WHEN 开发者需要区分端 THEN 系统 SHALL 在文档中用表格列出Mobile端和Web端的对应关系
