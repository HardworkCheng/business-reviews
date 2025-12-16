# Web模块 (backend-business-reviews-web)

## 模块职责

商家运营中心控制器层，为Vue.js商家运营中心网页提供RESTful API接口。

## 应用信息

- **应用名称**: MerchantApplication
- **默认端口**: 8081
- **API前缀**: `/merchant/*`

## 包结构

```
com.businessreviews.merchant/
├── config/                    # 配置类
│   └── MerchantWebConfig.java # Web MVC配置
│
├── context/                   # 上下文
│   └── MerchantContext.java   # 商家上下文（存储当前登录商家信息）
│
├── controller/                # 控制器
│   ├── MerchantAuthController.java       # 商家认证
│   ├── MerchantShopController.java       # 门店管理
│   ├── MerchantNoteController.java       # 笔记管理
│   ├── MerchantCommentController.java    # 评论管理
│   ├── MerchantCouponController.java     # 优惠券管理
│   ├── MerchantDashboardController.java  # 数据看板
│   └── MerchantUploadController.java     # 文件上传
│
├── exception/                 # 异常处理
│   └── (全局异常处理)
│
├── interceptor/               # 拦截器
│   └── MerchantAuthInterceptor.java  # 商家认证拦截器
│
└── MerchantApplication.java   # 启动类
```

## API端点列表

### 认证相关 (/merchant/auth)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /merchant/auth/send-code | 发送验证码 |
| POST | /merchant/auth/login | 登录（密码/验证码） |
| POST | /merchant/auth/register | 商家入驻注册 |
| GET | /merchant/auth/profile | 获取当前用户信息 |
| POST | /merchant/auth/logout | 退出登录 |

### 门店管理 (/merchant/shops)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /merchant/shops | 获取门店列表 |
| GET | /merchant/shops/{id} | 获取门店详情 |
| POST | /merchant/shops | 新增门店 |
| PUT | /merchant/shops/{id} | 更新门店信息 |
| PUT | /merchant/shops/{id}/status | 更新门店状态 |
| DELETE | /merchant/shops/{id} | 删除门店 |
| GET | /merchant/shops/{id}/stats | 门店统计数据 |

### 笔记管理 (/merchant/notes)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /merchant/notes | 获取笔记列表 |
| GET | /merchant/notes/{id} | 获取笔记详情 |
| POST | /merchant/notes | 创建笔记 |
| PUT | /merchant/notes/{id} | 更新笔记 |
| POST | /merchant/notes/{id}/publish | 发布笔记 |
| POST | /merchant/notes/{id}/offline | 下线笔记 |
| DELETE | /merchant/notes/{id} | 删除笔记 |
| GET | /merchant/notes/{id}/stats | 笔记统计 |

### 评论管理 (/merchant/comments)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /merchant/comments | 获取评论列表 |
| POST | /merchant/comments/{id}/reply | 回复评论 |
| DELETE | /merchant/comments/{id} | 删除评论 |
| GET | /merchant/comments/stats | 评论统计 |

### 优惠券管理 (/merchant/coupons)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /merchant/coupons | 获取优惠券列表 |
| GET | /merchant/coupons/{id} | 获取优惠券详情 |
| POST | /merchant/coupons | 创建优惠券 |
| PUT | /merchant/coupons/{id} | 更新优惠券 |
| PUT | /merchant/coupons/{id}/status | 更新优惠券状态 |
| GET | /merchant/coupons/{id}/stats | 优惠券统计 |
| POST | /merchant/coupons/redeem | 核销优惠券 |
| GET | /merchant/coupons/verify | 验证券码 |

### 数据看板 (/merchant/dashboard)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /merchant/dashboard | 获取看板数据 |
| GET | /merchant/dashboard/notes | 笔记分析 |
| GET | /merchant/dashboard/coupons | 优惠券分析 |
| GET | /merchant/dashboard/users | 用户分析 |

## 依赖关系

```
web模块
    ├── 依赖 → service模块 (业务逻辑)
    ├── 依赖 → entity模块 (实体和DTO)
    └── 依赖 → common模块 (工具类和配置)
```

## 与Mobile端的区别

| 特性 | Web端 | Mobile端 |
|------|-------|---------|
| 目标用户 | 商家/运营人员 | 普通用户 |
| 前端应用 | Vue.js商家运营中心 | UniApp移动应用 |
| API前缀 | /merchant/* | /api/* |
| 认证方式 | 手机验证码/密码登录 | 手机验证码/第三方登录 |
| 主要功能 | 管理、统计、运营 | 浏览、发布、互动 |
| 权限控制 | 基于商家ID和角色 | 基于用户ID |
