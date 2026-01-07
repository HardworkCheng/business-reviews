# 后端性能优化方案

本文档汇总了当前项目中可以进一步提升后端响应速度的优化方向，按照优先级进行划分，便于后续逐步实现。

---

## 🔴 P0 高优先级（预计性能提升最显著）

### 1. `MerchantCommentServiceImpl.convertToCommentVO()` 存在 N+1 查询
- **位置**: `MerchantCommentServiceImpl.java` 第 487‑554 行
- **问题**: 在 `convertToCommentVO` 方法中，每条评论都会单独查询用户和门店信息。
- **影响**: 20 条评论会产生 41 次 SQL 查询（1 次主查询 + 20 次用户查询 + 20 次门店查询）。
- **优化方案**: 在 `getCommentList()` 调用前统一收集所有 `userId`、`shopId`，使用 `selectBatchIds` 批量查询并构建 `Map<Long, UserDO>`、`Map<Long, ShopDO>`，随后在 `convertToCommentVO` 中直接从 Map 读取，消除 N+1。

### 2. `MerchantCommentServiceImpl.exportComments()` 导出 Excel 时的 N+1 查询
- **位置**: `MerchantCommentServiceImpl.java` 第 604‑627 行
- **问题**: 循环中逐条查询用户、门店信息。
- **影响**: 导出 100 条评论会产生 201 次 SQL 查询。
- **优化方案**: 同上，批量预加载用户、门店信息后组装导出数据。

### 3. `MerchantDashboardServiceImpl` 重复查询问题
- **位置**: `MerchantDashboardServiceImpl.java`
- **问题**:
  - `calculateTodayViews(noteIds)` 与 `calculateTodayInteractions(noteIds)` 实际查询相同笔记数据，导致重复 IO。
  - `getShopIdsByMerchant()` 未使用 `merchantId` 过滤，可能返回所有店铺。
  - `getDashboardData()` 中多次独立查询笔记、优惠券、店铺等，数据重复拉取。
- **影响**: 看板页面加载慢，尤其在数据量大时。
- **优化方案**:
  - 合并重复查询，一次性获取笔记列表后在内存中计算各指标。
  - 为 `getShopIdsByMerchant` 添加 `merchantId` 条件。
  - 使用 `CompletableFuture` 并行执行统计查询，降低整体响应时间。

---

## 🟡 P1 中优先级（数据库层优化）

### 4. 数据库索引优化
- **现状**: 已有基本索引，但高频组合查询缺少复合索引。
- **建议新增索引**:
```sql
-- notes 表：推荐流查询优化
ALTER TABLE notes ADD INDEX idx_status_type_recommend_like (status, type, is_recommend, like_count);

-- notes 表：用户笔记列表优化
ALTER TABLE notes ADD INDEX idx_user_status_created (user_id, status, created_at);

-- shop_reviews 表：商家评论列表优化
ALTER TABLE shop_reviews ADD INDEX idx_shop_status_created (shop_id, status, created_at);
ALTER TABLE shop_reviews ADD INDEX idx_shop_rating_status (shop_id, rating, status);

-- user_coupons 表：领取时间范围查询
ALTER TABLE user_coupons ADD INDEX idx_coupon_receive_time (coupon_id, receive_time);
ALTER TABLE user_coupons ADD INDEX idx_coupon_status_use_time (coupon_id, status, use_time);
```
- **收益**: 关键查询的索引命中率提升 30‑50%，响应时间显著下降。

### 5. Druid 连接池配置优化
- **位置**: `application.yml` 第 19‑29 行
- **现状**: 配置保守，最大连接数偏小。
- **建议**:
```yaml
druid:
  initial-size: 10
  min-idle: 10
  max-active: 50
  max-wait: 30000
  pool-prepared-statements: true
  max-pool-prepared-statement-per-connection-size: 20
```
- **收益**: 高并发下的吞吐量提升约 20%，连接等待时间降低。

---

## 🟢 P2 优化加分项（进阶优化）

### 6. 热点数据预热与二级缓存
- **现状**: 已有 Redis 缓存，但首次访问仍需查询数据库。
- **建议**:
  - 应用启动时预热热门分类、热门店铺 TOP100 等数据写入 Redis。
  - 引入 Caffeine 本地缓存作为二级缓存，降低 Redis 网络延迟。

### 7. 商家看板数据异步聚合
- **位置**: `MerchantDashboardServiceImpl.getDashboardData()`
- **问题**: 多个统计指标串行执行，每个都涉及数据库查询。
- **优化方案**: 使用 `CompletableFuture.allOf()` 并行执行各统计查询，预计可将看板接口响应时间降低 50‑70%。

### 8. SQL 查询监控与慢查询分析
- **现状**: 已开启 MyBatis SQL 日志。
- **建议**:
  - 开启 Druid 监控页面，设置慢查询阈值（>500ms）告警。
  - 生产环境关闭控制台日志（`log-impl: org.apache.ibatis.logging.nop.NopImpl`），仅保留慢查询日志。

### 9. 接口响应 GZIP 压缩
- **现状**: 未配置压缩。
- **建议**: 在 `application.yml` 中添加:
```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,text/html,text/xml,text/plain
    min-response-size: 1024
```
- **收益**: 网络传输体积下降 60‑80%。

### 10. 分页接口统一参数校验
- **现状**: 部分接口未限制 `pageSize` 上限，存在内存风险。
- **建议**: 在 `PageConstants` 中定义 `MAX_PAGE_SIZE = 100`，在服务层统一校验并强制限制。

---

## 📊 优化效果预估表
| 优化项 | 预计性能提升 | 实现难度 |
|-------|------------|---------|
| 商家评论 N+1 修复 | **减少 90%+ 数据库查询** | ⭐⭐ |
| 看板数据合并查询 | **减少 60% 数据库查询** | ⭐⭐ |
| 并行统计查询 | **响应时间降低 50‑70%** | ⭐⭐⭐ |
| 数据库索引优化 | **查询速度提升 30‑50%** | ⭐ |
| 连接池调优 | **高并发吞吐提升 20%** | ⭐ |
| GZIP 压缩 | **网络传输减少 60‑80%** | ⭐ |

以上即为后端性能优化的完整方案，您可以根据业务优先级逐步落地。祝开发顺利！
