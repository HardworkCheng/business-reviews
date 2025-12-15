package com.businessreviews.config;

import com.businessreviews.handler.MessageWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 移动端WebSocket配置类
 * 用于私信功能的实时通信
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class MobileWebSocketConfig implements WebSocketConfigurer {
    
    private final MessageWebSocketHandler messageWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，路径需要包含context-path前缀
        // 因为WebSocket不会自动加上servlet的context-path
        registry.addHandler(messageWebSocketHandler, "/api/ws")
                .setAllowedOrigins("*");
    }
}
