package com.businessreviews.handler;

import com.businessreviews.pubsub.WebSocketMessagePublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 * <p>
 * 核心职责：
 * 1. 管理用户WebSocket连接 Session（本地内存）
 * 2. 处理心跳机制（Heartbeat）
 * 3. 实现点对点（P2P）消息实时推送
 * 4. 通过 Redis Pub/Sub 支持多实例部署的跨服务器消息路由
 * </p>
 * 
 * <p>
 * 多实例部署架构说明：
 * 当用户 A 连在 Server 1，用户 B 连在 Server 2 时，
 * Server 1 发送消息给 B，会通过 Redis Pub/Sub 广播到所有服务器。
 * Server 2 收到消息后检查 B 是否在本地，如果在则推送。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // 使用 @Autowired 而非构造器注入，避免循环依赖
    @Autowired(required = false)
    private WebSocketMessagePublisher messagePublisher;

    // 存储用户ID和WebSocket会话的映射（本地内存）
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    public MessageWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从URL参数中获取用户ID
        String query = session.getUri().getQuery();
        Long userId = extractUserId(query);

        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            log.info("WebSocket连接建立: userId={}, sessionId={}, 当前本地在线用户数={}",
                    userId, session.getId(), USER_SESSIONS.size());

            // 发送连接成功消息
            sendMessageDirect(session, Map.of(
                    "type", "connected",
                    "message", "WebSocket连接成功",
                    "serverId", WebSocketMessagePublisher.getServerId()));
        } else {
            log.warn("无法获取用户ID，关闭连接: sessionId={}", session.getId());
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: sessionId={}, payload={}", session.getId(), payload);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");

            // 处理心跳消息
            if ("heartbeat".equals(type)) {
                sendMessageDirect(session, Map.of(
                        "type", "heartbeat",
                        "message", "pong"));
            }
            // 其他消息类型可以在这里处理
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除会话
        Long userId = findUserIdBySession(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("WebSocket连接关闭: userId={}, sessionId={}, status={}, 当前本地在线用户数={}",
                    userId, session.getId(), status, USER_SESSIONS.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: sessionId={}", session.getId(), exception);

        Long userId = findUserIdBySession(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
        }

        if (session.isOpen()) {
            session.close();
        }
    }

    /**
     * 向指定用户发送消息（推荐使用）
     * <p>
     * 此方法会：
     * 1. 先检查用户是否在本地服务器，如果在则直接推送
     * 2. 同时通过 Redis Pub/Sub 广播消息，让其他服务器也检查
     * 这种策略确保消息不会丢失，即使用户连接在其他服务器上。
     * </p>
     *
     * @param userId  目标用户ID
     * @param message 消息内容
     */
    public void sendMessageToUser(Long userId, Map<String, Object> message) {
        log.info("发送WebSocket消息: targetUserId={}, 本地在线用户数={}", userId, USER_SESSIONS.size());

        // 1. 先尝试本地发送
        boolean sentLocally = sendMessageToUserLocal(userId, message);

        // 2. 无论本地是否发送成功，都通过 Redis Pub/Sub 广播
        // 这样可以确保消息能到达其他服务器上的用户
        // （虽然有一定冗余，但保证了消息不丢失）
        if (messagePublisher != null) {
            messagePublisher.publishMessage(userId, message);
        } else {
            log.warn("WebSocketMessagePublisher 未注入，无法进行跨实例消息广播");
        }

        if (!sentLocally) {
            log.info("用户 {} 不在本地服务器，消息已通过Redis Pub/Sub广播", userId);
        }
    }

    /**
     * 向本地用户发送消息（仅本地）
     * <p>
     * 只检查本地内存中的 Session，不进行跨实例广播。
     * 供 Redis Pub/Sub 订阅者调用。
     * </p>
     *
     * @param userId  目标用户ID
     * @param message 消息内容
     * @return 是否发送成功（用户是否在本地在线）
     */
    public boolean sendMessageToUserLocal(Long userId, Map<String, Object> message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            log.info("向本地用户推送消息: userId={}, sessionId={}", userId, session.getId());
            sendMessageDirect(session, message);
            return true;
        }
        return false;
    }

    /**
     * 直接发送消息到WebSocket会话
     */
    private void sendMessageDirect(WebSocketSession session, Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * 从查询参数中提取用户ID
     */
    private Long extractUserId(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "userId".equals(keyValue[0])) {
                try {
                    return Long.parseLong(keyValue[1]);
                } catch (NumberFormatException e) {
                    log.error("解析用户ID失败: {}", keyValue[1]);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 根据会话查找用户ID
     */
    private Long findUserIdBySession(WebSocketSession session) {
        for (Map.Entry<Long, WebSocketSession> entry : USER_SESSIONS.entrySet()) {
            if (entry.getValue().getId().equals(session.getId())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 检查用户是否在本地服务器在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取本地在线用户数
     */
    public int getLocalOnlineUserCount() {
        return USER_SESSIONS.size();
    }
}
