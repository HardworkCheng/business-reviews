# Requirements Document

## Introduction

本文档定义了按照《阿里巴巴Java开发手册》规范对后端项目进行重构的需求。重构涵盖三个主要方面：项目分层结构优化、领域模型（POJO）拆分、以及命名与代码细节规范化。目标是使代码结构更清晰、职责更分明、命名更规范，提高代码的可维护性和可读性。

## Glossary

- **DO (Data Object)**: 数据对象，与数据库表结构一一对应的实体类
- **DTO (Data Transfer Object)**: 数据传输对象，用于Service层向外传输数据或Web层接收复杂表单
- **VO (View Object)**: 视图对象，专门用于Web层返回给前端展示，需要脱敏处理
- **Query**: 查询对象，封装超过2个参数的查询条件
- **Web层**: 控制器层，负责参数校验、异常捕获和业务转发
- **Service层**: 服务层，负责核心业务逻辑处理
- **DAO/Mapper层**: 数据访问层，负责与数据库交互
- **Manager层**: 通用业务处理层，用于处理第三方接口调用或复杂业务逻辑
- **Result<T>**: 统一响应结果包装类，包含code、message、data字段
- **POJO**: Plain Old Java Object，简单Java对象
- **SOA**: Service-Oriented Architecture，面向服务架构

## Requirements

### Requirement 1: 项目分层结构重构

**User Story:** 作为开发者，我希望项目按照阿里巴巴Java开发手册的分层规范进行组织，以便代码职责清晰、易于维护。

#### Acceptance Criteria

1. WHEN Controller类处理HTTP请求 THEN Web层 SHALL 仅执行参数校验、异常捕获和业务转发，禁止包含核心业务逻辑
2. WHEN Controller方法返回响应 THEN Web层 SHALL 使用统一的Result<T>对象包装返回值
3. WHEN 定义业务服务 THEN Service层 SHALL 遵循SOA理念，定义XxxService接口并将实现类放在impl子包下命名为XxxServiceImpl
4. WHEN Web层调用业务逻辑 THEN Web层 SHALL 仅依赖Service接口，禁止直接依赖实现类
5. WHEN Mapper层返回数据 THEN Mapper层 SHALL 返回DO对象（与数据库表一一对应的实体）
6. WHEN Service层存在第三方接口调用（如OSS、短信服务） THEN 系统 SHALL 将这部分逻辑剥离到Manager层

### Requirement 2: 领域模型（POJO）拆分

**User Story:** 作为开发者，我希望实体类按照DO/DTO/VO/Query进行拆分，以便数据在不同层之间传输时职责明确、安全可控。

#### Acceptance Criteria

1. WHEN 定义与数据库表对应的实体类 THEN 系统 SHALL 将其放在model.do包下并以DO结尾命名（如UserDO）
2. WHEN Service层向外传输数据或Web层接收复杂表单 THEN 系统 SHALL 使用DTO对象，放在model.dto包下并以DTO结尾命名
3. WHEN Web层返回数据给前端展示 THEN 系统 SHALL 使用VO对象，放在model.vo包下并以VO结尾命名，且敏感字段需脱敏处理
4. WHEN Controller接收的查询参数超过2个 THEN 系统 SHALL 封装成Query对象而非使用Map
5. WHEN 不同层之间需要对象转换 THEN 系统 SHALL 使用BeanUtils或MapStruct进行对象转换

### Requirement 3: 命名规范修正

**User Story:** 作为开发者，我希望方法命名遵循阿里巴巴Java开发手册规范，以便代码风格统一、语义清晰。

#### Acceptance Criteria

1. WHEN 方法用于获取单个对象 THEN 方法名 SHALL 使用get作为前缀
2. WHEN 方法用于获取多个对象 THEN 方法名 SHALL 使用list作为前缀
3. WHEN 方法用于统计数量 THEN 方法名 SHALL 使用count作为前缀
4. WHEN 方法用于插入数据 THEN 方法名 SHALL 使用save或insert作为前缀
5. WHEN 方法用于删除数据 THEN 方法名 SHALL 使用remove或delete作为前缀
6. WHEN 方法用于修改数据 THEN 方法名 SHALL 使用update作为前缀

### Requirement 4: Boolean字段规范

**User Story:** 作为开发者，我希望布尔类型字段命名规范，以避免序列化错误。

#### Acceptance Criteria

1. WHEN POJO类定义布尔类型变量 THEN 变量名 SHALL 禁止使用is前缀（如isDeleted应改为deleted）
2. WHEN 布尔字段需要表达状态 THEN 字段名 SHALL 使用形容词或过去分词形式（如deleted、enabled、active）

### Requirement 5: 数据类型规范

**User Story:** 作为开发者，我希望POJO类属性和方法返回值使用包装数据类型，以避免NPE风险和默认值问题。

#### Acceptance Criteria

1. WHEN POJO类定义属性 THEN 属性类型 SHALL 使用包装数据类型（如Integer而非int）
2. WHEN Service方法定义返回值 THEN 返回值类型 SHALL 使用包装数据类型（如Long而非long）
3. WHEN 方法参数涉及数值类型 THEN 参数类型 SHALL 使用包装数据类型以支持null值判断
