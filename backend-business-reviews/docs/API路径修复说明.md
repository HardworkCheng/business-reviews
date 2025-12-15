# API路径修复说明

## 问题原因

前端调用的API路径与后端Controller定义的路径不匹配，导致请求被当作静态资源处理，返回 404 错误。

## 已修复的问题

### 1. 获取聊天记录接口

**错误的调用：**
```javascript
get(`/messages/conversations/${otherUserId}`, { pageNum, pageSize })
```

**正确的调用：**
```javascript
get(`/messages/chat/${otherUserId}`, { pageNum, pageSize })
```

**后端接口：**
```java
@GetMapping("/chat/{targetUserId}")
public Result<PageResult<MessageResponse>> getChatHistory(...)
```

### 2. 标记消息已读接口

**错误的调用：**
```javascript
post(`/messages/conversations/${conversationId}/read`)
```

**正确的调用：**
```javascript
post(`/messages/read/${targetUserId}`)
```

**后端接口：**
```java
@PostMapping("/read/{targetUserId}")
public Result<?> markAsRead(@PathVariable Long targetUserId)
```

### 3. 发送消息接口

**后端Controller修复：**

**修复前：**
```java
messageService.sendMessage(userId, 
    Long.parseLong(request.getTargetUserId()),  // 错误：字段不存在
    request.getContent(), 
    request.getType());  // 错误：字段不存在
```

**修复后：**
```java
messageService.sendMessage(userId, 
    request.getReceiverId(),  // 正确：使用 receiverId
    request.getContent(), 
    request.getMessageType());  // 正确：使用 messageType
```

### 4. 消息列表处理

**chat.vue 中的修复：**

添加了正确的 `isMine` 属性判断逻辑：

```javascript
const userInfo = uni.getStorageSync('userInfo')
const myUserId = userInfo?.userId || userInfo?.id

const processedMessages = result.list.map(msg => ({
    ...msg,
    isMine: msg.senderId?.toString() === myUserId?.toString()
})).reverse()
```

## 修改的文件

1. **front-business-reviews-Mobile/src/api/message.js**
   - 修复了 `getConversationMessages` 的URL
   - 修复了 `markAsRead` 的URL
   - 删除了不存在的 `deleteConversation` 函数

2. **backend-business-reviews/backend-business-reviews-mobile/src/main/java/com/businessreviews/controller/MessageController.java**
   - 修复了 `sendMessage` 方法中的字段引用

3. **front-business-reviews-Mobile/src/pages/chat/chat.vue**
   - 修复了 `loadMessages` 函数中的消息处理逻辑
   - 添加了正确的 `isMine` 属性判断

## 测试步骤

1. **重启后端服务**
   ```bash
   # 重新编译并启动
   ```

2. **刷新前端页面**
   - 清除浏览器缓存
   - 重新加载页面

3. **测试私信功能**
   - 进入用户主页
   - 点击"💬 私信"按钮
   - 发送消息
   - 查看消息是否正确显示

## 预期结果

- ✅ 能够正常加载聊天记录
- ✅ 能够正常发送消息
- ✅ 消息正确显示在左侧（对方）或右侧（自己）
- ✅ 不再出现 "No static resource" 错误

## 注意事项

1. **用户ID的获取**
   - 确保 localStorage 中存储的 userInfo 包含 userId 或 id 字段
   - 代码中已兼容两种字段名

2. **消息ID**
   - 确保后端返回的消息包含唯一的 id 字段
   - 用于消息列表的 key 和滚动定位

3. **时间格式**
   - 后端返回的 createdAt 应该是可以被 JavaScript Date 解析的格式
   - 建议使用 ISO 8601 格式：`2025-12-14T23:08:29`

## 如果仍有问题

请检查：

1. **后端日志**
   - 查看是否有其他异常
   - 确认请求是否到达 Controller

2. **前端控制台**
   - 查看 API 请求的完整 URL
   - 查看返回的数据格式

3. **数据库**
   - 确认 messages 表是否存在
   - 确认是否有测试数据

4. **网络请求**
   - 使用浏览器开发者工具的 Network 标签
   - 查看请求和响应的详细信息
