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
 * Redis 缓存操作工具类
 * <p>
 * 封装了Spring Data Redis的常用操作，提供简化的API
 * 支持 String, Object(JSON), Set, Hash 等数据类型的 CRUD
 * </p>
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

    // ========== 批量操作 ==========

    /**
     * 批量获取字符串值
     * 
     * @param keys Key列表
     * @return 值列表（按Key顺序返回，不存在的Key对应值为null）
     */
    public java.util.List<String> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量设置字符串值（无过期时间）
     * 
     * @param map Key-Value映射
     */
    public void multiSet(java.util.Map<String, String> map) {
        redisTemplate.opsForValue().multiSet(map);
    }

    /**
     * 批量获取对象值
     * 
     * @param keys  Key列表
     * @param clazz 对象类型
     * @return 对象列表（按Key顺序返回，不存在或反序列化失败的Key对应值为null）
     */
    public <T> java.util.List<T> multiGetObjects(Collection<String> keys, Class<T> clazz) {
        java.util.List<String> jsonList = redisTemplate.opsForValue().multiGet(keys);
        if (jsonList == null) {
            return new java.util.ArrayList<>();
        }

        java.util.List<T> result = new java.util.ArrayList<>();
        for (String json : jsonList) {
            if (json == null) {
                result.add(null);
            } else {
                try {
                    result.add(objectMapper.readValue(json, clazz));
                } catch (JsonProcessingException e) {
                    result.add(null); // 反序列化失败时返回null
                }
            }
        }
        return result;
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
    @SuppressWarnings("null")
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 自减
     */
    @SuppressWarnings("null")
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ========== GEO 地理位置操作 ==========

    /**
     * 添加地理位置
     * 
     * @param key       Redis Key
     * @param longitude 经度
     * @param latitude  纬度
     * @param member    成员名称（如商家ID）
     * @return 添加成功的数量
     */
    @SuppressWarnings("null")
    public Long geoAdd(String key, double longitude, double latitude, String member) {
        return redisTemplate.opsForGeo().add(key,
                new org.springframework.data.geo.Point(longitude, latitude), member);
    }

    /**
     * 批量添加地理位置
     * 
     * @param key       Redis Key
     * @param locations 地理位置映射 (memberId -> Point)
     * @return 添加成功的数量
     */
    @SuppressWarnings("null")
    public Long geoAddAll(String key, java.util.Map<String, org.springframework.data.geo.Point> locations) {
        if (locations == null || locations.isEmpty()) {
            return 0L;
        }

        java.util.List<org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation<String>> geoLocations = locations
                .entrySet().stream()
                .map(entry -> new org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation<>(
                        entry.getKey(), entry.getValue()))
                .collect(java.util.stream.Collectors.toList());

        return redisTemplate.opsForGeo().add(key, geoLocations);
    }

    /**
     * 查找指定范围内的成员（按距离升序排序）
     * 
     * @param key       Redis Key
     * @param longitude 中心点经度
     * @param latitude  中心点纬度
     * @param radius    搜索半径（公里）
     * @param limit     返回数量限制
     * @return 包含成员ID和距离的结果列表
     */
    public java.util.List<org.springframework.data.geo.GeoResult<org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation<String>>> geoRadius(
            String key, double longitude, double latitude, double radius, long limit) {

        org.springframework.data.geo.Circle circle = new org.springframework.data.geo.Circle(
                new org.springframework.data.geo.Point(longitude, latitude),
                new org.springframework.data.geo.Distance(radius,
                        org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit.KILOMETERS));

        org.springframework.data.redis.connection.RedisGeoCommands.GeoRadiusCommandArgs args = org.springframework.data.redis.connection.RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(limit);

        org.springframework.data.geo.GeoResults<org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation<String>> results = redisTemplate
                .opsForGeo().radius(key, circle, args);

        if (results == null) {
            return java.util.Collections.emptyList();
        }

        return results.getContent();
    }

    /**
     * 删除地理位置成员
     */
    @SuppressWarnings("null")
    public Long geoRemove(String key, String... members) {
        return redisTemplate.opsForGeo().remove(key, members);
    }
}
