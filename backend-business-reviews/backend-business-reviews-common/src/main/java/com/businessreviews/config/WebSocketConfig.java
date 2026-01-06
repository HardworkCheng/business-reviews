package com.businessreviews.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.businessreviews.handler.MessageWebSocketHandler;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket全局配置类
 * <p>
 * 注册WebSocket处理器，配置跨域规则和拦截器
 * 用于支持私信等实时通信功能
 * </p>
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
