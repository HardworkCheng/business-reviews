# N+1 查询问题优化指南

> **文档版本**: 1.0  
> **更新日期**: 2026-01-07  
> **作者**: 系统优化团队

---

## 📋 目录

1. [问题背景](#问题背景)
2. [问题定义：什么是 N+1 查询问题](#问题定义什么是-n1-查询问题)
3. [问题诊断：如何发现 N+1 问题](#问题诊断如何发现-n1-问题)
4. [解决方案：In-Memory Map Assembly 模式](#解决方案in-memory-map-assembly-模式)
5. [优化实践案例](#优化实践案例)
   - [案例一：优惠券列表查询优化](#案例一优惠券列表查询优化)
   - [案例二：收藏列表查询优化](#案例二收藏列表查询优化)
   - [案例三：浏览历史查询优化](#案例三浏览历史查询优化)
   - [案例四：粉丝列表查询优化](#案例四粉丝列表查询优化)
6. [优化效果总结](#优化效果总结)
7. [最佳实践与开发规范](#最佳实践与开发规范)

---

## 问题背景

在本项目的测试过程中，我们发现**优惠券页面加载时间长达 5 秒**，严重影响用户体验。经过排查，根本原因是后端存在严重的 **N+1 查询问题**。

由于后端 MySQL 数据库部署在云服务器上，每次数据库查询的网络往返延迟约为 **100-200ms**。当存在 N+1 问题时，查询次数会随数据量线性增长，导致响应时间剧增。

---

## 问题定义：什么是 N+1 查询问题

### 定义

N+1 查询问题是指：在查询一个主列表（1 次查询）后，需要为列表中的每一条记录单独查询关联数据（N 次查询），总共执行 **N+1 次** 数据库查询。

### 图解

```
┌─────────────────────────────────────────────────────────────────┐
│                     N+1 查询问题示意图                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   第 1 次查询: SELECT * FROM coupons LIMIT 20                   │
│       ↓ 返回 20 条优惠券                                         │
│                                                                  │
│   第 2 次查询: SELECT * FROM shops WHERE id = 1                 │ ← 为第1张优惠券查询商家
│   第 3 次查询: SELECT * FROM shops WHERE id = 2                 │ ← 为第2张优惠券查询商家
│   第 4 次查询: SELECT * FROM shops WHERE id = 3                 │ ...
│   ...                                                            │
│   第 21 次查询: SELECT * FROM shops WHERE id = 20               │ ← 为第20张优惠券查询商家
│                                                                  │
│   第 22 次查询: SELECT COUNT(*) FROM user_coupons WHERE ...     │ ← 检查第1张是否已领取
│   第 23 次查询: SELECT COUNT(*) FROM user_coupons WHERE ...     │ ← 检查第2张是否已领取
│   ...                                                            │
│   第 41 次查询: SELECT COUNT(*) FROM user_coupons WHERE ...     │ ← 检查第20张是否已领取
│                                                                  │
│   总计: 1 + 20 + 20 = 41 次查询                                  │
│   预计耗时: 41 × 100ms = 4100ms ≈ 4秒                            │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 典型代码特征

N+1 问题通常出现在以下代码模式中：

```java
// ❌ 错误示例：在循环中查询数据库
List<CouponDO> coupons = couponMapper.selectPage(page, wrapper).getRecords();

for (CouponDO coupon : coupons) {
    // 每次循环都执行一次数据库查询！
    ShopDO shop = shopMapper.selectById(coupon.getShopId());  // N 次查询
    Long count = userCouponMapper.selectCount(wrapper);        // N 次查询
    // ...
}
```

或在 Stream 的 map 操作中：

```java
// ❌ 错误示例：在 stream.map() 中查询数据库
List<Map<String, Object>> list = coupons.stream()
    .map(c -> {
        ShopDO shop = shopMapper.selectById(c.getShopId());  // N 次查询！
        // ...
    })
    .collect(Collectors.toList());
```

---

## 问题诊断：如何发现 N+1 问题

### 1. 代码审查标志

在代码中搜索以下模式：

```bash
# 在循环中调用 selectById
grep -r "for.*{" --include="*.java" | xargs grep -l "selectById"

# 在 stream.map 中调用 Mapper
grep -r "\.map\(.*->.*Mapper\." --include="*.java"
```

### 2. SQL 日志分析

在 `application.yml` 中开启 SQL 日志：

```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

如果看到大量重复的 `SELECT ... WHERE id = ?` 语句，很可能存在 N+1 问题。

### 3. 性能监控

- 接口响应时间 > 1 秒
- 数据库连接池频繁耗尽
- 云服务器数据库 CPU 使用率异常高

---

## 解决方案：In-Memory Map Assembly 模式

### 核心思想

1. **收集所有需要的 ID**：遍历主列表，收集所有关联数据的 ID
2. **批量查询**：使用 `IN` 条件一次性查询所有关联数据
3. **构建 Map**：将批量查询结果转换为 `Map<ID, Entity>`
4. **内存组装**：遍历主列表时，从 Map 中获取关联数据（O(1) 复杂度）

### 模式图解

```
┌─────────────────────────────────────────────────────────────────┐
│                 In-Memory Map Assembly 模式                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   第 1 步: 查询主列表                                             │
│   SELECT * FROM coupons LIMIT 20                                │
│       ↓ 返回 20 条优惠券，收集 shopId: [1, 2, 3, ..., 15]       │
│                                                                  │
│   第 2 步: 批量查询商家                                          │
│   SELECT * FROM shops WHERE id IN (1, 2, 3, ..., 15)            │
│       ↓ 返回 15 个商家，构建 Map<shopId, ShopDO>                 │
│                                                                  │
│   第 3 步: 批量查询用户领取状态                                   │
│   SELECT * FROM user_coupons WHERE user_id = ? AND coupon_id IN (...)│
│       ↓ 返回已领取列表，构建 Set<couponId>                       │
│                                                                  │
│   第 4 步: 内存组装（零数据库查询）                               │
│   for (coupon : coupons) {                                      │
│       shop = shopMap.get(coupon.getShopId());  // O(1) 内存访问 │
│       claimed = claimedSet.contains(coupon.getId());            │
│   }                                                              │
│                                                                  │
│   总计: 1 + 1 + 1 = 3 次查询                                     │
│   预计耗时: 3 × 100ms = 300ms ≈ 0.3秒                            │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 通用代码模板

```java
public PageResult<ResponseVO> getList(Integer pageNum, Integer pageSize) {
    // 1. 查询主列表
    Page<MainDO> page = new Page<>(pageNum, pageSize);
    Page<MainDO> mainPage = mainMapper.selectPage(page, wrapper);
    List<MainDO> mainList = mainPage.getRecords();
    
    if (mainList.isEmpty()) {
        return PageResult.empty(pageNum, pageSize);
    }

    // 2. 收集所有关联 ID
    Set<Long> relatedIds = mainList.stream()
            .map(MainDO::getRelatedId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    // 3. 批量查询关联数据，构建 Map
    Map<Long, RelatedDO> relatedMap = new HashMap<>();
    if (!relatedIds.isEmpty()) {
        List<RelatedDO> relatedList = relatedMapper.selectBatchIds(relatedIds);
        relatedMap = relatedList.stream()
                .collect(Collectors.toMap(RelatedDO::getId, r -> r, (a, b) -> a));
    }

    // 4. 内存组装响应数据
    final Map<Long, RelatedDO> finalRelatedMap = relatedMap;
    
    List<ResponseVO> resultList = mainList.stream()
            .map(main -> {
                ResponseVO vo = new ResponseVO();
                vo.setId(main.getId());
                
                // 从 Map 获取关联数据（O(1) 复杂度，无数据库查询）
                RelatedDO related = finalRelatedMap.get(main.getRelatedId());
                if (related != null) {
                    vo.setRelatedName(related.getName());
                }
                
                return vo;
            })
            .collect(Collectors.toList());

    return PageResult.of(resultList, mainPage.getTotal(), pageNum, pageSize);
}
```

---

## 优化实践案例

### 案例一：优惠券列表查询优化

**文件位置**: `backend-business-reviews-web/src/main/java/com/businessreviews/web/app/CouponController.java`

**优化方法**: `getAvailableCoupons()`, `getCouponList()`

#### 优化前代码

```java
public Result<PageResult<Map<String, Object>>> getAvailableCoupons(...) {
    Page<CouponDO> couponPage = couponMapper.selectPage(page, wrapper);
    
    // ❌ N+1 问题：每张优惠券调用一次 convertToCouponResponse
    List<Map<String, Object>> list = couponPage.getRecords().stream()
            .map(c -> convertToCouponResponse(c, userId))  // 内部有多次数据库查询
            .collect(Collectors.toList());
    
    return Result.success(PageResult.of(list, ...));
}

// convertToCouponResponse 内部的 N+1 查询
private Map<String, Object> convertToCouponResponse(CouponDO coupon, Long userId) {
    // ...
    
    // ❌ 每张优惠券查询一次商家
    if (coupon.getShopId() != null) {
        ShopDO shop = shopMapper.selectById(coupon.getShopId());
        // ...
    }

    // ❌ 每张优惠券查询一次领取状态
    if (userId != null) {
        Long count = userCouponMapper.selectCount(wrapper);
        // ...
    }
    
    return map;
}
```

#### 优化后代码

```java
public Result<PageResult<Map<String, Object>>> getAvailableCoupons(...) {
    Page<CouponDO> couponPage = couponMapper.selectPage(page, wrapper);
    List<CouponDO> coupons = couponPage.getRecords();
    
    if (coupons.isEmpty()) {
        return Result.success(PageResult.of(new ArrayList<>(), 0L, pageNum, pageSize));
    }

    // ✅ 第1步：收集所有 shopId
    Set<Long> shopIds = coupons.stream()
            .map(CouponDO::getShopId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    
    // ✅ 第2步：批量查询商家（1次查询）
    Map<Long, ShopDO> shopMap = new HashMap<>();
    if (!shopIds.isEmpty()) {
        List<ShopDO> shops = shopMapper.selectBatchIds(shopIds);
        shopMap = shops.stream()
                .collect(Collectors.toMap(ShopDO::getId, s -> s, (a, b) -> a));
    }
    
    // ✅ 第3步：批量查询用户已领取的优惠券（1次查询）
    Long userId = UserContext.getUserId();
    Set<Long> claimedCouponIds = new HashSet<>();
    if (userId != null) {
        List<Long> couponIds = coupons.stream()
                .map(CouponDO::getId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<UserCouponDO> ucWrapper = new LambdaQueryWrapper<>();
        ucWrapper.eq(UserCouponDO::getUserId, userId)
                 .in(UserCouponDO::getCouponId, couponIds);
        List<UserCouponDO> userCoupons = userCouponMapper.selectList(ucWrapper);
        claimedCouponIds = userCoupons.stream()
                .map(UserCouponDO::getCouponId)
                .collect(Collectors.toSet());
    }
    
    // ✅ 第4步：内存组装（0次数据库查询）
    final Map<Long, ShopDO> finalShopMap = shopMap;
    final Set<Long> finalClaimedIds = claimedCouponIds;
    
    List<Map<String, Object>> list = coupons.stream()
            .map(c -> convertToCouponResponseOptimized(c, userId, finalShopMap, finalClaimedIds))
            .collect(Collectors.toList());

    return Result.success(PageResult.of(list, couponPage.getTotal(), pageNum, pageSize));
}
```

#### 性能对比

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 数据库查询次数 (20条数据) | 41 次 | **3 次** | 93% ↓ |
| 预计响应时间 | ~4 秒 | **~0.3 秒** | 93% ↓ |

---

### 案例二：收藏列表查询优化

**文件位置**: `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/UserServiceImpl.java`

**优化方法**: `getMyFavorites()`

#### 优化要点

收藏列表包含两种类型：笔记(type=1) 和 商店(type=2)，需要根据类型分组批量查询。

```java
// ✅ 按类型分组收集 ID
List<Long> noteIds = favPage.getRecords().stream()
        .filter(f -> f.getType() == 1)
        .map(UserFavoriteDO::getTargetId)
        .collect(Collectors.toList());

List<Long> shopIds = favPage.getRecords().stream()
        .filter(f -> f.getType() == 2)
        .map(UserFavoriteDO::getTargetId)
        .collect(Collectors.toList());

// ✅ 分别批量查询
Map<Long, NoteDO> noteMap = noteMapper.selectBatchIds(noteIds).stream()
        .collect(Collectors.toMap(NoteDO::getId, n -> n, (a, b) -> a));

Map<Long, ShopDO> shopMap = shopMapper.selectBatchIds(shopIds).stream()
        .collect(Collectors.toMap(ShopDO::getId, s -> s, (a, b) -> a));
```

---

### 案例三：浏览历史查询优化

**文件位置**: `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/UserServiceImpl.java`

**优化方法**: `getBrowseHistory()`

#### 优化要点

浏览历史需要关联：
1. 笔记信息
2. 笔记作者信息（二级关联）
3. 商店信息

优化策略：级联批量查询

```java
// ✅ 第1步：批量查询笔记
List<NoteDO> notes = noteMapper.selectBatchIds(noteIds);
Map<Long, NoteDO> noteMap = notes.stream()
        .collect(Collectors.toMap(NoteDO::getId, n -> n, (a, b) -> a));

// ✅ 第2步：收集所有作者ID（从笔记中提取）
Set<Long> authorIds = notes.stream()
        .map(NoteDO::getUserId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

// ✅ 第3步：批量查询作者信息
Map<Long, UserDO> authorMap = userMapper.selectBatchIds(authorIds).stream()
        .collect(Collectors.toMap(UserDO::getId, u -> u, (a, b) -> a));
```

---

### 案例四：粉丝列表查询优化

**文件位置**: `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/UserServiceImpl.java`

**优化方法**: `getFollowerList()`

#### 优化要点

粉丝列表需要显示"是否已回关"状态，这导致原代码在循环中调用 `isFollowing()`。

优化策略：批量查询关注关系。

```java
// ✅ 批量查询当前用户是否回关了这些粉丝
LambdaQueryWrapper<UserFollowDO> followBackWrapper = new LambdaQueryWrapper<>();
followBackWrapper.eq(UserFollowDO::getUserId, userId)
        .in(UserFollowDO::getFollowUserId, followerUserIds);

List<UserFollowDO> followBackList = userFollowMapper.selectList(followBackWrapper);

// ✅ 构建 Set 用于 O(1) 判断
Set<Long> followingSet = followBackList.stream()
        .map(UserFollowDO::getFollowUserId)
        .collect(Collectors.toSet());

// ✅ 组装时使用 Set.contains()
item.setFollowing(followingSet.contains(user.getId()));
```

---

## 优化效果总结

| 文件 | 方法 | 优化前查询次数 | 优化后查询次数 | 性能提升 |
|------|------|--------------|--------------|---------|
| `CouponController` | `getAvailableCoupons` | 41 次 | **3 次** | ~93% ↓ |
| `CouponController` | `getCouponList` | 41 次 | **3 次** | ~93% ↓ |
| `UserServiceImpl` | `getMyFavorites` | 21 次 | **3 次** | ~86% ↓ |
| `UserServiceImpl` | `getBrowseHistory` | 61 次 | **4 次** | ~93% ↓ |
| `UserServiceImpl` | `getFollowingList` | 21 次 | **2 次** | ~90% ↓ |
| `UserServiceImpl` | `getFollowerList` | 41 次 | **3 次** | ~93% ↓ |
| `MessageServiceImpl` | `getConversations` | 21 次 | **2 次** | ~90% ↓ |
| `MessageServiceImpl` | `getChatHistory` | 21 次 | **2 次** | ~90% ↓ |
| `MessageServiceImpl` | `getNotifications` | 41 次 | **3 次** | ~93% ↓ |

> **注**: 以上数据基于每页 20 条数据计算

---

## 最佳实践与开发规范

### ✅ DO（推荐做法）

1. **优先使用批量查询**
   ```java
   // ✅ 使用 selectBatchIds
   List<ShopDO> shops = shopMapper.selectBatchIds(shopIds);
   
   // ✅ 使用 IN 条件
   wrapper.in(UserCouponDO::getCouponId, couponIds);
   ```

2. **提前检查空集合**
   ```java
   if (mainList.isEmpty()) {
       return PageResult.empty(pageNum, pageSize);
   }
   ```

3. **使用 `Objects::nonNull` 过滤空值**
   ```java
   Set<Long> ids = list.stream()
           .map(Entity::getRelatedId)
           .filter(Objects::nonNull)  // 避免 NullPointerException
           .collect(Collectors.toSet());
   ```

4. **处理 Map 重复键**
   ```java
   // 使用 (a, b) -> a 处理重复键
   .collect(Collectors.toMap(Entity::getId, e -> e, (a, b) -> a))
   ```

### ❌ DON'T（禁止做法）

1. **禁止在循环中查询数据库**
   ```java
   // ❌ 禁止
   for (Item item : items) {
       Related r = relatedMapper.selectById(item.getRelatedId());
   }
   ```

2. **禁止在 Stream.map() 中查询数据库**
   ```java
   // ❌ 禁止
   items.stream().map(i -> {
       return relatedMapper.selectById(i.getRelatedId());
   });
   ```

3. **禁止忽略空集合检查**
   ```java
   // ❌ 风险：selectBatchIds([]) 可能返回全表
   List<Shop> shops = shopMapper.selectBatchIds(emptyList);
   ```

---

## 附录：相关源码位置

| 优化文件 | 路径 |
|---------|------|
| CouponController | `backend-business-reviews-web/src/main/java/com/businessreviews/web/app/CouponController.java` |
| UserServiceImpl | `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/UserServiceImpl.java` |
| MessageServiceImpl | `backend-business-reviews-service/src/main/java/com/businessreviews/service/impl/app/MessageServiceImpl.java` |

---

> 📝 **备注**: 本优化已于 2026-01-07 完成并通过测试。如有疑问请联系后端开发团队。
