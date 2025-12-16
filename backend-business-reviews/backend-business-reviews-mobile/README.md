# Mobile模块 (backend-business-reviews-mobile)

## 模块职责

移动端控制器层，为UniApp移动应用提供RESTful API接口。

## 应用信息

- **应用名称**: MobileBusinessReviewsApplication
- **默认端口**: 8080
- **API前缀**: `/api/*`

## 包结构

```
com.businessreviews/
├── config/                    # 配置类
│   ├── AsyncConfig.java       # 异步配置
│   ├── CorsConfig.java        # 跨域配置
│   ├── MobileWebSocketConfig.java  # WebSocket配置
│   ├── MybatisPlusConfig.java # MyBatis-Plus配置
│   ├── RedisConfig.java       # Redis配置
│   └── WebMvcConfig.java      # Web MVC配置
│
├── controller/                # 控制器
│   ├── AuthController.java    # 用户认证
│   ├── NoteController.java    # 笔记管理
│   ├── ShopController.java    # 店铺浏览
│   ├── CommentController.java # 评论管理
│   ├── UserController.java    # 用户信息
│   ├── MessageController.java # 消息通知
│   ├── CouponController.java  # 优惠券
│   ├── CommonController.java  # 公共接口
│   └── UploadController.java  # 文件上传
│
├── exception/                 # 异常处理
│   └── GlobalExceptionHandler.java
│
├── interceptor/               # 拦截器
│   └── AuthInterceptor.java   # 认证拦截器
│
└── MobileBusinessReviewsApplication.java  # 启动类
```

## API端点列表

### 认证相关 (/auth)
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/send-code | 发送验证码 |
| POST | /auth/login-by-code | 验证码登录 |
| POST | /auth/oauth-login | 第三方登录 |
| POST | /auth/logout | 退出登录 |

### 笔记相关 (/notes)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /notes/recommended | 获取推荐笔记 |
| GET | /notes/my | 获取我的笔记 |
| GET | /notes/liked | 获取点赞的笔记 |
| GET | /notes/explore | 探索页笔记 |
| GET | /notes/nearby | 附近笔记 |
| GET | /notes/following | 关注用户的笔记 |
| GET | /notes/{id} | 笔记详情 |
| POST | /notes | 发布笔记 |
| PUT | /notes/{id} | 更新笔记 |
| DELETE | /notes/{id} | 删除笔记 |
| POST | /notes/{id}/like | 点赞笔记 |
| POST | /notes/{id}/bookmark | 收藏笔记 |

### 店铺相关 (/shops)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /shops | 获取商家列表 |
| GET | /shops/nearby | 附近商家 |
| GET | /shops/search | 搜索商家 |
| GET | /shops/{id} | 商家详情 |
| GET | /shops/{id}/notes | 商家笔记 |
| GET | /shops/{id}/reviews | 商家评价 |
| POST | /shops/{id}/reviews | 发表评价 |
| POST | /shops/{id}/bookmark | 收藏商家 |

### 用户相关 (/user)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /user/info | 获取用户信息 |
| PUT | /user/info | 更新用户信息 |
| GET | /user/profile/{userId} | 用户主页 |
| GET | /user/notes | 我的笔记 |
| GET | /user/favorites | 我的收藏 |
| GET | /user/history | 浏览历史 |
| POST | /user/follow | 关注用户 |
| GET | /user/following | 关注列表 |
| GET | /user/followers | 粉丝列表 |

### 消息相关 (/messages)
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /messages/conversations | 会话列表 |
| GET | /messages/chat/{targetUserId} | 聊天记录 |
| POST | /messages/send | 发送消息 |
| GET | /messages/notifications | 系统通知 |
| GET | /messages/unread-count | 未读数量 |

## 依赖关系

```
mobile模块
    ├── 依赖 → service模块 (业务逻辑)
    ├── 依赖 → entity模块 (实体和DTO)
    └── 依赖 → common模块 (工具类和配置)
```

## 与Web端的区别

| 特性 | Mobile端 | Web端 |
|------|---------|-------|
| 目标用户 | 普通用户 | 商家/运营人员 |
| 前端应用 | UniApp移动应用 | Vue.js商家运营中心 |
| API前缀 | /api/* | /merchant/api/* |
| 认证方式 | 手机验证码/第三方登录 | 手机验证码/密码登录 |
| 主要功能 | 浏览、发布、互动 | 管理、统计、运营 |
