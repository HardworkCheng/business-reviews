package com.businessreviews.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.businessreviews.handler.MessageWebSocketHandler;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket配置类
 * 用于私信功能的实时通信
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    
    private final MessageWebSocketHandler messageWebSocketHandler;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器
        registry.addHandler(messageWebSocketHandler, "/ws")
                .setAllowedOrigins("*"); // 生产环境应该配置具体的域名
    }
}
