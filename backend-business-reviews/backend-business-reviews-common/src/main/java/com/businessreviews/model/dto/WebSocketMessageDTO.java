package com.businessreviews.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * WebSocket 消息传输对象
 * <p>
 * 用于 Redis Pub/Sub 跨实例传递 WebSocket 消息
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 目标用户ID
     */
    private Long targetUserId;

    /**
     * 消息内容
     */
    private Map<String, Object> message;

    /**
     * 消息来源服务器ID（用于避免重复处理）
     */
    private String sourceServerId;
}
