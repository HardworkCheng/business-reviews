# 美食点评系统 - Business Reviews

> 一个基于 Spring Boot + Uni-App 的全栈美食点评社交平台

## 📖 项目简介

美食点评系统是一个类似于"大众点评"的美食社交平台，提供美食笔记分享、商家点评、用户互动等功能。项目采用前后端分离架构，后端基于 Spring Boot 3.x + MyBatis-Plus，前端使用 Uni-App 开发跨平台移动应用。

### ✨ 核心功能

#### 用户系统
- ✅ 手机号验证码登录/注册
- ✅ 用户信息管理（头像、昵称、简介、性别、生日）
- ✅ 修改密码（需验证码验证）
- ✅ 修改手机号（双重验证码验证）
- ✅ 第三方登录绑定（微信/QQ/微博）
- ✅ 关注/粉丝系统
- ✅ 用户主页与统计

#### 笔记系统
- ✅ 发布美食笔记（图文）
- ✅ 笔记点赞/收藏/评论
- ✅ 笔记浏览历史
- ✅ 话题标签
- ✅ 笔记推荐算法

#### 商家系统
- ✅ 商家详情展示
- ✅ 商家评价与打分
- ✅ 商家收藏
- ✅ 地理位置与距离计算
- ✅ 营业时间显示

#### 评论系统
- ✅ 笔记评论
- ✅ 评论点赞
- ✅ 评论回复

#### 消息系统
- ✅ 私信聊天
- ✅ 系统通知
- ✅ 点赞/评论/关注通知
- ✅ 未读消息统计

#### 搜索功能
- ✅ 笔记/商家搜索
- ✅ 搜索历史
- ✅ 热门搜索

---

## 🏗️ 技术架构

### 后端技术栈

#### 核心框架
- **Spring Boot 3.2.0** - 基础框架
- **Spring MVC** - Web 框架
- **MyBatis-Plus 3.5.5** - ORM 框架
- **Maven** - 项目构建管理

#### 数据存储
- **MySQL 8.0+** - 关系型数据库
- **Redis** - 缓存与会话管理

#### 安全认证
- **JWT (JJWT 0.12.3)** - Token 认证
- **自定义拦截器** - 权限控制

#### 第三方服务
- **阿里云 OSS** - 对象存储（图片上传）
- **阿里云短信** - 验证码发送

#### 工具库
- **Lombok** - 简化代码
- **Hutool 5.8.25** - 工具类库
- **Jackson** - JSON 处理
- **Druid 1.2.20** - 数据库连接池

### 前端技术栈

#### 核心框架
- **Uni-App 3.0** - 跨平台开发框架
- **Vue 3.3.4** - 渐进式 JavaScript 框架
- **Vite 4.4.4** - 构建工具

#### UI 组件
- **uni-ui** - Uni-App 官方组件库
- **自定义组件** - Clay 风格 UI

#### 样式
- **SCSS** - CSS 预处理器
- **响应式布局** - 适配多端

#### 其他
- **Vue i18n** - 国际化支持
- **Axios 封装** - HTTP 请求

---

## 📁 项目结构

### 后端项目结构（多模块 Maven 架构）

```
backend-business-reviews/
├── backend-business-reviews-common/          # 公共模块
│   └── src/main/java/com/businessreviews/
│       ├── common/                            # 公共类
│       │   ├── Constants.java                 # 系统常量
│       │   └── Result.java                    # 统一响应
│       ├── config/                            # 配置类
│       │   └── OssConfig.java                 # OSS 配置
│       ├── context/                           # 上下文
│       │   └── UserContext.java               # 用户上下文
│       ├── exception/                         # 异常类
│       │   └── BusinessException.java         # 业务异常
│       └── util/                              # 工具类
│           ├── JwtUtil.java                   # JWT 工具
│           ├── RedisUtil.java                 # Redis 工具
│           ├── SmsUtil.java                   # 短信工具
│           ├── TimeUtil.java                  # 时间工具
│           └── DistanceUtil.java              # 距离计算
│
├── backend-business-reviews-entity/          # 实体模块
│   └── src/main/java/com/businessreviews/
│       ├── common/                            
│       │   └── PageResult.java                # 分页结果
│       ├── entity/                            # 实体类（29个）
│       │   ├── User.java                      # 用户
│       │   ├── Note.java                      # 笔记
│       │   ├── Shop.java                      # 商家
│       │   ├── Comment.java                   # 评论
│       │   ├── Message.java                   # 消息
│       │   └── ...                            # 其他实体
│       └── dto/                               # 数据传输对象
│           ├── request/                       # 请求 DTO（14个）
│           │   ├── LoginByCodeRequest.java    
│           │   ├── PublishNoteRequest.java    
│           │   ├── ChangePhoneRequest.java    
│           │   └── ...
│           └── response/                      # 响应 DTO（23个）
│               ├── UserInfoResponse.java      
│               ├── NoteDetailResponse.java    
│               └── ...
│
├── backend-business-reviews-mapper/          # 数据访问层
│   └── src/main/java/com/businessreviews/mapper/
│       ├── UserMapper.java                    # 用户 Mapper
│       ├── NoteMapper.java                    # 笔记 Mapper
│       ├── ShopMapper.java                    # 商家 Mapper
│       └── ...                                # 其他 Mapper（29个）
│
├── backend-business-reviews-service/         # 业务逻辑层
│   └── src/main/java/com/businessreviews/service/
│       ├── AuthService.java                   # 认证服务接口
│       ├── UserService.java                   # 用户服务接口
│       ├── NoteService.java                   # 笔记服务接口
│       ├── ShopService.java                   # 商家服务接口
│       └── impl/                              # 服务实现
│           ├── AuthServiceImpl.java           
│           ├── UserServiceImpl.java           
│           ├── NoteServiceImpl.java           
│           └── ...                            # 其他实现（9个）
│
├── backend-business-reviews-web/             # Web 控制层（启动模块）
│   └── src/main/
│       ├── java/com/businessreviews/
│       │   ├── controller/                    # 控制器
│       │   │   ├── AuthController.java        # 认证接口
│       │   │   ├── UserController.java        # 用户接口
│       │   │   ├── NoteController.java        # 笔记接口
│       │   │   ├── ShopController.java        # 商家接口
│       │   │   ├── CommentController.java     # 评论接口
│       │   │   ├── MessageController.java     # 消息接口
│       │   │   ├── UploadController.java      # 上传接口
│       │   │   └── CommonController.java      # 公共接口
│       │   ├── config/                        # 配置类
│       │   │   ├── MybatisPlusConfig.java     # MyBatis-Plus
│       │   │   ├── RedisConfig.java           # Redis
│       │   │   ├── CorsConfig.java            # 跨域
│       │   │   └── AsyncConfig.java           # 异步
│       │   ├── interceptor/                   # 拦截器
│       │   │   └── AuthInterceptor.java       # 认证拦截
│       │   ├── exception/                     # 异常处理
│       │   │   └── GlobalExceptionHandler.java
│       │   └── BusinessReviewsApplication.java # 启动类
│       └── resources/
│           └── application.yml                # 配置文件
│
├── docs/                                      # 项目文档
│   ├── README.md                              # 项目说明（本文件）
│   ├── API使用说明.md                         # API 使用指南
│   ├── 后端接口文档.md                        # 接口文档
│   ├── 数据库设计.md                          # 数据库设计
│   └── ...                                    # 其他文档
│
└── sql/                                       # SQL 脚本
    └── database.sql                           # 数据库初始化
```

### 前端项目结构

```
front-business-reviews-Mobile/
├── api/                                       # API 接口层
│   ├── request.js                             # 请求封装
│   ├── auth.js                                # 认证接口
│   ├── user.js                                # 用户接口
│   ├── note.js                                # 笔记接口
│   ├── shop.js                                # 商家接口
│   ├── comment.js                             # 评论接口
│   ├── message.js                             # 消息接口
│   ├── upload.js                              # 上传接口
│   └── common.js                              # 公共接口
│
├── pages/                                     # 页面
│   ├── login/                                 # 登录页
│   │   └── login.vue                          
│   ├── index/                                 # 首页（笔记流）
│   │   └── index.vue                          
│   ├── profile/                               # 个人主页
│   │   └── profile.vue                        
│   ├── settings/                              # 设置页
│   │   └── settings.vue                       
│   ├── note-detail/                           # 笔记详情
│   │   └── note-detail.vue                    
│   ├── shop-detail/                           # 商家详情
│   │   └── shop-detail.vue                    
│   ├── publish/                               # 发布笔记
│   │   └── publish.vue                        
│   ├── message/                               # 消息页
│   │   └── message.vue                        
│   ├── search/                                # 搜索页
│   │   └── search.vue                         
│   ├── map/                                   # 地图页
│   │   └── map.vue                            
│   ├── change-password/                       # 修改密码
│   │   └── change-password.vue                
│   └── change-phone/                          # 修改手机号
│       └── change-phone.vue                   
│
├── static/                                    # 静态资源
│   ├── styles/                                # 样式文件
│   │   └── common.scss                        
│   └── tabbar/                                # 底部导航图标
│
├── App.vue                                    # 应用主组件
├── main.js                                    # 入口文件
├── pages.json                                 # 页面配置
├── manifest.json                              # 应用配置
├── vite.config.js                             # Vite 配置
└── package.json                               # 依赖配置
```

---

## 🗄️ 数据库设计

### 核心数据表（29张）

#### 用户相关（5张）
- `users` - 用户基本信息
- `user_stats` - 用户统计数据
- `user_follow` - 用户关注关系
- `verification_code` - 验证码记录
- `browse_history` - 浏览历史

#### 笔记相关（6张）
- `notes` - 笔记主表
- `note_topic` - 笔记话题关联
- `note_tag` - 笔记标签关联
- `user_note_like` - 笔记点赞
- `user_note_bookmark` - 笔记收藏
- `note_comment` - 笔记评论（已废弃）

#### 商家相关（4张）
- `shops` - 商家信息
- `shop_review` - 商家评价
- `shop_tag` - 商家标签关联
- `user_shop_favorite` - 商家收藏

#### 评论相关（3张）
- `comments` - 评论主表
- `comment_like` - 评论点赞
- `user_comment_like` - 用户评论点赞

#### 消息相关（4张）
- `messages` - 私信消息
- `chat_session` - 聊天会话
- `chat_message` - 聊天消息
- `notifications` - 系统通知

#### 其他（7张）
- `categories` - 分类
- `topics` - 话题
- `tags` - 标签
- `user_favorite` - 用户收藏
- `user_browse_history` - 浏览记录
- `search_history` - 搜索历史
- `system_notice` - 系统公告

---

## 🚀 快速开始

### 环境要求

#### 后端
- **JDK 17+**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis 5.0+**
- **阿里云 OSS**（可选，图片存储）
- **阿里云短信**（可选，验证码）

#### 前端
- **Node.js 16+**
- **pnpm/npm/yarn**
- **HBuilderX**（推荐）或 CLI

### 后端部署

#### 1. 克隆项目
```bash
git clone <repository-url>
cd backend-business-reviews
```

#### 2. 配置数据库
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE business_reviews CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入 SQL
mysql -u root -p business_reviews < sql/database.sql
```

#### 3. 配置 Redis
确保 Redis 服务已启动：
```bash
redis-server
```

#### 4. 修改配置文件
编辑 `backend-business-reviews-web/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/business_reviews?...
    username: your_username
    password: your_password
  
  data:
    redis:
      host: your_redis_host
      port: 6379
      password: your_redis_password

# 阿里云 OSS 配置（可选）
aliyun:
  oss:
    endpoint: oss-cn-beijing.aliyuncs.com
    access-key-id: YOUR_ACCESS_KEY_ID
    access-key-secret: YOUR_ACCESS_KEY_SECRET
    bucket-name: YOUR_BUCKET_NAME
    url-prefix: https://YOUR_BUCKET.oss-cn-beijing.aliyuncs.com

# 阿里云短信配置（可选）
sms:
  aliyun:
    access-key-id: YOUR_ACCESS_KEY_ID
    access-key-secret: YOUR_ACCESS_KEY_SECRET
    sign-name: YOUR_SIGN_NAME
    template-code: YOUR_TEMPLATE_CODE
```

#### 5. 构建并运行
```bash
# 使用 Maven 构建
mvn clean install

# 运行启动类
cd backend-business-reviews-web
mvn spring-boot:run
```

或在 IntelliJ IDEA 中直接运行 `BusinessReviewsApplication.java`

访问：`http://localhost:8080/api`

### 前端部署

#### 1. 安装依赖
```bash
cd front-business-reviews-Mobile
npm install
# 或
pnpm install
```

#### 2. 配置后端地址
编辑 `api/request.js`：
```javascript
const BASE_URL = 'http://localhost:8080/api'  // 修改为你的后端地址
```

#### 3. 运行项目

**H5 开发模式：**
```bash
npm run dev:h5
```
访问：`http://localhost:5173`

**微信小程序：**
```bash
npm run dev:mp-weixin
```
然后在微信开发者工具中打开 `dist/dev/mp-weixin` 目录

**App：**
```bash
npm run dev:app
```
使用 HBuilderX 运行

#### 4. 构建生产版本
```bash
# H5
npm run build:h5

# 微信小程序
npm run build:mp-weixin

# App
npm run build:app
```

---

## 📡 核心接口

### 认证模块 (`/auth`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/send-code` | POST | 发送验证码 |
| `/login-by-code` | POST | 验证码登录 |
| `/oauth-login` | POST | 第三方登录 |
| `/logout` | POST | 退出登录 |

### 用户模块 (`/user`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/info` | GET | 获取用户信息 |
| `/info` | PUT | 更新用户信息 |
| `/phone` | GET | 获取手机号 |
| `/phone` | PUT | 修改手机号 |
| `/password` | PUT | 修改密码 |
| `/notes` | GET | 我的笔记 |
| `/favorites` | GET | 我的收藏 |
| `/history` | GET | 浏览历史 |
| `/follow` | POST | 关注用户 |
| `/follow/{id}` | DELETE | 取消关注 |

### 笔记模块 (`/notes`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 笔记列表 |
| `/` | POST | 发布笔记 |
| `/{id}` | GET | 笔记详情 |
| `/{id}` | DELETE | 删除笔记 |
| `/{id}/like` | POST | 点赞笔记 |
| `/{id}/like` | DELETE | 取消点赞 |
| `/{id}/bookmark` | POST | 收藏笔记 |
| `/{id}/bookmark` | DELETE | 取消收藏 |

### 商家模块 (`/shops`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 商家列表 |
| `/{id}` | GET | 商家详情 |
| `/{id}/reviews` | GET | 商家评价 |
| `/{id}/reviews` | POST | 发表评价 |
| `/{id}/favorite` | POST | 收藏商家 |
| `/{id}/favorite` | DELETE | 取消收藏 |

### 评论模块 (`/comments`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | POST | 发表评论 |
| `/{id}` | DELETE | 删除评论 |
| `/{id}/like` | POST | 点赞评论 |
| `/{id}/like` | DELETE | 取消点赞 |

### 消息模块 (`/messages`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/chats` | GET | 会话列表 |
| `/chat/{userId}` | GET | 聊天记录 |
| `/send` | POST | 发送消息 |
| `/notifications` | GET | 系统通知 |
| `/unread-count` | GET | 未读数统计 |

### 上传模块 (`/upload`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/avatar` | POST | 上传头像 |
| `/image` | POST | 上传图片 |

### 公共模块 (`/common`)
| 接口 | 方法 | 说明 |
|------|------|------|
| `/categories` | GET | 分类列表 |
| `/topics` | GET | 话题列表 |
| `/hot-searches` | GET | 热门搜索 |

---

## 🔐 安全机制

### JWT 认证
- Token 有效期：7天
- 自动续期机制
- Token 黑名单（退出登录）

### 验证码安全
- 5 分钟有效期
- 10 秒发送频率限制
- Redis 存储防篡改
- 支持 5 种类型：
  1. 登录
  2. 注册
  3. 重置密码
  4. 修改手机号-验证原手机
  5. 修改手机号-验证新手机

### 修改手机号安全
- 双重验证码验证（原手机+新手机）
- 24 小时频率限制（只能修改一次）
- 新手机号重复检查
- 原手机号控制权验证

### 数据安全
- 手机号脱敏显示
- 密码单向加密
- SQL 注入防护（MyBatis-Plus）
- XSS 防护

---

## 📊 性能优化

### 缓存策略
- **Redis 缓存**：用户信息、商家信息、分类、热门搜索
- **缓存过期时间**：
  - 用户信息：30 分钟
  - 商家信息：1 小时
  - 分类列表：1 天
  - 推荐笔记：5 分钟

### 数据库优化
- 索引优化：主键、外键、常用查询字段
- 连接池：Druid（初始5，最大20）
- 分页查询：MyBatis-Plus 分页插件
- 逻辑删除：软删除机制

### 异步处理
- 消息通知异步发送
- 统计数据异步更新
- 日志异步记录

---

## 🧪 测试

### 后端测试
```bash
mvn test
```

### API 测试
推荐使用：
- **Postman** - API 调试
- **Apifox** - API 文档与测试
- **JMeter** - 性能测试

导入 `docs/后端接口文档.md` 中的接口进行测试。

---

## 📝 开发规范

### 后端规范

#### 包命名
- `controller` - 控制器层
- `service` - 业务逻辑层
- `mapper` - 数据访问层
- `entity` - 实体类
- `dto` - 数据传输对象
- `common` - 公共类
- `util` - 工具类
- `config` - 配置类
- `exception` - 异常类

#### 接口规范
- RESTful 风格
- 统一响应格式：`Result<T>`
- 统一异常处理：`GlobalExceptionHandler`
- 参数校验：`@Valid` + `@NotNull` 等

#### 代码规范
- 使用 Lombok 简化代码
- 业务异常使用 `BusinessException`
- 日志使用 `@Slf4j`
- 事务使用 `@Transactional`

### 前端规范

#### 目录规范
- `pages` - 页面组件
- `api` - 接口调用
- `static` - 静态资源
- `components` - 公共组件（如需）

#### 命名规范
- 文件名：`kebab-case`（如 `user-profile.vue`）
- 组件名：`PascalCase`
- 变量名：`camelCase`
- 常量名：`UPPER_CASE`

#### 样式规范
- 使用 SCSS
- 尺寸单位：`rpx`（响应式）
- Clay 风格 UI（柔和渐变 + 阴影）

---

## 🐛 常见问题

### 1. 启动失败
**问题**：数据库连接失败  
**解决**：检查 `application.yml` 中的数据库配置，确保 MySQL 服务已启动

### 2. Redis 连接失败
**问题**：无法连接到 Redis  
**解决**：检查 Redis 服务是否启动，防火墙是否开放端口

### 3. 图片上传失败
**问题**：OSS 上传失败  
**解决**：检查阿里云 OSS 配置，确保 AccessKey 和 Bucket 配置正确

### 4. 验证码发送失败
**问题**：短信验证码发送失败  
**解决**：
- 检查阿里云短信配置
- 可启用开发模式（`sms.aliyun.dev-mode=true`），使用固定验证码 `123456`

### 5. 跨域问题
**问题**：前端请求后端出现跨域错误  
**解决**：确保后端 `CorsConfig` 已配置，允许前端域名访问

### 6. Token 过期
**问题**：接口返回 401 未授权  
**解决**：重新登录获取新 Token

---

## 📚 相关文档

- [API 使用说明](backend-business-reviews/docs/API使用说明.md)
- [后端接口文档](backend-business-reviews/docs/后端接口文档.md)
- [前端接口文档](backend-business-reviews/docs/前端接口文档.md)
- [数据库设计](backend-business-reviews/docs/数据库设计.md)
- [阿里云 OSS 配置指南](backend-business-reviews/docs/阿里云OSS配置指南.md)
- [头像上传功能说明](backend-business-reviews/docs/头像上传功能说明.md)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

### 开发流程
1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 License

本项目采用 MIT 许可证 - 详见 [LICENSE](../LICENSE) 文件

---

## 👨‍💻 作者

**程明杰**

---

## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis-Plus](https://baomidou.com/)
- [Uni-App](https://uniapp.dcloud.io/)
- [Vue.js](https://vuejs.org/)
- [Redis](https://redis.io/)
- [Hutool](https://hutool.cn/)

---

## 📞 联系方式

如有问题或建议，欢迎联系：
- 📧 Email: your-email@example.com
- 💬 微信: your-wechat-id

---

**⭐ 如果这个项目对你有帮助，请给个 Star！**
