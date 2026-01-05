# Requirements Document

## Introduction

本文档定义了按照《阿里巴巴Java开发手册》规范对后端项目进行彻底重构的需求。重构的核心目标是将现有的 `entity` 包和 `dto` 包下的类迁移到符合规范的 `model.do`、`model.dto`、`model.vo` 包结构中，并更新全项目的引用。重构过程中严格保持业务逻辑不变，仅进行结构调整和重命名。

## Glossary

- **DO (Data Object)**: 数据对象，与数据库表结构一一对应的实体类，位于 `model.do` 包（由于 Java 保留字限制，实际使用 `model.dataobject`）
- **DTO (Data Transfer Object)**: 数据传输对象，用于接收前端请求参数，位于 `model.dto` 包
- **VO (View Object)**: 视图对象，用于返回给前端展示的数据，位于 `model.vo` 包
- **entity 包**: 当前存放数据库实体类的包，需要迁移到 `model.do`
- **dto.request 包**: 当前存放请求参数类的包，需要迁移到 `model.dto`
- **dto.response 包**: 当前存放响应类的包，需要迁移到 `model.vo`
- **merchant 子包**: 商家端相关的类，迁移时保留子包结构
- **mobile 子包**: 移动端相关的类，迁移时保留子包结构
- **@JsonProperty 注解**: Jackson 注解，用于指定 JSON 序列化时的字段名

## Requirements

### Requirement 1: Entity 类迁移为 DO 类

**User Story:** 作为开发者，我希望将 entity 包下的实体类迁移到 model.dataobject 包并重命名为 DO 结尾，以便符合阿里巴巴规范。

#### Acceptance Criteria

1. WHEN 迁移 entity 包下的实体类 THEN 系统 SHALL 将类移动到 `com.businessreviews.model.dataobject` 包下
2. WHEN 重命名实体类 THEN 系统 SHALL 将类名添加 DO 后缀（例如 User -> UserDO）
3. WHEN 实体类包含 Boolean 字段以 is 开头 THEN 系统 SHALL 将字段名去除 is 前缀（例如 isDeleted -> deleted）
4. WHEN Boolean 字段被重命名 THEN 系统 SHALL 添加 @JsonProperty 注解保持 JSON 序列化字段名不变
5. WHEN 迁移完成 THEN 系统 SHALL 更新所有 Mapper、Service、Controller 层对该类的引用

### Requirement 2: Request 类迁移为 DTO 类

**User Story:** 作为开发者，我希望将 dto.request 包下的请求类迁移到 model.dto 包并重命名为 DTO 结尾，以便符合阿里巴巴规范。

#### Acceptance Criteria

1. WHEN 迁移 dto.request 包下的请求类 THEN 系统 SHALL 将类移动到 `com.businessreviews.model.dto` 包下
2. WHEN 迁移 dto.merchant.request 包下的类 THEN 系统 SHALL 将类移动到 `com.businessreviews.model.dto.merchant` 包下
3. WHEN 迁移 dto.mobile.request 包下的类 THEN 系统 SHALL 将类移动到 `com.businessreviews.model.dto.mobile` 包下
4. WHEN 重命名请求类 THEN 系统 SHALL 将类名中的 Request 替换为 DTO（例如 LoginByCodeRequest -> LoginByCodeDTO）
5. WHEN 迁移完成 THEN 系统 SHALL 更新所有 Controller 层对该类的引用

### Requirement 3: Response 类迁移为 VO 类

**User Story:** 作为开发者，我希望将 dto.response 包下的响应类迁移到 model.vo 包并重命名为 VO 结尾，以便符合阿里巴巴规范。

#### Acceptance Criteria

1. WHEN 迁移 dto.response 包下的响应类 THEN 系统 SHALL 将类移动到 `com.businessreviews.model.vo` 包下
2. WHEN 迁移 dto.merchant.response 包下的类 THEN 系统 SHALL 将类移动到 `com.businessreviews.model.vo.merchant` 包下
3. WHEN 迁移 dto.mobile.response 包下的类 THEN 系统 SHALL 将类移动到 `com.businessreviews.model.vo.mobile` 包下
4. WHEN 重命名响应类 THEN 系统 SHALL 将类名中的 Response 替换为 VO（例如 UserInfoResponse -> UserInfoVO）
5. WHEN 迁移完成 THEN 系统 SHALL 更新所有 Service、Controller 层对该类的引用

### Requirement 4: 全局引用更新

**User Story:** 作为开发者，我希望所有对迁移类的引用都被正确更新，以便项目能够正常编译和运行。

#### Acceptance Criteria

1. WHEN 类被迁移和重命名 THEN 系统 SHALL 更新所有 import 语句为新的包路径和类名
2. WHEN 类被迁移和重命名 THEN 系统 SHALL 更新所有代码中对该类的使用（变量声明、方法参数、返回类型等）
3. WHEN 更新完成 THEN 系统 SHALL 确保项目能够正常编译通过
4. WHEN 更新完成 THEN 系统 SHALL 确保 API 接口的请求和响应格式保持不变

### Requirement 5: Boolean 字段规范化

**User Story:** 作为开发者，我希望所有 POJO 类中的 Boolean 字段命名符合规范，以避免序列化问题。

#### Acceptance Criteria

1. WHEN DO、DTO、VO 类中存在以 is 开头的 Boolean 字段 THEN 系统 SHALL 将字段名去除 is 前缀
2. WHEN Boolean 字段被重命名 THEN 系统 SHALL 添加 @JsonProperty("isXxx") 注解保持 JSON 字段名不变
3. WHEN 字段被重命名 THEN 系统 SHALL 更新所有对该字段的 getter/setter 调用

### Requirement 6: 业务逻辑保持不变

**User Story:** 作为开发者，我希望重构过程中业务逻辑完全保持不变，以确保系统功能正常。

#### Acceptance Criteria

1. WHEN 进行重构 THEN 系统 SHALL 仅进行结构调整和重命名，禁止修改任何业务逻辑代码
2. WHEN 进行重构 THEN 系统 SHALL 保持所有 API 接口的路径、参数、返回值格式不变
3. WHEN 进行重构 THEN 系统 SHALL 保持数据库操作逻辑不变
