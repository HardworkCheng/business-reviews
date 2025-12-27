# 美食点评系统后端项目学习指南

这份文档旨在帮助你快速理解、运行并深入学习 `backend-business-reviews` 项目。即使你是刚接触该项目，通过本指南也能建立起完整的知识体系。

## 1. 项目简介

这是一个基于 **Spring Boot 3.2** 的美食点评系统后端 API 项目。它采用了典型的 **Maven 多模块分层架构**，旨在提供高性能、可维护的后端服务。

项目主要包含两大部分 API：
*   **移动端 API (`mobile`)**：服务于 C 端用户（UniApp），提供用户浏览、点评、收藏等功能。
*   **商家端 API (`web`)**：服务于 B 端商家（Web），提供店铺管理、数据看板、优惠券管理等功能。

---

## 2. 技术栈概览

在开始之前，了解项目所使用的技术是非常重要的：

*   **核心框架**: `Spring Boot 3.2.0` (最新这一代 Spring Boot，原生支持 JDK 17+)
*   **编程语言**: `Java 17` (使用了 Records, Switch Expressions 等新特性)
*   **数据访问层 (ORM)**: `MyBatis-Plus 3.5.5` (简化了 MyBatis 的开发，无需编写大量 XML)
*   **数据库**: `MySQL 8.0`
*   **缓存**: `Redis` (用于缓存热点数据、Token 存储等)
*   **API 鉴权**: `JWT (JSON Web Token)` + `HandlerInterceptor` (拦截器)
*   **数据库连接池**: `Druid`
*   **工具库**: `Hutool` (国产 Java 工具包，简化了大量代码)
*   **云服务集成**:
    *   阿里云 OSS (对象存储，用于图片上传)
    *   阿里云 SMS (短信服务，用于验证码)

---

## 3. 项目目录结构解析 (关键)

本项目采用了 **Maven 多模块** 结构，这种结构在企业级开发中非常常见，目的是实现 **关注点分离 (Separation of Concerns)**。

```text
backend-business-reviews/
├── pom.xml                               # 父工程配置文件，管理所有依赖版本
├── backend-business-reviews-common/      # [基础设施层]
│   └── Result, Constants, Utils          # 放通用的工具类、统一响应结果 Result、常量定义
├── backend-business-reviews-entity/      # [领域模型层]
│   └── Entity, DTO, VO                   # 放数据库实体类(POJO)、数据传输对象(DTO)
├── backend-business-reviews-mapper/      # [数据访问层]
│   └── Mapper Interfaces                 # 放 MyBatis 的 Mapper 接口 (DAO)
├── backend-business-reviews-service/     # [业务逻辑层]
│   └── Service Interfaces & Impl         # 核心业务逻辑，处理复杂的业务规则
├── backend-business-reviews-web/         # [接入层/启动层]
│   ├── controller/                       # Controller 接口入口
│   ├── interceptor/                      # 拦截器 (处理登录校验)
│   ├── exception/                        # 全局异常处理
│   └── BusinessReviewsApplication.java   # Spring Boot 启动类
└── docs/                                 # 项目文档目录
```

### 为什么要这样分层？
*   **Entity 独立**: 可以在不同模块间共享数据模型，而不会产生循环依赖。
*   **Common 复用**: 工具类可以在 Service 和 Web 层通用。
*   **Service 纯粹**: 业务逻辑不依赖于 HTTP (Web) 细节，方便后续可能的微服务拆分或单元测试。

---

## 4. 快速启动 (Getting Started)

### 环境准备
1.  **JDK**: 确保安装了 `Java 17` 或更高版本。
2.  **Maven**: 确保安装了 `Maven 3.6+`。
3.  **MySQL**: 准备好 MySQL 数据库服务。
4.  **Redis**: 准备好 Redis 服务。

### 步骤
1.  **导入数据库**:
    *   找到 `sql/` 目录下的 SQL 脚本，在你的 MySQL 数据库中执行，建立表结构和初始数据。
2.  **配置环境**:
    *   打开 `backend-business-reviews-web/src/main/resources/application.yml`。
    *   **修改数据库配置**: 修改 `spring.datasource.url`, `username`, `password` 为你本地的配置。
    *   **修改 Redis 配置**: 修改 `spring.data.redis` 相关配置。
    *   *(可选)* 如果需要测试上传/短信，配置阿里云相关的 Key。
3.  **启动项目**:
    *   找到启动类: `com.businessreviews.BusinessReviewsApplication` (位于 `backend-business-reviews-web` 模块)。
    *   运行 `main` 方法。
4.  **验证**:
    *   项目启动后，观察控制台日志，默认端口通常是 **8080**。
    *   你可以尝试访问 Swagger UI (如果集成了) 或者直接用 Postman 测试接口。

---

## 5. 核心机制解析 (学习重点)

### 5.1 请求处理流程
一个典型的请求 (例如 "查询店铺详情") 是这样流转的：

1.  **Request**: 用户发起 HTTP GET 请求 `/api/shop/1`.
2.  **Interceptor (`Web 层`)**: `AuthInterceptor` 拦截请求，检查 Header 中的 `Authorization` (Token) 是否合法。
    *   *位置*: `backend-business-reviews-web/.../interceptor/AuthInterceptor.java`
3.  **Controller (`Web 层`)**: `ShopController` 接收请求，解析参数。
    *   *位置*: `backend-business-reviews-web/.../controller/ShopController.java`
4.  **Service (`Service 层`)**: `shopService.getById(1)` 执行业务逻辑 (比如查缓存、组装数据)。
    *   *位置*: `backend-business-reviews-service/.../impl/ShopServiceImpl.java`
5.  **Mapper (`Mapper 层`)**: `shopMapper.selectById(1)` 生成 SQL 并查询数据库。
    *   *位置*: `backend-business-reviews-mapper/.../ShopMapper.java`
6.  **Response**: 数据原路返回，最终封装成 `Result<Shop>` JSON 格式返回给前端。

### 5.2 统一响应体 (Result)
所有的接口都返回统一的 JSON 格式，这是前后端分离的标准实践。
*   **类名**: `com.businessreviews.common.Result`
*   **位置**: `backend-business-reviews-common` 模块
*   **结构**: `{ code: 200, msg: "success", data: ... }`

### 5.3 全局异常处理
项目使用了 `@RestControllerAdvice` 进行全局异常捕获，不需要在每个 Controller 里写 try-catch。
*   **类名**: `GlobalExceptionHandler`
*   **位置**: `backend-business-reviews-web` 模型
*   它会捕获所有异常，并将其转换为标准的 `Result` 错误响应 (例如 code: 500, msg: "系统内部错误")。

---

## 6. 学习与开发建议 (Roadmap)

如果你想彻底掌握这个项目，建议按照以下顺序进行：

1.  **Level 1: 跑通流程**
    *   成功启动项目。
    *   使用 Postman/Apifox 调用 `Login` 接口获取 Token。
    *   携带 Token 调用一个业务接口 (如查询个人信息)，观察控制台 SQL 日志。

2.  **Level 2: 阅读代码**
    *   **实体类**: 去 `entity` 模块看看数据库表对应的 Java 类，了解数据结构。
    *   **Mapper**: 看看 `mapper` 模块，了解 MyBatis-Plus 的用法 (它继承了 `BaseMapper`，所以很多基础 SQL 不用写)。
    *   **Service**: 重点看 `service` 模块，这里是业务逻辑的核心。比如 "发布笔记" 功能，它是如何处理图片、内容审核(如果有)、保存数据的。

3.  **Level 3: 动手修改**
    *   **任务**: 尝试给用户表增加一个 "生日 (birthday)" 字段。
    *   **步骤**:
        1.  修改数据库表结构。
        2.  修改 `User` 实体类。
        3.  修改 `UserDTO` (如果是更新接口)。
        4.  修改 `UserService` 逻辑。
        5.  修改 `UserController` 接口。
        6.  测试。

## 7. 文档索引

`docs/` 目录下还有许多具体的文档，建议在需要时查阅：

*   **架构设计**: 查看 `ARCHITECTURE.md` (项目根目录)
*   **接口定义**: 查看 `docs/后端接口文档.md`
*   **数据库**: 查看 `docs/数据库设计.md`
*   **特定功能**:
    *   私信系统: `docs/PRIVATE_MESSAGE_*.md` 系列
    *   OSS 配置: `docs/阿里云OSS配置指南.md`

祝你学习愉快！
