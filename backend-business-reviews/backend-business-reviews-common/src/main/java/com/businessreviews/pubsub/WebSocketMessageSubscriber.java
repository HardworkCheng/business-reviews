package com.businessreviews.pubsub;

import com.businessreviews.handler.MessageWebSocketHandler;
import com.businessreviews.model.dto.WebSocketMessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * WebSocket 消息订阅者
 * <p>
 * 监听 Redis Pub/Sub 频道，收到消息后检查目标用户是否在本地服务器，
 * 如果在则通过本地 WebSocket Session 推送消息。
 * </p>
 *
 * @author businessreviews
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final MessageWebSocketHandler webSocketHandler;

    @Override
    public void onMessage(@org.springframework.lang.NonNull Message message,
            @org.springframework.lang.Nullable byte[] pattern) {
        try {
            String json = new String(message.getBody());
            WebSocketMessageDTO dto = objectMapper.readValue(json, WebSocketMessageDTO.class);

            // 检查是否是自己发的消息（避免重复处理）
            if (WebSocketMessagePublisher.getServerId().equals(dto.getSourceServerId())) {
                log.debug("忽略自己发布的消息: targetUserId={}", dto.getTargetUserId());
                return;
            }

            log.debug("收到Redis WebSocket消息: targetUserId={}, sourceServer={}",
                    dto.getTargetUserId(), dto.getSourceServerId());

            // 检查目标用户是否在本地服务器
            if (webSocketHandler.isUserOnline(dto.getTargetUserId())) {
                log.info("目标用户在本地服务器，推送消息: targetUserId={}", dto.getTargetUserId());
                webSocketHandler.sendMessageToUserLocal(dto.getTargetUserId(), dto.getMessage());
            } else {
                log.debug("目标用户不在本地服务器: targetUserId={}", dto.getTargetUserId());
            }

        } catch (Exception e) {
            log.error("处理Redis WebSocket消息失败", e);
        }
    }
}
