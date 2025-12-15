package com.businessreviews.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 处理私信的实时推送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper;
    
    // 存储用户ID和WebSocket会话的映射
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从URL参数中获取用户ID
        String query = session.getUri().getQuery();
        Long userId = extractUserId(query);
        
        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            log.info("WebSocket连接建立: userId={}, sessionId={}", userId, session.getId());
            
            // 发送连接成功消息
            sendMessage(session, Map.of(
                "type", "connected",
                "message", "WebSocket连接成功"
            ));
        } else {
            log.warn("无法获取用户ID，关闭连接: sessionId={}", session.getId());
            session.close();
        }
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到WebSocket消息: sessionId={}, payload={}", session.getId(), payload);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");
            
            // 处理心跳消息
            if ("heartbeat".equals(type)) {
                sendMessage(session, Map.of(
                    "type", "heartbeat",
                    "message", "pong"
                ));
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
            log.info("WebSocket连接关闭: userId={}, sessionId={}, status={}", userId, session.getId(), status);
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
     * 向指定用户发送消息
     */
    public void sendMessageToUser(Long userId, Map<String, Object> message) {
        log.info("尝试向用户发送WebSocket消息: userId={}, 当前在线用户数={}", userId, USER_SESSIONS.size());
        log.info("当前在线用户列表: {}", USER_SESSIONS.keySet());
        
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            log.info("找到用户会话，准备发送消息: userId={}, sessionId={}", userId, session.getId());
            sendMessage(session, message);
            log.info("消息发送完成: userId={}", userId);
        } else {
            log.info("用户不在线或会话已关闭: userId={}, session={}, isOpen={}", 
                userId, session != null ? session.getId() : "null", 
                session != null ? session.isOpen() : "N/A");
        }
    }
    
    /**
     * 发送消息到WebSocket会话
     */
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
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
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }
}
