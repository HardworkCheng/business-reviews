# 私信功能实现文档

## 功能概述
实现用户之间的即时私信功能，包括：
1. 点击用户头像进入用户主页
2. 用户主页添加"私信"按钮
3. 私信聊天页面（支持即时通信）
4. 消息页显示私信列表
5. WebSocket实时推送消息

## 数据库表
已创建SQL文件：`backend-business-reviews/sql/private_message_tables.sql`

包含三个表：
- `conversations`: 会话表
- `private_messages`: 私信消息表
- `user_online_status`: 用户在线状态表

## 后端实现

### 1. 实体类（已创建）
- `Conversation.java`: 会话实体
- `PrivateMessage.java`: 私信消息实体
- `ConversationItemResponse.java`: 会话列表响应
- `PrivateMessageResponse.java`: 私信消息响应
- `SendMessageRequest.java`: 发送消息请求

### 2. Mapper（已创建）
- `ConversationMapper.java`
- `PrivateMessageMapper.java`

### 3. Service层（需要创建）

#### MessageService.java
```java
package com.businessreviews.service;

import com.businessreviews.common.PageResult;
import com.businessreviews.dto.request.SendMessageRequest;
import com.businessreviews.dto.response.ConversationItemResponse;
import com.businessreviews.dto.response.PrivateMessageResponse;

public interface MessageService {
    
    /**
     * 获取会话列表
     */
    PageResult<ConversationItemResponse> getConversationList(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 获取会话详情（聊天记录）
     */
    PageResult<PrivateMessageResponse> getConversationMessages(Long userId, Long otherUserId, Integer pageNum, Integer pageSize);
    
    /**
     * 发送消息
     */
    PrivateMessageResponse sendMessage(Long senderId, SendMessageRequest request);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long userId, Long conversationId);
    
    /**
     * 获取未读消息总数
     */
    Integer getUnreadCount(Long userId);
    
    /**
     * 删除会话
     */
    void deleteConversation(Long userId, Long conversationId);
}
```

### 4. Controller层（需要创建）

#### MessageController.java
```java
package com.businessreviews.controller;

import com.businessreviews.common.PageResult;
import com.businessreviews.common.Result;
import com.businessreviews.context.UserContext;
import com.businessreviews.dto.request.SendMessageRequest;
import com.businessreviews.dto.response.ConversationItemResponse;
import com.businessreviews.dto.response.PrivateMessageResponse;
import com.businessreviews.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final MessageService messageService;
    
    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<PageResult<ConversationItemResponse>> getConversationList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<ConversationItemResponse> result = messageService.getConversationList(userId, pageNum, pageSize);
        return Result.success(result);
    }
    
    /**
     * 获取聊天记录
     */
    @GetMapping("/conversations/{otherUserId}")
    public Result<PageResult<PrivateMessageResponse>> getConversationMessages(
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {
        Long userId = UserContext.requireUserId();
        PageResult<PrivateMessageResponse> result = messageService.getConversationMessages(userId, otherUserId, pageNum, pageSize);
        return Result.success(result);
    }
    
    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Result<PrivateMessageResponse> sendMessage(@RequestBody @Valid SendMessageRequest request) {
        Long senderId = UserContext.requireUserId();
        PrivateMessageResponse response = messageService.sendMessage(senderId, request);
        return Result.success(response);
    }
    
    /**
     * 标记消息为已读
     */
    @PostMapping("/conversations/{conversationId}/read")
    public Result<?> markAsRead(@PathVariable Long conversationId) {
        Long userId = UserContext.requireUserId();
        messageService.markAsRead(userId, conversationId);
        return Result.success("标记成功");
    }
    
    /**
     * 获取未读消息总数
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.requireUserId();
        Integer count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/conversations/{conversationId}")
    public Result<?> deleteConversation(@PathVariable Long conversationId) {
        Long userId = UserContext.requireUserId();
        messageService.deleteConversation(userId, conversationId);
        return Result.success("删除成功");
    }
}
```

### 5. WebSocket配置（需要创建）

#### WebSocketConfig.java
```java
package com.businessreviews.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，消息前缀为/topic和/queue
        registry.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息的前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 点对点消息前缀
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

## 前端实现（UniApp）

### 1. API文件

#### message.js
```javascript
import { get, post, del } from './request'

/**
 * 获取会话列表
 */
export const getConversationList = (pageNum = 1, pageSize = 20) => {
  return get('/messages/conversations', { pageNum, pageSize })
}

/**
 * 获取聊天记录
 */
export const getConversationMessages = (otherUserId, pageNum = 1, pageSize = 50) => {
  return get(`/messages/conversations/${otherUserId}`, { pageNum, pageSize })
}

/**
 * 发送消息
 */
export const sendMessage = (data) => {
  return post('/messages/send', data)
}

/**
 * 标记消息为已读
 */
export const markAsRead = (conversationId) => {
  return post(`/messages/conversations/${conversationId}/read`)
}

/**
 * 获取未读消息总数
 */
export const getUnreadCount = () => {
  return get('/messages/unread-count')
}

/**
 * 删除会话
 */
export const deleteConversation = (conversationId) => {
  return del(`/messages/conversations/${conversationId}`)
}

export default {
  getConversationList,
  getConversationMessages,
  sendMessage,
  markAsRead,
  getUnreadCount,
  deleteConversation
}
```

### 2. 页面文件

需要创建以下页面：
1. `pages/user-profile/user-profile.vue` - 用户主页（添加私信按钮）
2. `pages/chat/chat.vue` - 聊天页面
3. `pages/message/message.vue` - 修改消息页面，添加聊天标签

### 3. WebSocket连接管理

#### utils/websocket.js
```javascript
class WebSocketManager {
  constructor() {
    this.ws = null
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.messageHandlers = []
  }
  
  connect(userId, token) {
    const wsUrl = `ws://localhost:8080/api/ws?userId=${userId}&token=${token}`
    
    this.ws = new WebSocket(wsUrl)
    
    this.ws.onopen = () => {
      console.log('WebSocket连接成功')
      this.startHeartbeat()
    }
    
    this.ws.onmessage = (event) => {
      const message = JSON.parse(event.data)
      this.messageHandlers.forEach(handler => handler(message))
    }
    
    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
    }
    
    this.ws.onclose = () => {
      console.log('WebSocket连接关闭')
      this.stopHeartbeat()
      this.reconnect(userId, token)
    }
  }
  
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    }
  }
  
  onMessage(handler) {
    this.messageHandlers.push(handler)
  }
  
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      this.send({ type: 'heartbeat' })
    }, 30000)
  }
  
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
    }
  }
  
  reconnect(userId, token) {
    if (this.reconnectTimer) return
    
    this.reconnectTimer = setTimeout(() => {
      console.log('尝试重新连接WebSocket')
      this.connect(userId, token)
      this.reconnectTimer = null
    }, 5000)
  }
  
  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }
  }
}

export default new WebSocketManager()
```

## 实现步骤

### 后端步骤：
1. 执行SQL脚本创建数据库表
2. 添加Spring WebSocket依赖到pom.xml
3. 创建Service实现类
4. 创建WebSocket配置和处理器
5. 测试API接口

### 前端步骤：
1. 创建message.js API文件
2. 创建WebSocket管理工具
3. 修改用户主页，添加私信按钮
4. 创建聊天页面
5. 修改消息页面，添加聊天列表
6. 测试功能

## 注意事项

1. WebSocket需要在pom.xml中添加依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

2. 需要处理用户认证，确保WebSocket连接时验证token

3. 消息推送需要根据用户在线状态决定是否实时推送

4. 考虑消息的持久化和离线消息处理

5. 前端需要处理WebSocket断线重连

6. 考虑消息的已读/未读状态同步
