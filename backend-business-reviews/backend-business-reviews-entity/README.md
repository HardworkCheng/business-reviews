# Entity模块 (backend-business-reviews-entity)

## 模块职责

数据模型层，包含数据库实体类(Entity)和数据传输对象(DTO)。

## 包结构

```
com.businessreviews/
├── entity/                    # 数据库实体类
│   ├── User.java              # 用户实体
│   ├── Note.java              # 笔记实体
│   ├── Shop.java              # 店铺实体
│   ├── Comment.java           # 评论实体
│   ├── Coupon.java            # 优惠券实体
│   ├── Message.java           # 消息实体
│   ├── Merchant.java          # 商家实体
│   ├── MerchantUser.java      # 商家用户实体
│   └── ...                    # 其他实体
│
├── common/                    # 通用类
│   └── PageResult.java        # 分页结果类
│
└── dto/                       # 数据传输对象
    ├── common/                # 共用DTO
    │   └── PageResult.java    # 通用分页结果
    │
    ├── mobile/                # Mobile端DTO (UniApp)
    │   ├── request/           # 请求DTO
    │   │   ├── LoginByCodeRequest.java
    │   │   ├── SendCodeRequest.java
    │   │   ├── PublishNoteRequest.java
    │   │   ├── OAuthLoginRequest.java
    │   │   └── AddCommentRequest.java
    │   └── response/          # 响应DTO
    │       ├── LoginResponse.java
    │       ├── NoteItemResponse.java
    │       └── UserInfoResponse.java
    │
    ├── merchant/              # Merchant端DTO (商家运营中心)
    │   ├── request/           # 请求DTO
    │   │   ├── MerchantLoginRequest.java
    │   │   ├── MerchantRegisterRequest.java
    │   │   └── CreateCouponRequest.java
    │   └── response/          # 响应DTO
    │       ├── MerchantLoginResponse.java
    │       └── MerchantUserInfoResponse.java
    │
    └── request/               # 原有请求DTO（保留兼容）
    └── response/              # 原有响应DTO（保留兼容）
```

## Mobile端 vs Merchant端 DTO区别

| 特性 | Mobile端 (mobile/) | Merchant端 (merchant/) |
|------|-------------------|------------------------|
| 目标用户 | 普通用户 | 商家/运营人员 |
| 认证方式 | 手机验证码/第三方登录 | 手机验证码/密码登录 |
| 主要操作 | 浏览、发布、互动 | 管理、统计、运营 |

## DTO说明

### Mobile端请求DTO

| DTO | 说明 |
|-----|------|
| LoginByCodeRequest | 验证码登录请求 |
| SendCodeRequest | 发送验证码请求 |
| PublishNoteRequest | 发布笔记请求 |
| OAuthLoginRequest | 第三方登录请求 |
| AddCommentRequest | 添加评论请求 |

### Merchant端请求DTO

| DTO | 说明 |
|-----|------|
| MerchantLoginRequest | 商家登录请求 |
| MerchantRegisterRequest | 商家注册请求 |
| CreateCouponRequest | 创建优惠券请求 |

### 共用DTO

| DTO | 说明 |
|-----|------|
| PageResult | 通用分页结果 |

## 依赖关系

```
entity模块
    └── 依赖 → common模块 (工具类)
```

## 注意事项

1. **向后兼容**: 原有的`dto/request/`和`dto/response/`包保留，确保现有代码正常运行
2. **新包结构**: 新的分包结构（mobile/merchant/common）用于更清晰的代码组织
3. **Entity共用**: 所有Entity类被Mobile端和Merchant端共用，不需要分包
