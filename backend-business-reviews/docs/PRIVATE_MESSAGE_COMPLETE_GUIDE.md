# 私信功能完整实现指南

## ✅ 已完成的工作

### 1. 数据库
- ✅ SQL脚本已创建: `backend-business-reviews/sql/EXECUTE_THIS_FOR_PRIVATE_MESSAGE.sql`
  - `conversations` 表 - 会话表
  - `private_messages` 表 - 私信消息表
  - `user_online_status` 表 - 用户在线状态表

### 2. 后端实现
- ✅ 实体类 (Entity)
  - `Conversation.java`
  - `PrivateMessage.java`
  - `ConversationItemResponse.java`
  - `PrivateMessageResponse.java`
  - `SendMessageRequest.java`

- ✅ 数据访问层 (Mapper)
  - `ConversationMapper.java`
  - `PrivateMessageMapper.java`

- ✅ 业务逻辑层 (Service)
  - `MessageService.java` - 接口
  - `MessageServiceImpl.java` - 实现类（已集成WebSocket推送）

- ✅ 控制器层 (Controller)
  - `MessageController.java` - 提供REST API

- ✅ WebSocket配置
  - `WebSocketConfig.java` - WebSocket配置类
  - `MessageWebSocketHandler.java` - WebSocket消息处理器

### 3. 前端实现 (UniApp)
- ✅ API接口
  - `src/api/message.js` - 私信相关API

- ✅ WebSocket管理
  - `src/utils/websocket.js` - WebSocket连接管理器

- ✅ 页面组件
  - `src/pages/chat/chat.vue` - 聊天页面
  - `src/pages/message/message.vue` - 消息页面（已连接后端API）
  - `src/pages/user-profile/user-profile.vue` - 用户主页（已添加私信按钮）
  - `src/pages/user-list/user-list.vue` - 用户列表（已添加私信按钮）
  - `src/pages/note-detail/note-detail.vue` - 笔记详情（头像可点击跳转）

- ✅ 页面注册
  - `src/pages.json` - 已注册聊天页面

## 📋 部署步骤

### 第一步：执行数据库脚本

```bash
# 在MySQL中执行以下SQL文件
backend-business-reviews/sql/EXECUTE_THIS_FOR_PRIVATE_MESSAGE.sql
```

这将创建私信功能所需的3个数据库表。

### 第二步：重新编译后端

```bash
cd backend-business-reviews
mvn clean install
```

### 第三步：启动后端服务

```bash
# 启动移动端API服务
cd backend-business-reviews-mobile
mvn spring-boot:run

# 或者启动Web端API服务
cd backend-business-reviews-web
mvn spring-boot:run
```

### 第四步：重新编译前端

```bash
cd front-business-reviews-Mobile
npm install
npm run dev:mp-weixin  # 微信小程序
# 或
npm run dev:h5  # H5版本
```

## 🔧 功能说明

### 1. 私信入口

用户可以从以下位置进入私信功能：

1. **笔记详情页**
   - 点击作者头像 → 进入用户主页 → 点击"💬 私信"按钮

2. **用户主页**
   - 直接点击"💬 私信"按钮（关注按钮旁边）

3. **关注/粉丝列表**
   - 每个用户右侧有"💬"按钮，点击即可私信

4. **消息页面**
   - 查看所有聊天会话
   - 点击会话进入聊天页面

### 2. 实时通信

- 使用WebSocket实现即时消息推送
- 自动心跳保持连接
- 断线自动重连
- 消息持久化存储

### 3. 消息功能

- ✅ 发送文本消息
- ✅ 查看聊天记录
- ✅ 未读消息提示
- ✅ 会话列表
- ✅ 消息时间显示
- ✅ 在线状态（可扩展）

## 🔍 API接口说明

### 获取会话列表
```
GET /api/messages/conversations?pageNum=1&pageSize=20
```

### 获取聊天记录
```
GET /api/messages/chat/{targetUserId}?pageNum=1&pageSize=20
```

### 发送消息
```
POST /api/messages/send
Body: {
  "targetUserId": "123",
  "content": "你好",
  "type": 1
}
```

### 标记消息已读
```
POST /api/messages/read/{targetUserId}
```

### 获取未读消息数
```
GET /api/messages/unread-count
```

### 获取通知列表
```
GET /api/messages/notifications?pageNum=1&pageSize=20&type=1
```

## 🌐 WebSocket连接

### 连接地址
```
ws://localhost:8080/api/ws?userId={userId}&token={token}
```

### 消息格式

**客户端发送心跳：**
```json
{
  "type": "heartbeat"
}
```

**服务端推送消息：**
```json
{
  "type": "private_message",
  "data": {
    "id": "123",
    "senderId": "456",
    "senderName": "张三",
    "senderAvatar": "https://...",
    "content": "你好",
    "createdAt": "2024-01-01 12:00:00"
  }
}
```

## 🧪 测试步骤

### 1. 测试数据库
```sql
-- 检查表是否创建成功
SHOW TABLES LIKE '%message%';
SHOW TABLES LIKE '%conversation%';
```

### 2. 测试后端API
使用Postman或其他工具测试：
1. 登录获取token
2. 测试发送消息接口
3. 测试获取会话列表接口
4. 测试获取聊天记录接口

### 3. 测试WebSocket
1. 打开浏览器开发者工具
2. 切换到Network标签
3. 筛选WS连接
4. 查看WebSocket连接状态和消息

### 4. 测试前端功能
1. 登录两个不同的账号（可以用两个设备或浏览器）
2. 账号A进入账号B的主页，点击私信
3. 发送消息
4. 在账号B的消息页面查看是否收到消息
5. 测试实时推送（账号A发送消息，账号B立即收到）

## ⚠️ 注意事项

### 1. WebSocket连接地址
- 开发环境：`ws://localhost:8080/api/ws`
- 生产环境：需要改为 `wss://your-domain.com/api/ws`
- 在 `front-business-reviews-Mobile/src/utils/websocket.js` 中修改

### 2. 跨域配置
如果遇到跨域问题，需要在后端配置CORS：
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

### 3. 认证问题
- WebSocket连接需要传递token
- 确保token有效期足够长
- 如果token过期，需要重新登录

### 4. 性能优化建议
- 聊天记录分页加载（已实现）
- WebSocket心跳间隔：30秒（已实现）
- 消息列表虚拟滚动（可选）
- 图片消息压缩（可选）

## 🚀 后续扩展功能

### 可以添加的功能：
1. 图片消息支持
2. 语音消息支持
3. 视频消息支持
4. 消息撤回功能
5. 消息已读回执
6. 输入状态提示（正在输入...）
7. 消息搜索功能
8. 聊天记录导出
9. 消息推送通知
10. 群聊功能

## 📞 问题排查

### 问题1：WebSocket连接失败
**解决方案：**
1. 检查后端服务是否启动
2. 检查WebSocket端点是否正确
3. 检查防火墙设置
4. 查看浏览器控制台错误信息

### 问题2：消息发送失败
**解决方案：**
1. 检查token是否有效
2. 检查目标用户ID是否正确
3. 查看后端日志
4. 检查数据库连接

### 问题3：消息不能实时推送
**解决方案：**
1. 检查WebSocket连接状态
2. 检查接收者是否在线
3. 查看后端日志中的推送记录
4. 测试心跳是否正常

### 问题4：会话列表为空
**解决方案：**
1. 确认是否发送过消息
2. 检查数据库中是否有消息记录
3. 检查API返回数据格式
4. 查看前端控制台错误

## 📝 总结

私信功能已经完整实现，包括：
- ✅ 完整的后端API
- ✅ WebSocket实时通信
- ✅ 前端UI和交互
- ✅ 消息持久化
- ✅ 未读消息提示
- ✅ 会话管理

只需要执行数据库脚本并重新编译启动项目即可使用！
