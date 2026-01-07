package com.businessreviews.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 配置
 * <p>
 * 【重要】关于序列化方式的设计说明：
 * </p>
 * 
 * <h3>设计原则</h3>
 * <ol>
 * <li><strong>不使用 GenericJackson2JsonRedisSerializer</strong>
 * - 虽然方便，但会在 JSON 中嵌入类的全限定名（@class 字段）
 * - 如果类包路径重构，旧缓存数据反序列化会直接报错
 * - 跨服务/跨语言读取时不兼容
 * </li>
 * <li><strong>推荐使用 StringRedisTemplate + 手动 JSON 序列化</strong>
 * - 使用 RedisUtil 工具类进行操作
 * - 调用方明确知道存入/取出的类型，类型安全
 * - JSON 结构干净，不包含 Java 类元数据
 * - 类路径变更不影响已有缓存数据
 * </li>
 * <li><strong>缓存 Key 统一使用 String 序列化</strong>
 * - Key 必须是可读的字符串，便于排查和管理
 * - 所有 Key 都应使用 RedisKeyConstants 中定义的常量
 * </li>
 * </ol>
 * 
 * <h3>使用方式</h3>
 * 
 * <pre>
 * // 推荐：使用 RedisUtil 工具类（基于 StringRedisTemplate）
 * redisUtil.setObject("user:1", userVO, 3600);
 * UserVO user = redisUtil.getObject("user:1", UserVO.class);
 * 
 * // 也可以使用 RedisTemplate，但请确保正确使用
 * redisTemplate.opsForValue().set("key", value);
 * </pre>
 *
 * @author businessreviews
 */
@Configuration
@EnableCaching
public class RedisConfig {

        /**
         * 创建通用的 ObjectMapper 用于 Redis 序列化
         * <p>
         * 配置说明：
         * - 支持 Java 8 时间类型（LocalDateTime 等）
         * - 忽略未知属性，提高兼容性
         * - 不序列化 null 值，减少存储空间
         * - 不包含类型信息，避免类路径依赖
         * </p>
         */
        @Bean(name = "redisObjectMapper")
        public ObjectMapper redisObjectMapper() {
                ObjectMapper objectMapper = new ObjectMapper();

                // 设置属性可见性
                objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

                // 注册 Java 8 时间模块
                objectMapper.registerModule(new JavaTimeModule());

                // 反序列化时忽略未知属性（提高版本兼容性）
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                // 序列化时不包含 null 值
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

                // 日期格式处理：不使用时间戳，使用 ISO-8601 格式
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

                // 【重要】不启用 DefaultTyping，避免在 JSON 中嵌入类名
                // 这样即使类的包路径变更，已有的缓存数据仍可正常反序列化

                return objectMapper;
        }

        /**
         * StringRedisTemplate（推荐使用）
         * <p>
         * 所有缓存操作推荐通过 RedisUtil 使用此 Template。
         * Key 和 Value 都是 String，Value 需要手动进行 JSON 序列化/反序列化。
         * </p>
         */
        @Bean
        @Primary
        public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
                return new StringRedisTemplate(connectionFactory);
        }

        /**
         * RedisTemplate<String, Object>（兼容旧代码）
         * <p>
         * 保留此配置以兼容可能存在的旧代码。
         * 新代码请优先使用 StringRedisTemplate + RedisUtil。
         * </p>
         * 
         * <p>
         * 【注意】此配置使用 Jackson2JsonRedisSerializer 而非 GenericJackson2JsonRedisSerializer：
         * - 不会在 JSON 中嵌入 @class 字段
         * - 序列化为纯 JSON，类路径变更不影响已有数据
         * - 缺点是反序列化时需要明确指定类型
         * </p>
         */
        @Bean
        @SuppressWarnings("deprecation")
        public RedisTemplate<String, Object> redisTemplate(
                        RedisConnectionFactory connectionFactory,
                        ObjectMapper redisObjectMapper) {

                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // Key 序列化：使用 String
                StringRedisSerializer stringSerializer = new StringRedisSerializer();
                template.setKeySerializer(stringSerializer);
                template.setHashKeySerializer(stringSerializer);

                // Value 序列化：使用 Jackson2JsonRedisSerializer（不含类型信息）
                Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
                jsonSerializer.setObjectMapper(redisObjectMapper);

                template.setValueSerializer(jsonSerializer);
                template.setHashValueSerializer(jsonSerializer);

                template.afterPropertiesSet();
                return template;
        }

        /**
         * Spring Cache 缓存管理器
         * <p>
         * 用于 @Cacheable、@CachePut、@CacheEvict 等注解。
         * </p>
         */
        @Bean
        public CacheManager cacheManager(
                        RedisConnectionFactory connectionFactory,
                        ObjectMapper redisObjectMapper) {

                // 创建序列化器
                StringRedisSerializer stringSerializer = new StringRedisSerializer();
                Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
                jsonSerializer.setObjectMapper(redisObjectMapper);

                // 缓存配置
                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                // 默认过期时间 30 分钟
                                .entryTtl(Duration.ofMinutes(30))
                                // Key 序列化
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(stringSerializer))
                                // Value 序列化（不含类型信息的纯 JSON）
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(jsonSerializer))
                                // 不缓存 null 值
                                .disableCachingNullValues();

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(config)
                                .build();
        }
}
