# 后端性能与架构优化清单 (Backend Optimization Plan)

经过对核心业务代码 (`NoteServiceImpl`, `ShopServiceImpl`, `CommentServiceImpl`) 及数据库设计的深度分析，我整理了以下优化清单。这些优化点按**优先级**排序，建议优先解决性能瓶颈。

---

## 🚀 P0: 核心性能瓶颈 (必须修复)

### 1. 解决 N+1 查询问题 (N+1 Query Problem)
**现状**: 在列表查询接口中，先查询主数据（如笔记列表），然后在循环中逐条查询关联数据（用户信息、店铺信息）。这会导致数据库连接数瞬间飙升。
- **NoteServiceImpl.java**:
  - `convertToNoteItem` 方法中，每次都调用 `userMapper.selectById` 和 `shopMapper.selectById`。
  - **影响**: 访问一次推荐列表(20条)，实际执行 1 + 20(用户) + 20(店铺) = 41 次 SQL 查询。
- **ShopServiceImpl.java**:
  - `convertToShopItem` 方法中，每次循环调用 `categoryMapper.selectById`。
  - `getShopReviews` 方法中，每次循环调用 `userMapper.selectById`。
  - `getShopNotes` 方法中，每次循环调用 `userMapper.selectById`。
- **CommentServiceImpl.java**:
  - `convertToResponse` 方法中，每次循环调用 `userMapper.selectById`。

**优化方案**:
- 使用 **In-Memory Map 组装** 模式。
- **步骤**:
  1. 获取主列表 (List<NoteDO>)。
  2. 提取所有涉及的 `userId` 和 `shopId` 到 Set 集合中。
  3. 使用 `selectBatchIds` (MyBatis-Plus) 一次性查询所有 User 和 Shop。
  4. 将结果转为 `Map<Long, UserDO>` 和 `Map<Long, ShopDO>`。
  5. 在循环中直接从 Map 获取数据，将复杂度从 O(N) 降为 O(1)。

### 2. 批量写入优化
**现状**: 在发布笔记 (`publishNote`) 和更新笔记时，标签 (`saveNoteTags`) 和话题 (`saveNoteTopics`) 是在循环中逐条执行 `insert`。
**优化方案**:
- 使用 MyBatis-Plus 的 `saveBatch` 功能或在 Mapper XML 中编写 `<foreach>` 批量插入语句，显著减少数据库交互次数。

---

## ⚡ P1: 缓存与逻辑优化

### 3. 实现真正的 Redis 缓存
**现状**: `NoteServiceImpl.getRecommendedNotes` 中定义了 `cacheKey` 变量，但**并未编写实际的缓存读写逻辑**，紧接着直接查询了数据库。
**优化方案**:
- **读缓存**: 先查 Redis，若存在直接反序列化返回。
- **写缓存**: 若 Redis 未命中，查 DB，然后 `redisUtil.set(key, value, expireTime)`。
- **一致性**: 在发布/删除/点赞笔记时，考虑是否需要主动失效相关缓存列表（或接受短时间的一致性延迟）。
- **对象**: 对 `ShopDetail` (商家详情) 和 `CategoryList` (分类列表) 这种读多写少的数据，必须加上缓存。

### 4. 附近的商家 (Geo) 算法优化
**现状**: `ShopServiceImpl.getNearbyShops` 目前逻辑是：先分页查询所有状态正常的商家（按评分排序），然后在**内存中**计算距离并格式化。
- **问题**: 这**不是**真正的"附近搜索"。如果第 2 页的商家比第 1 页的离我更近，用户永远看不见。当前的逻辑实际上是"按评分推荐，顺便告诉你有多远"。
**优化方案**:
- **方案 A (简单)**: 利用 MySQL 5.7+ 的 `ST_Distance_Sphere` 函数进行空间排序。
- **方案 B (高性能)**: 使用 **Redis GEO** (GeoAdd, GeoRadius) 存储商家经纬度，实现毫秒级“查找附近的店”。

---

## 🔒 P2+: 安全与稳定性增强

### 5. WebSocket 内存泄漏风险与横向扩展限制
**现状**: `MessageWebSocketHandler` 使用 `ConcurrentHashMap<Long, WebSocketSession> USER_SESSIONS` 在**本地内存**中存储 WebSocket 会话。
- **内存风险**: 如果单机连接数过大，内存可能溢出。虽然有 `afterConnectionClosed` 移除会话，但异常断开等情况需确保清理逻辑极度健壮。
- **扩展限制**: 这是最大的问题。如果有 2 台后端服务器，用户 A 连在 Server 1，用户 B 连在 Server 2。用户 A 给 B 发私信时，Server 1 的内存里找不到 B 的 Session，**导致消息丢失**。
**优化方案**:
- **引入 Redis Pub/Sub**: 当 Server 1 需要给用户 B 发消息时，向 Redis 频道 `ws:message` 发布消息。所有 Server 订阅该频道，收到消息后检查用户 B 是否在自己的内存中，如果在则推送。这样可完美支持多实例部署。

### 6. 敏感数据处理
**现状**: `UserServiceImpl` 等地方虽然用 `DefaultAvatar` 处理头像，但在日志 (`log.info`) 和部分 API 响应中，并没有对手机号进行严格的**脱敏处理**。
**优化方案**:
- 创建 `@Sensitive` 注解和对应的 Jackson Serializer，在 JSON 序列化阶段自动把手机号中间四位变成 `****`。

### 7. Redis 序列化配置隐患
**现状**: `RedisConfig` 使用了 `GenericJackson2JsonRedisSerializer`。虽然灵活，但如果 Redis 中存储的 Java 类名发生重构或包路径变更，**反序列化会直接报错**，导致缓存数据不可用甚至服务异常。
**优化方案**:
- 建议手动指定 Key 的序列化方式，对于 Value，如果对性能和版本兼容性要求极其严格，可以考虑自定义序列化器，或者确保 Model 类路径相对稳定。

---

## 🛠 P2: 代码质量与维护性

### 8. 消除"魔法数字" (Magic Numbers)
**现状**: 代码中充斥着大量的 `1` (正常), `2` (隐藏/商家笔记), `0` (删除/未点赞)。虽然有 Enum 类，但并未完全利用。
- `note.setStatus(1)` -> `note.setStatus(NoteStatus.NORMAL.getCode())`
- `item.setNoteType(1)` -> `item.setNoteType(NoteType.USER.getCode())`
**优化方案**: 将所有硬编码数字替换为对应的 Enum 枚举引用，提高代码可读性。

### 9. 数据库索引复核
**现状**: 虽然定义了基础索引，但部分高频组合查询可能未命中索引。
**建议**:
- **用户笔记列表**: `getUserNotes` 使用了 `.eq(userId).eq(status).orderByDesc(createdAt)`。
  - 需要联合索引: `idx_user_status_create (user_id, status, created_at)`。
- **推荐排序**: 推荐流使用了 `status=1` AND `type in (1,2)` ORDER BY `recommend` DESC, `like` DESC。
  - 建议索引: `idx_status_rec_like (status, recommend, like_count)`。

### 10. 重复的常量类
**现状**: 项目中同时存在 `Constants` (在 common 包与废弃) 和独立的 `RedisKeyConstants`, `SmsCodeConstants` 等。
**优化方案**:
- 删除废弃的 `com.businessreviews.common.Constants` 类。

---

## 📊 P3: 交互体验优化 (后端支持)

### 11. 对话式 AI 响应支持 (SSE)
**现状**: AI 创作和回复目前看似是同步或简单的异步事件。
**建议**: 为了获得 ChatGPT 式的打字机体验，后端 Controller 应提供 `SseEmitter` 接口（Server-Sent Events），支持流式输出 AI 生成的内容，而不是等待生成完毕再一次性返回。

---

## 📋 执行路线图

1.  **立即执行**: 重构 `AppNoteService` 等，修复 N+1 问题。
2.  **本周内**: 补充 Redis 缓存逻辑，删除废弃常量类。
3.  **下个迭代**: 解决 WebSocket 分布式问题，迁移 Geo 搜索逻辑。
