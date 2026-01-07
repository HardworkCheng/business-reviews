# Redis 序列化配置优化方案

> 本文档详细说明了 Business Reviews 系统中 Redis 序列化配置的优化方案，解决类路径依赖导致的缓存失效问题。

## 📋 目录

1. [问题背景](#问题背景)
2. [技术分析](#技术分析)
3. [解决方案](#解决方案)
4. [配置详解](#配置详解)
5. [使用指南](#使用指南)
6. [最佳实践](#最佳实践)
7. [迁移指南](#迁移指南)
8. [常见问题](#常见问题)

---

## 问题背景

### GenericJackson2JsonRedisSerializer 的隐患

Spring Data Redis 提供的 `GenericJackson2JsonRedisSerializer` 在序列化对象时，会在 JSON 中**嵌入类的全限定名**：

```json
{
  "@class": "com.businessreviews.model.vo.UserVO",
  "id": 1,
  "username": "张三",
  "phone": "138****5678"
}
```

这种设计的初衷是方便反序列化时自动识别类型，但带来了严重的**类路径依赖**问题。

### 典型故障场景

```
┌─────────────────────────────────────────────────────────────────────┐
│                        场景：包路径重构                              │
└─────────────────────────────────────────────────────────────────────┘

Day 1: 存入缓存
┌─────────────────────────────────────────────────────────────────────┐
│ 代码结构：com.businessreviews.model.vo.UserVO                        │
│                                                                      │
│ Redis 存储：                                                         │
│ {                                                                    │
│   "@class": "com.businessreviews.model.vo.UserVO",  ◀─ 记录类名     │
│   "id": 1,                                                           │
│   "username": "张三"                                                 │
│ }                                                                    │
└─────────────────────────────────────────────────────────────────────┘
         │
         │ 重构：将 VO 移动到新的包
         ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 新代码结构：com.businessreviews.dto.response.UserVO                  │
└─────────────────────────────────────────────────────────────────────┘
         │
         │ Day 2: 读取缓存
         ▼
┌─────────────────────────────────────────────────────────────────────┐
│ ❌ 报错！                                                            │
│                                                                      │
│ com.fasterxml.jackson.databind.exc.InvalidTypeIdException:          │
│ Could not resolve type id 'com.businessreviews.model.vo.UserVO'     │
│ as a subtype of `java.lang.Object`:                                 │
│ no such class found                                                  │
│                                                                      │
│ 原因：Redis 中存储的类名与当前代码的类名不匹配！                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 问题影响

| 影响范围 | 描述 |
|----------|------|
| 🔴 **服务不可用** | 缓存读取失败导致异常，可能雪崩 |
| 🔴 **数据丢失** | 已缓存的数据无法恢复使用 |
| 🟡 **跨服务不兼容** | 不同服务的类路径不同，无法共享缓存 |
| 🟡 **跨语言不兼容** | 其他语言（如 Python）无法解析 `@class` 字段 |

---

## 技术分析

### 序列化方式对比

| 序列化器 | 类型信息 | 类路径依赖 | 推荐度 |
|----------|----------|------------|--------|
| `GenericJackson2JsonRedisSerializer` | 包含 `@class` | ❌ 强依赖 | ⭐ |
| `Jackson2JsonRedisSerializer` | 不包含 | ✅ 无依赖 | ⭐⭐⭐⭐ |
| `StringRedisSerializer` + 手动序列化 | 不包含 | ✅ 无依赖 | ⭐⭐⭐⭐⭐ |
| `JdkSerializationRedisSerializer` | 包含 | ❌ 强依赖 | ⭐ |

### 为什么选择「无类型信息」方案？

```
┌─────────────────────────────────────────────────────────────────────┐
│               优化后的 JSON 结构（纯净）                             │
│                                                                      │
│ {                                                                    │
│   "id": 1,                                                           │
│   "username": "张三",                                                │
│   "phone": "138****5678"                                             │
│ }                                                                    │
│                                                                      │
│ ✅ 优点：                                                            │
│ - 类路径变更不影响已有数据                                           │
│ - 跨服务、跨语言可共享                                               │
│ - JSON 结构干净，便于调试                                            │
│                                                                      │
│ ⚠️ 需要注意：                                                        │
│ - 读取时需要明确指定目标类型                                         │
│ - 建议使用 RedisUtil 工具类统一封装                                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 解决方案

### 架构总览

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Redis 序列化架构                             │
└─────────────────────────────────────────────────────────────────────┘

                    ┌───────────────────────────┐
                    │      RedisConfig.java     │
                    │                           │
                    │  ┌─────────────────────┐  │
                    │  │ redisObjectMapper   │  │
                    │  │ (专用 ObjectMapper) │  │
                    │  └──────────┬──────────┘  │
                    │             │             │
                    │     ┌───────┴───────┐     │
                    │     ▼               ▼     │
                    │ ┌───────┐     ┌────────┐  │
                    │ │String │     │Jackson2│  │
                    │ │Redis  │     │Json    │  │
                    │ │Template     │Redis   │  │
                    │ │(推荐) │     │Serializer│ │
                    │ └───┬───┘     └────┬───┘  │
                    │     │              │      │
                    └─────┼──────────────┼──────┘
                          │              │
                          ▼              ▼
                    ┌───────────────────────────┐
                    │      RedisUtil.java       │
                    │                           │
                    │  setObject(key, obj, ttl) │
                    │  getObject(key, Class<T>) │
                    │                           │
                    │  内部使用 ObjectMapper    │
                    │  手动进行 JSON 序列化     │
                    └───────────────────────────┘
                                │
                                ▼
                    ┌───────────────────────────┐
                    │        Redis Server       │
                    │                           │
                    │  Key: "user:info:123"     │
                    │  Value: {"id":123,...}    │
                    │         (纯 JSON)         │
                    └───────────────────────────┘
```

---

## 配置详解

### RedisConfig.java

```java
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 专用 ObjectMapper - 不包含类型信息
     */
    @Bean(name = "redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 设置属性可见性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        // 注册 Java 8 时间模块
        objectMapper.registerModule(new JavaTimeModule());
        
        // 反序列化时忽略未知属性（提高版本兼容性）
        objectMapper.configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // 序列化时不包含 null 值
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // 日期格式：ISO-8601（不使用时间戳）
        objectMapper.configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // 【关键】不启用 DefaultTyping，避免嵌入类名
        // 这是解决类路径依赖问题的核心配置
        
        return objectMapper;
    }

    /**
     * StringRedisTemplate - 推荐使用
     */
    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * RedisTemplate - 兼容旧代码
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key：String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Value：Jackson（不含类型信息）
        Jackson2JsonRedisSerializer<Object> jsonSerializer = 
                new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(redisObjectMapper);
        
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```

### ObjectMapper 配置说明

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `FAIL_ON_UNKNOWN_PROPERTIES` | `false` | 忽略 JSON 中的未知字段，提高兼容性 |
| `Include.NON_NULL` | `true` | 不序列化 null 值，减少存储空间 |
| `WRITE_DATES_AS_TIMESTAMPS` | `false` | 日期使用 ISO-8601 格式，便于阅读 |
| `DefaultTyping` | **不启用** | 🔑 不在 JSON 中嵌入类名 |

---

## 使用指南

### 推荐方式：使用 RedisUtil

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final RedisUtil redisUtil;
    
    /**
     * 缓存用户信息
     */
    public void cacheUserInfo(Long userId, UserVO user) {
        String key = RedisKeyConstants.USER_INFO + userId;
        // 存储：对象 → JSON 字符串
        redisUtil.setObject(key, user, 3600); // TTL 1小时
    }
    
    /**
     * 获取缓存的用户信息
     */
    public UserVO getCachedUserInfo(Long userId) {
        String key = RedisKeyConstants.USER_INFO + userId;
        // 读取：JSON 字符串 → 指定类型
        return redisUtil.getObject(key, UserVO.class);
    }
}
```

### RedisUtil 核心方法

```java
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 存储对象（自动 JSON 序列化）
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
     * 获取对象（自动 JSON 反序列化）
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
}
```

### 使用 @Cacheable 注解

Spring Cache 注解同样使用优化后的序列化方式：

```java
@Service
public class CategoryService {

    @Cacheable(value = "categories", key = "'all'")
    public List<CategoryVO> getAllCategories() {
        // 首次调用：查询数据库
        // 后续调用：从 Redis 缓存获取
        return categoryMapper.selectAll();
    }
    
    @CacheEvict(value = "categories", key = "'all'")
    public void refreshCategories() {
        // 清除缓存
    }
}
```

---

## 最佳实践

### ✅ 推荐做法

```java
// 1. 使用 RedisUtil 进行缓存操作
redisUtil.setObject("user:123", userVO, 3600);
UserVO user = redisUtil.getObject("user:123", UserVO.class);

// 2. 使用 RedisKeyConstants 管理 Key
String key = RedisKeyConstants.USER_INFO + userId;

// 3. 为复杂类型使用 TypeReference
List<UserVO> users = objectMapper.readValue(json, 
    new TypeReference<List<UserVO>>() {});

// 4. 缓存前进行 Bean 属性复制（DO → VO）
UserVO vo = BeanUtil.copyProperties(userDO, UserVO.class);
redisUtil.setObject(key, vo, 3600);
```

### ❌ 避免的做法

```java
// 1. 不要直接使用 RedisTemplate 存储复杂对象
//    因为需要手动处理反序列化类型
redisTemplate.opsForValue().set("user:123", userVO);  // ❌

// 2. 不要缓存包含复杂嵌套类型的对象
//    （如 Entity 中的懒加载关联）
redisUtil.setObject("user:123", userDO);  // ❌ 可能包含 Hibernate 代理

// 3. 不要在 Key 中使用中文或特殊字符
redisUtil.setObject("用户:张三", userVO);  // ❌
```

---

## 迁移指南

### 从旧版本迁移

如果系统之前使用了 `GenericJackson2JsonRedisSerializer`，迁移时需要注意：

```
┌─────────────────────────────────────────────────────────────────────┐
│                          迁移策略                                    │
└─────────────────────────────────────────────────────────────────────┘

方案 A：清空旧缓存（推荐，简单）
─────────────────────────────────
1. 部署新版本代码
2. 执行 Redis FLUSHDB 或删除相关 Key
3. 系统自动重建缓存

方案 B：双读兼容（复杂，无损）
─────────────────────────────────
1. 新代码读取时先尝试新格式
2. 新格式失败则尝试旧格式（带 @class）
3. 读取成功后重新写入新格式
4. 等待旧缓存过期
```

### 清空缓存命令

```bash
# 连接 Redis
redis-cli -h <host> -p <port> -a <password>

# 方案 1：删除特定前缀的 Key
KEYS "user:*" | xargs redis-cli DEL
KEYS "shop:*" | xargs redis-cli DEL

# 方案 2：清空当前数据库（谨慎使用）
FLUSHDB

# 方案 3：清空所有数据库（非常危险）
FLUSHALL
```

---

## 常见问题

### 1. 如何处理泛型集合？

```java
// 问题：getObject(key, List.class) 无法正确反序列化泛型

// 解决：使用 TypeReference
String json = redisUtil.get(key);
if (json != null) {
    List<UserVO> users = objectMapper.readValue(json, 
        new TypeReference<List<UserVO>>() {});
}

// 或者在 RedisUtil 中添加泛型方法
public <T> T getObject(String key, TypeReference<T> typeReference) {
    String json = redisTemplate.opsForValue().get(key);
    if (json == null) return null;
    return objectMapper.readValue(json, typeReference);
}
```

### 2. LocalDateTime 序列化失败？

```java
// 确保 ObjectMapper 注册了 JavaTimeModule
objectMapper.registerModule(new JavaTimeModule());

// 同时添加依赖
// <dependency>
//     <groupId>com.fasterxml.jackson.datatype</groupId>
//     <artifactId>jackson-datatype-jsr310</artifactId>
// </dependency>
```

### 3. 如何查看 Redis 中的缓存内容？

```bash
# 查看 Key
redis-cli KEYS "user:*"

# 查看 Value（现在是纯 JSON，易于阅读）
redis-cli GET "user:info:123"

# 输出（优化后）：
# {"id":123,"username":"张三","phone":"138****5678"}

# 输出（优化前，带 @class）：
# {"@class":"com.businessreviews.model.vo.UserVO","id":123,...}
```

### 4. 性能影响？

| 指标 | 优化前 | 优化后 | 变化 |
|------|--------|--------|------|
| JSON 大小 | 较大（含@class） | 较小 | ↓ 约 10-20% |
| 序列化速度 | 快 | 快 | 基本无变化 |
| 反序列化速度 | 自动推断类型 | 明确指定类型 | 略快 |
| 兼容性 | 差 | 好 | ↑ 大幅提升 |

---

## 总结

通过本次优化，我们解决了 Redis 序列化的类路径依赖问题：

| 目标 | 实现效果 |
|------|----------|
| 消除类路径依赖 | JSON 不再包含 `@class` 字段 |
| 提高兼容性 | 类重构、包移动不影响已有缓存 |
| 跨服务共享 | 不同服务可以共享同一份缓存数据 |
| 便于调试 | JSON 结构干净，易于阅读 |
| 统一规范 | 通过 RedisUtil 提供统一的操作接口 |

这套方案在保证性能的同时，大幅提升了系统的健壮性和可维护性。
