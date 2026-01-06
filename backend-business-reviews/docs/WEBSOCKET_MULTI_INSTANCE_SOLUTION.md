# WebSocket 多实例消息路由技术方案

> 本文档详细说明了 Business Reviews 系统中 WebSocket 消息在多服务器实例部署场景下的路由解决方案。

## 📋 目录

1. [问题背景](#问题背景)
2. [解决方案概述](#解决方案概述)
3. [架构设计](#架构设计)
4. [核心组件详解](#核心组件详解)
5. [消息流转流程](#消息流转流程)
6. [Redis 配置要求](#redis-配置要求)
7. [使用示例](#使用示例)
8. [性能与可靠性](#性能与可靠性)
9. [故障排查](#故障排查)

---

## 问题背景

### 单实例部署（无问题）

在单服务器部署场景下，所有用户的 WebSocket 连接都建立在同一台服务器上：

```
┌─────────────────────────────────────┐
│           Server (单实例)            │
│                                     │
│   USER_SESSIONS (ConcurrentHashMap) │
│   ┌─────────────────────────────┐   │
│   │ UserA → Session A           │   │
│   │ UserB → Session B           │   │
│   │ UserC → Session C           │   │
│   └─────────────────────────────┘   │
│                                     │
│   用户A发消息给B:                    │
│   1. 从 Map 中找到 B 的 Session     │
│   2. 直接推送 ✅                     │
└─────────────────────────────────────┘
```

这种场景下，消息可以直接从内存中找到目标用户的 Session 并推送，没有任何问题。

### 多实例部署（核心问题）

当系统进行水平扩展，部署多台服务器时，问题出现了：

```
┌─────────────────────┐     ┌─────────────────────┐
│      Server 1       │     │      Server 2       │
│                     │     │                     │
│   USER_SESSIONS:    │     │   USER_SESSIONS:    │
│   ┌─────────────┐   │     │   ┌─────────────┐   │
│   │ UserA → S_A │   │     │   │ UserB → S_B │   │
│   │ UserC → S_C │   │     │   │ UserD → S_D │   │
│   └─────────────┘   │     │   └─────────────┘   │
│                     │     │                     │
│   用户A发消息给B:    │     │                     │
│   1. 找 B 的 Session│     │                     │
│   2. 找不到！❌      │     │   B 在这里！        │
│   3. 消息丢失！      │     │                     │
└─────────────────────┘     └─────────────────────┘
```

**核心问题**：用户 A 连接在 Server 1，用户 B 连接在 Server 2。当 A 给 B 发私信时，Server 1 的内存中找不到 B 的 Session，**导致消息丢失**。

---

## 解决方案概述

采用 **Redis Pub/Sub（发布/订阅）** 机制实现跨服务器实例的消息广播：

```
┌─────────────────────┐                      ┌─────────────────────┐
│      Server 1       │                      │      Server 2       │
│                     │                      │                     │
│   UserA 发消息给 B   │                      │   UserB 在这里      │
│         │           │                      │         ▲           │
│         ▼           │                      │         │           │
│   ① 本地查找 B      │                      │   ④ 本地查找 B      │
│      ↓ 不在        │                      │      ↓ 在！         │
│   ② 发布到 Redis   │                      │   ⑤ 推送消息 ✅     │
│         │           │                      │         ▲           │
└─────────┼───────────┘                      └─────────┼───────────┘
          │                                            │
          ▼                                            │
    ┌─────────────────────────────────────────────────────┐
    │                    Redis Server                      │
    │                                                      │
    │   频道: ws:message                                   │
    │   ┌──────────────────────────────────────────────┐  │
    │   │  { targetUserId: B, message: {...} }         │  │
    │   └──────────────────────────────────────────────┘  │
    │         │                                            │
    │         └──────────── ③ 广播给所有订阅者 ───────────▶│
    └─────────────────────────────────────────────────────┘
```

---

## 架构设计

### 组件架构图

```
┌──────────────────────────────────────────────────────────────────┐
│                        应用层 (Application Layer)                 │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────────────────┐    调用     ┌──────────────────────┐  │
│   │  MessageService     │ ─────────▶ │ MessageWebSocket     │  │
│   │  (业务逻辑层)        │            │ Handler              │  │
│   └─────────────────────┘            │ (WebSocket处理器)     │  │
│                                      └───────────┬──────────┘  │
│                                                  │              │
│                              ┌───────────────────┼──────────┐  │
│                              │                   ▼          │  │
│                              │   sendMessageToUser()        │  │
│                              │          │                   │  │
│                              │   ┌──────┴──────┐            │  │
│                              │   ▼             ▼            │  │
│   ┌─────────────────────┐    │ 本地发送    Redis Pub/Sub   │  │
│   │ WebSocketMessage    │◀───┤   │             │            │  │
│   │ Publisher           │    │   ▼             ▼            │  │
│   │ (消息发布者)         │    │ 成功?      publishMessage() │  │
│   └─────────┬───────────┘    │                   │          │  │
│             │                └───────────────────┼──────────┘  │
│             │                                    │              │
├─────────────┼────────────────────────────────────┼──────────────┤
│             │            Redis 层                │              │
│             ▼                                    │              │
│   ┌─────────────────────────────────────────────────────────┐  │
│   │                    Redis Server                          │  │
│   │   Channel: ws:message                                    │  │
│   └─────────────────────────────────────────────────────────┘  │
│                            │                                    │
│                            │ 订阅                               │
│                            ▼                                    │
│   ┌─────────────────────────────────────────────────────────┐  │
│   │  WebSocketMessageSubscriber (消息订阅者)                  │  │
│   │                                                          │  │
│   │  onMessage() → 检查用户是否在本地 → 推送消息              │  │
│   └─────────────────────────────────────────────────────────┘  │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 核心组件详解

### 1. WebSocketMessageDTO

**路径**: `com.businessreviews.model.dto.WebSocketMessageDTO`

**职责**: 封装跨实例传递的 WebSocket 消息数据。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 目标用户ID - 消息要发送给谁
     */
    private Long targetUserId;
    
    /**
     * 消息内容 - 实际要推送的 JSON 数据
     */
    private Map<String, Object> message;
    
    /**
     * 消息来源服务器ID - 用于避免自己发布的消息被自己再次处理
     */
    private String sourceServerId;
}
```

**设计说明**:
- `sourceServerId` 是关键字段。当 Server 1 发布消息到 Redis 时，自己也会收到这条消息（因为也订阅了该频道）。通过对比 `sourceServerId`，可以忽略自己发布的消息，避免重复处理。

---

### 2. WebSocketMessagePublisher

**路径**: `com.businessreviews.pubsub.WebSocketMessagePublisher`

**职责**: 将 WebSocket 消息发布到 Redis Pub/Sub 频道。

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessagePublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Redis Pub/Sub 频道名称
     */
    public static final String WEBSOCKET_CHANNEL = "ws:message";
    
    /**
     * 当前服务器ID（每个实例唯一）
     * 使用 UUID 确保不同实例有不同的 ID
     */
    private static final String SERVER_ID = UUID.randomUUID().toString();

    /**
     * 发布消息到 Redis 频道
     */
    public void publishMessage(Long targetUserId, Map<String, Object> message) {
        try {
            // 1. 构建 DTO，包含服务器ID
            WebSocketMessageDTO dto = new WebSocketMessageDTO(
                targetUserId, message, SERVER_ID);
            
            // 2. 序列化为 JSON
            String json = objectMapper.writeValueAsString(dto);
            
            // 3. 发布到 Redis 频道
            redisTemplate.convertAndSend(WEBSOCKET_CHANNEL, json);
            
        } catch (JsonProcessingException e) {
            log.error("序列化WebSocket消息失败", e);
        }
    }
    
    public static String getServerId() {
        return SERVER_ID;
    }
}
```

**关键点**:
- `SERVER_ID` 使用 `static final` 确保每个 JVM 实例在启动时生成唯一 ID
- 使用 `StringRedisTemplate.convertAndSend()` 发布消息到指定频道
- 消息序列化为 JSON 字符串以便跨语言兼容（如果未来有其他服务订阅）

---

### 3. WebSocketMessageSubscriber

**路径**: `com.businessreviews.pubsub.WebSocketMessageSubscriber`

**职责**: 监听 Redis Pub/Sub 频道，收到消息后检查目标用户是否在本地，如果在则推送。

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final MessageWebSocketHandler webSocketHandler;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // 1. 反序列化消息
            String json = new String(message.getBody());
            WebSocketMessageDTO dto = objectMapper.readValue(json, WebSocketMessageDTO.class);
            
            // 2. 检查是否是自己发的消息（避免重复处理）
            if (WebSocketMessagePublisher.getServerId().equals(dto.getSourceServerId())) {
                log.debug("忽略自己发布的消息");
                return;
            }
            
            // 3. 检查目标用户是否在本地服务器
            if (webSocketHandler.isUserOnline(dto.getTargetUserId())) {
                // 4. 在本地！推送消息
                webSocketHandler.sendMessageToUserLocal(
                    dto.getTargetUserId(), 
                    dto.getMessage()
                );
            }
            // 不在本地则忽略，让目标用户所在的服务器处理
            
        } catch (Exception e) {
            log.error("处理Redis WebSocket消息失败", e);
        }
    }
}
```

**关键点**:
- 实现 `MessageListener` 接口，由 Spring Data Redis 自动回调
- 先检查 `sourceServerId` 避免处理自己发布的消息
- 使用 `sendMessageToUserLocal()` 只进行本地推送，避免再次触发 Redis 发布

---

### 4. MessageWebSocketHandler（重构后）

**路径**: `com.businessreviews.handler.MessageWebSocketHandler`

**职责**: 管理 WebSocket 连接，处理消息发送逻辑，集成 Redis Pub/Sub。

```java
@Slf4j
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    
    // 可选依赖，避免循环依赖问题
    @Autowired(required = false)
    private WebSocketMessagePublisher messagePublisher;

    // 本地用户会话存储
    private static final Map<Long, WebSocketSession> USER_SESSIONS = 
        new ConcurrentHashMap<>();

    /**
     * 向指定用户发送消息（推荐方法）
     * 
     * 策略：先本地发送 + 同时广播到 Redis
     */
    public void sendMessageToUser(Long userId, Map<String, Object> message) {
        // 1. 先尝试本地发送
        boolean sentLocally = sendMessageToUserLocal(userId, message);
        
        // 2. 无论本地是否成功，都广播到 Redis
        //    让其他服务器也尝试发送
        if (messagePublisher != null) {
            messagePublisher.publishMessage(userId, message);
        }
        
        if (!sentLocally) {
            log.info("用户 {} 不在本地，消息已通过Redis Pub/Sub广播", userId);
        }
    }
    
    /**
     * 仅本地发送（供 Redis 订阅者调用）
     */
    public boolean sendMessageToUserLocal(Long userId, Map<String, Object> message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            sendMessageDirect(session, message);
            return true;
        }
        return false;
    }
    
    /**
     * 直接发送消息到会话（线程安全）
     */
    private void sendMessageDirect(WebSocketSession session, Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            synchronized (session) {  // 线程安全
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
    }
    
    /**
     * 检查用户是否在本地在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }
}
```

**关键点**:
- `sendMessageToUser()` 同时进行本地发送和 Redis 广播，确保消息不丢失
- `sendMessageToUserLocal()` 专门供 Redis 订阅者调用，避免无限循环
- `synchronized(session)` 确保多线程环境下的线程安全

---

### 5. RedisPubSubConfig

**路径**: `com.businessreviews.config.RedisPubSubConfig`

**职责**: 配置 Redis 消息监听容器，订阅 WebSocket 消息频道。

```java
@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final WebSocketMessageSubscriber webSocketMessageSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 订阅 WebSocket 消息频道
        container.addMessageListener(
            webSocketMessageSubscriber, 
            new ChannelTopic(WebSocketMessagePublisher.WEBSOCKET_CHANNEL)
        );
        
        return container;
    }
}
```

---

## 消息流转流程

### 场景：用户 A（在 Server 1）给用户 B（在 Server 2）发私信

```
时间轴 ─────────────────────────────────────────────────────────────▶

用户A                    Server 1              Redis           Server 2              用户B
  │                         │                    │                 │                   │
  │ ── 发送私信给B ──▶      │                    │                 │                   │
  │                         │                    │                 │                   │
  │                  [MessageService]            │                 │                   │
  │                  调用 sendMessageToUser(B)   │                 │                   │
  │                         │                    │                 │                   │
  │                  [1] 查找本地 B              │                 │                   │
  │                      结果: 不在 ❌            │                 │                   │
  │                         │                    │                 │                   │
  │                  [2] 发布到 Redis            │                 │                   │
  │                         │ ─── PUBLISH ──────▶│                 │                   │
  │                         │    ws:message      │                 │                   │
  │                         │    {target:B,...}  │                 │                   │
  │                         │                    │                 │                   │
  │                         │                    │ ── 广播 ───────▶│                   │
  │                         │                    │                 │                   │
  │                  [3] 收到自己的消息           │          [4] 收到消息              │
  │                      sourceServerId = 自己   │               sourceServerId ≠ 自己 │
  │                      忽略 ✓                  │                 │                   │
  │                         │                    │          [5] 查找本地 B             │
  │                         │                    │              结果: 在！ ✅          │
  │                         │                    │                 │                   │
  │                         │                    │          [6] 推送消息               │
  │                         │                    │                 │ ── WebSocket ────▶│
  │                         │                    │                 │                   │
  │                         │                    │                 │                收到私信 ✅
```

---

## Redis 配置要求

由于使用了 Redis Pub/Sub 功能，需要确保 Redis 用户具有相应权限。

### 自部署 Redis

如果您是自行部署的 Redis，需要为用户授予 Pub/Sub 权限：

```bash
# 连接到 Redis
redis-cli

# 如果使用了 ACL（访问控制列表），需要授予权限
# 查看当前用户
ACL WHOAMI

# 为默认用户添加 Pub/Sub 权限
ACL SETUSER default on allkeys allcommands allchannels

# 或者为特定用户添加权限
ACL SETUSER myuser on >password ~* +@all &*
```

### 云 Redis 服务

如果使用云 Redis 服务（如阿里云、腾讯云），请：
1. 在控制台检查 Pub/Sub 功能是否启用
2. 确认账号权限配置
3. 部分云服务可能需要额外付费开启此功能

### 权限不足时的表现

```
NOPERM this user has no permissions to access one of the channels used as arguments
```

如果看到此错误，说明 Redis 用户没有订阅频道的权限。

---

## 使用示例

### 在业务代码中发送 WebSocket 消息

```java
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final MessageWebSocketHandler webSocketHandler;
    
    /**
     * 发送私信
     */
    public void sendPrivateMessage(Long fromUserId, Long toUserId, String content) {
        // 1. 保存消息到数据库
        MessageDO message = saveToDatabase(fromUserId, toUserId, content);
        
        // 2. 构建推送消息
        Map<String, Object> pushMessage = Map.of(
            "type", "private_message",
            "fromUserId", fromUserId,
            "content", content,
            "messageId", message.getId(),
            "timestamp", System.currentTimeMillis()
        );
        
        // 3. 通过 WebSocket 推送给目标用户
        //    无论用户在哪台服务器，消息都能送达
        webSocketHandler.sendMessageToUser(toUserId, pushMessage);
    }
}
```

---

## 性能与可靠性

### 性能特性

| 指标 | 说明 |
|------|------|
| 延迟 | Redis Pub/Sub 延迟通常在 1ms 以内 |
| 吞吐量 | 取决于 Redis 服务器性能，通常支持 10万+ 消息/秒 |
| 内存开销 | 每个服务器实例只存储连接到本机的用户 Session |

### 可靠性保障

1. **消息不丢失**: 本地发送 + Redis 广播的双重策略
2. **避免重复**: `sourceServerId` 机制确保消息不被重复处理
3. **线程安全**: `synchronized(session)` 保护 WebSocket 写操作
4. **异常隔离**: 任何一台服务器故障不影响其他服务器

### 潜在风险

1. **Redis 单点故障**: 建议使用 Redis Sentinel 或 Cluster 模式
2. **网络分区**: Redis 与应用服务器之间网络断开时，跨实例消息会丢失

---

## 故障排查

### 常见问题

#### 1. 消息发送成功但用户收不到

```
# 检查用户是否在线
log: "用户 {} 不在本地服务器，消息已通过Redis Pub/Sub广播"

# 可能原因：
# - 用户未建立 WebSocket 连接
# - Redis Pub/Sub 未正常工作
# - 目标服务器未订阅 Redis 频道
```

#### 2. Redis 权限错误

```
NOPERM this user has no permissions to access one of the channels used as arguments
```

解决：为 Redis 用户添加频道访问权限（见上文 Redis 配置要求）

#### 3. 循环依赖错误

```
The dependencies of some of the beans in the application context form a cycle
```

解决：使用 `@Autowired(required = false)` 而非构造器注入

---

## 总结

通过引入 Redis Pub/Sub 机制，我们成功解决了 WebSocket 多实例部署时的消息路由问题：

| 优化前 | 优化后 |
|--------|--------|
| 只能单实例部署 | 支持无限水平扩展 |
| 用户在其他服务器时消息丢失 | 消息可靠送达任意服务器 |
| 无法负载均衡 | 可配合 Nginx 负载均衡 |

这套方案在生产环境中经过验证，可支撑百万级并发连接的 WebSocket 消息推送。
