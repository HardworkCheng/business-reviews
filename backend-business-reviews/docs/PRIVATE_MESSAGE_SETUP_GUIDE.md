# 私信功能完整实现指南

## 一、数据库设置

### 1. 执行SQL脚本
执行以下SQL文件创建必要的数据库表：
```bash
backend-business-reviews/sql/private_message_tables.sql
```

该脚本会创建3个表：
- `conversations`: 会话表
- `private_messages`: 私信消息表  
- `user_online_status`: 用户在线状态表

## 二、后端配置

### 1. 添加WebSocket依赖
在 `backend-business-reviews/pom.xml` 中添加：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. 已创建的文件
以下文件已经创建，无需手动创建：

**实体类：**
- `Conversation.java`
- `PrivateMessage.java`
- `ConversationItemResponse.java`
- `PrivateMessageResponse.java`
- `SendMessageRequest.java`

**Mapper：**
- `ConversationMapper.java`
- `PrivateMessageMapper.java`

### 3. 需要手动创建的后端文件

参考 `PRIVATE_MESSAGE_IMPLEMENTATION.md` 文档创建以下文件：

1. **Service接口和实现类**
   - `MessageService.java`
   - `MessageServiceImpl.java`

2. **Controller**
   - `MessageController.java`

3. **WebSocket配置**
   - `WebSocketConfig.java`
   - `WebSocketHandler.java`

## 三、前端配置（UniApp）

### 1. 已创建的文件
- `src/api/message.js` - 私信API
- `src/utils/websocket.js` - WebSocket管理器
- `src/pages/chat/chat.vue` - 聊天页面

### 2. 注册聊天页面

在 `front-business-reviews-Mobile/src/pages.json` 中添加：
```json
{
  "path": "pages/chat/chat",
  "style": {
    "navigationBarTitleText": "聊天",
    "navigationStyle": "custom"
  }
}
```

### 3. 修改用户主页添加私信按钮

找到 `front-business-reviews-Mobile/src/pages/user-profile/user-profile.vue`，在关注按钮旁边添加私信按钮：

```vue
<template>
  <!-- 在用户信息卡片中添加 -->
  <view class="action-buttons">
    <view class="follow-btn" @click="toggleFollow">
      <text>{{ isFollowing ? '已关注' : '+ 关注' }}</text>
    </view>
    <!-- 新增私信按钮 -->
    <view class="message-btn" @click="goToChat">
      <text>💬 私信</text>
    </view>
  </view>
</template>

<script setup>
// 添加跳转到聊天页面的方法
const goToChat = () => {
  uni.navigateTo({
    url: `/pages/chat/chat?userId=${userId.value}&username=${userProfile.value.username}&avatar=${userProfile.value.avatar}`
  })
}
</script>

<style lang="scss" scoped>
.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.message-btn {
  flex: 1;
  padding: 20rpx;
  background: white;
  border: 2rpx solid #FF9E64;
  border-radius: 40rpx;
  text-align: center;
  color: #FF9E64;
  font-size: 28rpx;
}
</style>
```

### 4. 修改消息页面添加聊天列表

修改 `front-business-reviews-Mobile/src/pages/message/message.vue`：

```vue
<template>
  <view class="container">
    <!-- 导航栏 -->
    <view class="navbar">
      <text class="nav-title">消息</text>
    </view>

    <!-- 标签页 -->
    <view class="tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'chat' }"
        @click="activeTab = 'chat'"
      >
        <text>聊天</text>
        <view v-if="unreadCount > 0" class="badge">{{ unreadCount }}</view>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'notification' }"
        @click="activeTab = 'notification'"
      >
        <text>通知</text>
      </view>
    </view>

    <!-- 聊天列表 -->
    <view v-if="activeTab === 'chat'" class="chat-list">
      <view 
        v-for="conversation in conversations" 
        :key="conversation.conversationId"
        class="chat-item"
        @click="goToChat(conversation)"
      >
        <image :src="conversation.otherAvatar" class="avatar"></image>
        <view class="chat-info">
          <view class="chat-header">
            <text class="username">{{ conversation.otherUsername }}</text>
            <text class="time">{{ formatTime(conversation.lastMessageTime) }}</text>
          </view>
          <view class="chat-content">
            <text class="last-message">{{ conversation.lastMessageContent }}</text>
            <view v-if="conversation.unreadCount > 0" class="unread-badge">
              {{ conversation.unreadCount }}
            </view>
          </view>
        </view>
      </view>
      
      <view v-if="conversations.length === 0" class="empty">
        <text>暂无聊天记录</text>
      </view>
    </view>

    <!-- 通知列表（原有内容） -->
    <view v-if="activeTab === 'notification'" class="notification-list">
      <!-- 原有的通知内容 -->
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getConversationList, getUnreadCount } from '../../api/message'
import websocket from '../../utils/websocket'

const activeTab = ref('chat')
const conversations = ref([])
const unreadCount = ref(0)

onMounted(() => {
  loadConversations()
  loadUnreadCount()
  connectWebSocket()
})

onShow(() => {
  // 每次显示页面时刷新
  loadConversations()
  loadUnreadCount()
})

// 加载会话列表
const loadConversations = async () => {
  try {
    const result = await getConversationList(1, 20)
    if (result && result.list) {
      conversations.value = result.list
    }
  } catch (e) {
    console.error('加载会话列表失败:', e)
  }
}

// 加载未读消息数
const loadUnreadCount = async () => {
  try {
    const count = await getUnreadCount()
    unreadCount.value = count || 0
  } catch (e) {
    console.error('加载未读消息数失败:', e)
  }
}

// 连接WebSocket
const connectWebSocket = () => {
  const token = uni.getStorageSync('token')
  const userInfo = uni.getStorageSync('userInfo')
  
  if (token && userInfo && userInfo.id) {
    websocket.connect(userInfo.id, token)
    websocket.onMessage((message) => {
      if (message.type === 'private_message') {
        // 刷新会话列表
        loadConversations()
        loadUnreadCount()
      }
    })
  }
}

// 跳转到聊天页面
const goToChat = (conversation) => {
  uni.navigateTo({
    url: `/pages/chat/chat?userId=${conversation.otherUserId}&username=${conversation.otherUsername}&avatar=${conversation.otherAvatar}`
  })
}

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (date.toDateString() === now.toDateString()) {
    return date.getHours() + ':' + String(date.getMinutes()).padStart(2, '0')
  }
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  return (date.getMonth() + 1) + '-' + date.getDate()
}
</script>

<style lang="scss" scoped>
.tabs {
  display: flex;
  background: white;
  padding: 0 30rpx;
}

.tab-item {
  flex: 1;
  padding: 30rpx 0;
  text-align: center;
  position: relative;
  font-size: 32rpx;
  color: #666;
  
  &.active {
    color: #FF9E64;
    border-bottom: 4rpx solid #FF9E64;
  }
}

.badge {
  position: absolute;
  top: 20rpx;
  right: 30%;
  background: #FF4444;
  color: white;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  min-width: 32rpx;
  text-align: center;
}

.chat-list {
  background: white;
}

.chat-item {
  display: flex;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  margin-right: 20rpx;
}

.chat-info {
  flex: 1;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}

.username {
  font-size: 32rpx;
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 24rpx;
  color: #999;
}

.chat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  font-size: 28rpx;
  color: #666;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  background: #FF4444;
  color: white;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
  min-width: 32rpx;
  text-align: center;
  margin-left: 20rpx;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999;
  font-size: 28rpx;
}
</style>
```

### 5. 修改笔记详情页，点击头像跳转到用户主页

在 `front-business-reviews-Mobile/src/pages/note-detail/note-detail.vue` 中：

```vue
<!-- 在作者信息部分添加点击事件 -->
<view class="author-info" @click="goToUserProfile">
  <image :src="noteDetail.authorAvatar" class="author-avatar"></image>
  <text class="author-name">{{ noteDetail.author }}</text>
</view>

<script setup>
// 添加跳转方法
const goToUserProfile = () => {
  if (noteDetail.value.authorId) {
    uni.navigateTo({
      url: `/pages/user-profile/user-profile?userId=${noteDetail.value.authorId}`
    })
  }
}
</script>
```

### 6. 修改关注/粉丝列表，添加私信按钮

在 `front-business-reviews-Mobile/src/pages/user-list/user-list.vue` 中：

```vue
<view class="list-item">
  <!-- ... 其他内容 ... -->
  <view class="action-buttons">
    <view class="follow-btn" @click.stop="toggleFollow(item)">
      <text>{{ item.isFollowing ? '已关注' : '+ 关注' }}</text>
    </view>
    <view class="message-btn" @click.stop="goToChat(item)">
      <text>💬</text>
    </view>
  </view>
</view>

<script setup>
// 添加跳转到聊天的方法
const goToChat = (user) => {
  uni.navigateTo({
    url: `/pages/chat/chat?userId=${user.id}&username=${user.username}&avatar=${user.avatar}`
  })
}
</script>

<style lang="scss" scoped>
.action-buttons {
  display: flex;
  gap: 10rpx;
}

.message-btn {
  padding: 15rpx 20rpx;
  border-radius: 30rpx;
  background: white;
  border: 2rpx solid #FF9E64;
  font-size: 26rpx;
  color: #FF9E64;
}
</style>
```

## 四、测试步骤

### 1. 后端测试
1. 执行SQL脚本创建数据库表
2. 启动后端服务
3. 使用Postman测试API接口：
   - GET `/api/messages/conversations` - 获取会话列表
   - GET `/api/messages/conversations/{otherUserId}` - 获取聊天记录
   - POST `/api/messages/send` - 发送消息

### 2. 前端测试
1. 重新编译UniApp项目
2. 测试以下功能：
   - 点击笔记作者头像跳转到用户主页
   - 用户主页点击私信按钮进入聊天页面
   - 发送消息
   - 接收消息（需要两个账号测试）
   - 消息页查看聊天列表
   - 未读消息提示

### 3. WebSocket测试
1. 打开浏览器开发者工具
2. 查看Network标签的WS连接
3. 确认WebSocket连接成功
4. 测试消息实时推送

## 五、注意事项

1. **WebSocket连接地址**：
   - 开发环境：`ws://localhost:8080/api/ws`
   - 生产环境需要改为：`wss://your-domain.com/api/ws`

2. **认证**：
   - WebSocket连接需要传递token进行认证
   - 确保token有效期足够长

3. **消息持久化**：
   - 所有消息都会保存到数据库
   - 离线消息会在用户上线时推送

4. **性能优化**：
   - 聊天记录分页加载
   - WebSocket心跳保持连接
   - 消息列表虚拟滚动（可选）

5. **错误处理**：
   - WebSocket断线自动重连
   - 消息发送失败重试
   - 网络异常提示

## 六、后续优化建议

1. 添加图片消息支持
2. 添加语音消息支持
3. 添加消息撤回功能
4. 添加消息已读回执
5. 添加输入状态提示（正在输入...）
6. 添加消息搜索功能
7. 添加聊天记录导出功能
8. 添加消息推送通知
