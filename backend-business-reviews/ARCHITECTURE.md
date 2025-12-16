# 后端架构说明文档

## 项目概述

美食点评系统后端API，采用Spring Boot 3.2 + MyBatis-Plus技术栈，严格遵循MVC分层架构。

## 模块依赖关系图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        应用层 (Application Layer)                    │
│  ┌─────────────────────────┐    ┌─────────────────────────────────┐ │
│  │  backend-business-      │    │  backend-business-              │ │
│  │  reviews-mobile         │    │  reviews-web                    │ │
│  │  (UniApp移动端API)       │    │  (商家运营中心API)               │ │
│  │  端口: 8080             │    │  端口: 8081                     │ │
│  └───────────┬─────────────┘    └───────────────┬─────────────────┘ │
└──────────────┼──────────────────────────────────┼───────────────────┘
               │                                  │
               ▼                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        业务层 (Service Layer)                        │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │              backend-business-reviews-service                   ││
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐ ││
│  │  │   mobile/   │  │  merchant/  │  │        common/          │ ││
│  │  │ (移动端服务) │  │ (商家端服务) │  │      (共用服务)          │ ││
│  │  └─────────────┘  └─────────────┘  └─────────────────────────┘ ││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        数据层 (Data Layer)                           │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │              backend-business-reviews-mapper                    ││
│  │                    (MyBatis Mapper接口)                          ││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        模型层 (Model Layer)                          │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │              backend-business-reviews-entity                    ││
│  │  ┌─────────────┐  ┌─────────────────────────────────────────┐  ││
│  │  │   entity/   │  │                 dto/                    │  ││
│  │  │ (数据库实体) │  │  ┌─────────┐ ┌─────────┐ ┌──────────┐  │  ││
│  │  │             │  │  │ mobile/ │ │merchant/│ │ common/  │  │  ││
│  │  └─────────────┘  │  └─────────┘ └─────────┘ └──────────┘  │  ││
│  │                   └─────────────────────────────────────────┘  ││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       基础设施层 (Infrastructure Layer)              │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │              backend-business-reviews-common                    ││
│  │  (配置、工具类、常量、枚举、异常处理、WebSocket)                    ││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
```

## MVC分层结构

| 层级 | 模块 | 职责 | 包路径 |
|------|------|------|--------|
| **Controller** | mobile | 处理UniApp移动端HTTP请求 | `com.businessreviews.controller` |
| **Controller** | web | 处理商家运营中心HTTP请求 | `com.businessreviews.merchant.controller` |
| **Service** | service | 业务逻辑处理 | `com.businessreviews.service` |
| **Mapper** | mapper | 数据库访问 | `com.businessreviews.mapper` |
| **Model** | entity | 数据模型定义 | `com.businessreviews.entity` / `dto` |
| **Common** | common | 公共基础设施 | `com.businessreviews.*` |

## Mobile端与Web端对应关系

### Controller层对应关系

| Mobile端 (UniApp) | Web端 (商家运营中心) | 功能说明 |
|-------------------|---------------------|----------|
| AuthController | MerchantAuthController | 用户/商家认证 |
| NoteController | MerchantNoteController | 笔记管理 |
| ShopController | MerchantShopController | 店铺管理 |
| CommentController | MerchantCommentController | 评论管理 |
| CouponController | MerchantCouponController | 优惠券管理 |
| UserController | - | 用户信息管理 |
| MessageController | - | 消息/私信管理 |
| CommonController | - | 公共接口 |
| UploadController | MerchantUploadController | 文件上传 |
| - | MerchantDashboardController | 商家仪表盘 |

### Service层对应关系

| Mobile端服务 | Web端服务 | 共用服务 |
|-------------|----------|----------|
| AuthService | MerchantAuthService | OssService |
| NoteService | MerchantNoteService | UploadService |
| ShopService | MerchantShopService | CommonService |
| CommentService | MerchantCommentService | - |
| UserService | - | - |
| MessageService | - | - |
| - | MerchantCouponService | - |
| - | MerchantDashboardService | - |

## API路径规范

| 端 | 基础路径 | 示例 |
|----|---------|------|
| Mobile端 | `/api/*` | `/api/auth/login`, `/api/notes` |
| Web端 | `/merchant/api/*` | `/merchant/api/auth/login`, `/merchant/api/shops` |

## 技术栈

- **框架**: Spring Boot 3.2.0
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **连接池**: Druid 1.2.20
- **JWT**: jjwt 0.12.3
- **工具库**: Hutool 5.8.25
- **云服务**: 阿里云OSS、阿里云SMS

## 目录结构说明

```
backend-business-reviews/
├── backend-business-reviews-common/     # 公共模块
├── backend-business-reviews-entity/     # 实体模块
├── backend-business-reviews-mapper/     # 数据访问模块
├── backend-business-reviews-service/    # 业务逻辑模块
├── backend-business-reviews-mobile/     # Mobile端控制器模块
├── backend-business-reviews-web/        # Web端控制器模块
├── docs/                                # 项目文档
├── sql/                                 # 数据库脚本
└── pom.xml                              # 父POM文件
```
