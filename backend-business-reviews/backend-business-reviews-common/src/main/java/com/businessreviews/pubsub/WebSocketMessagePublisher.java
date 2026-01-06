package com.businessreviews.pubsub;

import com.businessreviews.model.dto.WebSocketMessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * WebSocket 消息发布者
 * <p>
 * 通过 Redis Pub/Sub 向所有服务器实例广播 WebSocket 消息，
 * 解决多实例部署时用户可能连接在不同服务器上的消息路由问题。
 * </p>
 *
 * @author businessreviews
 */
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
     * 当前服务器ID（用于避免消息重复处理）
     */
    private static final String SERVER_ID = UUID.randomUUID().toString();

    /**
     * 发布 WebSocket 消息到 Redis 频道
     * <p>
     * 所有订阅该频道的服务器实例都会收到消息，
     * 然后各自检查目标用户是否在本地，如果在则推送。
     * </p>
     *
     * @param targetUserId 目标用户ID
     * @param message      消息内容
     */
    @SuppressWarnings("null")
    public void publishMessage(Long targetUserId, Map<String, Object> message) {
        try {
            WebSocketMessageDTO dto = new WebSocketMessageDTO(targetUserId, message, SERVER_ID);
            String json = objectMapper.writeValueAsString(dto);

            redisTemplate.convertAndSend(WEBSOCKET_CHANNEL, json);
            log.debug("WebSocket消息已发布到Redis: targetUserId={}, channel={}", targetUserId, WEBSOCKET_CHANNEL);

        } catch (JsonProcessingException e) {
            log.error("序列化WebSocket消息失败: targetUserId={}", targetUserId, e);
        }
    }

    /**
     * 获取当前服务器ID
     */
    public static String getServerId() {
        return SERVER_ID;
    }
}
