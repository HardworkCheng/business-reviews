# Service模块 (backend-business-reviews-service)

## 模块职责

业务逻辑层，负责处理所有业务逻辑，是Controller层和Mapper层之间的桥梁。

## 包结构

```
com.businessreviews.service/
├── mobile/                    # 移动端服务接口 (UniApp)
│   ├── AuthService.java       # 用户认证服务
│   ├── NoteService.java       # 笔记服务
│   ├── ShopService.java       # 店铺服务
│   ├── CommentService.java    # 评论服务
│   ├── UserService.java       # 用户服务
│   └── MessageService.java    # 消息服务
│
├── merchant/                  # 商家端服务接口 (商家运营中心)
│   ├── MerchantAuthService.java       # 商家认证服务
│   ├── MerchantNoteService.java       # 商家笔记服务
│   ├── MerchantShopService.java       # 商家门店服务
│   ├── MerchantCommentService.java    # 商家评论服务
│   ├── MerchantCouponService.java     # 商家优惠券服务
│   └── MerchantDashboardService.java  # 商家数据看板服务
│
├── common/                    # 共用服务接口
│   ├── OssService.java        # OSS文件上传服务
│   ├── UploadService.java     # 通用上传服务
│   └── CommonService.java     # 通用业务服务
│
└── impl/                      # 服务实现类
    ├── mobile/                # 移动端服务实现
    ├── merchant/              # 商家端服务实现
    └── common/                # 共用服务实现
```

## Mobile端 vs Web端 区别

| 特性 | Mobile端 (mobile/) | Web端 (merchant/) |
|------|-------------------|-------------------|
| 目标用户 | 普通用户 | 商家/运营人员 |
| 前端应用 | UniApp移动应用 | Vue.js商家运营中心 |
| 主要功能 | 浏览、发布、互动 | 管理、统计、运营 |
| 认证方式 | 手机验证码/第三方登录 | 手机验证码/密码登录 |

## 服务说明

### Mobile端服务

| 服务 | 说明 |
|------|------|
| AuthService | 用户登录、注册、验证码发送 |
| NoteService | 笔记发布、浏览、点赞、收藏 |
| ShopService | 店铺浏览、搜索、收藏、评价 |
| CommentService | 评论发布、点赞、回复 |
| UserService | 用户信息管理、关注、粉丝 |
| MessageService | 私信、系统通知 |

### Merchant端服务

| 服务 | 说明 |
|------|------|
| MerchantAuthService | 商家登录、注册、入驻 |
| MerchantNoteService | 商家笔记管理、发布、下线 |
| MerchantShopService | 门店管理、创建、编辑 |
| MerchantCommentService | 评论管理、回复 |
| MerchantCouponService | 优惠券管理、核销 |
| MerchantDashboardService | 数据统计、分析 |

### 共用服务

| 服务 | 说明 |
|------|------|
| OssService | 阿里云OSS文件上传 |
| UploadService | 图片上传处理 |
| CommonService | 分类、话题、搜索建议 |

## 依赖关系

```
service模块
    ├── 依赖 → mapper模块 (数据访问)
    ├── 依赖 → entity模块 (实体和DTO)
    └── 依赖 → common模块 (工具类和配置)
```

## 注意事项

1. **原有接口保留**: 为保持向后兼容，原有的Service接口（如`com.businessreviews.service.NoteService`）仍然保留
2. **新包结构**: 新的分包结构（mobile/merchant/common）用于更清晰的代码组织
3. **实现类**: 实现类位于`impl`子包下，按照相同的分包结构组织
