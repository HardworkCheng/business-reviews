package com.businessreviews.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 设置字符串值
     */
    public void set(String key, String value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 设置字符串值（无过期时间）
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * 获取字符串值
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 设置对象值
     */
    public <T> void setObject(String key, T value, long timeout) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, timeout, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }
    
    /**
     * 获取对象值
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }
    
    /**
     * 删除键
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
    
    /**
     * 批量删除键
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }
    
    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout) {
        return redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }
    
    /**
     * 获取过期时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
    
    // ========== Set操作 ==========
    
    /**
     * 添加到Set
     */
    public Long sAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }
    
    /**
     * 从Set中移除
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }
    
    /**
     * 判断是否是Set成员
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }
    
    /**
     * 获取Set所有成员
     */
    public Set<String> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }
    
    /**
     * 获取Set大小
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }
    
    // ========== Hash操作 ==========
    
    /**
     * Hash设置值
     */
    public void hSet(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }
    
    /**
     * Hash获取值
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }
    
    /**
     * Hash删除值
     */
    public Long hDelete(String key, Object... hashKeys) {
        return redisTemplate.opsForHash().delete(key, hashKeys);
    }
    
    /**
     * Hash判断是否存在
     */
    public Boolean hHasKey(String key, String hashKey) {
        return redisTemplate.opsForHash().hasKey(key, hashKey);
    }
    
    // ========== 计数器操作 ==========
    
    /**
     * 自增
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
    
    /**
     * 自减
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }
}
