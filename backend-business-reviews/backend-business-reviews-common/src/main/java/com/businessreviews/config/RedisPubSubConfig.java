package com.businessreviews.config;

import com.businessreviews.pubsub.WebSocketMessagePublisher;
import com.businessreviews.pubsub.WebSocketMessageSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis Pub/Sub 配置
 * <p>
 * 配置 Redis 消息监听容器，用于 WebSocket 跨实例消息广播。
 * </p>
 *
 * @author businessreviews
 */
@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final WebSocketMessageSubscriber webSocketMessageSubscriber;

    /**
     * Redis 消息监听容器
     * <p>
     * 订阅 WebSocket 消息频道，实现多实例间的消息广播。
     * </p>
     */
    @Bean
    @SuppressWarnings("null")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 订阅 WebSocket 消息频道
        container.addMessageListener(
                webSocketMessageSubscriber,
                new ChannelTopic(WebSocketMessagePublisher.WEBSOCKET_CHANNEL));

        return container;
    }
}
