# Common模块 (backend-business-reviews-common)

## 模块职责

公共基础设施层，提供配置、工具类、常量、枚举、异常处理等公共功能，被所有其他模块依赖。

## 包结构

```
com.businessreviews/
├── common/                    # 通用类
│   ├── Constants.java         # 系统常量
│   ├── DefaultAvatar.java     # 默认头像配置
│   └── Result.java            # 统一响应结果
│
├── config/                    # 配置类
│   ├── OssConfig.java         # 阿里云OSS配置
│   └── WebSocketConfig.java   # WebSocket配置
│
├── constants/                 # 常量定义
│   ├── CacheExpireConstants.java  # 缓存过期时间常量
│   ├── PageConstants.java         # 分页常量
│   ├── RedisKeyConstants.java     # Redis键常量
│   └── SmsCodeConstants.java      # 短信验证码常量
│
├── context/                   # 上下文
│   └── UserContext.java       # 用户上下文（存储当前登录用户信息）
│
├── enums/                     # 枚举类
│   ├── BrowseTargetType.java  # 浏览目标类型
│   ├── CodeType.java          # 验证码类型
│   ├── CommentStatus.java     # 评论状态
│   ├── ErrorCode.java         # 错误码
│   ├── FavoriteType.java      # 收藏类型
│   ├── Gender.java            # 性别
│   ├── MessageType.java       # 消息类型
│   ├── NoteStatus.java        # 笔记状态
│   ├── NoticeType.java        # 通知类型
│   ├── ShopStatus.java        # 店铺状态
│   └── UserStatus.java        # 用户状态
│
├── exception/                 # 异常类
│   └── BusinessException.java # 业务异常
│
├── handler/                   # 处理器
│   └── MessageWebSocketHandler.java  # WebSocket消息处理器
│
└── util/                      # 工具类
    ├── DistanceUtil.java      # 距离计算工具
    ├── JwtUtil.java           # JWT工具
    ├── RedisUtil.java         # Redis工具
    ├── SmsUtil.java           # 短信工具
    └── TimeUtil.java          # 时间工具
```

## 组件说明

### 通用类 (common/)

| 类 | 说明 |
|---|------|
| Result | 统一API响应结果封装 |
| Constants | 系统常量定义 |
| DefaultAvatar | 默认头像URL配置 |

### 配置类 (config/)

| 类 | 说明 |
|---|------|
| OssConfig | 阿里云OSS客户端配置 |
| WebSocketConfig | WebSocket端点配置 |

### 常量类 (constants/)

| 类 | 说明 |
|---|------|
| CacheExpireConstants | 缓存过期时间（秒） |
| PageConstants | 分页默认值 |
| RedisKeyConstants | Redis键前缀 |
| SmsCodeConstants | 短信验证码配置 |

### 枚举类 (enums/)

| 枚举 | 说明 |
|------|------|
| ErrorCode | 错误码定义 |
| NoteStatus | 笔记状态（草稿/已发布/已下线） |
| ShopStatus | 店铺状态（正常/停用） |
| UserStatus | 用户状态（正常/禁用） |
| MessageType | 消息类型（文本/图片） |
| NoticeType | 通知类型（点赞/评论/关注） |

### 工具类 (util/)

| 类 | 说明 |
|---|------|
| JwtUtil | JWT令牌生成和解析 |
| RedisUtil | Redis操作封装 |
| SmsUtil | 阿里云短信发送 |
| DistanceUtil | 经纬度距离计算 |
| TimeUtil | 时间格式化工具 |

### 异常类 (exception/)

| 类 | 说明 |
|---|------|
| BusinessException | 业务异常，包含错误码和消息 |

### 上下文 (context/)

| 类 | 说明 |
|---|------|
| UserContext | 基于ThreadLocal存储当前登录用户信息 |

## 依赖关系

```
common模块
    └── 无内部模块依赖（最底层模块）
```

## 被依赖关系

```
所有其他模块
    └── 依赖 → common模块
```

## 注意事项

1. **最底层模块**: common模块不依赖任何其他业务模块
2. **共用设计**: 所有类被Mobile端和Web端共用
3. **线程安全**: UserContext使用ThreadLocal，注意在请求结束时清理
4. **配置外部化**: 敏感配置（如OSS密钥）应放在application.yml中
